package net.deepthought.communication;

import net.deepthought.Application;
import net.deepthought.communication.listener.MessagesReceiverListener;
import net.deepthought.communication.messages.AskForDeviceRegistrationRequest;
import net.deepthought.communication.messages.AskForDeviceRegistrationResponseMessage;
import net.deepthought.communication.messages.CaptureImageOrDoOcrRequest;
import net.deepthought.communication.messages.CaptureImageResultResponse;
import net.deepthought.communication.messages.GenericRequest;
import net.deepthought.communication.messages.OcrResultResponse;
import net.deepthought.communication.messages.Request;
import net.deepthought.communication.messages.ResponseValue;
import net.deepthought.communication.messages.StopCaptureImageOrDoOcrRequest;
import net.deepthought.communication.model.ConnectedDevice;
import net.deepthought.communication.model.DoOcrConfiguration;
import net.deepthought.data.contentextractor.ocr.CaptureImageResult;
import net.deepthought.data.persistence.deserializer.DeserializationResult;
import net.deepthought.data.persistence.json.JsonIoJsonHelper;
import net.deepthought.data.persistence.serializer.SerializationResult;
import net.deepthought.util.StringUtils;
import net.deepthought.util.file.FileUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

/**
 * Created by ganymed on 20/08/15.
 */
public class MessagesReceiver extends NanoHTTPD {

  private final static Logger log = LoggerFactory.getLogger(MessagesReceiver.class);


  protected MessagesReceiverListener listener;

  protected IDeepThoughtsConnector connector;


  public MessagesReceiver(int port, MessagesReceiverListener listener) {
    super(port);
    this.listener = listener;

    this.connector = Application.getDeepThoughtsConnector();
  }

  public void unsetListener() {
    this.listener = null;
  }

  @Override
  public Response serve(IHTTPSession session) {
    if(isDeepThoughtMessage(session))
      return respondToRequest(session);

    log.debug("Don't know how to handle Request of Uri " + session.getUri());
    return super.serve(session);
  }

  private boolean isDeepThoughtMessage(IHTTPSession session) {
    return session.getUri().startsWith(Addresses.DeepThoughtUriPart) && isValidMethod(extractMethodName(session.getUri()));
  }

  protected String extractMethodName(String uri) {
    if(uri.startsWith(Addresses.DeepThoughtUriPart))
      uri = uri.substring(Addresses.DeepThoughtUriPart.length());

    if(uri.contains("?")) // remove parameters
      uri = uri.substring(0, uri.indexOf('?'));

    return uri;
  }

  protected boolean isValidMethod(String methodName) {
    return Addresses.isValidMethod(methodName);
  }

  protected Response respondToRequest(IHTTPSession session) {
    String methodName = extractMethodName(session.getUri());
    log.debug("Received " + methodName + " message");

    switch(methodName) {
      case Addresses.AskForDeviceRegistrationMethodName:
        return respondToAskForDeviceRegistrationRequest(session);
      case Addresses.SendAskForDeviceRegistrationResponseMethodName:
        return respondToSendAskForDeviceRegistrationResponse(session);
      case Addresses.NotifyRemoteWeHaveConnectedMethodName:
        return respondToNotifyRemoteWeHaveConnectedMessage(session);
      case Addresses.HeartbeatMethodName:
        return respondToHeartbeatMessage(session);
      case Addresses.StartCaptureImageAndDoOcrMethodName:
        return respondToStartCaptureImageAndDoOcrRequest(session);
      case Addresses.CaptureImageResultMethodName:
        return respondToCaptureImageResultResponse(session);
      case Addresses.OcrResultMethodName:
        return respondToOcrResultResponse(session);
      case Addresses.StopCaptureImageAndDoOcrMethodName:
        return respondToStopCaptureImageAndDoOcrRequest(session);
    }

    return createResponse(Response.Status.NOT_FOUND, new net.deepthought.communication.messages.Response(ResponseValue.Error, "Method not found"));
  }


  protected Response respondToAskForDeviceRegistrationRequest(IHTTPSession session) {
    AskForDeviceRegistrationRequest request = (AskForDeviceRegistrationRequest)parseRequestBody(session, AskForDeviceRegistrationRequest.class);

    listener.registerDeviceRequestRetrieved(request);

    if(connector.isRegistrationServerRunning()) {
      return createResponse(net.deepthought.communication.messages.Response.OK);
    }
    else
      return createResponse(Response.Status.FORBIDDEN, net.deepthought.communication.messages.Response.Denied);
  }

  protected Response respondToNotifyRemoteWeHaveConnectedMessage(IHTTPSession session) {
    GenericRequest<ConnectedDevice> message = (GenericRequest<ConnectedDevice>)parseRequestBody(session, GenericRequest.class);

    listener.notifyRegisteredDeviceConnected(message.getRequestBody());

    return createResponse(net.deepthought.communication.messages.Response.OK);
  }

  protected Response respondToHeartbeatMessage(IHTTPSession session) {
    GenericRequest<ConnectedDevice> message = (GenericRequest<ConnectedDevice>)parseRequestBody(session, GenericRequest.class);

    listener.notifyRegisteredDeviceConnected(message.getRequestBody());

    return createResponse(net.deepthought.communication.messages.Response.OK);
  }

  protected Response respondToSendAskForDeviceRegistrationResponse(IHTTPSession session) {
    AskForDeviceRegistrationResponseMessage message = (AskForDeviceRegistrationResponseMessage)parseRequestBody(session, AskForDeviceRegistrationResponseMessage.class);

    listener.askForDeviceRegistrationResponseReceived(message);

    return createResponse(net.deepthought.communication.messages.Response.OK);
  }


  protected Response respondToStartCaptureImageAndDoOcrRequest(IHTTPSession session) {
    CaptureImageOrDoOcrRequest request = null;
    try {
      request = parseStartCaptureImageAndDoOcrRequestBody(session);
    } catch(Exception ex) {
      log.error("Could not decode StartCaptureImageAndDoOcrRequest Post Body", ex);
      return createResponse(Response.Status.BAD_REQUEST, net.deepthought.communication.messages.Response.Denied);
    }

    listener.startCaptureImageOrDoOcr(request);

    return createResponse(net.deepthought.communication.messages.Response.OK);
  }

  protected Response respondToCaptureImageResultResponse(IHTTPSession session) {
    log.debug("Parsing CaptureImageResultResponse ...");
    CaptureImageResultResponse request = null;
    try {
      request = parseCaptureImageResultResponseRequestBody(session);
      log.debug("Parsing done");
    } catch(Exception ex) {
      log.error("Could not decode CaptureImageResultResponse Post Body", ex);
      return createResponse(Response.Status.BAD_REQUEST, net.deepthought.communication.messages.Response.Denied);
    }

    listener.captureImageResult(request);

    return createResponse(net.deepthought.communication.messages.Response.OK);
  }

  protected Response respondToOcrResultResponse(IHTTPSession session) {
    OcrResultResponse request = (OcrResultResponse)parseRequestBody(session, OcrResultResponse.class);

    listener.ocrResult(request);

    return createResponse(net.deepthought.communication.messages.Response.OK);
  }

  protected Response respondToStopCaptureImageAndDoOcrRequest(IHTTPSession session) {
    StopCaptureImageOrDoOcrRequest request = (StopCaptureImageOrDoOcrRequest)parseRequestBody(session, StopCaptureImageOrDoOcrRequest.class);

    listener.stopCaptureImageOrDoOcr(request);

    return createResponse(net.deepthought.communication.messages.Response.OK);
  }


  protected Request parseRequestBody(IHTTPSession session, Class<? extends Request> requestClass) {
    String messageBody = getMessageBody(session);

    if(messageBody != null) {
      log.debug("Deserializing received message body ...");
      DeserializationResult deserializationResult = JsonIoJsonHelper.parseJsonString(messageBody, requestClass);
      log.debug("Deserializing done");
      if(deserializationResult.successful())
        return (Request)deserializationResult.getResult();
    }

    return null;
  }

  protected String getMessageBody(IHTTPSession session) {
    log.debug("Extracting received message body ...");
    Map<String, String> bodyValues = new HashMap<>();
    try {
      session.parseBody(bodyValues);
    }
    catch(Exception ex) {
      log.error("Could not parse session's body" + session, ex);
      return null;
    }

    log.debug("Extracting done");
    if(bodyValues.size() == 1) {
      return new ArrayList<String>(bodyValues.values()).get(0);
    }

    return null;
  }

  protected CaptureImageOrDoOcrRequest parseStartCaptureImageAndDoOcrRequestBody(IHTTPSession session) throws Exception {
    if(session.isMultipartMessage() == false) // a CaptureImageOrDoOcrRequest without ImageData. Just a normal Request Body, no Multipart Request Body
      return (CaptureImageOrDoOcrRequest)parseRequestBody(session, CaptureImageOrDoOcrRequest.class);

    return parseMultiPartStartCaptureImageAndDoOcrRequestBody(session);
  }

  protected CaptureImageOrDoOcrRequest parseMultiPartStartCaptureImageAndDoOcrRequestBody(IHTTPSession session) throws Exception {
    Map<String, String> partFiles = parseMultipartRequestBody(session);

    String address = getAddressFromMultipartRequest(partFiles);
    int port = getPortFromMultipartRequest(partFiles);

    DoOcrConfiguration configuration = parseConfigurationFromStartCaptureImageAndDoOcrRequestBody(partFiles);

    return new CaptureImageOrDoOcrRequest(address, port, configuration);
  }

  protected DoOcrConfiguration parseConfigurationFromStartCaptureImageAndDoOcrRequestBody(Map<String, String> partFiles) throws IOException {
    DoOcrConfiguration configuration = null;
    String imageFileUri = null;

    for(String partName : partFiles.keySet()) {
      String partFilename = partFiles.get(partName);

      if(ConnectorMessagesCreator.DoOcrMultipartKeyConfiguration.equals(partName)) {
        String json = FileUtils.readTextFile(new File(partFilename));
        DeserializationResult<DoOcrConfiguration> result = JsonIoJsonHelper.parseJsonString(json, DoOcrConfiguration.class);
        if(result.successful())
          configuration = result.getResult();
      }
      else if(ConnectorMessagesCreator.DoOcrMultipartKeyImage.equals(partName)) {
        // as NanoHTTPD deletes all temp file as soon as message is handled (soon after this method returns)
        // copy Image file to another temp file
        // TODO: why does it have to be saved to a public folder (e.g. SD Card) on Android, why isn't sufficient anymore to store it to DeepThought's Cache (Android 4.3 phanomena
        File tempFile = FileUtils.createTempFile();
        tempFile.deleteOnExit();
        FileUtils.moveFile(new File(partFilename), tempFile);
//        FileUtils.copyFile(new File(partFilename), tempFile);
        imageFileUri = tempFile.getAbsolutePath();
      }
    }

    if(configuration != null)
      configuration.setImageUri(imageFileUri);

    return configuration;
  }

  protected CaptureImageResultResponse parseCaptureImageResultResponseRequestBody(IHTTPSession session) throws Exception {
    Map<String, String> partFiles = parseMultipartRequestBody(session);

//    String address = getAddressFromMultipartRequest(partFiles);
//    int port = getPortFromMultipartRequest(partFiles);
    int messageId = getMessageIdFromMultipartRequest(partFiles);

    CaptureImageResult captureImageResult = parseCaptureImageResultFromCaptureImageResultResponseRequestBody(partFiles);

    return new CaptureImageResultResponse(captureImageResult, messageId);
  }

  protected CaptureImageResult parseCaptureImageResultFromCaptureImageResultResponseRequestBody(Map<String, String> partFiles) throws IOException {
    CaptureImageResult captureImageResult = null;
    String imageFileUri = null;

    for(String partName : partFiles.keySet()) {
      String partFilename = partFiles.get(partName);

      if(ConnectorMessagesCreator.CaptureImageResultMultipartKeyResponse.equals(partName)) {
        String json = FileUtils.readTextFile(new File(partFilename));
        DeserializationResult<CaptureImageResult> result = JsonIoJsonHelper.parseJsonString(json, CaptureImageResult.class);
        if(result.successful())
          captureImageResult = result.getResult();
      }
      else if(ConnectorMessagesCreator.CaptureImageResultMultipartKeyImage.equals(partName)) {
        // as NanoHTTPD deletes all temp file as soon as message is handled (soon after this method returns)
        // copy Image file to another temp file
        // TODO: why does it have to be saved to a public folder (e.g. SD Card) on Android, why isn't sufficient anymore to store it to DeepThought's Cache (Android 4.3 phanomena
        File tempFile = FileUtils.createTempFile();
        tempFile.deleteOnExit();
        FileUtils.moveFile(new File(partFilename), tempFile);
//        FileUtils.copyFile(new File(partFilename), tempFile);
        imageFileUri = tempFile.getAbsolutePath();
      }
    }

    if(captureImageResult != null)
      captureImageResult.setImageUri(imageFileUri);

    return captureImageResult;
  }

  protected String getAddressFromMultipartRequest(Map<String, String> partFiles) throws IOException {
    if(partFiles.containsKey(ConnectorMessagesCreator.MultipartKeyAddress)) {
      String partFilename = partFiles.get(ConnectorMessagesCreator.MultipartKeyAddress);
      return FileUtils.readTextFile(new File(partFilename));
    }

    return null;
  }

  protected int getPortFromMultipartRequest(Map<String, String> partFiles) throws IOException {
    if(partFiles.containsKey(ConnectorMessagesCreator.MultipartKeyPort)) {
      String partFilename = partFiles.get(ConnectorMessagesCreator.MultipartKeyPort);

      String portString = FileUtils.readTextFile(new File(partFilename));
      if(StringUtils.isNotNullOrEmpty(portString))
        return Integer.parseInt(portString);
    }

    return 0;
  }

  protected int getMessageIdFromMultipartRequest(Map<String, String> partFiles) throws IOException {
    if(partFiles.containsKey(ConnectorMessagesCreator.MultipartKeyMessageId)) {
      String partFilename = partFiles.get(ConnectorMessagesCreator.MultipartKeyMessageId);

      String portString = FileUtils.readTextFile(new File(partFilename));
      if(StringUtils.isNotNullOrEmpty(portString))
        return Integer.parseInt(portString);
    }

    return -1;
  }

  protected Map<String, String> parseMultipartRequestBody(IHTTPSession session) throws Exception {
//    String debug = getMessageBody(session);
    File tempFile = FileUtils.createTempFile(); // TODO: if filename is set, apply it to file
    RandomAccessFile randomAccessFile = new RandomAccessFile(tempFile, "rw");

    session.storeIncomingDataToBuffer(randomAccessFile, 1024);

    randomAccessFile.seek(0);
    ByteBuffer buffer = randomAccessFile.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, tempFile.length());

    Map<String, String> files = new HashMap<>();
    session.decodeMultipartFormData(buffer, files);

    buffer.clear();
    randomAccessFile.close();
    return files;
  }


  protected Response createResponse(net.deepthought.communication.messages.Response response) {
    return createResponse(Response.Status.OK, response);
  }

  protected Response createResponse(Response.IStatus status, net.deepthought.communication.messages.Response response) {
    String serializedResponse = serializeResponse(response);
    if(serializedResponse != null)
      return createResponse(status, serializedResponse, Constants.JsonMimeType, false);
    return null;
  }

  protected Response createResponse(String response) {
    return createResponse(Response.Status.OK, response);
  }

  protected Response createResponse(Response.IStatus status, String response) {
    return createResponse(status, response, Constants.JsonMimeType, false);
  }

  protected Response createResponse(Response.IStatus status, String response, String mimeType, boolean sendChunked) {

//    if(sendChunked)
//      return newChunkedResponse(status, mimeType, responseStream);
//    else
      return newFixedLengthResponse(status, mimeType, response);
  }


  protected String serializeResponse(net.deepthought.communication.messages.Response response) {
    SerializationResult serializationResult = JsonIoJsonHelper.generateJsonString(response);
    if(serializationResult.successful())
      return serializationResult.getSerializationResult();

    return null;
  }

}

package net.deepthought.communication.messages;

import net.deepthought.communication.ConnectorMessagesCreator;
import net.deepthought.communication.Constants;
import net.deepthought.communication.listener.AsynchronousResponseListener;
import net.deepthought.communication.listener.MessagesReceiverListener;
import net.deepthought.communication.messages.request.MultipartRequest;
import net.deepthought.communication.messages.request.Request;
import net.deepthought.communication.messages.request.RequestWithAsynchronousResponse;
import net.deepthought.communication.messages.response.ResponseCode;
import net.deepthought.communication.messages.response.ResponseToAsynchronousRequest;
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
import java.util.List;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

/**
 * Created by ganymed on 20/08/15.
 */
public class MessagesReceiver extends NanoHTTPD {

  private final static Logger log = LoggerFactory.getLogger(MessagesReceiver.class);


  protected MessagesReceiverConfig config;

  protected MessagesReceiverListener listener;


  public MessagesReceiver(MessagesReceiverConfig config, MessagesReceiverListener listener) {
    super(config.getPort());
    this.config = config;
    this.listener = listener;
  }

  public void unsetListener() {
    this.listener = null;
  }

  @Override
  public Response serve(IHTTPSession session) {
    String methodName = extractMethodName(session.getUri());
    if(isValidUri(session.getUri(), methodName)) {
      log.debug("Received " + methodName + " message");
      return respondToMessage(session, methodName);
    }

    log.debug("Don't know how to handle Request of Uri " + session.getUri());
    return createInvalidUrlResponse();
  }

  protected boolean isValidUri(String relativeUri, String methodName) {
    return relativeUri.startsWith(config.getUriPathStart()) && config.isAllowedMethodName(methodName);
  }

  protected String extractMethodName(String relativeUri) {
    String methodName = relativeUri;

    if(relativeUri.startsWith(config.getUriPathStart()))
      methodName = relativeUri.substring(config.getUriPathStart().length());

    if(methodName.contains("?")) // remove parameters
      methodName = methodName.substring(0, methodName.indexOf('?'));

    return methodName;
  }


  protected Response respondToMessage(IHTTPSession session, String methodName) {
    try {
      Request request = parseRequest(session, methodName);

      return handleRequest(methodName, request);
    } catch(Exception ex) {
      return createCouldNotDeserializeRequestResponse();
    }
  }

  protected Response handleRequest(String methodName, Request request) {
    try {
      if(request instanceof ResponseToAsynchronousRequest) {
        handleResponseToAsynchronousRequest((ResponseToAsynchronousRequest) request);
      }

      if(listener.messageReceived(methodName, request)) {
        return createOkResponse();
      }

      return createResponse(Response.Status.BAD_REQUEST, net.deepthought.communication.messages.response.Response.Denied);
    } catch(Exception ex) {
      return createCouldNotHandleRequestResponse();
    }
  }

  protected void handleResponseToAsynchronousRequest(ResponseToAsynchronousRequest responseToRequest) {
    try {
      int messageId = responseToRequest.getRequestMessageId();
      AsynchronousResponseListenerManager listenerManager = config.getListenerManager();

      AsynchronousResponseListener listener = listenerManager.getListenerForMessageId(messageId);
      RequestWithAsynchronousResponse originalRequest = listenerManager.getRequestWithAsynchronousResponseForMessageId(messageId);

      if (responseToRequest.isDone()) {
        listenerManager.removeListenerForMessageId(messageId);
      }

      if (listener != null && originalRequest != null) {
        listener.responseReceived(originalRequest, responseToRequest);
      }
    } catch(Exception ex) {
      log.error("Could not handle ResponseToAsynchronousRequest with messageId " + responseToRequest.getRequestMessageId(), ex);
    }
  }

  protected Request parseRequest(IHTTPSession session, String methodName) throws Exception {
    Class<? extends Request> requestClass = config.getRequestClassForMethod(methodName);
    if(session.isMultipartMessage() == false) {
      return parseSinglePartRequest(session, requestClass);
    }

    List<MultipartPart> multipartPartsConfig = config.getMultipartPartsConfigForMethod(methodName);
    if(multipartPartsConfig != null) {
      return parseMultipartRequest(session, requestClass, multipartPartsConfig);
    }

    log.error("Could not find Multipart Configuration for Web Method " + methodName + " even though session says it's a Multipart Request");
    throw new Exception("No Multipart Configuration found for Web Method " + methodName + " even though session says it's a Multipart Request");
  }

  protected Request parseMultipartRequest(IHTTPSession session, Class<? extends Request> requestClass, List<MultipartPart> multipartPartsConfig) {
    try {
      Map<String, String> parts = parseMultipartRequestBody(session);
      return createMultipartRequestFromParts(parts, requestClass, multipartPartsConfig);
    } catch(Exception ex) {
      log.error("Could not parse Multipart Request", ex);
    }

    return null;
  }

  protected Request createMultipartRequestFromParts(Map<String, String> parts, Class<? extends Request> requestClass, List<MultipartPart> multipartPartsConfig) throws Exception {
    MultipartRequest request = createMultipartRequestInstance(parts, requestClass);

    parseMultipartRequestParts(parts, multipartPartsConfig, request);

    return request;
  }

  protected MultipartRequest createMultipartRequestInstance(Map<String, String> parts, Class<? extends Request> requestClass) throws IOException, InstantiationException, IllegalAccessException {
    MultipartRequest request = (MultipartRequest)requestClass.newInstance();

    request.setMessageId(getMessageIdFromMultipartRequest(parts));
    request.setAddress(getAddressFromMultipartRequest(parts));
    request.setPort(getPortFromMultipartRequest(parts));

    return request;
  }

  protected void parseMultipartRequestParts(Map<String, String> parts, List<MultipartPart> multipartPartsConfig, MultipartRequest request) throws IOException {
    for(MultipartPart part : multipartPartsConfig) {
      String partFilename = parts.get(part.getPartName());
      if(partFilename != null) {
        if(parseMultipartPart(part, partFilename)) { // TODO: if a single part throws an Exception, should then really whole method abort?
          request.addPart(part);
        }
      }
    }
  }

  protected boolean parseMultipartPart(MultipartPart part, String partFilename) throws IOException {
    Object parseResult = null;

    if(part.getType() == MultipartType.Text) {
      parseResult = parseTextualMultipartPart(part.getDataType(), partFilename);
    }
    else if(part.getType() == MultipartType.Binary) {
      parseResult = parseBinaryMultipartPart(part, partFilename);
    }

    part.setData(parseResult);
    return parseResult != null;
  }

  protected Object parseTextualMultipartPart(Class dataType, String partFilename) throws IOException {
    String json = readPartContent(partFilename);
    DeserializationResult<?> result = JsonIoJsonHelper.parseJsonString(json, dataType);
    if(result.successful())
      return result.getResult();

    return null;
  }

  protected Object parseBinaryMultipartPart(MultipartPart part, String partFilename) {
    // as NanoHTTPD deletes all temp file as soon as message is handled (soon after this method returns)
    // copy Image file to another temp file
    // TODO: why does it have to be saved to a public folder (e.g. SD Card) on Android, why isn't sufficient anymore to store it to DeepThought's Cache (Android 4.3 phanomena
    File tempFile = FileUtils.createTempFile();
    tempFile.deleteOnExit();

    FileUtils.copyFile(new File(partFilename), tempFile);

    return tempFile.getAbsolutePath();
  }


  protected Request parseSinglePartRequest(IHTTPSession session, Class<? extends Request> requestClass) {
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

  protected String getAddressFromMultipartRequest(Map<String, String> partFiles) throws IOException {
    if(partFiles.containsKey(ConnectorMessagesCreator.MultipartKeyAddress)) {
      String partFilename = partFiles.get(ConnectorMessagesCreator.MultipartKeyAddress);
      if(StringUtils.isNotNullOrEmpty(partFilename)) {
        return readPartContent(partFilename);
      }
    }

    return "";
  }

  protected int getPortFromMultipartRequest(Map<String, String> partFiles) throws IOException {
    if(partFiles.containsKey(ConnectorMessagesCreator.MultipartKeyPort)) {
      String partFilename = partFiles.get(ConnectorMessagesCreator.MultipartKeyPort);

      String portString = readPartContent(partFilename);
      if(StringUtils.isNotNullOrEmpty(portString))
        return Integer.parseInt(portString);
    }

    return 0;
  }

  protected int getMessageIdFromMultipartRequest(Map<String, String> partFiles) throws IOException {
    if(partFiles.containsKey(ConnectorMessagesCreator.MultipartKeyMessageId)) {
      String partFilename = partFiles.get(ConnectorMessagesCreator.MultipartKeyMessageId);

      String messageIdString = readPartContent(partFilename);
      if(StringUtils.isNotNullOrEmpty(messageIdString))
        return Integer.parseInt(messageIdString);
    }

    return -1;
  }

  protected String readPartContent(String partFilename) throws IOException {
    String partContent = FileUtils.readTextFile(new File(partFilename));

    if(partContent != null && partContent.startsWith("\n")) {
      partContent = partContent.substring(1);
    }

    return partContent;
  }

  protected Map<String, String> parseMultipartRequestBody(IHTTPSession session) throws Exception {
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


  protected Response createResponse(net.deepthought.communication.messages.response.Response response) {
    return createResponse(Response.Status.OK, response);
  }

  protected Response createResponse(Response.IStatus status, net.deepthought.communication.messages.response.Response response) {
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

  protected Response createOkResponse() {
    return createResponse(net.deepthought.communication.messages.response.Response.OK);
  }

  protected Response createInvalidUrlResponse() {
    return createResponse(Response.Status.NOT_FOUND, new net.deepthought.communication.messages.response.Response(ResponseCode.Error, "Method not found"));
  }

  protected Response createCouldNotDeserializeRequestResponse() {
    return createResponse(Response.Status.BAD_REQUEST, net.deepthought.communication.messages.response.Response.CouldNotDeserializeRequest);
  }

  protected Response createCouldNotHandleRequestResponse() {
    return createResponse(Response.Status.INTERNAL_ERROR, net.deepthought.communication.messages.response.Response.CouldNotHandleRequest);
  }


  protected String serializeResponse(net.deepthought.communication.messages.response.Response response) {
    SerializationResult serializationResult = JsonIoJsonHelper.generateJsonString(response);
    if(serializationResult.successful())
      return serializationResult.getSerializationResult();

    return null;
  }

}

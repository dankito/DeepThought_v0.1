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
import net.deepthought.data.persistence.deserializer.DeserializationResult;
import net.deepthought.data.persistence.json.JsonIoJsonHelper;
import net.deepthought.data.persistence.serializer.SerializationResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    AskForDeviceRegistrationResponseMessage message = (AskForDeviceRegistrationResponseMessage)parseRequestBody(session, AskForDeviceRegistrationRequest.class);

    listener.askForDeviceRegistrationResponseReceived(message);

    return createResponse(net.deepthought.communication.messages.Response.OK);
  }


  protected Response respondToStartCaptureImageAndDoOcrRequest(IHTTPSession session) {
    CaptureImageOrDoOcrRequest request = (CaptureImageOrDoOcrRequest)parseRequestBody(session, CaptureImageOrDoOcrRequest.class);

    listener.startCaptureImageOrDoOcr(request);

    return createResponse(net.deepthought.communication.messages.Response.OK);
  }

  protected Response respondToCaptureImageResultResponse(IHTTPSession session) {
    CaptureImageResultResponse request = (CaptureImageResultResponse)parseRequestBody(session, CaptureImageResultResponse.class);

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
    Map<String, String> bodyValues = new HashMap<>();
    try {
      session.parseBody(bodyValues); }
    catch(Exception ex) {
      log.error("Could not parse session's body" + session, ex);
      return null;
    }

    if(bodyValues.size() == 1) {
      String body = new ArrayList<String>(bodyValues.values()).get(0);

      DeserializationResult deserializationResult = JsonIoJsonHelper.parseJsonString(body, requestClass);
      if(deserializationResult.successful())
        return (Request)deserializationResult.getResult();
    }

    return null;
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

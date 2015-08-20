package net.deepthought.communication;

import net.deepthought.Application;
import net.deepthought.communication.messages.AskForDeviceRegistrationRequest;
import net.deepthought.communication.messages.AskForDeviceRegistrationResponse;
import net.deepthought.communication.messages.Request;
import net.deepthought.communication.model.AllowDeviceToRegisterResult;
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


  protected DeepThoughtsConnectorListener listener;


  public MessagesReceiver(int port, DeepThoughtsConnectorListener listener) {
    super(port);
    this.listener = listener;
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

    switch(methodName) {
      case Addresses.AskForDeviceRegistrationMethodName:
        return respondToAskForDeviceRegistrationRequest(session);
    }

    return null;
  }

  protected Response respondToAskForDeviceRegistrationRequest(IHTTPSession session) {
    AskForDeviceRegistrationRequest request = (AskForDeviceRegistrationRequest)parseRequestBody(session, AskForDeviceRegistrationRequest.class);

    AllowDeviceToRegisterResult userAllowsRegistration = AllowDeviceToRegisterResult.createDenyRegistrationResult();
    if(listener != null)
      userAllowsRegistration = listener.registerDeviceRequestRetrieved(request);

    if(userAllowsRegistration.allowsDeviceToRegister()) {
      return createResponse(AskForDeviceRegistrationResponse.createAllowRegistrationResponse(userAllowsRegistration.useServersUserInformation(),
          Application.getLoggedOnUser(), Application.getApplication().getLocalDevice()));
    }
    else
      return createResponse(Response.Status.FORBIDDEN, AskForDeviceRegistrationResponse.createDenyRegistrationResponse());
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

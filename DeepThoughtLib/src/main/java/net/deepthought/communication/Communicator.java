package net.deepthought.communication;

import net.deepthought.Application;
import net.deepthought.communication.listener.AskForDeviceRegistrationListener;
import net.deepthought.communication.listener.MessagesReceiverListener;
import net.deepthought.communication.listener.ResponseListener;
import net.deepthought.communication.messages.AskForDeviceRegistrationRequest;
import net.deepthought.communication.messages.AskForDeviceRegistrationResponseMessage;
import net.deepthought.communication.messages.Request;
import net.deepthought.communication.messages.Response;
import net.deepthought.communication.model.HostInfo;
import net.deepthought.data.model.User;
import net.deepthought.data.persistence.deserializer.DeserializationResult;
import net.deepthought.data.persistence.json.JsonIoJsonHelper;
import net.deepthought.data.persistence.serializer.SerializationResult;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ganymed on 20/08/15.
 */
public class Communicator {

  private final static Logger log = LoggerFactory.getLogger(Communicator.class);


  protected IDeepThoughtsConnector connector;

  protected ResponseListener responseListener;

  protected Map<AskForDeviceRegistrationRequest, AskForDeviceRegistrationListener> askForDeviceRegistrationListeners = new HashMap<>();


  public Communicator(IDeepThoughtsConnector connector, ResponseListener responseListener) {
    this.connector = connector;
    this.responseListener = responseListener;

    connector.addMessagesReceiverListener(messagesReceiverListener);
  }


  public void askForDeviceRegistration(HostInfo serverInfo, final AskForDeviceRegistrationListener listener) {
    String address = Addresses.getAskForDeviceRegistrationAddress(serverInfo.getIpAddress(), serverInfo.getPort());

    User user = Application.getLoggedOnUser();
    final AskForDeviceRegistrationRequest request = AskForDeviceRegistrationRequest.fromUserAndDevice(user, Application.getApplication().getLocalDevice());
    if(listener != null)
      askForDeviceRegistrationListeners.put(request, listener);

    sendMessageAsync(address, request, new CommunicatorResponseListener() {
      @Override
      public void responseReceived(Response communicatorResponse) {
        // user choice isn't send directly as response anymore as a) asking user is an asynchronous operation which cannot be await synchronously and b) connection may timeout when waiting
        // TODO: check if Response contains an error and notify user accordingly
        responseListener.responseReceived(request, communicatorResponse);
      }
    });
  }

  public void sendAskForDeviceRegistrationResponse(AskForDeviceRegistrationRequest request, final AskForDeviceRegistrationResponseMessage response, final ResponseListener listener) {
    String address = Addresses.getSendAskForDeviceRegistrationResponseAddress(request.getIpAddress(), request.getPort());
    response.setRequestMessageId(request.getMessageId());

    sendMessageAsync(address, response, new CommunicatorResponseListener() {
      @Override
      public void responseReceived(Response communicatorResponse) {
        responseListener.responseReceived(response, communicatorResponse);

        if(listener != null)
          listener.responseReceived(response, communicatorResponse);
      }
    });
  }

  protected void sendMessageAsync(String address, Request request, CommunicatorResponseListener listener) {
    sendMessageAsync(address, request, Response.class, listener);
  }

  protected void sendMessageAsync(final String address, final Request request, final Class<? extends Response> responseClass, final CommunicatorResponseListener listener) {
    new Thread(new Runnable() {
      @Override
      public void run() {
        Response response = sendMessage(address, request, responseClass);
        if(listener != null)
          listener.responseReceived(response);
      }
    }).start();
  }

  protected Response sendMessage(String address, Request request, Class<? extends Response> responseClass) {
    try {
      HttpClient httpClient = new DefaultHttpClient();

      HttpPost postRequest = new HttpPost(address);
      SerializationResult result = JsonIoJsonHelper.generateJsonString(request);
      if (result.successful() == false) {
        log.error("Could not generate Json from Request", result.getError()); // TODO: what to do in this case?
      }

      StringEntity postEntity = new StringEntity(result.getSerializationResult(), Constants.MessagesCharsetName);
      postEntity.setContentType(Constants.JsonMimeType);
      postRequest.setEntity(postEntity);

      HttpResponse response = httpClient.execute(postRequest);
      HttpEntity entity = response.getEntity();
      log.debug("Request Handled for url " + address + " ?: " + response.getStatusLine());

      String responseString = EntityUtils.toString(entity);
      httpClient.getConnectionManager().shutdown();

      return deserializeResponse(responseString, responseClass);
    } catch(Exception ex) {
      log.error("Could not send message to address " + address + " for Request " + request, ex);
    }

    return null;
  }

  protected Response deserializeResponse(String responseString, Class<? extends Response> responseClass) {
    DeserializationResult deserializationResult = JsonIoJsonHelper.parseJsonString(responseString, responseClass);
    if(deserializationResult.successful())
      return (Response)deserializationResult.getResult();

    return null;
  }


  protected MessagesReceiverListener messagesReceiverListener = new MessagesReceiverListener() {

    @Override
    public void registerDeviceRequestRetrieved(AskForDeviceRegistrationRequest request) {

    }

    @Override
    public void askForDeviceRegistrationResponseReceived(AskForDeviceRegistrationResponseMessage message) {
      Integer messageId = message.getRequestMessageId();

      for(AskForDeviceRegistrationRequest request : askForDeviceRegistrationListeners.keySet()) {
        if(messageId.equals(request.getMessageId())) {
          AskForDeviceRegistrationListener listener = askForDeviceRegistrationListeners.get(request);
          listener.serverResponded(message);

          askForDeviceRegistrationListeners.remove(request);
          break;
        }
      }
    }
  };

}

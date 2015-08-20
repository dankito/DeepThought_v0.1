package net.deepthought.communication;

import net.deepthought.Application;
import net.deepthought.communication.messages.AskForDeviceRegistrationRequest;
import net.deepthought.communication.messages.AskForDeviceRegistrationResponse;
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

/**
 * Created by ganymed on 20/08/15.
 */
public class Communicator {

  private final static Logger log = LoggerFactory.getLogger(Communicator.class);


  public void askForDeviceRegistration(HostInfo serverInfo, AskForDeviceRegistrationListener listener) {
    String address = Addresses.getAskForDeviceRegistrationAddress(serverInfo.getIpAddress(), serverInfo.getPort());

    User user = Application.getLoggedOnUser();
    AskForDeviceRegistrationRequest request = AskForDeviceRegistrationRequest.fromUserAndDevice(user, Application.getApplication().getLocalDevice());

    AskForDeviceRegistrationResponse response = (AskForDeviceRegistrationResponse)sendMessage(address, request, AskForDeviceRegistrationResponse.class);
    if(listener != null)
      listener.serverResponded(response);
  }

  protected Response sendMessage(String address, AskForDeviceRegistrationRequest request, Class<? extends Response> responseClass) {
    try {
      HttpClient httpClient = new DefaultHttpClient();

      HttpPost postRequest = new HttpPost(address);
      SerializationResult result = JsonIoJsonHelper.generateJsonString(request);
      if (result.successful() == false) {
        log.error("Could not generate Json from Request", result.getError()); // TODO: what to do in this case?
      }

      postRequest.setEntity(new StringEntity(result.getSerializationResult(), Constants.JsonMimeType, Constants.MessagesCharset.displayName()));

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
}

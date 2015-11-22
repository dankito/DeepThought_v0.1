package net.deepthought.communication.listener;

import net.deepthought.communication.messages.request.AskForDeviceRegistrationRequest;
import net.deepthought.communication.messages.response.AskForDeviceRegistrationResponse;

/**
 * Created by ganymed on 23/08/15.
 */
public interface CommunicatorListener extends ResponseListener {

  void serverAllowedDeviceRegistration(AskForDeviceRegistrationRequest request, AskForDeviceRegistrationResponse response);

}

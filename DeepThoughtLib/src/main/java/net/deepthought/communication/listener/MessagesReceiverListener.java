package net.deepthought.communication.listener;

import net.deepthought.communication.messages.request.Request;
import net.deepthought.communication.messages.response.AskForDeviceRegistrationResponseMessage;
import net.deepthought.communication.registration.UserDeviceRegistrationRequestListener;

/**
 * Created by ganymed on 21/08/15.
 */
public interface MessagesReceiverListener extends UserDeviceRegistrationRequestListener {

  void askForDeviceRegistrationResponseReceived(AskForDeviceRegistrationResponseMessage message);


  boolean messageReceived(String methodName, Request request);

}

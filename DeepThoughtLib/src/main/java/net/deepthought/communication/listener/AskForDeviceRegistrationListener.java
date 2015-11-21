package net.deepthought.communication.listener;

import net.deepthought.communication.messages.response.AskForDeviceRegistrationResponseMessage;

/**
 * Created by ganymed on 20/08/15.
 */
public interface AskForDeviceRegistrationListener {

  void serverResponded(AskForDeviceRegistrationResponseMessage response);

}

package net.deepthought.communication;

import net.deepthought.communication.messages.AskForDeviceRegistrationResponse;

/**
 * Created by ganymed on 20/08/15.
 */
public interface AskForDeviceRegistrationListener {

  void serverResponded(AskForDeviceRegistrationResponse response);

}

package net.deepthought.communication.registration;

import net.deepthought.communication.messages.request.AskForDeviceRegistrationRequest;

/**
 * Created by ganymed on 21/08/15.
 */
public interface UserDeviceRegistrationRequestListener {

  void registerDeviceRequestRetrieved(AskForDeviceRegistrationRequest request);

}

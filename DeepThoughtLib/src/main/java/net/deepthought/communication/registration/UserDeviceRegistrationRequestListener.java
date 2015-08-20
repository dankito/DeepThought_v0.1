package net.deepthought.communication.registration;

import net.deepthought.communication.messages.AskForDeviceRegistrationRequest;
import net.deepthought.communication.model.AllowDeviceToRegisterResult;

/**
 * Created by ganymed on 21/08/15.
 */
public interface UserDeviceRegistrationRequestListener {

  AllowDeviceToRegisterResult registerDeviceRequestRetrieved(AskForDeviceRegistrationRequest request);

}

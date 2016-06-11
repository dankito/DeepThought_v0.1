package net.deepthought.communication.registration;

import net.deepthought.communication.messages.request.AskForDeviceRegistrationRequest;
import net.deepthought.communication.model.HostInfo;

/**
 * Created by ganymed on 19/08/15.
 */
public interface IUnregisteredDevicesListener {

  void unregisteredDeviceFound(HostInfo device);

  void deviceIsAskingForRegistration(AskForDeviceRegistrationRequest request);

}

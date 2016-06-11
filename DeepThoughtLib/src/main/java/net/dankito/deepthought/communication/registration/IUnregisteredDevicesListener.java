package net.dankito.deepthought.communication.registration;

import net.dankito.deepthought.communication.messages.request.AskForDeviceRegistrationRequest;
import net.dankito.deepthought.communication.model.HostInfo;

/**
 * Created by ganymed on 19/08/15.
 */
public interface IUnregisteredDevicesListener {

  void unregisteredDeviceFound(HostInfo device);

  void deviceIsAskingForRegistration(AskForDeviceRegistrationRequest request);

}

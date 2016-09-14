package net.dankito.deepthought.communication.registration;

import net.dankito.deepthought.communication.messages.request.AskForDeviceRegistrationRequest;
import net.dankito.deepthought.communication.model.HostInfo;

/**
 * Created by ganymed on 22/11/15.
 */
public interface IRegisteredDevicesManager {
  boolean hasRegisteredDevices();

  int getRegisteredDevicesCount();

  boolean isDeviceRegistered(HostInfo info);

  boolean registerDevice(AskForDeviceRegistrationRequest response);
}

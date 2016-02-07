package net.deepthought.communication.registration;

import net.deepthought.communication.messages.request.AskForDeviceRegistrationRequest;
import net.deepthought.communication.model.ConnectedDevice;
import net.deepthought.communication.model.HostInfo;

/**
 * Created by ganymed on 22/11/15.
 */
public interface IRegisteredDevicesManager {
  boolean hasRegisteredDevices();

  int getRegisteredDevicesCount();

  boolean isDeviceRegistered(HostInfo info);

  boolean isDeviceRegistered(ConnectedDevice device);

  boolean registerDevice(AskForDeviceRegistrationRequest response, boolean useOtherSidesUserInfo);
}

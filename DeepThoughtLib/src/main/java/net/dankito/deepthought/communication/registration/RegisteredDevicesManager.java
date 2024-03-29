package net.dankito.deepthought.communication.registration;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.communication.messages.request.AskForDeviceRegistrationRequest;
import net.dankito.deepthought.communication.model.HostInfo;
import net.dankito.deepthought.data.model.DeepThoughtApplication;
import net.dankito.deepthought.data.model.Device;
import net.dankito.deepthought.data.model.User;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by ganymed on 21/08/15.
 */
public class RegisteredDevicesManager implements IRegisteredDevicesManager {


  @Override
  public boolean hasRegisteredDevices() {
    return getRegisteredDevicesCount() > 0;
  }

  @Override
  public int getRegisteredDevicesCount() {
    return getRegisteredDevices().size() - 1; // TODO: will there ever be other Devices than User's Devices in his/her own group?
  }

  protected Collection<Device> getRegisteredDevices() {
    return new ArrayList<>(Application.getLoggedOnUser().getDevices());
  }


  @Override
  public boolean isDeviceRegistered(HostInfo info) {
    User loggedOnUser = Application.getLoggedOnUser();
    if(loggedOnUser.getUniversallyUniqueId().equals(info.getUserUniqueId())) {
      Collection<Device> registeredDevices = getRegisteredDevices();
      for(Device device : registeredDevices) {
        if(device.getUniversallyUniqueId().equals(info.getDeviceUniqueId()))
          return true;
      }
    }

    return false;
  }

  @Override
  public boolean registerDevice(AskForDeviceRegistrationRequest response) {
    User loggedOnUser = Application.getLoggedOnUser();
    DeepThoughtApplication application = Application.getApplication();
    Device peerDevice = extractDeviceInformation(response);

    application.addDevice(peerDevice);
    loggedOnUser.addDevice(peerDevice);
    loggedOnUser.getUsersDefaultGroup().addDevice(peerDevice);

    return true;
  }

  protected Device extractDeviceInformation(AskForDeviceRegistrationRequest response) {
    HostInfo deviceInfo = response.getDevice();
    Device device = new Device(deviceInfo.getDeviceUniqueId(), deviceInfo.getDeviceName(),  deviceInfo.getPlatform(), deviceInfo.getOsVersion(), deviceInfo.getPlatformArchitecture());
    device.setId(deviceInfo.getDeviceDatabaseId());
    device.setDescription(deviceInfo.getDeviceDescription());
    device.setLastKnownIpAddress(response.getAddress());

    return device;
  }

}

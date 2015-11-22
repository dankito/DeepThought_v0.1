package net.deepthought.communication.helper;

import net.deepthought.communication.messages.request.AskForDeviceRegistrationRequest;
import net.deepthought.communication.model.ConnectedDevice;
import net.deepthought.communication.model.HostInfo;
import net.deepthought.communication.registration.IRegisteredDevicesManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ganymed on 22/11/15.
 */
public class TestRegisteredDevicesManager implements IRegisteredDevicesManager {

  protected List<HostInfo> registeredDevices = new ArrayList<>();


  @Override
  public boolean hasRegisteredDevices() {
    return getRegisteredDevicesCount() > 0;
  }

  @Override
  public int getRegisteredDevicesCount() {
    return registeredDevices.size();
  }

  @Override
  public boolean isDeviceRegistered(HostInfo info) {
    return registeredDevices.contains(info);
  }

  @Override
  public boolean isDeviceRegistered(ConnectedDevice device) {
    for(HostInfo info : registeredDevices) {
      if(device.getUniqueDeviceId().equals(info.getDeviceUniqueId()) && device.getAddress().equals(info.getIpAddress()) && device.getMessagesPort() == info.getPort()) {
        return true;
      }
    }

    return false;
  }

  @Override
  public boolean registerDevice(AskForDeviceRegistrationRequest response, boolean useOtherSidesUserInfo) {
    return registeredDevices.add(new HostInfo(response.getUser().getUniversallyUniqueId(), response.getUser().getUserName(), response.getDevice().getUniversallyUniqueId(),
        response.getDevice().getName(), response.getDevice().getPlatform(), response.getDevice().getOsVersion(), response.getDevice().getPlatformArchitecture(),
        response.getAddress(), response.getPort()));
  }
}

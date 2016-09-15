package net.dankito.deepthought.communication.helper;

import net.dankito.deepthought.communication.messages.request.AskForDeviceRegistrationRequest;
import net.dankito.deepthought.communication.model.HostInfo;
import net.dankito.deepthought.communication.registration.IRegisteredDevicesManager;

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
  public boolean registerDevice(AskForDeviceRegistrationRequest response) {
    HostInfo hostInfo = new HostInfo(response.getUser().getUniversallyUniqueId(), response.getUser().getUserName(), response.getDevice().getDeviceId(),
        response.getDevice().getDeviceName(), response.getDevice().getPlatform(), response.getDevice().getOsVersion(), response.getDevice().getPlatformArchitecture(),
        response.getDevice().getCountSynchronizingDevice());
    hostInfo.setAddress(response.getAddress());
    hostInfo.setMessagesPort(response.getPort());

    return registeredDevices.add(hostInfo);
  }
}

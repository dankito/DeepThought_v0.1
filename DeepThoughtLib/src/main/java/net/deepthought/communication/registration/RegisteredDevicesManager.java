package net.deepthought.communication.registration;

import net.deepthought.Application;
import net.deepthought.communication.messages.AskForDeviceRegistrationRequest;
import net.deepthought.communication.model.ConnectedDevice;
import net.deepthought.communication.model.DeviceInfo;
import net.deepthought.communication.model.GroupInfo;
import net.deepthought.communication.model.HostInfo;
import net.deepthought.communication.model.UserInfo;
import net.deepthought.data.model.Device;
import net.deepthought.data.model.Group;
import net.deepthought.data.model.User;

/**
 * Created by ganymed on 21/08/15.
 */
public class RegisteredDevicesManager {


  public boolean hasRegisteredDevices() {
    return getRegisteredDevicesCount() > 0;
  }

  public int getRegisteredDevicesCount() {
    return Application.getLoggedOnUser().getUsersDefaultGroup().getDevices().size() - 1; // TODO: will there ever be other Devices than User's Devices in his/her own group?
  }


  public boolean isDeviceRegistered(HostInfo info) {
    User loggedOnUser = Application.getLoggedOnUser();
    if(loggedOnUser.getDevices().size() > 1 && loggedOnUser.getUniversallyUniqueId().equals(info.getUserUniqueId())) {
      for(Device device : loggedOnUser.getDevices()) {
        if(device.getUniversallyUniqueId().equals(info.getDeviceUniqueId()))
          return true;
      }
    }

    return false;
  }

  public boolean isDeviceRegistered(ConnectedDevice device) {
    User loggedOnUser = Application.getLoggedOnUser();
    return device != null && loggedOnUser.containsDevice(device.getDevice());
  }

  public boolean registerDevice(AskForDeviceRegistrationRequest request, boolean useOtherSidesUserInfo) { // TODO: after calling this start searching for registered devices
    User loggedOnUser = Application.getLoggedOnUser();
    Device peerDevice = extractDeviceInformation(request);

    loggedOnUser.addDevice(peerDevice);

    if(useOtherSidesUserInfo)
      mergeUserInfo(request, loggedOnUser, peerDevice);
    return false;
  }

  protected Device extractDeviceInformation(AskForDeviceRegistrationRequest request) {
    DeviceInfo deviceInfo = request.getDevice();
    Device device = new Device(deviceInfo.getUniversallyUniqueId(), deviceInfo.getName(),  deviceInfo.getPlatform(), deviceInfo.getOsVersion(), deviceInfo.getPlatformArchitecture());
    device.setDescription(deviceInfo.getDescription());
    device.setLastKnownIpAddress(deviceInfo.getIpAddress());

    return device;
  }

  protected void mergeUserInfo(AskForDeviceRegistrationRequest request, User loggedOnUser, Device peerDevice) {
    String previousUserName = loggedOnUser.getUserName();

    UserInfo userInfo = request.getUser();
    loggedOnUser.setUniversallyUniqueId(userInfo.getUniversallyUniqueId());
    loggedOnUser.setUserName(userInfo.getUserName());
    loggedOnUser.setFirstName(userInfo.getFirstName());
    loggedOnUser.setLastName(userInfo.getLastName());

    GroupInfo groupInfo = request.getGroup();
    Group group = loggedOnUser.getUsersDefaultGroup();
    group.setUniversallyUniqueId(groupInfo.getUniversallyUniqueId());
    group.setName(groupInfo.getName());
    group.setDescription(groupInfo.getDescription());

    group.addDevice(peerDevice);

    if(group.getName().contains(previousUserName))
      group.setName(group.getName().replace(previousUserName, loggedOnUser.getUserName()));

    Device localDevice = Application.getApplication().getLocalDevice();
    if(localDevice.getName().contains(previousUserName))
      localDevice.setName(localDevice.getName().replace(previousUserName, loggedOnUser.getUserName()));
  }

}

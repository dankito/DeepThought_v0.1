package net.dankito.deepthought.communication.registration;

import net.dankito.deepthought.communication.messages.request.AskForDeviceRegistrationRequest;
import net.dankito.deepthought.communication.model.HostInfo;
import net.dankito.deepthought.communication.model.UserInfo;
import net.dankito.deepthought.data.model.Device;
import net.dankito.deepthought.Application;
import net.dankito.deepthought.communication.model.ConnectedDevice;
import net.dankito.deepthought.communication.model.GroupInfo;
import net.dankito.deepthought.data.model.Group;
import net.dankito.deepthought.data.model.User;
import net.dankito.deepthought.util.StringUtils;

import java.util.Collection;

/**
 * Created by ganymed on 21/08/15.
 */
public class RegisteredDevicesManager implements IRegisteredDevicesManager {


  public Collection<Device> getRegisteredDevices() {
    return Application.getLoggedOnUser().getUsersDefaultGroup().getDevices();
  }

  @Override
  public boolean hasRegisteredDevices() {
    return getRegisteredDevicesCount() > 0;
  }

  @Override
  public int getRegisteredDevicesCount() {
    return Application.getLoggedOnUser().getUsersDefaultGroup().getDevices().size() - 1; // TODO: will there ever be other Devices than User's Devices in his/her own group?
  }


  @Override
  public boolean isDeviceRegistered(HostInfo info) {
    User loggedOnUser = Application.getLoggedOnUser();
    if(loggedOnUser.getDevices().size() > 1 && loggedOnUser.getUniversallyUniqueId().equals(info.getUserUniqueId())) {
      for(Device device : loggedOnUser.getDevices()) {
        if(device.getUniversallyUniqueId().equals(info.getDeviceId()))
          return true;
      }
    }

    return false;
  }

  @Override
  public boolean isDeviceRegistered(ConnectedDevice device) {
    User loggedOnUser = Application.getLoggedOnUser();
    return device != null && loggedOnUser.containsDevice(device.getDevice());
  }

  @Override
  public boolean registerDevice(AskForDeviceRegistrationRequest response, boolean useOtherSidesUserInfo) { // TODO: after calling this start searching for registered devices
    User loggedOnUser = Application.getLoggedOnUser();
    Device peerDevice = extractDeviceInformation(response);

    Application.getApplication().addDevice(peerDevice);
    loggedOnUser.addDevice(peerDevice);
    loggedOnUser.getUsersDefaultGroup().addDevice(peerDevice);

    if(useOtherSidesUserInfo)
      mergeUserInfo(response, loggedOnUser, peerDevice);
    return false;
  }

  protected Device extractDeviceInformation(AskForDeviceRegistrationRequest response) {
    HostInfo deviceInfo = response.getDevice();
    Device device = new Device(deviceInfo.getDeviceId(), deviceInfo.getDeviceName(),  deviceInfo.getPlatform(), deviceInfo.getOsVersion(), deviceInfo.getPlatformArchitecture());
    device.setDescription(deviceInfo.getDeviceDescription());
    device.setLastKnownIpAddress(response.getAddress());

    return device;
  }

  protected void mergeUserInfo(AskForDeviceRegistrationRequest response, User loggedOnUser, Device peerDevice) {
    String previousUserName = loggedOnUser.getUserName();
    boolean isPreviousUserNameEmpty = StringUtils.isNullOrEmpty(previousUserName);

    UserInfo userInfo = response.getUser();
    loggedOnUser.setUniversallyUniqueId(userInfo.getUniversallyUniqueId());
    loggedOnUser.setUserName(userInfo.getUserName());
    loggedOnUser.setFirstName(userInfo.getFirstName());
    loggedOnUser.setLastName(userInfo.getLastName());

    GroupInfo groupInfo = response.getGroup();
    Group group = loggedOnUser.getUsersDefaultGroup();
    group.setUniversallyUniqueId(groupInfo.getUniversallyUniqueId());
    group.setName(groupInfo.getName());
    group.setDescription(groupInfo.getDescription());

    if(isPreviousUserNameEmpty == false && group.getName().contains(previousUserName)) {
      group.setName(group.getName().replace(previousUserName, loggedOnUser.getUserName()));
    }

    Device localDevice = Application.getApplication().getLocalDevice();
    if(isPreviousUserNameEmpty == false && localDevice.getName().contains(previousUserName)) {
      localDevice.setName(localDevice.getName().replace(previousUserName, loggedOnUser.getUserName()));
    }
  }

}

package net.dankito.deepthought.communication.model;

import net.dankito.deepthought.data.model.Device;
import net.dankito.deepthought.data.model.User;

/**
 * Created by ganymed on 20/08/15.
 */
public class HostInfo {

  protected String userUniqueId = "";

  protected String userName = "";

  protected String deviceDatabaseId = "";

  protected String deviceUniqueId = "";

  protected String deviceName = "";

  protected String deviceDescription = "";

  protected String platform = "";

  protected String osVersion = "";

  protected String platformArchitecture = "";

  protected String address = null;

  protected int countSynchronizingDevice = 0;

  protected int messagesPort = -1;


  public HostInfo() {

  }

  public HostInfo(String userUniqueId, String userName, String deviceDatabaseId, String deviceUniqueId, String deviceName, String platform, String osVersion, String platformArchitecture, int
      countSynchronizingDevices) {
    this.userUniqueId = userUniqueId;
    this.userName = userName;
    this.deviceDatabaseId = deviceDatabaseId;
    this.deviceUniqueId = deviceUniqueId;
    this.deviceName = deviceName;
    this.platform = platform;
    this.osVersion = osVersion;
    this.platformArchitecture = platformArchitecture;
    this.countSynchronizingDevice = countSynchronizingDevices;
  }


  public String getUserUniqueId() {
    return userUniqueId;
  }

  public void setUserUniqueId(String userUniqueId) {
    this.userUniqueId = userUniqueId;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getDeviceDatabaseId() {
    return deviceDatabaseId;
  }

  public String getDeviceUniqueId() {
    return deviceUniqueId;
  }

  public void setDeviceUniqueId(String deviceUniqueId) {
    this.deviceUniqueId = deviceUniqueId;
  }

  public String getDeviceName() {
    return deviceName;
  }

  public void setDeviceName(String deviceName) {
    this.deviceName = deviceName;
  }

  public String getDeviceDescription() {
    return deviceDescription;
  }

  public void setDeviceDescription(String deviceDescription) {
    this.deviceDescription = deviceDescription;
  }

  public String getPlatform() {
    return platform;
  }

  public void setPlatform(String platform) {
    this.platform = platform;
  }

  public String getOsVersion() {
    return osVersion;
  }

  public void setOsVersion(String osVersion) {
    this.osVersion = osVersion;
  }

  public String getPlatformArchitecture() {
    return platformArchitecture;
  }

  public void setPlatformArchitecture(String platformArchitecture) {
    this.platformArchitecture = platformArchitecture;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public int getCountSynchronizingDevice() {
    return countSynchronizingDevice;
  }

  public void setCountSynchronizingDevice(int countSynchronizingDevice) {
    this.countSynchronizingDevice = countSynchronizingDevice;
  }

  public int getMessagesPort() {
    return messagesPort;
  }

  public void setMessagesPort(int messagesPort) {
    this.messagesPort = messagesPort;
  }


  public String getDeviceInfoString() {
    String infoString = platform + " " + osVersion;

    if(platform.toLowerCase().contains("android")) {
      infoString = deviceName + " (" + infoString + ")";
    }

    infoString = userName + " on " + infoString;

    return infoString;
  }

  @Override
  public String toString() {
    return userName + " (" + platform + ")";
  }


  public static HostInfo fromUserAndDevice(User user, Device device) {
    return new HostInfo(user.getUniversallyUniqueId(), user.getUserName(), device.getId(), device.getUniversallyUniqueId(), device.getName(), device.getPlatform(),
        device.getOsVersion(), device.getPlatformArchitecture(), device.getCountSynchronizingDevices());
  }

}

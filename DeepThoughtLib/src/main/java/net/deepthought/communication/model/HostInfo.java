package net.deepthought.communication.model;

import net.deepthought.data.model.Device;
import net.deepthought.data.model.User;

/**
 * Created by ganymed on 20/08/15.
 */
public class HostInfo {

  protected String userUniqueId = "";

  protected String userName = "";

  protected String deviceId = "";

  protected String deviceName = "";

  protected String deviceDescription = "";

  protected String platform = "";

  protected String osVersion = "";

  protected String platformArchitecture = "";

  protected String address = null;

  protected int messagesPort = -1;


  public HostInfo() {

  }

  public HostInfo(String userUniqueId, String userName, String deviceId, String deviceName, String platform, String osVersion, String platformArchitecture) {
    this.userUniqueId = userUniqueId;
    this.userName = userName;
    this.deviceId = deviceId;
    this.deviceName = deviceName;
    this.platform = platform;
    this.osVersion = osVersion;
    this.platformArchitecture = platformArchitecture;
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

  public String getDeviceId() {
    return deviceId;
  }

  public void setDeviceId(String deviceId) {
    this.deviceId = deviceId;
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

    return infoString;
  }

  @Override
  public String toString() {
    return userName + " (" + platform + ")";
  }


  public static HostInfo fromUserAndDevice(User user, Device device) {
    return new HostInfo(user.getUniversallyUniqueId(), user.getUserName(), device.getUniversallyUniqueId(), device.getName(), device.getPlatform(),
        device.getOsVersion(), device.getPlatformArchitecture());
  }

}

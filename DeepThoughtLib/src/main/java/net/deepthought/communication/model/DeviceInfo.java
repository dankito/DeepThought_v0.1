package net.deepthought.communication.model;

import net.deepthought.data.model.Device;

/**
 * Created by ganymed on 19/08/15.
 */
public class DeviceInfo {

  protected String deviceId = "";

  protected String deviceName = "";

  protected String deviceDescription = "";

  protected String platform = "";

  protected String osVersion = "";

  protected String platformArchitecture = "";

  protected String ipAddress = null;

  protected int port = -1;


  public DeviceInfo() {

  }

  public DeviceInfo(String deviceId, String deviceName, String platform, String osVersion, String platformArchitecture) {
    this.deviceId = deviceId;
    this.deviceName = deviceName;
    this.platform = platform;
    this.osVersion = osVersion;
    this.platformArchitecture = platformArchitecture;
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

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }


  @Override
  public String toString() {
    String infoString = platform + " " + osVersion;

    if(platform != null && platform.toLowerCase().contains("android")) {
      infoString = deviceName + " (" + infoString + ")";
    }

    return infoString;
  }

  public static DeviceInfo fromDevice(Device device) {
    return new DeviceInfo(device.getUniversallyUniqueId(), device.getName(), device.getPlatform(), device.getOsVersion(), device.getPlatformArchitecture());
  }

}

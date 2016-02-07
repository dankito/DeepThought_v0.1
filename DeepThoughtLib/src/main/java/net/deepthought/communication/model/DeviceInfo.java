package net.deepthought.communication.model;

import net.deepthought.data.model.Device;

/**
 * Created by ganymed on 19/08/15.
 */
public class DeviceInfo {

  protected String universallyUniqueId = "";

  protected String name = "";

  protected String description = "";

  protected String platform = "";

  protected String osVersion = "";

  protected String platformArchitecture = "";

  protected String ipAddress = null;

  protected int port = -1;


  public DeviceInfo() {

  }

  public DeviceInfo(String universallyUniqueId, String platform, String osVersion, String platformArchitecture) {
    this.universallyUniqueId = universallyUniqueId;
    this.platform = platform;
    this.osVersion = osVersion;
    this.platformArchitecture = platformArchitecture;
  }


  public String getUniversallyUniqueId() {
    return universallyUniqueId;
  }

  public void setUniversallyUniqueId(String universallyUniqueId) {
    this.universallyUniqueId = universallyUniqueId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
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

  public String getIpAddress() {
    return ipAddress;
  }

  public void setIpAddress(String ipAddress) {
    this.ipAddress = ipAddress;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }


  @Override
  public String toString() {
    return platform + " " + osVersion;
  }

  public static DeviceInfo fromDevice(Device device) {
    return new DeviceInfo(device.getUniversallyUniqueId(), device.getPlatform(), device.getOsVersion(), device.getPlatformArchitecture());
  }

}

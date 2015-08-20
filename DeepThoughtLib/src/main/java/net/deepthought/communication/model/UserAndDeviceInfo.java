package net.deepthought.communication.model;

/**
 * Created by ganymed on 19/08/15.
 */
public class UserAndDeviceInfo {

  protected UserInfo userInfo;

  protected DeviceInfo deviceInfo;


  public UserAndDeviceInfo(UserInfo userInfo, DeviceInfo deviceInfo) {
    this.userInfo = userInfo;
    this.deviceInfo = deviceInfo;
  }

  public UserInfo getUserInfo() {
    return userInfo;
  }

  public void setUserInfo(UserInfo userInfo) {
    this.userInfo = userInfo;
  }

  public DeviceInfo getDeviceInfo() {
    return deviceInfo;
  }

  public void setDeviceInfo(DeviceInfo deviceInfo) {
    this.deviceInfo = deviceInfo;
  }

}

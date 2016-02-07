package net.deepthought.communication.model;

/**
 * Created by ganymed on 20/11/15.
 */
public class Host {

  protected String address;

  protected int communicatorPort;

  protected String deviceId;

  /**
   * Only used in Multiuser Environments.
   */
  protected String userId;


  public String getAddress() {
    return address;
  }

  public int getCommunicatorPort() {
    return communicatorPort;
  }

  public String getDeviceId() {
    return deviceId;
  }

  public String getUserId() {
    return userId;
  }

}

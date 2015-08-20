package net.deepthought.communication.messages;

import net.deepthought.communication.model.DeviceInfo;
import net.deepthought.communication.model.GroupInfo;
import net.deepthought.communication.model.UserInfo;
import net.deepthought.data.model.Device;
import net.deepthought.data.model.User;

/**
 * Created by ganymed on 20/08/15.
 */
public class AskForDeviceRegistrationResponse extends Response {

  protected boolean allowsRegistration = false;

  protected boolean useServersUserInformation = false;

  protected UserInfo user;

  protected GroupInfo group;

  protected DeviceInfo device;


  public AskForDeviceRegistrationResponse(boolean allowsRegistration) {
    this.allowsRegistration = allowsRegistration;
  }

  public AskForDeviceRegistrationResponse(boolean allowsRegistration, boolean useServersUserInformation, UserInfo user, GroupInfo group, DeviceInfo device) {
    this(allowsRegistration);
    this.useServersUserInformation = useServersUserInformation;
    this.user = user;
    this.group = group;
    this.device = device;
  }



  public boolean allowsRegistration() {
    return allowsRegistration;
  }

  public boolean isUseServersUserInformation() {
    return useServersUserInformation;
  }

  public UserInfo getUser() {
    return user;
  }

  public GroupInfo getGroup() {
    return group;
  }

  public DeviceInfo getDevice() {
    return device;
  }


  @Override
  public String toString() {
    return "Registration allowed? " + allowsRegistration;
  }


  public static AskForDeviceRegistrationResponse createDenyRegistrationResponse() {
    return new AskForDeviceRegistrationResponse(false);
  }

  public static AskForDeviceRegistrationResponse createAllowRegistrationResponse(boolean useServersUserInformation, User user, Device device) {
    return new AskForDeviceRegistrationResponse(true, useServersUserInformation, UserInfo.fromUser(user), GroupInfo.fromGroup(user.getUsersDefaultGroup()), DeviceInfo.fromDevice(device));
  }

}

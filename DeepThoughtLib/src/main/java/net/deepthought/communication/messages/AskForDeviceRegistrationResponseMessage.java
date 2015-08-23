package net.deepthought.communication.messages;

import net.deepthought.communication.model.DeviceInfo;
import net.deepthought.communication.model.GroupInfo;
import net.deepthought.communication.model.UserInfo;
import net.deepthought.data.model.Device;
import net.deepthought.data.model.User;

/**
 * Created by ganymed on 20/08/15.
 */
public class AskForDeviceRegistrationResponseMessage extends Request {

  public final static AskForDeviceRegistrationResponseMessage Deny = new AskForDeviceRegistrationResponseMessage(false);


  protected boolean allowsRegistration = false;

  protected boolean useServersUserInformation = false;

  protected UserInfo user;

  protected GroupInfo group;

  protected DeviceInfo device;

  protected int requestMessageId;


  public AskForDeviceRegistrationResponseMessage(boolean allowsRegistration) {
    this.allowsRegistration = allowsRegistration;
  }

  public AskForDeviceRegistrationResponseMessage(boolean allowsRegistration, boolean useServersUserInformation, UserInfo user, GroupInfo group, DeviceInfo device) {
    this(allowsRegistration);
    this.useServersUserInformation = useServersUserInformation;
    this.user = user;
    this.group = group;
    this.device = device;
  }



  public boolean allowsRegistration() {
    return allowsRegistration;
  }

  public boolean useServersUserInformation() {
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

  public int getRequestMessageId() {
    return requestMessageId;
  }

  public void setRequestMessageId(int requestMessageId) {
    this.requestMessageId = requestMessageId;
  }


  @Override
  public String toString() {
    return "Registration allowed? " + allowsRegistration;
  }


  public static AskForDeviceRegistrationResponseMessage createAllowRegistrationResponse(boolean useServersUserInformation, User user, Device device) {
    return new AskForDeviceRegistrationResponseMessage(true, useServersUserInformation, UserInfo.fromUser(user), GroupInfo.fromGroup(user.getUsersDefaultGroup()), DeviceInfo.fromDevice(device));
  }

}

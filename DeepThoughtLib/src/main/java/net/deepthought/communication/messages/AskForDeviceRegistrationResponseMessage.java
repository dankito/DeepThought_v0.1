package net.deepthought.communication.messages;

import net.deepthought.Application;
import net.deepthought.communication.NetworkHelper;
import net.deepthought.communication.model.DeviceInfo;
import net.deepthought.communication.model.GroupInfo;
import net.deepthought.communication.model.UserInfo;
import net.deepthought.data.model.Device;
import net.deepthought.data.model.User;

/**
 * Created by ganymed on 20/08/15.
 */
public class AskForDeviceRegistrationResponseMessage extends AskForDeviceRegistrationRequest {

  public final static AskForDeviceRegistrationResponseMessage Deny = new AskForDeviceRegistrationResponseMessage(false);


  protected boolean allowsRegistration = false;

  protected boolean useServersUserInformation = false;


  protected AskForDeviceRegistrationResponseMessage(boolean allowsRegistration) {
    this.allowsRegistration = allowsRegistration;
  }

  public AskForDeviceRegistrationResponseMessage(boolean allowsRegistration, boolean useServersUserInformation, UserInfo user, GroupInfo group, DeviceInfo device,
                                                 String ipAddress, int port) {
    super(user, group, device, ipAddress, port);
    this.allowsRegistration = allowsRegistration;
    this.useServersUserInformation = useServersUserInformation;
  }



  public boolean allowsRegistration() {
    return allowsRegistration;
  }

  public boolean useServersUserInformation() {
    return useServersUserInformation;
  }

  public int getRequestMessageId() {
    return messageId;
  }

  public void setRequestMessageId(int requestMessageId) {
    this.messageId = requestMessageId;
  }


  @Override
  public String toString() {
    return "Registration allowed? " + allowsRegistration;
  }


  public static AskForDeviceRegistrationResponseMessage createAllowRegistrationResponse(boolean useServersUserInformation, User user, Device device) {
    return new AskForDeviceRegistrationResponseMessage(true, useServersUserInformation, UserInfo.fromUser(user), GroupInfo.fromGroup(user.getUsersDefaultGroup()),
        DeviceInfo.fromDevice(device), NetworkHelper.getIPAddressString(true), Application.getDeepThoughtsConnector().getMessageReceiverPort());
  }

}

package net.deepthought.communication.messages.response;

import net.deepthought.Application;
import net.deepthought.communication.NetworkHelper;
import net.deepthought.communication.messages.request.AskForDeviceRegistrationRequest;
import net.deepthought.communication.model.GroupInfo;
import net.deepthought.communication.model.HostInfo;
import net.deepthought.communication.model.UserInfo;
import net.deepthought.data.model.Device;
import net.deepthought.data.model.User;

/**
 * Created by ganymed on 20/08/15.
 */
public class AskForDeviceRegistrationResponse extends AskForDeviceRegistrationRequest implements ResponseToAsynchronousRequest {

  public final static AskForDeviceRegistrationResponse Deny = new AskForDeviceRegistrationResponse(false);


  protected boolean allowsRegistration = false;

  protected boolean useServersUserInformation = false;


  protected AskForDeviceRegistrationResponse(boolean allowsRegistration) {
    this.allowsRegistration = allowsRegistration;
  }

  public AskForDeviceRegistrationResponse(boolean allowsRegistration, boolean useServersUserInformation, UserInfo user, GroupInfo group, HostInfo device,
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

  @Override
  public int getRequestMessageId() {
    return messageId;
  }

  @Override
  public boolean isDone() {
    return true;
  }

  public void setRequestMessageId(int requestMessageId) {
    this.messageId = requestMessageId;
  }


  @Override
  public String toString() {
    return "Registration allowed? " + allowsRegistration;
  }


  public static AskForDeviceRegistrationResponse createAllowRegistrationResponse(boolean useServersUserInformation, User user, Device device) {
    return new AskForDeviceRegistrationResponse(true, useServersUserInformation, UserInfo.fromUser(user), GroupInfo.fromGroup(user.getUsersDefaultGroup()),
        HostInfo.fromUserAndDevice(user, device), NetworkHelper.getIPAddressString(true), Application.getDeepThoughtConnector().getMessageReceiverPort());
  }

}

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
public class AskForDeviceRegistrationRequest extends Request {

  protected static int MessageId = 0;


  protected UserInfo user;

  protected GroupInfo group;

  protected DeviceInfo device;

  protected String ipAddress;

  protected int port;

  protected int messageId = MessageId++; // to be able to assign Server Response to Request


  public AskForDeviceRegistrationRequest(UserInfo user, GroupInfo group, DeviceInfo device, String ipAddress, int port) {
    this.user = user;
    this.group = group;
    this.device = device;
    this.ipAddress = ipAddress;
    this.port = port;
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

  public String getIpAddress() {
    return ipAddress;
  }

  public int getPort() {
    return port;
  }

  public int getMessageId() {
    return messageId;
  }


  @Override
  public String toString() {
    return user.toString();
  }


  public static AskForDeviceRegistrationRequest fromUserAndDevice(User user, Device device) {
    return new AskForDeviceRegistrationRequest(UserInfo.fromUser(user), GroupInfo.fromGroup(user.getUsersDefaultGroup()), DeviceInfo.fromDevice(device),
        NetworkHelper.getIPAddressString(true), Application.getDeepThoughtsConnector().getMessageReceiverPort());
  }

}

package net.dankito.deepthought.communication.messages.request;

import net.dankito.deepthought.communication.model.DeepThoughtInfo;
import net.dankito.deepthought.communication.model.HostInfo;
import net.dankito.deepthought.communication.model.GroupInfo;
import net.dankito.deepthought.communication.model.UserInfo;
import net.dankito.deepthought.data.model.Device;
import net.dankito.deepthought.data.model.User;

/**
 * Created by ganymed on 20/08/15.
 */
public class AskForDeviceRegistrationRequest extends RequestWithAsynchronousResponse {

  protected UserInfo user;

  protected GroupInfo group;

  protected HostInfo device;

  protected DeepThoughtInfo currentDeepThoughtInfo;


  protected AskForDeviceRegistrationRequest() {

  }

  public AskForDeviceRegistrationRequest(User user, Device device, String ipAddress, int port) {
    this(UserInfo.fromUser(user), GroupInfo.fromGroup(user.getUsersDefaultGroup()), HostInfo.fromUserAndDevice(user, device),
        DeepThoughtInfo.fromDeepThought(user.getLastViewedDeepThought()), ipAddress, port);
  }

  public AskForDeviceRegistrationRequest(UserInfo user, GroupInfo group, HostInfo device, DeepThoughtInfo currentDeepThoughtInfo, String ipAddress, int port) {
    super(ipAddress, port);

    this.user = user;
    this.group = group;
    this.device = device;
    this.currentDeepThoughtInfo = currentDeepThoughtInfo;
  }


  public UserInfo getUser() {
    return user;
  }

  public GroupInfo getGroup() {
    return group;
  }

  public HostInfo getDevice() {
    return device;
  }

  public DeepThoughtInfo getCurrentDeepThoughtInfo() {
    return currentDeepThoughtInfo;
  }


  @Override
  public String toString() {
    return user.toString();
  }

}

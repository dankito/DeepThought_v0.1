package net.dankito.deepthought.communication.messages.response;

import net.dankito.deepthought.communication.NetworkHelper;
import net.dankito.deepthought.communication.messages.request.AskForDeviceRegistrationRequest;
import net.dankito.deepthought.communication.model.DeepThoughtInfo;
import net.dankito.deepthought.communication.model.HostInfo;
import net.dankito.deepthought.communication.model.UserInfo;
import net.dankito.deepthought.data.model.Device;
import net.dankito.deepthought.data.model.User;
import net.dankito.deepthought.Application;
import net.dankito.deepthought.communication.model.GroupInfo;

/**
 * Created by ganymed on 20/08/15.
 */
public class AskForDeviceRegistrationResponse extends AskForDeviceRegistrationRequest implements ResponseToAsynchronousRequest {

  public final static AskForDeviceRegistrationResponse Deny = new AskForDeviceRegistrationResponse(false);


  protected boolean allowsRegistration = false;

  protected boolean useLocalUserInformation = false;

  protected boolean useLocalDatabaseIds = false;


  protected AskForDeviceRegistrationResponse(boolean allowsRegistration) {
    this.allowsRegistration = allowsRegistration;
  }

  public AskForDeviceRegistrationResponse(boolean allowsRegistration, boolean useLocalUserInformation, boolean useLocalDatabaseIds, UserInfo user, GroupInfo group,
                                          HostInfo device, DeepThoughtInfo deepThoughtInfo, String ipAddress, int port) {
    super(user, group, device, deepThoughtInfo, ipAddress, port);
    this.allowsRegistration = allowsRegistration;
    this.useLocalUserInformation = useLocalUserInformation;
    this.useLocalDatabaseIds = useLocalDatabaseIds;
  }



  public boolean allowsRegistration() {
    return allowsRegistration;
  }

  public boolean getUseLocalUserInformation() {
    return useLocalUserInformation;
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


  public static AskForDeviceRegistrationResponse createAllowRegistrationResponse(boolean useLocalUserInformation, boolean useLocalDatabaseIds, User user, Device device) {
    return new AskForDeviceRegistrationResponse(true, useLocalUserInformation, useLocalDatabaseIds, UserInfo.fromUser(user), GroupInfo.fromGroup(user.getUsersDefaultGroup()),
        HostInfo.fromUserAndDevice(user, device), DeepThoughtInfo.fromDeepThought(user.getLastViewedDeepThought()),
        NetworkHelper.getIPAddressString(true), Application.getDeepThoughtConnector().getMessageReceiverPort());
  }

}

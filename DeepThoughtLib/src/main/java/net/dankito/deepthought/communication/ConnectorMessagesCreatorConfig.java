package net.dankito.deepthought.communication;

import net.dankito.deepthought.data.model.Device;
import net.dankito.deepthought.data.model.User;

/**
 * Created by ganymed on 22/11/15.
 */
public class ConnectorMessagesCreatorConfig {

  protected User loggedOnUser;

  protected Device localDevice;

  protected String localHostIpAddress;

  protected int messageReceiverPort;


  public ConnectorMessagesCreatorConfig(User loggedOnUser, Device localDevice, String localHostIpAddress, int messageReceiverPort) {
    this.loggedOnUser = loggedOnUser;
    this.localDevice = localDevice;
    this.localHostIpAddress = localHostIpAddress;
    this.messageReceiverPort = messageReceiverPort;
  }


  public User getLoggedOnUser() {
    return loggedOnUser;
  }

  public Device getLocalDevice() {
    return localDevice;
  }

  public String getLocalHostIpAddress() {
    return localHostIpAddress;
  }

  public int getMessageReceiverPort() {
    return messageReceiverPort;
  }

}

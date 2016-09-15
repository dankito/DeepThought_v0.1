package net.dankito.deepthought.communication;

import net.dankito.deepthought.communication.model.ConnectedDevice;
import net.dankito.deepthought.data.model.Device;
import net.dankito.deepthought.data.model.User;

/**
 * Created by ganymed on 15/09/16.
 */
public interface ICommunicationConfigurationManager {
  ConnectedDevice getLocalHostDevice();

  void setLoggedOnUser(User loggedOnUser);

  void setLocalDevice(Device localDevice);

  void setLocalHostIpAddress(String localHostIpAddress);

  void setMessageReceiverPort(int messageReceiverPort);

  void setSynchronizationPort(int synchronizationPort);
}

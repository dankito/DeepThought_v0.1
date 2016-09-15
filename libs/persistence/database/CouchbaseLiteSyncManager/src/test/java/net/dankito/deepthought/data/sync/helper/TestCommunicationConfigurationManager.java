package net.dankito.deepthought.data.sync.helper;

import net.dankito.deepthought.communication.ICommunicationConfigurationManager;
import net.dankito.deepthought.communication.model.ConnectedDevice;
import net.dankito.deepthought.data.model.Device;
import net.dankito.deepthought.data.model.User;

/**
 * Created by ganymed on 15/09/16.
 */
public class TestCommunicationConfigurationManager implements ICommunicationConfigurationManager {

  @Override
  public ConnectedDevice getLocalHostDevice() {
    return null;
  }

  @Override
  public void setLoggedOnUser(User loggedOnUser) {

  }

  @Override
  public void setLocalDevice(Device localDevice) {

  }

  @Override
  public void setLocalHostIpAddress(String localHostIpAddress) {

  }

  @Override
  public void setMessageReceiverPort(int messageReceiverPort) {

  }

  @Override
  public void setSynchronizationPort(int synchronizationPort) {

  }
}

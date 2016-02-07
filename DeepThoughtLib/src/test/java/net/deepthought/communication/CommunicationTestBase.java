package net.deepthought.communication;

import net.deepthought.communication.connected_device.IConnectedDevicesManager;
import net.deepthought.communication.helper.TestConnectedDevicesManager;
import net.deepthought.communication.model.ConnectedDevice;
import net.deepthought.communication.registration.IRegisteredDevicesManager;
import net.deepthought.communication.helper.TestRegisteredDevicesManager;
import net.deepthought.data.model.Device;
import net.deepthought.data.model.User;
import net.deepthought.util.IThreadPool;
import net.deepthought.util.ThreadPool;

import org.junit.Before;

/**
 * Created by ganymed on 20/08/15.
 */
public class CommunicationTestBase {

  protected final static String TestDeviceId = "Cuddle";

  protected final static String TestIpAddress = "0.0.0.0";

  protected final static int CommunicatorPort = 54321;


  protected ConnectorMessagesCreator messagesCreator;

  protected IRegisteredDevicesManager registeredDevicesManager;

  protected IConnectedDevicesManager connectedDevicesManager;

  protected IThreadPool threadPool;

  protected ConnectedDevice localHost = new ConnectedDevice(TestDeviceId, TestIpAddress, CommunicatorPort);

  protected User loggedOnUser;
  protected Device localDevice;


  @Before
  public void setup() throws Exception {
    loggedOnUser = User.createNewLocalUser();
    localDevice = new Device("test", "test", "test");
    loggedOnUser.addDevice(localDevice);

    registeredDevicesManager = new TestRegisteredDevicesManager();
    connectedDevicesManager = new TestConnectedDevicesManager();
    messagesCreator = new ConnectorMessagesCreator(new ConnectorMessagesCreatorConfig(loggedOnUser, localDevice, TestIpAddress, CommunicatorPort));
    threadPool = new ThreadPool();
  }

}

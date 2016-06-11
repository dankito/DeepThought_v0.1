package net.dankito.deepthought.communication;

import net.dankito.deepthought.communication.connected_device.IConnectedDevicesManager;
import net.dankito.deepthought.communication.helper.TestConnectedDevicesManager;
import net.dankito.deepthought.communication.helper.TestRegisteredDevicesManager;
import net.dankito.deepthought.communication.model.ConnectedDevice;
import net.dankito.deepthought.communication.registration.IRegisteredDevicesManager;
import net.dankito.deepthought.data.model.Device;
import net.dankito.deepthought.data.model.User;
import net.dankito.deepthought.util.IThreadPool;
import net.dankito.deepthought.util.ThreadPool;

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

package net.deepthought.communication;

import net.deepthought.communication.connected_device.ConnectedDevicesManager;
import net.deepthought.communication.model.ConnectedDevice;
import net.deepthought.communication.registration.RegisteredDevicesManager;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Created by ganymed on 19/08/15.
 */
public class DeepThoughtsConnectorTest extends CommunicationTestBase {


  @Test
  public void startTwoDeepThoughtsConnectors_BothMessageHandlersStartSuccessfully() {
//    DeepThoughtsConnector connector1 = new DeepThoughtsConnector(null);
//    connector1.runAsync();

    DeepThoughtsConnector connector2 = new DeepThoughtsConnector();
    connector2.runAsync();

    try { Thread.sleep(500); } catch(Exception ex) { } // wait same time till Servers have started

    // now assure that both have started successfully even so by default the use the same port to listen on
    Assert.assertTrue(connector.isStarted());
    Assert.assertTrue(connector2.isStarted());
    Assert.assertNotEquals(connector.getMessageReceiverPort(), connector2.getMessageReceiverPort());
  }


  @Test
  public void registeredDevicesExists_NotConnectedToAllRegisteredDevices_RegisteredDevicesSearcherGetsStarted() {
    connector.shutDown();

    mockNumberOfRegisteredDevices(connector, 2);
    mockNumberOfConnectedDevices(connector, 1);

    connector.runAsync();
    try { Thread.sleep(500); } catch(Exception ex) { } // wait same time till Servers have started

    Assert.assertTrue(connector.isRegisteredDevicesSearcherRunning());
  }

  @Test
  public void registeredDevicesExists_AlreadyConnectedToAllRegisteredDevices_RegisteredDevicesSearcherWontBeStarted() {
    connector.shutDown();

    mockNumberOfRegisteredDevices(connector, 2);
    mockNumberOfConnectedDevices(connector, 2);

    connector.runAsync();
    try { Thread.sleep(500); } catch(Exception ex) { } // wait same time till Servers have started

    Assert.assertFalse(connector.isRegisteredDevicesSearcherRunning());
  }

  @Test
  public void connectsToARegisteredDevice_IsNowConnectedToAllRegisteredDevices_RegisteredDevicesSearcherGetsStopped() {
    DeepThoughtsConnector connector = new DeepThoughtsConnector();

    mockNumberOfRegisteredDevices(connector, 2);
    mockNumberOfConnectedDevices(connector, 1);

    connector.runAsync();
    try { Thread.sleep(500); } catch(Exception ex) { } // wait same time till Servers have started

    mockNumberOfConnectedDevices(connector, 2);
    connector.registeredDeviceConnectedListener.registeredDeviceConnected(new ConnectedDevice("", "", 0));

    Assert.assertFalse(connector.isRegisteredDevicesSearcherRunning());
  }

  @Test
  public void disconnectsFromARegisteredDevice_IsNowNotConnectedAnymoreToAllRegisteredDevices_RegisteredDevicesSearcherGetsStarted() {
    DeepThoughtsConnector connector = new DeepThoughtsConnector();

    mockNumberOfRegisteredDevices(connector, 2);
    mockNumberOfConnectedDevices(connector, 2);

    connector.runAsync();
    try { Thread.sleep(500); } catch(Exception ex) { } // wait same time till Servers have started

    mockNumberOfConnectedDevices(connector, 1);
    connector.registeredDeviceDisconnectedListener.registeredDeviceDisconnected(new ConnectedDevice("", "", 0));

    Assert.assertTrue(connector.isRegisteredDevicesSearcherRunning());
  }


  @Test
  public void noConnectedDevices_ConnectionsAliveWatcherIsNotRunning() {
    connector.shutDown();
    connector.runAsync();
    try { Thread.sleep(500); } catch(Exception ex) { } // wait same time till Servers have started

    Assert.assertFalse(connector.isConnectionWatcherRunning());
  }

  @Test
  public void deviceConnected_ConnectionsAliveWatcherIsRunning() {
    DeepThoughtsConnector connector = new DeepThoughtsConnector();
    connector.runAsync();
    try { Thread.sleep(500); } catch(Exception ex) { } // wait same time till Servers have started

    connector.registeredDeviceConnectedListener.registeredDeviceConnected(new ConnectedDevice("", "", 0));

    Assert.assertTrue(connector.isConnectionWatcherRunning());
  }

  @Test
  public void disconnectsFromLastDevice_IsNowNotConnectedAnymoreToAllRegisteredDevices_RegisteredDevicesSearcherGetsStarted() {
    DeepThoughtsConnector connector = new DeepThoughtsConnector();
    connector.runAsync();
    try { Thread.sleep(500); } catch(Exception ex) { } // wait same time till Servers have started

    ConnectedDevice connectedDevice = new ConnectedDevice("", "", 0);
    connector.registeredDeviceConnectedListener.registeredDeviceConnected(connectedDevice);
    connector.registeredDeviceDisconnectedListener.registeredDeviceDisconnected(connectedDevice);

    Assert.assertFalse(connector.isConnectionWatcherRunning());
  }



  protected void mockNumberOfRegisteredDevices(IDeepThoughtsConnector connector, int numberOfRegisteredDevices) {
    RegisteredDevicesManager registeredDevicesManager = Mockito.mock(RegisteredDevicesManager.class);
    ((DeepThoughtsConnector)connector).setRegisteredDevicesManager(registeredDevicesManager);

    Mockito.when(registeredDevicesManager.getRegisteredDevicesCount()).thenReturn(numberOfRegisteredDevices);

    Mockito.when(registeredDevicesManager.hasRegisteredDevices()).thenReturn(numberOfRegisteredDevices > 0);
  }

  protected void mockNumberOfConnectedDevices(IDeepThoughtsConnector connector, int numberOfConnectedDevices) {
    ConnectedDevicesManager connectedDevicesManager = Mockito.mock(ConnectedDevicesManager.class);
    ((DeepThoughtsConnector)connector).setConnectedDevicesManager(connectedDevicesManager);

    Mockito.when(connectedDevicesManager.getConnectedDevicesCount()).thenReturn(numberOfConnectedDevices);
  }
}

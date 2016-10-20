package net.dankito.deepthought.communication;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.TestApplicationConfiguration;
import net.dankito.deepthought.communication.connected_device.ConnectedDevicesManager;
import net.dankito.deepthought.communication.listener.AskForDeviceRegistrationResultListener;
import net.dankito.deepthought.communication.listener.MessagesReceiverListener;
import net.dankito.deepthought.communication.messages.AsynchronousResponseListenerManager;
import net.dankito.deepthought.communication.messages.DeepThoughtMessagesReceiverConfig;
import net.dankito.deepthought.communication.messages.MessagesDispatcher;
import net.dankito.deepthought.communication.messages.MessagesReceiver;
import net.dankito.deepthought.communication.messages.request.AskForDeviceRegistrationRequest;
import net.dankito.deepthought.communication.messages.request.Request;
import net.dankito.deepthought.communication.messages.response.AskForDeviceRegistrationResponse;
import net.dankito.deepthought.communication.model.ConnectedDevice;
import net.dankito.deepthought.communication.model.DeepThoughtInfo;
import net.dankito.deepthought.communication.model.GroupInfo;
import net.dankito.deepthought.communication.model.HostInfo;
import net.dankito.deepthought.communication.model.UserInfo;
import net.dankito.deepthought.communication.registration.RegisteredDevicesManager;
import net.dankito.deepthought.data.model.Device;
import net.dankito.deepthought.data.model.User;
import net.dankito.deepthought.util.ThreadPool;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by ganymed on 05/06/16.
 */
public class UdpDevicesFinderTest extends CommunicationTestBase {

  protected IDeepThoughtConnector connector;

  protected Communicator communicator;

  protected UdpDevicesFinder devicesFinder;


  @Override
  public void setup() throws Exception {
    super.setup();

    Application.instantiate(new TestApplicationConfiguration());
    try { Thread.sleep(200); } catch(Exception ex) { } // it is very critical that server is fully started therefore wait some time

    connector = Application.getDeepThoughtConnector();
    communicator = connector.getCommunicator();
    devicesFinder = (UdpDevicesFinder)((DeepThoughtConnector)connector).devicesFinder;
    devicesFinder = new UdpDevicesFinder(new ThreadPool());

    loggedOnUser = Application.getLoggedOnUser();
    localDevice = Application.getApplication().getLocalDevice();
    localHost = new ConnectedDevice(localDevice.getUniversallyUniqueId(), TestIpAddress, connector.getMessageReceiverPort());
  }

  @After
  public void tearDown() {
    Application.shutdown();
  }

  @Test
  public void askForDeviceRegistration_ListenerMethodAllowDeviceToRegisterGetsCalled() {
    final AtomicBoolean methodCalled = new AtomicBoolean(false);
    final CountDownLatch waitLatch = new CountDownLatch(1);

//    devicesFinder.openUserDeviceRegistrationServer(new IUnregisteredDevicesListener() {
//      @Override
//      public void askUserToSyncDataWithDevice(HostInfo hostInfo) {
//
//      }
//
//      @Override
//      public void deviceIsAskingForRegistration(AskForDeviceRegistrationRequest request) {
//        methodCalled.set(true);
//        waitLatch.countDown();
//      }
//    });

    communicator.askForDeviceRegistration(createLocalHostServerInfo(), loggedOnUser, localDevice, null);

    try { waitLatch.await(2, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertTrue(methodCalled.get());
  }

  @Test
  public void askForDeviceRegistration_RegistrationIsProhibitedByServer_RegistrationDeniedResponseIsReceived() {
    final List<AskForDeviceRegistrationResponse> responses = new ArrayList<>();
    final CountDownLatch waitLatch = new CountDownLatch(1);

//    devicesFinder.openUserDeviceRegistrationServer(new IUnregisteredDevicesListener() {
//      @Override
//      public void askUserToSyncDataWithDevice(HostInfo hostInfo) {
//
//      }
//
//      @Override
//      public void deviceIsAskingForRegistration(AskForDeviceRegistrationRequest request) {
//        communicator.respondToAskForDeviceRegistrationRequest(request, AskForDeviceRegistrationResponse.Deny, null);
//      }
//    });

    communicator.askForDeviceRegistration(createLocalHostServerInfo(), loggedOnUser, localDevice, new AskForDeviceRegistrationResultListener() {
      @Override
      public void responseReceived(AskForDeviceRegistrationRequest request, AskForDeviceRegistrationResponse response) {
        responses.add(response);
        waitLatch.countDown();
      }
    });

    try { waitLatch.await(3, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertFalse(responses.get(0).allowsRegistration());
  }

  @Test
  public void askForDeviceRegistration_ServerAllowsRegistration_RegistrationAllowedResponseIsReceived() {
    final List<AskForDeviceRegistrationResponse> responses = new ArrayList<>();
    final CountDownLatch waitLatch = new CountDownLatch(1);

//    devicesFinder.openUserDeviceRegistrationServer(new IUnregisteredDevicesListener() {
//      @Override
//      public void askUserToSyncDataWithDevice(HostInfo hostInfo) {
//
//      }
//
//      @Override
//      public void deviceIsAskingForRegistration(AskForDeviceRegistrationRequest request) {
//        communicator.respondToAskForDeviceRegistrationRequest(request, AskForDeviceRegistrationResponse.createAllowRegistrationResponse(true,
//            Application.getLoggedOnUser(), Application.getApplication().getLocalDevice()), null);
//      }
//    });

    communicator.askForDeviceRegistration(createLocalHostServerInfo(), loggedOnUser, localDevice, new AskForDeviceRegistrationResultListener() {
      @Override
      public void responseReceived(AskForDeviceRegistrationRequest request, AskForDeviceRegistrationResponse response) {
        responses.add(response);
        waitLatch.countDown();
      }
    });

    try { waitLatch.await(3, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertTrue(responses.get(0).allowsRegistration());
  }


  @Test
  public void askForDeviceRegistration_ServerAllowsRegistration_BothDevicesHaveRegisteredOtherEndpoint() throws IOException {
    final int device2MessagesPort = 43210;
    final int device2SynchronizationPort = 43211;
    final User user2 = User.createNewLocalUser();
    final Device device2 = new Device("test2", "test2", "test2");
    AsynchronousResponseListenerManager listenerManager2 = new AsynchronousResponseListenerManager();

    final Communicator communicator2 = new Communicator(new CommunicatorConfig(new MessagesDispatcher(threadPool), listenerManager2, device2MessagesPort,
        new ConnectorMessagesCreator(user2, device2, TestIpAddress, device2MessagesPort, device2SynchronizationPort)));
    MessagesReceiver messagesReceiver2 = new MessagesReceiver(new DeepThoughtMessagesReceiverConfig(device2MessagesPort, listenerManager2), new MessagesReceiverListener() {
      @Override
      public boolean messageReceived(String methodName, Request request) {
        if(Addresses.AskForDeviceRegistrationMethodName.equals(methodName)) {
          communicator2.respondToAskForDeviceRegistrationRequest((AskForDeviceRegistrationRequest)request, new AskForDeviceRegistrationResponse(true, true, true,
              UserInfo.fromUser(user2), GroupInfo.fromGroup(user2.getUsersDefaultGroup()), HostInfo.fromUserAndDevice(user2, device2),
              DeepThoughtInfo.fromDeepThought(user2.getLastViewedDeepThought()), TestIpAddress, device2MessagesPort), null);
        }
        return false;
      }
    });
    messagesReceiver2.start();
    try { Thread.sleep(200); } catch(Exception ex) { }

    final CountDownLatch waitLatch = new CountDownLatch(1);

    communicator.askForDeviceRegistration(createHostInfo(user2, device2, TestIpAddress, device2MessagesPort), loggedOnUser, localDevice, null);

    try { waitLatch.await(3, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertEquals(1, connector.getRegisteredDevicesManager().getRegisteredDevicesCount());
    Assert.assertEquals(1, registeredDevicesManager.getRegisteredDevicesCount());

//    Assert.assertEquals(loggedOnUser, registeredDevicesManager.);

    messagesReceiver2.stop();
  }



//  @Test
//  public void noConnectedDevices_ConnectionsAliveWatcherIsNotRunning() {
//    connector.shutDown();
//    connector.runAsync();
//    try { Thread.sleep(500); } catch(Exception ex) { } // wait same time till Servers have started
//
//    Assert.assertFalse(devicesFinder.isConnectionWatcherRunning());
//  }
//
//  @Test
//  public void deviceConnected_ConnectionsAliveWatcherIsRunning() {
//    DeepThoughtConnector connector = new DeepThoughtConnector(devicesFinder, threadPool);
//    connector.runAsync();
//    try { Thread.sleep(500); } catch(Exception ex) { } // wait same time till Servers have started
//
//    devicesFinder.connectedDevicesListener.registeredDeviceConnected(new ConnectedDevice("", "", 0));
//
//    Assert.assertTrue(devicesFinder.isConnectionWatcherRunning());
//  }
//
//  @Test
//  public void disconnectsFromLastDevice_IsNowNotConnectedAnymoreToAllRegisteredDevices_RegisteredDevicesSearcherGetsStarted() {
//    DeepThoughtConnector connector = new DeepThoughtConnector(devicesFinder, threadPool);
//    connector.runAsync();
//    try { Thread.sleep(500); } catch(Exception ex) { } // wait same time till Servers have started
//
//    ConnectedDevice connectedDevice = new ConnectedDevice("", "", 0);
//    devicesFinder.connectedDevicesListener.registeredDeviceConnected(connectedDevice);
//    devicesFinder.connectedDevicesListener.registeredDeviceDisconnected(connectedDevice);
//
//    Assert.assertFalse(devicesFinder.isConnectionWatcherRunning());
//  }



  protected HostInfo createLocalHostServerInfo() {
    return createHostInfo(Application.getLoggedOnUser(), Application.getApplication().getLocalDevice(), TestIpAddress, connector.getMessageReceiverPort());
  }

  protected HostInfo createHostInfo(User loggedOnUser, Device localDevice, String address, int port) {
    HostInfo hostInfo = HostInfo.fromUserAndDevice(Application.getLoggedOnUser(), Application.getApplication().getLocalDevice());
    hostInfo.setAddress(TestIpAddress);
    hostInfo.setMessagesPort(connector.getMessageReceiverPort());

    return hostInfo;
  }


  protected void mockNumberOfRegisteredDevices(IDeepThoughtConnector connector, int numberOfRegisteredDevices) {
    RegisteredDevicesManager registeredDevicesManager = Mockito.mock(RegisteredDevicesManager.class);
    ((DeepThoughtConnector)connector).setRegisteredDevicesManager(registeredDevicesManager);

    Mockito.when(registeredDevicesManager.getRegisteredDevicesCount()).thenReturn(numberOfRegisteredDevices);

    Mockito.when(registeredDevicesManager.hasRegisteredDevices()).thenReturn(numberOfRegisteredDevices > 0);
  }

  protected ConnectedDevicesManager mockNumberOfConnectedDevices(IDeepThoughtConnector connector, int numberOfConnectedDevices) {
    ConnectedDevicesManager connectedDevicesManager = Mockito.mock(ConnectedDevicesManager.class);
    ((DeepThoughtConnector)connector).setConnectedDevicesManager(connectedDevicesManager);

    Mockito.when(connectedDevicesManager.getConnectedDevicesCount()).thenReturn(numberOfConnectedDevices);

    return connectedDevicesManager;
  }

}

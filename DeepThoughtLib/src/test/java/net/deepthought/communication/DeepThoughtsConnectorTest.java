package net.deepthought.communication;

import net.deepthought.Application;
import net.deepthought.TestApplicationConfiguration;
import net.deepthought.communication.connected_device.ConnectedDevicesManager;
import net.deepthought.communication.listener.AskForDeviceRegistrationResultListener;
import net.deepthought.communication.listener.ImportFilesOrDoOcrListener;
import net.deepthought.communication.listener.MessagesReceiverListener;
import net.deepthought.communication.messages.AsynchronousResponseListenerManager;
import net.deepthought.communication.messages.DeepThoughtMessagesReceiverConfig;
import net.deepthought.communication.messages.MessagesDispatcher;
import net.deepthought.communication.messages.MessagesReceiver;
import net.deepthought.communication.messages.request.AskForDeviceRegistrationRequest;
import net.deepthought.communication.messages.request.DoOcrRequest;
import net.deepthought.communication.messages.request.ImportFilesRequest;
import net.deepthought.communication.messages.request.Request;
import net.deepthought.communication.messages.request.RequestWithAsynchronousResponse;
import net.deepthought.communication.messages.request.StopRequestWithAsynchronousResponse;
import net.deepthought.communication.messages.response.AskForDeviceRegistrationResponse;
import net.deepthought.communication.model.ConnectedDevice;
import net.deepthought.communication.model.DeviceInfo;
import net.deepthought.communication.model.DoOcrConfiguration;
import net.deepthought.communication.model.GroupInfo;
import net.deepthought.communication.model.HostInfo;
import net.deepthought.communication.model.ImportFilesConfiguration;
import net.deepthought.communication.model.ImportFilesSource;
import net.deepthought.communication.model.OcrSource;
import net.deepthought.communication.model.UserInfo;
import net.deepthought.communication.registration.IUnregisteredDevicesListener;
import net.deepthought.communication.registration.RegisteredDevicesManager;
import net.deepthought.data.model.Device;
import net.deepthought.data.model.User;

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
 * Created by ganymed on 19/08/15.
 */
public class DeepThoughtsConnectorTest extends CommunicationTestBase {

  protected IDeepThoughtsConnector connector;

  protected Communicator communicator;


  @Override
  public void setup() throws Exception {
    super.setup();

    Application.instantiate(new TestApplicationConfiguration());
    try { Thread.sleep(200); } catch(Exception ex) { } // it is very critical that server is fully started therefore wait some time

    connector = Application.getDeepThoughtsConnector();
    communicator = connector.getCommunicator();

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

    connector.openUserDeviceRegistrationServer(new IUnregisteredDevicesListener() {
      @Override
      public void unregisteredDeviceFound(HostInfo hostInfo) {

      }

      @Override
      public void deviceIsAskingForRegistration(AskForDeviceRegistrationRequest request) {
        methodCalled.set(true);
        waitLatch.countDown();
      }
    });

    communicator.askForDeviceRegistration(createLocalHostServerInfo(), loggedOnUser, localDevice, null);

    try { waitLatch.await(2, TimeUnit.SECONDS); } catch(Exception ex) { }
    connector.closeUserDeviceRegistrationServer();

    Assert.assertTrue(methodCalled.get());
  }

  @Test
  public void askForDeviceRegistration_RegistrationIsProhibitedByServer_RegistrationDeniedResponseIsReceived() {
    final List<AskForDeviceRegistrationResponse> responses = new ArrayList<>();
    final CountDownLatch waitLatch = new CountDownLatch(1);

    connector.openUserDeviceRegistrationServer(new IUnregisteredDevicesListener() {
      @Override
      public void unregisteredDeviceFound(HostInfo hostInfo) {

      }

      @Override
      public void deviceIsAskingForRegistration(AskForDeviceRegistrationRequest request) {
        communicator.respondToAskForDeviceRegistrationRequest(request, AskForDeviceRegistrationResponse.Deny, null);
      }
    });

    communicator.askForDeviceRegistration(createLocalHostServerInfo(), loggedOnUser, localDevice, new AskForDeviceRegistrationResultListener() {
      @Override
      public void responseReceived(AskForDeviceRegistrationRequest request, AskForDeviceRegistrationResponse response) {
        responses.add(response);
        waitLatch.countDown();
      }
    });

    try { waitLatch.await(3, TimeUnit.SECONDS); } catch(Exception ex) { }
    connector.closeUserDeviceRegistrationServer();

    Assert.assertFalse(responses.get(0).allowsRegistration());
  }

  @Test
  public void askForDeviceRegistration_ServerAllowsRegistration_RegistrationAllowedResponseIsReceived() {
    final List<AskForDeviceRegistrationResponse> responses = new ArrayList<>();
    final CountDownLatch waitLatch = new CountDownLatch(1);

    connector.openUserDeviceRegistrationServer(new IUnregisteredDevicesListener() {
      @Override
      public void unregisteredDeviceFound(HostInfo hostInfo) {

      }

      @Override
      public void deviceIsAskingForRegistration(AskForDeviceRegistrationRequest request) {
        communicator.respondToAskForDeviceRegistrationRequest(request, AskForDeviceRegistrationResponse.createAllowRegistrationResponse(true,
            Application.getLoggedOnUser(), Application.getApplication().getLocalDevice()), null);
      }
    });

    communicator.askForDeviceRegistration(createLocalHostServerInfo(), loggedOnUser, localDevice, new AskForDeviceRegistrationResultListener() {
      @Override
      public void responseReceived(AskForDeviceRegistrationRequest request, AskForDeviceRegistrationResponse response) {
        responses.add(response);
        waitLatch.countDown();
      }
    });

    try { waitLatch.await(3, TimeUnit.SECONDS); } catch(Exception ex) { }
    connector.closeUserDeviceRegistrationServer();

    Assert.assertTrue(responses.get(0).allowsRegistration());
  }

  @Test
  public void askForDeviceRegistration_ServerAllowsRegistration_BothDevicesHaveRegisteredOtherEndpoint() throws IOException {
    final int device2MessagesPort = 43210;
    final User user2 = User.createNewLocalUser();
    final Device device2 = new Device("test2", "test2", "test2");
    AsynchronousResponseListenerManager listenerManager2 = new AsynchronousResponseListenerManager();

    final Communicator communicator2 = new Communicator(new CommunicatorConfig(new MessagesDispatcher(threadPool), listenerManager2, device2MessagesPort,
        new ConnectorMessagesCreator(new ConnectorMessagesCreatorConfig(user2, device2, TestIpAddress, device2MessagesPort)), registeredDevicesManager));
    MessagesReceiver messagesReceiver2 = new MessagesReceiver(new DeepThoughtMessagesReceiverConfig(device2MessagesPort, listenerManager2), new MessagesReceiverListener() {
      @Override
      public boolean messageReceived(String methodName, Request request) {
        if(Addresses.AskForDeviceRegistrationMethodName.equals(methodName)) {
          communicator2.respondToAskForDeviceRegistrationRequest((AskForDeviceRegistrationRequest)request, new AskForDeviceRegistrationResponse(true, true,
              UserInfo.fromUser(user2), GroupInfo.fromGroup(user2.getUsersDefaultGroup()), DeviceInfo.fromDevice(device2), TestIpAddress, device2MessagesPort), null);
        }
        return false;
      }
    });
    messagesReceiver2.start();
    try { Thread.sleep(200); } catch(Exception ex) { }

    final CountDownLatch waitLatch = new CountDownLatch(1);

    communicator.askForDeviceRegistration(new HostInfo(user2.getUniversallyUniqueId(), user2.getUserName(), device2.getUniversallyUniqueId(), device2.getName(),
        device2.getPlatform(), device2.getOsVersion(), device2.getPlatformArchitecture(), TestIpAddress, device2MessagesPort), loggedOnUser, localDevice, null);

    try { waitLatch.await(3, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertEquals(1, connector.getRegisteredDevicesManager().getRegisteredDevicesCount());
    Assert.assertEquals(1, registeredDevicesManager.getRegisteredDevicesCount());

//    Assert.assertEquals(loggedOnUser, registeredDevicesManager.);

    connector.closeUserDeviceRegistrationServer();
    messagesReceiver2.stop();
  }


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

    ConnectedDevice disconnectedDevice = new ConnectedDevice("", "", 0);
    ConnectedDevicesManager mockConnectedDevicesManager = mockNumberOfConnectedDevices(connector, 1);
    Mockito.when(mockConnectedDevicesManager.disconnectedFromDevice(disconnectedDevice)).thenReturn(true);

    connector.registeredDeviceDisconnectedListener.registeredDeviceDisconnected(disconnectedDevice);

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


  @Test
  public void startCaptureImage_CaptureImageOrDoOcrListenerGetsCalled() {
    final AtomicBoolean methodCalled = new AtomicBoolean(false);
    final CountDownLatch waitLatch = new CountDownLatch(1);

    connector.addImportFilesOrDoOcrListener(new ImportFilesOrDoOcrListener() {

      @Override
      public void importFiles(ImportFilesRequest request) {
        methodCalled.set(true);
        waitLatch.countDown();
      }

      @Override
      public void doOcr(DoOcrRequest request) {

      }

      @Override
      public void scanBarcode(RequestWithAsynchronousResponse request) {

      }

      @Override
      public void stopCaptureImageOrDoOcr(StopRequestWithAsynchronousResponse request) {

      }
    });

    communicator.startImportFiles(localHost, new ImportFilesConfiguration(ImportFilesSource.CaptureImage), null);

    try { waitLatch.await(2, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertTrue(methodCalled.get());
  }

  @Test
  public void startCaptureImageAndDoOcr_CaptureImageOrDoOcrListenerGetsCalled() {
    final AtomicBoolean methodCalled = new AtomicBoolean(false);
    final CountDownLatch waitLatch = new CountDownLatch(1);

    connector.addImportFilesOrDoOcrListener(new ImportFilesOrDoOcrListener() {

      @Override
      public void importFiles(ImportFilesRequest request) {

      }

      @Override
      public void doOcr(DoOcrRequest request) {
        methodCalled.set(true);
        waitLatch.countDown();
      }

      @Override
      public void scanBarcode(RequestWithAsynchronousResponse request) {

      }

      @Override
      public void stopCaptureImageOrDoOcr(StopRequestWithAsynchronousResponse request) {

      }
    });

    communicator.startDoOcr(localHost, new DoOcrConfiguration(OcrSource.CaptureImage), null);

    try { waitLatch.await(2, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertTrue(methodCalled.get());
  }


  @Test
  public void startSelectRemoteImageAndDoOcr_CaptureImageOrDoOcrListenerGetsCalled() {
    final AtomicBoolean methodCalled = new AtomicBoolean(false);
    final CountDownLatch waitLatch = new CountDownLatch(1);

    connector.addImportFilesOrDoOcrListener(new ImportFilesOrDoOcrListener() {

      @Override
      public void importFiles(ImportFilesRequest request) {

      }

      @Override
      public void doOcr(DoOcrRequest request) {
        methodCalled.set(true);
        waitLatch.countDown();
      }

      @Override
      public void scanBarcode(RequestWithAsynchronousResponse request) {

      }

      @Override
      public void stopCaptureImageOrDoOcr(StopRequestWithAsynchronousResponse request) {

      }
    });

    communicator.startDoOcr(localHost, new DoOcrConfiguration(OcrSource.SelectAnExistingImageOnDevice), null);

    try { waitLatch.await(2, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertTrue(methodCalled.get());
  }


  @Test
  public void startRecognizeFromUri_CaptureImageOrDoOcrListenerGetsCalled() {
    final AtomicBoolean methodCalled = new AtomicBoolean(false);
    final CountDownLatch waitLatch = new CountDownLatch(1);

    connector.addImportFilesOrDoOcrListener(new ImportFilesOrDoOcrListener() {

      @Override
      public void importFiles(ImportFilesRequest request) {

      }

      @Override
      public void doOcr(DoOcrRequest request) {
        methodCalled.set(true);
        waitLatch.countDown();
      }

      @Override
      public void scanBarcode(RequestWithAsynchronousResponse request) {

      }

      @Override
      public void stopCaptureImageOrDoOcr(StopRequestWithAsynchronousResponse request) {

      }
    });

    communicator.startDoOcr(localHost, new DoOcrConfiguration(OcrSource.RecognizeFromUri), null);

    try { waitLatch.await(2, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertTrue(methodCalled.get());
  }


  @Test
  public void startAskUserForSourceAndDoOcr_CaptureImageOrDoOcrListenerGetsCalled() {
    final AtomicBoolean methodCalled = new AtomicBoolean(false);
    final CountDownLatch waitLatch = new CountDownLatch(1);

    connector.addImportFilesOrDoOcrListener(new ImportFilesOrDoOcrListener() {

      @Override
      public void importFiles(ImportFilesRequest request) {

      }

      @Override
      public void doOcr(DoOcrRequest request) {
        methodCalled.set(true);
        waitLatch.countDown();
      }

      @Override
      public void scanBarcode(RequestWithAsynchronousResponse request) {

      }

      @Override
      public void stopCaptureImageOrDoOcr(StopRequestWithAsynchronousResponse request) {

      }
    });

    communicator.startDoOcr(localHost, new DoOcrConfiguration(OcrSource.AskUser), null);

    try { waitLatch.await(2, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertTrue(methodCalled.get());
  }


  @Test
  public void startDoOcrOnLocalImage_CaptureImageOrDoOcrListenerGetsCalled() {
    final AtomicBoolean methodCalled = new AtomicBoolean(false);
    final CountDownLatch waitLatch = new CountDownLatch(1);

    connector.addImportFilesOrDoOcrListener(new ImportFilesOrDoOcrListener() {

      @Override
      public void importFiles(ImportFilesRequest request) {

      }

      @Override
      public void doOcr(DoOcrRequest request) {
        methodCalled.set(true);
        waitLatch.countDown();
      }

      @Override
      public void scanBarcode(RequestWithAsynchronousResponse request) {

      }

      @Override
      public void stopCaptureImageOrDoOcr(StopRequestWithAsynchronousResponse request) {

      }
    });

    communicator.startDoOcr(localHost, new DoOcrConfiguration(new byte[0]), null);

    try { waitLatch.await(2, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertTrue(methodCalled.get());
  }


  @Test
  public void startScanBarcode_CaptureImageOrDoOcrListenerGetsCalled() {
    final AtomicBoolean methodCalled = new AtomicBoolean(false);
    final CountDownLatch waitLatch = new CountDownLatch(1);

    connector.addImportFilesOrDoOcrListener(new ImportFilesOrDoOcrListener() {

      @Override
      public void importFiles(ImportFilesRequest request) {

      }

      @Override
      public void doOcr(DoOcrRequest request) {

      }

      @Override
      public void scanBarcode(RequestWithAsynchronousResponse request) {
        methodCalled.set(true);
        waitLatch.countDown();
      }

      @Override
      public void stopCaptureImageOrDoOcr(StopRequestWithAsynchronousResponse request) {

      }
    });

    communicator.startScanBarcode(localHost, null);

    try { waitLatch.await(200, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertTrue(methodCalled.get());
  }



  protected HostInfo createLocalHostServerInfo() {
    HostInfo hostInfo = HostInfo.fromUserAndDevice(Application.getLoggedOnUser(), Application.getApplication().getLocalDevice());
    hostInfo.setIpAddress(TestIpAddress);
    hostInfo.setPort(connector.getMessageReceiverPort());

    return hostInfo;
  }


  protected void mockNumberOfRegisteredDevices(IDeepThoughtsConnector connector, int numberOfRegisteredDevices) {
    RegisteredDevicesManager registeredDevicesManager = Mockito.mock(RegisteredDevicesManager.class);
    ((DeepThoughtsConnector)connector).setRegisteredDevicesManager(registeredDevicesManager);

    Mockito.when(registeredDevicesManager.getRegisteredDevicesCount()).thenReturn(numberOfRegisteredDevices);

    Mockito.when(registeredDevicesManager.hasRegisteredDevices()).thenReturn(numberOfRegisteredDevices > 0);
  }

  protected ConnectedDevicesManager mockNumberOfConnectedDevices(IDeepThoughtsConnector connector, int numberOfConnectedDevices) {
    ConnectedDevicesManager connectedDevicesManager = Mockito.mock(ConnectedDevicesManager.class);
    ((DeepThoughtsConnector)connector).setConnectedDevicesManager(connectedDevicesManager);

    Mockito.when(connectedDevicesManager.getConnectedDevicesCount()).thenReturn(numberOfConnectedDevices);

    return connectedDevicesManager;
  }
}

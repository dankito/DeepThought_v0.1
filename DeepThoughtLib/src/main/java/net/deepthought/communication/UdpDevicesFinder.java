package net.deepthought.communication;

import net.deepthought.communication.connected_device.ConnectedDevicesManager;
import net.deepthought.communication.connected_device.IConnectedDevicesListener;
import net.deepthought.communication.connected_device.RegisteredDevicesSearcher;
import net.deepthought.communication.messages.request.AskForDeviceRegistrationRequest;
import net.deepthought.communication.model.ConnectedDevice;
import net.deepthought.communication.model.HostInfo;
import net.deepthought.communication.registration.IUnregisteredDevicesListener;
import net.deepthought.communication.registration.LookingForRegistrationServersClient;
import net.deepthought.communication.registration.RegisteredDevicesManager;
import net.deepthought.communication.registration.RegistrationServer;
import net.deepthought.data.model.Device;
import net.deepthought.data.model.User;
import net.deepthought.util.IThreadPool;

/**
 * Created by ganymed on 05/06/16.
 */
public class UdpDevicesFinder implements IDevicesFinder {

  protected IDevicesFinderListener listener = null;

  protected RegistrationServer registrationServer = null;

  protected RegisteredDevicesSearcher registeredDevicesSearcher;

  protected LookingForRegistrationServersClient searchRegistrationServersClient = null;

  protected ConnectionsAliveWatcher connectionsAliveWatcher = null;

  // TODO: try to get rid of

  protected Communicator communicator;

  protected IThreadPool threadPool;

  protected RegisteredDevicesManager registeredDevicesManager;

  protected ConnectedDevicesManager connectedDevicesManager;

  protected ConnectorMessagesCreator connectorMessagesCreator;

  protected User loggedOnUser;

  protected Device localDevice;


  public UdpDevicesFinder(Communicator communicator, IThreadPool threadPool, RegisteredDevicesManager registeredDevicesManager, ConnectedDevicesManager connectedDevicesManager, ConnectorMessagesCreator connectorMessagesCreator, User loggedOnUser, Device localDevice) {
    this.communicator = communicator;
    this.threadPool = threadPool;
    this.registeredDevicesManager = registeredDevicesManager;
    this.connectedDevicesManager = connectedDevicesManager;
    this.connectorMessagesCreator = connectorMessagesCreator;
    this.loggedOnUser = loggedOnUser;
    this.localDevice = localDevice;
  }

  @Override
  public void startAsync(final IDevicesFinderListener listener) {
    threadPool.runTaskAsync(new Runnable() {
      @Override
      public void run() {
        start(listener);
      }
    });
  }

  protected void start(IDevicesFinderListener listener) {
    this.listener = listener;

    mayStartRegisteredDevicesSearcher();
    mayStartConnectionsAliveWatcher();

    openUserDeviceRegistrationServer();
    findOtherUserDevicesToRegisterAtAsync();
  }

  @Override
  public void stop() {

    stopRegisteredDevicesSearcher();
    stopConnectionsAliveWatcher();

    stopRegisteredDevicesSearcher();
    stopSearchingOtherUserDevicesToRegisterAt();
  }



  protected void mayStartRegisteredDevicesSearcher() {
    if(registeredDevicesManager.hasRegisteredDevices() && connectedDevicesManager.getConnectedDevicesCount() < registeredDevicesManager.getRegisteredDevicesCount() &&
        isRegisteredDevicesSearcherRunning() == false) {
      startRegisteredDevicesSearcher();
    }
  }

  protected void startRegisteredDevicesSearcher() {
    stopRegisteredDevicesSearcher();

    registeredDevicesSearcher = new RegisteredDevicesSearcher(connectorMessagesCreator, threadPool, registeredDevicesManager, connectedDevicesManager, loggedOnUser, localDevice);
    registeredDevicesSearcher.startSearchingAsync(connectedDevicesListener);
  }

  protected void stopRegisteredDevicesSearcher() {
    if(registeredDevicesSearcher != null) {
      registeredDevicesSearcher.stopSearching();
      registeredDevicesSearcher = null;
    }
  }


  protected void mayStartConnectionsAliveWatcher() {
    if(connectedDevicesManager.areDevicesConnected() && isConnectionWatcherRunning() == false)
      startConnectionsAliveWatcher();
  }

  protected void startConnectionsAliveWatcher() {
    stopConnectionsAliveWatcher();

    connectionsAliveWatcher = new ConnectionsAliveWatcher(connectedDevicesManager, communicator);
    connectionsAliveWatcher.startWatchingAsync(connectedDevicesListener);
  }

  protected void mayStopConnectionsAliveWatcher() {
    if(connectedDevicesManager.getConnectedDevicesCount() == 0 && isConnectionWatcherRunning() == true)
      stopConnectionsAliveWatcher();
  }

  protected void stopConnectionsAliveWatcher() {
    if(connectionsAliveWatcher != null) {
      connectionsAliveWatcher.stopWatching();
      connectionsAliveWatcher = null;
    }
  }


  public void openUserDeviceRegistrationServer() {
    if(isRegistrationServerRunning()) {
      closeUserDeviceRegistrationServer();
    }

    registrationServer = new RegistrationServer(connectorMessagesCreator, threadPool);
    registrationServer.startRegistrationServerAsync();
  }

  public void closeUserDeviceRegistrationServer() {
    if(registrationServer != null) {
      registrationServer.closeRegistrationServer();
      registrationServer = null;
    }
  }

  public void findOtherUserDevicesToRegisterAtAsync() {
    if(isSearchRegistrationServersClientRunning())
      stopSearchingOtherUserDevicesToRegisterAt();

    searchRegistrationServersClient = new LookingForRegistrationServersClient(connectorMessagesCreator, registeredDevicesManager, threadPool);
    searchRegistrationServersClient.findRegistrationServersAsync(unregisteredDevicesListener);
  }

  public void stopSearchingOtherUserDevicesToRegisterAt() {
    if(searchRegistrationServersClient != null) {
      searchRegistrationServersClient.stopSearchingForRegistrationServers();
      searchRegistrationServersClient = null;
    }
  }

  protected void mayStopRegisteredDevicesSearcher() {
    if(isRegisteredDevicesSearcherRunning() &&
        connectedDevicesManager.getConnectedDevicesCount() >= registeredDevicesManager.getRegisteredDevicesCount())
      stopRegisteredDevicesSearcher();
  }


  protected void connectedToDevice() {
    if(listener != null) {
      listener.deviceFound();
    }

    mayStopRegisteredDevicesSearcher();
    mayStartConnectionsAliveWatcher();
  }

  protected void disconnectedFromDevice() {
    if(listener != null) {
      listener.deviceDisconnected();
    }

    mayStartRegisteredDevicesSearcher();
    mayStopConnectionsAliveWatcher();
  }


  protected IConnectedDevicesListener connectedDevicesListener = new IConnectedDevicesListener() {
    @Override
    public void registeredDeviceConnected(ConnectedDevice device) {
      connectedToDevice();
    }

    @Override
    public void registeredDeviceDisconnected(ConnectedDevice device) {
      disconnectedFromDevice();
    }
  };

  protected IUnregisteredDevicesListener unregisteredDevicesListener = new IUnregisteredDevicesListener() {
    @Override
    public void unregisteredDeviceFound(HostInfo hostInfo) {
      connectedToDevice();
    }

    @Override
    public void deviceIsAskingForRegistration(AskForDeviceRegistrationRequest request) {

    }
  };


  public boolean isRegisteringAllowed() {
    return isRegistrationServerRunning();
  }

  public boolean isRegistrationServerRunning() {
    return registrationServer != null;
  }

  public boolean isSearchRegistrationServersClientRunning() {
    return searchRegistrationServersClient != null;
  }

  public boolean isRegisteredDevicesSearcherRunning() {
    return registeredDevicesSearcher != null;
  }

  public boolean isConnectionWatcherRunning() {
    return connectionsAliveWatcher != null;
  }

}

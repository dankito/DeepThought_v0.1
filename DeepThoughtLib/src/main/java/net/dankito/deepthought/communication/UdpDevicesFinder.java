package net.dankito.deepthought.communication;

import net.dankito.deepthought.communication.connected_device.ConnectedDevicesManager;
import net.dankito.deepthought.communication.connected_device.IConnectedDevicesListener;
import net.dankito.deepthought.communication.connected_device.UdpDevicesSearcher;
import net.dankito.deepthought.communication.messages.request.AskForDeviceRegistrationRequest;
import net.dankito.deepthought.communication.model.ConnectedDevice;
import net.dankito.deepthought.communication.model.HostInfo;
import net.dankito.deepthought.communication.registration.IUnregisteredDevicesListener;
import net.dankito.deepthought.util.IThreadPool;

/**
 * Created by ganymed on 05/06/16.
 */
public class UdpDevicesFinder implements IDevicesFinder {

  protected IDevicesFinderListener listener = null;

  protected UdpDevicesSearcher udpDevicesSearcher;

  protected ConnectionsAliveWatcher connectionsAliveWatcher = null;

  // TODO: try to get rid of

  protected Communicator communicator;

  protected IThreadPool threadPool;

  protected ConnectedDevicesManager connectedDevicesManager;

  protected ConnectorMessagesCreator connectorMessagesCreator;


  public UdpDevicesFinder(Communicator communicator, IThreadPool threadPool, ConnectedDevicesManager connectedDevicesManager, ConnectorMessagesCreator connectorMessagesCreator) {
    this.communicator = communicator;
    this.threadPool = threadPool;
    this.connectedDevicesManager = connectedDevicesManager;
    this.connectorMessagesCreator = connectorMessagesCreator;
  }

  @Override
  public void startAsync(final HostInfo localHost, final int searchDevicesPort, final IDevicesFinderListener listener) {
    threadPool.runTaskAsync(new Runnable() {
      @Override
      public void run() {
        start(localHost, searchDevicesPort, listener);
      }
    });
  }

  protected void start(HostInfo localHost, int searchDevicesPort, IDevicesFinderListener listener) {
    this.listener = listener;

    startDevicesSearcher(localHost, searchDevicesPort, listener);
    mayStartConnectionsAliveWatcher();
  }

  @Override
  public void stop() {
    stopDevicesSearcher();
    stopConnectionsAliveWatcher();
  }


  protected void startDevicesSearcher(HostInfo localHost, int searchDevicesPort, IDevicesFinderListener listener) {
    stopDevicesSearcher();

    udpDevicesSearcher = new UdpDevicesSearcher(connectorMessagesCreator, threadPool);
    udpDevicesSearcher.startSearchingAsync(localHost, searchDevicesPort, listener);
  }

  protected void stopDevicesSearcher() {
    if(udpDevicesSearcher != null) {
      udpDevicesSearcher.stopSearching();
      udpDevicesSearcher = null;
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


  protected void connectedToDevice(HostInfo device) {
    if(listener != null) {
      listener.deviceFound(device);
    }

    mayStartConnectionsAliveWatcher();
  }

  protected void disconnectedFromDevice(HostInfo device) {
    if(listener != null) {
      listener.deviceDisconnected(device);
    }

    mayStopConnectionsAliveWatcher();
  }


  protected IConnectedDevicesListener connectedDevicesListener = new IConnectedDevicesListener() {
    @Override
    public void registeredDeviceConnected(ConnectedDevice device) {
      connectedToDevice(device);
    }

    @Override
    public void registeredDeviceDisconnected(ConnectedDevice device) {
      disconnectedFromDevice(device);
    }
  };

  protected IUnregisteredDevicesListener unregisteredDevicesListener = new IUnregisteredDevicesListener() {
    @Override
    public void unregisteredDeviceFound(HostInfo device) {
      connectedToDevice(device);
    }

    @Override
    public void deviceIsAskingForRegistration(AskForDeviceRegistrationRequest request) {

    }
  };


  public boolean isRegisteredDevicesSearcherRunning() {
    return udpDevicesSearcher != null;
  }

  public boolean isConnectionWatcherRunning() {
    return connectionsAliveWatcher != null;
  }

  @Override
  public boolean isRunning() {
    return isRegisteredDevicesSearcherRunning() && isConnectionWatcherRunning();
  }

}

package net.deepthought.communication;

import net.deepthought.communication.connected_device.ConnectedDevicesManager;
import net.deepthought.communication.listener.RegisteredDeviceDisconnectedListener;
import net.deepthought.communication.listener.ResponseListener;
import net.deepthought.communication.messages.Request;
import net.deepthought.communication.messages.Response;
import net.deepthought.communication.model.ConnectedDevice;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by ganymed on 24/08/15.
 */
public class ConnectionsAliveWatcher {

  protected int checkConnectionsAliveInterval;

  protected Timer connectionsAliveCheckTimer = null;

  protected ConnectedDevicesManager connectedDevicesManager;

  protected Communicator communicator;


  public ConnectionsAliveWatcher(ConnectedDevicesManager connectedDevicesManager, Communicator communicator) {
    this(connectedDevicesManager, communicator, Constants.ConnectionsAliveWatcherDefaultInterval);
  }

  public ConnectionsAliveWatcher(ConnectedDevicesManager connectedDevicesManager, Communicator communicator, int checkConnectionsAliveInterval) {
    this.connectedDevicesManager = connectedDevicesManager;
    this.communicator = communicator;
    this.checkConnectionsAliveInterval = checkConnectionsAliveInterval;
  }


  public void startWatchingAsync(final RegisteredDeviceDisconnectedListener listener) {
    stopWatching();

    connectionsAliveCheckTimer = new Timer("ConnectionsAliveWatcher Timer");
    connectionsAliveCheckTimer.scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
        checkIfConnectedDevicesStillAreConnected(listener);
      }
    }, checkConnectionsAliveInterval, checkConnectionsAliveInterval);
  }

  public void stopWatching() {
      if(connectionsAliveCheckTimer != null) {
        connectionsAliveCheckTimer.cancel();
        connectionsAliveCheckTimer = null;
      }
  }


  protected void checkIfConnectedDevicesStillAreConnected(final RegisteredDeviceDisconnectedListener listener) {
    for(final ConnectedDevice connectedDevice : connectedDevicesManager.getConnectedDevices()) {
      new Thread(new Runnable() {
        @Override
        public void run() {
          communicator.sendHeartbeat(connectedDevice, new ResponseListener() {
            @Override
            public void responseReceived(Request request, Response response) {
              if(response == null)
                deviceDisconnected(connectedDevice, listener);
            }
          });
        }
      }).start();
    }
  }

  protected void deviceDisconnected(ConnectedDevice connectedDevice, RegisteredDeviceDisconnectedListener listener) {
    if(listener != null)
      listener.registeredDeviceDisconnected(connectedDevice);
  }
}

package net.dankito.deepthought.communication;

import net.dankito.deepthought.communication.connected_device.IConnectedDevicesManager;
import net.dankito.deepthought.communication.model.ConnectedDevice;
import net.dankito.deepthought.communication.model.HostInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ganymed on 24/08/15.
 */
public class ConnectionsAliveWatcher {

  private static final Logger log = LoggerFactory.getLogger(ConnectionsAliveWatcher.class);


  protected int connectionTimeout;

  protected Timer connectionsAliveCheckTimer = null;

  protected Map<String, Long> lastMessageReceivedFromDeviceTimestamps = new ConcurrentHashMap<>();

  protected IConnectedDevicesManager connectedDevicesManager;


  public ConnectionsAliveWatcher(IConnectedDevicesManager connectedDevicesManager) {
    this(connectedDevicesManager, (int)(Constants.SendWeAreAliveMessageInterval * 3.5));
  }

  public ConnectionsAliveWatcher(IConnectedDevicesManager connectedDevicesManager, int connectionTimeout) {
    this.connectedDevicesManager = connectedDevicesManager;
    this.connectionTimeout = connectionTimeout;
  }


  public boolean isRunning() {
    return connectionsAliveCheckTimer != null;
  }

  public void startWatchingAsync(final IConnectionsAliveWatcherListener listener) {
    log.info("Starting ConnectionsAliveWatcher ...");

    synchronized(this) {
      stopWatching();

      connectionsAliveCheckTimer = new Timer("ConnectionsAliveWatcher Timer");
      connectionsAliveCheckTimer.scheduleAtFixedRate(new TimerTask() {
        @Override
        public void run() {
          checkIfConnectedDevicesStillAreConnected(listener);
        }
      }, connectionTimeout, connectionTimeout);
    }
  }

  public void stopWatching() {
    log.info("Stopping ConnectionsAliveWatcher ...");

    synchronized(this) {
      if (connectionsAliveCheckTimer != null) {
        connectionsAliveCheckTimer.cancel();
        connectionsAliveCheckTimer = null;
      }
    }
  }


  public void receivedMessageFromDevice(HostInfo device) {
    lastMessageReceivedFromDeviceTimestamps.put(getDeviceKey(device), new Date().getTime());
  }


  protected void checkIfConnectedDevicesStillAreConnected(final IConnectionsAliveWatcherListener listener) {
    Long now = new Date().getTime();

    for(final ConnectedDevice connectedDevice : new ArrayList<>(connectedDevicesManager.getConnectedDevices())) {
      if(hasDeviceExpired(connectedDevice, now)) {
        deviceDisconnected(connectedDevice, listener);
      }
    }
  }

  protected boolean hasDeviceExpired(ConnectedDevice connectedDevice, Long now) {
    Long lastMessageReceivedFromDeviceTimestamp = lastMessageReceivedFromDeviceTimestamps.get(getDeviceKey(connectedDevice));

    if(lastMessageReceivedFromDeviceTimestamp != null) {
      return lastMessageReceivedFromDeviceTimestamp < now - connectionTimeout;
    }

    return false;
  }

  protected void deviceDisconnected(ConnectedDevice connectedDevice, IConnectionsAliveWatcherListener listener) {
    lastMessageReceivedFromDeviceTimestamps.remove(getDeviceKey(connectedDevice));

    if(listener != null) {
      listener.deviceDisconnected(connectedDevice);
    }
  }

  protected String getDeviceKey(ConnectedDevice connectedDevice) {
    return connectedDevice.getDevice().getName(); // TODO: use deviceId as soon as synchronizing is working
  }

  protected String getDeviceKey(HostInfo device) {
    return device.getDeviceName(); // TODO: use deviceId as soon as synchronizing is working
  }

}

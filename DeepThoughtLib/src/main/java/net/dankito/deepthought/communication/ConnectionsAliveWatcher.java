package net.dankito.deepthought.communication;

import net.dankito.deepthought.communication.model.HostInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
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


  public ConnectionsAliveWatcher() {
    this((int)(Constants.SendWeAreAliveMessageInterval * 3.5));
  }

  public ConnectionsAliveWatcher(int connectionTimeout) {
    this.connectionTimeout = connectionTimeout;
  }


  public boolean isRunning() {
    return connectionsAliveCheckTimer != null;
  }

  public void startWatchingAsync(final List<HostInfo> foundDevices, final IConnectionsAliveWatcherListener listener) {
    log.info("Starting ConnectionsAliveWatcher ...");

    synchronized(this) {
      stopWatching();

      connectionsAliveCheckTimer = new Timer("ConnectionsAliveWatcher Timer");
      connectionsAliveCheckTimer.scheduleAtFixedRate(new TimerTask() {
        @Override
        public void run() {
          checkIfConnectedDevicesStillAreConnected(foundDevices, listener);
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


  protected void checkIfConnectedDevicesStillAreConnected(List<HostInfo> foundDevices, final IConnectionsAliveWatcherListener listener) {
    Long now = new Date().getTime();

    for(final HostInfo connectedDevice : foundDevices) {
      if(hasDeviceExpired(connectedDevice, now)) {
        deviceDisconnected(connectedDevice, listener);
      }
    }
  }

  protected boolean hasDeviceExpired(HostInfo connectedDevice, Long now) {
    Long lastMessageReceivedFromDeviceTimestamp = lastMessageReceivedFromDeviceTimestamps.get(getDeviceKey(connectedDevice));

    if(lastMessageReceivedFromDeviceTimestamp != null) {
      return lastMessageReceivedFromDeviceTimestamp < now - connectionTimeout;
    }

    return false;
  }

  protected void deviceDisconnected(HostInfo connectedDevice, IConnectionsAliveWatcherListener listener) {
    lastMessageReceivedFromDeviceTimestamps.remove(getDeviceKey(connectedDevice));

    if(listener != null) {
      listener.deviceDisconnected(connectedDevice);
    }
  }

  protected String getDeviceKey(HostInfo device) {
    return device.getDeviceName(); // TODO: use deviceId as soon as synchronizing is working
  }

}

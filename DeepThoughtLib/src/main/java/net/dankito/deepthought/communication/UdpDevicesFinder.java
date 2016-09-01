package net.dankito.deepthought.communication;

import net.dankito.deepthought.communication.model.ConnectedDevice;
import net.dankito.deepthought.communication.model.HostInfo;
import net.dankito.deepthought.util.IThreadPool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by ganymed on 05/06/16.
 */
public class UdpDevicesFinder implements IDevicesFinder {

  private final static Logger log = LoggerFactory.getLogger(UdpDevicesFinder.class);


  protected ConnectionsAliveWatcher connectionsAliveWatcher = null;

  protected ConnectorMessagesCreator messagesCreator;

  protected IThreadPool threadPool;

  protected DatagramSocket listenerSocket = null;
  protected boolean isListenerSocketOpened = false;

  protected List<DatagramSocket> openedBroadcastSockets = new ArrayList<>();
  protected boolean areBroadcastSocketsOpened = false;

  protected List<HostInfo> foundDevices = new CopyOnWriteArrayList<>();


  public UdpDevicesFinder(ConnectorMessagesCreator messagesCreator, IThreadPool threadPool) {
    this.messagesCreator = messagesCreator;
    this.threadPool = threadPool;
    // 3.5 = from 3 messages one must be received to be still valued as 'connected'
    this.connectionsAliveWatcher = new ConnectionsAliveWatcher((int)(Constants.SendWeAreAliveMessageInterval * 3.5));
  }


  @Override
  public boolean isRunning() {
    return isListenerSocketOpened && areBroadcastSocketsOpened;
  }

  @Override
  public void startAsync(HostInfo localHost, int searchDevicesPort, IDevicesFinderListener listener) {
    log.info("Starting UdpDevicesFinder ...");

    startListenerAsync(localHost, searchDevicesPort, listener);

    startBroadcastAsync(localHost, searchDevicesPort);
  }

  @Override
  public void stop() {
    log.info("Stopping UdpDevicesFinder ...");

    connectionsAliveWatcher.stopWatching();

    if(isListenerSocketOpened) {
      listenerSocket.close();
      listenerSocket = null;
      isListenerSocketOpened = false;
    }

    synchronized(this) {
      areBroadcastSocketsOpened = false;

      for (DatagramSocket clientSocket : openedBroadcastSockets) {
        clientSocket.close();
      }
    }
  }

  @Override
  public void disconnectedFromDevice(ConnectedDevice device) {
    removeDeviceFromFoundDevices(device);
  }


  protected void startListenerAsync(final HostInfo localHost, final int searchDevicesPort, final IDevicesFinderListener listener) {
    threadPool.runTaskAsync(new Runnable() {
      @Override
      public void run() {
        startListener(localHost, searchDevicesPort, listener);
      }
    });
  }

  protected void startListener(HostInfo localHost, int searchDevicesPort, IDevicesFinderListener listener) {
    try {
      this.listenerSocket = createListenerSocket(searchDevicesPort);

      byte[] buffer = new byte[1024];
      DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

      while(isListenerSocketOpened) {
        try {
          listenerSocket.receive(packet);
        } catch(Exception ex) {
          if(isSocketCloseException(ex) == true) // communication has been cancelled by close() method
            break;
          else {
            log.error("An Error occurred receiving Packets. listenerSocket = " + listenerSocket, ex);
            startListener(localHost, searchDevicesPort, listener);
          }
        }

        listenerReceivedPacket(buffer, packet, localHost, listener);
      }
    } catch(Exception ex) {
      log.error("An error occurred starting UdpDevicesSearcher", ex);
    }
  }

  protected DatagramSocket createListenerSocket(int searchDevicesPort) throws SocketException {
    DatagramSocket listenerSocket = new DatagramSocket(null); // so that other Applications on the same Host can also use this port, set bindAddress to null ..,
    listenerSocket.setReuseAddress(true); // and reuseAddress to true
    listenerSocket.bind(new InetSocketAddress(searchDevicesPort));

    listenerSocket.setBroadcast(true);
    isListenerSocketOpened = true;

    return listenerSocket;
  }

  protected boolean isSocketCloseException(Exception ex) {
    return NetworkHelper.isSocketCloseException(ex);
  }

  protected void listenerReceivedPacket(byte[] buffer, DatagramPacket packet, HostInfo localHost, IDevicesFinderListener listener) {
    if(messagesCreator.isSearchingForDevicesMessage(buffer, packet.getLength())) {
      HostInfo remoteHost = messagesCreator.getHostInfoFromMessage(buffer, packet);
      remoteHost.setAddress(packet.getAddress().getHostAddress());

      if(isSelfSentPacket(remoteHost, localHost) == false) {
        if(hasDeviceAlreadyBeenFound(remoteHost) == false) {
          deviceFound(remoteHost, listener);
        }
        else {
          connectionsAliveWatcher.receivedMessageFromDevice(remoteHost);
        }
      }
    }
  }

  protected boolean isSelfSentPacket(HostInfo remoteHost, HostInfo localHost) {
    return localHost.getUserUniqueId().equals(remoteHost.getUserUniqueId()) &&
        localHost.getDeviceId().equals(remoteHost.getDeviceId()) &&
        remoteHost.getAddress().equals(NetworkHelper.getIPAddressString(true));
  }

  protected boolean hasDeviceAlreadyBeenFound(HostInfo hostInfo) {
    for(HostInfo foundDevice : foundDevices) {
      if(hostInfo.getAddress().equals(foundDevice.getAddress()) &&
          hostInfo.getDeviceId().equals(foundDevice.getDeviceId()) &&
          hostInfo.getUserUniqueId().equals(foundDevice.getUserUniqueId())) {
        return true;
      }
    }

    return false;
  }

  protected void deviceFound(HostInfo device, IDevicesFinderListener listener) {
    foundDevices.add(device);

    if(foundDevices.size() == 1) {
      startConnectionsAliveWatcher(listener);
    }

    listener.deviceFound(device);
  }

  protected void startConnectionsAliveWatcher(final IDevicesFinderListener listener) {
    connectionsAliveWatcher.startWatchingAsync(foundDevices, new IConnectionsAliveWatcherListener() {
      @Override
      public void deviceDisconnected(HostInfo device) {
        UdpDevicesFinder.this.deviceDisconnected(device, listener);
      }
    });
  }

  protected void deviceDisconnected(HostInfo device, IDevicesFinderListener listener) {
    removeDeviceFromFoundDevices(device);

    if(listener != null) {
      listener.deviceDisconnected(device);
    }
  }


  protected void startBroadcastAsync(final HostInfo localHost, final int searchDevicesPort) {
    threadPool.runTaskAsync(new Runnable() {
      @Override
      public void run() {
        startBroadcast(localHost, searchDevicesPort);
      }
    });
  }

  protected void startBroadcast(HostInfo localHost, int searchDevicesPort) {
    for(InetAddress broadcastAddress : NetworkHelper.getBroadcastAddresses()) {
      startBroadcastForBroadcastAddressAsync(broadcastAddress, localHost, searchDevicesPort);
    }
  }

  protected void startBroadcastForBroadcastAddressAsync(final InetAddress broadcastAddress, final HostInfo localHost, final int searchDevicesPort) {
    threadPool.runTaskAsync(new Runnable() {
      @Override
      public void run() {
        startBroadcastForBroadcastAddress(broadcastAddress, localHost, searchDevicesPort);
      }
    });
  }

  protected void startBroadcastForBroadcastAddress(InetAddress broadcastAddress, HostInfo localHost, int searchDevicesPort) {
    try {
      DatagramSocket broadcastSocket = new DatagramSocket();

      synchronized(this) {
        openedBroadcastSockets.add(broadcastSocket);
        areBroadcastSocketsOpened = true;
      }

      broadcastSocket.setSoTimeout(10000);

      byte[] message = messagesCreator.createSearchingForDevicesMessage(localHost);
      DatagramPacket searchDevicesPacket = new DatagramPacket(message, message.length, broadcastAddress, searchDevicesPort);

      while (areBroadcastSocketsOpened) {
        broadcastSocket.send(searchDevicesPacket);

        try { Thread.sleep(Constants.SendWeAreAliveMessageInterval); } catch(Exception ignored) { }
      }
    } catch (Exception ex) {
      log.error("An error occurred trying to find Devices", ex);
    }
  }


  protected void removeDeviceFromFoundDevices(HostInfo device) {
    for(HostInfo foundDevice : foundDevices) {
      if(device.getDeviceId().equals(foundDevice.getDeviceId()) &&
          device.getUserUniqueId().equals(foundDevice.getUserUniqueId())) {
        foundDevices.remove(foundDevice);
        break;
      }
    }
  }

  protected void removeDeviceFromFoundDevices(ConnectedDevice device) {
    for(HostInfo foundDevice : foundDevices) {
      if(device.getDeviceId().equals(foundDevice.getDeviceId()) &&
          device.getAddress().equals(foundDevice.getAddress())) {
        foundDevices.remove(foundDevice);
        break;
      }
    }
  }

}

package net.deepthought.communication.connected_device;

import net.deepthought.communication.ConnectorMessagesCreator;
import net.deepthought.communication.Constants;
import net.deepthought.communication.IDevicesFinderListener;
import net.deepthought.communication.NetworkHelper;
import net.deepthought.communication.model.HostInfo;
import net.deepthought.data.model.Device;
import net.deepthought.data.model.User;
import net.deepthought.util.IThreadPool;

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
 * Created by ganymed on 22/08/15.
 */
public class UdpDevicesSearcher {

  private final static Logger log = LoggerFactory.getLogger(UdpDevicesSearcher.class);


  protected ConnectorMessagesCreator messagesCreator;

  protected IThreadPool threadPool;

  protected User loggedOnUser;

  protected Device localDevice;

  protected DatagramSocket listenerSocket = null;
  protected boolean isListenerSocketOpened = false;

  protected List<DatagramSocket> openedBroadcastSockets = new ArrayList<>();
  protected boolean areBroadcastSocketsOpened = false;

  protected List<HostInfo> foundDevices = new CopyOnWriteArrayList<>();


  public UdpDevicesSearcher(ConnectorMessagesCreator messagesCreator, IThreadPool threadPool, User loggedOnUser, Device localDevice) {
    this.messagesCreator = messagesCreator;
    this.threadPool = threadPool;
    this.loggedOnUser = loggedOnUser;
    this.localDevice = localDevice;
  }


  public void startSearchingAsync(final IDevicesFinderListener listener) {
    startListenerAsync(listener);

    startBroadcastAsync();
  }

  public void stopSearching() {
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


  protected void startListenerAsync(final IDevicesFinderListener listener) {
    threadPool.runTaskAsync(new Runnable() {
      @Override
      public void run() {
        startListener(listener);
      }
    });
  }

  protected void startListener(IDevicesFinderListener listener) {
    try {
      this.listenerSocket = createListenerSocket();

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
            startListener(listener);
          }
        }

        listenerReceivedPacket(buffer, packet, listener);
      }
    } catch(Exception ex) {
      log.error("An error occurred starting UdpDevicesSearcher", ex);
    }
  }

  protected DatagramSocket createListenerSocket() throws SocketException {
    DatagramSocket listenerSocket = new DatagramSocket(null); // so that other Applications on the same Host can also use this port, set bindAddress to null ..,
    listenerSocket.setReuseAddress(true); // and reuseAddress to true
    listenerSocket.bind(new InetSocketAddress(Constants.SearchDevicesListenerPort));

    listenerSocket.setBroadcast(true);
    isListenerSocketOpened = true;

    return listenerSocket;
  }

  protected boolean isSocketCloseException(Exception ex) {
    return NetworkHelper.isSocketCloseException(ex);
  }

  protected void listenerReceivedPacket(byte[] buffer, DatagramPacket packet, IDevicesFinderListener listener) {
    if(messagesCreator.isSearchingForDevicesMessage(buffer, packet.getLength())) {
      HostInfo hostInfo = messagesCreator.getHostInfoFromMessage(buffer, packet);
      hostInfo.setAddress(packet.getAddress().getHostAddress());

      if(isSelfSentPacket(hostInfo) == false && hasDeviceAlreadyBeenFound(hostInfo) == false) {
        deviceFound(hostInfo, listener);
      }
    }
  }

  protected boolean isSelfSentPacket(HostInfo hostInfo) {
    return loggedOnUser.getUniversallyUniqueId().equals(hostInfo.getUserUniqueId()) &&
        localDevice.getUniversallyUniqueId().equals(hostInfo.getDeviceId()) &&
        hostInfo.getAddress().equals(NetworkHelper.getIPAddressString(true));
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

    listener.deviceFound(device);
  }


  protected void startBroadcastAsync() {
    threadPool.runTaskAsync(new Runnable() {
      @Override
      public void run() {
        startBroadcast();
      }
    });
  }

  protected void startBroadcast() {
    for(InetAddress broadcastAddress : NetworkHelper.getBroadcastAddresses()) {
      startBroadcastForBroadcastAddressAsync(broadcastAddress);
    }
  }

  protected void startBroadcastForBroadcastAddressAsync(final InetAddress broadcastAddress) {
    threadPool.runTaskAsync(new Runnable() {
      @Override
      public void run() {
        startBroadcastForBroadcastAddress(broadcastAddress);
      }
    });
  }

  protected void startBroadcastForBroadcastAddress(InetAddress broadcastAddress) {
    try {
      DatagramSocket broadcastSocket = new DatagramSocket();

      synchronized(this) {
        openedBroadcastSockets.add(broadcastSocket);
        areBroadcastSocketsOpened = true;
      }

      broadcastSocket.setSoTimeout(10000);

      byte[] message = messagesCreator.createSearchingForDevicesMessage();
      DatagramPacket searchDevicesPacket = new DatagramPacket(message, message.length, broadcastAddress, Constants.SearchDevicesListenerPort);

      while (areBroadcastSocketsOpened) {
        broadcastSocket.send(searchDevicesPacket);

        try { Thread.sleep(1000); } catch(Exception ignored) { }
      }
    } catch (Exception ex) {
      log.error("An error occurred trying to find Devices", ex);
    }
  }

}

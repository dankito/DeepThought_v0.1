package net.deepthought.communication.connected_device;

import net.deepthought.Application;
import net.deepthought.communication.ConnectorMessagesCreator;
import net.deepthought.communication.Constants;
import net.deepthought.communication.IDevicesFinderListener;
import net.deepthought.communication.NetworkHelper;
import net.deepthought.communication.model.HostInfo;
import net.deepthought.communication.registration.IRegisteredDevicesManager;
import net.deepthought.data.model.Device;
import net.deepthought.data.model.User;
import net.deepthought.util.DeepThoughtError;
import net.deepthought.util.IThreadPool;
import net.deepthought.util.localization.Localization;

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

  protected IRegisteredDevicesManager registeredDevicesManager;

  protected IConnectedDevicesManager connectedDevicesManager;

  protected User loggedOnUser;

  protected Device localDevice;

  protected DatagramSocket listenerSocket = null;
  protected boolean isListenerSocketOpened = false;

  protected List<DatagramSocket> openedClientSockets = new ArrayList<>();
  protected boolean areClientSocketsOpened = false;

  protected List<HostInfo> foundDevices = new CopyOnWriteArrayList<>();


  public UdpDevicesSearcher(ConnectorMessagesCreator messagesCreator, IThreadPool threadPool, IRegisteredDevicesManager registeredDevicesManager,
                            IConnectedDevicesManager connectedDevicesManager, User loggedOnUser, Device localDevice) {
    this.messagesCreator = messagesCreator;
    this.threadPool = threadPool;
    this.registeredDevicesManager = registeredDevicesManager;
    this.connectedDevicesManager = connectedDevicesManager;
    this.loggedOnUser = loggedOnUser;
    this.localDevice = localDevice;
  }


  public void startSearchingAsync(final IDevicesFinderListener listener) {
    startListenerAsync(listener);

    startClientAsync();
  }

  public void stopSearching() {
    if(isListenerSocketOpened) {
      listenerSocket.close();
      listenerSocket = null;
      isListenerSocketOpened = false;
    }

    synchronized(this) {
      areClientSocketsOpened = false;

      for (DatagramSocket clientSocket : openedClientSockets) {
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

  protected void respondToSearchingForRegisteredDevicesMessage(DatagramPacket requestPacket) {
    InetAddress address = requestPacket.getAddress();

    try {
      DatagramSocket responseSocket = new DatagramSocket();
      byte[] message = messagesCreator.createRegisteredDeviceFoundMessage();

      responseSocket.send(new DatagramPacket(message, message.length, address, requestPacket.getPort()));
    } catch(Exception ex) {
      log.error("Could not send response to SearchingForRegisteredDevices message from " + address, ex);
      Application.notifyUser(new DeepThoughtError(Localization.getLocalizedString("could.not.send.message.to.address", address), ex));
    }
  }


  protected void startClientAsync() {
    threadPool.runTaskAsync(new Runnable() {
      @Override
      public void run() {
        startClient();
      }
    });
  }

  protected void startClient() {
    for(InetAddress broadcastAddress : NetworkHelper.getBroadcastAddresses()) {
      startClientForBroadcastAddressAsync(broadcastAddress);
    }
  }

  protected void startClientForBroadcastAddressAsync(final InetAddress broadcastAddress) {
    threadPool.runTaskAsync(new Runnable() {
      @Override
      public void run() {
        startClientForBroadcastAddress(broadcastAddress);
      }
    });
  }

  protected void startClientForBroadcastAddress(InetAddress broadcastAddress) {
    try {
      DatagramSocket clientSocket = new DatagramSocket();

      synchronized(this) {
        openedClientSockets.add(clientSocket);
        areClientSocketsOpened = true;
      }

      clientSocket.setSoTimeout(10000);

      byte[] message = messagesCreator.createSearchingForDevicesMessage();
      DatagramPacket searchDevicesPacket = new DatagramPacket(message, message.length, broadcastAddress, Constants.SearchDevicesListenerPort);

      while (areClientSocketsOpened) {
        clientSocket.send(searchDevicesPacket);

        try { Thread.sleep(1000); } catch(Exception ignored) { }
      }
    } catch (Exception ex) {
      log.error("An error occurred trying to find Devices", ex);
    }
  }

}

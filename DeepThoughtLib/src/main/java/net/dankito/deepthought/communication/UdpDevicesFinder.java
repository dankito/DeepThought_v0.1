package net.dankito.deepthought.communication;

import net.dankito.deepthought.communication.model.ConnectedDevice;
import net.dankito.deepthought.communication.model.HostInfo;
import net.dankito.deepthought.util.AsyncProducerConsumerQueue;
import net.dankito.deepthought.util.ConsumerListener;
import net.dankito.deepthought.util.IThreadPool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by ganymed on 05/06/16.
 */
public class UdpDevicesFinder implements IDevicesFinder {

  private final static Logger log = LoggerFactory.getLogger(UdpDevicesFinder.class);


  protected ConnectionsAliveWatcher connectionsAliveWatcher = null;

  protected IThreadPool threadPool;

  protected DatagramSocket listenerSocket = null;
  protected boolean isListenerSocketOpened = false;

  protected List<DatagramSocket> openedBroadcastSockets = new ArrayList<>();
  protected boolean areBroadcastSocketsOpened = false;

  protected AsyncProducerConsumerQueue<ReceivedUdpDevicesFinderPacket> receivedPacketsQueue;

  protected List<HostInfo> foundDevices = new CopyOnWriteArrayList<>();


  public UdpDevicesFinder(IThreadPool threadPool) {
    this.threadPool = threadPool;
    // * 3.5 = from 3 messages one must be received to be still valued as 'connected'
    this.connectionsAliveWatcher = new ConnectionsAliveWatcher((int)(Constants.SendWeAreAliveMessageInterval * 8.5));
  }


  @Override
  public boolean isRunning() {
    return isListenerSocketOpened && areBroadcastSocketsOpened;
  }

  @Override
  public void startAsync(HostInfo localHost, int searchDevicesPort, ConnectorMessagesCreator messagesCreator, IDevicesFinderListener listener) {
    log.info("Starting UdpDevicesFinder ...");

    receivedPacketsQueue = new AsyncProducerConsumerQueue(3, receivedPacketsHandler);

    startListenerAsync(localHost, searchDevicesPort, messagesCreator, listener);

    startBroadcastAsync(localHost, searchDevicesPort, messagesCreator);
  }

  @Override
  public void stop() {
    log.info("Stopping UdpDevicesFinder ...");

    receivedPacketsQueue.cleanUp();

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


  protected void startListenerAsync(final HostInfo localHost, final int searchDevicesPort, final ConnectorMessagesCreator messagesCreator, final IDevicesFinderListener listener) {
    threadPool.runTaskAsync(new Runnable() {
      @Override
      public void run() {
        startListener(localHost, searchDevicesPort, messagesCreator, listener);
      }
    });
  }

  protected void startListener(HostInfo localHost, int searchDevicesPort, ConnectorMessagesCreator messagesCreator, IDevicesFinderListener listener) {
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
            startListener(localHost, searchDevicesPort, messagesCreator, listener);
          }
        }

        listenerReceivedPacket(buffer, packet, localHost, messagesCreator, listener);
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


  protected void listenerReceivedPacket(byte[] buffer, DatagramPacket packet, HostInfo localHost, ConnectorMessagesCreator messagesCreator, IDevicesFinderListener listener) {
    receivedPacketsQueue.add(new ReceivedUdpDevicesFinderPacket(Arrays.copyOf(buffer, packet.getLength()), packet, packet.getAddress().getHostAddress(), messagesCreator, listener));
  }


  protected ConsumerListener<ReceivedUdpDevicesFinderPacket> receivedPacketsHandler = new ConsumerListener<ReceivedUdpDevicesFinderPacket>() {
    @Override
    public void consumeItem(ReceivedUdpDevicesFinderPacket receivedPacket) {
      handleReceivedPacket(receivedPacket.getReceivedData(), receivedPacket.getPacket(), receivedPacket.getSenderAddress(), receivedPacket.getMessagesCreator(), receivedPacket.getListener());
    }
  };

  protected void handleReceivedPacket(byte[] receivedData, DatagramPacket packet, String senderAddress, ConnectorMessagesCreator messagesCreator, IDevicesFinderListener listener) {
    if(messagesCreator.isSearchingForDevicesMessage(receivedData, receivedData.length)) {
      HostInfo remoteHost = messagesCreator.getHostInfoFromMessage(receivedData, senderAddress);
      remoteHost.setAddress(senderAddress);

      if(isSelfSentPacket(remoteHost, messagesCreator) == false) {
        if(hasDeviceAlreadyBeenFound(remoteHost) == false) {
          foundDevices.add(remoteHost);

          deviceFound(remoteHost, listener);
        }
        else {
          connectionsAliveWatcher.receivedMessageFromDevice(remoteHost);
        }
      }
    }
  }

  protected boolean isSelfSentPacket(HostInfo remoteHost, ConnectorMessagesCreator messagesCreator) {
    return messagesCreator.equalsLocalHostDevice(remoteHost);
  }

  protected boolean hasDeviceAlreadyBeenFound(HostInfo hostInfo) {
    List<HostInfo> foundDevicesCopy = new ArrayList<>(foundDevices);

    for(HostInfo foundDevice : foundDevicesCopy) {
      // i removed check for user unique id as after initial synchronization this one changes so we would ask user again if she/he likes to connect to this device?
      if(hostInfo.getAddress().equals(foundDevice.getAddress()) &&
          hostInfo.getDeviceUniqueId().equals(foundDevice.getDeviceUniqueId()) &&
          hostInfo.getMessagesPort() == foundDevice.getMessagesPort()) {
        return true;
      }
    }

    return false;
  }

  protected void deviceFound(HostInfo device, IDevicesFinderListener listener) {
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


  protected void startBroadcastAsync(final HostInfo localHost, final int searchDevicesPort, final ConnectorMessagesCreator messagesCreator) {
    threadPool.runTaskAsync(new Runnable() {
      @Override
      public void run() {
        startBroadcast(localHost, searchDevicesPort, messagesCreator);
      }
    });
  }

  protected void startBroadcast(HostInfo localHost, int searchDevicesPort, ConnectorMessagesCreator messagesCreator) {
    for(InetAddress broadcastAddress : NetworkHelper.getBroadcastAddresses()) {
      startBroadcastForBroadcastAddressAsync(broadcastAddress, localHost, searchDevicesPort, messagesCreator);
    }
  }

  protected void startBroadcastForBroadcastAddressAsync(final InetAddress broadcastAddress, final HostInfo localHost, final int searchDevicesPort, final ConnectorMessagesCreator messagesCreator) {
    threadPool.runTaskAsync(new Runnable() {
      @Override
      public void run() {
        startBroadcastForBroadcastAddress(broadcastAddress, localHost, searchDevicesPort, messagesCreator);
      }
    });
  }

  protected void startBroadcastForBroadcastAddress(InetAddress broadcastAddress, HostInfo localHost, int searchDevicesPort, ConnectorMessagesCreator messagesCreator) {
    try {
      DatagramSocket broadcastSocket = new DatagramSocket();

      synchronized(this) {
        openedBroadcastSockets.add(broadcastSocket);
        areBroadcastSocketsOpened = true;
      }

      broadcastSocket.setSoTimeout(10000);

      while (areBroadcastSocketsOpened) {
        try {
          sendBroadcastOnSocket(broadcastSocket, broadcastAddress, searchDevicesPort, messagesCreator);
        } catch(Exception e) {
          log.error("Could not send Broadcast to Address " + broadcastAddress, e);
          startBroadcastForBroadcastAddress(broadcastAddress, localHost, searchDevicesPort, messagesCreator);
        }
      }
    } catch (Exception ex) {
      log.error("An error occurred trying to find Devices", ex);
    }
  }

  protected void sendBroadcastOnSocket(DatagramSocket broadcastSocket, InetAddress broadcastAddress, int searchDevicesPort, ConnectorMessagesCreator messagesCreator) throws IOException {
    DatagramPacket searchDevicesPacket = messagesCreator.getSearchDevicesDatagramPacket(broadcastAddress, searchDevicesPort);
    broadcastSocket.send(searchDevicesPacket);

    try { Thread.sleep(Constants.SendWeAreAliveMessageInterval); } catch(Exception ignored) { }
  }


  protected void removeDeviceFromFoundDevices(HostInfo device) {
    // TODO: is IP Address really set in all possible cases?
    for(HostInfo foundDevice : foundDevices) {
      if(device.getDeviceUniqueId().equals(foundDevice.getDeviceUniqueId()) &&
          foundDevice.getAddress().equals(device.getAddress()) &&
          device.getMessagesPort() == foundDevice.getMessagesPort()) {
        foundDevices.remove(foundDevice);
      }
    }
  }

}

package net.deepthought.communication.connected_device;

import net.deepthought.Application;
import net.deepthought.communication.ConnectorMessagesCreator;
import net.deepthought.communication.Constants;
import net.deepthought.communication.NetworkHelper;
import net.deepthought.communication.model.ConnectedDevice;
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

/**
 * Created by ganymed on 22/08/15.
 */
public class RegisteredDevicesSearcher {

  private final static Logger log = LoggerFactory.getLogger(RegisteredDevicesSearcher.class);


  protected ConnectorMessagesCreator messagesCreator;

  protected IThreadPool threadPool;

  protected IRegisteredDevicesManager registeredDevicesManager;

  protected IConnectedDevicesManager connectedDevicesManager;

  protected User loggedOnUser;

  protected Device localDevice;

  protected DatagramSocket serverSocket = null;
  protected boolean isServerSocketOpened = false;

  protected List<DatagramSocket> openedClientSockets = new ArrayList<>();
  protected boolean areClientSocketsOpened = false;


  public RegisteredDevicesSearcher(ConnectorMessagesCreator messagesCreator, IThreadPool threadPool, IRegisteredDevicesManager registeredDevicesManager,
                                   IConnectedDevicesManager connectedDevicesManager, User loggedOnUser, Device localDevice) {
    this.messagesCreator = messagesCreator;
    this.threadPool = threadPool;
    this.registeredDevicesManager = registeredDevicesManager;
    this.connectedDevicesManager = connectedDevicesManager;
    this.loggedOnUser = loggedOnUser;
    this.localDevice = localDevice;
  }


  public void startSearchingAsync(final IConnectedDevicesListener listener) {
    startServerAsync();

    startClientAsync(listener);
  }

  public void stopSearching() {
    if(isServerSocketOpened) {
      serverSocket.close();
      serverSocket = null;
      isServerSocketOpened = false;
    }

    synchronized(this) {
      areClientSocketsOpened = false;

      for (DatagramSocket clientSocket : openedClientSockets) {
        clientSocket.close();
      }
    }
  }


  protected void startServerAsync() {
    threadPool.runTaskAsync(new Runnable() {
      @Override
      public void run() {
        startServer();
      }
    });
  }

  protected void startServer() {
    try {
      this.serverSocket = createServerSocket();

      byte[] buffer = new byte[1024];
      DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

      while(isServerSocketOpened) {
        try {
          serverSocket.receive(packet);
        } catch(Exception ex) {
          if(isSocketCloseException(ex) == true) // communication has been cancelled by close() method
            break;
          else {
            log.error("An Error occurred receiving Packets. serverSocket = " + serverSocket, ex);
            startServer();
          }
        }

        serverReceivedPacket(buffer, packet);
      }
    } catch(Exception ex) {
      log.error("An error occurred starting RegisteredDevicesSearcher", ex);
    }
  }

  protected DatagramSocket createServerSocket() throws SocketException {
    DatagramSocket serverSocket = new DatagramSocket(null); // so that other Applications on the same Host can also use this port, set bindAddress to null ..,
    serverSocket.setReuseAddress(true); // and reuseAddress to true
    serverSocket.bind(new InetSocketAddress(Constants.RegisteredDevicesListenerPort));

    serverSocket.setBroadcast(true);
    isServerSocketOpened = true;

    return serverSocket;
  }

  protected boolean isSocketCloseException(Exception ex) {
    return NetworkHelper.isSocketCloseException(ex);
  }

  protected void serverReceivedPacket(byte[] buffer, DatagramPacket packet) {
    if(messagesCreator.isSearchingForRegisteredDevicesMessage(buffer, packet.getLength())) {
      HostInfo clientInfo = messagesCreator.getHostInfoFromMessage(buffer, packet);
      if(isSelfSentPacket(clientInfo)) // TODO: adjust isSelfSentPacket() as clientInfo's IP Address will now be set from received Packet
        return;

      clientInfo.setIpAddress(packet.getAddress().getHostAddress());

      if(registeredDevicesManager.isDeviceRegistered(clientInfo) == true &&
          connectedDevicesManager.isConnectedToDevice(clientInfo) == false) {
        respondToSearchingForRegisteredDevicesMessage(packet);
      }
    }
  }

  protected boolean isSelfSentPacket(HostInfo clientInfo) {
    return loggedOnUser.getUniversallyUniqueId().equals(clientInfo.getUserUniqueId()) &&
        localDevice.getUniversallyUniqueId().equals(clientInfo.getDeviceUniqueId()) &&
        clientInfo.getIpAddress().equals(NetworkHelper.getIPAddressString(true));
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


  protected void startClientAsync(final IConnectedDevicesListener listener) {
    threadPool.runTaskAsync(new Runnable() {
      @Override
      public void run() {
        startClient(listener);
      }
    });
  }

  protected void startClient(IConnectedDevicesListener listener) {
    for(InetAddress broadcastAddress : NetworkHelper.getBroadcastAddresses()) {
      startClientForBroadcastAddressAsync(broadcastAddress, listener);
    }
  }

  protected void startClientForBroadcastAddressAsync(final InetAddress broadcastAddress, final IConnectedDevicesListener listener) {
    threadPool.runTaskAsync(new Runnable() {
      @Override
      public void run() {
        startClientForBroadcastAddress(broadcastAddress, listener);
      }
    });
  }

  protected void startClientForBroadcastAddress(InetAddress broadcastAddress, IConnectedDevicesListener listener) {
    try {
      DatagramSocket clientSocket = new DatagramSocket();

      synchronized(this) {
        openedClientSockets.add(clientSocket);
        areClientSocketsOpened = true;
      }

      clientSocket.setSoTimeout(10000);

      byte[] message = messagesCreator.createSearchingForRegisteredDevicesMessage();
      DatagramPacket searchRegisteredDevicesPacket = new DatagramPacket(message, message.length, broadcastAddress, Constants.RegisteredDevicesListenerPort);

      while (areClientSocketsOpened) {
        clientSocket.send(searchRegisteredDevicesPacket);

        waitForResponsePackets(clientSocket, listener);
      }
    } catch (Exception ex) {
      log.error("An error occurred trying to find RegistrationServers", ex);
    }
  }

  protected void waitForResponsePackets(DatagramSocket clientSocket, IConnectedDevicesListener listener) {
    byte[] buffer = new byte[1024];
    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

    try {
      clientSocket.receive(packet);

      while(messagesCreator.isRegisteredDeviceFoundMessage(buffer, packet.getLength()) == false) {
        clientSocket.receive(packet);
      }

      clientReceivedResponseFromServer(listener, buffer, packet);
    } catch(Exception ex) { }
  }

  protected void clientReceivedResponseFromServer(IConnectedDevicesListener listener, byte[] buffer, DatagramPacket packet) {
    ConnectedDevice serverInfo = messagesCreator.getConnectedDeviceFromMessage(buffer, packet.getLength(), packet.getAddress());
    serverInfo.setAddress(packet.getAddress().getHostAddress());

    if(registeredDevicesManager.isDeviceRegistered(serverInfo) == true) {
      registeredDeviceConnected(listener, packet, serverInfo);
    }
  }

  protected void registeredDeviceConnected(IConnectedDevicesListener listener, DatagramPacket packet, ConnectedDevice serverInfo) {
    if (listener != null)
      listener.registeredDeviceConnected(serverInfo);
  }
}

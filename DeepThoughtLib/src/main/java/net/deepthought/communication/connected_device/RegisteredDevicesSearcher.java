package net.deepthought.communication.connected_device;

import net.deepthought.Application;
import net.deepthought.communication.ConnectorMessagesCreator;
import net.deepthought.communication.Constants;
import net.deepthought.communication.NetworkHelper;
import net.deepthought.communication.listener.RegisteredDeviceConnectedListener;
import net.deepthought.communication.model.ConnectedDevice;
import net.deepthought.communication.model.HostInfo;
import net.deepthought.util.DeepThoughtError;
import net.deepthought.util.Localization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;

/**
 * Created by ganymed on 22/08/15.
 */
public class RegisteredDevicesSearcher {

  private final static Logger log = LoggerFactory.getLogger(RegisteredDevicesSearcher.class);


  protected ConnectorMessagesCreator messagesCreator;

  protected DatagramSocket serverSocket = null;
  protected boolean isServerSocketOpened = false;

  protected DatagramSocket clientSocket = null;
  protected boolean isClientSocketOpened = false;


  public RegisteredDevicesSearcher(ConnectorMessagesCreator messagesCreator) {
    this.messagesCreator = messagesCreator;
  }


  public void startSearchingAsync(final RegisteredDeviceConnectedListener listener) {
    startServerAsync();

    startClientAsync(listener);
  }

  public void stopSearching() {
    if(isServerSocketOpened) {
      serverSocket.close();
      serverSocket = null;
      isServerSocketOpened = false;
    }

    if(isClientSocketOpened) {
      clientSocket.close();
      clientSocket = null;
      isClientSocketOpened = false;
    }
  }


  protected void startServerAsync() {
    Application.getThreadPool().runTaskAsync(new Runnable() {
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
      HostInfo clientInfo = messagesCreator.getHostInfoFromMessage(buffer, packet.getLength());
      if(isSelfSentPacket(clientInfo))
        return;

      if(Application.getDeepThoughtsConnector().getRegisteredDevicesManager().isDeviceRegistered(clientInfo) == true &&
          Application.getDeepThoughtsConnector().getConnectedDevicesManager().isConnectedToDevice(clientInfo) == false) {
        respondToSearchingForRegisteredDevicesMessage(packet);
      }
    }
  }

  protected boolean isSelfSentPacket(HostInfo clientInfo) {
    return Application.getLoggedOnUser().getUniversallyUniqueId().equals(clientInfo.getUserUniqueId()) &&
        Application.getApplication().getLocalDevice().getUniversallyUniqueId().equals(clientInfo.getDeviceUniqueId()) &&
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


  protected void startClientAsync(final RegisteredDeviceConnectedListener listener) {
    Application.getThreadPool().runTaskAsync(new Runnable() {
      @Override
      public void run() {
        startClient(listener);
      }
    });
  }

  protected void startClient(RegisteredDeviceConnectedListener listener) {
    try {
      clientSocket = new DatagramSocket();
      isClientSocketOpened = true;
      clientSocket.setSoTimeout(10000);

      byte[] message = messagesCreator.createSearchingForRegisteredDevicesMessage();
      DatagramPacket searchRegisteredDevicesPacket = new DatagramPacket(message, message.length, NetworkHelper.getBroadcastAddress(), Constants.RegisteredDevicesListenerPort);

      while (isClientSocketOpened) {
        clientSocket.send(searchRegisteredDevicesPacket);

        waitForResponsePackets(listener);
      }
    } catch (Exception ex) {
      log.error("An error occurred trying to find RegistrationServers", ex);
    }
  }

  protected void waitForResponsePackets(RegisteredDeviceConnectedListener listener) {
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

  protected void clientReceivedResponseFromServer(RegisteredDeviceConnectedListener listener, byte[] buffer, DatagramPacket packet) {
    ConnectedDevice serverInfo = messagesCreator.getConnectedDeviceFromMessage(buffer, packet.getLength());

    if(Application.getDeepThoughtsConnector().getRegisteredDevicesManager().isDeviceRegistered(serverInfo) == true) {
      registeredDeviceConnected(listener, packet, serverInfo);
    }
  }

  protected void registeredDeviceConnected(RegisteredDeviceConnectedListener listener, DatagramPacket packet, ConnectedDevice serverInfo) {
    if (listener != null)
      listener.registeredDeviceConnected(serverInfo);
  }
}

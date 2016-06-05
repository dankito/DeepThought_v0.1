package net.deepthought.communication.registration;

import net.deepthought.communication.ConnectorMessagesCreator;
import net.deepthought.communication.Constants;
import net.deepthought.communication.NetworkHelper;
import net.deepthought.communication.model.HostInfo;
import net.deepthought.data.model.Device;
import net.deepthought.util.IThreadPool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by ganymed on 19/08/15.
 */
public class LookingForRegistrationServersClient {

  private final static Logger log = LoggerFactory.getLogger(LookingForRegistrationServersClient.class);


  protected ConnectorMessagesCreator messagesCreator;

  protected IRegisteredDevicesManager registeredDevicesManager;

  protected IThreadPool threadPool;

  protected List<DatagramSocket> openedSockets = new ArrayList<>();
  protected boolean isSocketOpened = false;


  public LookingForRegistrationServersClient(ConnectorMessagesCreator messagesCreator, IRegisteredDevicesManager registeredDevicesManager, IThreadPool threadPool) {
    this.messagesCreator = messagesCreator;
    this.registeredDevicesManager = registeredDevicesManager;
    this.threadPool = threadPool;
  }


  public void findRegistrationServersAsync(final IUnregisteredDevicesListener listener) {
    threadPool.runTaskAsync(new Runnable() {
      @Override
      public void run() {
        findRegistrationServers(listener);
      }
    });
  }

  protected void findRegistrationServers(IUnregisteredDevicesListener listener) {
    for(InetAddress broadcastAddress : NetworkHelper.getBroadcastAddresses()) {
      findRegistrationServersForBroadcastAddressAsync(broadcastAddress, listener);
    }
  }

  protected void findRegistrationServersForBroadcastAddressAsync(final InetAddress broadcastAddress, final IUnregisteredDevicesListener listener) {
    threadPool.runTaskAsync(new Runnable() {
      @Override
      public void run() {
        findRegistrationServersForBroadcastAddress(broadcastAddress, listener);
      }
    });
  }

  protected void findRegistrationServersForBroadcastAddress(InetAddress broadcastAddress, IUnregisteredDevicesListener listener) {
    try {
      DatagramSocket socket = new DatagramSocket();

      synchronized(this) {
        openedSockets.add(socket);
        isSocketOpened = true;
      }

      socket.setSoTimeout(2000);

      byte[] message = messagesCreator.createLookingForRegistrationServerMessage();
      DatagramPacket findRegistrationServersPacket = new DatagramPacket(message, message.length, broadcastAddress, Constants.RegistrationServerPort);

      Map<InetAddress, Map<String, Set<String>>> receivedResponses = new HashMap<>();

      while(isSocketOpened) {
        socket.send(findRegistrationServersPacket);

        waitForResponsePackets(socket, listener, receivedResponses);
      }
    } catch(Exception ex) {
      log.error("An error occurred trying to find RegistrationServers", ex);
    }
  }

  protected void waitForResponsePackets(DatagramSocket socket, IUnregisteredDevicesListener listener, Map<InetAddress, Map<String, Set<String>>> receivedResponses) {
    byte[] buffer = new byte[1024];
    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

    try {
      HostInfo serverInfo = null;

      do {
        serverInfo = null; // may reset previously received one
        socket.receive(packet);
        serverInfo = messagesCreator.getHostInfoFromMessage(buffer, packet);
      } while(isSelfSentMessage(packet, serverInfo) ||
          hasResponseOfThisServerAlreadyBeenHandled(packet, serverInfo, receivedResponses)); // request of this client already  received and handled

      if(serverInfo != null && registeredDevicesManager.isDeviceRegistered(serverInfo) == false) {
        receivedPacketFromUnregisteredDevice(listener, receivedResponses, packet, serverInfo);
      }
    } catch(Exception ex) {
      if(ex instanceof SocketTimeoutException == false) {
        log.error("An error occurred trying to receive response from RegistrationServer", ex);
      }
    } // a receive time out (may notify user about that)
  }

  protected void receivedPacketFromUnregisteredDevice(IUnregisteredDevicesListener listener, Map<InetAddress, Map<String, Set<String>>> receivedResponses, DatagramPacket packet, HostInfo serverInfo) {
    boolean isOpenRegistrationServer = messagesCreator.isOpenRegistrationServerInfoMessage(packet.getData(), packet.getLength());

    if (isOpenRegistrationServer == true && listener != null) {
      listener.unregisteredDeviceFound(serverInfo);
    }

    InetAddress address = packet.getAddress();
    if(receivedResponses.containsKey(address) == false)
      receivedResponses.put(address, new HashMap<String, Set<String>>());
    if(receivedResponses.get(address).containsKey(serverInfo.getDeviceId()) == false)
      receivedResponses.get(address).put(serverInfo.getDeviceId(), new HashSet<String>());
    receivedResponses.get(address).get(serverInfo.getDeviceId()).add(serverInfo.getUserUniqueId());
  }

  protected boolean isSelfSentMessage(DatagramPacket packet, HostInfo serverInfo) {
    Device localDevice = this.messagesCreator.getConfig().getLocalDevice();
    return localDevice.getUniversallyUniqueId().equals(serverInfo.getDeviceId());
  }

  protected boolean hasResponseOfThisServerAlreadyBeenHandled(DatagramPacket packet, HostInfo serverInfo, Map<InetAddress, Map<String, Set<String>>> receivedResponses) {
    return receivedResponses.containsKey(packet.getAddress()) && receivedResponses.get(packet.getAddress()).containsKey((serverInfo.getDeviceId())) &&
        receivedResponses.get(packet.getAddress()).get(serverInfo.getDeviceId()).contains(serverInfo.getUserUniqueId());
  }

  public void stopSearchingForRegistrationServers() {
    synchronized(this) {
      isSocketOpened = false;

      for(DatagramSocket socket : openedSockets) {
        socket.close();
      }
    }
  }
}

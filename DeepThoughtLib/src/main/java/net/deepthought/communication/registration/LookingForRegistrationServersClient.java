package net.deepthought.communication.registration;

import net.deepthought.communication.ConnectorMessagesCreator;
import net.deepthought.communication.Constants;
import net.deepthought.communication.NetworkHelper;
import net.deepthought.communication.model.HostInfo;
import net.deepthought.util.IThreadPool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.HashSet;
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

  protected DatagramSocket socket = null;
  protected boolean isSocketOpened = false;


  public LookingForRegistrationServersClient(ConnectorMessagesCreator messagesCreator, IRegisteredDevicesManager registeredDevicesManager, IThreadPool threadPool) {
    this.messagesCreator = messagesCreator;
    this.registeredDevicesManager = registeredDevicesManager;
    this.threadPool = threadPool;
  }


  public void findRegistrationServersAsync(final RegistrationRequestListener listener) {
    threadPool.runTaskAsync(new Runnable() {
      @Override
      public void run() {
        findRegistrationServers(listener);
      }
    });
  }

  protected void findRegistrationServers(RegistrationRequestListener listener) {
    try {
      socket = new DatagramSocket();
      isSocketOpened = true;
      socket.setSoTimeout(2000);

      byte[] message = messagesCreator.createLookingForRegistrationServerMessage();
      DatagramPacket findRegistrationServersPacket = new DatagramPacket(message, message.length, NetworkHelper.getBroadcastAddress(), Constants.RegistrationServerPort);

      Map<InetAddress, Map<String, Set<String>>> receivedResponses = new HashMap<>();

      while(isSocketOpened) {
        socket.send(findRegistrationServersPacket);

        waitForResponsePackets(listener, receivedResponses);
      }
    } catch(Exception ex) {
      log.error("An error occurred trying to find RegistrationServers", ex);
    }
  }

  protected void waitForResponsePackets(RegistrationRequestListener listener, Map<InetAddress, Map<String, Set<String>>> receivedResponses) {
    byte[] buffer = new byte[1024];
    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

    try {
      socket.receive(packet);
      HostInfo serverInfo = messagesCreator.getHostInfoFromMessage(buffer, packet.getLength());

      while(hasResponseOfThisServerAlreadyBeenHandled(packet, serverInfo, receivedResponses)) { // request of this client already received and handled
        socket.receive(packet);
        serverInfo = messagesCreator.getHostInfoFromMessage(buffer, packet.getLength());
      }

      if(registeredDevicesManager.isDeviceRegistered(serverInfo) == false) {
        receivedPacketFromUnregisteredDevice(listener, receivedResponses, packet, serverInfo);
      }
    } catch(Exception ex) {
      log.error("An error occurred trying to receive response from RegistrationServer", ex);
    } // a receive time out (may notify user about that
  }

  protected void receivedPacketFromUnregisteredDevice(RegistrationRequestListener listener, Map<InetAddress, Map<String, Set<String>>> receivedResponses, DatagramPacket packet, HostInfo serverInfo) {
    boolean isOpenRegistrationServer = messagesCreator.isOpenRegistrationServerInfoMessage(packet.getData(), packet.getLength());

    if (isOpenRegistrationServer == true && listener != null) {
      listener.openRegistrationServerFound(serverInfo);
    }

    InetAddress address = packet.getAddress();
    if(receivedResponses.containsKey(address) == false)
      receivedResponses.put(address, new HashMap<String, Set<String>>());
    if(receivedResponses.get(address).containsKey(serverInfo.getDeviceUniqueId()) == false)
      receivedResponses.get(address).put(serverInfo.getDeviceUniqueId(), new HashSet<String>());
    receivedResponses.get(address).get(serverInfo.getDeviceUniqueId()).add(serverInfo.getUserUniqueId());
  }

  protected boolean hasResponseOfThisServerAlreadyBeenHandled(DatagramPacket packet, HostInfo serverInfo, Map<InetAddress, Map<String, Set<String>>> receivedResponses) {
    return receivedResponses.containsKey(packet.getAddress()) && receivedResponses.get(packet.getAddress()).containsKey((serverInfo.getDeviceUniqueId())) &&
        receivedResponses.get(packet.getAddress()).get(serverInfo.getDeviceUniqueId()).contains(serverInfo.getUserUniqueId());
  }

  public void stopSearchingForRegistrationServers() {
    synchronized(this) {
      if(isSocketOpened) {
        isSocketOpened = false;
        socket.close();
        socket = null;
      }
    }
  }
}

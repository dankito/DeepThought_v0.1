package net.deepthought.communication.registration;

import net.deepthought.Application;
import net.deepthought.communication.Constants;
import net.deepthought.communication.ConnectorMessagesCreator;
import net.deepthought.communication.NetworkHelper;
import net.deepthought.communication.model.HostInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * Created by ganymed on 19/08/15.
 */
public class LookingForRegistrationServersClient {

  private final static Logger log = LoggerFactory.getLogger(LookingForRegistrationServersClient.class);


  protected ConnectorMessagesCreator messagesCreator;

  protected DatagramSocket socket = null;
  protected boolean isSocketOpened = false;


  public LookingForRegistrationServersClient(ConnectorMessagesCreator messagesCreator) {
    this.messagesCreator = messagesCreator;
  }


  public void findRegistrationServersAsync(final RegistrationRequestListener listener) {
    new Thread(new Runnable() {
      @Override
      public void run() {
        findRegistrationServers(listener);
      }
    }).start();
  }

  protected void findRegistrationServers(RegistrationRequestListener listener) {
    try {
      socket = new DatagramSocket();
      isSocketOpened = true;
      socket.setSoTimeout(2000);

      byte[] message = messagesCreator.createLookingForRegistrationServerMessage();
      DatagramPacket findRegistrationServersPacket = new DatagramPacket(message, message.length, NetworkHelper.getBroadcastAddress(), Constants.RegistrationServerPort);

      while(isSocketOpened) {
        socket.send(findRegistrationServersPacket);

        waitForResponsePackets(listener);
      }
    } catch(Exception ex) {
      log.error("An error occurred trying to find RegistrationServers", ex);
    }
  }

  protected void waitForResponsePackets(RegistrationRequestListener listener) {
    byte[] buffer = new byte[1024];
    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

    try {
      socket.receive(packet);
      HostInfo serverInfo = messagesCreator.getHostInfoFromMessage(buffer, packet.getLength());
      if(Application.getDeepThoughtsConnector().getRegisteredPeersManager().isDeviceRegistered(serverInfo) == false) {
        boolean isOpenRegistrationServer = messagesCreator.isOpenRegistrationServerInfoMessage(buffer, packet.getLength());

        if (isOpenRegistrationServer == true && listener != null) {
          listener.openRegistrationServerFound(serverInfo);
        }
      }
    } catch(Exception ex) { } // a receive time out (may notify user about that
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

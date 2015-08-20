package net.deepthought.communication.registration;

import net.deepthought.Application;
import net.deepthought.communication.ConnectorMessagesCreator;
import net.deepthought.communication.Constants;
import net.deepthought.util.DeepThoughtError;
import net.deepthought.util.Localization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ganymed on 19/08/15.
 */
public class RegistrationServer {

  private final static Logger log = LoggerFactory.getLogger(RegistrationServer.class);


  protected ConnectorMessagesCreator messagesCreator = null;

  protected DatagramSocket serverSocket = null;
  protected boolean isSocketOpened = false;


  public RegistrationServer(ConnectorMessagesCreator messagesCreator) {
    this.messagesCreator = messagesCreator;
  }


  public void startRegistrationServerAsync() {
    startRegistrationServerAsync(null);
  }

  public void startRegistrationServerAsync(final RegistrationServerListener listener) {
    new Thread(new Runnable() {
      @Override
      public void run() {
        startRegistrationServer(listener);
      }
    }).start();
  }

  protected void startRegistrationServer(RegistrationServerListener listener) {
    try {
      serverSocket = new DatagramSocket(Constants.RegistrationServerPort);
      isSocketOpened = true;
      serverSocket.setBroadcast(true);

      byte[] buffer = new byte[1024];
      DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
      Map<InetAddress, List<Integer>> receivedRequests = new HashMap<>();

      while(isSocketOpened) {
        try {
          serverSocket.receive(packet);
        } catch(Exception ex) {
          if(isSocketCloseException(ex) == true)
            break; // TODO: check if communication has been cancelled by close() method; otherwise log error and restart server
          else {
            log.error("An Error occurred receiving Packets. serverSocket = " + serverSocket, ex);
            startRegistrationServer(listener);
          }
        }

        requestReceived(listener, buffer, packet, receivedRequests);

      }
    } catch(Exception ex) {
      log.error("An error occurred starting RegistrationServer", ex);
    }
  }

  public void closeRegistrationServer() {
    synchronized(this) {
      if(isSocketOpened) {
        isSocketOpened = false;
        serverSocket.close();
        serverSocket = null;
      }
    }
  }


  protected void requestReceived(RegistrationServerListener listener, byte[] buffer, DatagramPacket packet, Map<InetAddress, List<Integer>> receivedRequests) {
    if(hasRequestOfThisClientAlreadyBeenHandled(packet, receivedRequests)) // request of this client already received and handled
      return;

    if (messagesCreator.isLookingForRegistrationServerMessage(buffer, packet.getLength())) {
      if(listener != null)
        listener.registrationRequestReceived(messagesCreator.getHostInfoFromMessage(buffer, packet.getLength()));

      respondToRegistrationRequest(packet, receivedRequests);
    }
  }

  protected boolean hasRequestOfThisClientAlreadyBeenHandled(DatagramPacket packet, Map<InetAddress, List<Integer>> receivedRequests) {
    return receivedRequests.containsKey(packet.getAddress()) && receivedRequests.get(packet.getAddress()).contains((Integer) packet.getPort());
  }

  protected void respondToRegistrationRequest(DatagramPacket requestPacket, Map<InetAddress, List<Integer>> receivedRequests) {
    InetAddress address = requestPacket.getAddress();

    try {
      DatagramSocket responseSocket = new DatagramSocket();
      byte[] message = messagesCreator.createOpenRegistrationServerInfoMessage();

      responseSocket.send(new DatagramPacket(message, message.length, address, requestPacket.getPort()));

      if(receivedRequests.containsKey(address) == false)
        receivedRequests.put(address, new ArrayList<Integer>());
      receivedRequests.get(address).add(requestPacket.getPort());
    } catch(Exception ex) {
      log.error("Could not send response to Registration request from " + address, ex);
      Application.notifyUser(new DeepThoughtError(Localization.getLocalizedString("could.not.send.message.to.address", address), ex));
    }
  }

  protected boolean isSocketCloseException(Exception exception) {
    return exception instanceof SocketException && "Socket closed".equals(exception.getMessage());
  }
}

package net.deepthought.communication.registration;

import net.deepthought.Application;
import net.deepthought.communication.ConnectorMessagesCreator;
import net.deepthought.communication.Constants;
import net.deepthought.communication.NetworkHelper;
import net.deepthought.util.DeepThoughtError;
import net.deepthought.util.IThreadPool;
import net.deepthought.util.Localization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * Created by ganymed on 19/08/15.
 */
public class RegistrationServer {

  private final static Logger log = LoggerFactory.getLogger(RegistrationServer.class);


  protected ConnectorMessagesCreator messagesCreator = null;

  protected IThreadPool threadPool;

  protected DatagramSocket serverSocket = null;
  protected boolean isSocketOpened = false;


  public RegistrationServer(ConnectorMessagesCreator messagesCreator, IThreadPool threadPool) {
    this.messagesCreator = messagesCreator;
    this.threadPool = threadPool;
  }


  public void startRegistrationServerAsync() {
    threadPool.runTaskAsync(new Runnable() {
      @Override
      public void run() {
        startRegistrationServer();
      }
    });
  }

  protected void startRegistrationServer() {
    try {
      serverSocket = new DatagramSocket(null);
      serverSocket.setReuseAddress(true); // setReuseAddress() has to be called before bind() (therefore we may not pass port to DatagramSocket constructor)
      serverSocket.bind(new InetSocketAddress(Constants.RegistrationServerPort));

      serverSocket.setBroadcast(true);
      isSocketOpened = true;

      byte[] buffer = new byte[1024];
      DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

      while(isSocketOpened) {
        try {
          serverSocket.receive(packet);
        } catch(Exception ex) {
          if(isSocketCloseException(ex) == true) // communication has been cancelled by close() method
            break;
          else {
            log.error("An Error occurred receiving Packets. serverSocket = " + serverSocket, ex);
            startRegistrationServer();
          }
        }

        requestReceived(buffer, packet);

      }
    } catch(Exception ex) {
      log.error("An error occurred starting RegistrationServer", ex);
    }
  }

  protected boolean isSocketCloseException(Exception ex) {
    return NetworkHelper.isSocketCloseException(ex);
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


  protected void requestReceived(byte[] buffer, DatagramPacket packet) {
    if (messagesCreator.isLookingForRegistrationServerMessage(buffer, packet.getLength())) {
      respondToRegistrationRequest(packet);
    }
  }

  protected void respondToRegistrationRequest(DatagramPacket requestPacket) {
    InetAddress address = requestPacket.getAddress();

    try {
      DatagramSocket responseSocket = new DatagramSocket();
      byte[] message = messagesCreator.createOpenRegistrationServerInfoMessage();

      responseSocket.send(new DatagramPacket(message, message.length, address, requestPacket.getPort()));
    } catch(Exception ex) {
      log.error("Could not send response to Registration request from " + address, ex);
      Application.notifyUser(new DeepThoughtError(Localization.getLocalizedString("could.not.send.message.to.address", address), ex));
    }
  }
}

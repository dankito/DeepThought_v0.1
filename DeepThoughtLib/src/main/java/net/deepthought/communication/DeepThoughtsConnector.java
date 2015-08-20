package net.deepthought.communication;

import net.deepthought.Application;
import net.deepthought.communication.model.HostInfo;
import net.deepthought.util.DeepThoughtError;
import net.deepthought.util.Localization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.BindException;

/**
 * Created by ganymed on 19/08/15.
 */
public class DeepThoughtsConnector implements IDeepThoughtsConnector {

  private final static Logger log = LoggerFactory.getLogger(DeepThoughtsConnector.class);


  protected int messageReceiverPort;

  protected Communicator communicator;

  protected MessagesReceiver messagesReceiver;

  protected DeepThoughtsConnectorListener listener;


  public DeepThoughtsConnector(DeepThoughtsConnectorListener listener) {
    this(Constants.MessageHandlerDefaultPort, listener);
  }

  public DeepThoughtsConnector(int messageReceiverPort, DeepThoughtsConnectorListener listener) {
    this(messageReceiverPort, new Communicator(), listener);
  }

  public DeepThoughtsConnector(int messageReceiverPort, Communicator communicator, DeepThoughtsConnectorListener listener) {
    this.messageReceiverPort = messageReceiverPort;
    this.communicator = communicator;
    this.listener = listener;
  }


  @Override
  public void runAsync() {
    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          DeepThoughtsConnector.this.run();
        } catch(Exception ex) { log.error("An error occurred trying to run DeepThoughtsConnector on port " + messageReceiverPort, ex); }
      }
    }).start();
  }

  public void run() throws Exception {
    startMessageReceiver();
  }

  protected void startMessageReceiver() {
    startMessageReceiver(messageReceiverPort);
  }

  protected void startMessageReceiver(int messageReceiverPort) {
    try {
      messagesReceiver = new MessagesReceiver(messageReceiverPort, listener); // TODO: if already in use, find an unused port
      messagesReceiver.start();

      this.messageReceiverPort = messageReceiverPort;
    } catch(Exception ex) {
      if(isPortAlreadyInUseException(ex) == true)
        startMessageReceiver(++messageReceiverPort); // TODO: check if new port value is larger than 2e16, if so start at 1025 again
      else {
        messagesReceiver = null;
        log.error("Could not start MessagesReceiver", ex);
        Application.notifyUser(new DeepThoughtError(Localization.getLocalizedString("could.not.start.messages.receiver"), ex));
      }
    }
  }

  protected boolean isPortAlreadyInUseException(Exception exception) {
    return exception instanceof BindException && "Address already in use".equals(exception.getMessage());
  }

  @Override
  public void shutDown() {
    try {
      if(messagesReceiver != null) {
        messagesReceiver.stop();
        messagesReceiver = null;
      }
    } catch(Exception ex) { log.error("Could not close Connection", ex); }
  }


  public boolean isDeviceRegistered(HostInfo info) {
    return false; // TODO: implement
  }


  @Override
  public int getMessageReceiverPort() {
    return messageReceiverPort;
  }

  @Override
  public Communicator getCommunicator() {
    return communicator;
  }

  public boolean isStarted() {
    return messagesReceiver != null && messagesReceiver.wasStarted();
  }
}

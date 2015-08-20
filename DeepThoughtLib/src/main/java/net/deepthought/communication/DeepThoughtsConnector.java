package net.deepthought.communication;

import net.deepthought.Application;
import net.deepthought.communication.messages.AskForDeviceRegistrationRequest;
import net.deepthought.communication.model.AllowDeviceToRegisterResult;
import net.deepthought.communication.registration.LookingForRegistrationServersClient;
import net.deepthought.communication.registration.RegisteredPeersManager;
import net.deepthought.communication.registration.RegistrationRequestListener;
import net.deepthought.communication.registration.RegistrationServer;
import net.deepthought.communication.registration.UserDeviceRegistrationRequestListener;
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

  protected RegisteredPeersManager registeredPeersManager;

  protected DeepThoughtsConnectorListener listener;

  protected ConnectorMessagesCreator connectorMessagesCreator;

  protected RegistrationServer registrationServer = null;
  protected UserDeviceRegistrationRequestListener userDeviceRegistrationRequestListener = null;

  protected LookingForRegistrationServersClient searchRegistrationServersClient = null;


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

    this.registeredPeersManager = new RegisteredPeersManager(); // TODO: make configurable
    this.connectorMessagesCreator = new ConnectorMessagesCreator();
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
      messagesReceiver = new MessagesReceiver(messageReceiverPort, messagesReceiverListener);
      messagesReceiver.start();

      this.messageReceiverPort = messageReceiverPort;
    } catch(Exception ex) {
      stopMessagesReceiver();

      if(isPortAlreadyInUseException(ex) == true)
        startMessageReceiver(messageReceiverPort + determineNextPortNumber(messageReceiverPort));
      else {
        log.error("Could not start MessagesReceiver", ex);
        Application.notifyUser(new DeepThoughtError(Localization.getLocalizedString("could.not.start.messages.receiver"), ex));
      }
    }
  }

  protected int determineNextPortNumber(int messageReceiverPort) {
    // TODO: what if by any reason no port is available? then this would produce an infinite loop and therefore a stack overflow
    if(messageReceiverPort < 65535)
      return ++messageReceiverPort;
    return 1025; // maximum port number of 2e16 reached -> restart at first not well known port again
  }

  protected boolean isPortAlreadyInUseException(Exception exception) {
    return exception instanceof BindException && "Address already in use".equals(exception.getMessage());
  }

  @Override
  public void shutDown() {
    closeUserDeviceRegistrationServer();
    stopSearchingOtherUserDevicesToRegisterAt();

    stopMessagesReceiver();
  }

  protected void stopMessagesReceiver() {
    try {
      if(messagesReceiver != null) {
        messagesReceiver.stop();
        messagesReceiver.unsetListener();
        messagesReceiver = null;
      }
    } catch(Exception ex) { log.error("Could not close MessagesReceiver", ex); }
  }


  @Override
  public void openUserDeviceRegistrationServer(UserDeviceRegistrationRequestListener listener) {
    if(registrationServer != null)
      closeUserDeviceRegistrationServer();

    this.userDeviceRegistrationRequestListener = listener;

    registrationServer = new RegistrationServer(connectorMessagesCreator);
    registrationServer.startRegistrationServerAsync();
  }

  @Override
  public void closeUserDeviceRegistrationServer() {
    if(registrationServer != null) {
      registrationServer.closeRegistrationServer();
      registrationServer = null;
    }

    this.userDeviceRegistrationRequestListener = null;
  }

  @Override
  public void findOtherUserDevicesToRegisterAtAsync(RegistrationRequestListener listener) {
    if(searchRegistrationServersClient != null)
      stopSearchingOtherUserDevicesToRegisterAt();

    searchRegistrationServersClient = new LookingForRegistrationServersClient(connectorMessagesCreator);
    searchRegistrationServersClient.findRegistrationServersAsync(listener);
  }

  @Override
  public void stopSearchingOtherUserDevicesToRegisterAt() {
    if(searchRegistrationServersClient != null) {
      searchRegistrationServersClient.stopSearchingForRegistrationServers();
      searchRegistrationServersClient = null;
    }
  }


  @Override
  public int getMessageReceiverPort() {
    return messageReceiverPort;
  }

  @Override
  public Communicator getCommunicator() {
    return communicator;
  }

  @Override
  public RegisteredPeersManager getRegisteredPeersManager() {
    return registeredPeersManager;
  }

  @Override
  public boolean isStarted() {
    return messagesReceiver != null && messagesReceiver.wasStarted();
  }


  protected MessagesReceiverListener messagesReceiverListener = new MessagesReceiverListener() {
    @Override
    public AllowDeviceToRegisterResult registerDeviceRequestRetrieved(AskForDeviceRegistrationRequest request) {
      if(userDeviceRegistrationRequestListener != null)
        return userDeviceRegistrationRequestListener.registerDeviceRequestRetrieved(request);
      return null;
    }
  };
}

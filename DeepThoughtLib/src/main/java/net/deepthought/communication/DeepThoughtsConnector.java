package net.deepthought.communication;

import net.deepthought.Application;
import net.deepthought.communication.connected_device.ConnectedDevicesManager;
import net.deepthought.communication.connected_device.RegisteredDevicesSearcher;
import net.deepthought.communication.listener.MessagesReceiverListener;
import net.deepthought.communication.listener.RegisteredDeviceConnectedListener;
import net.deepthought.communication.listener.RegisteredDeviceDisconnectedListener;
import net.deepthought.communication.messages.AskForDeviceRegistrationRequest;
import net.deepthought.communication.model.AllowDeviceToRegisterResult;
import net.deepthought.communication.model.ConnectedDevice;
import net.deepthought.communication.registration.LookingForRegistrationServersClient;
import net.deepthought.communication.registration.RegisteredDevicesManager;
import net.deepthought.communication.registration.RegistrationRequestListener;
import net.deepthought.communication.registration.RegistrationServer;
import net.deepthought.communication.registration.UserDeviceRegistrationRequestListener;
import net.deepthought.util.DeepThoughtError;
import net.deepthought.util.Localization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.BindException;
import java.net.SocketException;

/**
 * Created by ganymed on 19/08/15.
 */
public class DeepThoughtsConnector implements IDeepThoughtsConnector {

  private final static Logger log = LoggerFactory.getLogger(DeepThoughtsConnector.class);


  protected int messageReceiverPort;

  protected Communicator communicator;

  protected MessagesReceiver messagesReceiver;

  protected RegisteredDevicesManager registeredDevicesManager;

  protected RegisteredDevicesSearcher registeredDevicesSearcher;

  protected ConnectedDevicesManager connectedDevicesManager;

  protected net.deepthought.communication.listener.DeepThoughtsConnectorListener listener;

  protected ConnectorMessagesCreator connectorMessagesCreator;

  protected RegistrationServer registrationServer = null;
  protected UserDeviceRegistrationRequestListener userDeviceRegistrationRequestListener = null;

  protected LookingForRegistrationServersClient searchRegistrationServersClient = null;


  protected DeepThoughtsConnector() {
    this(null);
  }

  public DeepThoughtsConnector(net.deepthought.communication.listener.DeepThoughtsConnectorListener listener) {
    this(Constants.MessageHandlerDefaultPort, listener);
  }

  public DeepThoughtsConnector(int messageReceiverPort, net.deepthought.communication.listener.DeepThoughtsConnectorListener listener) {
    this(messageReceiverPort, new Communicator(), listener);
  }

  public DeepThoughtsConnector(int messageReceiverPort, Communicator communicator, net.deepthought.communication.listener.DeepThoughtsConnectorListener listener) {
    this.messageReceiverPort = messageReceiverPort;
    this.communicator = communicator;
    this.listener = listener;

    this.registeredDevicesManager = new RegisteredDevicesManager(); // TODO: make configurable
    this.connectedDevicesManager = new ConnectedDevicesManager();
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

  @Override
  public void shutDown() {
    closeUserDeviceRegistrationServer();
    stopSearchingOtherUserDevicesToRegisterAt();

    stopMessagesReceiver();

    stopRegisteredDevicesSearcher();
  }

  protected void run() throws Exception {
    startMessageReceiver();

    if(registeredDevicesManager.hasRegisteredDevices() && connectedDevicesManager.getConnectedDevicesCount() < registeredDevicesManager.getRegisteredDevicesCount()) {
      startRegisteredDevicesSearcher();
    }
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
        startMessageReceiver(determineNextPortNumber(messageReceiverPort));
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
    return (exception instanceof BindException || exception instanceof SocketException) && "Address already in use".equals(exception.getMessage());
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

  protected void startRegisteredDevicesSearcher() {
    stopRegisteredDevicesSearcher();

    registeredDevicesSearcher = new RegisteredDevicesSearcher(connectorMessagesCreator);
    registeredDevicesSearcher.startSearchingAsync(registeredDeviceConnectedListener);
  }

  protected void stopRegisteredDevicesSearcher() {
    if(registeredDevicesSearcher != null) {
      registeredDevicesSearcher.stopSearching();
      registeredDevicesSearcher = null;
    }
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


  protected void connectedToRegisteredDevice(ConnectedDevice device) {
    connectedDevicesManager.connectedToDevice(device);

    if(isRegisteredDevicesSearcherRunning() &&
        connectedDevicesManager.getConnectedDevicesCount() >= registeredDevicesManager.getRegisteredDevicesCount())
      stopRegisteredDevicesSearcher();

    if(listener != null)
      listener.registeredDeviceConnected(device);
  }

  protected void disconnectedFromRegisteredDevice(ConnectedDevice device) {
    connectedDevicesManager.disconnectedFromDevice(device);

    if(isRegisteredDevicesSearcherRunning() == false &&
        registeredDevicesManager.getRegisteredDevicesCount() > connectedDevicesManager.getConnectedDevicesCount())
      startRegisteredDevicesSearcher();

    if(listener != null)
      listener.registeredDeviceDisconnected(device);
  }


  @Override
  public int getMessageReceiverPort() {
    return messageReceiverPort;
  }

  @Override
  public Communicator getCommunicator() {
    return communicator;
  }

  protected void setCommunicator(Communicator communicator) {
    this.communicator = communicator;
  }

  @Override
  public boolean isRegisteredDevicesSearcherRunning() {
    return registeredDevicesSearcher != null;
  }

  @Override
  public RegisteredDevicesManager getRegisteredDevicesManager() {
    return registeredDevicesManager;
  }

  public void setRegisteredDevicesManager(RegisteredDevicesManager registeredDevicesManager) {
    this.registeredDevicesManager = registeredDevicesManager;
  }

  @Override
  public ConnectedDevicesManager getConnectedDevicesManager() {
    return connectedDevicesManager;
  }

  public void setConnectedDevicesManager(ConnectedDevicesManager connectedDevicesManager) {
    this.connectedDevicesManager = connectedDevicesManager;
  }

  @Override
  public boolean isStarted() {
    return messagesReceiver != null && messagesReceiver.wasStarted();
  }


  protected net.deepthought.communication.listener.MessagesReceiverListener messagesReceiverListener = new MessagesReceiverListener() {
    @Override
    public AllowDeviceToRegisterResult registerDeviceRequestRetrieved(AskForDeviceRegistrationRequest request) {
      if(userDeviceRegistrationRequestListener != null)
        return userDeviceRegistrationRequestListener.registerDeviceRequestRetrieved(request);
      return null;
    }
  };

  protected RegisteredDeviceConnectedListener registeredDeviceConnectedListener = new RegisteredDeviceConnectedListener() {
    @Override
    public void registeredDeviceConnected(ConnectedDevice device) {
      connectedToRegisteredDevice(device);
    }
  };

  protected RegisteredDeviceDisconnectedListener registeredDeviceDisconnectedListener = new RegisteredDeviceDisconnectedListener() {
    @Override
    public void registeredDeviceDisconnected(ConnectedDevice device) {
      disconnectedFromRegisteredDevice(device);
    }
  };

}

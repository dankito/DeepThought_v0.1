package net.deepthought.communication;

import net.deepthought.Application;
import net.deepthought.communication.connected_device.ConnectedDevicesManager;
import net.deepthought.communication.connected_device.RegisteredDevicesSearcher;
import net.deepthought.communication.listener.DeepThoughtsConnectorListener;
import net.deepthought.communication.listener.MessagesReceiverListener;
import net.deepthought.communication.listener.RegisteredDeviceConnectedListener;
import net.deepthought.communication.listener.RegisteredDeviceDisconnectedListener;
import net.deepthought.communication.listener.ResponseListener;
import net.deepthought.communication.messages.AskForDeviceRegistrationRequest;
import net.deepthought.communication.messages.AskForDeviceRegistrationResponseMessage;
import net.deepthought.communication.messages.Request;
import net.deepthought.communication.messages.Response;
import net.deepthought.communication.messages.ResponseValue;
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
import java.util.HashSet;
import java.util.Set;

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

  protected ConnectorMessagesCreator connectorMessagesCreator;

  protected RegistrationServer registrationServer = null;
  protected UserDeviceRegistrationRequestListener userDeviceRegistrationRequestListener = null;

  protected LookingForRegistrationServersClient searchRegistrationServersClient = null;

  protected Set<DeepThoughtsConnectorListener> connectorListeners = new HashSet<>();

  protected Set<MessagesReceiverListener> messagesReceiverListeners = new HashSet<>();


  protected DeepThoughtsConnector() {
    this(null);
  }

  public DeepThoughtsConnector(DeepThoughtsConnectorListener listener) {
    this(Constants.MessageHandlerDefaultPort, listener);
  }

  public DeepThoughtsConnector(int messageReceiverPort, DeepThoughtsConnectorListener listener) {
    this.messageReceiverPort = messageReceiverPort;
    addConnectorListener(listener);

    // TODO: make configurable
    this.communicator = new Communicator(this, responseListener);
    this.registeredDevicesManager = new RegisteredDevicesManager();
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
    if(isRegistrationServerRunning())
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
    if(isSearchRegistrationServersClientRunning())
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

    for(DeepThoughtsConnectorListener listener : connectorListeners)
      listener.registeredDeviceConnected(device);
  }

  protected void disconnectedFromRegisteredDevice(ConnectedDevice device) {
    connectedDevicesManager.disconnectedFromDevice(device);

    if(isRegisteredDevicesSearcherRunning() == false &&
        registeredDevicesManager.getRegisteredDevicesCount() > connectedDevicesManager.getConnectedDevicesCount())
      startRegisteredDevicesSearcher();

    for(DeepThoughtsConnectorListener listener : connectorListeners)
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
  public boolean isRegistrationServerRunning() {
    return registrationServer != null;
  }

  @Override
  public boolean isSearchRegistrationServersClientRunning() {
    return searchRegistrationServersClient != null;
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


  public boolean addConnectorListener(DeepThoughtsConnectorListener listener) {
    return connectorListeners.add(listener);
  }

  public boolean removeConnectorListener(DeepThoughtsConnectorListener listener) {
    return connectorListeners.remove(listener);
  }

  public boolean addMessagesReceiverListener(MessagesReceiverListener listener) {
    return messagesReceiverListeners.add(listener);
  }

  public boolean removeMessagesReceiverListener(MessagesReceiverListener listener) {
    return messagesReceiverListeners.remove(listener);
  }


  protected MessagesReceiverListener messagesReceiverListener = new MessagesReceiverListener() {

    @Override
    public void registerDeviceRequestRetrieved(AskForDeviceRegistrationRequest request) {
      if(userDeviceRegistrationRequestListener != null)
        userDeviceRegistrationRequestListener.registerDeviceRequestRetrieved(request);

      for(MessagesReceiverListener listener : messagesReceiverListeners)
        listener.registerDeviceRequestRetrieved(request);
    }

    @Override
    public void askForDeviceRegistrationResponseReceived(AskForDeviceRegistrationResponseMessage message) {
      if(message.allowsRegistration()) {
        registerDevice(message, message.useServersUserInformation());
      }

      for(MessagesReceiverListener listener : messagesReceiverListeners)
        listener.askForDeviceRegistrationResponseReceived(message);
    }
  };

  protected ResponseListener responseListener = new ResponseListener() {
    @Override
    public void responseReceived(Request request, Response response) {
      if(request instanceof AskForDeviceRegistrationResponseMessage) {
        AskForDeviceRegistrationResponseMessage message = (AskForDeviceRegistrationResponseMessage)request;
        if(message.allowsRegistration() && response.getResponseValue() == ResponseValue.Ok)
          registerDevice(message, message.useServersUserInformation() == false);
      }
    }
  };

  protected void registerDevice(AskForDeviceRegistrationResponseMessage message, boolean useOtherSidesUserInfo) {
    registeredDevicesManager.registerDevice(message, useOtherSidesUserInfo);
  }

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

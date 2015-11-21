package net.deepthought.communication;

import net.deepthought.Application;
import net.deepthought.communication.connected_device.ConnectedDevicesManager;
import net.deepthought.communication.connected_device.RegisteredDevicesSearcher;
import net.deepthought.communication.listener.CaptureImageOrDoOcrListener;
import net.deepthought.communication.listener.CommunicatorListener;
import net.deepthought.communication.listener.ConnectedDevicesListener;
import net.deepthought.communication.listener.MessagesReceiverListener;
import net.deepthought.communication.listener.RegisteredDeviceConnectedListener;
import net.deepthought.communication.listener.RegisteredDeviceDisconnectedListener;
import net.deepthought.communication.messages.AsynchronousResponseListenerManager;
import net.deepthought.communication.messages.DeepThoughtMessagesReceiverConfig;
import net.deepthought.communication.messages.MessagesDispatcher;
import net.deepthought.communication.messages.MessagesReceiver;
import net.deepthought.communication.messages.request.AskForDeviceRegistrationRequest;
import net.deepthought.communication.messages.request.CaptureImageOrDoOcrRequest;
import net.deepthought.communication.messages.request.GenericRequest;
import net.deepthought.communication.messages.request.Request;
import net.deepthought.communication.messages.request.StopCaptureImageOrDoOcrRequest;
import net.deepthought.communication.messages.response.AskForDeviceRegistrationResponseMessage;
import net.deepthought.communication.messages.response.Response;
import net.deepthought.communication.model.ConnectedDevice;
import net.deepthought.communication.registration.LookingForRegistrationServersClient;
import net.deepthought.communication.registration.RegisteredDevicesManager;
import net.deepthought.communication.registration.RegistrationRequestListener;
import net.deepthought.communication.registration.RegistrationServer;
import net.deepthought.communication.registration.UserDeviceRegistrationRequestListener;
import net.deepthought.util.DeepThoughtError;
import net.deepthought.util.Localization;
import net.deepthought.util.Notification;
import net.deepthought.util.NotificationType;

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

  protected AsynchronousResponseListenerManager listenerManager;

  protected net.deepthought.communication.messages.MessagesReceiver messagesReceiver;

  protected RegisteredDevicesManager registeredDevicesManager;

  protected RegisteredDevicesSearcher registeredDevicesSearcher;

  protected ConnectedDevicesManager connectedDevicesManager;

  protected ConnectorMessagesCreator connectorMessagesCreator;

  protected RegistrationServer registrationServer = null;
  protected UserDeviceRegistrationRequestListener userDeviceRegistrationRequestListener = null;

  protected LookingForRegistrationServersClient searchRegistrationServersClient = null;

  protected ConnectionsAliveWatcher connectionsAliveWatcher = null;

  protected Set<ConnectedDevicesListener> connectedDevicesListeners = new HashSet<>();

  protected Set<CaptureImageOrDoOcrListener> captureImageOrDoOcrListeners = new HashSet<>();

  protected Set<MessagesReceiverListener> messagesReceiverListeners = new HashSet<>();


  public DeepThoughtsConnector() {
    this(Constants.MessageHandlerDefaultPort);
  }

  public DeepThoughtsConnector(int messageReceiverPort) {
    this.messageReceiverPort = messageReceiverPort;

    // TODO: make configurable
    this.listenerManager = new AsynchronousResponseListenerManager();
    this.communicator = new Communicator(new MessagesDispatcher(), listenerManager, this, communicatorListener);
    this.registeredDevicesManager = new RegisteredDevicesManager();
    this.connectedDevicesManager = new ConnectedDevicesManager();
    this.connectorMessagesCreator = new ConnectorMessagesCreator();
  }


  @Override
  public void runAsync() {
    Application.getThreadPool().runTaskAsync(new Runnable() {
      @Override
      public void run() {
        try {
          DeepThoughtsConnector.this.run();
        } catch (Exception ex) {
          log.error("An error occurred trying to run DeepThoughtsConnector on port " + messageReceiverPort, ex);
        }
      }
    });
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

    mayStartRegisteredDevicesSearcher();

    Application.notifyUser(new Notification(NotificationType.DeepThoughtsConnectorStarted, Localization.getLocalizedString("deep.thoughts.connector.started")));
  }

  protected void mayStartRegisteredDevicesSearcher() {
    if(registeredDevicesManager.hasRegisteredDevices() && connectedDevicesManager.getConnectedDevicesCount() < registeredDevicesManager.getRegisteredDevicesCount() &&
        isRegisteredDevicesSearcherRunning() == false) {
      startRegisteredDevicesSearcher();
    }
  }

  protected void startMessageReceiver() {
    startMessageReceiver(messageReceiverPort);
  }

  protected void startMessageReceiver(int messageReceiverPort) {
    try {
      messagesReceiver = new MessagesReceiver(new DeepThoughtMessagesReceiverConfig(messageReceiverPort, listenerManager), messagesReceiverListener);
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
    return (exception instanceof BindException || exception instanceof SocketException) && (
        "Address already in use".equals(exception.getMessage()) || "Socket is closed".equals(exception.getMessage()));
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

  protected void startConnectionsAliveWatcher() {
    stopConnectionsAliveWatcher();

    connectionsAliveWatcher = new ConnectionsAliveWatcher(connectedDevicesManager, communicator);
    connectionsAliveWatcher.startWatchingAsync(registeredDeviceDisconnectedListener);
  }

  protected void mayStartConnectionsAliveWatcher() {
    if(connectedDevicesManager.areDevicesConnected() && isConnectionWatcherRunning() == false)
      startConnectionsAliveWatcher();
  }

  protected void stopConnectionsAliveWatcher() {
    if(connectionsAliveWatcher != null) {
      connectionsAliveWatcher.stopWatching();
      connectionsAliveWatcher = null;
    }
  }

  protected void mayStopConnectionsAliveWatcher() {
    if(connectedDevicesManager.getConnectedDevicesCount() == 0 && isConnectionWatcherRunning() == true)
      stopConnectionsAliveWatcher();
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

  protected void mayStopRegisteredDevicesSearcher() {
    if(isRegisteredDevicesSearcherRunning() &&
        connectedDevicesManager.getConnectedDevicesCount() >= registeredDevicesManager.getRegisteredDevicesCount())
      stopRegisteredDevicesSearcher();
  }


  protected boolean connectedToRegisteredDevice(ConnectedDevice device) {
    if(device.getDevice() == null)
      device.setStoredDeviceInstance(); // if it's from a Communicator message locally stored Device instance isn't set yet

    if(connectedDevicesManager.connectedToDevice(device)) {
      communicator.notifyRemoteWeHaveConnected(device); // notify peer that we found him so that he for sure knows about our existence

      for (ConnectedDevicesListener listener : connectedDevicesListeners)
        listener.registeredDeviceConnected(device);
    }

    mayStopRegisteredDevicesSearcher();
    mayStartConnectionsAliveWatcher();

    return true;
  }

  protected void disconnectedFromRegisteredDevice(ConnectedDevice device) {
    if(connectedDevicesManager.disconnectedFromDevice(device)) {

      mayStartRegisteredDevicesSearcher();
      mayStopConnectionsAliveWatcher();

      for (ConnectedDevicesListener listener : connectedDevicesListeners)
        listener.registeredDeviceDisconnected(device);
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

  protected void setCommunicator(Communicator communicator) {
    this.communicator = communicator;
  }

  @Override
  public AsynchronousResponseListenerManager getListenerManager() {
    return listenerManager;
  }

  @Override
  public boolean isRegisteringAllowed() {
    return isRegistrationServerRunning();
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
  public boolean isConnectionWatcherRunning() {
    return connectionsAliveWatcher != null;
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


  public boolean addConnectedDevicesListener(ConnectedDevicesListener listener) {
    return connectedDevicesListeners.add(listener);
  }

  public boolean removeConnectedDevicesListener(ConnectedDevicesListener listener) {
    return connectedDevicesListeners.remove(listener);
  }

  public boolean addCaptureImageOrDoOcrListener(CaptureImageOrDoOcrListener listener) {
    return captureImageOrDoOcrListeners.add(listener);
  }

  public boolean removeCaptureImageOrDoOcrListener(CaptureImageOrDoOcrListener listener) {
    return captureImageOrDoOcrListeners.remove(listener);
  }

  public boolean addMessagesReceiverListener(MessagesReceiverListener listener) {
    return messagesReceiverListeners.add(listener);
  }

  public boolean removeMessagesReceiverListener(MessagesReceiverListener listener) {
    return messagesReceiverListeners.remove(listener);
  }


  protected CommunicatorListener communicatorListener = new CommunicatorListener() {

    @Override
    public void responseReceived(Request request, Response response) {

    }

    @Override
    public void serverAllowedDeviceRegistration(AskForDeviceRegistrationRequest request, AskForDeviceRegistrationResponseMessage response) {
      registerDevice(request, response.useServersUserInformation() == false);
    }
  };

  protected void registerDevice(AskForDeviceRegistrationRequest message, boolean useOtherSidesUserInfo) {
    registeredDevicesManager.registerDevice(message, useOtherSidesUserInfo);

//    mayStartRegisteredDevicesSearcher();
    communicator.notifyRemoteWeHaveConnected(new ConnectedDevice(message.getDevice().getUniversallyUniqueId(), message.getAddress(), message.getPort()));

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

  protected MessagesReceiverListener messagesReceiverListener = new MessagesReceiverListener() {

    @Override
    public void registerDeviceRequestRetrieved(AskForDeviceRegistrationRequest request) {
      if(userDeviceRegistrationRequestListener != null)
        userDeviceRegistrationRequestListener.registerDeviceRequestRetrieved(request);
      else // if listener is null it's not possible that the user chooses whether she/he likes to allow register or not -> send a deny directly
        communicator.sendAskForDeviceRegistrationResponse(request, AskForDeviceRegistrationResponseMessage.Deny, null);

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

    @Override
    public boolean messageReceived(String methodName, Request request) {
      return handleReceivedMessage(methodName, request);
    }
  };

  protected boolean handleReceivedMessage(String methodName, Request request) {
    for(MessagesReceiverListener listener : messagesReceiverListeners)
      listener.messageReceived(methodName, request);

    if(Addresses.NotifyRemoteWeHaveConnectedMethodName.equals(methodName)) {
      return connectedToRegisteredDevice(((GenericRequest<ConnectedDevice>)request).getRequestBody());
    }
    else if(Addresses.HeartbeatMethodName.equals(methodName)) {
      // TODO: is this correct, calling connectedToRegisteredDevice() ?
      return connectedToRegisteredDevice(((GenericRequest<ConnectedDevice>)request).getRequestBody());
    }
    else if(Addresses.StartCaptureImageAndDoOcrMethodName.equals(methodName)) {
      return handleStartCaptureImageAndDoOcrMessage((CaptureImageOrDoOcrRequest) request);
    }
    else if(Addresses.CaptureImageResultMethodName.equals(methodName)) {
      return true;
    }
    else if(Addresses.OcrResultMethodName.equals(methodName)) {
      return true;
    }
    else if(Addresses.StopCaptureImageAndDoOcrMethodName.equals(methodName)) {
      return handleStopCaptureImageOrDoOcrMessage((StopCaptureImageOrDoOcrRequest) request);
    }

    return false;
  }

  protected boolean handleStartCaptureImageAndDoOcrMessage(CaptureImageOrDoOcrRequest request) {
    for(CaptureImageOrDoOcrListener listener : captureImageOrDoOcrListeners)
      listener.startCaptureImageOrDoOcr(request);
    return true;
  }

  protected boolean handleStopCaptureImageOrDoOcrMessage(StopCaptureImageOrDoOcrRequest request) {
    for(CaptureImageOrDoOcrListener listener : captureImageOrDoOcrListeners)
      listener.stopCaptureImageOrDoOcr(request);
    return true;
  }

}

package net.deepthought.communication;

import net.deepthought.Application;
import net.deepthought.communication.connected_device.ConnectedDevicesManager;
import net.deepthought.communication.connected_device.IConnectedDevicesListener;
import net.deepthought.communication.listener.ImportFilesOrDoOcrListener;
import net.deepthought.communication.listener.MessagesReceiverListener;
import net.deepthought.communication.messages.AsynchronousResponseListenerManager;
import net.deepthought.communication.messages.DeepThoughtMessagesReceiverConfig;
import net.deepthought.communication.messages.MessagesDispatcher;
import net.deepthought.communication.messages.MessagesReceiver;
import net.deepthought.communication.messages.request.AskForDeviceRegistrationRequest;
import net.deepthought.communication.messages.request.DoOcrRequest;
import net.deepthought.communication.messages.request.GenericRequest;
import net.deepthought.communication.messages.request.ImportFilesRequest;
import net.deepthought.communication.messages.request.Request;
import net.deepthought.communication.messages.request.RequestWithAsynchronousResponse;
import net.deepthought.communication.messages.request.StopRequestWithAsynchronousResponse;
import net.deepthought.communication.messages.response.AskForDeviceRegistrationResponse;
import net.deepthought.communication.model.ConnectedDevice;
import net.deepthought.communication.model.HostInfo;
import net.deepthought.communication.registration.IUnregisteredDevicesListener;
import net.deepthought.communication.registration.RegisteredDevicesManager;
import net.deepthought.data.model.Device;
import net.deepthought.data.model.User;
import net.deepthought.util.DeepThoughtError;
import net.deepthought.util.IThreadPool;
import net.deepthought.util.Notification;
import net.deepthought.util.NotificationType;
import net.deepthought.util.localization.Localization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.BindException;
import java.net.SocketException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by ganymed on 19/08/15.
 */
public class DeepThoughtConnector implements IDeepThoughtConnector {

  private final static Logger log = LoggerFactory.getLogger(DeepThoughtConnector.class);


  protected int messageReceiverPort;

  protected Communicator communicator;

  protected IThreadPool threadPool;

  protected AsynchronousResponseListenerManager listenerManager;

  protected net.deepthought.communication.messages.MessagesReceiver messagesReceiver;

  protected RegisteredDevicesManager registeredDevicesManager;

  protected ConnectedDevicesManager connectedDevicesManager;

  protected ConnectorMessagesCreator connectorMessagesCreator;

  protected IDevicesFinder devicesFinder = null;

  protected Set<IUnregisteredDevicesListener> unregisteredDevicesListeners = new HashSet<>();

  protected Set<IConnectedDevicesListener> connectedDevicesListeners = new HashSet<>();

  protected Set<ImportFilesOrDoOcrListener> importFilesOrDoOcrListeners = new HashSet<>();

  protected Set<MessagesReceiverListener> messagesReceiverListeners = new HashSet<>();


  public DeepThoughtConnector() {
    this(Constants.MessageHandlerDefaultPort);
  }

  public DeepThoughtConnector(int messageReceiverPort) {
    this.messageReceiverPort = messageReceiverPort;

    // TODO: make configurable
    this.threadPool = Application.getThreadPool();
    this.listenerManager = new AsynchronousResponseListenerManager();
    this.connectorMessagesCreator = new ConnectorMessagesCreator(new ConnectorMessagesCreatorConfig(getLoggedOnUser(), getLocalDevice(),
          NetworkHelper.getIPAddressString(true), messageReceiverPort));
    this.registeredDevicesManager = new RegisteredDevicesManager();
    this.connectedDevicesManager = new ConnectedDevicesManager();

    this.communicator = new Communicator(new CommunicatorConfig(new MessagesDispatcher(threadPool), listenerManager, messageReceiverPort, connectorMessagesCreator, registeredDevicesManager));

    // TODO: create with DependencyResolver
    this.devicesFinder = new UdpDevicesFinder(communicator, threadPool, connectedDevicesManager, connectorMessagesCreator, getLoggedOnUser(), getLocalDevice());
  }


  @Override
  public void runAsync() {
    Application.getThreadPool().runTaskAsync(new Runnable() {
      @Override
      public void run() {
        try {
          DeepThoughtConnector.this.run();
        } catch (Exception ex) {
          log.error("An error occurred trying to run DeepThoughtConnector on port " + messageReceiverPort, ex);
        }
      }
    });
  }

  @Override
  public void shutDown() {
    stopMessagesReceiver();

    devicesFinder.stop();
  }

  protected void run() throws Exception {
    startMessageReceiver();

    devicesFinder.startAsync(devicesFinderListener);

    Application.notifyUser(new Notification(NotificationType.DeepThoughtsConnectorStarted, Localization.getLocalizedString("deep.thoughts.connector.started")));
  }

  protected void startMessageReceiver() {
    startMessageReceiver(messageReceiverPort);
  }

  protected void startMessageReceiver(int messageReceiverPort) {
    try {
      messagesReceiver = new MessagesReceiver(new DeepThoughtMessagesReceiverConfig(messageReceiverPort, listenerManager), messagesReceiverListener);
      messagesReceiver.start();

      setMessageReceiverPort(messageReceiverPort);
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

  protected void connectedToUnregisteredDevice(HostInfo device) {
    for(IUnregisteredDevicesListener listener : unregisteredDevicesListeners) {
      listener.unregisteredDeviceFound(device);
    }
  }

  protected void deviceIsAskingForRegistration(AskForDeviceRegistrationRequest request) {
    if(unregisteredDevicesListeners.size() > 0) {
      for (IUnregisteredDevicesListener listener : unregisteredDevicesListeners) {
        listener.deviceIsAskingForRegistration(request);
      }
    }
    else { // if listener is null it's not possible that the user chooses whether she/he likes to allow register or not -> send a deny directly
      communicator.respondToAskForDeviceRegistrationRequest(request, AskForDeviceRegistrationResponse.Deny, null);
    }
  }


  protected void foundRegisteredDevice(HostInfo device) {
    communicator.notifyRemoteWeHaveConnected(device); // tell registered Device of our presence -> it will send its Device Capabilities to us -> we're connected
  }

  protected boolean connectedToRegisteredDevice(ConnectedDevice device) {
    if(device.getDevice() == null) {
      device.setStoredDeviceInstance(getLoggedOnUser()); // if it's from a Communicator message locally stored Device instance isn't set yet
    }

    if(connectedDevicesManager.connectedToDevice(device)) { // check if we're not already aware of this device
      communicator.notifyRemoteWeHaveConnected(device); // notify peer that we found him so that he for sure knows about our existence

      for (IConnectedDevicesListener listener : connectedDevicesListeners) {
        listener.registeredDeviceConnected(device);
      }
    }

    return true;
  }

  protected void disconnectedFromRegisteredDevice(HostInfo device) {
    if(connectedDevicesManager.isConnectedToDevice(device)) {
      disconnectedFromRegisteredDevice(connectedDevicesManager.getConnectedDeviceForHostInfo(device));
    }
  }

  protected void disconnectedFromRegisteredDevice(ConnectedDevice device) {
    if(connectedDevicesManager.disconnectedFromDevice(device)) {
      for (IConnectedDevicesListener listener : connectedDevicesListeners) {
        listener.registeredDeviceDisconnected(device);
      }
    }
  }


  @Override
  public int getMessageReceiverPort() {
    return messageReceiverPort;
  }

  protected void setMessageReceiverPort(int messageReceiverPort) {
    this.messageReceiverPort = messageReceiverPort;

    // TODO: what did you learn in Object Orientated Programming?
    connectorMessagesCreator.config.messageReceiverPort = messageReceiverPort;
    communicator.setMessageReceiverPort(messageReceiverPort);
  }

  @Override
  public Communicator getCommunicator() {
    return communicator;
  }

  protected void setCommunicator(Communicator communicator) {
    this.communicator = communicator;
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


  public boolean addUnregisteredDevicesListener(IUnregisteredDevicesListener listener) {
    return unregisteredDevicesListeners.add(listener);
  }

  public boolean removeUnregisteredDevicesListener(IUnregisteredDevicesListener listener) {
    return unregisteredDevicesListeners.remove(listener);
  }

  public boolean addConnectedDevicesListener(IConnectedDevicesListener listener) {
    return connectedDevicesListeners.add(listener);
  }

  public boolean removeConnectedDevicesListener(IConnectedDevicesListener listener) {
    return connectedDevicesListeners.remove(listener);
  }

  public boolean addImportFilesOrDoOcrListener(ImportFilesOrDoOcrListener listener) {
    return importFilesOrDoOcrListeners.add(listener);
  }

  public boolean removeImportFilesOrDoOcrListener(ImportFilesOrDoOcrListener listener) {
    return importFilesOrDoOcrListeners.remove(listener);
  }

  public boolean addMessagesReceiverListener(MessagesReceiverListener listener) {
    return messagesReceiverListeners.add(listener);
  }

  public boolean removeMessagesReceiverListener(MessagesReceiverListener listener) {
    return messagesReceiverListeners.remove(listener);
  }


  protected Device getLocalDevice() {
    return Application.getApplication().getLocalDevice();
  }

  protected User getLoggedOnUser() {
    return Application.getLoggedOnUser();
  }


  protected void registerDevice(AskForDeviceRegistrationRequest message, boolean useOtherSidesUserInfo) {
    registeredDevicesManager.registerDevice(message, useOtherSidesUserInfo);

//    mayStartRegisteredDevicesSearcher();
    communicator.notifyRemoteWeHaveConnected(new ConnectedDevice(message.getDevice().getDeviceId(), message.getAddress(), message.getPort()));

  }

  protected IDevicesFinderListener devicesFinderListener = new IDevicesFinderListener() {
    @Override
    public void deviceFound(HostInfo device) {
      if(registeredDevicesManager.isDeviceRegistered(device)) {
        foundRegisteredDevice(device);
      }
      else {
        connectedToUnregisteredDevice(device);
      }
    }

    @Override
    public void deviceDisconnected(HostInfo device) {
      if(registeredDevicesManager.isDeviceRegistered(device)) {
        disconnectedFromRegisteredDevice(device);
      }
    }
  };


  protected MessagesReceiverListener messagesReceiverListener = new MessagesReceiverListener() {

    @Override
    public boolean messageReceived(String methodName, Request request) {
      return handleReceivedMessage(methodName, request);
    }
  };

  protected boolean handleReceivedMessage(String methodName, Request request) {
    for(MessagesReceiverListener listener : messagesReceiverListeners)
      listener.messageReceived(methodName, request);

    if(Addresses.AskForDeviceRegistrationMethodName.equals(methodName)) {
      return handleAskForDeviceRegistrationRequest((AskForDeviceRegistrationRequest) request);
    }
    else if(Addresses.AskForDeviceRegistrationResponseMethodName.equals(methodName)) {
      return handleAskForDeviceRegistrationResponse((AskForDeviceRegistrationResponse) request);
    }
    else if(Addresses.NotifyRemoteWeHaveConnectedMethodName.equals(methodName)) {
      return connectedToRegisteredDevice(((GenericRequest<ConnectedDevice>) request).getRequestBody());
    }
    else if(Addresses.HeartbeatMethodName.equals(methodName)) {
      // TODO: is this correct, calling connectedToRegisteredDevice() ?
      return connectedToRegisteredDevice(((GenericRequest<ConnectedDevice>) request).getRequestBody());
    }
    else if(Addresses.StartImportFilesMethodName.equals(methodName)) {
      return handleImportFilesMessage((ImportFilesRequest) request);
    }
    else if(Addresses.ImportFilesResultMethodName.equals(methodName)) {
      return true;
    }
    else if(Addresses.DoOcrOnImageMethodName.equals(methodName)) {
      return handleDoOcrOnImageMessage((DoOcrRequest) request);
    }
    else if(Addresses.OcrResultMethodName.equals(methodName)) {
      return true;
    }
    else if(Addresses.StartScanBarcodeMethodName.equals(methodName)) {
      return handleStartScanBarcodeMessage((RequestWithAsynchronousResponse) request);
    }
    else if(Addresses.ScanBarcodeResultMethodName.equals(methodName)) {
      return true;
    }
    else if(Addresses.StopScanBarcodeMethodName.equals(methodName)) {
      return handleStopScanBarcodeMessage((StopRequestWithAsynchronousResponse) request);
    }

    return false;
  }

  protected boolean handleAskForDeviceRegistrationRequest(AskForDeviceRegistrationRequest request) {
    deviceIsAskingForRegistration(request);

    return true;
  }

  protected boolean handleAskForDeviceRegistrationResponse(AskForDeviceRegistrationResponse message) {
    if(message.allowsRegistration()) {
      registerDevice(message, message.useServersUserInformation());
    }

    return true;
  }

  protected boolean handleImportFilesMessage(ImportFilesRequest request) {
    for(ImportFilesOrDoOcrListener listener : importFilesOrDoOcrListeners)
      listener.importFiles(request);
    return true;
  }

  protected boolean handleDoOcrOnImageMessage(DoOcrRequest request) {
    for(ImportFilesOrDoOcrListener listener : importFilesOrDoOcrListeners)
      listener.doOcr(request);
    return true;
  }

  protected boolean handleStartScanBarcodeMessage(RequestWithAsynchronousResponse request) {
    for(ImportFilesOrDoOcrListener listener : importFilesOrDoOcrListeners)
      listener.scanBarcode(request);
    return true;
  }

  protected boolean handleStopScanBarcodeMessage(StopRequestWithAsynchronousResponse request) {
    for(ImportFilesOrDoOcrListener listener : importFilesOrDoOcrListeners)
      // TODO
      listener.stopCaptureImageOrDoOcr(request);
    return true;
  }

}

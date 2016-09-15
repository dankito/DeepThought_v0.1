package net.dankito.deepthought.communication.registration;

import net.dankito.deepthought.communication.Communicator;
import net.dankito.deepthought.communication.ConnectorMessagesCreator;
import net.dankito.deepthought.communication.IDeepThoughtConnector;
import net.dankito.deepthought.communication.listener.AskForDeviceRegistrationResultListener;
import net.dankito.deepthought.communication.listener.ResponseListener;
import net.dankito.deepthought.communication.messages.request.AskForDeviceRegistrationRequest;
import net.dankito.deepthought.communication.messages.request.Request;
import net.dankito.deepthought.communication.messages.response.AskForDeviceRegistrationResponse;
import net.dankito.deepthought.communication.messages.response.Response;
import net.dankito.deepthought.communication.messages.response.ResponseCode;
import net.dankito.deepthought.communication.model.HostInfo;
import net.dankito.deepthought.data.model.DeepThought;
import net.dankito.deepthought.data.model.Device;
import net.dankito.deepthought.data.model.User;
import net.dankito.deepthought.data.sync.InitialSyncManager;
import net.dankito.deepthought.util.IThreadPool;
import net.dankito.deepthought.util.localization.Localization;

/**
 * Created by ganymed on 07/09/16.
 */
public abstract class DeviceRegistrationHandlerBase {

  protected Communicator communicator;

  protected InitialSyncManager initialSyncManager;

  protected IThreadPool threadPool;

  protected IRegisteredDevicesManager registeredDevicesManager;

  protected ConnectorMessagesCreator messagesCreator;

  protected DeepThought deepThought;
  protected User loggedOnUser;
  protected Device localDevice;


  public DeviceRegistrationHandlerBase(IDeepThoughtConnector deepThoughtConnector, IThreadPool threadPool, InitialSyncManager initialSyncManager,
                                       DeepThought deepThought, User loggedOnUser, Device localDevice) {
    this.communicator = deepThoughtConnector.getCommunicator();
    this.threadPool = threadPool;
    this.initialSyncManager = initialSyncManager;
    this.registeredDevicesManager = deepThoughtConnector.getRegisteredDevicesManager();
    this.messagesCreator = deepThoughtConnector.getMessagesCreator();

    this.deepThought = deepThought;
    this.loggedOnUser = loggedOnUser;
    this.localDevice = localDevice;

    deepThoughtConnector.addUnregisteredDevicesListener(unregisteredDevicesListener);
  }


  protected abstract void unregisteredDeviceFound(HostInfo device);

  protected abstract void deviceIsAskingForRegistration(AskForDeviceRegistrationRequest request);

  protected abstract void showMessageToReceivedAskForRegistrationResponse(AskForDeviceRegistrationResponse response);

  protected abstract void showErrorSynchronizingWithDeviceNotPossible(String message, String messageTitle);

  protected abstract void showRegistrationSuccessfulMessage(String message, String messageTitle);


  protected IUnregisteredDevicesListener unregisteredDevicesListener = new IUnregisteredDevicesListener() {
    @Override
    public void unregisteredDeviceFound(HostInfo device) {
      DeviceRegistrationHandlerBase.this.unregisteredDeviceFound(device);
    }

    @Override
    public void deviceIsAskingForRegistration(AskForDeviceRegistrationRequest request) {
      DeviceRegistrationHandlerBase.this.deviceIsAskingForRegistration(request);
    }
  };


  protected void askForRegistration(HostInfo device) {
    communicator.askForDeviceRegistration(device, loggedOnUser, localDevice, new AskForDeviceRegistrationResultListener() {
      @Override
      public void responseReceived(AskForDeviceRegistrationRequest request, final AskForDeviceRegistrationResponse response) {
        if(response != null) {
          askForRegistrationResponseReceived(response);
        }
      }
    });
  }

  protected void askForRegistrationResponseReceived(AskForDeviceRegistrationResponse response) {
    if(response.allowsRegistration()) {
      doInitialSynchronization(response, response.getUseSendersUserInformation(), response.getUseSendersDatabaseIds());
    }

    showMessageToReceivedAskForRegistrationResponse(response);
  }


  protected void sendAskUserIfRegisteringDeviceIsAllowedResponse(AskForDeviceRegistrationRequest request, boolean userAllowsDeviceRegistration) {
    final AskForDeviceRegistrationResponse result;

    if(userAllowsDeviceRegistration == false) {
      result = AskForDeviceRegistrationResponse.createDenyRegistrationResponse(loggedOnUser, localDevice);
    }
    else {
      result = createRegistrationIsAllowedResult(request);
    }

    if(result != null) { // createRegistrationIsAllowedResult() can catch an Exception and then return null
      sendRespondToAskForDeviceRegistrationRequest(request, result);
    }
  }

  protected AskForDeviceRegistrationResponse createRegistrationIsAllowedResult(AskForDeviceRegistrationRequest request) {
    // TODO: check if user information differ and if so ask which one to use
    boolean useLocalUserInformation = initialSyncManager.shouldUseLocalUserName(loggedOnUser, request.getUser());
    boolean useLocalDatabaseIds = false;
    try {
      useLocalDatabaseIds = initialSyncManager.shouldUseLocalDatabaseIds(deepThought, loggedOnUser, localDevice,
          request.getCurrentDeepThoughtInfo(), request.getUser(), request.getDevice());
    } catch(IllegalStateException e) {
      showErrorSynchronizingWithDeviceNotPossible(e.getLocalizedMessage(), Localization.getLocalizedString("alert.message.title.cannot.connect.with.device"));
      return null;
    }

    return AskForDeviceRegistrationResponse.createAllowRegistrationResponse(useLocalUserInformation, useLocalDatabaseIds, loggedOnUser, localDevice);
  }

  protected void sendRespondToAskForDeviceRegistrationRequest(final AskForDeviceRegistrationRequest request, final AskForDeviceRegistrationResponse result) {
    communicator.respondToAskForDeviceRegistrationRequest(request, result, new ResponseListener() {
      @Override
      public void responseReceived(Request request1, Response response) {
        if(response.getResponseCode() == ResponseCode.Ok) {
          if(result.allowsRegistration()) {
            successfullyRegisteredWithRemote(request, result);
          }
          else {
            // can this really ever happen?
          }
        }
      }
    });
  }

  protected void successfullyRegisteredWithRemote(AskForDeviceRegistrationRequest request, AskForDeviceRegistrationResponse response) {
    doInitialSynchronization(request, !response.getUseSendersUserInformation(), !response.getUseSendersDatabaseIds());

    showRegistrationSuccessfulMessage(Localization.getLocalizedString("alert.message.successfully.registered.device", request.getDevice()),
        Localization.getLocalizedString("alert.title.device.registration.successful"));
  }

  protected void doInitialSynchronization(AskForDeviceRegistrationRequest requestOrResponse, boolean syncUserInformation, boolean syncDatabaseIds) {
    if(syncUserInformation) {
      syncUserInformationWithRemoteOnes(requestOrResponse);
    }

    if(syncDatabaseIds) {
      syncLocalDatabaseIdsWithRemoteOnesAsync(requestOrResponse);
    }

    if(syncDatabaseIds == false) { // otherwise database ids synchronization is still being done async, so at end of this process initialSynchronizationDone() will be called
      initialSynchronizationDone(requestOrResponse);
    }
  }


  protected void syncUserInformationWithRemoteOnes(AskForDeviceRegistrationRequest requestOrResponse) {
    initialSyncManager.syncUserInformationWithRemoteOnes(loggedOnUser, requestOrResponse.getUser(), requestOrResponse.getGroup());

    messagesCreator.setLoggedOnUser(loggedOnUser);
  }

  protected void syncLocalDatabaseIdsWithRemoteOnesAsync(final AskForDeviceRegistrationRequest requestOrResponse) {
    threadPool.runTaskAsync(new Runnable() {
      @Override
      public void run() {
        syncLocalDatabaseIdsWithRemoteOnes(requestOrResponse);
      }
    });
  }

  protected void syncLocalDatabaseIdsWithRemoteOnes(AskForDeviceRegistrationRequest requestOrResponse) {
    initialSyncManager.syncLocalDatabaseIdsWithRemoteOnes(deepThought, loggedOnUser, localDevice, requestOrResponse);

    initialSynchronizationDone(requestOrResponse);
  }


  protected void initialSynchronizationDone(AskForDeviceRegistrationRequest requestOrResponse) {
    registeredDevicesManager.registerDevice(requestOrResponse);

    communicator.notifyRemoteWeHaveConnected(requestOrResponse.getDevice()); // so that remote Device knows we're done with initial synchronization and can start syncing / show us as connected to User
  }

}

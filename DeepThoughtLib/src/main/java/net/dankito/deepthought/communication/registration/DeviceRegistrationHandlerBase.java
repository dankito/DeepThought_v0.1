package net.dankito.deepthought.communication.registration;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.communication.Communicator;
import net.dankito.deepthought.communication.IDeepThoughtConnector;
import net.dankito.deepthought.communication.listener.AskForDeviceRegistrationResultListener;
import net.dankito.deepthought.communication.listener.ResponseListener;
import net.dankito.deepthought.communication.messages.request.AskForDeviceRegistrationRequest;
import net.dankito.deepthought.communication.messages.request.Request;
import net.dankito.deepthought.communication.messages.response.AskForDeviceRegistrationResponse;
import net.dankito.deepthought.communication.messages.response.Response;
import net.dankito.deepthought.communication.messages.response.ResponseCode;
import net.dankito.deepthought.communication.model.HostInfo;
import net.dankito.deepthought.data.model.Device;
import net.dankito.deepthought.data.model.User;
import net.dankito.deepthought.data.sync.InitialSyncManager;
import net.dankito.deepthought.util.localization.Localization;

/**
 * Created by ganymed on 07/09/16.
 */
public abstract class DeviceRegistrationHandlerBase {

  protected Communicator communicator;

  protected InitialSyncManager initialSyncManager;


  public DeviceRegistrationHandlerBase(IDeepThoughtConnector deepThoughtConnector, InitialSyncManager initialSyncManager) {
    this.communicator = deepThoughtConnector.getCommunicator();
    this.initialSyncManager = initialSyncManager;

    deepThoughtConnector.addUnregisteredDevicesListener(unregisteredDevicesListener);
  }


  protected abstract void unregisteredDeviceFound(HostInfo device);

  protected abstract void deviceIsAskingForRegistration(AskForDeviceRegistrationRequest request);

  protected abstract void askForRegistrationResponseReceived(AskForDeviceRegistrationResponse response);

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
    communicator.askForDeviceRegistration(device, Application.getLoggedOnUser(), Application.getApplication().getLocalDevice(), new AskForDeviceRegistrationResultListener() {
      @Override
      public void responseReceived(AskForDeviceRegistrationRequest request, final AskForDeviceRegistrationResponse response) {
        if (response != null) {
          askForRegistrationResponseReceived(response);
        }
      }
    });
  }


  protected void sendAskUserIfRegisteringDeviceIsAllowedResponse(AskForDeviceRegistrationRequest request, boolean userAllowsDeviceRegistration) {
    final AskForDeviceRegistrationResponse result;

    if(userAllowsDeviceRegistration == false) {
      result = AskForDeviceRegistrationResponse.Deny;
    }
    else {
      result = createRegistrationIsAllowedResult(request);
    }

    if(result != null) { // createRegistrationIsAllowedResult() can catch an Exception and then return null
      sendRespondToAskForDeviceRegistrationRequest(request, result);
    }
  }

  protected AskForDeviceRegistrationResponse createRegistrationIsAllowedResult(AskForDeviceRegistrationRequest request) {
    User loggedOnUser = Application.getLoggedOnUser();
    Device localDevice = Application.getApplication().getLocalDevice();

    // TODO: check if user information differ and if so ask which one to use
    boolean useLocalUserInformation = initialSyncManager.shouldUseLocalUserName(loggedOnUser, request.getUser());
    boolean useLocalDatabaseIds = false;
    try {
      useLocalDatabaseIds = initialSyncManager.shouldUseLocalDatabaseIds(Application.getDeepThought(), loggedOnUser, localDevice,
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
            showRegistrationSuccessfulMessage(Localization.getLocalizedString("alert.message.successfully.registered.device", request.getDevice()),
                Localization.getLocalizedString("alert.title.device.registration.successful"));
          }
          else {
            // can this really ever happen?
          }
        }
      }
    });
  }

}

package net.deepthought.util;

import net.deepthought.Application;
import net.deepthought.communication.IDeepThoughtConnector;
import net.deepthought.communication.listener.AskForDeviceRegistrationResultListener;
import net.deepthought.communication.listener.ResponseListener;
import net.deepthought.communication.messages.request.AskForDeviceRegistrationRequest;
import net.deepthought.communication.messages.request.Request;
import net.deepthought.communication.messages.response.AskForDeviceRegistrationResponse;
import net.deepthought.communication.messages.response.Response;
import net.deepthought.communication.messages.response.ResponseCode;
import net.deepthought.communication.model.HostInfo;
import net.deepthought.communication.registration.IUnregisteredDevicesListener;
import net.deepthought.controls.utils.FXUtils;
import net.deepthought.util.localization.Localization;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

/**
 * Created by ganymed on 07/06/16.
 */
public class DeviceRegistrationHandler {

  protected Stage stage;

  public DeviceRegistrationHandler(Stage stage, IDeepThoughtConnector deepThoughtConnector) {
    this.stage = stage;

    deepThoughtConnector.addUnregisteredDevicesListener(unregisteredDevicesListener);
  }


  protected IUnregisteredDevicesListener unregisteredDevicesListener = new IUnregisteredDevicesListener() {
    @Override
    public void unregisteredDeviceFound(HostInfo device) {
      Platform.runLater(() -> showNotificationUnregisteredDeviceFound(device)); // Alert has to run on UI thread but listener method for sure is not called on UI thread
    }

    @Override
    public void deviceIsAskingForRegistration(AskForDeviceRegistrationRequest request) {
      Platform.runLater(() -> askUserIfRegisteringDeviceIsAllowed(request));
    }
  };


  protected Map<String, Map<String, Alert>> unregisteredDeviceFoundAlerts = new ConcurrentHashMap<>();

  protected void showNotificationUnregisteredDeviceFound(HostInfo device) {
    Alert unregisteredDeviceFoundAlert = Alerts.createUnregisteredDeviceFoundAlert(device, stage);

    if(unregisteredDeviceFoundAlerts.containsKey(device.getDeviceId()) == false) {
      unregisteredDeviceFoundAlerts.put(device.getDeviceId(), new ConcurrentHashMap<>());
    }
    unregisteredDeviceFoundAlerts.get(device.getDeviceId()).put(device.getUserUniqueId(), unregisteredDeviceFoundAlert);


    Optional<ButtonType> result = unregisteredDeviceFoundAlert.showAndWait();
    boolean likesToConnectWithDevice = result.get() == ButtonType.YES;

    unregisteredDeviceFoundAlerts.get(device.getDeviceId()).remove(device.getUserUniqueId());
    if(unregisteredDeviceFoundAlerts.get(device.getDeviceId()).size() == 0) {
      unregisteredDeviceFoundAlerts.remove(device.getDeviceId());
    }

    if(likesToConnectWithDevice) {
      askForRegistration(device);
    }
  }

  protected void askForRegistration(HostInfo device) {
    Application.getDeepThoughtConnector().getCommunicator().askForDeviceRegistration(device, Application.getLoggedOnUser(), Application.getApplication().getLocalDevice(), new AskForDeviceRegistrationResultListener() {
      @Override
      public void responseReceived(AskForDeviceRegistrationRequest request, final AskForDeviceRegistrationResponse response) {
        if (response != null) {
          FXUtils.runOnUiThread(() -> showAskForDeviceRegistrationResponseToUser(response));
        }
      }
    });
  }

  protected void showAskForDeviceRegistrationResponseToUser(AskForDeviceRegistrationResponse response) {
    if (response != null) {
      FXUtils.runOnUiThread(() -> {
        if (response.allowsRegistration())
          Alerts.showDeviceRegistrationSuccessfulAlert(response, stage);
        else
          Alerts.showServerDeniedDeviceRegistrationAlert(response, stage);
      });
    }
  }


  protected void askUserIfRegisteringDeviceIsAllowed(final AskForDeviceRegistrationRequest request) {
    mayHideUnregisteredDeviceFoundAlert(request);

    Platform.runLater(() -> { // after hiding unregisteredDeviceFoundAlert in mayHideUnregisteredDeviceFoundAlert() we have to wait some time before JavaFX is able to show a new Alert
      boolean userAllowsDeviceRegistration = Alerts.showDeviceAsksForRegistrationAlert(request, stage);
      final AskForDeviceRegistrationResponse result;

      if(userAllowsDeviceRegistration == false)
        result = AskForDeviceRegistrationResponse.Deny;
      else {
        result = AskForDeviceRegistrationResponse.createAllowRegistrationResponse(true, Application.getLoggedOnUser(), Application.getApplication().getLocalDevice());
        // TODO: check if user information differ and if so ask which one to use
      }

      sendRespondToAskForDeviceRegistrationRequest(request, result);
    });
  }

  protected void mayHideUnregisteredDeviceFoundAlert(AskForDeviceRegistrationRequest request) {
    if(unregisteredDeviceFoundAlerts.containsKey(request.getDevice().getDeviceId())) {
      Map<String, Alert> userToAlertMap = unregisteredDeviceFoundAlerts.get(request.getDevice().getDeviceId());

      if(userToAlertMap.containsKey(request.getUser().getUniversallyUniqueId())) {
        Alert unregisteredDeviceFoundAlert = userToAlertMap.get(request.getUser().getUniversallyUniqueId());
        unregisteredDeviceFoundAlert.hide();
        unregisteredDeviceFoundAlert.close();
      }
    }
  }

  protected void sendRespondToAskForDeviceRegistrationRequest(final AskForDeviceRegistrationRequest request, final AskForDeviceRegistrationResponse result) {
    Application.getDeepThoughtConnector().getCommunicator().respondToAskForDeviceRegistrationRequest(request, result, new ResponseListener() {
      @Override
      public void responseReceived(Request request1, Response response) {
        if (result.allowsRegistration() && response.getResponseCode() == ResponseCode.Ok) {
          Alerts.showInfoMessage(stage, Localization.getLocalizedString("alert.message.successfully.registered.device", request.getDevice()),
              Localization.getLocalizedString("alert.title.device.registration.successful"));
        }
      }
    });
  }

}

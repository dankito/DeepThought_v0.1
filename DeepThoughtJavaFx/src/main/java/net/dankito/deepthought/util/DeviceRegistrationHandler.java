package net.dankito.deepthought.util;

import net.dankito.deepthought.communication.IDeepThoughtConnector;
import net.dankito.deepthought.communication.messages.request.AskForDeviceRegistrationRequest;
import net.dankito.deepthought.communication.messages.response.AskForDeviceRegistrationResponse;
import net.dankito.deepthought.communication.model.HostInfo;
import net.dankito.deepthought.communication.registration.DeviceRegistrationHandlerBase;
import net.dankito.deepthought.controls.utils.FXUtils;
import net.dankito.deepthought.data.model.DeepThought;
import net.dankito.deepthought.data.model.Device;
import net.dankito.deepthought.data.model.User;
import net.dankito.deepthought.data.persistence.IEntityManager;
import net.dankito.deepthought.data.sync.InitialSyncManager;

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
public class DeviceRegistrationHandler extends DeviceRegistrationHandlerBase {

  protected Stage stage;


  public DeviceRegistrationHandler(Stage stage, IDeepThoughtConnector deepThoughtConnector, IThreadPool threadPool, IEntityManager entityManager,
                                   DeepThought deepThought, User loggedOnUser, Device localDevice) {
    this(stage, deepThoughtConnector, threadPool, new InitialSyncManager(entityManager), deepThought, loggedOnUser, localDevice);
  }

  public DeviceRegistrationHandler(Stage stage, IDeepThoughtConnector deepThoughtConnector, IThreadPool threadPool, InitialSyncManager initialSyncManager,
                                   DeepThought deepThought, User loggedOnUser, Device localDevice) {
    super(deepThoughtConnector, threadPool, initialSyncManager, deepThought, loggedOnUser, localDevice);
    this.stage = stage;
  }


  @Override
  protected void askUserToSyncDataWithDevice(HostInfo device) {
    FXUtils.runOnUiThread(() -> showNotificationAskingUserToSyncDataWithDevice(device)); // Alert has to run on UI thread but listener method for sure is not called on UI thread
  }

  @Override
  protected void deviceIsAskingForRegistration(AskForDeviceRegistrationRequest request) {
    FXUtils.runOnUiThread(() -> askUserIfRegisteringDeviceIsAllowed(request));
  }


  protected Map<String, Map<String, Alert>> unregisteredDeviceFoundAlerts = new ConcurrentHashMap<>();

  protected void showNotificationAskingUserToSyncDataWithDevice(HostInfo device) {
    Alert unregisteredDeviceFoundAlert = Alerts.createAskUserToSyncDataWithDeviceAlert(device, stage);

    if(unregisteredDeviceFoundAlerts.containsKey(device.getDeviceUniqueId()) == false) {
      unregisteredDeviceFoundAlerts.put(device.getDeviceUniqueId(), new ConcurrentHashMap<>());
    }
    unregisteredDeviceFoundAlerts.get(device.getDeviceUniqueId()).put(device.getUserUniqueId(), unregisteredDeviceFoundAlert);


    Optional<ButtonType> result = unregisteredDeviceFoundAlert.showAndWait();
    ButtonType chosenOption = result.get();

    if(chosenOption == ButtonType.NO) {
      addDeviceToListDoNotAskAnymoreToSyncDataWith(device);
    }

    boolean likesToConnectWithDevice = chosenOption == ButtonType.YES;

    Map<String, Alert> alertsForDeviceId = unregisteredDeviceFoundAlerts.get(device.getDeviceUniqueId());
    if(alertsForDeviceId != null) {
      alertsForDeviceId.remove(device.getUserUniqueId());
      if(unregisteredDeviceFoundAlerts.get(device.getDeviceUniqueId()).size() == 0) {
        unregisteredDeviceFoundAlerts.remove(device.getDeviceUniqueId());
      }
    }

    if(likesToConnectWithDevice) {
      askForRegistration(device);
    }
  }

  @Override
  protected void showMessageToReceivedAskForRegistrationResponse(AskForDeviceRegistrationResponse response) {
    showAskForDeviceRegistrationResponseToUser(response);
  }

  protected void showAskForDeviceRegistrationResponseToUser(AskForDeviceRegistrationResponse response) {
    if(response != null) {
      FXUtils.runOnUiThread(() -> {
        if(response.allowsRegistration())
          Alerts.showDeviceRegistrationSuccessfulAlert(response, stage);
        else
          Alerts.showServerDeniedDeviceRegistrationAlert(response, stage);
      });
    }
  }


  protected void askUserIfRegisteringDeviceIsAllowed(final AskForDeviceRegistrationRequest request) {
    mayHideInfoUnregisteredDeviceFound(request);

    Platform.runLater(() -> { // after hiding unregisteredDeviceFoundAlert in mayHideInfoUnregisteredDeviceFound() we have to wait some time before JavaFX is able to show a new Alert
      boolean userAllowsDeviceRegistration = Alerts.showDeviceAsksForRegistrationAlert(request, stage);
      sendAskUserIfRegisteringDeviceIsAllowedResponse(request, userAllowsDeviceRegistration);
    });
  }

  protected void mayHideInfoUnregisteredDeviceFound(AskForDeviceRegistrationRequest request) {
    mayHideInfoUnregisteredDeviceFound(request.getDevice());
  }

  @Override
  protected void mayHideInfoUnregisteredDeviceFound(HostInfo device) {
    FXUtils.runOnUiThread(() -> mayHideInfoUnregisteredDeviceFoundOnMainThread(device) );
  }

  protected void mayHideInfoUnregisteredDeviceFoundOnMainThread(HostInfo device) {
    if(unregisteredDeviceFoundAlerts.containsKey(device.getDeviceUniqueId())) {
      Map<String, Alert> userToAlertMap = unregisteredDeviceFoundAlerts.get(device.getDeviceUniqueId());

      if(userToAlertMap.containsKey(device.getUserUniqueId())) {
        Alert unregisteredDeviceFoundAlert = userToAlertMap.get(device.getUserUniqueId());
        unregisteredDeviceFoundAlert.hide();
        unregisteredDeviceFoundAlert.close();
      }
    }
  }


  protected void showErrorSynchronizingWithDeviceNotPossible(String message, String messageTitle) {
    Alerts.showErrorMessage(stage, message, messageTitle);
  }

  protected void showRegistrationSuccessfulMessage(String message, String messageTitle) {
    Alerts.showInfoMessage(stage, message, messageTitle);
  }

}

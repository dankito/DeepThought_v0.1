package net.dankito.deepthought.dialogs;

import android.content.DialogInterface;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.MainActivity;
import net.dankito.deepthought.helper.AlertHelper;
import net.dankito.deepthought.R;
import net.dankito.deepthought.communication.IDeepThoughtConnector;
import net.dankito.deepthought.communication.listener.AskForDeviceRegistrationResultListener;
import net.dankito.deepthought.communication.listener.ResponseListener;
import net.dankito.deepthought.communication.messages.request.AskForDeviceRegistrationRequest;
import net.dankito.deepthought.communication.messages.request.Request;
import net.dankito.deepthought.communication.messages.response.AskForDeviceRegistrationResponse;
import net.dankito.deepthought.communication.messages.response.Response;
import net.dankito.deepthought.communication.messages.response.ResponseCode;
import net.dankito.deepthought.communication.model.HostInfo;
import net.dankito.deepthought.communication.registration.IUnregisteredDevicesListener;
import net.dankito.deepthought.util.StringUtils;
import net.dankito.deepthought.util.localization.Localization;

/**
 * Created by ganymed on 07/06/16.
 */
public class DeviceRegistrationHandler {

  protected MainActivity mainActivity;

  protected Snackbar snackbarAskRegisterUnknownDevice = null;

  protected String deviceIdShowingSnackbarFor = null;


  public DeviceRegistrationHandler(MainActivity mainActivity, IDeepThoughtConnector deepThoughtConnector) {
    this.mainActivity = mainActivity;
    deepThoughtConnector.addUnregisteredDevicesListener(unregisteredDevicesListener);
  }

  protected IUnregisteredDevicesListener unregisteredDevicesListener = new IUnregisteredDevicesListener() {
    @Override
    public void unregisteredDeviceFound(final HostInfo device) {
      mainActivity.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          notifyUnregisteredDeviceFound(device);
        }
      });
    }

    @Override
    public void deviceIsAskingForRegistration(final AskForDeviceRegistrationRequest request) {
      mainActivity.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          notifyDeviceIsAskingForRegistration(request);
        }
      });
    }
  };

  protected void notifyUnregisteredDeviceFound(final HostInfo device) {
    // TODO: may always show Snackbar in active Activity, see http://stackoverflow.com/a/29786451/119733
    View rootView = mainActivity.findViewById(R.id.pager); // has to be Pager otherwise Snackbar cannot be dismissed anymore by User
    snackbarAskRegisterUnknownDevice = Snackbar.make(rootView, "", Snackbar.LENGTH_INDEFINITE);
    deviceIdShowingSnackbarFor = device.getDeviceId();

    snackbarAskRegisterUnknownDevice.setCallback(new Snackbar.Callback() {
      @Override
      public void onDismissed(Snackbar snackbar, int event) {
        resetSnackbar();
      }
    });

    snackbarAskRegisterUnknownDevice.setAction(R.string.ok, new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        askForRegistration(device);
      }
    });

    // Warning: Actually a Snackbar's Design should not be manipulated:
    // "Don't customize the Snackbar. It should not contain any more elements than a short text and one action. See Google Material design guidelines."
    // Code found at: http://stackoverflow.com/questions/32453946/how-to-customize-snackbars-layout
    customizeSnackbar(device, rootView.getWidth());

    snackbarAskRegisterUnknownDevice.show();
  }

  protected void customizeSnackbar(HostInfo device, int windowWidth) {
    Snackbar.SnackbarLayout layout = (Snackbar.SnackbarLayout) snackbarAskRegisterUnknownDevice.getView();

    TextView txtvwSnackbarStandardText = (TextView) layout.findViewById(android.support.design.R.id.snackbar_text);
    txtvwSnackbarStandardText.setVisibility(View.INVISIBLE);

    // Inflate our custom view
    View snackView = mainActivity.getLayoutInflater().inflate(R.layout.snackbar_unregistered_device_found, null);

// Configure the view
    ImageView imageView = (ImageView) snackView.findViewById(R.id.imgvwDeviceIcon);
    imageView.setImageResource(getOsLogoId(device));

    TextView txtvwDeviceInfo = (TextView) snackView.findViewById(R.id.txtvwDeviceInfo);
    txtvwDeviceInfo.setText(device.getDeviceInfoString());
    txtvwDeviceInfo.setTextColor(txtvwSnackbarStandardText.getCurrentTextColor());
    imageView.setImageResource(getOsLogoId(device));

    TextView txtvwDeviceAddress = (TextView) snackView.findViewById(R.id.txtvwDeviceAddress);
    txtvwDeviceAddress.setText(device.getAddress());
    txtvwDeviceAddress.setTextColor(txtvwSnackbarStandardText.getCurrentTextColor());

    TextView txtvwAskToYouWantToConnectTo = (TextView) snackView.findViewById(R.id.txtvwAskToYouWantToConnectTo);
    txtvwAskToYouWantToConnectTo.setTextColor(txtvwSnackbarStandardText.getCurrentTextColor());

    CheckBox chkbxNeverAskAgainToConnectWithThisDevice = (CheckBox) snackView.findViewById(R.id.chkbxNeverAskAgainToConnectWithThisDevice);
    chkbxNeverAskAgainToConnectWithThisDevice.setTextColor(txtvwSnackbarStandardText.getCurrentTextColor());

//// Add the view to the Snackbar's layout
    layout.addView(snackView, 0);
  }

  protected void resetSnackbar() {
    snackbarAskRegisterUnknownDevice = null;
    deviceIdShowingSnackbarFor = null;
  }

  protected int getOsLogoId(HostInfo device) {
    String platform = device.getPlatform().toLowerCase();

    if(platform.contains("android"))
      return R.drawable.android_logo;
    else if(platform.contains("linux"))
      return R.drawable.linux_logo;
    else if(platform.contains("windows"))
      return R.drawable.windows_logo;
    else if(platform.contains("mac"))
      return R.drawable.apple_logo;
    else if(platform.contains("solaris"))
      return R.drawable.sun_solaris_logo;

    return 0; // TODO: create a placeholder logo
  }

  protected void askForRegistration(HostInfo device) {
    Application.getDeepThoughtConnector().getCommunicator().askForDeviceRegistration(device, Application.getLoggedOnUser(), Application.getApplication().getLocalDevice(), new AskForDeviceRegistrationResultListener() {
      @Override
      public void responseReceived(AskForDeviceRegistrationRequest request, final AskForDeviceRegistrationResponse response) {
        if (response != null) {
          mainActivity.runOnUiThread(new Runnable() { // listener is for sure not executed on UI thread
            @Override
            public void run() {
              showAskForDeviceRegistrationResponseToUser(response);
            }
          });
        }
      }
    });
  }

  protected void showAskForDeviceRegistrationResponseToUser(AskForDeviceRegistrationResponse response) {
    if (response.allowsRegistration())
      AlertHelper.showInfoMessage(mainActivity, R.string.device_registration_server_allowed_registration);
    else
      AlertHelper.showInfoMessage(mainActivity, R.string.device_registration_server_denied_registration);
  }


  protected void notifyDeviceIsAskingForRegistration(AskForDeviceRegistrationRequest request) {
    mayHideSnackbarForDevice(request);

    askUserIfRegisteringDeviceIsAllowed(request);
  }

  protected void mayHideSnackbarForDevice(AskForDeviceRegistrationRequest request) {
    if(snackbarAskRegisterUnknownDevice != null && deviceIdShowingSnackbarFor != null && deviceIdShowingSnackbarFor.equals(request.getDevice().getDeviceId())) {
      snackbarAskRegisterUnknownDevice.dismiss();
      resetSnackbar();
    }
  }


  protected void askUserIfRegisteringDeviceIsAllowed(final AskForDeviceRegistrationRequest request) {
    AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
    builder = builder.setTitle(Localization.getLocalizedString("alert.title.ask.for.device.registration"));

    HostInfo device = request.getDevice();

    String message = Localization.getLocalizedString("alert.message.ask.for.device.registration", device);
    if(StringUtils.isNotNullOrEmpty(request.getUser().getUserInfoString())) {
      message += Localization.getLocalizedString("user.info", request.getUser().getUserInfoString());
    }

    message += Localization.getLocalizedString("device.info", device.getDeviceInfoString());
    message += Localization.getLocalizedString("ip.address", device.getAddress());

    builder = builder.setMessage(message);

    builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        sendAskUserIfRegisteringDeviceIsAllowedResponse(request, true);
      }
    });

    builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        sendAskUserIfRegisteringDeviceIsAllowedResponse(request, false);
      }
    });

    builder.create().show();
  }

  protected void sendAskUserIfRegisteringDeviceIsAllowedResponse(final AskForDeviceRegistrationRequest request, boolean userAllowsDeviceRegistration) {
    final AskForDeviceRegistrationResponse result;

    if(userAllowsDeviceRegistration == false)
      result = AskForDeviceRegistrationResponse.Deny;
    else {
      result = AskForDeviceRegistrationResponse.createAllowRegistrationResponse(true, Application.getLoggedOnUser(), Application.getApplication().getLocalDevice());
      // TODO: check if user information differ and if so ask which one to use
    }

    Application.getDeepThoughtConnector().getCommunicator().respondToAskForDeviceRegistrationRequest(request, result, new ResponseListener() {
      @Override
      public void responseReceived(Request request1, Response response) {
        if (result.allowsRegistration() && response.getResponseCode() == ResponseCode.Ok) {
          AlertHelper.showInfoMessage(mainActivity, mainActivity.getString(R.string.device_registration_successfully_registered_device, request.getDevice()));
        }
      }
    });
  }

}

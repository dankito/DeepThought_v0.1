package net.dankito.deepthought.dialogs;

import android.content.DialogInterface;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import net.dankito.deepthought.MainActivity;
import net.dankito.deepthought.R;
import net.dankito.deepthought.communication.IDeepThoughtConnector;
import net.dankito.deepthought.communication.messages.request.AskForDeviceRegistrationRequest;
import net.dankito.deepthought.communication.messages.response.AskForDeviceRegistrationResponse;
import net.dankito.deepthought.communication.model.HostInfo;
import net.dankito.deepthought.communication.registration.DeviceRegistrationHandlerBase;
import net.dankito.deepthought.data.model.DeepThought;
import net.dankito.deepthought.data.model.Device;
import net.dankito.deepthought.data.model.User;
import net.dankito.deepthought.data.persistence.IEntityManager;
import net.dankito.deepthought.data.sync.InitialSyncManager;
import net.dankito.deepthought.helper.AlertHelper;
import net.dankito.deepthought.util.IThreadPool;
import net.dankito.deepthought.util.StringUtils;
import net.dankito.deepthought.util.localization.Localization;

/**
 * Created by ganymed on 07/06/16.
 */
public class DeviceRegistrationHandler extends DeviceRegistrationHandlerBase {

  protected MainActivity mainActivity;

  protected Snackbar snackbarAskRegisterUnknownDevice = null;

  protected String deviceIdShowingSnackbarFor = null;


  public DeviceRegistrationHandler(MainActivity mainActivity, IDeepThoughtConnector deepThoughtConnector, IThreadPool threadPool, IEntityManager entityManager,
                                   DeepThought deepThought, User loggedOnUser, Device localDevice) {
    this(mainActivity, deepThoughtConnector, threadPool, new InitialSyncManager(entityManager), deepThought, loggedOnUser, localDevice);
  }

  public DeviceRegistrationHandler(MainActivity mainActivity, IDeepThoughtConnector deepThoughtConnector, IThreadPool threadPool, InitialSyncManager initialSyncManager,
                                   DeepThought deepThought, User loggedOnUser, Device localDevice) {
    super(deepThoughtConnector, threadPool, initialSyncManager, deepThought, loggedOnUser, localDevice);
    this.mainActivity = mainActivity;
  }


  @Override
  protected void unregisteredDeviceFound(final HostInfo device) {
    mainActivity.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        notifyUnregisteredDeviceFound(device);
      }
    });
  }

  @Override
  protected void deviceIsAskingForRegistration(final AskForDeviceRegistrationRequest request) {
    mainActivity.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        notifyDeviceIsAskingForRegistration(request);
      }
    });
  }

  @Override
  protected void showMessageToReceivedAskForRegistrationResponse(final AskForDeviceRegistrationResponse response) {
    mainActivity.runOnUiThread(new Runnable() { // listener is for sure not executed on UI thread
      @Override
      public void run() {
        showAskForDeviceRegistrationResponseToUser(response);
      }
    });
  }

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


  protected void showAskForDeviceRegistrationResponseToUser(AskForDeviceRegistrationResponse response) {
    if (response.allowsRegistration())
      AlertHelper.showInfoMessage(mainActivity, R.string.device_registration_server_allowed_registration);
    else
      AlertHelper.showInfoMessage(mainActivity, R.string.device_registration_server_denied_registration);
  }


  protected void notifyDeviceIsAskingForRegistration(AskForDeviceRegistrationRequest request) {
    mayHideInfoUnregisteredDeviceFoundOnMainThread(request);

    askUserIfRegisteringDeviceIsAllowed(request);
  }

  @Override
  protected void mayHideInfoUnregisteredDeviceFound(final HostInfo device) {
    mainActivity.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        mayHideInfoUnregisteredDeviceFoundOnMainThread(device);
      }
    });
  }

  protected void mayHideInfoUnregisteredDeviceFoundOnMainThread(AskForDeviceRegistrationRequest request) {
    mayHideInfoUnregisteredDeviceFoundOnMainThread(request.getDevice());
  }

  protected void mayHideInfoUnregisteredDeviceFoundOnMainThread(HostInfo device) {
    if(snackbarAskRegisterUnknownDevice != null && deviceIdShowingSnackbarFor != null && deviceIdShowingSnackbarFor.equals(device.getDeviceId())) {
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


  protected void showErrorSynchronizingWithDeviceNotPossible(String message, String messageTitle) {
    AlertHelper.showErrorMessage(mainActivity, message, messageTitle);
  }

  protected void showRegistrationSuccessfulMessage(String message, String messageTitle) {
    AlertHelper.showInfoMessage(mainActivity, message, messageTitle);
  }

}

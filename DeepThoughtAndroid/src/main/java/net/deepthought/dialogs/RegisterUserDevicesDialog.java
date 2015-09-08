package net.deepthought.dialogs;

import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;

import net.deepthought.Application;
import net.deepthought.R;
import net.deepthought.adapter.DeviceRegistrationDevicesAdapter;
import net.deepthought.communication.listener.ResponseListener;
import net.deepthought.communication.messages.AskForDeviceRegistrationRequest;
import net.deepthought.communication.messages.AskForDeviceRegistrationResponseMessage;
import net.deepthought.communication.messages.Request;
import net.deepthought.communication.messages.Response;
import net.deepthought.communication.messages.ResponseValue;
import net.deepthought.communication.model.DeviceInfo;
import net.deepthought.communication.model.HostInfo;
import net.deepthought.communication.model.UserInfo;
import net.deepthought.communication.registration.RegistrationRequestListener;
import net.deepthought.communication.registration.UserDeviceRegistrationRequestListener;
import net.deepthought.helper.AlertHelper;
import net.deepthought.util.Localization;
import net.deepthought.util.StringUtils;

public class RegisterUserDevicesDialog extends DialogFragment {

  public final static String TAG = RegisterUserDevicesDialog.class.getSimpleName();


  protected boolean isStarted = false;

  protected Spinner spnDeviceRegistrationOptions;

  protected ListView lstvwRegisteredDevices;
  protected DeviceRegistrationDevicesAdapter devicesAdapter;

  protected Button btnStartStop;


  public RegisterUserDevicesDialog() {
    // Required empty public constructor
  }


  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.dialog_register_user_devices, container, false);
    setupControls(view);

    getDialog().setTitle(Localization.getLocalizedString("device.registration"));

    return view;
  }

  protected void setupControls(View view) {
    spnDeviceRegistrationOptions = (Spinner)view.findViewById(R.id.spnDeviceRegistrationOptions);
    String[] options = { Localization.getLocalizedString("open.registration.server"), Localization.getLocalizedString("open.registration.server")
                         /*, Localization.getLocalizedString("show.registered.devices")*/ };
    spnDeviceRegistrationOptions.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_dropdown_item_1line, options));
    spnDeviceRegistrationOptions.setOnItemSelectedListener(spnDeviceRegistrationOptionsItemSelectedListener);

    lstvwRegisteredDevices = (ListView)view.findViewById(R.id.lstvwRegisteredDevices);

    devicesAdapter = new DeviceRegistrationDevicesAdapter(getActivity());
    lstvwRegisteredDevices.setAdapter(devicesAdapter);

    btnStartStop = (Button)view.findViewById(R.id.btnStartStop);
    btnStartStop.setOnClickListener(btnStartStopOnClickListener);

    Button btnClose = (Button)view.findViewById(R.id.btnClose);
    btnClose.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        dismiss();
      }
    });
  }

  @Override
  public void onDismiss(DialogInterface dialog) {
    stop();
    super.onDismiss(dialog);
  }

  protected AdapterView.OnItemSelectedListener spnDeviceRegistrationOptionsItemSelectedListener = new AdapterView.OnItemSelectedListener() {
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
      btnStartStop.setEnabled(i == 0 || i == 1);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
  };


  protected View.OnClickListener btnStartStopOnClickListener = new View.OnClickListener() {
    @Override
    public void onClick(View view) {
      if(isStarted == false) {
        startSelectedOption();
      }
      else {
        stop();
      }
    }
  };

  protected void startSelectedOption() {
    spnDeviceRegistrationOptions.setEnabled(false);

    if(spnDeviceRegistrationOptions.getSelectedItemPosition() == 0) {
      Application.getDeepThoughtsConnector().openUserDeviceRegistrationServer(userDeviceRegistrationRequestListener);
    }
    else if(spnDeviceRegistrationOptions.getSelectedItemPosition() == 1)  {
      Application.getDeepThoughtsConnector().findOtherUserDevicesToRegisterAtAsync(registrationRequestListener);
    }

    isStarted = true;
    btnStartStop.setText(getString(R.string.stop));
  }

  protected void stop() {
    if(spnDeviceRegistrationOptions.getSelectedItemPosition() == 0) {
      Application.getDeepThoughtsConnector().closeUserDeviceRegistrationServer();
    }
    else {
      Application.getDeepThoughtsConnector().stopSearchingOtherUserDevicesToRegisterAt();
    }

    devicesAdapter.clearItems();

    spnDeviceRegistrationOptions.setEnabled(true);

    isStarted = false;
    btnStartStop.setText(getString(R.string.start));
  }


  protected void askUserIfRegisteringDeviceIsAllowed(final AskForDeviceRegistrationRequest request) {
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    builder = builder.setTitle(Localization.getLocalizedString("alert.title.ask.for.device.registration"));
    String message = Localization.getLocalizedString("alert.message.ask.for.device.registration", extractUserInfoString(request.getUser()),
        extractDeviceInfoString(request.getDevice()), request.getAddress());
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
    final AskForDeviceRegistrationResponseMessage result;

    if(userAllowsDeviceRegistration == false)
      result = AskForDeviceRegistrationResponseMessage.Deny;
    else {
      result = AskForDeviceRegistrationResponseMessage.createAllowRegistrationResponse(true, Application.getLoggedOnUser(), Application.getApplication().getLocalDevice());
      // TODO: check if user information differ and if so ask which one to use
    }

    Application.getDeepThoughtsConnector().getCommunicator().sendAskForDeviceRegistrationResponse(request, result, new ResponseListener() {
      @Override
      public void responseReceived(Request request1, Response response) {
        if (result.allowsRegistration() && response.getResponseValue() == ResponseValue.Ok) {
          AlertHelper.showInfoMessage(getActivity(), getActivity().getString(R.string.device_registration_successfully_registered_device, request.getDevice()));
        }
      }
    });
  }

  protected String extractUserInfoString(UserInfo user) {
    String userInfo = user.getUserName();

    if(StringUtils.isNotNullOrEmpty(user.getFirstName()) || StringUtils.isNotNullOrEmpty(user.getLastName()))
      userInfo += " (" + user.getFirstName() + " " + user.getLastName() + ")";

    return userInfo;
  }

  protected static String extractDeviceInfoString(DeviceInfo device) {
    String deviceInfo = device.getPlatform() + " " + device.getOsVersion();
    return deviceInfo;
  }


  protected UserDeviceRegistrationRequestListener userDeviceRegistrationRequestListener = new UserDeviceRegistrationRequestListener() {
    @Override
    public void registerDeviceRequestRetrieved(final AskForDeviceRegistrationRequest request) {
      getActivity().runOnUiThread(new Runnable() {
        @Override
        public void run() {
          askUserIfRegisteringDeviceIsAllowed(request);
        }
      });
    }
  };

  protected RegistrationRequestListener registrationRequestListener = new RegistrationRequestListener() {
    @Override
    public void openRegistrationServerFound(HostInfo hostInfo) {
      devicesAdapter.serverFound(hostInfo);
    }
  };


}

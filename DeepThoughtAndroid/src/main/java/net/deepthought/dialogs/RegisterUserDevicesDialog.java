package net.deepthought.dialogs;

import android.app.DialogFragment;
import android.os.Bundle;
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
import net.deepthought.communication.messages.Response;
import net.deepthought.communication.messages.ResponseValue;
import net.deepthought.communication.model.HostInfo;
import net.deepthought.communication.registration.RegistrationRequestListener;
import net.deepthought.communication.registration.UserDeviceRegistrationRequestListener;
import net.deepthought.helper.AlertHelper;

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

    getDialog().setTitle(R.string.device_registration);

    return view;
  }

  protected void setupControls(View view) {
    spnDeviceRegistrationOptions = (Spinner)view.findViewById(R.id.spnDeviceRegistrationOptions);
    String[] options = { getString(R.string.option_device_registration_open_server), getString(R.string.option_device_search_registration_servers) };
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
        stop();
        dismiss();
      }
    });
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
    // TODO: implement: ask user if he/she allows registration
//    boolean userAllowsDeviceRegistration = Alerts.showDeviceAsksForRegistrationAlert(request, windowStage);
    boolean userAllowsDeviceRegistration = false;
    final AskForDeviceRegistrationResponseMessage result;

    if(userAllowsDeviceRegistration == false)
      result = AskForDeviceRegistrationResponseMessage.Deny;
    else {
      result = AskForDeviceRegistrationResponseMessage.createAllowRegistrationResponse(true, Application.getLoggedOnUser(), Application.getApplication().getLocalDevice());
      // TODO: check if user information differ and if so ask which one to use
    }

    Application.getDeepThoughtsConnector().getCommunicator().sendAskForDeviceRegistrationResponse(request, result, new ResponseListener() {
      @Override
      public void responseReceived(Response response) {
        if(result.allowsRegistration() && response.getResponseValue() == ResponseValue.Ok) {
          AlertHelper.showInfoMessage(getActivity(), getActivity().getString(R.string.device_registration_successfully_registered_device, request.getDevice()));
        }
      }
    });
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

package net.deepthought.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import net.deepthought.Application;
import net.deepthought.R;
import net.deepthought.communication.listener.AskForDeviceRegistrationResultListener;
import net.deepthought.communication.messages.request.AskForDeviceRegistrationRequest;
import net.deepthought.communication.messages.response.AskForDeviceRegistrationResponse;
import net.deepthought.communication.model.HostInfo;
import net.deepthought.helper.AlertHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ganymed on 22/08/15.
 */
public class DeviceRegistrationDevicesAdapter extends BaseAdapter {

  protected Activity context;

  protected List<HostInfo> foundHosts = new ArrayList<>();


  public DeviceRegistrationDevicesAdapter(Activity context) {
    this.context = context;
  }


  @Override
  public int getCount() {
    return foundHosts.size();
  }

  @Override
  public Object getItem(int i) {
    return foundHosts.get(i);
  }

  @Override
  public long getItemId(int i) {
    return i;
  }

  @Override
  public View getView(int i, View convertView, ViewGroup viewGroup) {
    if(convertView == null)
      convertView = context.getLayoutInflater().inflate(R.layout.list_item_device, viewGroup, false);

    HostInfo serverInfo = (HostInfo)getItem(i);

    int osLogoId = getOsLogoId(serverInfo);
    if(osLogoId > 0) {
      ImageView imgvwOsLogo = (ImageView) convertView.findViewById(R.id.imgvwOsLogo);
      imgvwOsLogo.setImageResource(osLogoId);
    }

    TextView txtvwUserInfo = (TextView)convertView.findViewById(R.id.txtvwUserInfo);
    txtvwUserInfo.setText(serverInfo.getUserName());

    TextView txtvwDeviceIpAddress = (TextView)convertView.findViewById(R.id.txtvwDeviceIpAddress);
    txtvwDeviceIpAddress.setText(serverInfo.getIpAddress());

    TextView txtvwDeviceInfo = (TextView)convertView.findViewById(R.id.txtvwDeviceInfo);
    String infoString = serverInfo.getPlatform() + " " + serverInfo.getOsVersion();
    if(serverInfo.getPlatform() != null && serverInfo.getPlatform().toLowerCase().contains("android")) {
      infoString = serverInfo.getDeviceName() + " (" + infoString + ")";
    }
    txtvwDeviceInfo.setText(infoString);

    Button btnAskForRegistration = (Button)convertView.findViewById(R.id.btnAskForRegistration);
    btnAskForRegistration.setOnClickListener(btnAskForRegistrationOnClickListener);
    btnAskForRegistration.setTag(serverInfo);

    return convertView;
  }

  protected int getOsLogoId(HostInfo serverInfo) {
    String platform = serverInfo.getPlatform().toLowerCase();

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


  public void serverFound(HostInfo hostInfo) {
    foundHosts.add(hostInfo);
    notifyDataSetChangedThreadSafe();
  }

  public void clearItems() {
    foundHosts.clear();
    notifyDataSetChangedThreadSafe();
  }

  protected void notifyDataSetChangedThreadSafe() {
    context.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        notifyDataSetChanged();
      }
    });
  }


  protected View.OnClickListener btnAskForRegistrationOnClickListener = new View.OnClickListener() {
    @Override
    public void onClick(View view) {
      HostInfo serverInfo = (HostInfo) view.getTag();

      Application.getDeepThoughtConnector().getCommunicator().askForDeviceRegistration(serverInfo, Application.getLoggedOnUser(), Application.getApplication().getLocalDevice(), new AskForDeviceRegistrationResultListener() {
        @Override
        public void responseReceived(AskForDeviceRegistrationRequest request, final AskForDeviceRegistrationResponse response) {
          if (response != null) {
            context.runOnUiThread(new Runnable() { // listener is for sure not executed on UI thread
              @Override
              public void run() {
                showAskForDeviceRegistrationResponseToUser(response);
              }
            });
          }
        }
      });
    }
  };

  protected void showAskForDeviceRegistrationResponseToUser(AskForDeviceRegistrationResponse response) {
    if (response.allowsRegistration())
      AlertHelper.showInfoMessage(context, R.string.device_registration_server_allowed_registration);
    else
      AlertHelper.showInfoMessage(context, R.string.device_registration_server_denied_registration);
  }
}

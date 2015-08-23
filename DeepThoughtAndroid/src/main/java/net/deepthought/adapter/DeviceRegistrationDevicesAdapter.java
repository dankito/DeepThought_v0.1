package net.deepthought.adapter;

import android.app.Activity;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import net.deepthought.Application;
import net.deepthought.R;
import net.deepthought.communication.listener.AskForDeviceRegistrationListener;
import net.deepthought.communication.messages.AskForDeviceRegistrationResponse;
import net.deepthought.communication.model.HostInfo;

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
    txtvwDeviceInfo.setText(serverInfo.getPlatform() + " " + serverInfo.getOsVersion());

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

      Application.getDeepThoughtsConnector().getCommunicator().askForDeviceRegistration(serverInfo, new AskForDeviceRegistrationListener() {
        @Override
        public void serverResponded(final AskForDeviceRegistrationResponse response) {
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
    AlertDialog.Builder builder = new AlertDialog.Builder(context);
    if (response.allowsRegistration())
      builder = builder.setMessage(R.string.device_registration_server_allowed_registration);
    else
      builder = builder.setMessage(R.string.device_registration_server_denied_registration);

    builder.setNegativeButton(R.string.ok, null);

    builder.create().show();
  }
}

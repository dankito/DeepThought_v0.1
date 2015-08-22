package net.deepthought.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import net.deepthought.R;
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

    // TODO: set OS logo
    ImageView imgvwOsLogo = (ImageView)convertView.findViewById(R.id.imgvwOsLogo);

    TextView txtvwUserInfo = (TextView)convertView.findViewById(R.id.txtvwUserInfo);
    txtvwUserInfo.setText(serverInfo.getUserName());

    TextView txtvwDeviceIpAddress = (TextView)convertView.findViewById(R.id.txtvwDeviceIpAddress);
    txtvwDeviceIpAddress.setText(serverInfo.getIpAddress());

    TextView txtvwDeviceInfo = (TextView)convertView.findViewById(R.id.txtvwDeviceInfo);
    txtvwDeviceInfo.setText(serverInfo.getPlatform() + " " + serverInfo.getOsVersion());

    Button btnAskForRegistration = (Button)convertView.findViewById(R.id.btnAskForRegistration);
    btnAskForRegistration.setOnClickListener(btnAskForRegistrationOnClickListener);

    return convertView;
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
      // TODO: implement
//      Application.getDeepThoughtsConnector().getCommunicator().askForDeviceRegistration(, new AskForDeviceRegistrationListener() {
//        @Override
//        public void serverResponded(AskForDeviceRegistrationResponse response) {
//
//        }
//      });
    }
  };
}

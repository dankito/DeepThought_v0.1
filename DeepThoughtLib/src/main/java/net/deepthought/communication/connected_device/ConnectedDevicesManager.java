package net.deepthought.communication.connected_device;

import net.deepthought.Application;
import net.deepthought.communication.model.ConnectedDevice;
import net.deepthought.communication.model.HostInfo;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by ganymed on 22/08/15.
 */
public class ConnectedDevicesManager {

  protected List<ConnectedDevice> connectedDevices = new CopyOnWriteArrayList<>();


  public boolean connectedToDevice(ConnectedDevice device) {
    if(connectedDevices.add(device)) {
      // TODO: call listener
      return true;
    }

    return false;
  }


  public boolean isConnectedToDevice(HostInfo hostInfo) {
    for(ConnectedDevice device : connectedDevices) {
      if(device.getUniqueDeviceId().equals(hostInfo.getDeviceUniqueId()) && hostInfo.getUserUniqueId().equals(Application.getLoggedOnUser().getUniversallyUniqueId()))
        return true;
    }

    return false;
  }
}

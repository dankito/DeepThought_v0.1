package net.dankito.deepthought.communication.connected_device;

import net.dankito.deepthought.communication.model.HostInfo;
import net.dankito.deepthought.Application;
import net.dankito.deepthought.communication.model.ConnectedDevice;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by ganymed on 22/08/15.
 */
public class ConnectedDevicesManager implements IConnectedDevicesManager {

  protected List<ConnectedDevice> connectedDevices = new CopyOnWriteArrayList<>();


  @Override
  public boolean connectedToDevice(ConnectedDevice device) {
    if(containsDevice(device) == false) {
      return connectedDevices.add(device);
    }

    return false;
  }

  @Override
  public boolean disconnectedFromDevice(ConnectedDevice device) {
    if(containsDevice(device)) {
      return connectedDevices.remove(device);
    }

    return false;
  }

  @Override
  public boolean containsDevice(ConnectedDevice device) {
    for(ConnectedDevice connectedDevice : connectedDevices) {
      if(connectedDevice.getDeviceId().equals(device.getDeviceId()) && connectedDevice.getAddress().equals(device.getAddress())) // TODO: check if it's also the same user
        return true;
    }

    return false;
  }


  @Override
  public boolean isConnectedToDevice(HostInfo hostInfo) {
    return getConnectedDeviceForHostInfo(hostInfo) != null;
  }

  @Override
  public ConnectedDevice getConnectedDeviceForHostInfo(HostInfo hostInfo) {
    for(ConnectedDevice device : connectedDevices) {
      if(device.getDeviceId().equals(hostInfo.getDeviceId()) && hostInfo.getUserUniqueId().equals(Application.getLoggedOnUser().getUniversallyUniqueId()))
        return device;
    }

    return null;
  }


  @Override
  public boolean areDevicesConnected() {
    return getConnectedDevicesCount() > 0;
  }

  @Override
  public int getConnectedDevicesCount() {
    return connectedDevices.size();
  }

  @Override
  public List<ConnectedDevice> getConnectedDevices() {
    return connectedDevices;
  }

}

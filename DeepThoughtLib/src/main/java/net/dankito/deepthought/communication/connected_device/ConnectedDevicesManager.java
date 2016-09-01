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
    for(ConnectedDevice connectedDevice : connectedDevices) {
      if(doConnectedDeviceInstancesEqual(device, connectedDevice)) {
        return false; // we're already containing an instance of this remote device
      }
    }

    return connectedDevices.add(device);
  }

  @Override
  public boolean disconnectedFromDevice(ConnectedDevice device) {
    for(ConnectedDevice connectedDevice : connectedDevices) {
      if(doConnectedDeviceInstancesEqual(device, connectedDevice)) {
        return connectedDevices.remove(device);
      }
    }

    return false;
  }

  @Override
  public boolean containsDevice(ConnectedDevice device) {
    for(ConnectedDevice connectedDevice : connectedDevices) {
      if(doConnectedDeviceInstancesEqual(device, connectedDevice)) {
        return true;
      }
    }

    return false;
  }

  protected boolean doConnectedDeviceInstancesEqual(ConnectedDevice storedDevice, ConnectedDevice otherDeviceInstance) {
    return otherDeviceInstance.getDeviceId().equals(storedDevice.getDeviceId()) && otherDeviceInstance.getAddress().equals(storedDevice.getAddress()); // TODO: check if it's also the same user
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

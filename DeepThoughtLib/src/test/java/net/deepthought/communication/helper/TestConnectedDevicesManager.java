package net.deepthought.communication.helper;

import net.deepthought.communication.connected_device.IConnectedDevicesManager;
import net.deepthought.communication.model.ConnectedDevice;
import net.deepthought.communication.model.HostInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ganymed on 22/11/15.
 */
public class TestConnectedDevicesManager implements IConnectedDevicesManager {

  protected List<ConnectedDevice> connectedDevices = new ArrayList<>();


  @Override
  public boolean connectedToDevice(ConnectedDevice device) {
    return connectedDevices.add(device);
  }

  @Override
  public boolean disconnectedFromDevice(ConnectedDevice device) {
    return connectedDevices.remove(device);
  }

  @Override
  public boolean containsDevice(ConnectedDevice device) {
    return connectedDevices.contains(device);
  }

  @Override
  public boolean isConnectedToDevice(HostInfo hostInfo) {
    for(ConnectedDevice connectedDevice : connectedDevices) {
      if(connectedDevice.getAddress().equals(hostInfo.getIpAddress()) && connectedDevice.getMessagesPort() == hostInfo.getPort() && connectedDevice.getUniqueDeviceId().equals
          (hostInfo.getDeviceId())) {
        return true;
      }
    }

    return false;
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

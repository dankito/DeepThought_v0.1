package net.dankito.deepthought.communication.connected_device;

import net.dankito.deepthought.communication.model.ConnectedDevice;
import net.dankito.deepthought.communication.model.HostInfo;

import java.util.List;

/**
 * Created by ganymed on 22/11/15.
 */
public interface IConnectedDevicesManager {
  boolean connectedToDevice(ConnectedDevice device);

  boolean disconnectedFromDevice(ConnectedDevice device);

  boolean containsDevice(ConnectedDevice device);

  boolean isConnectedToDevice(HostInfo hostInfo);
  ConnectedDevice getConnectedDeviceForHostInfo(HostInfo hostInfo);

  boolean areDevicesConnected();

  int getConnectedDevicesCount();

  List<ConnectedDevice> getConnectedDevices();
}

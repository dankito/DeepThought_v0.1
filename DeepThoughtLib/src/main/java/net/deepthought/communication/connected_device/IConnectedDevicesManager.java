package net.deepthought.communication.connected_device;

import net.deepthought.communication.model.ConnectedDevice;
import net.deepthought.communication.model.HostInfo;

import java.util.List;

/**
 * Created by ganymed on 22/11/15.
 */
public interface IConnectedDevicesManager {
  boolean connectedToDevice(ConnectedDevice device);

  boolean disconnectedFromDevice(ConnectedDevice device);

  boolean containsDevice(ConnectedDevice device);

  boolean isConnectedToDevice(HostInfo hostInfo);

  boolean areDevicesConnected();

  int getConnectedDevicesCount();

  List<ConnectedDevice> getConnectedDevices();
}

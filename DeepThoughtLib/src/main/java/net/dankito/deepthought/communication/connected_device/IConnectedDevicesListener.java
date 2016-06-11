package net.dankito.deepthought.communication.connected_device;

import net.dankito.deepthought.communication.model.ConnectedDevice;

/**
 * Created by ganymed on 20/08/15.
 */
public interface IConnectedDevicesListener {

  void registeredDeviceConnected(ConnectedDevice device);

  void registeredDeviceDisconnected(ConnectedDevice device);

}

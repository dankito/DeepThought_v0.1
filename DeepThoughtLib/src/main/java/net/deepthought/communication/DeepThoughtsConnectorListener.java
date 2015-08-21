package net.deepthought.communication;

import net.deepthought.communication.model.ConnectedDevice;

/**
 * Created by ganymed on 20/08/15.
 */
public interface DeepThoughtsConnectorListener {

  void registeredDeviceConnected(ConnectedDevice device);

  void registeredDeviceDisconnected(ConnectedDevice device);

}

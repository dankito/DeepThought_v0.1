package net.deepthought.communication.listener;

import net.deepthought.communication.model.ConnectedDevice;

/**
 * Created by ganymed on 22/08/15.
 */
public interface RegisteredDeviceDisconnectedListener {

  void registeredDeviceDisconnected(ConnectedDevice device);

}

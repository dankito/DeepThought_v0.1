package net.dankito.deepthought.communication.connected_device;

/**
 * Created by ganymed on 05/09/16.
 */
public interface IConnectedRegisteredDevicesListenerManager {

  boolean addConnectedDevicesListener(ConnectedRegisteredDevicesListener listener);

  boolean removeConnectedDevicesListener(ConnectedRegisteredDevicesListener listener);

}

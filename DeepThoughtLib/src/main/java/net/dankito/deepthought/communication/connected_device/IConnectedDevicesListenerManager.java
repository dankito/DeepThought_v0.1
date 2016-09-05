package net.dankito.deepthought.communication.connected_device;

/**
 * Created by ganymed on 05/09/16.
 */
public interface IConnectedDevicesListenerManager {

  boolean addConnectedDevicesListener(IConnectedDevicesListener listener);

  boolean removeConnectedDevicesListener(IConnectedDevicesListener listener);

}

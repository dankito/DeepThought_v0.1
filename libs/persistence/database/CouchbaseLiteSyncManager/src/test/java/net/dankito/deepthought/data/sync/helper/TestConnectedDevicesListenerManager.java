package net.dankito.deepthought.data.sync.helper;

import net.dankito.deepthought.communication.connected_device.ConnectedRegisteredDevicesListener;
import net.dankito.deepthought.communication.connected_device.IConnectedRegisteredDevicesListenerManager;
import net.dankito.deepthought.communication.model.ConnectedDevice;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by ganymed on 05/09/16.
 */
public class TestConnectedDevicesListenerManager implements IConnectedRegisteredDevicesListenerManager {

  protected Set<ConnectedRegisteredDevicesListener> listeners = new HashSet<>();


  @Override
  public boolean addConnectedDevicesListener(ConnectedRegisteredDevicesListener listener) {
    return listeners.add(listener);
  }

  @Override
  public boolean removeConnectedDevicesListener(ConnectedRegisteredDevicesListener listener) {
    return listeners.remove(listener);
  }


  public void simulateDeviceConnected(ConnectedDevice device) {
    callDeviceConnectedListeners(device);
  }

  public void simulateDeviceDisconnected(ConnectedDevice device) {
    callDeviceDisconnectedListeners(device);
  }


  public void callDeviceConnectedListeners(ConnectedDevice device) {
    for(ConnectedRegisteredDevicesListener listener : listeners) {
      listener.registeredDeviceConnected(device);
    }
  }

  public void callDeviceDisconnectedListeners(ConnectedDevice device) {
    for(ConnectedRegisteredDevicesListener listener : listeners) {
      listener.registeredDeviceDisconnected(device);
    }
  }

}

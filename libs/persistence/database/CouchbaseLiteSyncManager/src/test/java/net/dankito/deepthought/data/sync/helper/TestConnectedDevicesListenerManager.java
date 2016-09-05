package net.dankito.deepthought.data.sync.helper;

import net.dankito.deepthought.communication.connected_device.IConnectedDevicesListener;
import net.dankito.deepthought.communication.connected_device.IConnectedDevicesListenerManager;
import net.dankito.deepthought.communication.model.ConnectedDevice;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by ganymed on 05/09/16.
 */
public class TestConnectedDevicesListenerManager implements IConnectedDevicesListenerManager {

  protected Set<IConnectedDevicesListener> listeners = new HashSet<>();


  @Override
  public boolean addConnectedDevicesListener(IConnectedDevicesListener listener) {
    return listeners.add(listener);
  }

  @Override
  public boolean removeConnectedDevicesListener(IConnectedDevicesListener listener) {
    return listeners.remove(listener);
  }


  public void simulateDeviceConnected(ConnectedDevice device) {
    callDeviceConnectedListeners(device);
  }

  public void simulateDeviceDisconnected(ConnectedDevice device) {
    callDeviceDisconnectedListeners(device);
  }


  public void callDeviceConnectedListeners(ConnectedDevice device) {
    for(IConnectedDevicesListener listener : listeners) {
      listener.registeredDeviceConnected(device);
    }
  }

  public void callDeviceDisconnectedListeners(ConnectedDevice device) {
    for(IConnectedDevicesListener listener : listeners) {
      listener.registeredDeviceDisconnected(device);
    }
  }

}

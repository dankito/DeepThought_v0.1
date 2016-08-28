package net.dankito.deepthought.data.sync;

import net.dankito.deepthought.communication.DeepThoughtConnector;
import net.dankito.deepthought.communication.connected_device.IConnectedDevicesListener;
import net.dankito.deepthought.communication.model.ConnectedDevice;

/**
 * Created by ganymed on 28/08/16.
 */
public abstract class SyncManagerBase implements IDeepThoughtSyncManager {


  public SyncManagerBase(DeepThoughtConnector connector) {
    connector.addConnectedDevicesListener(connectedDevicesListener);
  }


  protected abstract void startSynchronizationWithDevice(ConnectedDevice device);

  protected abstract void stopSynchronizationWithDevice(ConnectedDevice device);


  protected IConnectedDevicesListener connectedDevicesListener = new IConnectedDevicesListener() {
    @Override
    public void registeredDeviceConnected(ConnectedDevice device) {
      startSynchronizationWithDevice(device);
    }

    @Override
    public void registeredDeviceDisconnected(ConnectedDevice device) {
      stopSynchronizationWithDevice(device);
    }
  };

}

package net.dankito.deepthought.data.sync;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.communication.IDeepThoughtConnector;
import net.dankito.deepthought.communication.connected_device.IConnectedDevicesListener;
import net.dankito.deepthought.communication.model.ConnectedDevice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ganymed on 28/08/16.
 */
public abstract class SyncManagerBase implements IDeepThoughtSyncManager {

  private static final Logger log = LoggerFactory.getLogger(SyncManagerBase.class);


  public SyncManagerBase(IDeepThoughtConnector deepThoughtConnector) {
    deepThoughtConnector.addConnectedDevicesListener(connectedDevicesListener);
  }


  protected abstract void startSynchronizationWithDevice(ConnectedDevice device) throws Exception;

  protected abstract void stopSynchronizationWithDevice(ConnectedDevice device);


  protected void startSynchronizationWithDeviceAsync(final ConnectedDevice device) {
    Application.getThreadPool().runTaskAsync(new Runnable() {
      @Override
      public void run() {
        try {
          startSynchronizationWithDevice(device);
        } catch(Exception e) {
          log.error("Could not start Synchronization with Device " + device); // TODO: inform User (e.g. over a Notification)
        }
      }
    });
  }


  protected IConnectedDevicesListener connectedDevicesListener = new IConnectedDevicesListener() {
    @Override
    public void registeredDeviceConnected(ConnectedDevice device) {
      startSynchronizationWithDeviceAsync(device);
    }

    @Override
    public void registeredDeviceDisconnected(ConnectedDevice device) {
      stopSynchronizationWithDevice(device);
    }
  };

}

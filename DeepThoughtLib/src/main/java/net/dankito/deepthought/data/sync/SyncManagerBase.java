package net.dankito.deepthought.data.sync;

import net.dankito.deepthought.communication.connected_device.ConnectedRegisteredDevicesListener;
import net.dankito.deepthought.communication.connected_device.IConnectedRegisteredDevicesListenerManager;
import net.dankito.deepthought.communication.model.ConnectedDevice;
import net.dankito.deepthought.data.persistence.db.BaseEntity;
import net.dankito.deepthought.util.IThreadPool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by ganymed on 28/08/16.
 */
public abstract class SyncManagerBase implements IDeepThoughtSyncManager {

  private static final Logger log = LoggerFactory.getLogger(SyncManagerBase.class);


  protected IThreadPool threadPool;

  protected Set<ISynchronizationListener> synchronizationListeners = new HashSet<>();


  public SyncManagerBase(IConnectedRegisteredDevicesListenerManager connectedDevicesListenerManager, IThreadPool threadPool) {
    this.threadPool = threadPool;

    connectedDevicesListenerManager.addConnectedDevicesListener(connectedDevicesListener);
  }


  protected abstract void startSynchronizationWithDevice(ConnectedDevice device) throws Exception;

  protected abstract void stopSynchronizationWithDevice(ConnectedDevice device);


  protected void startSynchronizationWithDeviceAsync(final ConnectedDevice device) {
    threadPool.runTaskAsync(new Runnable() {
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


  protected boolean hasSynchronizationListeners() {
    return synchronizationListeners.size() > 0;
  }

  public boolean addSynchronizationListener(ISynchronizationListener listener) {
    return synchronizationListeners.add(listener);
  }

  public boolean removeSynchronizationListener(ISynchronizationListener listener) {
    return synchronizationListeners.remove(listener);
  }

  protected void callEntitySynchronizedListeners(BaseEntity synchronizedEntity) {
    for(ISynchronizationListener listener : synchronizationListeners) {
      listener.entitySynchronized(synchronizedEntity);
    }
  }


  protected ConnectedRegisteredDevicesListener connectedDevicesListener = new ConnectedRegisteredDevicesListener() {
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

package net.dankito.deepthought.data.sync;

import net.dankito.deepthought.communication.DeepThoughtConnector;
import net.dankito.deepthought.communication.model.ConnectedDevice;

public class CouchbaseLiteSyncManager extends SyncManagerBase {


  public CouchbaseLiteSyncManager(DeepThoughtConnector connector) {
    super(connector);
  }


  @Override
  protected void startSynchronizationWithDevice(ConnectedDevice device) {

  }

  @Override
  protected void stopSynchronizationWithDevice(ConnectedDevice device) {

  }

}

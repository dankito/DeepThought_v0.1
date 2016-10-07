package net.dankito.deepthought.data.sync;

import net.dankito.deepthought.data.listener.AllEntitiesListener;

/**
 * Created by ganymed on 07/10/16.
 */
public class NoOpSyncManager implements IDeepThoughtSyncManager {

  @Override
  public boolean addSynchronizationListener(AllEntitiesListener listener) {
    return false;
  }

  @Override
  public boolean removeSynchronizationListener(AllEntitiesListener listener) {
    return false;
  }

  @Override
  public void stop() {

  }

}

package net.dankito.deepthought.data.sync;

import net.dankito.deepthought.data.listener.AllEntitiesListener;

/**
 * Created by ganymed on 25/11/14.
 */
public interface IDeepThoughtSyncManager {

  boolean addSynchronizationListener(AllEntitiesListener listener);

  boolean removeSynchronizationListener(AllEntitiesListener listener);

  void stop();

}

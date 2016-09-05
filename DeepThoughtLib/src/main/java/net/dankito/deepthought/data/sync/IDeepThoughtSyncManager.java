package net.dankito.deepthought.data.sync;

/**
 * Created by ganymed on 25/11/14.
 */
public interface IDeepThoughtSyncManager {

  boolean addSynchronizationListener(ISynchronizationListener listener);

  boolean removeSynchronizationListener(ISynchronizationListener listener);

  void stop();

}

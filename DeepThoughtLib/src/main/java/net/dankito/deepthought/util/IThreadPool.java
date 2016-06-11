package net.dankito.deepthought.util;

/**
 * Created by ganymed on 24/08/15.
 */
public interface IThreadPool {

  void runTaskAsync(Runnable task);

  void shutDown();

}

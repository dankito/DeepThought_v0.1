package net.deepthought.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by ganymed on 24/08/15.
 */
public class ThreadPool implements IThreadPool {

  protected ExecutorService threadPool = Executors.newCachedThreadPool();


  @Override
  public void runTaskAsync(Runnable task) {
    threadPool.execute(task);
  }

  @Override
  public void shutDown() {
    if(threadPool != null && threadPool.isShutdown()) {
      threadPool.shutdownNow();
//      threadPool = null;
    }
  }
}

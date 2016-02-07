package net.deepthought.util;

import net.deepthought.Application;

/**
 * Created by ganymed on 20/12/15.
 */
public class ThreadHelper {

  public static void runTaskAsync(Runnable runnable) {
    if(Application.getThreadPool() != null) {
      Application.getThreadPool().runTaskAsync(runnable);
    }
    else {
      new Thread(runnable).start();
    }
  }

}

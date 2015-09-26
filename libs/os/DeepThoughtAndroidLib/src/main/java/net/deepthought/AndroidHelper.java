package net.deepthought;

import android.os.Looper;

/**
 * Created by ganymed on 26/09/15.
 */
public class AndroidHelper {

  public static boolean isRunningOnUiThread() {
    return Looper.getMainLooper().getThread() == Thread.currentThread();
  }

}

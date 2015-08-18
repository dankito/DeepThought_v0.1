package net.deepthought.helper;

import android.app.Activity;
import android.os.Looper;
import android.widget.Toast;

/**
 * Created by ganymed on 18/08/15.
 */
public class AlertHelper {

  public static boolean isRunningOnUiThread() {
    return Looper.getMainLooper().getThread() == Thread.currentThread();
  }

  public static void showErrorMessage(final Activity activity, final CharSequence errorMessage) {
    if(isRunningOnUiThread() == true) {
      Toast.makeText(activity, errorMessage, Toast.LENGTH_LONG).show();
    }
    else {
      activity.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          showErrorMessage(activity, errorMessage);
        }
      });
    }
  }
}

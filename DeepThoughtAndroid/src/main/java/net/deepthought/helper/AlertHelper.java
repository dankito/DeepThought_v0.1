package net.deepthought.helper;

import android.app.Activity;
import android.os.Looper;
import android.support.v7.app.AlertDialog;

import net.deepthought.R;

/**
 * Created by ganymed on 18/08/15.
 */
public class AlertHelper {

  public static boolean isRunningOnUiThread() {
    return Looper.getMainLooper().getThread() == Thread.currentThread();
  }


  public static void showErrorMessage(final Activity activity, int errorMessageResId) {
    showErrorMessage(activity, activity.getString(errorMessageResId));
  }

  public static void showErrorMessage(final Activity activity, final CharSequence errorMessage) {
    if(isRunningOnUiThread() == true) {
      showErrorMessageOnUiThread(activity, errorMessage);
    }
    else {
      activity.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          showErrorMessageOnUiThread(activity, errorMessage);
        }
      });
    }
  }

  public static void showErrorMessageOnUiThread(final Activity activity, final CharSequence errorMessage) {
    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
    builder = builder.setMessage(errorMessage);
    // TODO: set error icon

    builder.setNegativeButton(R.string.ok, null);

    builder.create().show();
  }

  public static void showInfoMessage(Activity activity, int infoMessageResId) {
    showInfoMessage(activity, activity.getString(infoMessageResId));
  }

  public static void showInfoMessage(final Activity activity, final CharSequence infoMessage) {
    if(isRunningOnUiThread() == true) {
      showInfoMessageOnUiThread(activity, infoMessage);
    }
    else {
      activity.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          showInfoMessageOnUiThread(activity, infoMessage);
        }
      });
    }
  }

  protected static void showInfoMessageOnUiThread(final Activity activity, final CharSequence infoMessage) {
    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
    builder = builder.setMessage(infoMessage);

    builder.setNegativeButton(R.string.ok, null);

    builder.create().show();
  }
}

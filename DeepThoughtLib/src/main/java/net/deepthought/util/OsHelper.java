package net.deepthought.util;

/**
 * Created by ganymed on 19/08/15.
 */
public class OsHelper {

  private static Boolean isRunningOnAndroid = null;

  public static boolean isRunningOnAndroid() {
    if(isRunningOnAndroid == null) {
      isRunningOnAndroid = determineIfIsRunningOnAndroid();
    }

    return isRunningOnAndroid;
  }

  private static Boolean determineIfIsRunningOnAndroid() {
    try {
      Class.forName("android.app.Activity");
      return true;
    } catch(Exception ex) { }

    return false;
  }

}

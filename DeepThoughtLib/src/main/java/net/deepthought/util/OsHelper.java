package net.deepthought.util;

import net.deepthought.Application;

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

  public static boolean isRunningOnJavaSeOrOnAndroidApiLevelAtLeastOf(int minimumApiLevel) {
    return isRunningOnAndroid() == false || Application.getPlatformConfiguration().getOsVersion() >= minimumApiLevel;
  }

  public static boolean isRunningOnOnAndroidApiLevel(int apiLevel) {
    return isRunningOnAndroid() == true && Application.getPlatformConfiguration().getOsVersion() == apiLevel;
  }

  private static Boolean determineIfIsRunningOnAndroid() {
    try {
      Class.forName("android.app.Activity");
      return true;
    } catch(Exception ex) { }

    return false;
  }

}

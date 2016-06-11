package net.dankito.deepthought.util;

import net.dankito.deepthought.Application;

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
    return isRunningOnAndroid() == false || isRunningOnAndroidAtLeastOfApiLevel(minimumApiLevel);
  }

  public static boolean isRunningOnAndroidApiLevel(int apiLevel) {
    return isRunningOnAndroid() == true && Application.getPlatformConfiguration().getOsVersion() == apiLevel;
  }

  public static boolean isRunningOnAndroidAtLeastOfApiLevel(int minimumApiLevel) {
    return isRunningOnAndroid() == true && Application.getPlatformConfiguration().getOsVersion() >= minimumApiLevel;
  }

  private static Boolean determineIfIsRunningOnAndroid() {
    try {
      Class.forName("android.app.Activity");
      return true;
    } catch(Exception ex) { }

    return false;
  }

}

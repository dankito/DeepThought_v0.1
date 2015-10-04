package net.deepthought.platform;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import net.deepthought.AndroidHelper;
import net.deepthought.util.OsHelper;

/**
 * Created by ganymed on 23/08/15.
 */
public class AndroidPlatformConfiguration implements IPlatformConfiguration {

  protected Context context;


  public AndroidPlatformConfiguration(Context context) {
    this.context = context;
  }


  @Override
  public String getUserName() {
    // here's a tip how it can be done but in my eyes it's not worth the effort: https://stackoverflow.com/questions/9323207/how-can-i-get-the-first-name-or-full-name-of-the-user-of-the-phone
//    return "";
    return System.getProperty("user.name");
  }

  @Override
  public String getPlatformName() {
    return "Android";
  }

  @Override
  public int getOsVersion() {
    return Build.VERSION.SDK_INT;
  }

  @Override
  public String getOsVersionString() {
    return Build.VERSION.RELEASE;
  }

  @Override
  public boolean isRunningInEmulator() {
    return Build.FINGERPRINT.startsWith("generic") || Build.DEVICE.startsWith("generic") || Build.PRODUCT.contains("sdk") || Build.MODEL.toLowerCase().contains("sdk");
  }

  @Override
  public boolean hasCaptureDevice() {
    return AndroidHelper.hasPermission(context, PackageManager.FEATURE_CAMERA);
  }

  @Override
  public String getLineSeparator() {
    if(OsHelper.isRunningOnJavaSeOrOnAndroidApiLevelAtLeastOf(19))
      return System.lineSeparator();
    return "\n"; // is always '\n' on Android, see https://developer.android.com/reference/java/lang/System.html#lineSeparator%28%29
  }

  @Override
  public String getTempDir() {
    return context.getCacheDir().getAbsolutePath();
  }

}

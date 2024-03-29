package net.dankito.deepthought.platform;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;

import net.dankito.deepthought.platform.IPlatformConfiguration;
import net.dankito.deepthought.AndroidHelper;
import net.dankito.deepthought.util.OsHelper;

import java.io.File;

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
  public String getDeviceName() {
    String manufacturer = android.os.Build.MANUFACTURER;
    if(manufacturer.length() > 0 && Character.isLowerCase(manufacturer.charAt(0))) {
      manufacturer = Character.toUpperCase(manufacturer.charAt(0)) + manufacturer.substring(1);
    }

    return manufacturer + " " + android.os.Build.MODEL;
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
  public boolean canScanBarcodes() {
    return hasCaptureDevice();
  }

  @Override
  public String getLineSeparator() {
    if(OsHelper.isRunningOnJavaSeOrOnAndroidApiLevelAtLeastOf(19))
      return System.lineSeparator();
    return "\n"; // is always '\n' on Android, see https://developer.android.com/reference/java/lang/System.html#lineSeparator%28%29
  }

  @Override
  public String getTempDir() {
    if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
      File tempDir = new File(Environment.getExternalStorageDirectory(), "tmp");
      tempDir.mkdirs();
      return tempDir.getAbsolutePath();
    }
    return context.getCacheDir().getAbsolutePath();
  }

}

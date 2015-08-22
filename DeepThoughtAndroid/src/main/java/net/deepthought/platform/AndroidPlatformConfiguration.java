package net.deepthought.platform;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

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
  public String getOsVersion() {
    return Build.VERSION.RELEASE;
  }

  @Override
  public boolean hasCaptureDevice() {
    try {
      PackageManager pm = context.getPackageManager();
      return pm.hasSystemFeature(PackageManager.FEATURE_CAMERA);
    } catch(Exception ex) { }

    return false;
  }
}

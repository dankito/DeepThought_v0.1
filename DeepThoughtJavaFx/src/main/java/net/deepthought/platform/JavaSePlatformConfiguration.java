package net.deepthought.platform;

/**
 * Created by ganymed on 23/08/15.
 */
public class JavaSePlatformConfiguration implements IPlatformConfiguration {

  @Override
  public String getUserName() {
    return System.getProperty("user.name");
  }

  @Override
  public String getPlatformName() {
    return System.getProperty("os.name");
  }

  @Override
  public String getOsVersion() {
    return System.getProperty("os.version");
  }

  @Override
  public boolean hasCaptureDevice() {
    return false;
  }
}

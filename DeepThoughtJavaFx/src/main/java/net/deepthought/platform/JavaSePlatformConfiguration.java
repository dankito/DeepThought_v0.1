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
  public int getOsVersion() {
    // TODO: don't know how to do this generically on JavaSE (but this information is anyway right now only used for Android)
    return 0;
  }

  @Override
  public String getOsVersionString() {
    return System.getProperty("os.version");
  }

  @Override
  public boolean isRunningInEmulator() {
    return false; // only needed on Android
  }

  @Override
  public boolean hasCaptureDevice() {
    return false;
  }

  @Override
  public boolean canScanBarcodes() {
    return false;
  }

  @Override
  public String getLineSeparator() {
    return System.lineSeparator();
  }

  @Override
  public String getTempDir() {
    return System.getProperty("java.io.tmpdir");
  }

}

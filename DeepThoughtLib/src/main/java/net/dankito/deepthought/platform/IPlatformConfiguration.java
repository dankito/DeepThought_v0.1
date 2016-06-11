package net.dankito.deepthought.platform;

/**
 * Created by ganymed on 23/08/15.
 */
public interface IPlatformConfiguration {

  String getUserName();

  String getDeviceName();

  String getPlatformName();

  int getOsVersion();

  String getOsVersionString();

  boolean isRunningInEmulator();

  boolean hasCaptureDevice();

  boolean canScanBarcodes();

  String getLineSeparator();

  String getTempDir();

}

package net.deepthought.platform;

/**
 * Created by ganymed on 23/08/15.
 */
public interface IPlatformConfiguration {

  String getUserName();

  String getPlatformName();

  int getOsVersion();

  String getOsVersionString();

  boolean isRunningInEmulator();

  boolean hasCaptureDevice();

  String getLineSeparator();

  String getTempDir();

}

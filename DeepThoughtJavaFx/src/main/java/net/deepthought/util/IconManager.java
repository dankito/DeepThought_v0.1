package net.deepthought.util;

import net.deepthought.controls.Constants;

/**
 * Created by ganymed on 21/08/15.
 */
public class IconManager {

  protected static IconManager instance = null;

  public static IconManager getInstance() {
    if(instance == null)
      instance = new IconManager();
    return instance;
  }



  public String getLogoForOperatingSystem(String platform, String version, String platformArchitecture) {
    if(platform.toLowerCase().contains("android"))
      return Constants.AndroidLogoPath;
    else if(platform.toLowerCase().contains("linux"))
      return Constants.LinuxLogoPath;
    else if(platform.toLowerCase().contains("windows"))
      return Constants.WindowsLogoPath;
    else if(platform.toLowerCase().contains("mac"))
      return Constants.AppleLogoPath;
    else if(platform.toLowerCase().contains("solaris"))
      return Constants.SolarisLogoPath;

    return null; // TODO: create a placeholder logo
  }

  public String getIconForOperatingSystem(String platform, String version, String platformArchitecture) {
    if(platform.toLowerCase().contains("android"))
      return Constants.AndroidIconPath;
    else if(platform.toLowerCase().contains("linux"))
      return Constants.LinuxIconPath;
    else if(platform.toLowerCase().contains("windows"))
      return Constants.WindowsIconPath;
    else if(platform.toLowerCase().contains("mac"))
      return Constants.AppleIconPath;
    else if(platform.toLowerCase().contains("solaris"))
      return Constants.SolarisIconPath;

    return null; // TODO: create a placeholder icon
  }

}

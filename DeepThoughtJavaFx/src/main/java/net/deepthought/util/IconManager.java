package net.deepthought.util;

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



//  public String getIconForPlatform(String platform, String version, String platformArchitecture) {
//
//  }

}

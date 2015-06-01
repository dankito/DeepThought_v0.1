package net.deepthought.plugin;

import java.io.File;

/**
 * Created by ganymed on 25/04/15.
 */
public interface IPluginManager {

  public File getPluginsFolderFile();

  public String getPluginsFolderPath();

  public void loadPluginsAsync();

}

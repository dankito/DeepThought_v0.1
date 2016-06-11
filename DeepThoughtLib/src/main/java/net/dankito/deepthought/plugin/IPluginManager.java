package net.dankito.deepthought.plugin;

import java.io.File;
import java.util.Collection;

/**
 * Created by ganymed on 25/04/15.
 */
public interface IPluginManager {

  public File getPluginsFolderFile();

  public String getPluginsFolderPath();

  public void loadPluginsAsync(Collection<IPlugin> staticallyLinkedPlugins);

}

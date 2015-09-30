package net.deepthought.plugin;

import net.deepthought.Application;
import net.deepthought.data.contentextractor.IContentExtractor;
import net.deepthought.util.Localization;
import net.deepthought.util.Notification;
import net.deepthought.util.NotificationType;
import net.deepthought.util.file.FileUtils;

import org.apache.xbean.finder.ResourceFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Created by ganymed on 25/04/15.
 */
public class DefaultPluginManager implements IPluginManager {

  public final static String PluginsFolderName = "plugins";


  private final static Logger log = LoggerFactory.getLogger(DefaultPluginManager.class);


  protected List<Class> loadedPluginTypes = new CopyOnWriteArrayList<>();


  public DefaultPluginManager() {
    FileUtils.ensureFolderExists(getPluginsFolderPath());
  }


  public File getPluginsFolderFile() {
    File pluginsParentFolder = new File(Application.getDataFolderPath());
    pluginsParentFolder = pluginsParentFolder.getAbsoluteFile();
    pluginsParentFolder = pluginsParentFolder.getParentFile();
    return new File(pluginsParentFolder, PluginsFolderName);
  }

  public String getPluginsFolderPath() {
    return getPluginsFolderFile().getPath();
  }


  public void loadPluginsAsync(final Collection<IPlugin> staticallyLinkedPlugins) {
    Application.getThreadPool().runTaskAsync(new Runnable() {
      @Override
      public void run() {
        loadPlugins(staticallyLinkedPlugins);
      }
    });
  }

  protected void loadPlugins(Collection<IPlugin> staticallyLinkedPlugins) {
    copyStaticallyProvidedPluginsToPluginsFolder();

    loadPluginsFromPluginsFolder();

//    if(staticallyLinkedPlugins != null)
//      loadStaticallyLinkedPlugins(staticallyLinkedPlugins);
  }

  protected void loadPluginsFromPluginsFolder() {
    try {
      for (File file : getPluginsFolderFile().listFiles()) {
        if ("jar" .equals(FileUtils.getFileExtension(file)))
          searchJarFileForPlugins(file);
      }
    } catch(Exception ex) {
      log.error("Could not load Plugins", ex);
    }
  }

  protected void searchJarFileForPlugins(File jarFile) {
    try {
      URL url = jarFile.toURI().toURL();
      ClassLoader classLoader = new URLClassLoader(new URL[] { url });

//      ServiceLoader<IPlugin> pluginLoader = ServiceLoader.load(IPlugin.class, classLoader);
//      for(IPlugin plugin : pluginLoader) {
//        pluginLoaded(plugin);
//      }

      // to circumvent problems with ServiceLoader, xbeans ResourceFinder can be used
      // for a really good explanation about the ServiceLoader's problems and ResourceFinder's usage see:
      // http://stackoverflow.com/questions/7039467/java-serviceloader-with-multiple-classloaders
      ResourceFinder finder = new ResourceFinder("META-INF/services/", classLoader, url);
      List<Class> implementations = finder.findAllImplementations(IPlugin.class);
      for(Class implementation : implementations)
        foundPlugin(implementation);
    } catch(Exception ex) {
      log.error("Could not search for Plugins in Jar file " + jarFile.getAbsolutePath(), ex);
    }
  }

  protected void foundPlugin(Class pluginImplementation) {
    try {
      Object newInstance = pluginImplementation.newInstance();
      if(newInstance instanceof IPlugin)
        pluginLoaded((IPlugin)newInstance);
    } catch(Exception ex) {
      log.error("Could not create new IPlugin instance for class " + pluginImplementation.getName(), ex);
    }
  }

  protected void copyStaticallyProvidedPluginsToPluginsFolder() {
    try {
      JarFile jar = FileUtils.getDeepThoughtLibJarFile();
      Enumeration enumEntries = jar.entries();

      while (enumEntries.hasMoreElements()) {
        JarEntry entry = (JarEntry)enumEntries.nextElement();

        if(entry.getName().startsWith("plugins/")) {
          String extension = FileUtils.getFileExtension(entry.getName());
          if("jar".equals(extension)) {
            FileUtils.extractJarFileEntry(jar, entry, getPluginsFolderFile().getParentFile());
          }
        }
      }
    } catch(Exception ex) {
      log.warn("Could not load Plugins from 'plugins' Resource folder", ex);
    }
  }

  protected void loadStaticallyLinkedPlugins(Collection<IPlugin> staticallyLinkedPlugins) {
    for(IPlugin plugin : staticallyLinkedPlugins) {
      pluginLoaded(plugin);
    }
  }

  protected void pluginLoaded(IPlugin plugin) {
    try {
      if(loadedPluginTypes.contains(plugin.getClass()))  // avoid that different instances of same plugin get added twice (can for example happen if the same plugin is loaded statically and dynamically
        return;
      loadedPluginTypes.add(plugin.getClass());

      if(plugin instanceof IContentExtractor)
        Application.getContentExtractorManager().addContentExtractor((IContentExtractor)plugin);

      Application.notifyUser(new Notification(NotificationType.PluginLoaded, Localization.getLocalizedString("plugin.loaded", plugin.getName()), plugin));
    } catch(Exception ex) {
      log.error("Could not create new IContentExtractor instance or add it to ContentExtractorManager for extractor " + plugin, ex);
    }
  }

}

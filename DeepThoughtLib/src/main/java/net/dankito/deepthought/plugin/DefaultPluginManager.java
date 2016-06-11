package net.dankito.deepthought.plugin;

import net.dankito.deepthought.data.contentextractor.IContentExtractor;
import net.dankito.deepthought.util.NotificationType;
import net.dankito.deepthought.Application;
import net.dankito.deepthought.util.localization.Localization;
import net.dankito.deepthought.util.Notification;
import net.dankito.deepthought.util.file.FileUtils;

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


  protected String getPluginsFileExtension() {
    return "jar";
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

    if(staticallyLinkedPlugins != null)
      loadStaticallyLinkedPlugins(staticallyLinkedPlugins);

    loadDefaultPlugins();
  }

  protected void loadPluginsFromPluginsFolder() {
    try {
      for (File file : getPluginsFolderFile().listFiles()) {
        if (getPluginsFileExtension().equals(FileUtils.getFileExtension(file)))
          searchFileForPlugins(file);
      }
    } catch(Exception ex) {
      log.error("Could not load Plugins", ex);
    }
  }

  protected void searchFileForPlugins(File pluginFile) {
    try {
      URL url = pluginFile.toURI().toURL();
      ClassLoader classLoader = createClassLoaderForPluginFile(url);

//      ServiceLoader<IPlugin> pluginLoader = ServiceLoader.load(IPlugin.class, classLoader);
//      for(IPlugin plugin : pluginLoader) {
//        pluginLoaded(plugin);
//      }

      // to circumvent problems with ServiceLoader, xbeans ResourceFinder can be used
      // for a really good explanation about the ServiceLoader's problems and ResourceFinder's usage see:
      // http://stackoverflow.com/questions/7039467/java-serviceloader-with-multiple-classloaders
      ResourceFinder finder = new ResourceFinder("META-INF/services/", classLoader, url);
      List<Class> implementations = finder.findAllImplementations(IPlugin.class);
      for(Class implementation : implementations) {
        if(IPlugin.class.isAssignableFrom(implementation)) {
          foundPlugin(implementation);
        }
      }
    } catch(Exception ex) {
      log.error("Could not search for Plugins in file " + pluginFile.getAbsolutePath(), ex);
    }
  }

  protected ClassLoader createClassLoaderForPluginFile(URL url) {
    return new URLClassLoader(new URL[] { url });
  }

  protected void foundPlugin(Class pluginImplementation) {
    try {
      Object newInstance = pluginImplementation.newInstance();
      if(newInstance instanceof IPlugin) {
        pluginLoaded((IPlugin) newInstance);
      }
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
          if(getPluginsFileExtension().equals(extension)) {
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

  protected void loadDefaultPlugins() {

  }

  protected void pluginLoaded(final IPlugin plugin) {
    // if plugin is outdated, e.g. does not have methods needed for loading plugin, an AbstractMethodError will be thrown which
    // is not caught by try-catch clause -> dispatch to a new Thread so that only new Thread dies, not current one
//    Application.getThreadPool().runTaskAsync(new Runnable() {
//      @Override
//      public void run() {
//        handleLoadedPluginOnNewThread(plugin);
//      }
//    });
    handleLoadedPluginOnNewThread(plugin);
  }

  protected void handleLoadedPluginOnNewThread(IPlugin plugin) {
    try {
      if(isCompatibleWithPluginSystemVersion(plugin) == false) {
        log.warn("Found plugin " + plugin.getName() + " of outdated version " + plugin.getSupportedPluginSystemVersion() + ". Plugin cannot be loaded though.");
        Application.notifyUser(new Notification(NotificationType.OutdatedPluginFound, Localization.getLocalizedString("alert.message.outdated.plugin.found", plugin.getName(),
            plugin.getSupportedPluginSystemVersion(), Application.CurrentPluginSystemVersion), Localization.getLocalizedString("alert.title.outdated.plugin.found"), plugin));
        return;
      }

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

  protected boolean isCompatibleWithPluginSystemVersion(IPlugin plugin) {
    return ((Integer)Application.CurrentPluginSystemVersion).equals(plugin.getSupportedPluginSystemVersion());
  }

}

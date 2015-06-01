package net.deepthought.plugin;

import net.deepthought.Application;
import net.deepthought.data.contentextractor.IContentExtractor;
import net.deepthought.util.file.FileUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ServiceLoader;

/**
 * Created by ganymed on 25/04/15.
 */
public class DefaultPluginManager implements IPluginManager {

  public final static String PluginsFolderName = "plugins";


  private final static Logger log = LoggerFactory.getLogger(DefaultPluginManager.class);


  public DefaultPluginManager() {
    FileUtils.ensureFolderExists(getPluginsFolderPath());
  }


  public File getPluginsFolderFile() {
//    return new File(Application.getDataFolderPath(), PluginsFolderName);
    return new File(PluginsFolderName); // TODO: on Android this will not work -> use a folder on SD-Card
  }

  public String getPluginsFolderPath() {
    return getPluginsFolderFile().getPath();
  }

  public void loadPluginsAsync() {
    new Thread(new Runnable() {
      @Override
      public void run() {
        loadPlugins();
      }
    }).start();
  }

  protected void loadPlugins() {
    try {
      for (File file : getPluginsFolderFile().listFiles()) {
        if ("jar" .equals(FileUtils.getFileExtension(file)))
          searchJarFileForPlugins(file);
      }
    } catch(Exception ex) {
      log.error("Could not load Plugins", ex);
    }
  }

  protected void searchJarFileForPlugins(File jar) {
    try {
      ClassLoader classLoader = new URLClassLoader(new URL[] { jar.toURI().toURL() });

      ServiceLoader<IContentExtractor> contentExtractorLoader = ServiceLoader.load(IContentExtractor.class, classLoader);
      for(IContentExtractor contentExtractor : contentExtractorLoader) {
        try {
          Application.getContentExtractorManager().addContentExtractor(contentExtractor);
        } catch(Exception ex) {
          log.error("Could not create new IContentExtractor instance or add it to ContentExtractorManager for extractor " + contentExtractor, ex);
        }
      }

      // to circumvent problems with ServiceLoader, xbeans ResourceFinder can be used
      // for a really good explanation about the ServiceLoader's problems and ResourceFinder's usage see:
      // http://stackoverflow.com/questions/7039467/java-serviceloader-with-multiple-classloaders
//      ResourceFinder finder = new ResourceFinder("META-INF/services/", classLoader, jar.toURI().toURL());
//      List<Class> implementations = finder.findAllImplementations(IContentExtractor.class);
//      for(Class implementation : implementations)
//        foundContentExtractorPlugin(implementation);
    } catch(Exception ex) {
      log.error("Could not search for Plugins in Jar file " + jar.getAbsolutePath(), ex);
    }
  }

  protected void foundContentExtractorPlugin(Class<IContentExtractor> contentExtractorClass) {
    try {
      Application.getContentExtractorManager().addContentExtractor(contentExtractorClass.newInstance());
    } catch(Exception ex) {
      log.error("Could not create new IContentExtractor instance or add it to ContentExtractorManager for class " + contentExtractorClass.getName(), ex);
    }
  }
}

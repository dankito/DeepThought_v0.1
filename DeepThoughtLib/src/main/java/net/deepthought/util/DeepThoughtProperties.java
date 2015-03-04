package net.deepthought.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Properties;

/**
 * Created by ganymed on 01/01/15.
 */
public class DeepThoughtProperties {

  public final static String DeepThoughtPropertiesFileName = "DeepThoughtFx.properties";

  public final static String DataFolderKey = "data.folder";

  public final static String CouldNotLoadDeepThoughtProperties = "Could not load Deep Thought Properties";

  public final static String DefaultDataFolder = "data/";


  private final static Logger log = LoggerFactory.getLogger(DeepThoughtProperties.class);


  protected static Properties deepThoughtProperties = null;

  protected static Boolean doesPropertiesFileExist = null;

  public static Properties getDeepThoughtProperties() {
    if(deepThoughtProperties == null) {
      deepThoughtProperties = loadDeepThoughtProperties();
    }

    return deepThoughtProperties;
  }

  protected static Properties loadDeepThoughtProperties() {
    try {
      Properties deepThoughtProperties = new Properties();
      deepThoughtProperties.load(new InputStreamReader(new FileInputStream(DeepThoughtPropertiesFileName), "UTF-8"));
      doesPropertiesFileExist = true;
      return deepThoughtProperties;
    } catch(Exception ex) {
      log.warn("Could not load data folder from " + DeepThoughtPropertiesFileName, ex);
    }

    doesPropertiesFileExist = false;
    return null;
  }

  protected static void saveDeepThoughtProperties() throws IOException {
    if(deepThoughtProperties != null) {
      deepThoughtProperties.store(new OutputStreamWriter(new FileOutputStream( DeepThoughtPropertiesFileName), "UTF-8"), null);
    }
  }

  public static String getValue(String key) {
    if(getDeepThoughtProperties() != null) {
      return getDeepThoughtProperties().getProperty(key);
    }

    return CouldNotLoadDeepThoughtProperties;
  }

  public static void setValue(String key, String value) {
    if(getDeepThoughtProperties() != null) {
      getDeepThoughtProperties().setProperty(key, value);
    }
  }


  public static String getDataFolderOrCreateDefaultValuesOnNull() {
    String dataFolder = getDataFolder();

    if(dataFolder == null || dataFolder == CouldNotLoadDeepThoughtProperties) {
      saveDefaultValuesIfPropertiesDontExist();
      return DefaultDataFolder;
    }

    return dataFolder;
  }

  public static String getDataFolder() {
    return getValue(DataFolderKey);
  }

  public static void setDataFolder(String dataFolder) {
    setValue(DataFolderKey, dataFolder);
  }


  public static void saveDefaultValuesIfPropertiesDontExist() {
    if(deepThoughtProperties != null)
      return;

    deepThoughtProperties = loadDeepThoughtProperties();
    if(deepThoughtProperties != null)
      return;

    try {
      deepThoughtProperties = new Properties();
      deepThoughtProperties.put(DataFolderKey, DefaultDataFolder);

      saveDeepThoughtProperties();
    } catch(Exception ex) {
      log.warn("Could not load data folder from " + DeepThoughtPropertiesFileName, ex);
    }
  }

  public static boolean doesPropertiesFileExist() {
    return doesPropertiesFileExist;
  }
}

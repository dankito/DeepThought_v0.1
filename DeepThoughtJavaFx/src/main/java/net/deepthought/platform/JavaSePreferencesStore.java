package net.deepthought.platform;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Properties;

/**
 * Created by ganymed on 24/08/15.
 */
public class JavaSePreferencesStore extends PreferencesStoreBase {

  public final static String DeepThoughtPropertiesFileName = "DeepThoughtFx.properties";


  private final static Logger log = LoggerFactory.getLogger(JavaSePreferencesStore.class);


  protected static Properties deepThoughtProperties = null;

  protected static Boolean doesPropertiesFileExist = null;

  public JavaSePreferencesStore() {
    deepThoughtProperties = loadDeepThoughtProperties();
  }

  protected Properties loadDeepThoughtProperties() {
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

  protected static void saveDeepThoughtProperties() {
    try {
      if (deepThoughtProperties != null) {
        deepThoughtProperties.store(new OutputStreamWriter(new FileOutputStream(DeepThoughtPropertiesFileName), "UTF-8"), null);
      }
    } catch(Exception ex) { log.error("Could not save DeepThoughtProperties to " + DeepThoughtPropertiesFileName, ex); }
  }

  protected String readValueFromStore(String key, String defaultValue) {
    if(deepThoughtProperties != null) {
      return deepThoughtProperties.getProperty(key, defaultValue);
    }

    return defaultValue;
  }

  protected void saveValueToStore(String key, String value) {
    if(deepThoughtProperties != null) {
      deepThoughtProperties.setProperty(key, value);

      saveDeepThoughtProperties();
    }
  }

  protected boolean doesValueExist(String key) {
    if(deepThoughtProperties != null)
      return deepThoughtProperties.containsKey(key);

    return false;
  }
}

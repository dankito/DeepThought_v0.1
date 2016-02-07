package net.deepthought.platform;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
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


  protected Properties deepThoughtProperties = null;

  protected Boolean doesPropertiesFileExist = null;

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
      if(ex instanceof FileNotFoundException)
        return tryToCreateSettingsFile();
      else
        log.warn("Could not load data folder from " + DeepThoughtPropertiesFileName, ex);
    }

    doesPropertiesFileExist = false;
    return null;
  }

  protected void saveDeepThoughtProperties() {
    saveDeepThoughtProperties(deepThoughtProperties);
  }

  protected boolean saveDeepThoughtProperties(Properties deepThoughtProperties) {
    try {
      if (deepThoughtProperties != null) {
        deepThoughtProperties.store(new OutputStreamWriter(new FileOutputStream(DeepThoughtPropertiesFileName), "UTF-8"), null);
        return true;
      }
    } catch(Exception ex) { log.error("Could not save DeepThoughtProperties to " + DeepThoughtPropertiesFileName, ex); }

    return false;
  }

  protected Properties tryToCreateSettingsFile() {
    Properties newFile = new Properties();
    if(saveDeepThoughtProperties(newFile)) {
      doesPropertiesFileExist = true;
      return newFile;
    }

    return null;
  }

  protected String readValueFromStore(String key, String defaultValue) {
    if(deepThoughtProperties != null) {
      return deepThoughtProperties.getProperty(key, defaultValue);
    }

    return defaultValue;
  }

  protected void saveValueToStore(String key, String value) {
    if(deepThoughtProperties != null) {
      if(value == null)
        value = "";
//      if(doesValueExist(key) == false)
        deepThoughtProperties.put(key, value);
//      deepThoughtProperties.setProperty(key, value);

      saveDeepThoughtProperties();
    }
  }

  protected boolean doesValueExist(String key) {
    if(deepThoughtProperties != null)
      return deepThoughtProperties.containsKey(key);

    return false;
  }
}

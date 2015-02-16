package net.deepthought.data.model.settings;

import net.deepthought.data.model.listener.SettingsChangedListener;
import net.deepthought.data.model.settings.enums.Setting;
import net.deepthought.data.persistence.deserializer.DeserializationResult;
import net.deepthought.data.persistence.json.JsonIoJsonHelper;
import net.deepthought.data.persistence.serializer.SerializationResult;
import net.deepthought.util.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by ganymed on 14/02/15.
 */
public abstract class SettingsBase implements Serializable {

  protected final static Logger log = LoggerFactory.getLogger(SettingsBase.class);


  protected transient Set<SettingsChangedListener> listeners = new CopyOnWriteArraySet<>();


  public SettingsBase() {

  }


  public boolean addSettingsChangedListener(SettingsChangedListener listener) {
    return listeners.add(listener);
  }

  public boolean removeSettingsChangedListener(SettingsChangedListener listener) {
    return listeners.remove(listener);
  }

  protected void callSettingsChangedListeners(Setting setting, Object previousValue, Object newValue) {
    for(SettingsChangedListener listener : listeners)
      listener.settingsChanged(setting, previousValue, newValue);
  }


  public static<T extends SettingsBase> T createSettingsFromString(String settingsString, Class<T> settingsClass) {
    T settings = null;

    if(StringUtils.isNotNullOrEmpty(settingsString)) {
      DeserializationResult<T> deserializationResult = JsonIoJsonHelper.parseJsonString(settingsString, settingsClass);
      if(deserializationResult.successful() == false)
        log.error("Could not deserialize Settings of type " + settingsClass.getName() + " from Json string " + settingsString, deserializationResult.getError());
      else
        settings = deserializationResult.getResult();
    }

    if(settings == null) {
      try {
        settings = settingsClass.newInstance();
      } catch(Exception ex) {
        log.error("This should never happen, cannot create instance of Settings class " + settingsClass.getName(), ex);
      }
    }

    return settings;
  }

  public static String serializeSettings(SettingsBase settings) {
    SerializationResult result = JsonIoJsonHelper.generateJsonString(settings);
    if(result.successful() == false) {
      log.error("Could not serialize Settings of type " + settings.getClass().getName() + " to Json", result.getError());
      return null;
    }

    return result.getSerializationResult();
  }

}

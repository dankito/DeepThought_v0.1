package net.deepthought.data.model.listener;

import net.deepthought.data.model.settings.enums.Setting;

/**
 * Created by ganymed on 14/02/15.
 */
public interface SettingsChangedListener {

  public void settingsChanged(Setting setting, Object previousValue, Object newValue);

}

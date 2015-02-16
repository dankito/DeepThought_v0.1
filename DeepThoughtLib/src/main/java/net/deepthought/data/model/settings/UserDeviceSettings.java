package net.deepthought.data.model.settings;

import net.deepthought.Application;
import net.deepthought.data.model.enums.ApplicationLanguage;
import net.deepthought.data.model.settings.enums.Setting;

import java.io.Serializable;

/**
 * Created by ganymed on 14/02/15.
 */
public class UserDeviceSettings extends SettingsBase implements Serializable {

  private static final long serialVersionUID = -7698884597742713670L;


  protected transient ApplicationLanguage language;

  protected Long languageId;

  protected boolean autoSaveChanges = true;

  protected int autoSaveChangesAfterMilliseconds = 10 * 1000;

  protected int maxBackupsToKeep = 7;


  public UserDeviceSettings() {

  }


  public ApplicationLanguage getLanguage() {
    if(language == null && languageId != null)
      language = Application.getEntityManager().getEntityById(ApplicationLanguage.class, languageId);
    return language;
  }

  public void setLanguage(ApplicationLanguage language) {
    Object previousValue = this.language;
    this.language = language;

    this.languageId = language == null ? null : language.getId();

    callSettingsChangedListeners(Setting.UserDeviceLanguage, previousValue, language);
  }

  public boolean autoSaveChanges() {
    return autoSaveChanges;
  }

  public void setAutoSaveChanges(boolean autoSaveChanges) {
    Object previousValue = this.autoSaveChanges;
    this.autoSaveChanges = autoSaveChanges;
    callSettingsChangedListeners(Setting.UserDeviceAutoSaveChanges, previousValue, autoSaveChanges);
  }

  public int getAutoSaveChangesAfterMilliseconds() {
    return autoSaveChangesAfterMilliseconds;
  }

  public void setAutoSaveChangesAfterMilliseconds(int autoSaveChangesAfterMilliseconds) {
    Object previousValue = this.autoSaveChangesAfterMilliseconds;
    this.autoSaveChangesAfterMilliseconds = autoSaveChangesAfterMilliseconds;
    callSettingsChangedListeners(Setting.UserDeviceAutoSaveChangesAfterMilliseconds, previousValue, autoSaveChangesAfterMilliseconds);
  }

  public int getMaxBackupsToKeep() {
    return maxBackupsToKeep;
  }

  public void setMaxBackupsToKeep(int maxBackupsToKeep) {
    Object previousValue = this.maxBackupsToKeep;
    this.maxBackupsToKeep = maxBackupsToKeep;
    callSettingsChangedListeners(Setting.UserDeviceMaxBackupsToKeep, previousValue, maxBackupsToKeep);
  }


}

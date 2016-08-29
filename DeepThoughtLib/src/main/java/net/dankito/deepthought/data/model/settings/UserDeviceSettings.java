package net.dankito.deepthought.data.model.settings;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.data.model.enums.ApplicationLanguage;
import net.dankito.deepthought.data.model.settings.enums.DialogsFieldsDisplay;
import net.dankito.deepthought.data.model.settings.enums.ReferencesDisplay;
import net.dankito.deepthought.data.model.settings.enums.Setting;
import net.dankito.deepthought.util.localization.Localization;

import java.io.Serializable;

/**
 * Created by ganymed on 14/02/15.
 */
public class UserDeviceSettings extends SettingsBase implements Serializable {

  private static final long serialVersionUID = -7698884597742713670L;


  protected transient ApplicationLanguage language;

  protected String languageId;

  protected boolean autoSaveChanges = true;

  protected int autoSaveChangesAfterMilliseconds = 2 * 1000;

  protected int maxBackupsToKeep = 7;

  protected DialogsFieldsDisplay dialogsFieldsDisplay = DialogsFieldsDisplay.ImportantOnes;

  protected ReferencesDisplay referencesDisplay = ReferencesDisplay.ShowOnlyReference;

  protected boolean showCategories = false;

  protected boolean showQuickEditEntryPane = false;


  public UserDeviceSettings() {

  }


  public ApplicationLanguage getLanguage() {
    if(language == null && languageId != null)
      language = Application.getEntityManager().getEntityById(ApplicationLanguage.class, languageId);
    return language;
  }

  public void setLanguage(ApplicationLanguage language) {
    if(language == this.language)
      return;

    Object previousValue = this.language;
    this.language = language;

    this.languageId = language == null ? null : language.getId();

    Localization.setLanguage(getLanguage());

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

  public DialogsFieldsDisplay getDialogsFieldsDisplay() {
    return dialogsFieldsDisplay;
  }

  public void setDialogsFieldsDisplay(DialogsFieldsDisplay dialogsFieldsDisplay) {
    Object previousValue = this.dialogsFieldsDisplay;
    this.dialogsFieldsDisplay = dialogsFieldsDisplay;
    callSettingsChangedListeners(Setting.UserDeviceDialogFieldsDisplay, previousValue, dialogsFieldsDisplay);
  }

  public ReferencesDisplay getReferencesDisplay() {
    return referencesDisplay;
  }

  public void setReferencesDisplay(ReferencesDisplay referencesDisplay) {
    Object previousValue = this.referencesDisplay;
    this.referencesDisplay = referencesDisplay;
    callSettingsChangedListeners(Setting.UserDeviceReferencesDisplay, previousValue, referencesDisplay);
  }

  public boolean showCategories() {
    return showCategories;
  }

  public void setShowCategories(boolean showCategories) {
    Object previousValue = this.showCategories;
    this.showCategories = showCategories;
    callSettingsChangedListeners(Setting.UserDeviceShowCategories, previousValue, showCategories);
  }

  public boolean showEntryQuickEditPane() {
    return showQuickEditEntryPane;
  }

  public void setShowQuickEditEntryPane(boolean showEntryQuickEditPane) {
    Object previousValue = this.showQuickEditEntryPane;
    this.showQuickEditEntryPane = showEntryQuickEditPane;
    callSettingsChangedListeners(Setting.UserDeviceShowQuickEditEntryPane, previousValue, showEntryQuickEditPane);
  }

}

package net.deepthought.data.model.settings;

import net.deepthought.Application;
import net.deepthought.data.model.Category;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.Tag;
import net.deepthought.data.model.enums.EntryTemplate;
import net.deepthought.data.model.enums.SelectedAndroidTab;
import net.deepthought.data.model.enums.SelectedTab;
import net.deepthought.data.model.settings.enums.Setting;

import java.io.Serializable;

/**
 * Created by ganymed on 15/02/15.
 */
public class DeepThoughtSettings extends SettingsBase implements Serializable {

  private static final long serialVersionUID = -4329819639101161877L;


  protected SelectedTab lastSelectedTab = SelectedTab.Categories;

  protected SelectedAndroidTab lastSelectedAndroidTab = SelectedAndroidTab.EntriesOverview;

  protected transient Category lastViewedCategory;

  protected Long lastViewedCategoryId;

  protected transient Tag lastViewedTag;

  protected Long lastViewedTagId;

  protected transient Entry lastViewedEntry;

  protected Long lastViewedEntryId;

  protected transient EntryTemplate defaultEntryTemplate;

  protected Long defaultEntryTemplateId;


  public DeepThoughtSettings() {

  }


  public SelectedTab getLastSelectedTab() {
    return lastSelectedTab;
  }

  public void setLastSelectedTab(SelectedTab lastSelectedTab) {
    SelectedTab previousLastSelectedTab = this.lastSelectedTab;
    this.lastSelectedTab = lastSelectedTab;
    this.lastSelectedAndroidTab = SelectedAndroidTab.fromOrdinal(lastSelectedTab.ordinal());
    callSettingsChangedListeners(Setting.DeepThoughtLastSelectedTab, previousLastSelectedTab, lastSelectedTab);
  }

  public SelectedAndroidTab getLastSelectedAndroidTab() {
    return lastSelectedAndroidTab;
  }

  public void setLastSelectedAndroidTab(SelectedAndroidTab lastSelectedAndroidTab) {
    SelectedAndroidTab previousLastSelectedAndroidTab = this.lastSelectedAndroidTab;
    this.lastSelectedAndroidTab = lastSelectedAndroidTab;
    if(lastSelectedAndroidTab.ordinal() <= 2)
      this.lastSelectedTab = SelectedTab.fromOrdinal(lastSelectedAndroidTab.ordinal());
    callSettingsChangedListeners(Setting.DeepThoughtLastSelectedAndroidTab, previousLastSelectedAndroidTab, lastSelectedAndroidTab);
  }

  public Category getLastViewedCategory() {
    if(lastViewedCategory == null && lastViewedCategoryId != null)
      lastViewedCategory = Application.getEntityManager().getEntityById(Category.class, lastViewedCategoryId);
    return lastViewedCategory;
  }

  public void setLastViewedCategory(Category lastViewedCategory) {
    Category previousLastViewedCategory = this.lastViewedCategory;
    this.lastViewedCategory = lastViewedCategory;

    this.lastViewedCategoryId = lastViewedCategory == null ? null : lastViewedCategory.getId();

    callSettingsChangedListeners(Setting.DeepThoughtLastViewedCategory, previousLastViewedCategory, lastViewedCategory);
  }

  public Tag getLastViewedTag() {
    if(lastViewedTag == null && lastViewedTagId != null)
      lastViewedTag = Application.getEntityManager().getEntityById(Tag.class, lastViewedTagId);
    return lastViewedTag;
  }

  public void setLastViewedTag(Tag lastViewedTag) {
    Tag previousLastViewedTag = this.lastViewedTag;
    this.lastViewedTag = lastViewedTag;

    this.lastViewedTagId = lastViewedTag == null ? null : lastViewedTag.getId();

    callSettingsChangedListeners(Setting.DeepThoughtLastViewedTag, previousLastViewedTag, lastViewedTag);
  }

  public Entry getLastViewedEntry() {
    if(lastViewedEntry == null && lastViewedEntryId != null)
      lastViewedEntry = Application.getEntityManager().getEntityById(Entry.class, lastViewedEntryId);
    return lastViewedEntry;
  }

  public void setLastViewedEntry(Entry lastViewedEntry) {
    Entry previousLastViewedEntry = this.lastViewedEntry;
    this.lastViewedEntry = lastViewedEntry;

    this.lastViewedEntryId = lastViewedEntry == null ? null : lastViewedEntry.getId();

    callSettingsChangedListeners(Setting.DeepThoughtLastViewedEntry, previousLastViewedEntry, lastViewedEntry);
  }

  public EntryTemplate getDefaultEntryTemplate() {
    if(defaultEntryTemplate == null && defaultEntryTemplateId != null)
      defaultEntryTemplate = Application.getEntityManager().getEntityById(EntryTemplate.class, defaultEntryTemplateId);
    return defaultEntryTemplate;
  }

  public void setDefaultEntryTemplate(EntryTemplate defaultEntryTemplate) {
    EntryTemplate previousValue = this.defaultEntryTemplate;
    this.defaultEntryTemplate = defaultEntryTemplate;

    this.defaultEntryTemplateId = defaultEntryTemplate == null ? null : defaultEntryTemplate.getId();

    callSettingsChangedListeners(Setting.DeepThoughtDefaultEntryTemplate, previousValue, defaultEntryTemplate);
  }


}

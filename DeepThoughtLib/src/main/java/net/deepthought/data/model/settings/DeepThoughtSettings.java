package net.deepthought.data.model.settings;

import net.deepthought.Application;
import net.deepthought.data.model.Category;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.Tag;
import net.deepthought.data.model.settings.enums.SelectedAndroidTab;
import net.deepthought.data.model.settings.enums.SelectedList;
import net.deepthought.data.model.settings.enums.SelectedTab;
import net.deepthought.data.model.settings.enums.Setting;
import net.deepthought.data.model.ui.AllEntriesSystemTag;
import net.deepthought.data.model.ui.EntriesWithoutTagsSystemTag;

import java.io.Serializable;

/**
 * Created by ganymed on 15/02/15.
 */
public class DeepThoughtSettings extends SettingsBase implements Serializable {

  private static final long serialVersionUID = -4329819639101161877L;


  protected SelectedTab lastSelectedTab = SelectedTab.Categories;

  protected SelectedAndroidTab lastSelectedAndroidTab = SelectedAndroidTab.EntriesOverview;

  protected SelectedList lastSelectedList = SelectedList.Persons;

  protected transient Category lastViewedCategory;

  protected Long lastViewedCategoryId;

  protected transient Tag lastViewedTag;

  protected Long lastViewedTagId;

  protected transient Entry lastViewedEntry;

  protected Long lastViewedEntryId;

  protected double mainWindowTabsAndEntriesOverviewDividerPosition = 300;

  protected double entriesOverviewDividerPosition = 0.5;

  protected WindowSettings mainWindowSettings = new WindowSettings(-1, -1, 1157, 621);

  protected ColumnSettings entriesOverviewIdColumnSettings = new ColumnSettings(false, 46);

  protected ColumnSettings entriesOverviewReferenceColumnSettings = new ColumnSettings(true, 140);

  protected ColumnSettings entriesOverviewEntryPreviewColumnSettings = new ColumnSettings(true, 265);

  protected ColumnSettings entriesOverviewTagsColumnSettings = new ColumnSettings(true, 150);

  protected ColumnSettings entriesOverviewCreatedColumnSettings = new ColumnSettings(false, 176);

  protected ColumnSettings entriesOverviewModifiedColumnSettings = new ColumnSettings(true, 176);

  protected WindowSettings searchAndSelectTagsToolWindowSettings = new WindowSettings(-1, -1, 400, 375);


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

  public SelectedList getLastSelectedList() {
    return lastSelectedList;
  }

  public void setLastSelectedList(SelectedList lastSelectedList) {
    Object previousValue = this.lastSelectedList;
    this.lastSelectedList = lastSelectedList;
    callSettingsChangedListeners(Setting.DeepThoughtLastSelectedList, previousValue, lastSelectedList);
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

    // As SettingsChangedListener triggers DeepThought to be saved in Database, don't call listener for LastViewedCategory, this would be overkill
    callSettingsChangedListeners(Setting.DeepThoughtLastViewedCategory, previousLastViewedCategory, lastViewedCategory);
  }

  public Tag getLastViewedTag() {
    if(lastViewedTag == null && lastViewedTagId != null) {
      if(lastViewedTagId == AllEntriesSystemTag.ID)
        lastViewedTag = Application.getDeepThought().AllEntriesSystemTag();
      else if(lastViewedTagId == EntriesWithoutTagsSystemTag.ID)
        lastViewedTag = Application.getDeepThought().EntriesWithoutTagsSystemTag();
      else
        lastViewedTag = Application.getEntityManager().getEntityById(Tag.class, lastViewedTagId);
    }

    return lastViewedTag;
  }

  public void setLastViewedTag(Tag lastViewedTag) {
    Tag previousLastViewedTag = this.lastViewedTag;
    this.lastViewedTag = lastViewedTag;

    this.lastViewedTagId = lastViewedTag == null ? null : lastViewedTag.getId();

    // As SettingsChangedListener triggers DeepThought to be saved in Database, don't call listener for LastViewedTag, this would be overkill
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

    // As SettingsChangedListener triggers DeepThought to be saved in Database, don't call listener for LastViewedEntry, this would be overkill
    callSettingsChangedListeners(Setting.DeepThoughtLastViewedEntry, previousLastViewedEntry, lastViewedEntry);
  }

  public double getMainWindowTabsAndEntriesOverviewDividerPosition() {
    return mainWindowTabsAndEntriesOverviewDividerPosition;
  }

  public void setMainWindowTabsAndEntriesOverviewDividerPosition(double mainWindowTabsAndEntriesOverviewDividerPosition) {
    Object previousValue = this.mainWindowTabsAndEntriesOverviewDividerPosition;
    this.mainWindowTabsAndEntriesOverviewDividerPosition = mainWindowTabsAndEntriesOverviewDividerPosition;
    callSettingsChangedListeners(Setting.DeepThoughtLastSelectedList, previousValue, mainWindowTabsAndEntriesOverviewDividerPosition);
  }

  public double getEntriesOverviewDividerPosition() {
    return entriesOverviewDividerPosition;
  }

  public void setEntriesOverviewDividerPosition(double entriesOverviewDividerPosition) {
    Object previousValue = this.entriesOverviewDividerPosition;
    this.entriesOverviewDividerPosition = entriesOverviewDividerPosition;
    callSettingsChangedListeners(Setting.DeepThoughtEntriesOverviewDividerPosition, previousValue, entriesOverviewDividerPosition);
  }

  public WindowSettings getMainWindowSettings() {
    return mainWindowSettings;
  }

  public ColumnSettings getEntriesOverviewIdColumnSettings() {
    return entriesOverviewIdColumnSettings;
  }

  public ColumnSettings getEntriesOverviewReferenceColumnSettings() {
    return entriesOverviewReferenceColumnSettings;
  }

  public ColumnSettings getEntriesOverviewEntryPreviewColumnSettings() {
    return entriesOverviewEntryPreviewColumnSettings;
  }

  public ColumnSettings getEntriesOverviewTagsColumnSettings() {
    return entriesOverviewTagsColumnSettings;
  }

  public ColumnSettings getEntriesOverviewCreatedColumnSettings() {
    return entriesOverviewCreatedColumnSettings;
  }

  public ColumnSettings getEntriesOverviewModifiedColumnSettings() {
    return entriesOverviewModifiedColumnSettings;
  }

  public WindowSettings getSearchAndSelectTagsToolWindowSettings() {
    return searchAndSelectTagsToolWindowSettings;
  }

}

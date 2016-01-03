package net.deepthought.javafx.dialogs.mainwindow.tabs.tags;

import net.deepthought.Application;
import net.deepthought.controls.IMainWindowControl;
import net.deepthought.controls.tag.IDisplayedTagsChangedListener;
import net.deepthought.data.model.DeepThought;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.Tag;
import net.deepthought.data.model.listener.AllEntitiesListener;
import net.deepthought.data.model.listener.EntityListener;
import net.deepthought.data.model.settings.enums.SelectedTab;
import net.deepthought.data.model.ui.SystemTag;
import net.deepthought.data.persistence.CombinedLazyLoadingList;
import net.deepthought.data.persistence.db.BaseEntity;
import net.deepthought.data.search.SearchCompletedListener;
import net.deepthought.data.search.specific.FindAllEntriesHavingTheseTagsResult;
import net.deepthought.data.search.specific.TagsSearch;
import net.deepthought.data.search.specific.TagsSearchResults;
import net.deepthought.javafx.dialogs.mainwindow.MainWindowController;
import net.deepthought.javafx.dialogs.mainwindow.tabs.tags.filterpanel.TagsFilterPanel;
import net.deepthought.javafx.dialogs.mainwindow.tabs.tags.table.TableViewTags;
import net.deepthought.util.Alerts;
import net.deepthought.util.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Created by ganymed on 01/02/15.
 */
public class TabTagsControl extends VBox implements IMainWindowControl, ITagsFilter, ISelectedTagsController {

  private final static Logger log = LoggerFactory.getLogger(TabTagsControl.class);


  protected Entry entry = null;

  protected DeepThought deepThought = null;
  protected Collection<Tag> systemTags = new ArrayList<>();

  protected String lastSearchTerm = TagsSearch.EmptySearchTerm;

  protected TagsSearch tagsSearch = null;
  protected TagsSearchResults lastTagsSearchResults = TagsSearchResults.EmptySearchResults;
  protected Collection<Tag> allTagsSearchResult = null;
  protected FindAllEntriesHavingTheseTagsResult lastFilterTagsResult = null;
  protected List<IDisplayedTagsChangedListener> displayedTagsChangedListeners = new ArrayList<>();

  protected ObservableSet<Tag> tagsFilter = FXCollections.observableSet();


  protected MainWindowController mainWindowController;

  protected TagsFilterPanel filterPanel;

  protected TableViewTags tblvwTags;



  public TabTagsControl(MainWindowController mainWindowController) {
    this.mainWindowController = mainWindowController;
    deepThought = Application.getDeepThought();

    setupControl();
  }

  public void deepThoughtChanged(DeepThought newDeepThought) {
    this.deepThought = newDeepThought;
    this.systemTags.clear();

    if(newDeepThought != null) {
      this.systemTags = Arrays.asList(new Tag[] { deepThought.AllEntriesSystemTag(), deepThought.EntriesWithoutTagsSystemTag() });
//      showAllTagsInListViewTags(deepThought);

      if (deepThought.getSettings().getLastViewedTag() != null) {
        tblvwTags.selectTag(deepThought.getSettings().getLastViewedTag());
      } else {
        tblvwTags.selectTagAtIndex(0);
      }
    }

    allTagsSearchResult = null;

    if(Application.isInstantiated() == true) // Application not instantiated yet -> searchForAllTags() will then be called in applicationInstantiated()
      searchForAllTags();
  }

  public void applicationInstantiated() {
    Application.getDataManager().addAllEntitiesListener(allEntitiesListener);

    updateTags();
  }

  public void clearData() {
    tblvwTags.clearTags();
  }

  protected void setupControl() {
    this.setPrefWidth(315.0);

    setupTagsFilterPanel();

    setupTableViewTags();
  }

  protected void setupTagsFilterPanel() {
    filterPanel = new TagsFilterPanel(this, this);
    this.getChildren().add(filterPanel);
  }

  protected void setupTableViewTags() {
    tblvwTags = new TableViewTags(this, this);
    VBox.setVgrow(tblvwTags, Priority.ALWAYS);
    this.getChildren().add(tblvwTags);

    tblvwTags.setOnKeyReleased(event -> {
      if (event.getCode() == KeyCode.DELETE) {
        removeSelectedTags();
      }
    });
  }

  public void setSelectedTagToAllEntriesSystemTag() {
    selectedTagChanged(deepThought.AllEntriesSystemTag());
  }

  @Override
  public void selectedTagChanged(Tag selectedTag) {
    log.debug("Selected Tag changed to {}", selectedTag);

    if(deepThought.getSettings().getLastViewedTag() != null)
      deepThought.getSettings().getLastViewedTag().removeEntityListener(selectedTagListener);

    deepThought.getSettings().setLastViewedTag(selectedTag);
    tblvwTags.selectTag(selectedTag);

    filterPanel.disableButtonRemoveSelectedTag(selectedTag == null || selectedTag instanceof SystemTag);

    if(selectedTag != null)
      selectedTag.addEntityListener(selectedTagListener);

    showEntriesForSelectedTag(selectedTag);
  }

  protected void showEntriesForSelectedTag(Tag tag) {
    log.debug("showEntriesForSelectedTag() has been called for Tag {}", tag);

    if(tag != null) {
      if (isTagsFilterApplied() && lastFilterTagsResult != null) {

        Set<Entry> filteredEntriesWithThisTag = new TreeSet<>();
        for (Entry tagEntry : tag.getEntries()) {
          if (lastFilterTagsResult.getEntriesHavingFilteredTags().contains(tagEntry))
            filteredEntriesWithThisTag.add(tagEntry);
        }

        mainWindowController.showEntries(filteredEntriesWithThisTag); // TODO: here can may be a problem when Entries are search. How to know about this filter?
      }
      else
        mainWindowController.showEntries(tag.getEntries());
    }
    else
      mainWindowController.showEntries(new HashSet<Entry>());
  }


  public void searchForAllTags() {
    searchTags(TagsSearch.EmptySearchTerm);
  }

  public void researchTagsWithLastSearchTerm() {
    searchTags(lastSearchTerm);
  }

  @Override
  public void searchTags() {
    searchTags(filterPanel.getTagsSearchText());
  }

  public void searchTags(String searchTerm) {
    this.lastSearchTerm = searchTerm;

    if(isTagsFilterApplied())
      filterTags();
    else {
      if (tagsSearch != null && tagsSearch.isCompleted() == false)
        tagsSearch.interrupt();

      lastSearchTerm = searchTerm;
      lastFilterTagsResult = null;

      if(StringUtils.isNullOrEmpty(filterPanel.getTagsSearchText()) && allTagsSearchResult != null) {
        setTableViewTagsItems(allTagsSearchResult);
      }
      else {
        tagsSearch = new TagsSearch(searchTerm, new SearchCompletedListener<TagsSearchResults>() {
          @Override
          public void completed(TagsSearchResults results) {
            Platform.runLater(() -> searchTagsCompleted(results));
          }
        });

        Application.getSearchEngine().searchTags(tagsSearch);
      }
    }
  }

  protected void searchTagsCompleted(TagsSearchResults results) {
    lastTagsSearchResults = results;

    if(results.hasEmptySearchTerm()) {
      allTagsSearchResult = new CombinedLazyLoadingList<Tag>(systemTags, results.getRelevantMatchesSorted());
      setTableViewTagsItems(allTagsSearchResult);
      setSelectedTagToAllEntriesSystemTag();
    }
    else
      setTableViewTagsItems(results);

    callDisplayedTagsChangedListeners(results);
  }

  protected boolean isTagsFilterApplied() {
    return tagsFilter.size() > 0;
  }

  protected void filterTags() {
    filterTags(null);
  }

  protected void filterTags(final Tag tagToSelect) {
    if(isTagsFilterApplied() == false)
      researchTagsWithLastSearchTerm();
    else {
      Application.getSearchEngine().findAllEntriesHavingTheseTags(tagsFilter, lastSearchTerm, new SearchCompletedListener<FindAllEntriesHavingTheseTagsResult>() {
        @Override
        public void completed(FindAllEntriesHavingTheseTagsResult results) {
          lastFilterTagsResult = results;
          setTableViewTagsItems(results.getTagsOnEntriesContainingFilteredTags());

          if(tagToSelect != null)
            tblvwTags.selectTag(tagToSelect);
        }
      });
    }
  }

  @Override
  public void toggleCurrentTagsTagsFilter() {
    if(StringUtils.isNullOrEmpty(filterPanel.getTagsSearchText())) // toggling all Tags is really not that senseful
      return;

    if(tblvwTags.getTagsSize() == 0)
      clearTagFilter();
    else {
      for (Tag tag : tblvwTags.getItems()) {
        toggleTagFilter(tag);
      }
    }

    filterTags();
  }

  protected void toggleTagFilter(Tag tag) {
    if(tagsFilter.contains(tag))
      tagsFilter.remove(tag);
    else
      tagsFilter.add(tag);

    setButtonRemoveTagsFilterDisabledState();
  }

  protected void setButtonRemoveTagsFilterDisabledState() {
    filterPanel.disableButtonRemoveTagsFilter(isTagsFilterApplied() == false);
  }

  @Override
  public void clearTagFilter() {
    tagsFilter.clear();
    lastFilterTagsResult = null;
    setButtonRemoveTagsFilterDisabledState();
    searchTags();
  }

  @Override
  public void setTagFilterState(Tag tag, Boolean filterTag) {
    if(filterTag == true)
      addTagToTagFilter(tag);
    else
      removeTagFromTagFilter(tag);

    setButtonRemoveTagsFilterDisabledState();
  }

  @Override
  public void removeSelectedTags() {
    List<Tag> selectedTags = new ArrayList<>(tblvwTags.getSelectionModel().getSelectedItems()); // make a copy as when multiple Tags are selected after removing the first one SelectionModel gets cleared
    for(Tag selectedTag : selectedTags) {
      if(selectedTag instanceof SystemTag == false)
        Alerts.deleteTagWithUserConfirmationIfIsSetOnEntries(deepThought, selectedTag);
    }
  }


  protected void addTagToTagFilter(Tag tag) {
    if(tagsFilter.contains(tag))
      return;

    tagsFilter.add(tag);
    filterTags(tag);

    tblvwTags.selectTag(tag);
  }

  protected void removeTagFromTagFilter(Tag tag) {
    if(tagsFilter.contains(tag) == false)
      return;

    tagsFilter.remove(tag);
    filterTags(tag);

    tblvwTags.selectTag(tag);
  }

  protected void showLastSearchResult() {
    if(lastTagsSearchResults.hasEmptySearchTerm() && allTagsSearchResult != null)
      setTableViewTagsItems(allTagsSearchResult);
    else
      setTableViewTagsItems(lastTagsSearchResults);
  }

  protected void setTableViewTagsItems(TagsSearchResults results) {
    setTableViewTagsItems(results.getRelevantMatchesSorted());
    selectTagAccordingToSearchResult(results);
  }

  protected void setTableViewTagsItems(Collection<Tag> tags) {
    tblvwTags.setTags(tags);

    if(tags == allTagsSearchResult) {
      setSelectedTagToAllEntriesSystemTag();
      callDisplayedTagsChangedListeners(TagsSearchResults.EmptySearchResults);
    }
  }

  protected void selectTagAccordingToSearchResult(TagsSearchResults results) {
    selectTagAccordingToSearchResult(results, deepThought.getSettings().getLastViewedTag());
  }

  protected void selectTagAccordingToSearchResult(TagsSearchResults results, Tag selectedTag) {
    if(results.hasEmptySearchTerm())
      setSelectedTagToAllEntriesSystemTag();
    else if(results.getLastResult().hasExactMatch())
      setSelectedTag(results.getLastResult().getExactMatch());
    else if(results.getLastResult().hasSingleMatch())
      setSelectedTag(results.getLastResult().getSingleMatch());
    else {
      if (results.getRelevantMatchesSorted().size() == 1)
        setSelectedTag(new ArrayList<Tag>(results.getRelevantMatchesSorted()).get(0));
      else if (results.getLastResult().getAllMatches().size() == 1)
        setSelectedTag(new ArrayList<Tag>(results.getLastResult().getAllMatches()).get(0));
      // don't do this, isMatch() starts loading all search results
//      else if (results.isMatch(selectedTag))
//        setSelectedTag(selectedTag);
    }
  }

  protected void setSelectedTag(Tag tagToSelect) {
    tblvwTags.getSelectionModel().select(tagToSelect);
  }


  protected void updateTags() {
    allTagsSearchResult = null;
    searchTags();
  }

  @Override
  public boolean addDisplayedTagsChangedListener(IDisplayedTagsChangedListener listener) {
    return displayedTagsChangedListeners.add(listener);
  }

  @Override
  public boolean removeDisplayedTagsChangedListener(IDisplayedTagsChangedListener listener) {
    return displayedTagsChangedListeners.remove(listener);
  }

  protected void callDisplayedTagsChangedListeners(TagsSearchResults results) {
    for(IDisplayedTagsChangedListener listener : displayedTagsChangedListeners)
      listener.displayedTagsChanged(results);
  }

  @Override
  public ObservableSet<Tag> getTagsFilter() {
    return tagsFilter;
  }

  @Override
  public TagsSearchResults getLastTagsSearchResults() {
    return lastTagsSearchResults;
  }


  protected AllEntitiesListener allEntitiesListener = new AllEntitiesListener() {
    @Override
    public void entityCreated(BaseEntity entity) {
      if(entity instanceof Tag) {
        updateTags();
      }
    }

    @Override
    public void entityUpdated(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {
      if(entity instanceof Tag) {
        updateTags();
      }
    }

    @Override
    public void entityDeleted(BaseEntity entity) {
      if(entity instanceof Tag) {
        updateTags();
      }
    }

    @Override
    public void entityAddedToCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity) {

    }

    @Override
    public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {

    }
  };

  protected EntityListener selectedTagListener = new EntityListener() {
    @Override
    public void propertyChanged(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {
      researchTagsWithLastSearchTerm();
    }

    @Override
    public void entityAddedToCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity) {
      Tag selectedTag = tblvwTags.getSelectionModel().getSelectedItem();
//      if(deepThought.getSettings().getLastSelectedTab() == SelectedTab.Tags && deepThought.getSettings().getLastViewedTag() != null &&
//          collection == deepThought.getSettings().getLastViewedTag().getEntries()) {
//      if(deepThought.getSettings().getLastSelectedTab() == SelectedTab.Tags && tblvwTags.getSelectionModel().getSelectedItem() != null &&
//          collection == tblvwTags.getSelectionModel().getSelectedItem().getEntries()) {
//      if(deepThought.getSettings().getLastSelectedTab() == SelectedTab.Tags && deepThought.getSettings().getLastViewedTag() != null &&
//          collection == deepThought.getSettings().getLastViewedTag().getEntries()) {
      if(deepThought.getSettings().getLastSelectedTab() == SelectedTab.Tags && collection == ((Tag)collectionHolder).getEntries()) {
        if(tblvwTags.getTagsSize() > 0) {
          filterTags();
        }
        showEntriesForSelectedTag((Tag)collectionHolder);
      }
    }

    @Override
    public void entityOfCollectionUpdated(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity updatedEntity) {

    }

    @Override
    public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {
//      if(deepThought.getSettings().getLastSelectedTab() == SelectedTab.Tags && deepThought.getSettings().getLastViewedTag() != null &&
//          collection == deepThought.getSettings().getLastViewedTag().getEntries()) {
      if(deepThought.getSettings().getLastSelectedTab() == SelectedTab.Tags && collection == ((Tag)collectionHolder).getEntries()) {
        if(tblvwTags.getTagsSize() > 0) {
          filterTags();
        }
        showEntriesForSelectedTag((Tag)collectionHolder);
      }
    }
  };
}

package net.dankito.deepthought.javafx.dialogs.mainwindow.tabs.tags;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.controls.IMainWindowControl;
import net.dankito.deepthought.controls.tag.IDisplayedTagsChangedListener;
import net.dankito.deepthought.data.listener.AllEntitiesListener;
import net.dankito.deepthought.data.model.DeepThought;
import net.dankito.deepthought.data.model.Entry;
import net.dankito.deepthought.data.model.Tag;
import net.dankito.deepthought.data.model.ui.SystemTag;
import net.dankito.deepthought.data.persistence.CombinedLazyLoadingList;
import net.dankito.deepthought.data.persistence.db.BaseEntity;
import net.dankito.deepthought.data.search.SearchBase;
import net.dankito.deepthought.data.search.specific.FindAllEntriesHavingTheseTagsResult;
import net.dankito.deepthought.data.search.specific.TagsSearch;
import net.dankito.deepthought.data.search.specific.TagsSearchResults;
import net.dankito.deepthought.data.search.ui.EntriesForTag;
import net.dankito.deepthought.javafx.dialogs.mainwindow.MainWindowController;
import net.dankito.deepthought.javafx.dialogs.mainwindow.tabs.tags.filterpanel.TagsFilterPanel;
import net.dankito.deepthought.javafx.dialogs.mainwindow.tabs.tags.table.TableViewTags;
import net.dankito.deepthought.util.Alerts;
import net.dankito.deepthought.util.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
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

  protected String lastSearchTerm = SearchBase.EmptySearchTerm;

  protected TagsSearch tagsSearch = null;
  protected TagsSearchResults lastTagsSearchResults = TagsSearchResults.EmptySearchResults;
  protected Collection<Tag> allTagsSearchResult = null;

  protected FindAllEntriesHavingTheseTagsResult lastFilterTagsResult = null;

  protected List<IDisplayedTagsChangedListener> displayedTagsChangedListeners = new ArrayList<>();

  protected ObservableSet<Tag> tagsFilter = FXCollections.observableSet();


  protected MainWindowController mainWindowController;

  protected EntriesForTag entriesForTag;

  protected TagsFilterPanel filterPanel;

  protected TableViewTags tblvwTags;



  public TabTagsControl(MainWindowController mainWindowController, EntriesForTag entriesForTag) {
    this.mainWindowController = mainWindowController;
    this.entriesForTag = entriesForTag;

    deepThought = Application.getDeepThought();

    setupControl();
  }

  public void deepThoughtChanged(DeepThought newDeepThought) {
    this.deepThought = newDeepThought;
    this.systemTags.clear();

    if(newDeepThought != null) {
      this.systemTags = Arrays.asList(new Tag[] { deepThought.AllEntriesSystemTag(), deepThought.EntriesWithoutTagsSystemTag() });
    }

    allTagsSearchResult = null;

    if(Application.isInstantiated()) { // Application not instantiated yet -> searchForAllTags() will then be called in applicationInstantiated()
      searchForAllTags();
    }
  }

  public void applicationInstantiated() {
    Application.getEntityChangesService().addAllEntitiesListener(allEntitiesListener);

    updateTags();
  }

  public void clearData() {
    tblvwTags.clearTags();
  }

  protected void setupControl() {
    this.setId("tabTags");
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
  }


  public void setSelectedTagToAllEntriesSystemTag() {
    selectedTagChanged(deepThought.AllEntriesSystemTag());
  }

  @Override
  public void selectedTagChanged(Tag selectedTag) {
    log.debug("Selected Tag changed to {}", selectedTag);

    deepThought.getSettings().setLastViewedTag(selectedTag);
    setSelectedTag(selectedTag);

    filterPanel.disableButtonRemoveSelectedTag(selectedTag == null || selectedTag instanceof SystemTag);

    showEntriesForSelectedTag(selectedTag);
  }

  protected void showEntriesForSelectedTag(Tag tag) {
    log.debug("showEntriesForSelectedTag() has been called for Tag {}", tag);

    if(tag != null) {
      if (isTagsFilterApplied() && lastFilterTagsResult != null) {
        showEntriesForSelectedTagWithAppliedTagsFilter(tag);
      }
      else {
        entriesForTag.setTag(tag);
      }
    }
    else {
      entriesForTag.setTag(tag);
    }
  }

  protected void showEntriesForSelectedTagWithAppliedTagsFilter(Tag tag) {
    Set<Entry> filteredEntriesWithThisTag = new TreeSet<>();
    for (Entry tagEntry : tag.getEntries()) {
      if (lastFilterTagsResult.getEntriesHavingFilteredTags().contains(tagEntry))
        filteredEntriesWithThisTag.add(tagEntry);
    }

    mainWindowController.showEntries(filteredEntriesWithThisTag); // TODO: here can may be a problem when Entries are search. How to know about this filter?
  }


  protected void searchForAllTags() {
    searchTags(SearchBase.EmptySearchTerm);
  }

  protected void researchTagsWithLastSearchTerm() {
    searchTags(lastSearchTerm);
  }

  @Override
  public void searchTags() {
    searchTags(filterPanel.getTagsSearchText());
  }

  protected void searchTags(String searchTerm) {
    this.lastSearchTerm = searchTerm;

    if(isTagsFilterApplied())
      filterTags();
    else {
      if (tagsSearch != null && tagsSearch.isCompleted() == false)
        tagsSearch.interrupt();

      searchTagsWithNoFilterApplied(searchTerm);
    }
  }

  protected void searchTagsWithNoFilterApplied(String searchTerm) {
    lastSearchTerm = searchTerm;
    lastFilterTagsResult = null;

    if(StringUtils.isNullOrEmpty(filterPanel.getTagsSearchText()) && allTagsSearchResult != null) {
      setTableViewTagsItems(allTagsSearchResult);
    }
    else {
      tagsSearch = new TagsSearch(searchTerm, results -> {
        Platform.runLater(() -> searchTagsCompleted(results));
      });

      Application.getSearchEngine().searchTags(tagsSearch);
    }
  }

  protected void searchTagsCompleted(TagsSearchResults results) {
    lastTagsSearchResults = results;

    if(results.hasEmptySearchTerm()) {
      allTagsSearchResult = new CombinedLazyLoadingList<>(systemTags, results.getRelevantMatchesSorted());
      setTableViewTagsItems(allTagsSearchResult);
    }
    else {
      setTableViewTagsItems(results);
    }
  }

  protected boolean isTagsFilterApplied() {
    return tagsFilter.size() > 0;
  }

  protected void filterTags() {
    filterTags(null);
  }

  protected void filterTags(final Tag tagToSelect) {
    if(isTagsFilterApplied() == false) {
      researchTagsWithLastSearchTerm();
    }
    else {
      Application.getSearchEngine().findAllEntriesHavingTheseTags(tagsFilter, lastSearchTerm, results -> {
        lastFilterTagsResult = results;
        setTableViewTagsItems(results.getTagsOnEntriesContainingFilteredTags());

        if(tagToSelect != null) {
          setSelectedTag(tagToSelect);
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
  public void removeSelectedTags() {
    List<Tag> selectedTags = new ArrayList<>(tblvwTags.getSelectionModel().getSelectedItems()); // make a copy as when multiple Tags are selected after removing the first one SelectionModel gets cleared
    for(Tag selectedTag : selectedTags) {
      if(selectedTag instanceof SystemTag == false)
        Alerts.deleteTagWithUserConfirmationIfIsSetOnEntries(deepThought, selectedTag);
    }
  }


  @Override
  public void setTagFilterState(Tag tag, Boolean filterTag) {
    if(filterTag)
      addTagToTagFilter(tag);
    else
      removeTagFromTagFilter(tag);

    setButtonRemoveTagsFilterDisabledState();
  }

  protected void addTagToTagFilter(Tag tag) {
    if(tagsFilter.contains(tag))
      return;

    tagsFilter.add(tag);
    filterTags(tag);

    setSelectedTag(tag);
  }

  protected void removeTagFromTagFilter(Tag tag) {
    if(tagsFilter.contains(tag) == false)
      return;

    tagsFilter.remove(tag);
    filterTags(tag);

    setSelectedTag(tag);
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
        setSelectedTag(new ArrayList<>(results.getRelevantMatchesSorted()).get(0));
      else if (results.getLastResult().getAllMatches().size() == 1)
        setSelectedTag(new ArrayList<>(results.getLastResult().getAllMatches()).get(0));
    }
  }

  protected void setSelectedTag(Tag tagToSelect) {
    tblvwTags.selectTag(tagToSelect);
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
      checkIfTagsHaveToBeUpdated(entity);
    }

    @Override
    public void entityUpdated(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {
      checkIfTagsHaveToBeUpdated(entity);
    }

    @Override
    public void entityDeleted(BaseEntity entity) {
      checkIfTagsHaveToBeUpdated(entity);
    }

    @Override
    public void entityAddedToCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity) {

    }

    @Override
    public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {

    }
  };

  protected void checkIfTagsHaveToBeUpdated(BaseEntity entity) {
    if(entity instanceof Tag) {
      updateTags();
    }
  }
}

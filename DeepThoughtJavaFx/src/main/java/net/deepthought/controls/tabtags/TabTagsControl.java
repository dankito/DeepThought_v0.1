package net.deepthought.controls.tabtags;

import net.deepthought.Application;
import net.deepthought.MainWindowController;
import net.deepthought.controller.Dialogs;
import net.deepthought.controls.Constants;
import net.deepthought.controls.FXUtils;
import net.deepthought.controls.IMainWindowControl;
import net.deepthought.controls.LazyLoadingObservableList;
import net.deepthought.controls.tag.IFilteredTagsChangedListener;
import net.deepthought.data.model.DeepThought;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.Tag;
import net.deepthought.data.model.listener.EntityListener;
import net.deepthought.data.model.settings.enums.SelectedTab;
import net.deepthought.data.model.ui.SystemTag;
import net.deepthought.data.persistence.CombinedLazyLoadingList;
import net.deepthought.data.persistence.db.BaseEntity;
import net.deepthought.data.persistence.db.TableConfig;
import net.deepthought.data.search.SearchCompletedListener;
import net.deepthought.data.search.specific.FilterTagsSearch;
import net.deepthought.data.search.specific.FilterTagsSearchResults;
import net.deepthought.data.search.specific.FindAllEntriesHavingTheseTagsResult;
import net.deepthought.util.Alerts;
import net.deepthought.util.JavaFxLocalization;
import net.deepthought.util.StringUtils;

import org.controlsfx.control.textfield.CustomTextField;
import org.controlsfx.control.textfield.TextFields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

/**
 * Created by ganymed on 01/02/15.
 */
public class TabTagsControl extends VBox implements IMainWindowControl {

  private final static Logger log = LoggerFactory.getLogger(TabTagsControl.class);


  protected Entry entry = null;

  protected DeepThought deepThought = null;
  protected Collection<Tag> systemTags = new ArrayList<>();

  protected LazyLoadingObservableList<Tag> tableViewTagsItems = null;
  protected ObservableList<TagFilterTableCell> tagFilterTableCells = FXCollections.observableArrayList();

  protected String lastSearchTerm = FilterTagsSearch.EmptySearchTerm;

  protected FilterTagsSearch tagsSearch = null;
  protected FilterTagsSearchResults lastTagsSearchResults = FilterTagsSearchResults.EmptySearchResults;
  protected Collection<Tag> allTagsSearchResult = null;
  protected FindAllEntriesHavingTheseTagsResult lastFilterTagsResult = null;
  protected List<IFilteredTagsChangedListener> filteredTagsChangedListeners = new ArrayList<>();

  protected ObservableSet<Tag> tagsFilter = FXCollections.observableSet();


  protected MainWindowController mainWindowController;

  @FXML
  protected HBox hboxTagsBar;
  @FXML
  protected TextField txtfldTagsQuickFilter;
  @FXML
  protected Button btnRemoveTagsFilter;
  @FXML
  protected Button btnRemoveSelectedTag;
  @FXML
  protected Button btnAddTag;
  @FXML
  protected TableView<Tag> tblvwTags;
  @FXML
  protected TableColumn<Tag, String> clmnTagName;
  @FXML
  protected TableColumn<Tag, Boolean> clmnTagFilter;



  public TabTagsControl(MainWindowController mainWindowController) {
    this.mainWindowController = mainWindowController;
    deepThought = Application.getDeepThought();

    if(FXUtils.loadControl(this, "TabTagsControl"))
      setupControl();

    if(deepThought != null)
      deepThought.addEntityListener(deepThoughtListener);
  }

  public void deepThoughtChanged(DeepThought newDeepThought) {
    if(this.deepThought != null)
      this.deepThought.removeEntityListener(deepThoughtListener);

    this.deepThought = newDeepThought;
    this.systemTags.clear();

    if(newDeepThought != null) {
      newDeepThought.addEntityListener(deepThoughtListener);

      this.systemTags = Arrays.asList(new Tag[] { deepThought.AllEntriesSystemTag(), deepThought.EntriesWithoutTagsSystemTag() });
//      showAllTagsInListViewTags(deepThought);

      if (deepThought.getSettings().getLastViewedTag() != null) {
        tblvwTags.getSelectionModel().select(deepThought.getSettings().getLastViewedTag());
      } else {
        tblvwTags.getSelectionModel().select(0);
      }
    }

    allTagsSearchResult = null;
    searchForAllTags();
  }

  public void applicationInstantiated() {
    allTagsSearchResult = null;
    searchForAllTags();
  }

  public void clearData() {
    tableViewTagsItems.clear(); // TODO: is it so clever calling clear on LazyLoadingObservableList?
  }

  protected void setupControl() {
    // replace normal TextField txtfldTagsQuickFilter with a SearchTextField (with a cross to clear selection)
    hboxTagsBar.getChildren().remove(txtfldTagsQuickFilter);
    txtfldTagsQuickFilter = (CustomTextField) TextFields.createClearableTextField();
    JavaFxLocalization.bindTextInputControlPromptText(txtfldTagsQuickFilter, "search.tags.prompt.text");
    hboxTagsBar.getChildren().add(1, txtfldTagsQuickFilter);
    HBox.setHgrow(txtfldTagsQuickFilter, Priority.ALWAYS);
    txtfldTagsQuickFilter.setMinWidth(60);
    txtfldTagsQuickFilter.setPrefWidth(Region.USE_COMPUTED_SIZE);
    txtfldTagsQuickFilter.textProperty().addListener((observable, oldValue, newValue) -> searchTags());
    txtfldTagsQuickFilter.setOnAction(event -> toggleCurrentTagsTagsFilter());
    txtfldTagsQuickFilter.setOnKeyReleased(event -> {
      if (event.getCode() == KeyCode.ESCAPE)
        txtfldTagsQuickFilter.clear();
    });

    btnRemoveTagsFilter.setGraphic(new ImageView(Constants.FilterDeleteIconPath));
    JavaFxLocalization.bindControlToolTip(btnRemoveTagsFilter, "button.remove.tags.filter.tool.tip");

    btnRemoveSelectedTag.setTextFill(Constants.RemoveEntityButtonTextColor);
    JavaFxLocalization.bindControlToolTip(btnRemoveSelectedTag, "delete.selected.tags.tool.tip");
    btnAddTag.setTextFill(Constants.AddEntityButtonTextColor);
    JavaFxLocalization.bindControlToolTip(btnAddTag, "add.new.tag.tool.tip");

    // TODO: isn't this setting listener twice?
    tblvwTags.selectionModelProperty().addListener(new ChangeListener<TableView.TableViewSelectionModel<Tag>>() {
      @Override
      public void changed(ObservableValue<? extends TableView.TableViewSelectionModel<Tag>> observable, TableView.TableViewSelectionModel<Tag> oldValue, TableView.TableViewSelectionModel<Tag> newValue) {
        tblvwTags.getSelectionModel().selectedItemProperty().addListener(tableViewTagsSelectedItemChangedListener);
      }
    });
    tblvwTags.getSelectionModel().selectedItemProperty().addListener(tableViewTagsSelectedItemChangedListener);

    tableViewTagsItems = new LazyLoadingObservableList<>();
    tblvwTags.setItems(tableViewTagsItems);

    tblvwTags.setOnKeyReleased(event -> {
      if (event.getCode() == KeyCode.DELETE)
        removeSelectedTags();
    });

    clmnTagName.setCellFactory(new Callback<TableColumn<Tag, String>, TableCell<Tag, String>>() {
      @Override
      public TableCell<Tag, String> call(TableColumn<Tag, String> param) {
        return new TagNameTableCell(TabTagsControl.this);
      }
    });

    clmnTagFilter.setText(null);
    ImageView columnFilterGraphic = new ImageView(Constants.FilterIconPath);
    Label columnFilterGraphicLabel = new Label(null, columnFilterGraphic); // wrap Image in a Label so that a Tooltip can be set
    columnFilterGraphicLabel.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    JavaFxLocalization.bindControlToolTip(columnFilterGraphicLabel, "filter.tags.tool.tip");
    clmnTagFilter.setGraphic(columnFilterGraphicLabel);
    clmnTagFilter.setCellFactory(new Callback<TableColumn<Tag, Boolean>, TableCell<Tag, Boolean>>() {
      @Override
      public TableCell<Tag, Boolean> call(TableColumn<Tag, Boolean> param) {
        final TagFilterTableCell cell = new TagFilterTableCell(TabTagsControl.this);
        tagFilterTableCells.add(cell);

        cell.isFilteredProperty().addListener(new ChangeListener<Boolean>() {
          @Override
          public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
            if(newValue == true)
              addTagToTagFilter(cell.getTag());
            else
              removeTagFromTagFilter(cell.getTag());

            setButtonRemoveTagsFilterDisabledState();
          }
        });

        return cell;
      }
    });
  }

  protected ChangeListener<Tag> tableViewTagsSelectedItemChangedListener = new ChangeListener<Tag>() {
    @Override
    public void changed(ObservableValue<? extends Tag> observable, Tag oldValue, Tag newValue) {
      log.debug("tblvwTags selected tag changed from {} to {}", oldValue, newValue);
      selectedTagChanged(newValue);
      log.debug("done tableViewTagsSelectedItemChangedListener");
    }
  };

  public void setSelectedTagToAllEntriesSystemTag() {
    selectedTagChanged(deepThought.AllEntriesSystemTag());
  }

  public void selectedTagChanged(Tag selectedTag) {
    log.debug("Selected Tag changed to {}", selectedTag);

    if(deepThought.getSettings().getLastViewedTag() != null)
      deepThought.getSettings().getLastViewedTag().removeEntityListener(selectedTagListener);

    deepThought.getSettings().setLastViewedTag(selectedTag);

    btnRemoveSelectedTag.setDisable(selectedTag == null || selectedTag instanceof SystemTag);

    if(selectedTag != null)
      selectedTag.addEntityListener(selectedTagListener);

    showEntriesForSelectedTag(selectedTag);
  }

  protected void showEntriesForSelectedTag(Tag tag) {
    log.debug("showEntriesForSelectedTag() has been called for Tag {}", tag);

    if(tag != null) {
      if (isTagsFilterApplied() && lastFilterTagsResult != null) {
        log.debug("Determining Entries in entriesHavingFilteredTags with Tag " + tag + " ...");

        Set<Entry> filteredEntriesWithThisTag = new TreeSet<>();
        for (Entry tagEntry : tag.getEntries()) {
          if (lastFilterTagsResult.getEntriesHavingFilteredTags().contains(tagEntry))
            filteredEntriesWithThisTag.add(tagEntry);
        }

        log.debug("done");
        mainWindowController.showEntries(filteredEntriesWithThisTag);
      }
      else
        mainWindowController.showEntries(tag.getEntries());
    }
    else
      mainWindowController.showEntries(new HashSet<Entry>());
  }


  public void searchForAllTags() {
    searchTags(FilterTagsSearch.EmptySearchTerm);
  }

  public void researchTagsWithLastSearchTerm() {
    searchTags(lastSearchTerm);
  }

  public void searchTags() {
    searchTags(txtfldTagsQuickFilter.getText());
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

      if(StringUtils.isNullOrEmpty(txtfldTagsQuickFilter.getText()) && allTagsSearchResult != null) {
        setTableViewTagsItems(allTagsSearchResult);
      }
      else {
        tagsSearch = new FilterTagsSearch(searchTerm, new SearchCompletedListener<FilterTagsSearchResults>() {
          @Override
          public void completed(FilterTagsSearchResults results) {
            Platform.runLater(() -> searchTagsCompleted(results));
          }
        });

        Application.getSearchEngine().filterTags(tagsSearch);
      }
    }
  }

  protected void searchTagsCompleted(FilterTagsSearchResults results) {
    lastTagsSearchResults = results;

    if(results.hasEmptySearchTerm()) {
      allTagsSearchResult = new CombinedLazyLoadingList<Tag>(systemTags, results.getRelevantMatchesSorted());
      setTableViewTagsItems(allTagsSearchResult);
      setSelectedTagToAllEntriesSystemTag();
    }
    else
      setTableViewTagsItems(results);

    callFilteredTagsChangedListeners(results);
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
            tblvwTags.getSelectionModel().select(tagToSelect);
        }
      });
    }
  }

  protected void addTagToTagFilter(Tag tag) {
    if(tagsFilter.contains(tag))
      return;

    tagsFilter.add(tag);
    filterTags(tag);

    tblvwTags.getSelectionModel().select(tag);
  }

  protected void removeTagFromTagFilter(Tag tag) {
    if(tagsFilter.contains(tag) == false)
      return;

    tagsFilter.remove(tag);
    filterTags(tag);

    tblvwTags.getSelectionModel().select(tag);
  }

  protected void toggleCurrentTagsTagsFilter() {
    if(tableViewTagsItems.size() == 0)
      clearTagFilter();
    else {
      for (Tag tag : tableViewTagsItems) {
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
    btnRemoveTagsFilter.setDisable(isTagsFilterApplied() == false);
  }

  protected void clearTagFilter() {
    tagsFilter.clear();
    lastFilterTagsResult = null;
    setButtonRemoveTagsFilterDisabledState();
    searchTags();
  }

  protected void showLastSearchResult() {
    if(lastTagsSearchResults.hasEmptySearchTerm() && allTagsSearchResult != null)
      setTableViewTagsItems(allTagsSearchResult);
    else
      setTableViewTagsItems(lastTagsSearchResults);
  }

  protected void setTableViewTagsItems(FilterTagsSearchResults results) {
    setTableViewTagsItems(results.getRelevantMatchesSorted());
    selectTagAccordingToSearchResult(results);
  }

  protected void setTableViewTagsItems(Collection<Tag> tags) {
//    tableViewTagsItems.setUnderlyingCollection(tags);
    tableViewTagsItems = new LazyLoadingObservableList<>(tags);
    tblvwTags.setItems(tableViewTagsItems);
  }

  protected void selectTagAccordingToSearchResult(FilterTagsSearchResults results) {
    selectTagAccordingToSearchResult(results, deepThought.getSettings().getLastViewedTag());
  }

  protected void selectTagAccordingToSearchResult(FilterTagsSearchResults results, Tag selectedTag) {
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


  protected void clearTableViewTagsItemsWithoutInvokingTableViewTagsSelectedItemChangedEvent() {
    log.debug("Calling clear() on tableViewTagsItems");
    tblvwTags.getSelectionModel().selectedItemProperty().removeListener(tableViewTagsSelectedItemChangedListener);
    tableViewTagsItems.clear();
    tblvwTags.getSelectionModel().selectedItemProperty().addListener(tableViewTagsSelectedItemChangedListener);
  }


  protected void tagHasBeenAdded(Tag tag) {
    allTagsSearchResult = null;
    searchTags();
  }

  protected void tagHasBeenRemoved(Tag tag) {
    allTagsSearchResult.remove(tag);
    tableViewTagsItems.remove(tag);
  }

  protected void sortTags() {
    log.debug("Going to sort sortedFilteredTags containing {} items", tableViewTagsItems.size());

    try {
      FXCollections.sort(tableViewTagsItems, tagComparator);
    } catch(Exception ex) {
      log.error("Could not sort Tags", ex);
    }
  }

  public boolean addFilteredTagsChangedListener(IFilteredTagsChangedListener listener) {
    return filteredTagsChangedListeners.add(listener);
  }

  public boolean removeFilteredTagsChangedListener(IFilteredTagsChangedListener listener) {
    return filteredTagsChangedListeners.remove(listener);
  }

  protected void callFilteredTagsChangedListeners(FilterTagsSearchResults results) {
    for(IFilteredTagsChangedListener listener : filteredTagsChangedListeners)
      listener.filteredTagsChanged(results);
  }


  protected Comparator<Tag> tagComparator = new Comparator<Tag>() {
    @Override
    public int compare(Tag tag1, Tag tag2) {
      if(tag1 == null || tag2 == null) {
        log.debug("This should actually never be the case, both tag's name are null");
        return 0;
      }
      if(tag1 == null || tag1.getName() == null) {
        log.debug("tag1 {} or its name is null", tag1);
        return -1;
      }
      if(tag2 == null || tag2.getName() == null) {
        log.debug("tag2 {} or its name is null", tag2);
        return 1;
      }

      if(tag1 instanceof SystemTag == true && tag2 instanceof SystemTag == false)
        return -1;
      else if(tag1 instanceof SystemTag == false && tag2 instanceof SystemTag == true)
        return 1;

      return tag1.getName().compareTo(tag2.getName());
    }
  };


  @FXML
  protected void handleButtonRemoveTagsFilterAction(ActionEvent event) {
    clearTagFilter();
  }

  @FXML
  protected void handleButtonRemoveSelectedTagsAction(ActionEvent event) {
    removeSelectedTags();
  }

  protected void removeSelectedTags() {
    List<Tag> selectedTags = new ArrayList<>(tblvwTags.getSelectionModel().getSelectedItems()); // make a copy as when multiple Tags are selected after removing the first one SelectionModel gets cleared
    for(Tag selectedTag : selectedTags) {
      if(selectedTag instanceof SystemTag == false)
        Alerts.deleteTagWithUserConfirmationIfIsSetOnEntries(deepThought, selectedTag);
    }
  }

  @FXML
  protected void handleButtonAddTagAction(ActionEvent event) {
    addNewTag();
  }

  protected void addNewTag() {
    Point2D buttonCoordinates = FXUtils.getNodeScreenCoordinates(btnAddTag);

    final double centerX = buttonCoordinates.getX() + btnAddTag.getWidth() / 2;
    final double y = buttonCoordinates.getY() + btnAddTag.getHeight() + 6;

    Dialogs.showEditTagDialog(new Tag(), centerX, y, getScene().getWindow(), true);
  }


  protected EntityListener deepThoughtListener = new EntityListener() {
    @Override
    public void propertyChanged(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {

    }

    @Override
    public void entityAddedToCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity) {
      if(collection == deepThought.getTags()){
        if(Platform.isFxApplicationThread())
          tagHasBeenAdded((Tag)addedEntity);
        else
          Platform.runLater(() -> tagHasBeenAdded((Tag)addedEntity));
      }
    }

    @Override
    public void entityOfCollectionUpdated(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity updatedEntity) {
      if(collection == deepThought.getTags()) {
        searchTags();
      }
    }

    @Override
    public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {
      if(collection == deepThought.getTags()) {
        if(Platform.isFxApplicationThread()) {
          tagHasBeenRemoved((Tag)removedEntity);
        }
        else {
          Platform.runLater(() -> {
            tagHasBeenRemoved((Tag)removedEntity);
          });
        }
      }
    }
  };

  protected EntityListener selectedTagListener = new EntityListener() {
    @Override
    public void propertyChanged(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {
      if(TableConfig.TagNameColumnName.equals(propertyName))
        sortTags();
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
//        if(filteredTags.size() > 0)
//          reapplyTagsFilter();
        if(tableViewTagsItems.size() > 0)
          filterTags();
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
//        if(isTagsFilterApplied())
//          reapplyTagsFilter();
        if(tableViewTagsItems.size() > 0)
          filterTags();
        showEntriesForSelectedTag((Tag)collectionHolder);
      }
    }
  };
}

package net.deepthought.controls.tabtags;

import net.deepthought.Application;
import net.deepthought.MainWindowController;
import net.deepthought.controls.FXUtils;
import net.deepthought.controls.IMainWindowControl;
import net.deepthought.data.model.DeepThought;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.Tag;
import net.deepthought.data.model.listener.EntityListener;
import net.deepthought.data.model.settings.enums.SelectedTab;
import net.deepthought.data.model.ui.SystemTag;
import net.deepthought.data.persistence.db.BaseEntity;
import net.deepthought.data.persistence.db.TableConfig;
import net.deepthought.data.search.FilterEntriesSearch;
import net.deepthought.data.search.Search;
import net.deepthought.data.search.SearchCompletedListener;
import net.deepthought.util.Alerts;
import net.deepthought.util.JavaFxLocalization;
import net.deepthought.util.StringUtils;

import org.controlsfx.control.textfield.CustomTextField;
import org.controlsfx.control.textfield.TextFields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

/**
 * Created by ganymed on 01/02/15.
 */
public class TabTagsControl extends VBox implements IMainWindowControl {

  private final static Logger log = LoggerFactory.getLogger(TabTagsControl.class);


  protected Entry entry = null;

  protected DeepThought deepThought = null;

  protected ObservableList<Tag> tableViewTagsItems = null;
  protected FilteredList<Tag> filteredTags = null;
  protected SortedList<Tag> sortedFilteredTags = null;
  protected ObservableList<TagFilterTableCell> tagFilterTableCells = FXCollections.observableArrayList();

  protected Search<Tag> filterTagsSearch = null;

  protected ObservableSet<Tag> tagsToFilterFor = FXCollections.observableSet();
  protected ObservableSet<Entry> entriesHavingFilteredTags = FXCollections.observableSet();
  protected Integer tagsToFilterForSize = 0;


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

    if(newDeepThought != null) {
      newDeepThought.addEntityListener(deepThoughtListener);

      showAllTagsInListViewTags(deepThought);

      if (deepThought.getSettings().getLastViewedTag() != null) {
        tblvwTags.getSelectionModel().select(deepThought.getSettings().getLastViewedTag());
      } else {
        tblvwTags.getSelectionModel().select(0);
      }
    }
  }

  public void clearData() {
    tableViewTagsItems.clear();
  }

  protected void setupControl() {
    // replace normal TextField txtfldTagsQuickFilter with a SearchTextField (with a cross to clear selection)
    hboxTagsBar.getChildren().remove(txtfldTagsQuickFilter);
    txtfldTagsQuickFilter = (CustomTextField) TextFields.createClearableTextField();
    hboxTagsBar.getChildren().add(1, txtfldTagsQuickFilter);
    HBox.setHgrow(txtfldTagsQuickFilter, Priority.ALWAYS);
    txtfldTagsQuickFilter.setPromptText("Quickly filter Tags");
    txtfldTagsQuickFilter.setPrefWidth(80);
    txtfldTagsQuickFilter.textProperty().addListener((observable, oldValue, newValue) -> quickFilterTags());
    txtfldTagsQuickFilter.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
      if (event.getCode() == KeyCode.ESCAPE)
        txtfldTagsQuickFilter.clear();
    });

    btnRemoveTagsFilter.setGraphic(new ImageView("icons/filter_delete_16x16.gif"));
    JavaFxLocalization.bindControlToolTip(btnRemoveTagsFilter, "button.remove.tags.filter.tool.tip");

    tblvwTags.selectionModelProperty().addListener(new ChangeListener<TableView.TableViewSelectionModel<Tag>>() {
      @Override
      public void changed(ObservableValue<? extends TableView.TableViewSelectionModel<Tag>> observable, TableView.TableViewSelectionModel<Tag> oldValue, TableView.TableViewSelectionModel<Tag> newValue) {
        tblvwTags.getSelectionModel().selectedItemProperty().addListener(tableViewTagsSelectedItemChangedListener);
      }
    });
    tblvwTags.getSelectionModel().selectedItemProperty().addListener(tableViewTagsSelectedItemChangedListener);

    tableViewTagsItems = FXCollections.observableList(tblvwTags.getItems());
    filteredTags = new FilteredList<>(tableViewTagsItems, tag -> true);
//    sortedFilteredTags = new SortedList<>(filteredTags, tagComparator);

//    tblvwTags.setItems(sortedFilteredTags);
    tblvwTags.setItems(filteredTags);

    clmnTagName.setCellFactory(new Callback<TableColumn<Tag, String>, TableCell<Tag, String>>() {
      @Override
      public TableCell<Tag, String> call(TableColumn<Tag, String> param) {
        return new TagNameTableCell();
      }
    });

    clmnTagFilter.setCellFactory(new Callback<TableColumn<Tag, Boolean>, TableCell<Tag, Boolean>>() {
      @Override
      public TableCell<Tag, Boolean> call(TableColumn<Tag, Boolean> param) {
        final TagFilterTableCell cell = new TagFilterTableCell(tagsToFilterFor);
        tagFilterTableCells.add(cell);

        cell.isFilteredProperty().addListener(new ChangeListener<Boolean>() {
          @Override
          public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
            if(newValue == true)
              addTagToTagFilter(cell.getTag());
            else
              removeTagFromTagFilter(cell.getTag());

            btnRemoveTagsFilter.setDisable(tagsToFilterFor.size() == 0);
          }
        });

        return cell;
      }
    });
  }

  protected ChangeListener<Tag> tableViewTagsSelectedItemChangedListener = new ChangeListener<Tag>() {
    @Override
    public void changed(ObservableValue<? extends Tag> observable, Tag oldValue, Tag newValue) {
      log.debug("tblvwTags selected tag changed from {} to {}, isAddingTag = {}", oldValue, newValue, isAddingTag);
      if(isAddingTag == false) {
        selectedTagChanged(newValue);
      }
      log.debug("done tableViewTagsSelectedItemChangedListener");
    }
  };

  public void setSelectedTagToAllEntriesSystemTag() {
    selectedTagChanged(deepThought.AllEntriesSystemTag());
  }

  public void selectedTagChanged(Tag selectedTag) {
//    if(selectedTag == deepThought.getSettings().getLastViewedTag())
//      return;
//    if(selectedTag == tblvwTags.getSelectionModel().getSelectedItem()) // this Tag is already selected, nothing to do
//      return;

    log.debug("Selected Tag changed to {}", selectedTag);

    if(deepThought.getSettings().getLastViewedTag() != null)
      deepThought.getSettings().getLastViewedTag().removeEntityListener(selectedTagListener);

//    if(selectedTag instanceof SystemTag)
//      deepThought.getSettings().setLastViewedTag(null); // TODO: i think setting to null is a bad solution
//    else
      deepThought.getSettings().setLastViewedTag(selectedTag);

    btnRemoveSelectedTag.setDisable(selectedTag == null || selectedTag instanceof SystemTag);

    if(selectedTag != null)
      selectedTag.addEntityListener(selectedTagListener);

    showEntriesForSelectedTag(selectedTag);
  }

  protected void showEntriesForSelectedTag(Tag tag) {
    log.debug("showEntriesForSelectedTag() has been called for Tag {}", tag);

    if(tag != null) {
      if (tagsToFilterFor.size() > 0) {
        log.debug("Determining Entries in entriesHavingFilteredTags with Tag " + tag + " ...");
//      tagsToFilterForSize = tagsToFilterFor.size();
        Set<Entry> filteredEntriesWithThisTag = new TreeSet<>();
        for (Entry tagEntry : tag.getEntries()) {
          if (tagEntry.hasTags(tagsToFilterFor))
            filteredEntriesWithThisTag.add(tagEntry);
        }
//      for(Entry filteredEntry : entriesHavingFilteredTags) {
//        if(filteredEntry.hasTag(tag))
//          filteredEntriesWithThisTag.add(filteredEntry);
//      }

        log.debug("done");
        mainWindowController.showEntries(filteredEntriesWithThisTag);
      }
      else
        mainWindowController.showEntries(tag.getEntries());
    }
    else
      mainWindowController.showEntries(new HashSet<Entry>());
  }


  protected void showAllTagsInListViewTags(DeepThought deepThought) {
    clearTableViewTagsItemsWithoutInvokingTableViewTagsSelectedItemChangedEvent();
    log.debug("Adding system tags ...");

//    if(tableViewTagsItems.contains(deepThought.AllEntriesSystemTag()) == false)
      tableViewTagsItems.add(deepThought.AllEntriesSystemTag());
//    if(tableViewTagsItems.contains(deepThought.EntriesWithoutTagsSystemTag()) == false)
      tableViewTagsItems.add(deepThought.EntriesWithoutTagsSystemTag());

//    tableViewTagsItems.addAll(deepThought.getTags());
    tableViewTagsItems.addAll(deepThought.getSortedTags());

//    tblvwTags.getSelectionModel().select(deepThought.getSettings().getLastViewedTag());
  }

  protected void quickFilterTags() {
    log.debug("Starting to quick filter Tags ... ");
    if(filterTagsSearch != null)
      filterTagsSearch.interrupt();

    final Tag selectedTag = deepThought.getSettings().getLastViewedTag();


    if(tagsToFilterFor.size() == 0 && StringUtils.isNullOrEmpty(txtfldTagsQuickFilter.getText())) {
      showAllTagsInListViewTags(deepThought);
      filteredTags.setPredicate((tag) -> true);
    }
    else {
      filterTagsSearch = new Search<Tag>(txtfldTagsQuickFilter.getText(), new SearchCompletedListener<Tag>() {
        @Override
        public void completed(final Collection<Tag> results) {
          filteredTags.setPredicate((tag) -> results.contains(tag));

          if (results.contains(selectedTag))
            tblvwTags.getSelectionModel().select(selectedTag);

          log.debug("Done quick filtering Tags");
        }
      });
      Application.getSearchEngine().filterTags(filterTagsSearch);
    }

//    quickFilterTagsManually();
//
//    if(filteredTags.contains(selectedTag))
//      tblvwTags.getSelectionModel().select(selectedTag);
//
//    log.debug("Done quick filtering Tags");
  }

//  protected void quickFilterTagsManually() {
//    String filter = txtfldTagsQuickFilter.getText();
//    String lowerCaseFilter = filter.toLowerCase(); // TODO: can filter ever be null?
//    String[] parts = lowerCaseFilter.split(",");
//
//    filteredTags.setPredicate((tag) -> {
//      // If filter text is empty, display all Tags.
//      if (/*filter == null || */filter.isEmpty()) {
//        return true;
//      }
//
//      if (tag instanceof SystemTag)
//        return false;
//
//      String lowerCaseTagName = tag.getName().toLowerCase();
//
//      for (String part : parts) {
//        if (lowerCaseTagName.contains(part.trim())) {
//          return true; // Filter matches Tag's name
//        }
//      }
//
//      return false; // Does not match.
//    });
//  }

  protected void addTagToTagFilter(Tag tag) {
    if(tagsToFilterFor.contains(tag))
      return;

    tagsToFilterFor.add(tag);
    reapplyTagsFilter();

    tblvwTags.getSelectionModel().select(tag);
  }

  protected void removeTagFromTagFilter(Tag tag) {
    if(tagsToFilterFor.contains(tag) == false)
      return;

    tagsToFilterFor.remove(tag);
    reapplyTagsFilter();

    tblvwTags.getSelectionModel().select(tag);
  }

  protected void clearTagFilter() {
    for(TagFilterTableCell cell : tagFilterTableCells) {
      cell.uncheck();
    }

    tagsToFilterFor.clear();

    showAllTagsInListViewTags(deepThought);

    quickFilterTags();
  }

  protected void reapplyTagsFilter() {
    log.debug("Applying Tags filter for {} tags to filter ... ", tagsToFilterFor.size());
    entriesHavingFilteredTags.clear();

    if(tagsToFilterFor.size() == 0) {
      showAllTagsInListViewTags(deepThought);
    }
    else {
      final Set<Tag> tagsOnEntriesContainingFilteredTags = new HashSet<>();
      tagsOnEntriesContainingFilteredTags.addAll(tagsToFilterFor);

      Application.getSearchEngine().findAllEntriesHavingTheseTags(tagsToFilterFor, entriesHavingFilteredTags, tagsOnEntriesContainingFilteredTags);

      clearTableViewTagsItemsWithoutInvokingTableViewTagsSelectedItemChangedEvent();
      log.debug("Adding all tagsOnEntriesContainingFilteredTags");
      tableViewTagsItems.addAll(tagsOnEntriesContainingFilteredTags);
    }

    quickFilterTags();
    log.debug("Done applying Tags filter");
  }

  protected void clearTableViewTagsItemsWithoutInvokingTableViewTagsSelectedItemChangedEvent() {
    log.debug("Calling clear() on tableViewTagsItems");
    tblvwTags.getSelectionModel().selectedItemProperty().removeListener(tableViewTagsSelectedItemChangedListener);
    tableViewTagsItems.clear();
    tblvwTags.getSelectionModel().selectedItemProperty().addListener(tableViewTagsSelectedItemChangedListener);
  }


  protected boolean isAddingTag = false;

  protected void tagHasBeenAdded(Tag tag) {
//    if(tagsToFilterFor.size() == 0) {
      isAddingTag = true;
      tableViewTagsItems.add(tag);
      isAddingTag = false;
//    }

//    sortTags();
  }

  protected void tagHasBeenRemoved(Tag tag) {
    tableViewTagsItems.remove(tag);

//    sortTags();
  }

  protected void sortTags() {
    log.debug("Going to sort sortedFilteredTags containing {} items", tableViewTagsItems.size());

    try {
      FXCollections.sort(tableViewTagsItems, tagComparator);
    } catch(Exception ex) {
      log.error("Could not sort Tags", ex);
    }
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
    Tag selectedTag = tblvwTags.getSelectionModel().getSelectedItem();
    if(selectedTag == null)
      return;

    Alerts.deleteTagWithUserConfirmationIfIsSetOnEntries(deepThought, selectedTag);
  }

  @FXML
  protected void handleButtonAddTagAction(ActionEvent event) {
    addNewTag("New tag");
  }

  protected Tag addNewTag(String name) {
    Tag newTag = new Tag(name);
    deepThought.addTag(newTag);

    return newTag;
  }


  protected EntityListener entryListener = new EntityListener() {
    @Override
    public void propertyChanged(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {

    }

    @Override
    public void entityAddedToCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity) {

    }

    @Override
    public void entityOfCollectionUpdated(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity updatedEntity) {

    }

    @Override
    public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {

    }
  };

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
//        FXCollections.sort(tableViewTagsItems);
        quickFilterTags();
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
        if(filteredTags.size() > 0)
          reapplyTagsFilter();
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
        if(filteredTags.size() > 0)
          reapplyTagsFilter();
        showEntriesForSelectedTag((Tag)collectionHolder);
      }
    }
  };
}

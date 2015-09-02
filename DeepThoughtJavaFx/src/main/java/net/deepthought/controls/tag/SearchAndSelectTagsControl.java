package net.deepthought.controls.tag;

import net.deepthought.Application;
import net.deepthought.controls.ICleanableControl;
import net.deepthought.data.listener.ApplicationListener;
import net.deepthought.data.model.DeepThought;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.Tag;
import net.deepthought.data.model.listener.EntityListener;
import net.deepthought.data.persistence.db.BaseEntity;
import net.deepthought.data.search.specific.FilterTagsSearch;
import net.deepthought.data.search.specific.FilterTagsSearchResult;
import net.deepthought.data.search.specific.FilterTagsSearchResults;
import net.deepthought.data.search.SearchCompletedListener;
import net.deepthought.util.JavaFxLocalization;
import net.deepthought.util.Localization;
import net.deepthought.util.Notification;
import net.deepthought.util.StringUtils;

import org.controlsfx.control.textfield.TextFields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Created by ganymed on 01/02/15.
 */
public class SearchAndSelectTagsControl extends VBox implements ICleanableControl {

  private final static Logger log = LoggerFactory.getLogger(SearchAndSelectTagsControl.class);


  protected Entry entry = null;
  protected IEditedTagsHolder editedTagsHolder = null;

  protected DeepThought deepThought = null;

  protected ObservableList<Tag> listViewAllTagsItems = null;
  protected FilteredList<Tag> filteredTags = null;
  protected SortedList<Tag> sortedFilteredTags = null;

  protected FilterTagsSearch filterTagsSearch = null;
  protected FilterTagsSearchResults lastFilterTagsResults = FilterTagsSearchResults.NoFilterSearchResults;
  protected List<IFilteredTagsChangedListener> filteredTagsChangedListeners = new ArrayList<>();

  protected List<TagListCell> tagListCells = new ArrayList<>();


  @FXML
  protected Pane pnContent;
  @FXML
  protected Pane pnFilterTags;
  @FXML
  protected TextField txtfldFilterTags;
  @FXML
  protected Button btnCreateTag;
  @FXML
  protected ListView<Tag> lstvwAllTags;


  public SearchAndSelectTagsControl(Entry entry, IEditedTagsHolder editedTagsHolder) {
    this.entry = entry;
    this.editedTagsHolder = editedTagsHolder;

    deepThought = Application.getDeepThought();

    Application.addApplicationListener(applicationListener);

    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("controls/SearchAndSelectTagsControl.fxml"));
    fxmlLoader.setRoot(this);
    fxmlLoader.setController(this);
    fxmlLoader.setResources(Localization.getStringsResourceBundle());

    try {
      fxmlLoader.load();
      setupControl();

      if(deepThought != null)
        deepThought.addEntityListener(deepThoughtListener);
    } catch (IOException ex) {
      log.error("Could not load SearchAndSelectTagsControl", ex);
    }
  }

  protected ApplicationListener applicationListener = new ApplicationListener() {
    @Override
    public void deepThoughtChanged(DeepThought deepThought) {
      SearchAndSelectTagsControl.this.deepThoughtChanged(deepThought);
    }

    @Override
    public void notification(Notification notification) {

    }
  };


  @Override
  public void cleanUpControl() {
    Application.removeApplicationListener(applicationListener);

    if(deepThought != null)
      deepThought.removeEntityListener(deepThoughtListener);

    editedTagsHolder = null;

    filteredTagsChangedListeners.clear();

    filterTagsSearch = null;
    lastFilterTagsResults = null;

    for(TagListCell cell : tagListCells)
      cell.cleanUpControl();
    tagListCells.clear();
  }

  protected void deepThoughtChanged(DeepThought newDeepThought) {
    if(this.deepThought != null)
      this.deepThought.removeEntityListener(deepThoughtListener);

    this.deepThought = newDeepThought;

    listViewAllTagsItems.clear();

    if(newDeepThought != null) {
      newDeepThought.addEntityListener(deepThoughtListener);
      listViewAllTagsItems.addAll(deepThought.getTags());
    }
  }

  protected void setupControl() {
    // replace normal TextField txtfldFilterCategories with a SearchTextField (with a cross to clear selection)
    pnFilterTags.getChildren().remove(txtfldFilterTags);
    txtfldFilterTags = TextFields.createClearableTextField();
    pnFilterTags.getChildren().add(0, txtfldFilterTags);
    HBox.setHgrow(txtfldFilterTags, Priority.ALWAYS);
    JavaFxLocalization.bindTextInputControlPromptText(txtfldFilterTags, "find.tags.to.add");

    lstvwAllTags.setCellFactory(listView -> {
      TagListCell cell = new TagListCell(this, editedTagsHolder);
      tagListCells.add(cell);
      return cell;
    });

    listViewAllTagsItems = lstvwAllTags.getItems();
    if(deepThought != null)
      listViewAllTagsItems.addAll(deepThought.getTags());
    filteredTags = new FilteredList<>(listViewAllTagsItems, tag -> true);
    sortedFilteredTags = new SortedList<>(filteredTags, tagComparator);
    lstvwAllTags.setItems(sortedFilteredTags);

    txtfldFilterTags.textProperty().addListener((observable, oldValue, newValue) -> filterTags(newValue));
    txtfldFilterTags.setOnAction(event -> createNewTagOrToggleTagsAffiliation());
    txtfldFilterTags.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
      if(event.getCode() == KeyCode.ESCAPE) {
        txtfldFilterTags.clear();
        event.consume();
      }
    });
  }


  protected void setControlsForEnteredTagsFilter(String tagsFilter) {
    if(tagsFilter.contains(",")) {
      JavaFxLocalization.bindLabeledText(btnCreateTag, "toggle");
    }
    else {
      JavaFxLocalization.bindLabeledText(btnCreateTag, "new...");
    }

    btnCreateTag.setDisable(StringUtils.isNullOrEmpty(tagsFilter));
  }

  protected void filterTags(final String tagsFilter) {
    if(filterTagsSearch != null)
      filterTagsSearch.interrupt();

     btnCreateTag.setDisable(false);

    if(StringUtils.isNullOrEmpty(tagsFilter)) {
      lastFilterTagsResults = FilterTagsSearchResults.NoFilterSearchResults;
      filteredTags.setPredicate((tag) -> true);
      setControlsForEnteredTagsFilter(tagsFilter);
      callFilteredTagsChangedListeners(lastFilterTagsResults);
    }
    else {
      filterTagsSearch = new FilterTagsSearch(tagsFilter, new SearchCompletedListener<FilterTagsSearchResults>() {
        @Override
        public void completed(final FilterTagsSearchResults results) {
          Platform.runLater(() -> {
            lastFilterTagsResults = results;
            filteredTags.setPredicate((tag) -> results.isRelevantMatch(tag));

            if(results.getResults().size() > 0 && results.getLastResult().hasExactMatch())
              lstvwAllTags.scrollTo(results.getLastResult().getExactMatch());

            if(tagsFilter.contains(",") == false && results.getResults().size() == 1 && results.getExactMatches().size() == 1)
              btnCreateTag.setDisable(true);

            setControlsForEnteredTagsFilter(tagsFilter);
            callFilteredTagsChangedListeners(results);
          });
        }
      });

      Application.getSearchEngine().filterTags(filterTagsSearch);
    }
  }

  protected void createNewTagOrToggleTagsAffiliation() {
    if(lastFilterTagsResults.getResults().size() == 0 || (lastFilterTagsResults.getResults().size() == 1 &&
        lastFilterTagsResults.getExactMatches().size() == 0 && lastFilterTagsResults.getOverAllSearchTerm().endsWith(",") == false))
      addNewTagToEntry();
    else {
      for(FilterTagsSearchResult result : lastFilterTagsResults.getResults()) {
        if(result.hasExactMatch())
          toggleTagAffiliation(result.getExactMatch());
        else {
//          addNewTagToEntry(result.getSearchTerm());
          for (Tag match : result.getAllMatches())
            toggleTagAffiliation(match);
        }
      }
    }
  }

  protected void toggleTagAffiliation(Tag tag) {
    if(tag == null)
      return;

    if(editedTagsHolder.containsEditedTag(tag) == false)
      editedTagsHolder.addTagToEntry(tag);
    else
      editedTagsHolder.removeTagFromEntry(tag);
  }

  protected void addNewTagToEntry() {
    addNewTagToEntry(txtfldFilterTags.getText());
  }

  protected void addNewTagToEntry(String tagName) {
    Tag newTag = new Tag(tagName);
    Application.getDeepThought().addTag(newTag);

    editedTagsHolder.addTagToEntry(newTag);

    btnCreateTag.setDisable(true);
  }

  protected Comparator<Tag> tagComparator = new Comparator<Tag>() {
    @Override
    public int compare(Tag tag1, Tag tag2) {
      if(tag1 == null && tag2 == null) {
//        log.debug("This should actually never be the case, both tag's name are null");
        return 0;
      }
      if(tag1 == null || tag1.getName() == null) {
//        log.debug("tag1 {} or its name is null", tag1);
        return -1;
      }
      if(tag2 == null || tag2.getName() == null) {
//        log.debug("tag2 {} or its name is null", tag2);
        return 1;
      }

      return tag1.compareTo(tag2);
    }
  };


  public void setEntry(Entry entry) {
//    txtfldFilterTags.clear();
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


  @FXML
  public void handleButtonCreateTagAction(ActionEvent event) {
    createNewTagOrToggleTagsAffiliation();
  }


  protected EntityListener deepThoughtListener = new EntityListener() {
    @Override
    public void propertyChanged(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {

    }

    @Override
    public void entityAddedToCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity) {
      checkIfTagsHaveBeenUpdated(collectionHolder, addedEntity);
    }

    @Override
    public void entityOfCollectionUpdated(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity updatedEntity) {
      checkIfTagsHaveBeenUpdated(collectionHolder, updatedEntity);
    }

    @Override
    public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {
      checkIfTagsHaveBeenUpdated(collectionHolder, removedEntity);
    }
  };

  protected void checkIfTagsHaveBeenUpdated(BaseEntity collectionHolder, BaseEntity entity) {
    if(collectionHolder instanceof DeepThought && entity instanceof Tag) {
      Tag tag = (Tag)entity;

      DeepThought deepThought = (DeepThought)collectionHolder;
      resetListViewAllTagsItemsThreadSafe(deepThought);
    }
  }

  protected void resetListViewAllTagsItemsThreadSafe(final DeepThought deepThought) {
    if(Platform.isFxApplicationThread())
      resetListViewAllTagsItems(deepThought);
    else
      Platform.runLater(() -> resetListViewAllTagsItems(deepThought));
  }

  protected void resetListViewAllTagsItems(DeepThought deepThought) {
    listViewAllTagsItems.clear();
    listViewAllTagsItems.addAll(deepThought.getTags());
    filterTags(txtfldFilterTags.getText());
  }

}

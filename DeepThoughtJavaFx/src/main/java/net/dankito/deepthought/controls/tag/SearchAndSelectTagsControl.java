package net.dankito.deepthought.controls.tag;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.controls.ICleanUp;
import net.dankito.deepthought.controls.LazyLoadingObservableList;
import net.dankito.deepthought.controls.utils.FXUtils;
import net.dankito.deepthought.controls.utils.IEditedEntitiesHolder;
import net.dankito.deepthought.data.listener.ApplicationListener;
import net.dankito.deepthought.data.model.DeepThought;
import net.dankito.deepthought.data.model.Tag;
import net.dankito.deepthought.data.model.listener.EntityListener;
import net.dankito.deepthought.data.persistence.db.BaseEntity;
import net.dankito.deepthought.data.search.SearchCompletedListener;
import net.dankito.deepthought.data.search.specific.TagsSearch;
import net.dankito.deepthought.data.search.specific.TagsSearchResult;
import net.dankito.deepthought.data.search.specific.TagsSearchResults;
import net.dankito.deepthought.util.Alerts;
import net.dankito.deepthought.util.Notification;
import net.dankito.deepthought.util.NotificationType;
import net.dankito.deepthought.util.StringUtils;
import net.dankito.deepthought.util.localization.JavaFxLocalization;

import org.controlsfx.control.textfield.TextFields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Created by ganymed on 01/02/15.
 */
public class SearchAndSelectTagsControl extends VBox implements ICleanUp {

  private final static Logger log = LoggerFactory.getLogger(SearchAndSelectTagsControl.class);


  protected IEditedEntitiesHolder<Tag> editedTagsHolder = null;

  protected DeepThought deepThought = null;

  protected LazyLoadingObservableList<Tag> listViewTagsItems = null;

  protected TagsSearch lastTagsSearch = null;
  protected TagsSearchResults lastSearchResults = TagsSearchResults.EmptySearchResults;
  protected List<net.dankito.deepthought.controls.tag.IDisplayedTagsChangedListener> displayedTagsChangedListeners = new ArrayList<>();

  protected List<TagListCell> tagListCells = new ArrayList<>();


  @FXML
  protected Pane pnContent;
  @FXML
  protected Pane pnSearchTags;
  @FXML
  protected TextField txtfldSearchTags;
  @FXML
  protected Button btnCreateTag;
  @FXML
  protected ListView<Tag> lstvwTags;


  public SearchAndSelectTagsControl(IEditedEntitiesHolder editedTagsHolder) {
    this.editedTagsHolder = editedTagsHolder;

    Application.addApplicationListener(applicationListener);

    if(FXUtils.loadControl(this, "SearchAndSelectTagsControl")) {
      setupControl();

      if(Application.getDeepThought() != null && this.deepThought == null) {
        deepThoughtChanged(Application.getDeepThought());
      }
    }
  }

  protected ApplicationListener applicationListener = new ApplicationListener() {
    @Override
    public void deepThoughtChanged(DeepThought deepThought) {
      SearchAndSelectTagsControl.this.deepThoughtChanged(deepThought);
    }

    @Override
    public void notification(Notification notification) {
      if(notification.getType() == NotificationType.ApplicationInstantiated)
        searchTags();
    }
  };


  @Override
  public void cleanUp() {
    Application.removeApplicationListener(applicationListener);

    if(deepThought != null)
      deepThought.removeEntityListener(deepThoughtListener);

    editedTagsHolder = null;

    displayedTagsChangedListeners.clear();

    lastTagsSearch = null;
    lastSearchResults = null;

    listViewTagsItems.clear();

    for(TagListCell cell : tagListCells)
      cell.cleanUp();
    tagListCells.clear();
  }

  protected void deepThoughtChanged(DeepThought newDeepThought) {
    if(this.deepThought != null)
      this.deepThought.removeEntityListener(deepThoughtListener);

    this.deepThought = newDeepThought;

    listViewTagsItems.clear();

    if(newDeepThought != null) {
      newDeepThought.addEntityListener(deepThoughtListener);
    }

    if(Application.isInstantiated())
      searchTags();
  }

  protected void setupControl() {
    // replace normal TextField txtfldSearchCategories with a SearchTextField (with a cross to clear selection)
    pnSearchTags.getChildren().remove(txtfldSearchTags);
    txtfldSearchTags = TextFields.createClearableTextField();
    txtfldSearchTags.setId("txtfldSearchTags");
    pnSearchTags.getChildren().add(0, txtfldSearchTags);
    HBox.setHgrow(txtfldSearchTags, Priority.ALWAYS);
    JavaFxLocalization.bindTextInputControlPromptText(txtfldSearchTags, "search.tags.prompt.text");

    lstvwTags.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    lstvwTags.setOnKeyPressed(event -> {
      if (event.getCode() == KeyCode.ENTER) {
        toggleSelectedTagsAffiliation();
        event.consume();
      } else if (event.getCode() == KeyCode.DELETE) {
        deleteSelectedTags();
        event.consume();
      }
    });

    lstvwTags.setCellFactory(listView -> {
      TagListCell cell = new TagListCell(this, editedTagsHolder);
      tagListCells.add(cell);
      return cell;
    });

    listViewTagsItems = new LazyLoadingObservableList<>();
    lstvwTags.setItems(listViewTagsItems);

    txtfldSearchTags.textProperty().addListener((observable, oldValue, newValue) -> searchTags(newValue));
    txtfldSearchTags.setOnAction(event -> createNewTagOrToggleTagsAffiliation());
    txtfldSearchTags.setOnKeyReleased(event -> {
      if (event.getCode() == KeyCode.ESCAPE) {
        txtfldSearchTags.clear();
        event.consume();
      }
    });
  }


  protected void setControlsForEnteredSearchTerm(String searchTerm) {
    if(searchTerm.contains(",")) {
      JavaFxLocalization.bindLabeledText(btnCreateTag, "toggle");
    }
    else {
      JavaFxLocalization.bindLabeledText(btnCreateTag, "new...");
    }

    btnCreateTag.setDisable(StringUtils.isNullOrEmpty(searchTerm));
  }

  protected void searchTags() {
    searchTags(txtfldSearchTags.getText());
  }

  protected void searchTags(final String searchTerm) {
    if(lastTagsSearch != null)
      lastTagsSearch.interrupt();

     btnCreateTag.setDisable(false);

    lastTagsSearch = new TagsSearch(searchTerm, new SearchCompletedListener<TagsSearchResults>() {
      @Override
      public void completed(final TagsSearchResults results) {
        Platform.runLater(() -> {
          lastSearchResults = results;
          listViewTagsItems.setUnderlyingCollection(results.getRelevantMatchesSorted());

          if(results.getResults().size() > 0 && results.getLastResult().hasExactMatch())
            lstvwTags.scrollTo(results.getLastResult().getExactMatch());

          if(searchTerm.contains(",") == false && results.getResults().size() == 1 && results.getExactMatches().size() == 1)
            btnCreateTag.setDisable(true);

          setControlsForEnteredSearchTerm(searchTerm);
          callDisplayedTagsChangedListeners(results);
        });
      }
    });

    Application.getSearchEngine().searchTags(lastTagsSearch);
  }

  protected void createNewTagOrToggleTagsAffiliation() {
    if(lastSearchResults.getResults().size() == 0 || (lastSearchResults.getResults().size() == 1 &&
        lastSearchResults.getExactMatches().size() == 0 && lastSearchResults.getOverAllSearchTerm().endsWith(",") == false))
      addNewTagToEntry();
    else {
      for(TagsSearchResult result : lastSearchResults.getResults()) {
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

    if(editedTagsHolder.containsEditedEntity(tag) == false)
      editedTagsHolder.addEntityToEntry(tag);
    else
      editedTagsHolder.removeEntityFromEntry(tag);
  }

  protected void toggleSelectedTagsAffiliation() {
    for(Tag selectedTag : getSelectedTags()) {
      toggleTagAffiliation(selectedTag);
    }
  }

  protected void deleteSelectedTags() {
    for(Tag selectedTag : getSelectedTags()) {
      if(Alerts.deleteTagWithUserConfirmationIfIsSetOnEntries(deepThought, selectedTag)) {
        if(editedTagsHolder != null && editedTagsHolder.containsEditedEntity(selectedTag))
          editedTagsHolder.removeEntityFromEntry(selectedTag);
      }
    }
  }

  protected Collection<Tag> getSelectedTags() {
    return new ArrayList<>(lstvwTags.getSelectionModel().getSelectedItems()); // make a copy as when multiple Tags are selected after removing first one SelectionModel gets cleared
  }


  protected void addNewTagToEntry() {
    addNewTagToEntry(txtfldSearchTags.getText());
  }

  protected void addNewTagToEntry(String tagName) {
    Tag newTag = new Tag(tagName);
    Application.getDeepThought().addTag(newTag);

    editedTagsHolder.addEntityToEntry(newTag);

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


  public boolean addDisplayedTagsChangedListener(net.dankito.deepthought.controls.tag.IDisplayedTagsChangedListener listener) {
    return displayedTagsChangedListeners.add(listener);
  }

  public boolean removeDisplayedTagsChangedListener(net.dankito.deepthought.controls.tag.IDisplayedTagsChangedListener listener) {
    return displayedTagsChangedListeners.remove(listener);
  }

  protected void callDisplayedTagsChangedListeners(TagsSearchResults results) {
    for(net.dankito.deepthought.controls.tag.IDisplayedTagsChangedListener listener : displayedTagsChangedListeners)
      listener.displayedTagsChanged(results);
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
    FXUtils.runOnUiThread(() -> resetListViewAllTagsItems(deepThought));
  }

  protected void resetListViewAllTagsItems(DeepThought deepThought) {
    listViewTagsItems.clear();
    searchTags();
  }

}

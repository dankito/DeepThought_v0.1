package net.deepthought.controls.tag;

import net.deepthought.Application;
import net.deepthought.controls.event.EntryTagsEditedEvent;
import net.deepthought.data.listener.ApplicationListener;
import net.deepthought.data.model.DeepThought;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.Tag;
import net.deepthought.data.model.listener.EntityListener;
import net.deepthought.data.persistence.db.BaseEntity;
import net.deepthought.data.search.FilterTagsSearch;
import net.deepthought.data.search.FilterTagsSearchResults;
import net.deepthought.data.search.SearchCompletedListener;
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
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;

/**
 * Created by ganymed on 01/02/15.
 */
public class EntryTagsControl extends TitledPane {

  private final static Logger log = LoggerFactory.getLogger(EntryTagsControl.class);


  protected Entry entry = null;
  protected boolean hasEntryBeenSetBefore = false;

  protected DeepThought deepThought = null;

  protected ObservableSet<Tag> editedTags = FXCollections.observableSet();
  protected Set<Tag> addedTags = new HashSet<>();
  protected Set<Tag> removedTags = new HashSet<>();

  protected ObservableList<Tag> listViewAllTagsItems = null;
  protected FilteredList<Tag> filteredTags = null;
  protected SortedList<Tag> sortedFilteredTags = null;

  protected List<TagListCell> tagListCells = new ArrayList<>();

  protected FilterTagsSearch filterTagsSearch = null;
  protected List<IFilteredTagsChangedListener> filteredTagsChangedListeners = new ArrayList<>();

  protected EventHandler<EntryTagsEditedEvent> tagAddedEventHandler = null;
  protected EventHandler<EntryTagsEditedEvent> tagRemovedEventHandler = null;


  @FXML
  protected Pane pnGraphicsPane;
  @FXML
  protected Label lblTags;
  @FXML
  protected FlowPane pnSelectedTagsPreview;

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


  public EntryTagsControl() {
    this(null);
  }

  public EntryTagsControl(Entry entry) {
    this.entry = entry;
    if(entry != null) {
      editedTags.addAll(entry.getTags());
      entry.addEntityListener(entryListener);
    }

    setDisable(entry == null);
    deepThought = Application.getDeepThought();

    Application.addApplicationListener(new ApplicationListener() {
      @Override
      public void deepThoughtChanged(DeepThought deepThought) {
        EntryTagsControl.this.deepThoughtChanged(deepThought);
      }

      @Override
      public void notification(Notification notification) {

      }
    });

    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("controls/EntryTagsControl.fxml"));
    fxmlLoader.setRoot(this);
    fxmlLoader.setController(this);
    fxmlLoader.setResources(Localization.getStringsResourceBundle());

    try {
      fxmlLoader.load();
      setupControl();

      if(deepThought != null)
        deepThought.addEntityListener(deepThoughtListener);
    } catch (IOException ex) {
      log.error("Could not load EntryTagsControl", ex);
    }
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
    this.setExpanded(false);

    pnContent.setPrefHeight(175);

    // replace normal TextField txtfldFilterCategories with a SearchTextField (with a cross to clear selection)
    pnFilterTags.getChildren().remove(txtfldFilterTags);
    txtfldFilterTags = TextFields.createClearableTextField();
    pnFilterTags.getChildren().add(0, txtfldFilterTags);
    HBox.setHgrow(txtfldFilterTags, Priority.ALWAYS);
    txtfldFilterTags.setPromptText("Find tags to add");

    lstvwAllTags.setCellFactory(listView -> {
      TagListCell cell = new TagListCell(entry, this);
      tagListCells.add(cell);
      return cell;
    });

    listViewAllTagsItems = lstvwAllTags.getItems();
    if(deepThought != null)
      listViewAllTagsItems.addAll(deepThought.getTags());
    filteredTags = new FilteredList<>(listViewAllTagsItems, tag -> true);
    sortedFilteredTags = new SortedList<>(filteredTags, tagComparator);
    lstvwAllTags.setItems(sortedFilteredTags);

    txtfldFilterTags.textProperty().addListener(new ChangeListener<String>() {
      @Override
      public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        setControlsForEnteredTagsFilter(newValue);
      }
    });
    txtfldFilterTags.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        enterHasBeenPressedInTextFieldFilterTags();
      }
    });
    txtfldFilterTags.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
      if(event.getCode() == KeyCode.ESCAPE) {
        txtfldFilterTags.clear();
        event.consume();
      }
    });

    showEntryTags(entry);

//    this.sceneProperty().addListener(new ChangeListener<Scene>() {
//      @Override
//      public void changed(ObservableValue<? extends Scene> observable, Scene oldValue, Scene newValue) {
//        newValue.getWindow().widthProperty().addListener(new ChangeListener<Number>() {
//          @Override
//          public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
//            pnGraphicsPane.setPrefWidth(newValue.doubleValue() - 50);
//          }
//        });
//      }
//    });

//    widthProperty().addListener((observable, oldValue, newValue) -> {
//      pnSelectedTagsPreview.setPrefWrapLength(newValue.doubleValue() - lblTags.getWidth() - 50);
//    });

    // before PrefWrapLength was set to 200, so it wrapped very early and used a lot of lines if an Entry had many Tags. This now fits the wrap length to EntryTagsControl's width
    this.widthProperty().addListener((observable, oldValue, newValue) -> {
      if(lblTags.getWidth() > 0)
        pnSelectedTagsPreview.setPrefWrapLength(this.getWidth() - lblTags.getWidth() - 60);
    });
    lblTags.widthProperty().addListener((observable, oldValue, newValue) -> pnSelectedTagsPreview.setPrefWrapLength(this.getWidth() - lblTags.getWidth() - 60));
  }

  protected void showEntryTags(Entry entry) {
//    if(hasEntryBeenSetBefore == false) { // doing this in setupControls() is too early as Dialog hasn't been fully layouted yet, so do it here
//      hasEntryBeenSetBefore = true;
//
//      // before PrefWrapLength was set to 200, so it wrapped very early and used a lot of lines if an Entry had many Tags. This now fits the wrap length to EntryTagsControl's width
//      ChangeListener<? super Number> setWrapLengthListener = (observable, oldValue, newValue) -> setPaneSelectedTagsPreviewWrapLength();
//      this.widthProperty().addListener(setWrapLengthListener);
//      lblTags.widthProperty().addListener(setWrapLengthListener);
//    }

    pnSelectedTagsPreview.getChildren().clear();

    if(entry != null) {
//      for(final Tag tag : entry.getTagsSorted()) {
      for(final Tag tag : new TreeSet<>(editedTags)) {
        pnSelectedTagsPreview.getChildren().add(new EntryTagLabel(entry, tag, event -> {
          removeTagFromEntry(tag);
          for (TagListCell cell : tagListCells) {
            if (tag.equals(cell.tag)) {
              cell.setComboBoxToUnselected();
              break;
            }
          }
        }));
      }
    }
  }

  protected void setPaneSelectedTagsPreviewWrapLength() {
    if(lblTags.getWidth() > 0) { // on the first call this.getWidth() == 0 -> maxWidth would be less than zero -> less than zero this means 'MAX_VALUE'
      double adjustment = /*isInScrollPane() ? 90 :*/ 70;
      double debug1 = this.getWidth();
      double debug2 = lblTags.getWidth();
      double debug3 = getScene().getWidth();
      double debug = this.getWidth() - lblTags.getWidth() - adjustment;
//      pnSelectedTagsPreview.setPrefWidth(this.getWidth() - lblTags.getWidth() - adjustment);
      pnSelectedTagsPreview.setPrefWrapLength(this.getWidth() - lblTags.getWidth() - adjustment);
//      pnSelectedTagsPreview.setMaxWidth(this.getWidth() - lblTags.getWidth() - adjustment);
//      pnSelectedTagsPreview.setPrefWrapLength(pnSelectedTagsPreview.getMaxWidth());
//      pnSelectedTagsPreview.setPrefWidth(pnSelectedTagsPreview.getPrefWrapLength());
//      pnSelectedTagsPreview.setPrefWidth(Region.USE_COMPUTED_SIZE);
//      pnSelectedTagsPreview.getChildren().add(dummy);
//      pnSelectedTagsPreview.getChildren().remove(dummy);
    }
//    else
//      pnSelectedTagsPreview.setPrefWrapLength(this.getMinWidth() / 2);
  }

  protected boolean isInScrollPane() {
    if(this.getParent().getClass().getName().contains("ScrollPane"))
      return true;
    else if(this.getParent().getParent().getClass().getName().contains("ScrollPane"))
      return true;

    return false;
  }


  protected void enterHasBeenPressedInTextFieldFilterTags() {
    //        if (checkIfTagOfThatNameExists(txtfldFilterTags.getText()) == false)
//          addNewTagToEntry();

    String tagsFilter = txtfldFilterTags.getText();
    if(tagsFilter.contains(",") == false) {
      Tag tagOfThatName = findTagOfThatName(tagsFilter);
      if (tagOfThatName != null) {
        if(editedTags.contains(tagOfThatName) == false)
          addTagToEntry(tagOfThatName);
        else
          removeTagFromEntry(tagOfThatName);
      }
      else
        addNewTagToEntry();
    }
    else {
      for(Tag filteredTag : filteredTags)
        addTagToEntry(filteredTag);
    }
  }

  protected void setControlsForEnteredTagsFilter(String newValue) {
    filterTags(newValue);
    btnCreateTag.setDisable(checkIfTagOfThatNameExists(newValue));
  }

  protected boolean checkIfTagOfThatNameExists(String tagName) {
    if(tagName == null || tagName.isEmpty())
      return true;

    if(checkIfSystemTagOfThatNameExists(tagName))
      return true;

    if(findTagOfThatName(tagName) != null) return true;

    return false;
  }

  protected Tag findTagOfThatName(String tagName) {
    for(Tag tag : Application.getDeepThought().getTags()) {
      if(tagName.equals(tag.getName()))
        return tag;
    }

    return null;
  }

  protected boolean checkIfSystemTagOfThatNameExists(String tagName) {
    return Localization.getLocalizedStringForResourceKey("system.tag.all.entries").equals(tagName) ||
        Localization.getLocalizedStringForResourceKey("system.tag.entries.with.no.tags").equals(tagName);
  }

  protected void addTagToEntry(Tag tag) {
    if(removedTags.contains(tag)) {
      removedTags.remove(tag);
    }
    else {
      addedTags.add(tag);
    }

    addTagToEditedTags(tag);

    showEntryTags(entry);
    fireTagAddedEvent(entry, tag);

  }

  protected void addTagToEditedTags(Tag tag) {
//    SortedSet<Tag> sortedTags = new TreeSet<>(editedTags);
//    sortedTags.add(tag);
//
//    editedTags.clear();
//    editedTags.addAll(sortedTags);

    editedTags.add(tag);
  }

  protected void removeTagFromEntry(Tag tag) {
    if(addedTags.contains(tag)) {
      addedTags.remove(tag);
    }
    else {
      removedTags.add(tag);
    }

    editedTags.remove(tag);

    showEntryTags(entry);
    fireTagRemovedEvent(entry, tag);
  }

//  protected void filterTags(String filterConstraint) {
//    filteredTags.setPredicate((tag) -> {
//      // If filter text is empty, display all Tags.
//      if (filterConstraint == null || filterConstraint.isEmpty()) {
//        return true;
//      }
//
//      String lowerCaseFilterConstraint = filterConstraint.toLowerCase();
//      String[] parts = lowerCaseFilterConstraint.split(",");
//
//      for (String part : parts) {
//        if (tag.getName().toLowerCase().contains(part.trim())) {
//          return true; // Filter matches Tag's name
//        }
//      }
//
//      return false; // Does not match.
//    });
//  }

  protected void filterTags(String filterConstraint) {
    log.debug("Starting to filter Tags for filter " + filterConstraint + " ... ");
    if(filterTagsSearch != null)
      filterTagsSearch.interrupt();

    if(StringUtils.isNullOrEmpty(txtfldFilterTags.getText())) {
      filteredTags.setPredicate((tag) -> true);
      callFilteredTagsChangedListeners(FilterTagsSearchResults.NoFilterSearchResults);
    }
    else {
      filterTagsSearch = new FilterTagsSearch(txtfldFilterTags.getText(), new SearchCompletedListener<FilterTagsSearchResults>() {
        @Override
        public void completed(final FilterTagsSearchResults results) {
          Platform.runLater(() -> {
            filteredTags.setPredicate((tag) -> results.isRelevantMatch(tag));
            callFilteredTagsChangedListeners(results);
            log.debug("Done filtering Tags");
          });
        }
      });
      Application.getSearchEngine().filterTags(filterTagsSearch);
    }
  }

  protected void addNewTagToEntry() {
    String newTagName = txtfldFilterTags.getText();
    Tag newTag = new Tag(newTagName);
    Application.getDeepThought().addTag(newTag);

    //entry.addTag(newTag);
    addTagToEntry(newTag);

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
    if(this.entry != null)
      this.entry.removeEntityListener(entryListener);

    this.entry = entry;

    editedTags.clear();
    addedTags.clear();
    removedTags.clear();

    if(this.entry != null) {
      editedTags.addAll(entry.getTags());
      this.entry.addEntityListener(entryListener);
    }

    setDisable(entry == null);
    txtfldFilterTags.clear();
    showEntryTags(entry);

    for(TagListCell cell : tagListCells)
      cell.setEntry(this.entry);
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
    addNewTagToEntry();
  }


  protected EntityListener entryListener = new EntityListener() {
    @Override
    public void propertyChanged(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {

    }

    @Override
    public void entityAddedToCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity) {
      if(addedEntity instanceof Tag)
        addTagToEditedTags((Tag) addedEntity);
      showEntryTags(entry);
    }

    @Override
    public void entityOfCollectionUpdated(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity updatedEntity) {

    }

    @Override
    public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {
      if(removedEntity instanceof Tag)
        editedTags.remove((Tag)removedEntity);
      showEntryTags(entry);
    }
  };

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

//      if(updatedEntity instanceof Tag && entry != null && entry.getTags().contains((Tag)updatedEntity))
      if(updatedEntity instanceof Tag && entry != null && editedTags.contains((Tag) updatedEntity))
        showEntryTags(entry);
    }

    @Override
    public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {
      checkIfTagsHaveBeenUpdated(collectionHolder, removedEntity);
    }
  };

  protected void checkIfTagsHaveBeenUpdated(BaseEntity collectionHolder, BaseEntity entity) {
    if(collectionHolder instanceof DeepThought && entity instanceof Tag) {
      DeepThought deepThought = (DeepThought)collectionHolder;
      resetListViewAllTagsItems(deepThought);
    }
  }

  protected void resetListViewAllTagsItems(DeepThought deepThought) {
    listViewAllTagsItems.clear();
    listViewAllTagsItems.addAll(deepThought.getTags());
  }


  protected void fireTagAddedEvent(Entry entry, Tag tag) {
    if(tagAddedEventHandler != null)
      tagAddedEventHandler.handle(new EntryTagsEditedEvent(this, entry, tag));
  }

  protected void fireTagRemovedEvent(Entry entry, Tag tag) {
    if(tagRemovedEventHandler != null)
      tagRemovedEventHandler.handle(new EntryTagsEditedEvent(this, entry, tag));
  }

  public EventHandler<EntryTagsEditedEvent> getTagAddedEventHandler() {
    return tagAddedEventHandler;
  }

  public void setTagAddedEventHandler(EventHandler<EntryTagsEditedEvent> tagAddedEventHandler) {
    this.tagAddedEventHandler = tagAddedEventHandler;
  }

  public EventHandler<EntryTagsEditedEvent> getTagRemovedEventHandler() {
    return tagRemovedEventHandler;
  }


  public void setTagRemovedEventHandler(EventHandler<EntryTagsEditedEvent> tagRemovedEventHandler) {
    this.tagRemovedEventHandler = tagRemovedEventHandler;
  }

  public Set<Tag> getAddedTags() {
    return addedTags;
  }

  public Set<Tag> getRemovedTags() {
    return removedTags;
  }

  public ObservableSet<Tag> getEditedTags() {
    return editedTags;
  }
}

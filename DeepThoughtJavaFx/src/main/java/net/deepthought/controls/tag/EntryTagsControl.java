package net.deepthought.controls.tag;

import net.deepthought.Application;
import net.deepthought.controller.Dialogs;
import net.deepthought.controls.Constants;
import net.deepthought.controls.FXUtils;
import net.deepthought.controls.ICleanableControl;
import net.deepthought.controls.event.EntryTagsEditedEvent;
import net.deepthought.data.listener.ApplicationListener;
import net.deepthought.data.model.DeepThought;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.Tag;
import net.deepthought.data.model.listener.EntityListener;
import net.deepthought.data.persistence.db.BaseEntity;
import net.deepthought.util.Localization;
import net.deepthought.util.Notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

/**
 * Created by ganymed on 01/02/15.
 */
public class EntryTagsControl extends TitledPane implements IEditedEntitiesHolder<Tag>, ICleanableControl {

  private final static Logger log = LoggerFactory.getLogger(EntryTagsControl.class);


  protected Entry entry = null;

  protected DeepThought deepThought = null;

  protected ObservableSet<Tag> editedTags = FXCollections.observableSet();
  protected Set<Tag> addedTags = new HashSet<>();
  protected Set<Tag> removedTags = new HashSet<>();

  protected EventHandler<EntryTagsEditedEvent> tagAddedEventHandler = null;
  protected EventHandler<EntryTagsEditedEvent> tagRemovedEventHandler = null;


  @FXML
  protected ToggleButton btnShowHideSearchTagsToolWindow;
  @FXML
  protected Label lblTags;
  @FXML
  protected FlowPane pnSelectedTagsPreview;

  protected SearchAndSelectTagsControl searchAndSelectTagsControl = null;


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

    Application.addApplicationListener(applicationListener);

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

  protected ApplicationListener applicationListener = new ApplicationListener() {
    @Override
    public void deepThoughtChanged(DeepThought deepThought) {
      EntryTagsControl.this.deepThoughtChanged(deepThought);
    }

    @Override
    public void notification(Notification notification) {

    }
  };

  public void cleanUpControl() {
    Application.removeApplicationListener(applicationListener);

    if(deepThought != null)
      deepThought.removeEntityListener(deepThoughtListener);

    if(entry != null)
      entry.removeEntityListener(entryListener);

    clearEntryTagLabels();

    searchAndSelectTagsControl.cleanUpControl();

    tagAddedEventHandler = null;
    tagRemovedEventHandler = null;
  }

  protected void deepThoughtChanged(DeepThought newDeepThought) {
    if(this.deepThought != null)
      this.deepThought.removeEntityListener(deepThoughtListener);

    this.deepThought = newDeepThought;

    if(newDeepThought != null) {
      newDeepThought.addEntityListener(deepThoughtListener);
    }
  }

  protected void setupControl() {
    this.setExpanded(false);

    btnShowHideSearchTagsToolWindow.setGraphic(new ImageView(Constants.WindowIconPath));
    btnShowHideSearchTagsToolWindow.selectedProperty().addListener(((observableValue, oldValue, newValue) -> btnShowHideSearchTagsToolWindowIsSelectedChanged(newValue)));

    searchAndSelectTagsControl = new SearchAndSelectTagsControl(this);
//    searchAndSelectTagsControl.setMaxHeight(200);
    this.setContent(searchAndSelectTagsControl);

    showEntryTags();

    // before PrefWrapLength was set to 200, so it wrapped very early and used a lot of lines if an Entry had many Tags. This now fits the wrap length to EntryTagsControl's width
    this.widthProperty().addListener((observable, oldValue, newValue) -> setPaneSelectedTagsPreviewWrapLength());
    lblTags.widthProperty().addListener((observable, oldValue, newValue) -> setPaneSelectedTagsPreviewWrapLength());
  }

  protected void setPaneSelectedTagsPreviewWrapLength() {
    if(lblTags.getWidth() > 0) {
      if(getScene() != null && this.getWidth() > getScene().getWidth()) { // when Window was maximized, stage width is set yet but control width isn't yet
        pnSelectedTagsPreview.setPrefWrapLength(getMinWidth()); // -> setting WrapLength to control width would result in a way to large wrap length and therefor control being larger than space is available
      }
      else
        pnSelectedTagsPreview.setPrefWrapLength(this.getWidth() - lblTags.getWidth() - 86);
    }
  }

  protected void showEntryTags() {
    clearEntryTagLabels();

    for(final Tag tag : new TreeSet<>(editedTags)) {
      pnSelectedTagsPreview.getChildren().add(new EntryTagLabel(tag, event -> removeEntityFromEntry(tag)));
    }
  }

  protected void clearEntryTagLabels() {
    FXUtils.cleanUpChildrenAndClearPane(pnSelectedTagsPreview);
  }


  public ObservableSet<Tag> getEditedEntities() {
    return editedTags;
  }

  public boolean containsEditedEntity(Tag entity) {
    return editedTags.contains(entity);
  }

  public void addEntityToEntry(Tag entity) {
    if(removedTags.contains(entity)) {
      removedTags.remove(entity);
    }
    else {
      addedTags.add(entity);
    }

    addTagToEditedTags(entity);

    showEntryTags();
    fireTagAddedEvent(entity);

  }

  protected void addTagToEditedTags(Tag tag) {
    editedTags.add(tag);
  }

  public void removeEntityFromEntry(Tag entity) {
    if(addedTags.contains(entity)) {
      addedTags.remove(entity);
    } else {
      removedTags.add(entity);
    }

    editedTags.remove(entity);

    showEntryTags();
    fireTagRemovedEvent(entity);
  }

  public void setEntry(Entry entry) {
    if(this.entry != null)
      this.entry.removeEntityListener(entryListener);

    this.entry = entry;

    clearEditedTagsSets();

    if(this.entry != null) {
      editedTags.addAll(entry.getTags());
      this.entry.addEntityListener(entryListener);
    }

    setDisable(entry == null);
    showEntryTags();
  }

  public void setEntryTags(Collection<Tag> tags) {
    if(this.entry != null)
      this.entry.removeEntityListener(entryListener);

    this.entry = null;

    clearEditedTagsSets();

    editedTags.addAll(tags);

    setDisable(false);
    showEntryTags();
  }

  protected void clearEditedTagsSets() {
    editedTags.clear();
    addedTags.clear();
    removedTags.clear();
  }

  public void setSearchAndSelectTagsControlHeight(double height) {
    searchAndSelectTagsControl.setMinHeight(height);
    searchAndSelectTagsControl.setMaxHeight(height);
  }

  protected Stage toolWindowSearchTags = null;

  protected void btnShowHideSearchTagsToolWindowIsSelectedChanged(Boolean isSelected) {
    if(isSelected == false)
      toolWindowSearchTags.hide();
    else {
      if(toolWindowSearchTags == null)
        toolWindowSearchTags = createToolWindowSearchTags();
      toolWindowSearchTags.show();
    }
  }

  protected Stage createToolWindowSearchTags() {
    Stage stage = Dialogs.createToolWindowStage(new SearchAndSelectTagsControl(this), getScene().getWindow(), "tags", deepThought.getSettings().getSearchAndSelectTagsToolWindowSettings());
    stage.setOnHidden(event -> btnShowHideSearchTagsToolWindow.setSelected(false));

    return stage;
  }


  protected EntityListener entryListener = new EntityListener() {
    @Override
    public void propertyChanged(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {

    }

    @Override
    public void entityAddedToCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity) {
      if(addedEntity instanceof Tag)
        addTagToEditedTags((Tag) addedEntity);
      showEntryTags();
    }

    @Override
    public void entityOfCollectionUpdated(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity updatedEntity) {

    }

    @Override
    public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {
      if(removedEntity instanceof Tag)
        editedTags.remove((Tag)removedEntity);
      showEntryTags();
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
        showEntryTags();
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

      if(editedTags.contains(tag)) // TODO: is this correct? as also entityAddedToCollection() calls this method
        removeEntityFromEntry(tag);
    }
  }


  protected void fireTagAddedEvent(Tag tag) {
    if(tagAddedEventHandler != null)
      tagAddedEventHandler.handle(new EntryTagsEditedEvent(this, tag));
  }

  protected void fireTagRemovedEvent(Tag tag) {
    if(tagRemovedEventHandler != null)
      tagRemovedEventHandler.handle(new EntryTagsEditedEvent(this, tag));
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
}

package net.dankito.deepthought.controls.tag;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.controls.utils.IEditedEntitiesHolder;
import net.dankito.deepthought.controls.CollapsiblePane;
import net.dankito.deepthought.controls.ICleanUp;
import net.dankito.deepthought.data.listener.ApplicationListener;
import net.dankito.deepthought.data.model.DeepThought;
import net.dankito.deepthought.data.model.Entry;
import net.dankito.deepthought.data.model.Tag;
import net.dankito.deepthought.data.model.listener.EntityListener;
import net.dankito.deepthought.data.persistence.db.BaseEntity;
import net.dankito.deepthought.util.Notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

/**
 * Created by ganymed on 01/02/15.
 */
public class EntryTagsControl extends CollapsiblePane implements IEditedEntitiesHolder<Tag>, ICleanUp {

  private final static Logger log = LoggerFactory.getLogger(EntryTagsControl.class);


  protected Entry entry = null;

  protected DeepThought deepThought = null;

  protected ObservableSet<Tag> editedTags = FXCollections.observableSet(); // TODO: this has to be made lazy loading as it otherwise loads all Tags
  protected Set<Tag> addedTags = new HashSet<>();
  protected Set<Tag> removedTags = new HashSet<>();

  protected EventHandler<net.dankito.deepthought.controls.event.EntryTagsEditedEvent> tagAddedEventHandler = null;
  protected EventHandler<net.dankito.deepthought.controls.event.EntryTagsEditedEvent> tagRemovedEventHandler = null;


  @FXML
  protected ToggleButton btnShowHideSearchTagsToolWindow;
  @FXML
  protected Label lblTags;
  @FXML
  protected FlowPane pnSelectedTagsPreview;

  protected SearchAndSelectTagsControl searchAndSelectTagsControl = null;

  protected Stage toolWindowSearchTags = null;


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

    setupControl();
    if(deepThought != null)
      deepThought.addEntityListener(deepThoughtListener);
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

  public void cleanUp() {
    Application.removeApplicationListener(applicationListener);

    if(deepThought != null)
      deepThought.removeEntityListener(deepThoughtListener);

    if(entry != null)
      entry.removeEntityListener(entryListener);

    clearEntryTagLabels();

    searchAndSelectTagsControl.cleanUp();

    if(toolWindowSearchTags != null) {
      net.dankito.deepthought.controller.Dialogs.cleanUpAndCloseToolWindowStage(toolWindowSearchTags);
      toolWindowSearchTags = null;
    }

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
    setMinHeight(22);

    setupTitle();
    this.setExpanded(false);

    searchAndSelectTagsControl = new SearchAndSelectTagsControl(this);
//    searchAndSelectTagsControl.setPrefHeight(250);
//    searchAndSelectTagsControl.setMaxHeight(200);
    searchAndSelectTagsControl.setMaxHeight(net.dankito.deepthought.controls.utils.FXUtils.SizeMaxValue);
    this.setContent(searchAndSelectTagsControl);

    showEntryTags();
  }

  protected void setupTitle() {
    HBox titlePane = new HBox();
    titlePane.setAlignment(Pos.CENTER_LEFT);
    titlePane.setPrefWidth(USE_COMPUTED_SIZE);
    titlePane.setMaxWidth(net.dankito.deepthought.controls.utils.FXUtils.SizeMaxValue);
//    titlePane.setPrefHeight(USE_COMPUTED_SIZE);
//    titlePane.setMinHeight(USE_PREF_SIZE);
    titlePane.setMaxHeight(net.dankito.deepthought.controls.utils.FXUtils.SizeMaxValue);

    btnShowHideSearchTagsToolWindow = new ToggleButton("", new ImageView(net.dankito.deepthought.controls.Constants.WindowIconPath));
    btnShowHideSearchTagsToolWindow.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    btnShowHideSearchTagsToolWindow.setMinHeight(24);
    btnShowHideSearchTagsToolWindow.setMaxHeight(24);
    btnShowHideSearchTagsToolWindow.setMinWidth(24);
    btnShowHideSearchTagsToolWindow.setMaxWidth(24);
    btnShowHideSearchTagsToolWindow.selectedProperty().addListener(((observableValue, oldValue, newValue) -> btnShowHideSearchTagsToolWindowIsSelectedChanged(newValue)));
    titlePane.getChildren().add(btnShowHideSearchTagsToolWindow);

    lblTags = new Label();
    net.dankito.deepthought.util.localization.JavaFxLocalization.bindLabeledText(lblTags, "tags");
    lblTags.setMinHeight(22);
    lblTags.setPrefWidth(USE_COMPUTED_SIZE);
    lblTags.setMinWidth(USE_PREF_SIZE);
    lblTags.setMaxWidth(USE_PREF_SIZE);
    titlePane.getChildren().add(lblTags);
    HBox.setMargin(lblTags, new Insets(0, 4, 0, 4));

    pnSelectedTagsPreview = new FlowPane();
    pnSelectedTagsPreview.setColumnHalignment(HPos.LEFT);
    pnSelectedTagsPreview.setRowValignment(VPos.CENTER);
    pnSelectedTagsPreview.setAlignment(Pos.CENTER_LEFT);
    pnSelectedTagsPreview.setVgap(2);
    titlePane.getChildren().add(pnSelectedTagsPreview);
    HBox.setHgrow(pnSelectedTagsPreview, Priority.ALWAYS);

    setTitle(titlePane);
  }

  protected void showEntryTags() {
    clearEntryTagLabels();

    for(final Tag tag : new TreeSet<>(editedTags)) {
      pnSelectedTagsPreview.getChildren().add(new EntryTagLabel(tag, event -> removeEntityFromEntry(tag)));
    }
  }

  protected void clearEntryTagLabels() {
    net.dankito.deepthought.controls.utils.FXUtils.cleanUpChildrenAndClearPane(pnSelectedTagsPreview);
  }


  public ObservableSet<Tag> getEditedEntities() {
    return editedTags;
  }

  @Override
  public Set<Tag> getAddedEntities() {
    return addedTags;
  }

  @Override
  public Set<Tag> getRemovedEntities() {
    return removedTags;
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

  protected void removeTagFromEditedTags(Tag tag) {
    editedTags.remove(tag);
  }

  public void removeEntityFromEntry(Tag entity) {
    if(addedTags.contains(entity)) {
      addedTags.remove(entity);
    } else {
      removedTags.add(entity);
    }

    removeTagFromEditedTags(entity);

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
    Stage stage = net.dankito.deepthought.controller.Dialogs.createToolWindowStage(new SearchAndSelectTagsControl(this), getScene().getWindow(), "tags", deepThought.getSettings().getSearchAndSelectTagsToolWindowSettings());
    stage.setOnHidden(event -> btnShowHideSearchTagsToolWindow.setSelected(false));

    return stage;
  }


  protected EntityListener entryListener = new EntityListener() {
    @Override
    public void propertyChanged(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {

    }

    @Override
    public void entityAddedToCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity) {
      if(addedEntity instanceof Tag) { // TODO: is it ever useful / needed that showEntryTags() is called even thought addedEntity is not a Tag?
        net.dankito.deepthought.controls.utils.FXUtils.runOnUiThread(() -> handleEntityAddedToEntry(addedEntity));
      }
    }

    @Override
    public void entityOfCollectionUpdated(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity updatedEntity) {

    }

    @Override
    public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {
      if(removedEntity instanceof Tag) { // TODO: is it ever useful / needed that showEntryTags() is called even thought removedEntity is not a Tag?
        net.dankito.deepthought.controls.utils.FXUtils.runOnUiThread(() -> handleEntityRemovedFromEntry(removedEntity));
      }
    }
  };

  protected void handleEntityAddedToEntry(BaseEntity addedEntity) {
    addTagToEditedTags((Tag) addedEntity);
    showEntryTags();
  }

  protected void handleEntityRemovedFromEntry(BaseEntity removedEntity) {
    removeTagFromEditedTags((Tag) removedEntity);
    showEntryTags();
  }

  protected EntityListener deepThoughtListener = new EntityListener() {
    @Override
    public void propertyChanged(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {

    }

    @Override
    public void entityAddedToCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity) {

    }

    @Override
    public void entityOfCollectionUpdated(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity updatedEntity) {
      if(updatedEntity instanceof Tag && entry != null && editedTags.contains((Tag) updatedEntity)) {
        net.dankito.deepthought.controls.utils.FXUtils.runOnUiThread(() -> showEntryTags());
      }
    }

    @Override
    public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {
      if(collectionHolder instanceof DeepThought && removedEntity instanceof Tag) {
        Tag tag = (Tag)removedEntity;

        if(editedTags.contains(tag)) {
          Platform.runLater(() -> removeEntityFromEntry(tag));
        }
      }
    }
  };


  protected void fireTagAddedEvent(Tag tag) {
    if(tagAddedEventHandler != null)
      tagAddedEventHandler.handle(new net.dankito.deepthought.controls.event.EntryTagsEditedEvent(this, tag));
  }

  protected void fireTagRemovedEvent(Tag tag) {
    if(tagRemovedEventHandler != null)
      tagRemovedEventHandler.handle(new net.dankito.deepthought.controls.event.EntryTagsEditedEvent(this, tag));
  }

  public EventHandler<net.dankito.deepthought.controls.event.EntryTagsEditedEvent> getTagAddedEventHandler() {
    return tagAddedEventHandler;
  }

  public void setTagAddedEventHandler(EventHandler<net.dankito.deepthought.controls.event.EntryTagsEditedEvent> tagAddedEventHandler) {
    this.tagAddedEventHandler = tagAddedEventHandler;
  }

  public EventHandler<net.dankito.deepthought.controls.event.EntryTagsEditedEvent> getTagRemovedEventHandler() {
    return tagRemovedEventHandler;
  }


  public void setTagRemovedEventHandler(EventHandler<net.dankito.deepthought.controls.event.EntryTagsEditedEvent> tagRemovedEventHandler) {
    this.tagRemovedEventHandler = tagRemovedEventHandler;
  }

  public Set<Tag> getAddedTags() {
    return addedTags;
  }

  public Set<Tag> getRemovedTags() {
    return removedTags;
  }
}

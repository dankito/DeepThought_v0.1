package net.deepthought.controller;

import net.deepthought.Application;
import net.deepthought.controller.enums.DialogResult;
import net.deepthought.controller.enums.FieldWithUnsavedChanges;
import net.deepthought.controls.categories.EntryCategoriesControl;
import net.deepthought.controls.event.FieldChangedEvent;
import net.deepthought.controls.file.FilesControl;
import net.deepthought.controls.html.CollapsibleHtmlEditor;
import net.deepthought.controls.html.DeepThoughtFxHtmlEditorListener;
import net.deepthought.controls.person.EntryPersonsControl;
import net.deepthought.controls.reference.EntryReferenceControl;
import net.deepthought.controls.tag.EntryTagsControl;
import net.deepthought.controls.utils.EditedEntitiesHolder;
import net.deepthought.controls.utils.FXUtils;
import net.deepthought.data.contentextractor.EntryCreationResult;
import net.deepthought.data.model.Category;
import net.deepthought.data.model.DeepThought;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.FileLink;
import net.deepthought.data.model.Person;
import net.deepthought.data.model.Reference;
import net.deepthought.data.model.ReferenceBase;
import net.deepthought.data.model.ReferenceSubDivision;
import net.deepthought.data.model.SeriesTitle;
import net.deepthought.data.model.Tag;
import net.deepthought.data.model.listener.EntityListener;
import net.deepthought.data.model.settings.enums.DialogsFieldsDisplay;
import net.deepthought.data.model.settings.enums.Setting;
import net.deepthought.data.persistence.db.BaseEntity;
import net.deepthought.data.persistence.db.TableConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Created by ganymed on 21/12/14.
 */
public class EditEntryDialogController extends EntityDialogFrameController implements Initializable {

  private final static Logger log = LoggerFactory.getLogger(EditEntryDialogController.class);


  protected Entry entry = null;

  protected EntryCreationResult creationResult = null;

  protected EditedEntitiesHolder<FileLink> editedAttachedFiles = null;
  protected EditedEntitiesHolder<FileLink> editedEmbeddedFiles = null;


  protected DeepThoughtFxHtmlEditorListener abstractListener = null;

  protected DeepThoughtFxHtmlEditorListener contentListener = null;



  @FXML
  protected ScrollPane scrpnContent;
  @FXML
  protected VBox contentPane;

  protected CollapsibleHtmlEditor htmledAbstract;

  protected CollapsibleHtmlEditor htmledContent;

  protected EntryTagsControl entryTagsControl = null;

  protected EntryCategoriesControl entryCategoriesControl = null;

  protected EntryReferenceControl entryReferenceControl;


  protected EntryPersonsControl entryPersonsControl = null;

  protected FilesControl filesControl = null;


  @Override
  protected String getEntityType() {
    return "entry";
  }

  @Override
  protected void settingsChanged(Setting setting, Object previousValue, Object newValue) {
    super.settingsChanged(setting, previousValue, newValue);

    if (setting == Setting.UserDeviceShowCategories)
      setCategoriesPaneVisibility((boolean) newValue);
  }

  protected void setupControls() {
    super.setupControls();

    editedAttachedFiles = new EditedEntitiesHolder<>(entry.getAttachedFiles(), event -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.EntryAttachedFiles), event -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.EntryAttachedFiles));
    editedEmbeddedFiles = new EditedEntitiesHolder<>(entry.getEmbeddedFiles()); // no added / removed listener needed as Abstract / Content is updated then anyway

    setButtonChooseFieldsToShowVisibility(true);

//    scrpnContent = new ScrollPane();
//    scrpnContent.setPrefWidth(905);
//    scrpnContent.setMaxWidth(Double.MAX_VALUE);
//    scrpnContent.setMaxHeight(Double.MAX_VALUE);
//    scrpnContent.setFitToWidth(true);
//    scrpnContent.setFitToHeight(true);
//    scrpnContent.setPrefViewportHeight(650);
//    scrpnContent.setPrefViewportWidth(650);
//    scrpnContent.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
//    scrpnContent.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
//
//    contentPane = new VBox();
//    contentPane.setFillWidth(true);
//    scrpnContent.setContent(contentPane);

    abstractListener = new DeepThoughtFxHtmlEditorListener(editedEmbeddedFiles, fieldsWithUnsavedChanges, FieldWithUnsavedChanges.EntryAbstract);
    htmledAbstract = new CollapsibleHtmlEditor("abstract", abstractListener);
    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(htmledAbstract);
    htmledAbstract.setExpanded(false);
    contentPane.getChildren().add(0, htmledAbstract);
    VBox.setVgrow(htmledAbstract, Priority.SOMETIMES);
    VBox.setMargin(htmledAbstract, new Insets(6, 0, 0, 0));

    contentListener = new DeepThoughtFxHtmlEditorListener(editedEmbeddedFiles, fieldsWithUnsavedChanges, FieldWithUnsavedChanges.EntryContent);
    htmledContent = new CollapsibleHtmlEditor("content", contentListener);
    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(htmledContent);
    contentPane.getChildren().add(1, htmledContent);
    VBox.setVgrow(htmledContent, Priority.ALWAYS);
    VBox.setMargin(htmledContent, new Insets(6, 0, 0, 0));

    setupTagsAndCategoriesControl();

    entryReferenceControl = new EntryReferenceControl(entry, event -> referenceControlFieldChanged(event));
    entryReferenceControl.setExpanded(false);
    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(entryReferenceControl);
    contentPane.getChildren().add(4, entryReferenceControl);
    VBox.setVgrow(entryReferenceControl, Priority.SOMETIMES);
    VBox.setMargin(entryReferenceControl, new Insets(6, 0, 0, 0));

    entryPersonsControl = new EntryPersonsControl(entry);
    entryPersonsControl.setExpanded(false);
    entryPersonsControl.setPersonAddedEventHandler((event) -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.EntryPersons));
    entryPersonsControl.setPersonRemovedEventHandler((event) -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.EntryPersons));
    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(entryPersonsControl);
    contentPane.getChildren().add(5, entryPersonsControl);
    VBox.setVgrow(entryPersonsControl, Priority.SOMETIMES);
    VBox.setMargin(entryPersonsControl, new Insets(6, 0, 0, 0));

//    entryPersonsControl.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> showContextHelpForTarget(event));
    entryPersonsControl.addEventHandler(MouseEvent.MOUSE_ENTERED_TARGET, event -> showContextHelpForTarget(event));
    entryPersonsControl.addEventHandler(MouseEvent.MOUSE_EXITED_TARGET, event -> contextHelpControl.showContextHelpForResourceKey("default")); // TODO: remove as soon as other context help texts are
    // implemented

    filesControl = new FilesControl(editedAttachedFiles);
    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(filesControl);
    filesControl.setMinHeight(Region.USE_PREF_SIZE);
    filesControl.setMaxHeight(Double.MAX_VALUE);
    contentPane.getChildren().add(6, filesControl);
    VBox.setVgrow(filesControl, Priority.SOMETIMES);
    VBox.setMargin(filesControl, new Insets(6, 0, 0, 0));
  }

  protected void setupTagsAndCategoriesControl() {
    // TODO: replace entry by IEditedEntitiesHolder<Tags> so that Dialog controls edited tags -> if a Tag is removed which is in creationResult, it can be removed from creationResult as well (also Categories, ...)
    entryTagsControl = new EntryTagsControl(entry);
    entryTagsControl.setTagAddedEventHandler(event -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.EntryTags));
    entryTagsControl.setTagRemovedEventHandler(event -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.EntryTags));
    entryTagsControl.setMinWidth(150);
    entryTagsControl.setPrefHeight(250);
    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(entryTagsControl);
    entryTagsControl.setExpanded(true);
    VBox.setVgrow(entryTagsControl, Priority.SOMETIMES);
    VBox.setMargin(entryTagsControl, new Insets(6, 0, 0, 0));
    contentPane.getChildren().add(2, entryTagsControl);

    entryCategoriesControl = new EntryCategoriesControl(entry);
    entryCategoriesControl.setCategoryAddedEventHandler(event -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.EntryCategories));
    entryCategoriesControl.setCategoryRemovedEventHandler(event -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.EntryCategories));
    entryCategoriesControl.setMinWidth(150);
    entryTagsControl.setPrefHeight(250);
    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(entryCategoriesControl);
    entryCategoriesControl.setExpanded(true);
    VBox.setVgrow(entryCategoriesControl, Priority.SOMETIMES);
    VBox.setMargin(entryCategoriesControl, new Insets(6, 0, 0, 0));
    contentPane.getChildren().add(3, entryCategoriesControl);

    setCategoriesPaneVisibility();
  }

  protected void dialogFieldsDisplayChanged(DialogsFieldsDisplay dialogsFieldsDisplay) {
    entryReferenceControl.setVisible(entry.isAReferenceSet() || (creationResult != null && creationResult.isAReferenceSet()) || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);
    entryPersonsControl.setVisible(entry.hasPersons() || (creationResult != null && creationResult.hasPersons()) || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);
    filesControl.setVisible(entry.hasAttachedFiles() || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);

    setButtonChooseFieldsToShowVisibility(dialogsFieldsDisplay != DialogsFieldsDisplay.ShowAll && (entryReferenceControl.isVisible() == false ||
        entryPersonsControl.isVisible() == false || filesControl.isVisible() == false));

    setCategoriesPaneVisibility();
  }

  protected void setCategoriesPaneVisibility() {
    setCategoriesPaneVisibility(Application.getSettings().showCategories());
  }

  protected void setCategoriesPaneVisibility(boolean showCategories) {
    entryCategoriesControl.setVisible(showCategories);
  }

  protected void referenceControlFieldChanged(FieldChangedEvent event) {
    fieldsWithUnsavedChanges.add(event.getFieldWithUnsavedChanges());
  }

  protected void setEntryValues(final Entry entry) {
    htmledAbstract.setHtml(entry.getAbstract(), true);

    htmledAbstract.setExpanded(entry.hasAbstract());

    htmledContent.setHtml(entry.getContent(), true);

    entryTagsControl.setExpanded(entry.hasTags() == false);
//    entryCategoriesControl.setExpanded(entry.hasCategories() == false);
    entryCategoriesControl.setExpanded(false);

    entryReferenceControl.setExpanded(entry.isAReferenceSet() == false);

    fieldsWithUnsavedChanges.clear();

    btnApplyChanges.setDisable(entry.isPersisted() == true || entry.hasContent() == false); // e.g. for new Entries created by a ContentExtractor: User should be able to save them immediately by clicking on 'Apply'

    dialogFieldsDisplayChanged(Application.getSettings().getDialogsFieldsDisplay());
  }


  @Override
  protected void closeDialog() {
    entry.removeEntityListener(entryListener);

    htmledAbstract.cleanUp();
    htmledContent.cleanUp();
    entryTagsControl.cleanUp();
    entryCategoriesControl.cleanUp();
    entryReferenceControl.cleanUp();
    entryPersonsControl.cleanUp();

    filesControl.cleanUp();

    super.closeDialog();
  }

  @Override
  protected void saveEntity() {
    persistEntitiesIfNecessary();

    saveEditedFieldsOnEntry();
  }

  protected void persistEntitiesIfNecessary() {
    if(creationResult == null)
      persistEntities();
    else
      creationResult.saveCreatedEntities(htmledAbstract.getHtml(), htmledContent.getHtml());
  }

  protected void persistEntities() {
    boolean isSeriesUnPersisted = entry.getSeries() != null && entry.getSeries().isPersisted() == false;
    boolean isReferenceUnPersisted = entry.getReference() != null && entry.getReference().isPersisted() == false;
    boolean isReferenceSubDivisionUnPersisted = entry.getReferenceSubDivision() != null && entry.getReferenceSubDivision().isPersisted() == false;
    boolean isEntryUnPersisted = entry.isPersisted() == false;

    DeepThought deepThought = Application.getDeepThought();

    if(isSeriesUnPersisted)
      deepThought.addSeriesTitle(entry.getSeries());

    if(isReferenceUnPersisted)
      deepThought.addReference(entry.getReference());

    if(isReferenceSubDivisionUnPersisted)
      deepThought.addReferenceSubDivision(entry.getReferenceSubDivision());

    if(isEntryUnPersisted) // a new entry
      deepThought.addEntry(entry);
  }

  protected void saveEditedFieldsOnEntry() {
    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.EntryAbstract)) {
      entry.setAbstract(abstractListener.handleEditedEmbeddedFiles(entry.getAbstract(), htmledAbstract.getHtml()));
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.EntryAbstract);
    }

    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.EntryContent)) {
      entry.setContent(contentListener.handleEditedEmbeddedFiles(entry.getContent(), htmledContent.getHtml()));
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.EntryContent);
    }

    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.EntrySeriesTitle) || fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.EntryReference)
        || fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.EntryReferenceSubDivision)) {
      ReferenceBase referenceBase = entryReferenceControl.getSelectedReferenceBase();

      if(referenceBase instanceof ReferenceSubDivision)
        entry.setReferenceSubDivision((ReferenceSubDivision)referenceBase);
      else if(referenceBase instanceof Reference)
        entry.setReference((Reference) referenceBase);
      else if(referenceBase instanceof SeriesTitle)
        entry.setSeries((SeriesTitle)referenceBase);
      else // Reference has been unset
        entry.clearReferenceBases();

      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.EntrySeriesTitle);
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.EntryReference);
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.EntryReferenceSubDivision);
    }
    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.EntryReferenceIndication)) {
      entry.setIndication(entryReferenceControl.getReferenceIndication());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.EntryReferenceIndication);
    }

    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.EntryPersons)) {
      for(Person removedPerson : entryPersonsControl.getCopyOfRemovedPersonsAndClear())
        entry.removePerson(removedPerson);

      for(Person addedPerson : entryPersonsControl.getCopyOfAddedPersonsAndClear())
        entry.addPerson(addedPerson);

      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.EntryPersons);
    }

    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.EntryTags)) {
      for(Tag removedTag : entryTagsControl.getRemovedTags())
        entry.removeTag(removedTag);
      entryTagsControl.getRemovedTags().clear();

      for(Tag addedTag : entryTagsControl.getAddedTags())
        entry.addTag(addedTag);
      entryTagsControl.getAddedTags().clear();

      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.EntryTags);
    }

    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.EntryCategories)) {
      for(Category removedCategory : entryCategoriesControl.getRemovedCategories())
        entry.removeCategory(removedCategory);
      entryCategoriesControl.getRemovedCategories().clear();

      for(Category addedCategory : entryCategoriesControl.getAddedCategories())
        entry.addCategory(addedCategory);
      entryCategoriesControl.getAddedCategories().clear();

      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.EntryCategories);
    }

    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.EntryAttachedFiles)) {
      for(FileLink removedFile : editedAttachedFiles.getRemovedEntities())
        entry.removeAttachedFile(removedFile);
      editedAttachedFiles.getRemovedEntities().clear();

      for(FileLink addedFile : editedAttachedFiles.getAddedEntities())
        entry.addAttachedFile(addedFile);
      editedAttachedFiles.getAddedEntities().clear();

      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.EntryAttachedFiles);
    }

    for(FileLink removedEmbeddedFile : editedEmbeddedFiles.getRemovedEntities())
      entry.removeEmbeddedFile(removedEmbeddedFile);
    editedEmbeddedFiles.getRemovedEntities().clear();

    for(FileLink addedEmbeddedFile : editedEmbeddedFiles.getAddedEntities())
      entry.addEmbeddedFile(addedEmbeddedFile);
    editedEmbeddedFiles.getAddedEntities().clear();

    if(fieldsWithUnsavedChanges.size() > 0) {
      log.warn("We're at end of () method an  still contains unsaved fields:");
      for(FieldWithUnsavedChanges field : fieldsWithUnsavedChanges)
        log.warn("" + field);
    }

    // if it's a new Entry e.g. created by a ContentExtractor, then btnApply was enabled without that fieldsWithUnsavedChanges contained unsaved fields. So disable Button now
    btnApplyChanges.setDisable(true);
  }


  @Override
  public ContextMenu createHiddenFieldsContextMenu() {
    ContextMenu hiddenFieldsMenu = new ContextMenu();

    if(htmledAbstract.isVisible() == false)
      createHiddenFieldMenuItem(hiddenFieldsMenu, htmledAbstract, "entry.abstract");
    if(htmledContent.isVisible() == false)
      createHiddenFieldMenuItem(hiddenFieldsMenu, htmledContent, "content");

    if(entryReferenceControl.isVisible() == false)
      createHiddenFieldMenuItem(hiddenFieldsMenu, entryReferenceControl, "reference");
    if(entryPersonsControl.isVisible() == false)
      createHiddenFieldMenuItem(hiddenFieldsMenu, entryPersonsControl, "persons");
    if(entryTagsControl.isVisible() == false)
      createHiddenFieldMenuItem(hiddenFieldsMenu, entryTagsControl, "tags");
    if(entryCategoriesControl.isVisible() == false)
      createHiddenFieldMenuItem(hiddenFieldsMenu, entryCategoriesControl, "categories");

    if(filesControl.isVisible() == false)
      createHiddenFieldMenuItem(hiddenFieldsMenu, filesControl, "files");

    hiddenFieldsMenu.show(btnChooseFieldsToShow, Side.TOP, 0, 0);

    return hiddenFieldsMenu;
  }


  @FXML
  public void handleButtonAddFileAction(ActionEvent event) {
    final FileLink newFile = new FileLink();

    Dialogs.showEditFileDialog(newFile, new ChildWindowsControllerListener() {
      @Override
      public void windowClosing(Stage stage, ChildWindowsController controller) {

      }

      @Override
      public void windowClosed(Stage stage, ChildWindowsController controller) {
        if (controller.getDialogResult() == DialogResult.Ok) {
          entry.addAttachedFile(newFile); // TODO: no, don't add file directly, wait till 'OK' button has been pressed
        }
      }
    });
  }


  public Entry getEntry() {
    return entry;
  }

  public void setWindowStageAndEntry(final Stage windowStage, Entry entry) {
    this.entry = entry;
    super.setWindowStage(windowStage, entry);

    setEntryValues(entry);
    entry.addEntityListener(entryListener);

//    contentPane.minHeightProperty().bind(windowStage.getScene().heightProperty().subtract(pnBottomBar.heightProperty()).subtract(8));

    // TODO: for a better user experience it would be better if Content editor is focused by default so that user can start editing Content right away, but that's not working with HtmlEditor
    FXUtils.focusNode(htmledContent);
  }

  public void setWindowStageAndEntryCreationResult(final Stage windowStage, EntryCreationResult creationResult) {
    this.creationResult = creationResult;
    setWindowStageAndEntry(windowStage, creationResult.getCreatedEntry());

    entryTagsControl.setEntryTags(creationResult.getTags());
    entryCategoriesControl.setEntryCategories(creationResult.getCategories());

    entryReferenceControl.setEntryCreationResult(creationResult);

    entryTagsControl.setExpanded(creationResult.getTags().size() == 0);

    entryReferenceControl.setExpanded(creationResult.isAReferenceSet() == false);

    for(FileLink embeddedFile : creationResult.getEmbeddedFiles()) {
      editedEmbeddedFiles.addEntityToEntry(embeddedFile);
    }

    for(FileLink attachedFile : creationResult.getAttachedFiles()) {
      editedAttachedFiles.addEntityToEntry(attachedFile);
    }
    filesControl.setVisible(creationResult.getAttachedFiles().size() > 0);
  }


  @Override
  protected String getEntityPreview() {
    return entry.getPreview();
  }

  protected EntityListener entryListener = new EntityListener() {
    @Override
    public void propertyChanged(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {
      if(propertyName.equals(TableConfig.EntryAbstractColumnName)) {
        if(htmledAbstract.getHtml().equals(((Entry) entity).getAbstract()) == false) // don't update Html Control if change has been committed by it
          htmledAbstract.setHtml(((Entry) entity).getAbstract());
      }
      else if(propertyName.equals(TableConfig.EntryContentColumnName)) {
        if(htmledContent.getHtml().equals(((Entry) entity).getContent()) == false) // don't update Html Control if change has been committed by it
          htmledContent.setHtml(((Entry) entity).getContent());
      }
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


}

package net.deepthought.controller;

import net.deepthought.Application;
import net.deepthought.controller.enums.DialogResult;
import net.deepthought.controller.enums.FieldWithUnsavedChanges;
import net.deepthought.controls.FXUtils;
import net.deepthought.controls.categories.EntryCategoriesControl;
import net.deepthought.controls.event.FieldChangedEvent;
import net.deepthought.controls.file.FileRootTreeItem;
import net.deepthought.controls.file.FileTreeTableCell;
import net.deepthought.controls.html.CollapsibleHtmlEditor;
import net.deepthought.controls.html.HtmlEditorListener;
import net.deepthought.controls.person.EntryPersonsControl;
import net.deepthought.controls.reference.EntryReferenceControl;
import net.deepthought.controls.tag.EntryTagsControl;
import net.deepthought.data.contentextractor.EntryCreationResult;
import net.deepthought.data.model.Category;
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

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

/**
 * Created by ganymed on 21/12/14.
 */
public class EditEntryDialogController extends EntityDialogFrameController implements Initializable {

  private final static Logger log = LoggerFactory.getLogger(EditEntryDialogController.class);


  protected Entry entry = null;

  protected EntryCreationResult creationResult = null;

  protected ObservableList<FileLink> listViewFilesItems;



  @FXML
  protected ScrollPane scrpnContent;
  @FXML
  protected VBox contentPane;

  @FXML
  protected Pane paneTitle;
  @FXML
  protected TextField txtfldTitle;

  protected CollapsibleHtmlEditor htmledAbstract;

  protected CollapsibleHtmlEditor htmledContent;

  protected SplitPane paneTagsAndCategories;

  protected EntryTagsControl entryTagsControl = null;

  protected EntryCategoriesControl entryCategoriesControl = null;

  @FXML
  protected EntryReferenceControl entryReferenceControl;


  protected EntryPersonsControl entryPersonsControl = null;

  @FXML
  protected TitledPane ttldpnFiles;
  @FXML
  protected FlowPane flpnFilesPreview;
  @FXML
  protected TreeTableView<FileLink> trtblvwFiles;
  @FXML
  protected TreeTableColumn<FileLink, String> clmnFile;


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

    setButtonChooseFieldsToShowVisiblity(true);

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(paneTitle);
    txtfldTitle.textProperty().addListener((observable, oldValue, newValue) -> {
      fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.EntryTitle);
    });
    paneTitle.setVisible(false);
    ((Pane)paneTitle.getParent()).getChildren().remove(paneTitle); // TODO: remove paneTitle completely or leave on parent if Title doesn't get removed

    htmledAbstract = new CollapsibleHtmlEditor("abstract", abstractListener);
    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(htmledAbstract);
    htmledAbstract.setExpanded(false);
    contentPane.getChildren().add(1, htmledAbstract);
    VBox.setVgrow(htmledAbstract, Priority.SOMETIMES);
    VBox.setMargin(htmledAbstract, new Insets(6, 0, 0, 0));

    htmledContent = new CollapsibleHtmlEditor("content", contentListener);
    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(htmledContent);
    contentPane.getChildren().add(2, htmledContent);
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

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(ttldpnFiles);
    clmnFile.setCellFactory(new Callback<TreeTableColumn<FileLink, String>, TreeTableCell<FileLink, String>>() {
      @Override
      public TreeTableCell<FileLink, String> call(TreeTableColumn<FileLink, String> param) {
        return new FileTreeTableCell(entry);
      }
    });
  }

  protected void setupTagsAndCategoriesControl() {
    paneTagsAndCategories = new SplitPane();

    paneTagsAndCategories.setMinHeight(26);
    paneTagsAndCategories.setMinHeight(Region.USE_PREF_SIZE);
    paneTagsAndCategories.setPrefHeight(Region.USE_COMPUTED_SIZE);
//    paneTagsAndCategories.setMaxHeight(Region.USE_PREF_SIZE);
    paneTagsAndCategories.setMaxHeight(Double.MAX_VALUE);

    // as ScrollPane is too stupid to resize correctly when entryTagsControl or entryCategoriesControl is expanded, i wrapped paneTagsAndCategories in another ScrollPane
    ScrollPane tagsAndCategoriesScrollPane = new ScrollPane(paneTagsAndCategories);
    tagsAndCategoriesScrollPane.setFitToWidth(true);

    contentPane.getChildren().add(3, tagsAndCategoriesScrollPane);
    VBox.setVgrow(tagsAndCategoriesScrollPane, Priority.SOMETIMES);
    VBox.setMargin(tagsAndCategoriesScrollPane, new Insets(6, 0, 0, 0));

    // TODO: replace entry by IEditedEntitiesHolder<Tags> so that Dialog controls edited tags -> if a Tag is removed which is in creationResult, it can be removed from creationResult as well (also Categories, ...)
    entryTagsControl = new EntryTagsControl(entry);
    entryTagsControl.setTagAddedEventHandler(event -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.EntryTags));
    entryTagsControl.setTagRemovedEventHandler(event -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.EntryTags));
    entryTagsControl.setMinWidth(150);
    entryTagsControl.setPrefHeight(250);
    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(entryTagsControl);
    entryTagsControl.setExpanded(true);
    paneTagsAndCategories.getItems().add(entryTagsControl);

    entryCategoriesControl = new EntryCategoriesControl(entry);
    entryCategoriesControl.setCategoryAddedEventHandler(event -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.EntryCategories));
    entryCategoriesControl.setCategoryRemovedEventHandler(event -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.EntryCategories));
    entryCategoriesControl.setMinWidth(150);
    entryTagsControl.setPrefHeight(250);
    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(entryCategoriesControl);
    entryCategoriesControl.setExpanded(true);
    paneTagsAndCategories.getItems().add(entryCategoriesControl);

    entryTagsControl.heightProperty().addListener((observable, oldValue, newValue) -> setPaneTagsAndCategoriesHeight());
    entryCategoriesControl.heightProperty().addListener((observable, oldValue, newValue) -> setPaneTagsAndCategoriesHeight());

    setCategoriesPaneVisibility();
  }

  protected void setPaneTagsAndCategoriesHeight() {
    // TODO: i just don't know how to do it but paneTagsAndCategories never resized correctly
    // right now if entryTagsControl or entryCategoriesControl is expanded it uses rest of dialog an other controls are now shown
    double height = entryTagsControl.getHeight() > entryCategoriesControl.getHeight() ? entryTagsControl.getHeight() : entryCategoriesControl.getHeight();
//    paneTagsAndCategories.setMinHeight(height);
//    paneTagsAndCategories.setPrefHeight(height);
//    paneTagsAndCategories.setMaxHeight(height);
//    paneTagsAndCategories.layout();
    entryTagsControl.setVisible(false);

    Platform.runLater(() -> {
      entryTagsControl.setVisible(true);
      Platform.runLater(() -> {
        scrpnContent.layout();
      });
    });
  }

  protected void dialogFieldsDisplayChanged(DialogsFieldsDisplay dialogsFieldsDisplay) {
//    paneTitle.setVisible(StringUtils.isNotNullOrEmpty(entry.getTitle()) || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);
    entryReferenceControl.setVisible(entry.isAReferenceSet() || (creationResult != null && creationResult.isAReferenceSet()) || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);
    entryPersonsControl.setVisible(entry.hasPersons() || (creationResult != null && creationResult.hasPersons()) || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);
    ttldpnFiles.setVisible(entry.hasFiles() || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);

    setButtonChooseFieldsToShowVisiblity(dialogsFieldsDisplay != DialogsFieldsDisplay.ShowAll && (entryReferenceControl.isVisible() == false ||
                          entryPersonsControl.isVisible() == false || ttldpnFiles.isVisible() == false));

    setCategoriesPaneVisibility();
  }

  protected void setCategoriesPaneVisibility() {
    setCategoriesPaneVisibility(Application.getSettings().showCategories());
  }

  protected void setCategoriesPaneVisibility(boolean showCategories) {
    entryCategoriesControl.setVisible(showCategories);

    if(showCategories) {
      if(paneTagsAndCategories.getItems().contains(entryCategoriesControl) == false)
        paneTagsAndCategories.getItems().add(entryCategoriesControl);
    }
    else {
      paneTagsAndCategories.getItems().remove(entryCategoriesControl);
    }
  }

  protected void referenceControlFieldChanged(FieldChangedEvent event) {
    fieldsWithUnsavedChanges.add(event.getFieldWithUnsavedChanges());
  }

  protected void setEntryValues(final Entry entry) {
//    txtfldTitle.setText(entry.getTitle());


    htmledAbstract.setHtml(entry.getAbstract());

    htmledAbstract.setExpanded(entry.hasAbstract());

    htmledContent.setHtml(entry.getContent());

    entryTagsControl.setExpanded(entry.hasTags() == false);
    entryCategoriesControl.setExpanded(entry.hasCategories() == false);

    ttldpnFiles.setExpanded(entry.hasFiles() == false);
    trtblvwFiles.setRoot(new FileRootTreeItem(entry));

    entryReferenceControl.setExpanded(entry.isAReferenceSet() == false);

    fieldsWithUnsavedChanges.clear();

    btnApplyChanges.setDisable(entry.isPersisted() == true || entry.hasContent() == false); // e.g. for new Entries created by a ContentExtractor: User should be able to save them immediately by clicking on 'Apply'

    dialogFieldsDisplayChanged(Application.getSettings().getDialogsFieldsDisplay());
  }


  @Override
  protected void closeDialog() {
    entry.removeEntityListener(entryListener);

    htmledAbstract.cleanUpControl();
    htmledContent.cleanUpControl();
    entryTagsControl.cleanUpControl();
    entryCategoriesControl.cleanUpControl();
    entryReferenceControl.cleanUpControl();
    entryPersonsControl.cleanUpControl();

    ((FileRootTreeItem)trtblvwFiles.getRoot()).cleanUpControl();

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
      creationResult.saveCreatedEntities();
  }

  protected void persistEntities() {
    boolean isSeriesUnPersisted = entry.getSeries() != null && entry.getSeries().isPersisted() == false;
    boolean isReferenceUnPersisted = entry.getReference() != null && entry.getReference().isPersisted() == false;
    boolean isReferenceSubDivisionUnPersisted = entry.getReferenceSubDivision() != null && entry.getReferenceSubDivision().isPersisted() == false;
    boolean isEntryUnPersisted = entry.isPersisted() == false;

    if(isSeriesUnPersisted)
      Application.getDeepThought().addSeriesTitle(entry.getSeries());

    if(isReferenceUnPersisted)
      Application.getDeepThought().addReference(entry.getReference());

    if(isReferenceSubDivisionUnPersisted)
      Application.getDeepThought().addReferenceSubDivision(entry.getReferenceSubDivision());

    if(isEntryUnPersisted) // a new entry
      Application.getDeepThought().addEntry(entry);
  }

  protected void saveEditedFieldsOnEntry() {
    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.EntryTitle)) {
//      entry.setTitle(txtfldTitle.getText());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.EntryTitle);
    }

    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.EntryAbstract)) {
      entry.setAbstract(htmledAbstract.getHtml());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.EntryAbstract);
    }

    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.EntryContent)) {
      entry.setContent(htmledContent.getHtml());
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
        removedCategory.removeEntry(entry);
      entryCategoriesControl.getRemovedCategories().clear();

      for(Category addedCategory : entryCategoriesControl.getAddedCategories())
        addedCategory.addEntry(entry);
      entryCategoriesControl.getAddedCategories().clear();

      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.EntryCategories);
    }

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

//    if(paneTitle.isVisible() == false)
//      createHiddenFieldMenuItem(hiddenFieldsMenu, paneTitle, "title");
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

    if(ttldpnFiles.isVisible() == false)
      createHiddenFieldMenuItem(hiddenFieldsMenu, ttldpnFiles, "files");

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
          entry.addFile(newFile); // TODO: no, don't add file directly, wait till 'OK' button has been pressed
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

    contentPane.minHeightProperty().bind(windowStage.getScene().heightProperty().subtract(pnBottomBar.heightProperty()).subtract(8));

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
    entryCategoriesControl.setExpanded(creationResult.getCategories().size() == 0);

    entryReferenceControl.setExpanded(creationResult.isAReferenceSet() == false);
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


  protected HtmlEditorListener abstractListener = new HtmlEditorListener() {
    @Override
    public void htmlCodeUpdated(String newHtmlCode) {
      fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.EntryAbstract);
    }
  };

  protected HtmlEditorListener contentListener = new HtmlEditorListener() {
    @Override
    public void htmlCodeUpdated(String newHtmlCode) {
      fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.EntryContent);
    }
  };


}

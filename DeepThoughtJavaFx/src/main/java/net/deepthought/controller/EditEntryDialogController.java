package net.deepthought.controller;

import net.deepthought.Application;
import net.deepthought.communication.listener.CaptureImageOrDoOcrResponseListener;
import net.deepthought.communication.listener.ConnectedDevicesListener;
import net.deepthought.communication.model.ConnectedDevice;
import net.deepthought.controller.enums.DialogResult;
import net.deepthought.controller.enums.FieldWithUnsavedChanges;
import net.deepthought.controls.Constants;
import net.deepthought.controls.ContextHelpControl;
import net.deepthought.controls.FXUtils;
import net.deepthought.controls.categories.EntryCategoriesControl;
import net.deepthought.controls.event.FieldChangedEvent;
import net.deepthought.controls.file.FileRootTreeItem;
import net.deepthought.controls.file.FileTreeTableCell;
import net.deepthought.controls.html.DeepThoughtHTMLEditor;
import net.deepthought.controls.person.EntryPersonsControl;
import net.deepthought.controls.reference.EntryReferenceControl;
import net.deepthought.controls.tag.EntryTagsControl;
import net.deepthought.data.contentextractor.ocr.TextRecognitionResult;
import net.deepthought.data.model.Category;
import net.deepthought.data.model.Device;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.FileLink;
import net.deepthought.data.model.Person;
import net.deepthought.data.model.Reference;
import net.deepthought.data.model.ReferenceBase;
import net.deepthought.data.model.ReferenceSubDivision;
import net.deepthought.data.model.SeriesTitle;
import net.deepthought.data.model.Tag;
import net.deepthought.data.model.listener.EntityListener;
import net.deepthought.data.model.listener.SettingsChangedListener;
import net.deepthought.data.model.settings.enums.DialogsFieldsDisplay;
import net.deepthought.data.model.settings.enums.Setting;
import net.deepthought.data.persistence.db.BaseEntity;
import net.deepthought.data.persistence.db.TableConfig;
import net.deepthought.util.IconManager;
import net.deepthought.util.JavaFxLocalization;
import net.deepthought.util.Localization;
import net.deepthought.util.StringUtils;

import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.Dialogs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Collection;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventTarget;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.HTMLEditor;
import javafx.stage.Stage;
import javafx.util.Callback;

/**
 * Created by ganymed on 21/12/14.
 */
public class EditEntryDialogController extends ChildWindowsController implements Initializable {

  private final static Logger log = LoggerFactory.getLogger(EditEntryDialogController.class);


  protected Entry entry = null;

  protected ObservableSet<FieldWithUnsavedChanges> fieldsWithUnsavedChanges = FXCollections.observableSet();

  protected ObservableList<FileLink> listViewFilesItems;


  @FXML
  protected BorderPane dialogPane;

  @FXML
  protected Button btnApplyChanges;

  @FXML
  protected Pane contentPane;

  @FXML
  protected Pane paneTitle;
  @FXML
  protected TextField txtfldTitle;

  @FXML
  protected Button btnChooseFieldsToShow;
  @FXML
  protected ToggleButton tglbtnShowHideContextHelp;
  protected ContextHelpControl contextHelpControl;

  @FXML
  protected TitledPane ttldpnAbstract;
  @FXML
  protected TextArea txtarAbstract;
  @FXML
  protected HTMLEditor htmledAbstract;

  @FXML
  protected TitledPane ttldpnContent;
  @FXML
  protected Pane pnConnectedDevices;
  @FXML
  protected HTMLEditor htmledContent;

//  @FXML
//  protected BorderPane paneTagsAndCategories;
//  @FXML
//  protected SplitPane paneTagsAndCategories;
  @FXML
  protected HBox paneTagsAndCategories;
//  @FXML
//  protected FlowPane paneTagsAndCategories;

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
  public void initialize(URL location, ResourceBundle resources) {
    fieldsWithUnsavedChanges.addListener(new SetChangeListener<FieldWithUnsavedChanges>() {
      @Override
      public void onChanged(Change<? extends FieldWithUnsavedChanges> c) {
        btnApplyChanges.setDisable(fieldsWithUnsavedChanges.size() == 0);
      }
    });

    Application.getSettings().addSettingsChangedListener(settingsChangedListener);

    Application.getDeepThoughtsConnector().addConnectedDevicesListener(connectedDevicesListener);

    // TODO: what to do when DeepThought changes -> close dialog
  }

  protected SettingsChangedListener settingsChangedListener = new SettingsChangedListener() {
    @Override
    public void settingsChanged(Setting setting, Object previousValue, Object newValue) {
      if (setting == Setting.UserDeviceShowCategories) {
        entryCategoriesControl.setVisible((boolean) newValue);
        if ((boolean) newValue) {
          paneTagsAndCategories.getChildren().add(entryCategoriesControl);
//            contentPane.getChildren().add(entryCategoriesControl);
        } else
          paneTagsAndCategories.getChildren().remove(entryCategoriesControl);
//            contentPane.getChildren().remove(entryCategoriesControl);
      } else if (setting == Setting.UserDeviceDialogFieldsDisplay)
        dialogFieldsDisplayChanged((DialogsFieldsDisplay) newValue);
    }
  };

  protected void setupControls() {
    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(btnChooseFieldsToShow);

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(paneTitle);
    txtfldTitle.textProperty().addListener((observable, oldValue, newValue) -> {
      fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.EntryTitle);
      updateWindowTitle(newValue);
    });
    paneTitle.setVisible(false);
    ((Pane)paneTitle.getParent()).getChildren().remove(paneTitle); // TODO: remove paneTitle completely or leave on parent if Title doesn't get removed

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(ttldpnAbstract);
    ttldpnAbstract.setExpanded(false);

    htmledAbstract = new DeepThoughtHTMLEditor();
    ttldpnAbstract.setContent(htmledAbstract);
    FXUtils.addHtmlEditorTextChangedListener(htmledAbstract, editor -> {
      fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.EntryAbstract);
    });

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(ttldpnContent);
    htmledContent = new DeepThoughtHTMLEditor();
    ttldpnContent.setContent(htmledContent);
//    txtarContent.textProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.EntryContent));
    FXUtils.addHtmlEditorTextChangedListener(htmledContent, editor -> {
      fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.EntryContent);
    });

    for(ConnectedDevice connectedDevice : Application.getDeepThoughtsConnector().getConnectedDevicesManager().getConnectedDevices()) {
      if(connectedDevice.hasCaptureDevice() || connectedDevice.canDoOcr())
        addConnectedDeviceIcon(connectedDevice);
    }

    entryTagsControl = new EntryTagsControl(entry);
    entryTagsControl.setTagAddedEventHandler(event -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.EntryTags));
    entryTagsControl.setTagRemovedEventHandler(event -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.EntryTags));
    entryTagsControl.setMinWidth(150);
//    entryTagsControl.setPrefWidth(250);
    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(entryTagsControl);
    entryTagsControl.setExpanded(true);
//    contentPane.getChildren().add(entryTagsControl);
    HBox.setHgrow(entryTagsControl, Priority.ALWAYS);
    paneTagsAndCategories.getChildren().add(entryTagsControl);

    entryCategoriesControl = new EntryCategoriesControl(entry);
    entryCategoriesControl.setCategoryAddedEventHandler(event -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.EntryCategories));
    entryCategoriesControl.setCategoryRemovedEventHandler(event -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.EntryCategories));
    entryCategoriesControl.setMinWidth(150);
//    entryCategoriesControl.setPrefWidth(250);
    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(entryCategoriesControl);
    entryCategoriesControl.setVisible(Application.getSettings().showCategories());
    entryCategoriesControl.setExpanded(true);
//    contentPane.getChildren().addAll(entryCategoriesControl);
    HBox.setHgrow(entryCategoriesControl, Priority.ALWAYS);
    HBox.setMargin(entryCategoriesControl, new Insets(0, 0, 0, 12));
    paneTagsAndCategories.getChildren().add(entryCategoriesControl);

    entryReferenceControl = new EntryReferenceControl(entry, event -> referenceControlFieldChanged(event));
    entryReferenceControl.setExpanded(false);
    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(entryReferenceControl);
    VBox.setVgrow(entryReferenceControl, Priority.SOMETIMES);
    VBox.setMargin(entryReferenceControl, new Insets(6, 0, 0, 0));
    contentPane.getChildren().add(contentPane.getChildren().size() - 1, entryReferenceControl);

    entryPersonsControl = new EntryPersonsControl(entry);
    entryPersonsControl.setExpanded(false);
    entryPersonsControl.setPersonAddedEventHandler((event) -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.EntryPersons));
    entryPersonsControl.setPersonRemovedEventHandler((event) -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.EntryPersons));
    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(entryPersonsControl);
    VBox.setVgrow(entryPersonsControl, Priority.SOMETIMES);
    VBox.setMargin(entryPersonsControl, new Insets(6, 0, 0, 0));
    contentPane.getChildren().add(contentPane.getChildren().size() - 1, entryPersonsControl);

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


    contextHelpControl = new ContextHelpControl("context.help.entry.");
    dialogPane.setRight(contextHelpControl);

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(contextHelpControl);
    contextHelpControl.visibleProperty().bind(tglbtnShowHideContextHelp.selectedProperty());

    tglbtnShowHideContextHelp.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    tglbtnShowHideContextHelp.setGraphic(new ImageView(Constants.ContextHelpIconPath));
  }

  protected void dialogFieldsDisplayChanged(DialogsFieldsDisplay dialogsFieldsDisplay) {
    btnChooseFieldsToShow.setVisible(dialogsFieldsDisplay != DialogsFieldsDisplay.ShowAll);

//    paneTitle.setVisible(StringUtils.isNotNullOrEmpty(entry.getTitle()) || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);
    entryReferenceControl.setVisible(entry.isAReferenceSet() || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);
    entryPersonsControl.setVisible(entry.hasPersons() || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);
    ttldpnFiles.setVisible(entry.hasFiles() || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);
  }

  protected void referenceControlFieldChanged(FieldChangedEvent event) {
    fieldsWithUnsavedChanges.add(event.getFieldWithUnsavedChanges());
  }

  protected Boolean doesSearchTermMatchReference(FXUtils.DoesItemMatchSearchTermParam<Reference> param) {
    return param.getItem().getTitle().toLowerCase().contains(param.getSearchTerm().toLowerCase());
  }

  protected void setEntryValues(final Entry entry) {
//    txtfldTitle.setText(entry.getTitle());


    htmledAbstract.setHtmlText(entry.getAbstract());
    ttldpnAbstract.setExpanded(entry.hasAbstract());

    htmledContent.setHtmlText(entry.getContent());

    entryTagsControl.setExpanded(entry.hasTags() == false);
    entryCategoriesControl.setExpanded(entry.hasCategories() == false);

    ttldpnFiles.setExpanded(entry.hasFiles() == false);
    trtblvwFiles.setRoot(new FileRootTreeItem(entry));

    entryReferenceControl.setExpanded(entry.isAReferenceSet() == false);

    fieldsWithUnsavedChanges.clear();

    btnApplyChanges.setDisable(entry.isPersisted() == true || entry.hasContent() == false); // e.g. for new Entries created by a ContentExtractor: User should be able to save them immediately by clicking on 'Apply'

    dialogFieldsDisplayChanged(Application.getSettings().getDialogsFieldsDisplay());
  }


  @FXML
  public void handleButtonApplyAction(ActionEvent actionEvent) {
    saveEntry();
  }

  @FXML
  public void handleButtonCancelAction(ActionEvent actionEvent) {
    closeDialog(DialogResult.Cancel);
  }

  @FXML
  public void handleButtonOkAction(ActionEvent actionEvent) {
    saveEntry();
    closeDialog(DialogResult.Ok);
  }

  @Override
  protected void closeDialog() {
    entry.removeEntityListener(entryListener);
    Application.getSettings().removeSettingsChangedListener(settingsChangedListener);
    Application.getDeepThoughtsConnector().removeConnectedDevicesListener(connectedDevicesListener);

    entryTagsControl.cleanUpControl();
    entryCategoriesControl.cleanUpControl();
    entryReferenceControl.cleanUpControl();
    entryPersonsControl.cleanUpControl();

    ((FileRootTreeItem)trtblvwFiles.getRoot()).cleanUpControl();

    super.closeDialog();
  }

  protected void saveEntry() {
    persistEntitiesIfNecessary();

    saveEditedFieldsOnEntry();
  }

  protected void persistEntitiesIfNecessary() {
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
      if(FXUtils.hasHtmlEditorDefaultText(htmledAbstract)) {
        if(StringUtils.isNotNullOrEmpty(entry.getAbstract()))
          entry.setAbstract("");
      }
      else
        entry.setAbstract(htmledAbstract.getHtmlText());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.EntryAbstract);
    }

    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.EntryContent)) {
      if(FXUtils.hasHtmlEditorDefaultText(htmledContent)) {
        if(StringUtils.isNotNullOrEmpty(entry.getContent()))
          entry.setContent("");
      }
      else
        entry.setContent(htmledContent.getHtmlText());
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
  protected boolean askIfStageShouldBeClosed() {
    if(hasUnsavedChanges()) {
      Action response = Dialogs.create()
          .owner(windowStage)
          .title("Entry contains unsaved changes")
          .message("Entry contains unsaved changes. Do you like to save changes now?")
          .actions(Dialog.ACTION_CANCEL, Dialog.ACTION_NO, Dialog.ACTION_YES)
          .showConfirm();

      if(response.equals(Dialog.ACTION_CANCEL))
        return false;
      else if(response.equals(Dialog.ACTION_YES)) {
        saveEntry();
      }
    }

    return true;
  }


  @FXML
  public void handleButtonChooseFieldsToShowAction(ActionEvent event) {
    ContextMenu hiddenFieldsMenu = new ContextMenu();

//    if(paneTitle.isVisible() == false)
//      createHiddenFieldMenuItem(hiddenFieldsMenu, paneTitle, "title");
    if(ttldpnAbstract.isVisible() == false)
      createHiddenFieldMenuItem(hiddenFieldsMenu, ttldpnAbstract, "entry.abstract");
    if(ttldpnContent.isVisible() == false)
      createHiddenFieldMenuItem(hiddenFieldsMenu, ttldpnContent, "content");

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
  }

  protected void createHiddenFieldMenuItem(ContextMenu hiddenFieldsMenu, final Node nodeToShowOnClick, String menuItemText) {
    MenuItem titleMenuItem = new MenuItem();
    JavaFxLocalization.bindMenuItemText(titleMenuItem, menuItemText);
    hiddenFieldsMenu.getItems().add(titleMenuItem);
    titleMenuItem.setOnAction(event -> {
      nodeToShowOnClick.setVisible(true);

      if(nodeToShowOnClick instanceof TitledPane)
        ((TitledPane)nodeToShowOnClick).setExpanded(true);
    });
  }


  @FXML
  public void handleButtonAddFileAction(ActionEvent event) {
    final FileLink newFile = new FileLink();

    net.deepthought.controller.Dialogs.showEditFileDialog(newFile, new ChildWindowsControllerListener() {
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
    super.setWindowStage(windowStage);
    this.entry = entry;

    updateWindowTitle(entry.getPreview());

    setupControls();

    setEntryValues(entry);
    entry.addEntityListener(entryListener);

    // TODO: for a better user experience it would be better if Content editor is focused by default so that user can start editing Content right away, but that's not working with HtmlEditor
//    FXUtils.focusNode(htmledContent);
  }

  protected void showContextHelpForTarget(MouseEvent event) {
    EventTarget target = event.getTarget();
    log.debug("Target has been {}, source {}", target, event.getSource());
    if(target instanceof Node && ("txtfldSearchForPerson".equals(((Node)target).getId()) || isNodeChildOf((Node)target, entryPersonsControl)))
      contextHelpControl.showContextHelpForResourceKey("search.person");
    else  // TODO: add Context Help for other fields
      contextHelpControl.showContextHelpForResourceKey("default");
  }

  protected boolean isNodeChildOf(Node node, Node parentToSearchFor) {
    Parent parent = node.getParent();

    while(parent != null) {
      if(parent.equals(parentToSearchFor))
        return true;

      parent = parent.getParent();
    }

    return false;
  }

  public boolean hasUnsavedChanges() {
    return fieldsWithUnsavedChanges.size() > 0;
  }

  protected void updateWindowTitle(String entryTitle) {
    if(this.entry.isPersisted() == false)
      windowStage.setTitle(Localization.getLocalizedString("create.entry", entryTitle));
    else
      windowStage.setTitle(Localization.getLocalizedString("edit.entry", entryTitle));
  }


  protected EntityListener entryListener = new EntityListener() {
    @Override
    public void propertyChanged(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {
      if(propertyName.equals(TableConfig.EntryAbstractColumnName)) {
        if(htmledAbstract.getHtmlText().equals(((Entry) entity).getAbstract()) == false) // don't update Html Control if change has been committed by it
          htmledAbstract.setHtmlText(((Entry) entity).getAbstract());
      }
      else if(propertyName.equals(TableConfig.EntryContentColumnName)) {
        if(htmledContent.getHtmlText().equals(((Entry) entity).getContent()) == false) // don't update Html Control if change has been committed by it
          htmledContent.setHtmlText(((Entry) entity).getContent());
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


  protected ConnectedDevicesListener connectedDevicesListener = new ConnectedDevicesListener() {

    @Override
    public void registeredDeviceConnected(ConnectedDevice device) {
      Platform.runLater(() -> addConnectedDeviceIcon(device));
    }

    @Override
    public void registeredDeviceDisconnected(ConnectedDevice device) {
      Platform.runLater(() -> removeConnectedDeviceIcon(device));
    }
  };

  protected void addConnectedDeviceIcon(final ConnectedDevice connectedDevice) {
    Device device = connectedDevice.getDevice();

    ImageView icon = new ImageView(IconManager.getInstance().getIconForOperatingSystem(device.getPlatform(), device.getOsVersion(), device.getPlatformArchitecture()));
    icon.setPreserveRatio(true);
    icon.setFitHeight(24);
    icon.maxHeight(24);
//    icon.setUserData(connectedDevice);

//    pnConnectedDevices.getChildren().add(icon);
//    HBox.setMargin(icon, new Insets(0, 4, 0, 0));

    Label label = new Label(null, icon);
    label.setUserData(connectedDevice);
    JavaFxLocalization.bindControlToolTip(label, "connected.device.tool.tip", connectedDevice.getDevice().getPlatform(), connectedDevice.getDevice().getOsVersion(),
        connectedDevice.getAddress(), connectedDevice.hasCaptureDevice(), connectedDevice.canDoOcr());

    pnConnectedDevices.getChildren().add(label);
    HBox.setMargin(label, new Insets(0, 4, 0, 0));
    label.setOnContextMenuRequested(event -> createConnectedDeviceContextMenu(connectedDevice, label));
  }

  protected void createConnectedDeviceContextMenu(final ConnectedDevice connectedDevice, Node icon) {
    ContextMenu contextMenu = new ContextMenu();

    if(connectedDevice.hasCaptureDevice()) {
      MenuItem captureImageMenuItem = new MenuItem(); // TODO: add icon
      JavaFxLocalization.bindMenuItemText(captureImageMenuItem, "capture.image");
      captureImageMenuItem.setOnAction(event -> Application.getDeepThoughtsConnector().getCommunicator().startCaptureImage(connectedDevice, captureImageOrDoOcrResponseListener));
      contextMenu.getItems().add(captureImageMenuItem);
    }

    if(connectedDevice.canDoOcr()) {
      MenuItem captureImageMenuItem = new MenuItem(); // TODO: add icon
      JavaFxLocalization.bindMenuItemText(captureImageMenuItem, "do.ocr");
      captureImageMenuItem.setOnAction(event -> {
        // TODO: load image which text should be recognized
//        Application.getDeepThoughtsConnector().getCommunicator().startCaptureImage(connectedDevice, captureImageOrDoOcrResponseListener);
      });
      contextMenu.getItems().add(captureImageMenuItem);
    }

    if(connectedDevice.hasCaptureDevice() && connectedDevice.canDoOcr()) {
      MenuItem captureImageMenuItem = new MenuItem(); // TODO: add icon
      JavaFxLocalization.bindMenuItemText(captureImageMenuItem, "capture.image.and.do.ocr");
      captureImageMenuItem.setOnAction(event -> Application.getDeepThoughtsConnector().getCommunicator().startCaptureImageAndDoOcr(connectedDevice, captureImageOrDoOcrResponseListener));
      contextMenu.getItems().add(captureImageMenuItem);
    }

    contextMenu.show(icon, Side.BOTTOM, 0, 0);
  }

  protected void removeConnectedDeviceIcon(ConnectedDevice device) {
    for(Node node : pnConnectedDevices.getChildren()) {
      if(/*node instanceof ImageView &&*/ device.equals(node.getUserData())) { // TODO: will this ever return true as ConnectedDevice instance should be a different one than in  registeredDeviceConnected event
        pnConnectedDevices.getChildren().remove(node); // TODO: will foreach loop throw exception immediately or at next iteration (which would be ok than; but must be that way)
        break;
      }
    }
  }

  protected CaptureImageOrDoOcrResponseListener captureImageOrDoOcrResponseListener = new CaptureImageOrDoOcrResponseListener() {
    @Override
    public void ocrResult(final TextRecognitionResult ocrResult) {
      if(ocrResult.recognitionSuccessful())
        Platform.runLater(() -> htmledContent.setHtmlText(htmledContent.getHtmlText() + ocrResult.getRecognizedText()));
    }
  };

}

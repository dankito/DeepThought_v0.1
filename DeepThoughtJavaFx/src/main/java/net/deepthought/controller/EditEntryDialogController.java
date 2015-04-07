package net.deepthought.controller;

import com.sun.webkit.WebPage;

import net.deepthought.Application;
import net.deepthought.controller.enums.DialogResult;
import net.deepthought.controller.enums.FieldWithUnsavedChanges;
import net.deepthought.controls.Constants;
import net.deepthought.controls.FXUtils;
import net.deepthought.controls.categories.EntryCategoriesControl;
import net.deepthought.controls.event.FieldChangedEvent;
import net.deepthought.controls.file.FileRootTreeItem;
import net.deepthought.controls.file.FileTreeTableCell;
import net.deepthought.controls.person.EntryPersonsControl;
import net.deepthought.controls.reference.EntryReferenceControl;
import net.deepthought.controls.tag.EntryTagsControl;
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
import net.deepthought.data.model.listener.SettingsChangedListener;
import net.deepthought.data.model.settings.enums.DialogsFieldsDisplay;
import net.deepthought.data.model.settings.enums.Setting;
import net.deepthought.data.persistence.db.BaseEntity;
import net.deepthought.util.JavaFxLocalization;
import net.deepthought.util.Localization;
import net.deepthought.util.StringUtils;

import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.Dialogs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import java.lang.reflect.Field;
import java.net.URL;
import java.util.Collection;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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
import javafx.scene.control.Control;
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
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.HTMLEditor;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
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
  protected Button btnApplyChanges;

  @FXML
  protected Pane contentPane;

  @FXML
  protected Pane paneFirstLine;

  @FXML
  protected Pane paneTitle;
  @FXML
  protected TextField txtfldTitle;

  @FXML
  protected Pane paneViewConfig;
  @FXML
  protected Button btnChooseFieldsToShow;
  @FXML
  protected ToggleButton tglbtnShowHideContextHelp;

  @FXML
  protected TitledPane ttldpnAbstract;
  @FXML
  protected TextArea txtarAbstract;

  @FXML
  protected TitledPane ttldpnContent;
  @FXML
  protected TextArea txtarContent;
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

  @FXML
  protected Control paneContextHelp;
  @FXML
  protected WebView wbvwContextHelp;


  @Override
  public void initialize(URL location, ResourceBundle resources) {
    fieldsWithUnsavedChanges.addListener(new SetChangeListener<FieldWithUnsavedChanges>() {
      @Override
      public void onChanged(Change<? extends FieldWithUnsavedChanges> c) {
        btnApplyChanges.setDisable(fieldsWithUnsavedChanges.size() == 0);
      }
    });

    Application.getSettings().addSettingsChangedListener(new SettingsChangedListener() {
      @Override
      public void settingsChanged(Setting setting, Object previousValue, Object newValue) {
        if(setting == Setting.UserDeviceShowCategories) {
          entryCategoriesControl.setVisible((boolean) newValue);
          if((boolean)newValue) {
            paneTagsAndCategories.getChildren().add(entryCategoriesControl);
//            contentPane.getChildren().add(entryCategoriesControl);
          }
          else
            paneTagsAndCategories.getChildren().remove(entryCategoriesControl);
//            contentPane.getChildren().remove(entryCategoriesControl);
        }
        else if(setting == Setting.UserDeviceDialogFieldsDisplay)
          dialogFieldsDisplayChanged((DialogsFieldsDisplay)newValue);
      }
    });

//    Application.getDeepThought().addPersonsChangedListener(personsChangedListener);
    Application.getDeepThought().addEntityListener(deepThoughtListener);
    // TODO: what to do when DeepThought changes -> close dialog
  }

  protected void setupControls() {
    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(btnApplyChanges);

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(btnChooseFieldsToShow);

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(paneTitle);
    txtfldTitle.textProperty().addListener((observable, oldValue, newValue) -> {
      fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.EntryTitle);
      updateWindowTitle(newValue);
    });
    paneTitle.setVisible(false);

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(ttldpnAbstract);
    txtarAbstract.textProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.EntryAbstract));

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(ttldpnContent);
//    txtarContent.textProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.EntryContent));
    FXUtils.addHtmlEditorTextChangedListener(htmledContent, editor -> {
      fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.EntryContent);
    });

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
    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(entryReferenceControl);
    VBox.setMargin(entryReferenceControl, new Insets(6, 0, 6, 0));
    contentPane.getChildren().add(entryReferenceControl);

    entryPersonsControl = new EntryPersonsControl(entry);
    entryPersonsControl.setPrefHeight(250);
    entryPersonsControl.setExpanded(true);
    entryPersonsControl.setPersonAddedEventHandler((event) -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.EntryPersons));
    entryPersonsControl.setPersonRemovedEventHandler((event) -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.EntryPersons));
    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(entryPersonsControl);
    contentPane.getChildren().add(entryPersonsControl);

//    entryPersonsControl.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> showContextHelpForTarget(event));
    entryPersonsControl.addEventHandler(MouseEvent.MOUSE_ENTERED_TARGET, event -> showContextHelpForTarget(event));
    entryPersonsControl.addEventHandler(MouseEvent.MOUSE_EXITED_TARGET, event -> showContextHelp("default")); // TODO: remove as soon as other context help texts are implemented

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(ttldpnFiles);
    clmnFile.setCellFactory(new Callback<TreeTableColumn<FileLink, String>, TreeTableCell<FileLink, String>>() {
      @Override
      public TreeTableCell<FileLink, String> call(TreeTableColumn<FileLink, String> param) {
        return new FileTreeTableCell(entry);
      }
    });

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(paneContextHelp);
    paneContextHelp.visibleProperty().bind(tglbtnShowHideContextHelp.selectedProperty());
    tglbtnShowHideContextHelp.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    tglbtnShowHideContextHelp.setGraphic(new ImageView(("icons/context_help_28x30.png")));

    try {
      // Use reflection to retrieve the WebEngine's private 'page' field.
      Field f = wbvwContextHelp.getEngine().getClass().getDeclaredField("page");
      f.setAccessible(true);
      final WebPage page = (WebPage) f.get(wbvwContextHelp.getEngine());
      wbvwContextHelp.getEngine().documentProperty().addListener(new ChangeListener<Document>() {
        @Override
        public void changed(ObservableValue<? extends Document> observable, Document oldValue, Document newValue) {
          page.setBackgroundColor(Constants.ContextHelpBackgroundColor);
        }
      });
    } catch (Exception e) { }

    showContextHelp("default");
  }

  protected void dialogFieldsDisplayChanged(DialogsFieldsDisplay dialogsFieldsDisplay) {
    btnChooseFieldsToShow.setVisible(dialogsFieldsDisplay != DialogsFieldsDisplay.ShowAll);

//    paneTitle.setVisible(StringUtils.isNotNullOrEmpty(entry.getTitle()) || dialogsFieldsDisplay == DialogsFieldsDisplay.ShowAll);
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
    btnApplyChanges.setVisible(entry.isPersisted());

//    txtfldTitle.setText(entry.getTitle());


    txtarAbstract.setText(entry.getAbstract());

    htmledContent.setHtmlText(entry.getContent());
    // TODO: check which Content format Content has

    entryTagsControl.setExpanded(entry.hasTags() == false);
    entryCategoriesControl.setExpanded(entryTagsControl.isExpanded());

    ttldpnFiles.setExpanded(entry.hasFiles());
    trtblvwFiles.setRoot(new FileRootTreeItem(entry));

    entryReferenceControl.setVisible(true);

    fieldsWithUnsavedChanges.clear();

    dialogFieldsDisplayChanged(Application.getSettings().getDialogsFieldsDisplay());
  }


  @FXML
  public void handleButtonApplyAction(ActionEvent actionEvent) {
    saveEditedFieldsOnEntry();

    if(entry.isPersisted() == false) // a new Entry
      Application.getDeepThought().addEntry(entry);
  }

  @FXML
  public void handleButtonCancelAction(ActionEvent actionEvent) {
    setDialogResult(DialogResult.Cancel);
    closeDialog();
  }

  @FXML
  public void handleButtonOkAction(ActionEvent actionEvent) {
    setDialogResult(DialogResult.Ok);

    if(entry.isPersisted() == false) // a new entry
      Application.getDeepThought().addEntry(entry);

    saveEditedFieldsOnEntry();
    closeDialog();
  }

  @Override
  protected void closeDialog() {
    entry.removeEntityListener(entryListener);
    Application.getDeepThought().removeEntityListener(deepThoughtListener);

    super.closeDialog();
  }

  protected void saveEditedFieldsOnEntry() {
    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.EntryTitle)) {
//      entry.setTitle(txtfldTitle.getText());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.EntryTitle);
    }

    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.EntryAbstract)) {
      entry.setAbstract(txtarAbstract.getText());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.EntryAbstract);
    }

    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.EntryContent)) {
      if(FXUtils.HtmlEditorDefaultText.equals(htmledContent.getHtmlText())) {
        if(StringUtils.isNotNullOrEmpty(entry.getContent()))
          entry.setContent("");
      }
      else
        entry.setContent(htmledContent.getHtmlText());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.EntryContent);
    }

    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.EntrySeriesTitle) || fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.EntryReference)) {
      ReferenceBase referenceBase = entryReferenceControl.getReferenceBase();
      ReferenceSubDivision subDivision = entryReferenceControl.getReferenceSubDivision();

      if(referenceBase instanceof SeriesTitle)
        entry.setSeries((SeriesTitle)referenceBase);
      else {
        entry.setReference((Reference) referenceBase);
      }

      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.EntrySeriesTitle);
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.EntryReference);

      entry.setReferenceSubDivision(subDivision);
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.EntryReferenceSubDivision);
    }
    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.EntryReferenceSubDivision)) {
      entry.setReferenceSubDivision(entryReferenceControl.getReferenceSubDivision());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.EntryReferenceSubDivision);
    }
    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.EntryReferenceIndication)) {
      entry.setIndication(entryReferenceControl.getReferenceIndication());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.EntryReferenceIndication);
    }

    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.EntryPersons)) {
      for(Person removedPerson : entryPersonsControl.getRemovedPersons())
        entry.removePerson(removedPerson);
      entryPersonsControl.getRemovedPersons().clear();

      for(Person addedPerson : entryPersonsControl.getAddedPersons())
        entry.addPerson(addedPerson);
      entryPersonsControl.getAddedPersons().clear();

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
  }

  protected void askIfStageShouldBeClosed(WindowEvent event) {
    if(hasUnsavedChanges()) {
      Action response = Dialogs.create()
          .owner(windowStage)
          .title("Entry contains unsaved changes")
          .message("Entry contains unsaved changes. Do you like to save changes now?")
          .actions(Dialog.ACTION_CANCEL, Dialog.ACTION_NO, Dialog.ACTION_YES)
          .showConfirm();

      if(response.equals(Dialog.ACTION_CANCEL))
        event.consume(); // consume event so that stage doesn't get closed
      else if(response.equals(Dialog.ACTION_YES)) {
        saveEditedFieldsOnEntry();
        closeDialog();
      }
      else
        closeDialog();
    }
  }


  @FXML
  public void handleButtonChooseFieldsToShowAction(ActionEvent event) {
    ContextMenu hiddenFieldsMenu = new ContextMenu();

    if(paneTitle.isVisible() == false)
      createHiddenFieldMenuItem(hiddenFieldsMenu, paneTitle, "title");
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

    hiddenFieldsMenu.show(btnChooseFieldsToShow, Side.BOTTOM, 0, 0);
  }

  protected void createHiddenFieldMenuItem(ContextMenu hiddenFieldsMenu, Node nodeToShowOnClick, String menuItemText) {
    MenuItem titleMenuItem = new MenuItem();
    JavaFxLocalization.bindMenuItemText(titleMenuItem, menuItemText);
    hiddenFieldsMenu.getItems().add(titleMenuItem);
    titleMenuItem.setOnAction(event -> nodeToShowOnClick.setVisible(true));
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
          entry.addFile(newFile);
        }
      }
    });
  }


  public Entry getEntry() {
    return entry;
  }

  public void setWindowStageAndEntry(Stage windowStage, Entry entry) {
    super.setWindowStage(windowStage);
    this.entry = entry;

    updateWindowTitle(entry.getPreview());
    windowStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
      @Override
      public void handle(WindowEvent event) {
        askIfStageShouldBeClosed(event);
      }
    });

    setupControls();
    htmledContent.requestFocus();

    setEntryValues(entry);
    entry.addEntityListener(entryListener);
  }

  protected void showContextHelpForTarget(MouseEvent event) {
    EventTarget target = event.getTarget();
    log.debug("Target has been {}, source {}", target, event.getSource());
    if(target instanceof Node && ("txtfldSearchForPerson".equals(((Node)target).getId()) || isNodeChildOf((Node)target, entryPersonsControl)))
      showContextHelp("search.person");
    else  // TODO: add Context Help for other fields
      showContextHelp("default");
  }

  protected void showContextHelp(String contextHelpResourceKey) {
    wbvwContextHelp.getEngine().loadContent(Localization.getLocalizedStringForResourceKey("context.help.entry." + contextHelpResourceKey));
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
      windowStage.setTitle(Localization.getLocalizedStringForResourceKey("create.entry", entryTitle));
    else
      windowStage.setTitle(Localization.getLocalizedStringForResourceKey("edit.entry", entryTitle));
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

    }

    @Override
    public void entityOfCollectionUpdated(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity updatedEntity) {

    }

    @Override
    public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {

    }
  };

}

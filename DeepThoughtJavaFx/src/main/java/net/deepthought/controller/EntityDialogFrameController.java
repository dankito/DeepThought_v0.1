package net.deepthought.controller;

import net.deepthought.Application;
import net.deepthought.controller.enums.DialogResult;
import net.deepthought.controller.enums.FieldWithUnsavedChanges;
import net.deepthought.controls.CollapsiblePane;
import net.deepthought.controls.Constants;
import net.deepthought.controls.ContextHelpControl;
import net.deepthought.controls.FXUtils;
import net.deepthought.controls.html.HtmlEditorListener;
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
import net.deepthought.data.model.listener.SettingsChangedListener;
import net.deepthought.data.model.settings.enums.DialogsFieldsDisplay;
import net.deepthought.data.model.settings.enums.Setting;
import net.deepthought.data.persistence.db.BaseEntity;
import net.deepthought.data.persistence.db.TableConfig;
import net.deepthought.data.persistence.db.UserDataEntity;
import net.deepthought.util.Alerts;
import net.deepthought.util.JavaFxLocalization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventTarget;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/**
 * Created by ganymed on 21/12/14.
 */
public abstract class EntityDialogFrameController extends ChildWindowsController implements Initializable {

  private final static Logger log = LoggerFactory.getLogger(EntityDialogFrameController.class);


  protected UserDataEntity entity = null;

  protected ObservableSet<FieldWithUnsavedChanges> fieldsWithUnsavedChanges = FXCollections.observableSet();


  @FXML
  protected BorderPane dialogPane;

  @FXML
  protected Pane pnBottomBar;

  @FXML
  protected Button btnApplyChanges;

  @FXML
  protected Button btnChooseFieldsToShow;
  @FXML
  protected ToggleButton tglbtnShowHideContextHelp;
  protected ContextHelpControl contextHelpControl;


  protected abstract String getHelpTextResourceKeyPrefix();


  @Override
  public void initialize(URL location, ResourceBundle resources) {
    fieldsWithUnsavedChanges.addListener(new SetChangeListener<FieldWithUnsavedChanges>() {
      @Override
      public void onChanged(Change<? extends FieldWithUnsavedChanges> c) {
        fieldsWithUnsavedChangesChanged();
      }
    });

    Application.getSettings().addSettingsChangedListener(settingsChangedListener);
  }

  protected void loadFrame() {
    FXMLLoader fxmlLoader = new FXMLLoader(this.getClass().getClassLoader().getResource(Dialogs.DialogsBaseFolder + "EntityDialogFrame.fxml"));
//    fxmlLoader.setRoot(this);
    fxmlLoader.setController(this);
    fxmlLoader.setResources(JavaFxLocalization.Resources);

    try {
      Object loadedObject = fxmlLoader.load();
      if(loadedObject instanceof Node)
        JavaFxLocalization.resolveResourceKeys((Node)loadedObject);

      dialogPane.setCenter(getContent());
    } catch (IOException ex) {
      log.error("Could not load EntityDialogFrame", ex);
    }
  }

  protected abstract Node getContent();

  protected void fieldsWithUnsavedChangesChanged() {
    btnApplyChanges.setDisable(fieldsWithUnsavedChanges.size() == 0);
  }

  protected SettingsChangedListener settingsChangedListener = new SettingsChangedListener() {
    @Override
    public void settingsChanged(Setting setting, Object previousValue, Object newValue) {
      if (setting == Setting.UserDeviceDialogFieldsDisplay)
        dialogFieldsDisplayChanged((DialogsFieldsDisplay) newValue);
      EntityDialogFrameController.this.settingsChanged(setting, previousValue, newValue);
    }
  };

  protected void settingsChanged(Setting setting, Object previousValue, Object newValue) {

  }

  protected void setupControls() {
//    loadFrame();

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(btnChooseFieldsToShow);

    tglbtnShowHideContextHelp.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    tglbtnShowHideContextHelp.setGraphic(new ImageView(Constants.ContextHelpIconPath));

    contextHelpControl = new ContextHelpControl(getHelpTextResourceKeyPrefix());
    dialogPane.setRight(contextHelpControl);

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(contextHelpControl);
    contextHelpControl.visibleProperty().bind(tglbtnShowHideContextHelp.selectedProperty());
  }

  protected void setButtonChooseFieldsToShowVisiblity(boolean isVisible) {
    btnChooseFieldsToShow.setVisible(isVisible);
  }

  protected void dialogFieldsDisplayChanged(DialogsFieldsDisplay dialogsFieldsDisplay) {
    // may be overwritten in sub class
  }


  @FXML
  public void handleButtonApplyAction(ActionEvent actionEvent) {
    saveEntity();
  }

  @FXML
  public void handleButtonCancelAction(ActionEvent actionEvent) {
    closeDialog(DialogResult.Cancel);
  }

  @FXML
  public void handleButtonOkAction(ActionEvent actionEvent) {
    saveEntity();
    closeDialog(DialogResult.Ok);
  }

  @Override
  protected void closeDialog() {
    Application.getSettings().removeSettingsChangedListener(settingsChangedListener);

    super.closeDialog();
  }

  protected abstract void saveEntity();

  protected boolean hasUnsavedChanges() {
    return fieldsWithUnsavedChanges.size() > 0;
  }

  protected abstract String getEntityType();


  @Override
  protected boolean askIfStageShouldBeClosed() {
    if(hasUnsavedChanges()) {
      ButtonType result = Alerts.askUserIfEditedEntityShouldBeSaved(windowStage, getEntityType());

      if(result.equals(ButtonType.CANCEL))
        return false;
      else if(result.equals(ButtonType.YES)) {
        saveEntity();
      }
    }

    return true;
  }


  protected abstract ContextMenu createHiddenFieldsContextMenu();

  @FXML
  public void handleButtonChooseFieldsToShowAction(ActionEvent event) {
    ContextMenu hiddenFieldsMenu = createHiddenFieldsContextMenu();

    hiddenFieldsMenu.show(btnChooseFieldsToShow, Side.TOP, 0, 0);
  }

  protected void createHiddenFieldMenuItem(ContextMenu hiddenFieldsMenu, final Node nodeToShowOnClick, String menuItemText) {
    MenuItem titleMenuItem = new MenuItem();
    JavaFxLocalization.bindMenuItemText(titleMenuItem, menuItemText);
    hiddenFieldsMenu.getItems().add(titleMenuItem);
    titleMenuItem.setOnAction(event -> {
      nodeToShowOnClick.setVisible(true);

      if (nodeToShowOnClick instanceof CollapsiblePane)
        ((CollapsiblePane) nodeToShowOnClick).setExpanded(true);
    });
  }

  public void setWindowStage(final Stage windowStage, UserDataEntity entity) {
    super.setWindowStage(windowStage);
    this.entity = entity;

    updateWindowTitle();

    setupControls();
  }

  protected void showContextHelpForTarget(MouseEvent event) {
    EventTarget target = event.getTarget();
    log.debug("Target has been {}, source {}", target, event.getSource());
//    if(target instanceof Node && ("txtfldSearchForPerson".equals(((Node)target).getId()) || isNodeChildOf((Node)target, entryPersonsControl)))
//      contextHelpControl.showContextHelpForResourceKey("search.person");
//    else  // TODO: add Context Help for other fields
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

  protected void updateWindowTitle() {
    if(this.entity == null || this.entity.isPersisted() == false)
      JavaFxLocalization.bindStageTitle(windowStage, "create." + getEntityType());
    else
      JavaFxLocalization.bindStageTitle(windowStage, "edit." + getEntityType(), getEntityPreview());
  }

  protected String getEntityPreview() {
    if(entity != null)
      return entity.getTextRepresentation();
    return "";
  }


}

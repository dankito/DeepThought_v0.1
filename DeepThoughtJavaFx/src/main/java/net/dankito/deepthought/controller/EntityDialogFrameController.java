package net.dankito.deepthought.controller;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.controller.enums.DialogResult;
import net.dankito.deepthought.ui.enums.FieldWithUnsavedChanges;
import net.dankito.deepthought.controls.CollapsiblePane;
import net.dankito.deepthought.controls.ContextHelpControl;
import net.dankito.deepthought.data.listener.ApplicationListener;
import net.dankito.deepthought.data.model.DeepThought;
import net.dankito.deepthought.data.model.listener.SettingsChangedListener;
import net.dankito.deepthought.data.model.settings.enums.DialogsFieldsDisplay;
import net.dankito.deepthought.data.model.settings.enums.Setting;
import net.dankito.deepthought.data.persistence.db.UserDataEntity;
import net.dankito.deepthought.util.Alerts;
import net.dankito.deepthought.util.localization.JavaFxLocalization;
import net.dankito.deepthought.util.Notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventTarget;
import javafx.fxml.FXML;
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

  protected boolean hasApplyBeenPressed = false;


  @FXML
  protected BorderPane dialogPane;

  @FXML
  protected Pane pnBottomBar;

  @FXML
  protected Button btnCancel;
  @FXML
  protected Button btnApplyChanges;
  @FXML
  protected Button btnOk;

  @FXML
  protected Button btnChooseFieldsToShow;
  @FXML
  protected ToggleButton tglbtnShowHideContextHelp;
  protected ContextHelpControl contextHelpControl;


  protected String getHelpTextResourceKeyPrefix() {
    return "context.help." + getEntityType() + ".";
  }


  @Override
  public void initialize(URL location, ResourceBundle resources) {
    fieldsWithUnsavedChanges.addListener(new SetChangeListener<FieldWithUnsavedChanges>() {
      @Override
      public void onChanged(Change<? extends FieldWithUnsavedChanges> c) {
        fieldsWithUnsavedChangesChanged();
      }
    });

    Application.getSettings().addSettingsChangedListener(settingsChangedListener);
    Application.addApplicationListener(applicationListener);
  }

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
    // maybe overwritten in sub class
  }

  protected ApplicationListener applicationListener = new ApplicationListener() {
    @Override
    public void deepThoughtChanged(DeepThought deepThought) {

    }

    @Override
    public void notification(Notification notification) {
      notificationReceived(notification);
    }
  };

  protected void notificationReceived(Notification notification) {
    // maybe overwritten in sub class
  }


  protected void setupControls() {
    net.dankito.deepthought.controls.utils.FXUtils.ensureNodeOnlyUsesSpaceIfVisible(btnChooseFieldsToShow);
    setButtonChooseFieldsToShowVisibility(false);

    tglbtnShowHideContextHelp.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    tglbtnShowHideContextHelp.setGraphic(new ImageView(net.dankito.deepthought.controls.Constants.ContextHelpIconPath));

    contextHelpControl = new ContextHelpControl(getHelpTextResourceKeyPrefix());
    dialogPane.setRight(contextHelpControl);

    net.dankito.deepthought.controls.utils.FXUtils.ensureNodeOnlyUsesSpaceIfVisible(contextHelpControl);
    contextHelpControl.visibleProperty().bind(tglbtnShowHideContextHelp.selectedProperty());
  }

  protected void setButtonChooseFieldsToShowVisibility(boolean isVisible) {
    btnChooseFieldsToShow.setVisible(isVisible);
  }

  protected void dialogFieldsDisplayChanged(DialogsFieldsDisplay dialogsFieldsDisplay) {
    // may be overwritten in sub class
  }


  @FXML
  public void handleButtonApplyAction(ActionEvent actionEvent) {
    hasApplyBeenPressed = true;
    saveEntity();
  }

  @FXML
  public void handleButtonCancelAction(ActionEvent actionEvent) {
    if(hasApplyBeenPressed)
      closeDialog(DialogResult.ApplyAndThenCancel);
    else
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
    Application.removeApplicationListener(applicationListener);

    // added 13.03.2016
    entity = null;
    settingsChangedListener = null;
    applicationListener = null;

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


  protected ContextMenu createHiddenFieldsContextMenu() {
    return null; // may be overwritten in sub class if needed
  }

  @FXML
  public void handleButtonChooseFieldsToShowAction(ActionEvent event) {
    ContextMenu hiddenFieldsMenu = createHiddenFieldsContextMenu();

    if(hiddenFieldsMenu != null)
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

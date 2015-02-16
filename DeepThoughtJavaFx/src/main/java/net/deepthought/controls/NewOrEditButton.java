package net.deepthought.controls;

import net.deepthought.controls.event.NewOrEditButtonMenuActionEvent;
import net.deepthought.util.JavaFxLocalization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitMenuButton;

/**
 * Created by ganymed on 30/11/14.
 */
public class NewOrEditButton extends SplitMenuButton {

  private final static Logger log = LoggerFactory.getLogger(NewOrEditButton.class);


  public enum ButtonFunction { New, Edit }


  protected ButtonFunction buttonFunction = ButtonFunction.New;

  private BooleanProperty showNewMenuItem;
  public final void setShowNewMenuItem(boolean value) { showNewMenuItemProperty().set(value); }
  public final boolean showNewMenuItem() { return showNewMenuItem == null ? false : showNewMenuItem.get(); }
  public final BooleanProperty showNewMenuItemProperty() {
    if (showNewMenuItem == null) {
      showNewMenuItem = new SimpleBooleanProperty(this, "showNewMenuItem");
    }
    return showNewMenuItem;
  }

  private BooleanProperty showEditMenuItem;
  public final void setShowEditMenuItem(boolean value) { showEditMenuItemProperty().set(value); }
  public final boolean showEditMenuItem() { return showEditMenuItem == null ? false : showEditMenuItem.get(); }
  public final BooleanProperty showEditMenuItemProperty() {
    if (showEditMenuItem == null) {
      showEditMenuItem = new SimpleBooleanProperty(this, "showEditMenuItem");
    }
    return showEditMenuItem;
  }

  protected MenuItem newMenuItem;
  protected MenuItem editMenuItem;

  protected EventHandler<NewOrEditButtonMenuActionEvent> onNewMenuItemEventActionHandler = null;
  protected EventHandler<NewOrEditButtonMenuActionEvent> onEditMenuItemEventActionHandler = null;


  public NewOrEditButton() {
    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("controls/NewOrEditButton.fxml"));
    fxmlLoader.setRoot(this);
    fxmlLoader.setController(this);
//    fxmlLoader.setResources(Localization.getStringsResourceBundle());

    try {
      fxmlLoader.load();
      setupControl();
    } catch (IOException ex) {
      log.error("Could not load NewOrEditButton", ex);
    }
  }


  protected void setupControl() {
    setButtonText();

    newMenuItem = new MenuItem();
    JavaFxLocalization.bindMenuItemText(newMenuItem, "new...");
    newMenuItem.setOnAction((event) -> onNewMenuItemAction(event));
    this.getItems().add(newMenuItem);
    newMenuItem.setVisible(false);

    showNewMenuItemProperty().addListener((observable, oldValue, newValue) -> {
//      if(newValue == false)
//        NewOrEditButton.this.getItems().remove(newMenuItem);
//      else
//        NewOrEditButton.this.getItems().add(newMenuItem);

      newMenuItem.setVisible(newValue);
    });

    editMenuItem = new MenuItem();
    JavaFxLocalization.bindMenuItemText(editMenuItem, "edit");
    editMenuItem.setOnAction((event) -> onEditMenuItemAction(event));
    this.getItems().add(editMenuItem);
    editMenuItem.setVisible(false);

    showEditMenuItemProperty().addListener((observable, oldValue, newValue) -> editMenuItem.setVisible(newValue));
  }

  protected void setButtonText() {
    if(buttonFunction == ButtonFunction.New)
      JavaFxLocalization.bindLabeledText(this, "new...");
    else
      JavaFxLocalization.bindLabeledText(this, "edit");
  }


  public ButtonFunction getButtonFunction() {
    return buttonFunction;
  }

  public void setButtonFunction(ButtonFunction buttonFunction) {
    this.buttonFunction = buttonFunction;
    setButtonText();
  }

  protected void onNewMenuItemAction(ActionEvent event) {
    if(onNewMenuItemEventActionHandler != null)
      onNewMenuItemEventActionHandler.handle(new NewOrEditButtonMenuActionEvent(event, this));
  }

  protected void onEditMenuItemAction(ActionEvent event) {
    if(onEditMenuItemEventActionHandler != null)
      onEditMenuItemEventActionHandler.handle(new NewOrEditButtonMenuActionEvent(event, this));
  }

  public EventHandler<NewOrEditButtonMenuActionEvent> getOnNewMenuItemEventActionHandler() {
    return onNewMenuItemEventActionHandler;
  }

  public void setOnNewMenuItemEventActionHandler(EventHandler<NewOrEditButtonMenuActionEvent> onNewMenuItemEventActionHandler) {
    this.onNewMenuItemEventActionHandler = onNewMenuItemEventActionHandler;
  }

  public EventHandler<NewOrEditButtonMenuActionEvent> getOnEditMenuItemEventActionHandler() {
    return onEditMenuItemEventActionHandler;
  }

  public void setOnEditMenuItemEventActionHandler(EventHandler<NewOrEditButtonMenuActionEvent> onEditMenuItemEventActionHandler) {
    this.onEditMenuItemEventActionHandler = onEditMenuItemEventActionHandler;
  }
}

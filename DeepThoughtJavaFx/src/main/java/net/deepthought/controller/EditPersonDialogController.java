package net.deepthought.controller;

import net.deepthought.controller.enums.DialogResult;
import net.deepthought.controller.enums.FieldWithUnsavedChanges;
import net.deepthought.controls.Constants;
import net.deepthought.controls.ContextHelpControl;
import net.deepthought.controls.FXUtils;
import net.deepthought.data.model.Person;
import net.deepthought.data.model.listener.EntityListener;
import net.deepthought.data.persistence.db.BaseEntity;
import net.deepthought.util.Localization;

import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.Dialogs;

import java.net.URL;
import java.util.Collection;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * Created by ganymed on 31/12/14.
 */
public class EditPersonDialogController extends ChildWindowsController implements Initializable {

  protected Person person = null;

  protected ObservableSet<FieldWithUnsavedChanges> fieldsWithUnsavedChanges = FXCollections.observableSet();


  @FXML
  protected BorderPane dialogPane;

  @FXML
  protected Button btnApply;

  @FXML
  protected ToggleButton tglbtnShowHideContextHelp;

  protected ContextHelpControl contextHelpControl;

  @FXML
  protected TextField txtfldFirstName;
  @FXML
  protected TextField txtfldLastName;
  @FXML
  protected TextArea txtarNotes;


  @Override
  public void initialize(URL location, ResourceBundle resources) {
    btnApply.managedProperty().bind(btnApply.visibleProperty());

    setupFields();

    fieldsWithUnsavedChanges.addListener(new SetChangeListener<FieldWithUnsavedChanges>() {
      @Override
      public void onChanged(Change<? extends FieldWithUnsavedChanges> c) {
        btnApply.setDisable(fieldsWithUnsavedChanges.size() == 0);
      }
    });
  }

  protected void setupFields() {
    txtfldFirstName.textProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.PersonFirstName));
    txtfldFirstName.focusedProperty().addListener((observable, oldValue, newValue) -> fieldFocused("first.name"));

    txtfldLastName.textProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.PersonLastName));
    txtfldLastName.focusedProperty().addListener((observable, oldValue, newValue) -> fieldFocused("last.name"));

    txtarNotes.textProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.PersonNotes));
    txtarNotes.focusedProperty().addListener((observable, oldValue, newValue) -> fieldFocused("notes"));

    contextHelpControl = new ContextHelpControl("context.help.person.");
    dialogPane.setRight(contextHelpControl);

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(contextHelpControl);
    contextHelpControl.visibleProperty().bind(tglbtnShowHideContextHelp.selectedProperty());

    tglbtnShowHideContextHelp.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    tglbtnShowHideContextHelp.setGraphic(new ImageView(Constants.ContextHelpIconPath));
  }

  protected void fieldFocused(String fieldName) {
    contextHelpControl.showContextHelpForResourceKey(fieldName);
  }


  public void setPersonAndStage(Stage dialogStage, Person personToEdit) {
    setWindowStage(dialogStage);
    this.person = personToEdit;

    updateStageTitle();
    btnApply.setVisible(personToEdit.isPersisted());

    personToEditSet(personToEdit);
    fieldsWithUnsavedChanges.clear();
    person.addEntityListener(personListener);

    txtfldFirstName.selectAll();
    txtfldFirstName.requestFocus();
  }

  protected void personToEditSet(Person person) {
    txtfldFirstName.setText(person.getFirstName());
    txtfldLastName.setText(person.getLastName());
    txtarNotes.setText(person.getNotes());
  }

  public boolean hasUnsavedChanges() {
    return fieldsWithUnsavedChanges.size() > 0;
  }

  protected void updateStageTitle() {
    if(person.isPersisted() == false)
      windowStage.setTitle(Localization.getLocalizedString("create.person", person.getNameRepresentation()));
    else
      windowStage.setTitle(Localization.getLocalizedString("edit.person", person.getNameRepresentation()));
  }


  @FXML
  public void handleButtonApplyAction(ActionEvent actionEvent) {
    saveEditedFields();
  }

  @FXML
  public void handleButtonCancelAction(ActionEvent actionEvent) {
    setDialogResult(DialogResult.Cancel);
    closeDialog();
  }

  @FXML
  public void handleButtonOkAction(ActionEvent actionEvent) {
    setDialogResult(DialogResult.Ok);
    saveEditedFields();
    closeDialog();
  }

  @Override
  protected void closeDialog() {
    if(person != null)
      person.removeEntityListener(personListener);

    super.closeDialog();
  }

  protected void saveEditedFields() {
    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.PersonFirstName)) {
      person.setFirstName(txtfldFirstName.getText());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.PersonFirstName);
    }
    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.PersonLastName)) {
      person.setLastName(txtfldLastName.getText());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.PersonLastName);
    }

    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.PersonNotes)) {
      person.setNotes(txtarNotes.getText());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.PersonNotes);
    }
  }

  @Override
  public void setWindowStage(Stage windowStage) {
    super.setWindowStage(windowStage);

    windowStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
      @Override
      public void handle(WindowEvent event) {
        askIfStageShouldBeClosed(event);
      }
    });
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
        saveEditedFields();
        closeDialog();
      }
      else
        closeDialog();
    }
  }


  protected EntityListener personListener = new EntityListener() {
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

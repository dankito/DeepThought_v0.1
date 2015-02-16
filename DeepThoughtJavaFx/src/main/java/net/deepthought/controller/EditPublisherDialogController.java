package net.deepthought.controller;

import net.deepthought.controller.enums.DialogResult;
import net.deepthought.data.model.Publisher;
import net.deepthought.util.Localization;

import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * Created by ganymed on 31/12/14.
 */
public class EditPublisherDialogController extends ChildWindowsController implements Initializable {


  protected Publisher publisher;


  @FXML
  protected Button btnApply;

  @FXML
  protected TextField txtfldName;

  @FXML
  protected TextArea txtarNotes;


  @Override
  public void initialize(URL location, ResourceBundle resources) {
    btnApply.managedProperty().bind(btnApply.visibleProperty());

    setupFields();
  }

  protected void setupFields() {
    txtfldName.textProperty().addListener((observable, oldValue, newValue) -> {
      btnApply.setDisable(false);
      updateWindowTitle();
    });

    txtarNotes.textProperty().addListener(new ChangeListener<String>() {
      @Override
      public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        btnApply.setDisable(false);
      }
    });
  }

  public void setWindowStageAndPublisher(Stage dialogStage, Publisher publisher) {
    setWindowStage(dialogStage);
    this.publisher = publisher;

    updateWindowTitle();
    btnApply.setVisible(publisher.getId() != null);
    btnApply.setDisable(true);

    setPublisherValues(publisher);
  }

  protected void setPublisherValues(Publisher publisher) {
    txtfldName.setText(publisher.getName());
    txtarNotes.setText(publisher.getNotes());
  }

  protected void updateWindowTitle() {
    if(publisher.getId() == null)
      windowStage.setTitle(Localization.getLocalizedStringForResourceKey("create.publisher"));
    else
      windowStage.setTitle(Localization.getLocalizedStringForResourceKey("edit.publisher", publisher.getTextRepresentation()));
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
//    if(person != null)
//      person.removePersonListener(personListener);

    super.closeDialog();
  }

  protected void saveEditedFields() {
    if(txtfldName.getText().equals(publisher.getName()) == false)
      publisher.setName(txtfldName.getText());

    if(txtarNotes.getText() != null && txtarNotes.getText().equals(publisher.getNotes()) == false)
      publisher.setNotes(txtarNotes.getText());

    btnApply.setDisable(true);
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
      Action response = org.controlsfx.dialog.Dialogs.create()
          .owner(windowStage)
          .title(Localization.getLocalizedStringForResourceKey("alert.message.title.publisher.contains.unsaved.changes"))
          .message(Localization.getLocalizedStringForResourceKey("alert.message.message.publisher.contains.unsaved.changes"))
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

  protected boolean hasUnsavedChanges() {
    return txtfldName.getText().equals(publisher.getName()) == false || txtarNotes.getText().equals(publisher.getNotes()) == false;
  }

}

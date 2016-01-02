package net.deepthought.controller;

import net.deepthought.Application;
import net.deepthought.controller.enums.FieldWithUnsavedChanges;
import net.deepthought.controller.enums.FileLinkOptions;
import net.deepthought.data.model.FileLink;
import net.deepthought.data.model.enums.FileType;
import net.deepthought.util.Alerts;
import net.deepthought.util.DeepThoughtError;
import net.deepthought.util.Localization;
import net.deepthought.util.StringUtils;
import net.deepthought.util.file.FileNameSuggestion;
import net.deepthought.util.file.FileUtils;
import net.deepthought.util.file.enums.ExistingFileHandling;
import net.deepthought.util.file.listener.FileOperationListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * Created by ganymed on 31/12/14.
 */
public class EditFileDialogController extends EntityDialogFrameController implements Initializable {

  private final static Logger log = LoggerFactory.getLogger(EditFileDialogController.class);


  protected FileLink file;

  protected String previousUriString = null;

  protected FileType detectedFileType = null;


  @FXML
  protected RadioButton rdbtnFileOrUrl;
  @FXML
  protected Pane pnFileSettings;
  @FXML
  protected TextField txtfldFileLocation;
  @FXML
  protected Label lblFileType;
  @FXML
  protected ComboBox<FileLinkOptions> cmbxLocalFileLinkOptions;

  @FXML
  protected RadioButton rdbtnFolder;
  @FXML
  protected Pane pnFolderSettings;
  @FXML
  protected TextField txtfldFolderLocation;

  @FXML
  protected TextField txtfldFileName;

  @FXML
  protected TextArea txtarDescription;


  @Override
  protected String getEntityType() {
    return "file";
  }


  @Override
  protected void setupControls() {
    super.setupControls();

    cmbxLocalFileLinkOptions.setItems(FXCollections.observableArrayList(FileLinkOptions.values()));
    cmbxLocalFileLinkOptions.getSelectionModel().select(FileLinkOptions.Link);

    rdbtnFileOrUrl.selectedProperty().addListener((observable, oldValue, newValue) -> {
      fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.FileIsFolder);
      pnFileSettings.setDisable(false);
      pnFolderSettings.setDisable(true);
      updateWindowTitle();
    });

    txtfldFileLocation.textProperty().addListener((observable, oldValue, newValue) -> {
      fileLocationChanged();
    });

    rdbtnFolder.selectedProperty().addListener((observable, oldValue, newValue) -> {
      fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.FileIsFolder);
      pnFileSettings.setDisable(true);
      pnFolderSettings.setDisable(false);
      updateWindowTitle();
    });

    txtfldFolderLocation.textProperty().addListener((observable, oldValue, newValue) -> {
      fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.FileFolderLocation);
      updateWindowTitle();
      updateFileName(txtfldFolderLocation.getText());
    });

    txtfldFileName.textProperty().addListener((observable, oldValue, newValue) -> {
      fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.FileName);
      updateWindowTitle();
    });

    txtarDescription.textProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.FileNotes));
  }

  protected void fileLocationChanged() {
    fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.FileFileLocation);
    updateWindowTitle();
    updateFileName(txtfldFileLocation.getText());

    try {
      String scheme = new URI(txtfldFileLocation.getText()).getScheme();
      cmbxLocalFileLinkOptions.setDisable(scheme != null && "file".equals(scheme) == false);
    } catch(Exception ex) { }
  }

  protected void updateFileName(String uriString) {
    if(uriString == null)
      return;

    try {
        if(previousUriString == null || previousUriString.endsWith(txtfldFileName.getText())) {
          File file = new File(uriString);
          txtfldFileName.setText(file.getName());
        }
        else if(previousUriString != null && previousUriString.endsWith("/") &&
            (StringUtils.isNullOrEmpty(txtfldFileName.getText()) || previousUriString.endsWith(txtfldFileName.getText() + "/"))) {
          File file = new File(uriString);
          txtfldFileName.setText(file.getName());
        }

      detectedFileType = FileUtils.getFileType(uriString);
      lblFileType.setText(detectedFileType.getName());
    } catch(Exception ex) {
      log.debug("Could not extract file's name from uriString " + uriString, ex);
    }

    previousUriString = uriString;
  }

  public void setEditFile(Stage dialogStage, FileLink file) {
    this.file = file;
    setWindowStage(dialogStage, file);

    fileToEditSet(file);
    fieldsWithUnsavedChanges.clear();
  }

  protected void fileToEditSet(FileLink file) {
    if(file.isFolder() == false) {
      rdbtnFileOrUrl.setSelected(true);
      txtfldFileLocation.setText(file.getUriString());
      txtfldFileLocation.selectAll();
      txtfldFileLocation.requestFocus();
      lblFileType.setText(file.getFileType().getName());
    }
    else {
      rdbtnFolder.setSelected(true);
      txtfldFolderLocation.setText(file.getUriString());
      txtfldFolderLocation.selectAll();
      txtfldFolderLocation.requestFocus();
    }

    txtarDescription.setText(file.getDescription());
  }


  @FXML
  public void handleButtonSelectFileAction(ActionEvent event) {
    FileChooser fileChooser = new FileChooser();

    if(txtfldFileLocation.getText() != null) {
      try {
        File currentFile = new File(txtfldFileLocation.getText());
        if(currentFile.exists())
          fileChooser.setInitialFileName(currentFile.getName());
        if(currentFile.getParentFile() != null && currentFile.getParentFile().exists())
          fileChooser.setInitialDirectory(currentFile.getParentFile());
      } catch (Exception ex) {
        log.debug("Could not extract file name and directory from " + txtfldFileLocation.getText(), ex);
      }
    }

    java.io.File selectedFile = fileChooser.showOpenDialog(windowStage);
    if(selectedFile != null) {
      txtfldFileLocation.setText(selectedFile.getAbsolutePath());
      txtfldFileLocation.positionCaret(txtfldFileLocation.getText().length());
      txtfldFileLocation.requestFocus();
    }
  }

  @FXML
  public void handleButtonSelectFolderAction(ActionEvent event) {
    DirectoryChooser directoryChooser = new DirectoryChooser();

    if(txtfldFolderLocation.getText() != null) {
      try {
        java.io.File currentFolder = new java.io.File(txtfldFolderLocation.getText());
        if(currentFolder.exists())
          directoryChooser.setInitialDirectory(currentFolder);
      } catch (Exception ex) {
        log.debug("Could not extract current directory from " + txtfldFolderLocation.getText(), ex);
      }
    }

    java.io.File selectedFolder = directoryChooser.showDialog(windowStage);
    if(selectedFolder != null) {
      txtfldFolderLocation.setText(selectedFolder.getAbsolutePath());
      txtfldFolderLocation.positionCaret(txtfldFolderLocation.getText().length());
      txtfldFolderLocation.requestFocus();
    }
  }

  @Override
  protected void closeDialog() {
    cleanUp();

    super.closeDialog();
  }

  protected void cleanUp() {
    //    if(person != null)
//      person.removePersonListener(personListener);
  }

  @Override
  protected void saveEntity() {
    saveEditedFields();

    if(file.isPersisted() == false)
      Application.getDeepThought().addFile(file);
  }

  protected void saveEditedFields() {
    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.FileIsFolder)) {
      file.setIsFolder(rdbtnFolder.isSelected());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.FileIsFolder);
    }

    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.FileFileLocation)) {
      if(rdbtnFileOrUrl.isSelected())
        file.setUriString(txtfldFileLocation.getText());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.FileFileLocation);
    }
    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.FileFolderLocation)) {
      if(rdbtnFolder.isSelected())
        file.setUriString(txtfldFolderLocation.getText());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.FileFolderLocation);
    }

    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.FileName)) {
      if(txtfldFileName.getText().equals(file.getName()) == false)
        file.setName(txtfldFileName.getText());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.FileName);
    }

    if(cmbxLocalFileLinkOptions.isDisabled() == false) {
      FileLinkOptions fileLinkOption = cmbxLocalFileLinkOptions.getSelectionModel().getSelectedItem();
      if(fileLinkOption == FileLinkOptions.CopyToDataFolder)
        copyFileToDataFolder();
      else if(fileLinkOption == FileLinkOptions.MoveToDataFolder)
        moveFileToDataFolder();
    }

    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.FileNotes)) {
      file.setDescription(txtarDescription.getText());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.FileNotes);
    }

    file.setFileType(detectedFileType);
  }

  protected void copyFileToDataFolder() {
    FileUtils.copyFileToDataFolder(file, new FileOperationListener() {
      @Override
      public ExistingFileHandling destinationFileAlreadyExists(File existingFile, File newFile, FileNameSuggestion suggestion) {
        // TODO: ask user what to do
        return ExistingFileHandling.RenameNewFile;
      }

      @Override
      public void errorOccurred(final DeepThoughtError error) {
        Platform.runLater(new Runnable() {
          @Override
          public void run() {
            Alerts.showErrorMessage(windowStage, Localization.getLocalizedString("error.could.not.copy.file.to.destination"), error.getNotificationMessage());
          }
        });
      }

      @Override
      public void fileOperationDone(boolean successful, File destinationFile) {
        if (successful)
          file = new FileLink(destinationFile.getPath());
      }
    });
  }

  protected void moveFileToDataFolder() {
    FileUtils.moveFileToDataFolder(file, new FileOperationListener() {
      @Override
      public ExistingFileHandling destinationFileAlreadyExists(File existingFile, File newFile, FileNameSuggestion suggestion) {
        // TODO: ask user what to do
        return ExistingFileHandling.RenameNewFile;
      }

      @Override
      public void errorOccurred(final DeepThoughtError error) {
        Platform.runLater(new Runnable() {
          @Override
          public void run() {
            Alerts.showErrorMessage(windowStage, Localization.getLocalizedString("error.could.not.copy.file.to.destination"), error.getNotificationMessage());
          }
        });
      }

      @Override
      public void fileOperationDone(boolean successful, File destinationFile) {
        if (successful)
          file = new FileLink(destinationFile.getPath());
      }
    });
  }

}

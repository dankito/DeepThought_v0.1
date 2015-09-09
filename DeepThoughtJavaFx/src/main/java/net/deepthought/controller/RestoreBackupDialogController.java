package net.deepthought.controller;

import net.deepthought.Application;
import net.deepthought.controller.enums.DialogResult;
import net.deepthought.controls.backup.BackupRestoreProgressMessageTableCell;
import net.deepthought.controls.backup.BackupRestoreProgressStateTableCell;
import net.deepthought.data.backup.BackupFile;
import net.deepthought.data.backup.IBackupFileService;
import net.deepthought.data.backup.RestoreBackupParams;
import net.deepthought.data.backup.RestoreBackupResult;
import net.deepthought.data.backup.RestoreBackupStepResult;
import net.deepthought.data.backup.enums.BackupRestoreType;
import net.deepthought.data.backup.enums.BackupStep;
import net.deepthought.data.backup.listener.RestoreBackupListener;
import net.deepthought.data.merger.enums.MergeEntities;
import net.deepthought.data.model.enums.BackupFileServiceType;
import net.deepthought.data.persistence.db.BaseEntity;
import net.deepthought.util.Alerts;
import net.deepthought.util.file.FileUtils;
import net.deepthought.util.Localization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;

/**
 * Created by ganymed on 10/01/15.
 */
public class RestoreBackupDialogController extends ChildWindowsController implements Initializable {

  private final static Logger log = LoggerFactory.getLogger(RestoreBackupDialogController.class);

  protected final static BackupFileServiceType FileServiceTypeNotFoundForThisFile = new BackupFileServiceType("File Service Type not found");


  protected BackupFile selectedAutomaticallyCreatedBackup;

  protected String selectedCustomBackupFile;

  protected BackupRestoreType selectedRestoreType;


  @FXML
  protected RadioButton rdbtnAutomaticallyCreatedBackups;
  @FXML
  protected ComboBox<BackupFileServiceType> cmbxShowAutomaticallyCreatedBackupsOfType;
  @FXML
  protected Button btnShowBackupsFolder;
  @FXML
  protected Pane paneAutomaticallyCreatedBackupsContent;
  @FXML
  protected TableView<BackupFile> tblvwAutomaticallyCreatedBackups;
  @FXML
  protected TableColumn<BackupFile, String> clmBackupName;
  @FXML
  protected TableColumn<BackupFile, String> clmBackupType;
  @FXML
  protected TableColumn<BackupFile, String> clmBackupDate;

  @FXML
  protected RadioButton rdbtnChooseBackupFile;
  @FXML
  protected Pane paneChooseBackupFileContent;
  @FXML
  protected TextField txtfldChooseBackupFilePath;

  @FXML
  protected ComboBox<BackupRestoreType> cmbxRestoreType;

  @FXML
  protected Pane paneSelectEntitiesToRestore;
  @FXML
  protected RadioButton rdbtnTryToRestoreAllData;
  @FXML
  protected RadioButton rdbtnSelectDataToRestore;

  @FXML
  protected Pane paneBackupRestoreProgress;
  @FXML
  protected TableView<RestoreProgressItem> tblvwRestoreProgress;
  @FXML
  protected TableColumn<RestoreProgressItem, String> clmRestoreProgressStateIcon;
  @FXML
  protected TableColumn<RestoreProgressItem, String> clmRestoreProgressMessage;

  @FXML
  protected Button btnRun;


  @Override
  public void initialize(URL location, ResourceBundle resources) {
    setupControls();
  }


  protected void setupControls() {
    rdbtnAutomaticallyCreatedBackups.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        paneAutomaticallyCreatedBackupsContent.setDisable(newValue == false);
        setRunButtonState();
      }
    });

    for(IBackupFileService backupFileService : Application.getBackupManager().getRegisteredBackupFileServices())
      cmbxShowAutomaticallyCreatedBackupsOfType.getItems().add(backupFileService.getFileServiceType());
    FXCollections.sort(cmbxShowAutomaticallyCreatedBackupsOfType.getItems());

    cmbxShowAutomaticallyCreatedBackupsOfType.setConverter(new StringConverter<BackupFileServiceType>() {
      @Override
      public String toString(BackupFileServiceType fileServiceType) {
        return fileServiceType.getTextRepresentation();
      }

      @Override
      public BackupFileServiceType fromString(String string) {
        return null;
      }
    });

    cmbxShowAutomaticallyCreatedBackupsOfType.valueProperty().addListener(new ChangeListener<BackupFileServiceType>() {
      @Override
      public void changed(ObservableValue<? extends BackupFileServiceType> observable, BackupFileServiceType oldValue, BackupFileServiceType newValue) {
        ObservableList automaticallyCreatedBackupsItems = tblvwAutomaticallyCreatedBackups.getItems();
        automaticallyCreatedBackupsItems.clear();

        automaticallyCreatedBackupsItems.addAll(Application.getBackupManager().getAvailableBackupsForFileType(newValue));
      }
    });

    cmbxShowAutomaticallyCreatedBackupsOfType.getSelectionModel().select(0);

    btnShowBackupsFolder.setTooltip(new Tooltip(Application.getBackupManager().getBackupsFolderPath()));

    clmBackupName.setCellValueFactory(new PropertyValueFactory<BackupFile, String>("fileName"));
    clmBackupType.setCellValueFactory(new PropertyValueFactory<BackupFile, String>("fileTypeKeyLocalized"));
    clmBackupDate.setCellValueFactory(new PropertyValueFactory<BackupFile, String>("backupTimeLocalized"));

    tblvwAutomaticallyCreatedBackups.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<BackupFile>() {
      @Override
      public void changed(ObservableValue<? extends BackupFile> observable, BackupFile oldValue, BackupFile newValue) {
        selectedAutomaticallyCreatedBackup = newValue;
        setRunButtonState();
      }
    });


    rdbtnChooseBackupFile.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        paneChooseBackupFileContent.setDisable(newValue == false);
        setRunButtonState();
      }
    });

    txtfldChooseBackupFilePath.textProperty().addListener(new ChangeListener<String>() {
      @Override
      public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        selectedCustomBackupFile = newValue;
        setRunButtonState();
      }
    });


    cmbxRestoreType.getItems().addAll(BackupRestoreType.values());

    cmbxRestoreType.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<BackupRestoreType>() {
      @Override
      public void changed(ObservableValue<? extends BackupRestoreType> observable, BackupRestoreType oldValue, BackupRestoreType newValue) {
        selectedRestoreType = newValue;
        setRunButtonState();
        setSelectEntitiesToRestoreState(selectedRestoreType);
      }
    });

    cmbxRestoreType.getSelectionModel().select(0);


    paneBackupRestoreProgress.managedProperty().bind(paneBackupRestoreProgress.visibleProperty());

    clmRestoreProgressStateIcon.setCellFactory(new Callback<TableColumn<RestoreProgressItem, String>, TableCell<RestoreProgressItem, String>>() {
      @Override
      public TableCell<RestoreProgressItem, String> call(TableColumn<RestoreProgressItem, String> param) {
        return new BackupRestoreProgressStateTableCell();
      }
    });

    clmRestoreProgressMessage.setCellFactory(new Callback<TableColumn<RestoreProgressItem, String>, TableCell<RestoreProgressItem, String>>() {
      @Override
      public TableCell<RestoreProgressItem, String> call(TableColumn<RestoreProgressItem, String> param) {
        return new BackupRestoreProgressMessageTableCell();
      }
    });
  }

  protected void setRunButtonState() {
//    if(selectedRestoreType == BackupRestoreType.AddAsNewToExistingData) { // TODO: remove again as soon as Adding is implemented
//      btnRun.setDisable(true);
//      return;
//    }

    if(rdbtnAutomaticallyCreatedBackups.isSelected() && selectedAutomaticallyCreatedBackup == null)
      btnRun.setDisable(true);
    else if(rdbtnChooseBackupFile.isSelected() && new File(txtfldChooseBackupFilePath.getText()).exists() == false)
      btnRun.setDisable(true);
    else
      btnRun.setDisable(false);
  }

  protected void setSelectEntitiesToRestoreState(BackupRestoreType selectedRestoreType) {
    if(selectedRestoreType == BackupRestoreType.ReplaceExistingDataCollection || selectedRestoreType == BackupRestoreType.TryToMergeWithExistingDataAndReplaceExistingDataCollectionOnFailure)
      paneSelectEntitiesToRestore.setDisable(true);
    else
      paneSelectEntitiesToRestore.setDisable(false);
  }


  @FXML
  public void handleButtonCancelAction(ActionEvent event) {
    closeDialog(DialogResult.Cancel);
  }

  @FXML
  public void handleButtonRunAction(ActionEvent event) {
    paneBackupRestoreProgress.setVisible(true);

    MergeEntities mergeEntities = rdbtnTryToRestoreAllData.isSelected() ? MergeEntities.TryToMergeAllEntities : MergeEntities.SelectEntitiesToMerge;
    RestoreBackupListener listener = createNewRestoreBackupListener();

    RestoreBackupParams restoreBackupParams;
    if(rdbtnAutomaticallyCreatedBackups.isSelected())
      restoreBackupParams = new RestoreBackupParams(selectedAutomaticallyCreatedBackup, selectedRestoreType, mergeEntities, listener);
    else {
      BackupFileServiceType fileServiceType = getFileServiceTypeForBackupFile(txtfldChooseBackupFilePath.getText());
      if(fileServiceType == FileServiceTypeNotFoundForThisFile) {
        Alerts.showErrorMessage(windowStage, Localization.getLocalizedString("error.can.not.restore.backup.file"),
            Localization.getLocalizedString("error.can.not.restore.backup.file.of.this.type", txtfldChooseBackupFilePath.getText()));
        return;
      }
      else
        restoreBackupParams = new RestoreBackupParams(new BackupFile(txtfldChooseBackupFilePath.getText(), fileServiceType), selectedRestoreType, mergeEntities, listener);
    }

    Application.getBackupManager().restoreBackupAsync(restoreBackupParams);
  }

  protected BackupFileServiceType getFileServiceTypeForBackupFile(String backupFilePath) {
    String backupFileExtension = FileUtils.getFileExtension(backupFilePath);

    for(IBackupFileService backupFileService : Application.getBackupManager().getRegisteredBackupFileServices()) {
      if(backupFileService.getFileTypeFileExtension().equals(backupFileExtension))
        return backupFileService.getFileServiceType();
    }

    return FileServiceTypeNotFoundForThisFile;
  }

  protected RestoreBackupListener createNewRestoreBackupListener() {
    return new RestoreBackupListener() {
      @Override
      public void beginStep(BackupFile file, BackupStep step) {
        tblvwRestoreProgress.getItems().add(new RestoreProgressItem(step));
      }

      @Override
      public void stepDone(RestoreBackupStepResult stepResult) {
        RestoreProgressItem currentItem = tblvwRestoreProgress.getItems().get(tblvwRestoreProgress.getItems().size() - 1);
        currentItem.setResult(stepResult);
        currentItem.setInProgress(false);
      }

      @Override
      public List<BaseEntity> selectEntitiesToRestore(BackupFile file, final BaseEntity restoredData) {
        final List<BaseEntity> entitiesToRestore = new ArrayList<>();
        final CountDownLatch selectEntitiesLatch = new CountDownLatch(1);

        Platform.runLater(new Runnable() {
          @Override
          public void run() {
            Dialogs.showSelectEntitiesToImportDialog(restoredData, windowStage, new ChildWindowsControllerListener() {
              @Override
              public void windowClosing(Stage stage, ChildWindowsController controller) {
                if(controller.getDialogResult() == DialogResult.Ok)
                  entitiesToRestore.addAll(((SelectEntitiesToImportDialogController) controller).getSelectedEntitiesToRestore());

                selectEntitiesLatch.countDown();
              }

              @Override
              public void windowClosed(Stage stage, ChildWindowsController controller) {

              }
            });

          }
        });

        try { selectEntitiesLatch.await(); } catch(Exception ex) { }
        return entitiesToRestore;
      }

      @Override
      public void restoreBackupDone(final boolean successful, final RestoreBackupResult result) {
        Platform.runLater(new Runnable() {
          @Override
          public void run() {
            if(successful)
              Alerts.showInfoMessage(windowStage, Localization.getLocalizedString("restoring.backup.was.successful"),
                  Localization.getLocalizedString("successfully.restored.backup.file", result.getBackup().getFilePath()));
            else
              Alerts.showErrorMessage(windowStage, Localization.getLocalizedString("error.could.not.restore.backup"),
                  Localization.getLocalizedString("error.could.not.restore.backup.file", result.getBackup().getFilePath(), result.getError()));

            closeDialog(DialogResult.Ok);
          }
        });
      }
    };
  }


  @FXML
  public void handleButtonShowBackupsFolderAction(ActionEvent event) {

  }

  @FXML
  public void handleButtonChooseBackupFileAction(ActionEvent event) {
    FileChooser fileChooser = new FileChooser();

    if(txtfldChooseBackupFilePath.getText() != null) {
      try {
        java.io.File currentFile = new java.io.File(txtfldChooseBackupFilePath.getText());
        if(currentFile.exists())
          fileChooser.setInitialFileName(currentFile.getName());
        if(currentFile.getParentFile().exists())
          fileChooser.setInitialDirectory(currentFile.getParentFile());
      } catch (Exception ex) {
        log.debug("Could not extract file name and directory from " + txtfldChooseBackupFilePath.getText(), ex);
      }
    }

    java.io.File selectedFile = fileChooser.showOpenDialog(windowStage);
    if(selectedFile != null) {
      txtfldChooseBackupFilePath.setText(selectedFile.getAbsolutePath());
      txtfldChooseBackupFilePath.positionCaret(txtfldChooseBackupFilePath.getText().length());
      txtfldChooseBackupFilePath.requestFocus();
    }
  }


  @Override
  public void setWindowStage(Stage windowStage) {
    super.setWindowStage(windowStage);

    windowStage.setTitle(Localization.getLocalizedString("restore.backup.dialog.title"));
  }

  public static class RestoreProgressItem {

    protected boolean isInProgress = true;

    protected BackupStep step;

    protected RestoreBackupStepResult result;


    public RestoreProgressItem(BackupStep step) {
      this.step = step;
    }


    public boolean isInProgress() {
      return isInProgress;
    }

    public void setInProgress(boolean isInProgress) {
      this.isInProgress = isInProgress;
    }

    public BackupStep getStep() {
      return step;
    }

    public RestoreBackupStepResult getResult() {
      return result;
    }

    public void setResult(RestoreBackupStepResult result) {
      this.result = result;
      for(RestoreProgressItemListener listener : listeners)
        listener.resultRetrieved(result);
    }



    public interface RestoreProgressItemListener {
      public void resultRetrieved(RestoreBackupStepResult result);
    }

    protected Set<RestoreProgressItemListener> listeners = new HashSet<>();

    public boolean addRestoreProgressItemListener(RestoreProgressItemListener listener) {
      return listeners.add(listener);
    }

    public boolean removeRestoreProgressItemListener(RestoreProgressItemListener listener) {
      return listeners.remove(listener);
    }

  }

}

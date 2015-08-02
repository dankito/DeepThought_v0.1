package net.deepthought.data.backup;

import net.deepthought.Application;
import net.deepthought.data.backup.enums.BackupRestoreType;
import net.deepthought.data.backup.enums.BackupStep;
import net.deepthought.data.backup.enums.CreateBackupResult;
import net.deepthought.data.backup.listener.CreateBackupListener;
import net.deepthought.data.backup.listener.RestoreBackupListener;
import net.deepthought.data.model.DeepThoughtApplication;
import net.deepthought.data.persistence.EntityManagerConfiguration;
import net.deepthought.data.persistence.IEntityManager;
import net.deepthought.util.DeepThoughtError;
import net.deepthought.util.file.FileNameSuggestion;
import net.deepthought.util.file.FileUtils;
import net.deepthought.util.Localization;
import net.deepthought.util.file.enums.ExistingFileHandling;
import net.deepthought.util.file.listener.FileOperationListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by ganymed on 05/01/15.
 */
public class DatabaseBackupFileService extends AbstractBackupFileService {

//  public final static String BackupFileTypeKey = "Database";

  public final static String DatabaseBackupFileNameStart = "DeepThought.DatabaseBackup";

  public final static String DatabaseBackupFileExtension = "db";


  private final static Logger log = LoggerFactory.getLogger(DatabaseBackupFileService.class);


  public DatabaseBackupFileService() {
    super("backup.file.service.type.database");
  }


//  @Override
//  public String getFileTypeKey() {
//    return BackupFileTypeKey;
//  }

  @Override
  public String getFileTypeFileExtension() {
    return DatabaseBackupFileExtension;
  }

  @Override
  public void createBackup(CreateBackupParams params) {
    File databaseFile = new File(Application.getDataCollectionSavePath());
    final CreateBackupListener listener = params.getListener();
    log.debug("Trying to create Database Backup for file {}", databaseFile);
    final Date startTime = new Date();

    final BackupFile backupFile = createBackupFile(params);
    if(backupFile == null)
      return;

    log.debug("Trying to copyFile Database file to {}", backupFile.getFilePath());

    try {
      FileUtils.copyFile(databaseFile, backupFile.getFile(), new FileOperationListener() {
        @Override
        public ExistingFileHandling destinationFileAlreadyExists(File existingFile, File newFile, FileNameSuggestion suggestion) {
          return ExistingFileHandling.RenameExistingFile;
        }

        @Override
        public void errorOccurred(DeepThoughtError error) {
          log.error("An error occurred while backing up Database file", error.getException());
          if (listener != null)
            listener.createBackupDone(CreateBackupResult.CouldNotCopyDatabaseFile, error, backupFile);
        }

        @Override
        public void fileOperationDone(boolean successful, File destinationFile) {
          log.debug("Was copying database file successful? {}", successful);
          logElapsedTime(startTime);
          if (successful && listener != null) // in error case listener has already been notified in notification() FileOperationListener method
            listener.createBackupDone(CreateBackupResult.Successful, null, backupFile);
        }
      });

      checkIfMaximumCountBackupsExceeded();
    } catch(Exception ex) {
      if (listener != null)
        listener.createBackupDone(CreateBackupResult.CouldNotWriteSerializedDeepThoughtToFile, DeepThoughtError.errorFromLocalizationKey(ex, "error.could.not.write.serialized.deep.thought.to.file",
            backupFile.getFilePath(), ex.getLocalizedMessage()), backupFile);
    }
  }

  protected String createBackupFileName(CreateBackupParams params) {
    return DatabaseBackupFileNameStart + "_" + BackupDateFormat.format(new Date()) + "." + DatabaseBackupFileExtension;
  }


  @Override
  public void restoreBackup(RestoreBackupParams params) {
    Application.getBackupManager().copyDataCollectionAndBackupFileToRestoredBackupsFolder(params);

    if(params.getRestoreType() == BackupRestoreType.ReplaceExistingDataCollection) { // this case is very simple, simply copyFile Database file back
      restoreDeepThoughtReplaceExisting(null, params);
    }
    else {
      BackupFile backup = params.getBackupFile();
      RestoreBackupListener listener = params.getListener();
      String errorMessage = "";
      log.debug("Going to do a " + params.getRestoreType() + " restore from Database Backup file " + backup.getFilePath());
      if(listener != null)
        listener.beginStep(backup, BackupStep.DeserializeBackedUpDeepThought);

      // TODO: how to tell IEntityManager Database backup path?
      EntityManagerConfiguration configuration = Application.getEntityManagerConfiguration().copy();
      configuration.setDataCollectionPersistencePath(params.getRestoredBackupPath().getAbsolutePath());
      IEntityManager restoreBackupEntityManager = null;

      try {
        restoreBackupEntityManager = Application.getDependencyResolver().createEntityManager(configuration);
        if (restoreBackupEntityManager != null) {
          List<DeepThoughtApplication> applicationsQueryResult = restoreBackupEntityManager.getAllEntitiesOfType(DeepThoughtApplication.class);

          if (applicationsQueryResult.size() > 0) {
            if (listener != null)
              listener.stepDone(new RestoreBackupStepResult(backup, BackupStep.DeserializeBackedUpDeepThought, true, BackupStep.InsertEntitiesIntoDatabase));

            DeepThoughtApplication application = applicationsQueryResult.get(0);
//            Application.getDataManager().ensureAllLazyLoadingDataIsLoaded(application);
            restoreBackupEntityManager.resolveAllLazyRelations(application);

            restoreDeepThought(application, params);
            return;
          } else
            errorMessage = Localization.getLocalizedString("error.no.data.found.in.backup.file");
        }
      } catch (Exception ex) {
        log.error("Could not deserialize AppSettings from Database Backup file " + backup.getFilePath(), ex);
        errorMessage = ex.getLocalizedMessage();
      }
      finally {
        if(restoreBackupEntityManager != null)
          restoreBackupEntityManager.close();
      }

      if (listener != null) {
        DeepThoughtError error = DeepThoughtError.errorFromLocalizationKey("error.could.not.deserialize.backup.file.to.deep.thought", backup.getFilePath(), errorMessage);
        listener.stepDone(new RestoreBackupStepResult(backup, BackupStep.DeserializeBackedUpDeepThought, error, BackupStep.Abort));
        listener.restoreBackupDone(false, new RestoreBackupResult(backup, error));
        return;
      }
    }
  }

  @Override
  protected void restoreDeepThoughtReplaceExisting(DeepThoughtApplication application, RestoreBackupParams params) {
    BackupFile backup = params.getBackupFile();
    RestoreBackupListener listener = params.getListener();
    log.debug("Going to restore Backup file {} by move existing Data Collection to restored_backups folder and then replacing it with Backup file", backup.getFilePath());
    if(listener != null)
      listener.beginStep(backup, BackupStep.DeleteExistingDataCollection);

    // TODO: inform User that we're going to delete existing Data Collection
//    if(listener != null)
//      listener.stepDone(new RestoreBackupStepResult(backup, BackupStep));
    Application.getDataManager().deleteExistingDataCollection();

    // TODO: how to find out if replacing was successful?
    // TODO: also a nice feature would be telling user where Data Collection has been moved to
    if(listener != null)
      listener.stepDone(new RestoreBackupStepResult(backup, BackupStep.DeleteExistingDataCollection, true, BackupStep.CopyBackupFileToDataFolder));

    final List<DeepThoughtError> occurredErrorsContainer = new ArrayList<>();
    final List<Boolean> successContainer = new ArrayList<>();
    final List<File> newFileNameContainer = new ArrayList<>();

    FileUtils.copyFile(backup.getFile(), new File(Application.getDataCollectionSavePath()), new FileOperationListener() {
      @Override
      public ExistingFileHandling destinationFileAlreadyExists(File existingFile, File newFile, FileNameSuggestion suggestion) {
        return ExistingFileHandling.RenameExistingFile;
      }

      @Override
      public void errorOccurred(DeepThoughtError error) {
        occurredErrorsContainer.add(error);
      }

      @Override
      public void fileOperationDone(boolean successful, File destinationFile) {
        successContainer.add(successful);
        newFileNameContainer.add(destinationFile);
      }
    });

    Application.getDataManager().recreateEntityManagerAndRetrieveDeepThoughtApplication();

    boolean successful = successContainer.size() == 1 && successContainer.get(0) == true;
    log.error("Copying Backup " + backup.getFilePath() + " back to Data folder was " + (successful ? "" : "not ") + "successful");
    if(listener != null) {
      DeepThoughtError error = occurredErrorsContainer.size() == 0 ? DeepThoughtError.Success : occurredErrorsContainer.get(0);
      String stepResult = null;
      if(newFileNameContainer.size() > 0)
        stepResult = Localization.getLocalizedString("copied.backup.file.back.to", newFileNameContainer.get(0).getAbsolutePath());

      listener.stepDone(new RestoreBackupStepResult(backup, BackupStep.CopyBackupFileToDataFolder, error, BackupStep.Done, stepResult));
      listener.restoreBackupDone(successful, new RestoreBackupResult(backup, error));
    }
  }
}

package net.deepthought.data.backup;

import net.deepthought.Application;
import net.deepthought.data.backup.enums.BackupStep;
import net.deepthought.data.backup.enums.CreateBackupResult;
import net.deepthought.data.backup.listener.CreateBackupListener;
import net.deepthought.data.backup.listener.RestoreBackupListener;
import net.deepthought.data.model.DeepThoughtApplication;
import net.deepthought.data.persistence.db.BaseEntity;
import net.deepthought.data.persistence.deserializer.DeserializationResult;
import net.deepthought.data.persistence.serializer.SerializationResult;
import net.deepthought.util.DeepThoughtError;
import net.deepthought.util.FileUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * Created by ganymed on 05/01/15.
 */
public abstract class SerializeToFileBackupFileService extends AbstractBackupFileService {


  private final static Logger log = LoggerFactory.getLogger(SerializeToFileBackupFileService.class);


  public SerializeToFileBackupFileService(String fileServiceTypeKey) {
    super(fileServiceTypeKey);
  }


  @Override
  public void createBackup(CreateBackupParams params) {
    log.debug("Trying to create DeepThought Json Backup");
    CreateBackupListener listener = params.getListener();
    Date startTime = new Date();

    BackupFile backupFile = createBackupFile(params);
//    if(backupFile == null)
//      return;

    // don't know why but seems i have to call this, otherwise if not called already Json-io will run into a StackOverflow
    Application.getDataManager().ensureAllLazyLoadingDataIsLoaded(params.getApplication());

    SerializationResult result = serializeEntity(params.getApplication());
    if(result.successful() == false) {
      log.error("Could not serialize DeepThought", result.getError());
      if(listener != null)
        listener.createBackupDone(CreateBackupResult.CouldNotSerializeDeepThought, DeepThoughtError.errorFromLocalizationKey(result.getError(),
            "error.could.not.serialize.deep.thought", result.getError().getLocalizedMessage()), backupFile);
    }
    else {
      if(writeBackupToFile(backupFile, result.getSerializationResult(), listener)) {
        logElapsedTime(startTime);
        checkIfMaximumCountBackupsExceeded();
      }
    }
  }

  protected abstract SerializationResult serializeEntity(BaseEntity entity);

  @Override
  public void restoreBackup(RestoreBackupParams params) {
    Application.getBackupManager().copyDataCollectionAndBackupFileToRestoredBackupsFolder(params);

    BackupFile backup = params.getBackupFile();
    RestoreBackupListener listener = params.getListener();
    log.debug("Going to do a " + params.getRestoreType() + " restore from Json Backup file " + backup.getFilePath());
    if(listener != null)
      listener.beginStep(backup, BackupStep.ReadingBackupFileFromFileSystem);

    String backupFileContent = null;
    try {
      backupFileContent = FileUtils.readTextFile(params.getRestoredBackupPath());
    } catch(Exception ex) {
      log.error("Could not read Backup file from File system", ex);
      if(listener != null) {
        DeepThoughtError error = DeepThoughtError.errorFromLocalizationKey(ex, "error.could.not.read.backup.file", params.getRestoredBackupPath().getAbsolutePath(), ex.getLocalizedMessage());
        listener.stepDone(new RestoreBackupStepResult(backup, BackupStep.ReadingBackupFileFromFileSystem, error, BackupStep.Abort));
        listener.restoreBackupDone(false, new RestoreBackupResult(backup, error));
        return;
      }
    }

    if(listener != null) {
      listener.stepDone(new RestoreBackupStepResult(backup, BackupStep.ReadingBackupFileFromFileSystem, true, BackupStep.DeserializeBackedUpDeepThought));
      listener.beginStep(backup, BackupStep.DeserializeBackedUpDeepThought);
    }

    DeserializationResult<DeepThoughtApplication> result = deserializeEntity(backupFileContent, DeepThoughtApplication.class);
    if(result.successful() == false) {
      log.error("Could not deserialize Backup File to DeepThoughtApplication. File Content was : " + backupFileContent, result.getError());
      if(listener != null) {
        DeepThoughtError error = DeepThoughtError.errorFromLocalizationKey(result.getError(), "error.could.not.deserialize.backup.file.to.deep.thought", backup.getFilePath(), result.getError().getLocalizedMessage());
        listener.stepDone(new RestoreBackupStepResult(backup, BackupStep.DeserializeBackedUpDeepThought, error, BackupStep.Abort));
        listener.restoreBackupDone(false, new RestoreBackupResult(backup, error));
      }
      return;
    }

    if(listener != null)
      listener.stepDone(new RestoreBackupStepResult(backup, BackupStep.DeserializeBackedUpDeepThought, true, BackupStep.InsertEntitiesIntoDatabase));

    restoreDeepThought(result.getResult(), params);
  }

  protected abstract <T extends BaseEntity> DeserializationResult<T> deserializeEntity(String backupFileContent, Class<T> entityClass);
}

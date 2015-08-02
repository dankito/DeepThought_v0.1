package net.deepthought.data.backup;

import net.deepthought.Application;
import net.deepthought.data.backup.enums.BackupStep;
import net.deepthought.data.backup.enums.CreateBackupResult;
import net.deepthought.data.backup.listener.CreateBackupListener;
import net.deepthought.data.backup.listener.RestoreBackupListener;
import net.deepthought.data.merger.MergeDataListener;
import net.deepthought.data.merger.enums.MergeEntities;
import net.deepthought.data.model.DeepThoughtApplication;
import net.deepthought.data.model.enums.BackupFileServiceType;
import net.deepthought.data.persistence.db.BaseEntity;
import net.deepthought.util.DeepThoughtError;
import net.deepthought.util.file.FileUtils;
import net.deepthought.util.Localization;
import net.deepthought.util.file.enums.ExistingFileHandling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Created by ganymed on 01/01/15.
 */
public abstract class AbstractBackupFileService implements IBackupFileService {

  public final static DateFormat BackupDateFormat = new SimpleDateFormat("yyyy.MM.dd_HH-mm-ss");
  public final static Date CouldNotExtractBackupTimeFromFileName = new Date(-1);


  private final static Logger log = LoggerFactory.getLogger(AbstractBackupFileService.class);


  protected BackupFileServiceType fileServiceType = null;


  public AbstractBackupFileService(String fileServiceTypeKey) {
    this.fileServiceType = findFileServiceTypeForKey(fileServiceTypeKey);
  }


  protected BackupFileServiceType findFileServiceTypeForKey(String fileServiceTypeKey) {
    String translatedKey = Localization.getLocalizedString(fileServiceTypeKey);

    for(BackupFileServiceType fileServiceType : Application.getDeepThought().getBackupFileServiceTypes()) {
      if(fileServiceType.getName().equals(translatedKey))
        return fileServiceType;
    }

    return null;
  }


  protected void restoreDeepThought(final DeepThoughtApplication application, RestoreBackupParams params) {
    switch(params.getRestoreType()) {
      case TryToMergeWithExistingData:
        restoreDeepThoughtTryToMergeWithExistingData(application, params);
        break;
      case AddAsNewToExistingData:
        restoreDeepThoughtAddAsNewToExistingData(application, params);
        break;
      case TryToMergeWithExistingDataAndReplaceExistingDataCollectionOnFailure:
        restoreDeepThoughtTryToInsertIntoExistingButReplaceOnFailure(application, params);
        break;
      case ReplaceExistingDataCollection:
        restoreDeepThoughtReplaceExisting(application, params);
        break;
    }
  }

  protected void restoreDeepThoughtTryToInsertIntoExistingButReplaceOnFailure(DeepThoughtApplication application, RestoreBackupParams params) {
    log.debug("Called restoreDeepThoughtTryToInsertIntoExistingButReplaceOnFailure() for BackupFile {}", params.getBackupFile().getFilePath());

    if(restoreDeepThoughtTryToMergeWithExistingData(application, params) == false)
      restoreDeepThoughtReplaceExisting(application, params);
  }

  protected boolean restoreDeepThoughtTryToMergeWithExistingData(final DeepThoughtApplication application, RestoreBackupParams params) {
    final BackupFile backup = params.getBackupFile();
    final RestoreBackupListener listener = params.getListener();
    log.debug("Called restoreDeepThoughtTryToMergeWithExistingData() for BackupFile {} with AppSettings {}", backup.getFilePath(), application);

    List<BaseEntity> entitiesToMerge;
    boolean mergeTheirSubEntitiesAsWell = true;
    if(params.getMergeEntities() == MergeEntities.TryToMergeAllEntities)
      entitiesToMerge = new ArrayList<BaseEntity>() {{ add(application); }};
    else {
      listener.beginStep(backup, BackupStep.SelectEntitiesToRestore);
      entitiesToMerge = listener.selectEntitiesToRestore(backup, application.getLastLoggedOnUser().getLastViewedDeepThought()); // TODO: is this senseful?
      mergeTheirSubEntitiesAsWell = false;
      if(entitiesToMerge == null || entitiesToMerge.size() == 0) {
        listener.stepDone(new RestoreBackupStepResult(backup, BackupStep.SelectEntitiesToRestore, DeepThoughtError.errorFromLocalizationKey("error.no.entities.to.restore.selected"), BackupStep.Abort));
        return false;
      }
      listener.stepDone(new RestoreBackupStepResult(backup, BackupStep.SelectEntitiesToRestore, true, BackupStep.InsertEntityIntoDatabase));
    }

    boolean result = Application.getDataMerger().mergeWithCurrentData(entitiesToMerge, mergeTheirSubEntitiesAsWell, new MergeDataListener() {
          @Override
          public void beginToMergeEntity(BaseEntity entity) {
            if(listener != null)
              listener.beginStep(backup, BackupStep.InsertEntityIntoDatabase);
          }

          @Override
          public void mergeEntityResult(BaseEntity entity, boolean successful, DeepThoughtError error) {
            if (listener != null) {
              if (successful)
                listener.stepDone(new RestoreBackupStepResult(backup, BackupStep.InsertEntityIntoDatabase, true, BackupStep.InsertEntityIntoDatabase));
              else
                listener.stepDone(new RestoreBackupStepResult(backup, BackupStep.InsertEntityIntoDatabase, DeepThoughtError.errorFromLocalizationKey("error.could.not.insert.backed.up.entity",
                    entity.toString()), BackupStep.InsertEntityIntoDatabase));
            }
          }

          @Override
          public void addingEntitiesDone(boolean successful, List<BaseEntity> entitiesSucceededToInsert, List<BaseEntity> entitiesFailedToInsert) {
            if (listener != null) {
              RestoreBackupResult result = new RestoreBackupResult(backup, entitiesSucceededToInsert, entitiesFailedToInsert);
              listener.stepDone(new RestoreBackupStepResult(backup, BackupStep.InsertEntitiesIntoDatabase, successful, result.getError(), successful ? BackupStep.Done : BackupStep.Abort));
              listener.restoreBackupDone(successful, result);
            }
          }
        });

    return result;
  }

  protected boolean restoreDeepThoughtAddAsNewToExistingData(final DeepThoughtApplication application, RestoreBackupParams params) {
    final BackupFile backup = params.getBackupFile();
    final RestoreBackupListener listener = params.getListener();
    log.debug("Called restoreDeepThoughtAddAsNewToExistingData() for BackupFile {} with AppSettings {}", backup.getFilePath(), application);

    List<BaseEntity> entitiesToMerge;
    if(params.getMergeEntities() == MergeEntities.TryToMergeAllEntities)
      entitiesToMerge = new ArrayList<BaseEntity>() {{ add(application); }};
    else {
      listener.beginStep(backup, BackupStep.SelectEntitiesToRestore);
      entitiesToMerge = listener.selectEntitiesToRestore(backup, application.getLastLoggedOnUser().getLastViewedDeepThought()); // TODO: is this senseful?
      if(entitiesToMerge == null || entitiesToMerge.size() == 0) {
        listener.stepDone(new RestoreBackupStepResult(backup, BackupStep.SelectEntitiesToRestore, DeepThoughtError.errorFromLocalizationKey("error.no.entities.to.restore.selected"), BackupStep.Abort));
        return false;
      }
      listener.stepDone(new RestoreBackupStepResult(backup, BackupStep.SelectEntitiesToRestore, true, BackupStep.InsertEntityIntoDatabase));
    }

    boolean result = Application.getDataMerger().addToCurrentData(entitiesToMerge, new MergeDataListener() {
      @Override
      public void beginToMergeEntity(BaseEntity entity) {
        if(listener != null)
          listener.beginStep(backup, BackupStep.InsertEntityIntoDatabase);
      }

      @Override
          public void mergeEntityResult(BaseEntity entity, boolean successful, DeepThoughtError error) {
            if (listener != null) {
              if (successful)
                listener.stepDone(new RestoreBackupStepResult(backup, BackupStep.InsertEntityIntoDatabase, true, BackupStep.InsertEntityIntoDatabase));
              else
                listener.stepDone(new RestoreBackupStepResult(backup, BackupStep.InsertEntityIntoDatabase, DeepThoughtError.errorFromLocalizationKey("error.could.not.insert.backed.up.entity",
                    entity.toString()), BackupStep.InsertEntityIntoDatabase));
            }
          }

          @Override
          public void addingEntitiesDone(boolean successful, List<BaseEntity> entitiesSucceededToInsert, List<BaseEntity> entitiesFailedToInsert) {
            if (listener != null) {
              RestoreBackupResult result = new RestoreBackupResult(backup, entitiesSucceededToInsert, entitiesFailedToInsert);
              listener.stepDone(new RestoreBackupStepResult(backup, BackupStep.InsertEntitiesIntoDatabase, successful, result.getError(), successful ? BackupStep.Done : BackupStep.Abort));
              listener.restoreBackupDone(successful, result);
            }
          }
        });

    return result;
  }

  protected void restoreDeepThoughtReplaceExisting(DeepThoughtApplication application, RestoreBackupParams params) {
    BackupFile backup = params.getBackupFile();
    RestoreBackupListener listener = params.getListener();
    log.debug("Called restoreDeepThoughtReplaceExisting() for BackupFile {} with DeepThoughtApplication {}", backup.getFilePath(), application);

    Application.getDataManager().replaceExistingDataCollectionWithData(application); // TODO: how to get notified?
  }


  protected boolean writeBackupToFile(BackupFile backupFile, String fileContent, CreateBackupListener listener) {
    log.debug("Trying to save backed up Application to file {}", backupFile.getFilePath());

    try {
      FileUtils.writeToFile(fileContent, backupFile.getFile());

      log.debug("Successfully wrote Backup to {}", backupFile.getFilePath());
      if(listener != null)
        listener.createBackupDone(CreateBackupResult.Successful, null, backupFile);

      return true;
    } catch(Exception ex) {
      log.error("Could not write DeepThought Back Up to file " + backupFile.getFilePath(), ex);
      if(listener != null)
        listener.createBackupDone(CreateBackupResult.CouldNotWriteSerializedDeepThoughtToFile, DeepThoughtError.errorFromLocalizationKey(ex, "error.could.not.write.serialized.deep.thought.to.file",
            backupFile.getFilePath(), ex.getLocalizedMessage()), backupFile);
    }

    return false;
  }


  protected BackupFile createBackupFile(CreateBackupParams params) {
    CreateBackupListener listener = params.getListener();
    BackupFile backupFile = null;

    File backupsFolder = new File(params.getBackupBaseFolder());
    if(backupsFolder == null) {
      if(listener != null) {
        listener.createBackupDone(CreateBackupResult.CouldNotCreateBackupFolder, DeepThoughtError.errorFromLocalizationKey("error.backup.folder.could.not.be.created"), null);
      }
      return backupFile;
    }

    if(backupsFolder.exists() == false)
      backupsFolder.mkdirs();

    try {
      File backupIoFile = new File(backupsFolder, createBackupFileName(params));
      if(FileUtils.isFileUnique(backupIoFile, ExistingFileHandling.RenameExistingFile))
        backupFile = new BackupFile(backupIoFile, getFileServiceType());
    } catch(Exception ex) {
      log.error("Could not create Backup File", ex);
      if(listener != null)
        listener.createBackupDone(CreateBackupResult.CouldNotCreateBackupFile, DeepThoughtError.errorFromLocalizationKey(ex, "error.backup.file.could.not.be.created",
                ex.getLocalizedMessage()), backupFile);
    }

    return backupFile;
  }

  protected abstract String createBackupFileName(CreateBackupParams params);

  protected void checkIfMaximumCountBackupsExceeded() {
    if(Application.getSettings() == null)
      return;

    List<BackupFile> sortedBackupFiles = getAvailableBackupsForThisType();

    if(sortedBackupFiles.size() > Application.getSettings().getMaxBackupsToKeep())
      deleteSupernumeraryBackups(sortedBackupFiles, Application.getSettings().getMaxBackupsToKeep());
  }

  public List<BackupFile> getAvailableBackupsForThisType() {
    File backupsFolder = Application.getBackupManager().getBackupsFolder();

    if(backupsFolder == null)
      return new ArrayList<>();

    File[] backupsOfType = backupsFolder.listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        return name.toLowerCase().endsWith(getFileTypeFileExtension());
      }
    });

    if(backupsOfType == null)
      return new ArrayList<>();

    return getBackupFilesSortedByDate(backupsOfType);
  }

  protected List<BackupFile> getBackupFilesSortedByDate(File[] backups) {
    List<BackupFile> sortedBackupFiles = new ArrayList<>();

    for(File backupFile : backups)
      sortedBackupFiles.add(new BackupFile(backupFile, getFileServiceType()));

    Collections.sort(sortedBackupFiles, new Comparator<BackupFile>() {
      @Override
      public int compare(BackupFile o1, BackupFile o2) {
        int comparisonResult = o2.getBackupTime().compareTo(o1.getBackupTime()); // we want BackupFiles to be sorted in reversed order -> compare o2 to o1
        if (comparisonResult == 0) // Backup Times equal, e.g. DeepThoughtBackup_2015.01.03_16-49-07.json and DeepThoughtBackup_2015.01.03_16-49-07(2).json
          comparisonResult = o2.getFileName().compareTo(o1.getFileName()); // -> compare their file names

        return comparisonResult;
      }
    });

    return sortedBackupFiles;
  }

  protected void deleteSupernumeraryBackups(List<BackupFile> sortedBackupFiles, int maxBackupsToKeep) {
    for(int i = sortedBackupFiles.size() - 1; i >= maxBackupsToKeep; i--) {
      BackupFile backupFile = sortedBackupFiles.get(i);
      try {
        FileUtils.deleteFile(backupFile.getFile());
      } catch(Exception ex) {
        log.error("Could not delete file " + backupFile, ex);
      }
    }
  }

  public BackupFileServiceType getFileServiceType() {
    return fileServiceType;
  }


  /**
   *
   * @param backupFile
   * @return Returns {@code net.deepthought.data.backup.IBackupManager.CouldNotExtractBackupTimeFromFileName} if Time couldn't be extracted
   */
  public static Date getBackupTimeFromFileName(File backupFile) {
    return getBackupTimeFromFileName(backupFile.getName());
  }

  /**
   *
   * @param backupFileName
   * @return Returns {@code net.deepthought.data.backup.IBackupManager.CouldNotExtractBackupTimeFromFileName} if Time couldn't be extracted
   */
  public static Date getBackupTimeFromFileName(String backupFileName) {
    int fileExtensionIndex = backupFileName.lastIndexOf('.');
    if(fileExtensionIndex <= 0)
      return CouldNotExtractBackupTimeFromFileName;

    int secondUnderscoreIndex = backupFileName.lastIndexOf('_', fileExtensionIndex - 1);
    if(secondUnderscoreIndex <= 0)
      return CouldNotExtractBackupTimeFromFileName;

    int firstUnderscoreIndex = backupFileName.lastIndexOf('_', secondUnderscoreIndex - 1);
    if(firstUnderscoreIndex <= 0)
      return CouldNotExtractBackupTimeFromFileName;

    String dateString = backupFileName.substring(firstUnderscoreIndex + 1, fileExtensionIndex);
    if(dateString.contains("(")) // e.g. automatically renamed files contain '(2)' like: DeepThoughtBackup_2015.01.03_16-45-45(2).json
      dateString = dateString.substring(0, dateString.lastIndexOf("(") - 1);
    try {
      return BackupDateFormat.parse(dateString);
    } catch(Exception ex) {
      log.error("Could not extract Backup Time from Backup File name " + backupFileName, ex);
    }

    return CouldNotExtractBackupTimeFromFileName;
  }

  protected void logElapsedTime(Date startTime) {
    long millisecondsElapsed = (new Date().getTime() - startTime.getTime());
    log.debug("Writing took " + (millisecondsElapsed / 1000) + "." + String.format("%03d", millisecondsElapsed).substring(0, 3) + " seconds");
  }


  @Override
  public String toString() {
//    return Localization.getLocalizedString(getFileTypeKey());
    return "" + getFileServiceType();
  }
}

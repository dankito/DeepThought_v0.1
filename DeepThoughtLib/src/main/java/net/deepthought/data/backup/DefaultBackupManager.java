package net.deepthought.data.backup;

import net.deepthought.Application;
import net.deepthought.data.model.enums.BackupFileServiceType;
import net.deepthought.util.DeepThoughtError;
import net.deepthought.util.file.FileUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ganymed on 01/01/15.
 */
public class DefaultBackupManager implements IBackupManager {

  private final static Logger log = LoggerFactory.getLogger(DefaultBackupManager.class);


  protected Map<BackupFileServiceType, IBackupFileService> mapBackupFileServices = new HashMap<>();


  public DefaultBackupManager() {
    registerBackupFileService(AllBackupsFileService);

    IBackupFileService copyDatabaseBackupFileService = new DatabaseBackupFileService();
    registerBackupFileService(copyDatabaseBackupFileService);

//    IBackupFileService jsonBackupFileService = new JsonIoBackupFileService();
//    registerBackupFileService(jsonBackupFileService);
  }


  public Collection<IBackupFileService> getRegisteredBackupFileServices() {
    return mapBackupFileServices.values();
  }

  public boolean registerBackupFileService(IBackupFileService backupFileService) {
    return mapBackupFileServices.put(backupFileService.getFileServiceType(), backupFileService) != null;
  }

  public boolean unregisterBackupFileService(IBackupFileService backupFileService) {
    return mapBackupFileServices.remove(backupFileService.getFileServiceType(), backupFileService);
  }


  @Override
  public void createBackupsForAllRegisteredBackupFileServices() {
    for(IBackupFileService backupFileService : mapBackupFileServices.values()) {
      try {
        if(backupFileService != AllBackupsFileService)
          backupFileService.createBackup(new CreateBackupParams(Application.getApplication(), getBackupsFolderPath(), null));
      } catch(Exception ex) { log.error("Could not create Database Backup for BackupFileService " + backupFileService.getFileServiceType(), ex); }
    }
  }


  public void restoreBackupAsync(final RestoreBackupParams params) {
    Application.getThreadPool().runTaskAsync(new Runnable() {
      @Override
      public void run() {
        restoreBackup(params);
      }
    });
  }

  public void restoreBackup(RestoreBackupParams params) {
    if(mapBackupFileServices.containsKey(params.getBackupFile().getFileServiceType())) {
      IBackupFileService backupFileService = mapBackupFileServices.get(params.getBackupFile().getFileServiceType());
      backupFileService.restoreBackup(params);
    }
    else {
      log.error("Could not find IBackupFileService for fileTypeKey " + params.getBackupFile().getFileServiceType());
      if(params.getListener() != null)
        params.getListener().restoreBackupDone(false,new RestoreBackupResult(params.getBackupFile(), DeepThoughtError.errorFromLocalizationKey("error.could.not.find.backup.restore.service.for.type", params.getBackupFile().getFileServiceType())));
    }
  }


  public boolean copyDataCollectionAndBackupFileToRestoredBackupsFolder(RestoreBackupParams params) {
    Date backupStartTime = new Date();

    boolean result = copyCurrentDataCollectionToRestoredBackupsFolder(backupStartTime);
    result &= copyBackupFileToRestoredBackupsFolder(params, backupStartTime);

    return result;
  }

  protected boolean copyCurrentDataCollectionToRestoredBackupsFolder(Date backupStartTime) {
    File restoredBackupsFolder = getRestoredBackupsFolder();
    File currentDataCollectionPath = new File(Application.getDataCollectionSavePath());
    String extension = (currentDataCollectionPath.isDirectory() ? "" : ".db");

    File databaseBackupPath = new File(restoredBackupsFolder, BackupDateFormat.format(backupStartTime) + "_DeepThoughtDb_BeforeRestore" + extension);
    return FileUtils.copyFile(currentDataCollectionPath, databaseBackupPath);
  }

  protected boolean copyBackupFileToRestoredBackupsFolder(RestoreBackupParams params, Date backupStartTime) {
    File restoredBackupsFolder = getRestoredBackupsFolder();
    File currentBackupFilePath = params.getBackupFile().getFile();

    File restoredBackupPath = new File(restoredBackupsFolder, BackupDateFormat.format(backupStartTime) + "_" + currentBackupFilePath.getName());
    params.setRestoredBackupPath(restoredBackupPath);

    return FileUtils.copyFile(currentBackupFilePath, restoredBackupPath);
  }

  public File getRestoredBackupsFolder() {
    File restoredBackupsFolder = new File(Application.getDataFolderPath(), "restored_backups");

    if(restoredBackupsFolder.exists() == false)
      restoredBackupsFolder.mkdirs();

    return restoredBackupsFolder;
  }


  public List<BackupFile> getAllAvailableBackups() {
    List<BackupFile> allBackupFiles = new ArrayList<>();

    for(IBackupFileService backupFileService : mapBackupFileServices.values()) {
      if(backupFileService != AllBackupsFileService)
        allBackupFiles.addAll(backupFileService.getAvailableBackupsForThisType());
    }

    return allBackupFiles;
  }

  public List<BackupFile> getAvailableBackupsForFileType(BackupFileServiceType fileServiceType) {
    if(AllBackupsFileService.getFileServiceType().equals(fileServiceType))
      return getAllAvailableBackups();
    else if(mapBackupFileServices.containsKey(fileServiceType)) {
      IBackupFileService backupFileService = mapBackupFileServices.get(fileServiceType);
      return backupFileService.getAvailableBackupsForThisType();
    }
    else {
      log.error("Could not find IBackupFileService for fileServiceType " + fileServiceType);
    }

    return new ArrayList<>();
  }

  /**
   *
   * @param backupFile
   * @return Returns {@code net.deepthought.data.backup.IBackupManager.CouldNotExtractBackupTimeFromFileName} if Time couldn't be extracted
   */
  public Date getBackupTimeFromFileName(File backupFile) {
    return getBackupTimeFromFileName(backupFile.getName());
  }

  /**
   *
   * @param backupFileName
   * @return Returns {@code net.deepthought.data.backup.IBackupManager.CouldNotExtractBackupTimeFromFileName} if Time couldn't be extracted
   */
  public Date getBackupTimeFromFileName(String backupFileName) {
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

  @Override
  public String getBackupsFolderPath() {
    if(Application.getDataFolderPath() == Application.CouldNotGetDataFolderPath)
      return Application.CouldNotGetDataFolderPath;

    return Application.getDataFolderPath() + "backups/";
  }

  @Override
  public File getBackupsFolder() {
    if(getBackupsFolderPath() == Application.CouldNotGetDataFolderPath)
      return null;

    return new File(getBackupsFolderPath());
  }

}

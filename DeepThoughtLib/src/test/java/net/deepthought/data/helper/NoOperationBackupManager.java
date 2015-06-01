package net.deepthought.data.helper;

import net.deepthought.data.backup.BackupFile;
import net.deepthought.data.backup.IBackupFileService;
import net.deepthought.data.backup.IBackupManager;
import net.deepthought.data.backup.RestoreBackupParams;
import net.deepthought.data.model.enums.BackupFileServiceType;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Created by ganymed on 14/02/15.
 */
public class NoOperationBackupManager implements IBackupManager {

  @Override
  public void createBackupsForAllRegisteredBackupFileServices() {

  }

  @Override
  public Collection<IBackupFileService> getRegisteredBackupFileServices() {
    return new ArrayList<>();
  }

  @Override
  public boolean registerBackupFileService(IBackupFileService backupFileService) {
    return false;
  }

  @Override
  public boolean unregisterBackupFileService(IBackupFileService backupFileService) {
    return false;
  }

  @Override
  public List<BackupFile> getAllAvailableBackups() {
    return new ArrayList<>();
  }

  @Override
  public List<BackupFile> getAvailableBackupsForFileType(BackupFileServiceType fileServiceType) {
    return new ArrayList<>();
  }

  @Override
  public void restoreBackupAsync(RestoreBackupParams params) {

  }

  @Override
  public void restoreBackup(RestoreBackupParams params) {

  }

  @Override
  public boolean copyDataCollectionAndBackupFileToRestoredBackupsFolder(RestoreBackupParams params) {
    return false;
  }

  @Override
  public Date getBackupTimeFromFileName(File backupFile) {
    return null;
  }

  @Override
  public Date getBackupTimeFromFileName(String backupFileName) {
    return null;
  }

  @Override
  public File getBackupsFolder() {
    return new File(getBackupsFolderPath());
  }

  @Override
  public String getBackupsFolderPath() {
    return "data/tests/backups";
  }

  @Override
  public File getRestoredBackupsFolder() {
    return null;
  }
}

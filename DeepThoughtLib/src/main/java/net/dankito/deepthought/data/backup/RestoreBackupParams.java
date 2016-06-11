package net.dankito.deepthought.data.backup;

import net.dankito.deepthought.data.backup.enums.BackupRestoreType;
import net.dankito.deepthought.data.backup.listener.RestoreBackupListener;
import net.dankito.deepthought.data.merger.enums.MergeEntities;

import java.io.File;

/**
 * Created by ganymed on 05/01/15.
 */
public class RestoreBackupParams {

  protected BackupFile backupFile;

  protected BackupRestoreType restoreType;

  protected MergeEntities mergeEntities = MergeEntities.TryToMergeAllEntities;

  protected File restoredBackupPath = null;

  protected RestoreBackupListener listener;


  public RestoreBackupParams(BackupFile backupFile, BackupRestoreType restoreType, RestoreBackupListener listener) {
    this.backupFile = backupFile;
    this.restoreType = restoreType;
    this.listener = listener;
  }

  public RestoreBackupParams(BackupFile backupFile, BackupRestoreType restoreType, MergeEntities mergeEntities, RestoreBackupListener listener) {
    this(backupFile, restoreType, listener);
    this.mergeEntities = mergeEntities;
  }


  public BackupFile getBackupFile() {
    return backupFile;
  }

  public BackupRestoreType getRestoreType() {
    return restoreType;
  }

  public MergeEntities getMergeEntities() {
    return mergeEntities;
  }

  public RestoreBackupListener getListener() {
    return listener;
  }

  public File getRestoredBackupPath() {
    if(restoredBackupPath == null)
      return backupFile.getFile();

    return restoredBackupPath;
  }

  protected void setRestoredBackupPath(File restoredBackupPath) {
    this.restoredBackupPath = restoredBackupPath;
  }
}

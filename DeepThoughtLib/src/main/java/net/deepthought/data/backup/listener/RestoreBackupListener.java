package net.deepthought.data.backup.listener;

import net.deepthought.data.backup.BackupFile;
import net.deepthought.data.backup.RestoreBackupResult;
import net.deepthought.data.backup.RestoreBackupStepResult;
import net.deepthought.data.backup.enums.BackupStep;
import net.deepthought.data.persistence.db.BaseEntity;

import java.util.List;

/**
 * Created by ganymed on 01/01/15.
 */
public interface RestoreBackupListener {

  public void beginStep(BackupFile file, BackupStep step);

  public void stepDone(RestoreBackupStepResult stepResult);

  public List<BaseEntity> selectEntitiesToRestore(BackupFile file, BaseEntity restoredData);

  public void restoreBackupDone(boolean successful, RestoreBackupResult result);

}

package net.dankito.deepthought.data.backup.listener;

import net.dankito.deepthought.data.backup.BackupFile;
import net.dankito.deepthought.data.backup.RestoreBackupStepResult;
import net.dankito.deepthought.data.backup.enums.BackupStep;
import net.dankito.deepthought.data.backup.RestoreBackupResult;
import net.dankito.deepthought.data.persistence.db.BaseEntity;

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

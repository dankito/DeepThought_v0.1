package net.dankito.deepthought.data.backup.listener;

import net.dankito.deepthought.data.backup.BackupFile;
import net.dankito.deepthought.data.backup.enums.CreateBackupResult;
import net.dankito.deepthought.util.DeepThoughtError;

/**
 * Created by ganymed on 01/01/15.
 */
public interface CreateBackupListener {

  public void createBackupDone(CreateBackupResult result, DeepThoughtError error, BackupFile file);

}

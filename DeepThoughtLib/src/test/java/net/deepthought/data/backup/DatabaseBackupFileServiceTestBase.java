package net.deepthought.data.backup;

/**
 * Created by ganymed on 01/01/15.
 */
public abstract class DatabaseBackupFileServiceTestBase extends BackupFileServiceTestBase {

  @Override
  protected IBackupFileService createBackupFileService() {
    return new DatabaseBackupFileService();
  }

}

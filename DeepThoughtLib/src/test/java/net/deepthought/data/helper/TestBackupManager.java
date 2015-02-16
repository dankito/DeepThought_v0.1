package net.deepthought.data.helper;

import net.deepthought.data.backup.DefaultBackupManager;

/**
 * Created by ganymed on 07/01/15.
 */
public class TestBackupManager extends DefaultBackupManager {

  @Override
  public String getBackupsFolderPath() {
    return "data/tests/backups";
  }

}

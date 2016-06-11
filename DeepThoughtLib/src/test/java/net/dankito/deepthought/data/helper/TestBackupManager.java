package net.dankito.deepthought.data.helper;

import net.dankito.deepthought.data.backup.DefaultBackupManager;

/**
 * Created by ganymed on 07/01/15.
 */
public class TestBackupManager extends DefaultBackupManager {

  @Override
  public String getBackupsFolderPath() {
    return "data/tests/backups";
  }

}

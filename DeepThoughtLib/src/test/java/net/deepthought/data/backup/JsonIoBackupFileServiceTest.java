package net.deepthought.data.backup;

import net.deepthought.data.helper.MockEntityManager;
import net.deepthought.data.persistence.EntityManagerConfiguration;
import net.deepthought.data.persistence.IEntityManager;

/**
 * Created by ganymed on 01/01/15.
 */
public class JsonIoBackupFileServiceTest extends BackupFileServiceTestBase {

  @Override
  protected IBackupFileService createBackupFileService() {
    return new JsonIoBackupFileService();
  }

  @Override
  protected IEntityManager createTestEntityManager(EntityManagerConfiguration configuration) throws Exception {
    return new MockEntityManager();
  }
}

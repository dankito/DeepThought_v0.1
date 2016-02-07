package net.deepthought.data.backup;

import net.deepthought.data.helper.MockEntityManager;
import net.deepthought.data.persistence.EntityManagerConfiguration;
import net.deepthought.data.persistence.IEntityManager;

import org.junit.Ignore;

/**
 * Created by ganymed on 01/01/15.
 */
@Ignore
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

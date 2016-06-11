package net.dankito.deepthought.data.backup;

import net.dankito.deepthought.data.helper.MockEntityManager;
import net.dankito.deepthought.data.persistence.EntityManagerConfiguration;
import net.dankito.deepthought.data.persistence.IEntityManager;

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

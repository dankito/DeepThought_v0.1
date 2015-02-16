package net.deepthought.data.backup;

import net.deepthought.data.persistence.EntityManagerConfiguration;
import net.deepthought.data.persistence.IEntityManager;
import net.deepthought.persistence.JpaEntityManager;

/**
 * Created by ganymed on 07/01/15.
 */
public class JsonIoBackupFileServiceTest extends JsonIoBackupFileServiceTestBase {
  @Override
  protected IEntityManager createTestEntityManager(EntityManagerConfiguration configuration) throws Exception {
    return new JpaEntityManager(configuration);
  }
}

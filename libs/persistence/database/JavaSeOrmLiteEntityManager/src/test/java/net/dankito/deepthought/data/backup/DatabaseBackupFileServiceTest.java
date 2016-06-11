package net.dankito.deepthought.data.backup;

import net.dankito.deepthought.data.backup.DatabaseBackupFileServiceTestBase;
import net.dankito.deepthought.data.persistence.EntityManagerConfiguration;
import net.dankito.deepthought.data.persistence.IEntityManager;
import net.dankito.deepthought.javase.db.OrmLiteJavaSeEntityManager;

/**
 * Created by ganymed on 07/01/15.
 */
public class DatabaseBackupFileServiceTest extends DatabaseBackupFileServiceTestBase {

  @Override
  protected IEntityManager createTestEntityManager(EntityManagerConfiguration configuration) throws Exception {
    return new OrmLiteJavaSeEntityManager(configuration);
  }
}

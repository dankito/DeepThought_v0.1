package net.dankito.deepthought.android.data.persistence.db;

import android.test.AndroidTestCase;

import net.dankito.deepthought.android.data.persistence.db.OrmLiteAndroidEntityManager;
import net.dankito.deepthought.data.model.DeepThoughtTestBase;
import net.dankito.deepthought.data.persistence.EntityManagerConfiguration;
import net.dankito.deepthought.data.persistence.IEntityManager;

import java.sql.SQLException;

/**
 * Created by ganymed on 10/12/14.
 */
public class DeepThoughtTests extends AndroidTestCase {

  DeepThoughtTestBase deepThoughtTests = null;

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    deepThoughtTests = new DeepThoughtTestBase() {
      @Override
      protected IEntityManager getEntityManager(EntityManagerConfiguration configuration) throws SQLException {
        return new OrmLiteAndroidEntityManager(getContext(), configuration);
      }
    };
    deepThoughtTests.setup();
  }

  @Override
  protected void tearDown() throws Exception {
    deepThoughtTests.tearDown();

    super.tearDown();
  }

  public void testAddCategory_CategoryGetsPersisted() throws Exception {
    deepThoughtTests.addCategory_CategoryGetsPersisted();
  }

  public void testUpdateName_UpdatedNameGetsPersistedInDb() throws Exception {
    deepThoughtTests.addCategory_RelationsGetSet();
  }
}

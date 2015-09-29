package net.deepthought.android.data.persistence.db;

import android.test.AndroidTestCase;

import com.j256.ormlite.dao.cda.jointable.JoinTableDaoRegistry;

import net.deepthought.Application;
import net.deepthought.TestApplicationConfiguration;
import net.deepthought.TestEntityManagerConfiguration;
import net.deepthought.android.data.persistence.db.helper.TestJoinTableDaoRegistry;
import net.deepthought.data.persistence.EntityManagerConfiguration;
import net.deepthought.data.persistence.IEntityManager;

import java.io.File;

/**
 * Created by ganymed on 10/11/14.
 */
public class EntitiesTestBase extends AndroidTestCase {

  protected IEntityManager entityManager = null;

  @Override
  public void setUp() throws Exception {
    super.setUp();

//    clearDatabase();

    EntityManagerConfiguration configuration = new TestEntityManagerConfiguration();
    entityManager = new OrmLiteAndroidEntityManager(this.getContext(), configuration);

    Application.instantiate(new TestApplicationConfiguration(entityManager));
    JoinTableDaoRegistry.setJoinTableRegistry(new TestJoinTableDaoRegistry());

//    entityManager.clearData();
//    entityManager.setPersistUpdatedEntitiesAfterMilliseconds(0);
//    Data.createInstance(entityManager, false);
  }

  @Override
  public void tearDown() throws Exception {
    super.tearDown();

    Application.shutdown();
//    clearDatabase();
  }

  protected void clearDatabase() {
    try {
//    new File(((OrmLiteAndroidEntityManager) entityManager).getDatabasePath()).delete();
      new File("/data/data/net.deepthought.android.test/databases/DeepThought.db").delete();
    } catch(Exception ex) {

    }
  }
}

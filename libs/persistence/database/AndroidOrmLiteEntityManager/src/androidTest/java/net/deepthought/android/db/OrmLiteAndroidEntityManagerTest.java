package net.deepthought.android.db;

import android.test.AndroidTestCase;

import net.deepthought.Application;
import net.deepthought.data.TestApplicationConfiguration;
import net.deepthought.data.helper.TestDependencyResolver;
import net.deepthought.data.model.DeepThought;
import net.deepthought.data.model.Entry;
import net.deepthought.data.persistence.EntityManagerConfiguration;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ganymed on 09/03/15.
 */
public class OrmLiteAndroidEntityManagerTest extends AndroidTestCase {

  private final static Logger log = LoggerFactory.getLogger(OrmLiteAndroidEntityManagerTest.class);

  protected OrmLiteAndroidEntityManager entityManager = null;
  protected DeepThought deepThought = null;


  @Override
  public void setUp() throws Exception {
    super.setUp();

    EntityManagerConfiguration configuration = EntityManagerConfiguration.createTestConfiguration();
    entityManager = new OrmLiteAndroidEntityManager(getContext(), configuration);

    Application.instantiate(new TestApplicationConfiguration(), new TestDependencyResolver(entityManager));
    deepThought = Application.getDeepThought();
  }

  @Override
  public void tearDown() throws Exception {
//    entityManager.close();
    super.tearDown();

    Application.shutdown();
//    clearDatabase();
  }


  public void testInit() {

    Assert.assertTrue(true);
  }

  public void testPersistEntries() {
    Entry entry1 = new Entry("Test");
    deepThought.addEntry(entry1);

    Assert.assertNotNull(entry1.getId());
  }
}

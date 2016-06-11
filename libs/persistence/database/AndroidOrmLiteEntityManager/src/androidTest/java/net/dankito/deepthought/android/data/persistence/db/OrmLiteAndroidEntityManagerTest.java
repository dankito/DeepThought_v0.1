package net.dankito.deepthought.android.data.persistence.db;

import android.test.AndroidTestCase;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.TestApplicationConfiguration;
import net.dankito.deepthought.TestEntityManagerConfiguration;
import net.dankito.deepthought.data.model.DeepThought;
import net.dankito.deepthought.data.model.Entry;
import net.dankito.deepthought.data.persistence.EntityManagerConfiguration;

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

    EntityManagerConfiguration configuration = new TestEntityManagerConfiguration();
    entityManager = new OrmLiteAndroidEntityManager(getContext(), configuration);

    Application.instantiate(new TestApplicationConfiguration(entityManager));
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

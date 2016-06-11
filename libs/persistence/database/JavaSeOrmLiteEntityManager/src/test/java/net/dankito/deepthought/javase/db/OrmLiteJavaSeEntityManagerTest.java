package net.dankito.deepthought.javase.db;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.TestApplicationConfiguration;
import net.dankito.deepthought.TestEntityManagerConfiguration;
import net.dankito.deepthought.data.model.DeepThought;
import net.dankito.deepthought.data.model.Entry;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

/**
 * Created by ganymed on 11/10/14.
 */
public class OrmLiteJavaSeEntityManagerTest {

  private final static Logger log = LoggerFactory.getLogger(OrmLiteJavaSeEntityManagerTest.class);

  protected net.dankito.deepthought.javase.db.OrmLiteJavaSeEntityManager entityManager = null;
  protected DeepThought deepThought = null;

  @Before
  public void setup() throws SQLException {
    entityManager = new net.dankito.deepthought.javase.db.OrmLiteJavaSeEntityManager(new TestEntityManagerConfiguration(true));

    Application.instantiate(new TestApplicationConfiguration(entityManager));
    deepThought = Application.getDeepThought();
  }


  @Test
  public void init() {

    Assert.assertTrue(true);
  }

  @Test
  public void persistEntries() {
    Entry entry1 = new Entry("Test");
    deepThought.addEntry(entry1);

    Assert.assertNotNull(entry1.getId());
  }

}

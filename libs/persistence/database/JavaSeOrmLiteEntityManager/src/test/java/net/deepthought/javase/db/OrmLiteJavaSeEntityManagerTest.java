package net.deepthought.javase.db;

import net.deepthought.Application;
import net.deepthought.data.helper.TestDependencyResolver;
import net.deepthought.data.model.DeepThought;
import net.deepthought.data.model.Entry;
import net.deepthought.data.persistence.EntityManagerConfiguration;

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

  protected OrmLiteJavaSeEntityManager entityManager = null;
  protected DeepThought deepThought = null;

  @Before
  public void setup() throws SQLException {
    entityManager = new OrmLiteJavaSeEntityManager(EntityManagerConfiguration.createTestConfiguration(true));

    Application.instantiate(new TestDependencyResolver(entityManager));
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

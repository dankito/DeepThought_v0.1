package net.deepthought.data.search;

import net.deepthought.Application;
import net.deepthought.DefaultDependencyResolver;
import net.deepthought.data.model.DeepThought;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.Tag;
import net.deepthought.data.persistence.EntityManagerConfiguration;
import net.deepthought.data.persistence.IEntityManager;
import net.deepthought.data.search.helper.TestApplicationConfiguration;
import net.deepthought.javase.db.OrmLiteJavaSeEntityManager;

import org.apache.lucene.queryparser.classic.ParseException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

/**
 * Created by ganymed on 17/03/15.
 */
public class LuceneSearchEngineDatabaseTest {

  protected LuceneSearchEngine searchEngine;

  protected IEntityManager entityManager = null;
  protected EntityManagerConfiguration configuration = null;

  protected DeepThought deepThought = null;


  @Before
  public void setup() throws Exception {
    searchEngine = new LuceneSearchEngine();

    configuration = EntityManagerConfiguration.createTestConfiguration(true);
//    FileUtils.deleteFile(configuration.getDataCollectionPersistencePath());

    entityManager = new OrmLiteJavaSeEntityManager(configuration);

    Application.instantiate(new TestApplicationConfiguration(), new DefaultDependencyResolver(entityManager));
    deepThought = Application.getDeepThought();
  }

  @After
  public void tearDown() {
    Application.shutdown();
  }


  @Test
  public void findAllEntriesWithoutTags() throws IOException, ParseException {
    Tag tag1 = new Tag("tag1");
    Tag tag2 = new Tag("tag2");
    Tag tag3 = new Tag("tag3");
    deepThought.addTag(tag1);
    deepThought.addTag(tag2);
    deepThought.addTag(tag3);

    Entry entryWithoutTags1 = new Entry("without1");
    Entry entryWithoutTags2 = new Entry("without2");
    Entry entryWithoutTags3 = new Entry("without3");
    deepThought.addEntry(entryWithoutTags1);
    deepThought.addEntry(entryWithoutTags2);
    deepThought.addEntry(entryWithoutTags3);

    Entry entryWithTags1 = new Entry("test1");
    Entry entryWithTags2 = new Entry("test2");
    deepThought.addEntry(entryWithTags1);
    deepThought.addEntry(entryWithTags2);

    entryWithTags1.addTag(tag1);
    entryWithTags1.addTag(tag2);
    entryWithTags2.addTag(tag2);
    entryWithTags2.addTag(tag3);

    searchEngine.updateIndex(entryWithoutTags1);
    searchEngine.updateIndex(entryWithTags1);
    searchEngine.updateIndex(entryWithoutTags2);
    searchEngine.updateIndex(entryWithTags2);
    searchEngine.updateIndex(entryWithoutTags3);

    List<Long> entriesWithoutTags = searchEngine.getEntriesWithoutTags();
    Assert.assertEquals(3, entriesWithoutTags.size());

    Assert.assertTrue(entriesWithoutTags.contains(entryWithoutTags1.getId()));
    Assert.assertTrue(entriesWithoutTags.contains(entryWithoutTags2.getId()));
    Assert.assertTrue(entriesWithoutTags.contains(entryWithoutTags3.getId()));

    Assert.assertFalse(entriesWithoutTags.contains(entryWithTags1.getId()));
    Assert.assertFalse(entriesWithoutTags.contains(entryWithTags2.getId()));
  }

}

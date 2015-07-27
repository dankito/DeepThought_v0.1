package net.deepthought.data.search;

import net.deepthought.Application;
import net.deepthought.DefaultDependencyResolver;
import net.deepthought.data.TestApplicationConfiguration;
import net.deepthought.data.model.DeepThought;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.Tag;
import net.deepthought.data.persistence.EntityManagerConfiguration;
import net.deepthought.data.persistence.IEntityManager;
import net.deepthought.javase.db.OrmLiteJavaSeEntityManager;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.store.RAMDirectory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;

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
    searchEngine = new LuceneSearchEngine(new RAMDirectory());

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

    searchEngine.indexEntity(entryWithoutTags1);
    searchEngine.indexEntity(entryWithTags1);
    searchEngine.indexEntity(entryWithoutTags2);
    searchEngine.indexEntity(entryWithTags2);
    searchEngine.indexEntity(entryWithoutTags3);

    final List<Entry> entriesWithoutTags = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.getEntriesWithoutTags(new SearchCompletedListener<Collection<Entry>>() {
      @Override
      public void completed(Collection<Entry> results) {
        entriesWithoutTags.addAll(results);
        countDownLatch.countDown();
      }
    });

    try { countDownLatch.await(); } catch(Exception ex) { }
    Assert.assertEquals(3, entriesWithoutTags.size());

    Assert.assertTrue(entriesWithoutTags.contains(entryWithoutTags1));
    Assert.assertTrue(entriesWithoutTags.contains(entryWithoutTags2));
    Assert.assertTrue(entriesWithoutTags.contains(entryWithoutTags3));

    Assert.assertFalse(entriesWithoutTags.contains(entryWithTags1));
    Assert.assertFalse(entriesWithoutTags.contains(entryWithTags2));
  }

}

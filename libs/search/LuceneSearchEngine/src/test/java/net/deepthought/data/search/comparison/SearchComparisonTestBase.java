package net.deepthought.data.search.comparison;

import net.deepthought.Application;
import net.deepthought.DefaultDependencyResolver;
import net.deepthought.data.ApplicationConfiguration;
import net.deepthought.data.TestApplicationConfiguration;
import net.deepthought.data.model.DeepThought;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.ReferenceBase;
import net.deepthought.data.model.Tag;
import net.deepthought.data.persistence.EntityManagerConfiguration;
import net.deepthought.data.persistence.IEntityManager;
import net.deepthought.data.search.ISearchEngine;
import net.deepthought.data.search.LuceneSearchEngine;
import net.deepthought.data.search.Search;
import net.deepthought.data.search.SearchCompletedListener;
import net.deepthought.javase.db.OrmLiteJavaSeEntityManager;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

/**
 * Created by ganymed on 16/04/15.
 */
public abstract class SearchComparisonTestBase {

  protected final static Logger log = LoggerFactory.getLogger(SearchComparisonTestBase.class);

  private final static Marker operationProcessTimeMarker = MarkerFactory.getMarker("OperationTime");

  protected static Map<String, Long> operationsProcessTime = new HashMap<>();


  protected ISearchEngine searchEngine;

  protected static IEntityManager entityManager = null;

  protected static DeepThought deepThought = null;

  protected Date startTime = null;


  @BeforeClass
  public static void suiteSetup() throws Exception {
    ApplicationConfiguration configuration = new TestApplicationConfiguration("data/tests/big_data_tests/bin/data/");

    Application.instantiate(configuration, new DefaultDependencyResolver() {
      @Override
      public IEntityManager createEntityManager(EntityManagerConfiguration configuration) throws Exception {
        entityManager = new OrmLiteJavaSeEntityManager(configuration);
        SearchComparisonTestBase.entityManager = entityManager;
        return entityManager;
      }

//      @Override
//      public ISearchEngine createSearchEngine() {
//        SearchComparisonTestBase.this.searchEngine = SearchComparisonTestBase.this.createSearchEngine();
//        searchEngine = SearchComparisonTestBase.this.searchEngine;
//        return searchEngine;
//      }
    });

    deepThought = Application.getDeepThought();

    LuceneSearchEngine rebuildIndexSearchEngine = new LuceneSearchEngine();
//    rebuildIndexSearchEngine.rebuildIndex();
    int i = 0;
    for(Entry entry : deepThought.getEntries()) {
      rebuildIndexSearchEngine.indexEntity(entry);
      i++;
      if(i > 10)
        break;
    }
  }


  @Before
  public void setup() throws Exception {
    searchEngine = createSearchEngine();

    startTime = new Date();
  }

  @After
  public void tearDown() {
//    Application.shutdown();
  }

  @AfterClass
  public static void tearDownSuite() {

  }


  protected abstract ISearchEngine createSearchEngine();


  @Test
  public void filterTags() {
    searchEngine.filterTags(new Search<Tag>("Dürer", new SearchCompletedListener<Tag>() {
      @Override
      public void completed(Collection<Tag> results) {
        logOperationProcessTime("filterTags");
        Assert.assertEquals(2, results.size());
      }
    }));
  }


  @Test
  public void findAllEntriesHavingTheseTags() {
    List<Tag> allTags = new ArrayList<>(deepThought.getTags());
    List<Tag> tagsToFilterFor = new ArrayList<>();
    tagsToFilterFor.add(allTags.get(0));
    tagsToFilterFor.add(allTags.get(33));

    Set<Entry> entriesHavingFilteredTags = new HashSet<>();
    final Set<Tag> tagsOnEntriesContainingFilteredTags = new HashSet<>();

    searchEngine.findAllEntriesHavingTheseTags(tagsToFilterFor, entriesHavingFilteredTags, tagsOnEntriesContainingFilteredTags);

    logOperationProcessTime("findAllEntriesHavingTheseTags");
    Assert.assertEquals(2, entriesHavingFilteredTags.size());
    Assert.assertEquals(2, tagsOnEntriesContainingFilteredTags.size());
  }


//  @Test
//  public void filterPersons() {
//    searchEngine.filterPersons(new Search<Person>("Dürer", new SearchCompletedListener<Person>() {
//      @Override
//      public void completed(Collection<Person> results) {
//        logOperationProcessTime("filterPersons");
//        Assert.assertEquals(1, results.size());
//      }
//    }));
//  }

  @Test
  public void filterReferenceBases() {
    final CountDownLatch filterReferenceBasesForWorldLatch = new CountDownLatch(1);

    searchEngine.filterReferenceBases(new Search<ReferenceBase>("World", new SearchCompletedListener<ReferenceBase>() {
      @Override
      public void completed(Collection<ReferenceBase> results) {
        logOperationProcessTime("filterReferenceBases for World");
//        Assert.assertEquals(1, results.size());
        filterReferenceBasesForWorldLatch.countDown();
      }
    }));

    try { filterReferenceBasesForWorldLatch.await(); } catch(Exception ex) { }
    startTime = new Date();

    searchEngine.filterReferenceBases(new Search<>("Ardents", new SearchCompletedListener<ReferenceBase>() {
      @Override
      public void completed(Collection<ReferenceBase> results) {
        logOperationProcessTime("filterReferenceBases for Angkor");
        Assert.assertEquals(1, results.size());
      }
    }));
  }

  private void logOperationProcessTime(String operationName) {
    long millisecondsElapsed = (new Date().getTime() - startTime.getTime());
    operationsProcessTime.put(operationName, millisecondsElapsed);
    log.info(operationProcessTimeMarker, operationName + " took " + (millisecondsElapsed / 1000) + "." + String.format("%03d", millisecondsElapsed).substring(0, 3) + " seconds");
  }
}

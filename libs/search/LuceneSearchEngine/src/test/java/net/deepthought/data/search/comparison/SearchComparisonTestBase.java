package net.deepthought.data.search.comparison;

import net.deepthought.Application;
import net.deepthought.DefaultDependencyResolver;
import net.deepthought.data.ApplicationConfiguration;
import net.deepthought.data.TestApplicationConfiguration;
import net.deepthought.data.backup.IBackupManager;
import net.deepthought.data.helper.NoOperationBackupManager;
import net.deepthought.data.model.DeepThought;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.Person;
import net.deepthought.data.model.Tag;
import net.deepthought.data.persistence.EntityManagerConfiguration;
import net.deepthought.data.persistence.IEntityManager;
import net.deepthought.data.search.FilterTagsSearch;
import net.deepthought.data.search.FilterTagsSearchResult;
import net.deepthought.data.search.FilterTagsSearchResults;
import net.deepthought.data.search.ISearchEngine;
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
    long startTime = new Date().getTime();

    ApplicationConfiguration configuration = new TestApplicationConfiguration("data/tests/big_data/");
//    ApplicationConfiguration configuration = new TestApplicationConfiguration("data/tests/big_data_3000_Entries/");
//    ApplicationConfiguration configuration = new TestApplicationConfiguration("data/tests/big_data_tests/data/");
//    ApplicationConfiguration configuration = new TestApplicationConfiguration("data/");
//    ApplicationConfiguration configuration = new TestApplicationConfiguration("/run/media/ganymed/fast_data/programme/DeepThought/data/");

    Application.instantiate(configuration, new DefaultDependencyResolver() {
      @Override
      public IEntityManager createEntityManager(EntityManagerConfiguration configuration) throws Exception {
        entityManager = new OrmLiteJavaSeEntityManager(configuration);
        SearchComparisonTestBase.entityManager = entityManager;
        return entityManager;
      }

      @Override
      public IBackupManager createBackupManager() {
        return new NoOperationBackupManager();
      }

      //      @Override
//      public ISearchEngine createSearchEngine() {
//        SearchComparisonTestBase.this.searchEngine = SearchComparisonTestBase.this.createSearchEngine();
//        searchEngine = SearchComparisonTestBase.this.searchEngine;
//        return searchEngine;
//      }
    });

    deepThought = Application.getDeepThought();
    logOperationProcessTime("Start DeepThought", startTime);
  }


  @Before
  public void setup() throws Exception {
    searchEngine = createSearchEngine();

    startTime = new Date();
  }

  @After
  public void tearDown() {
    searchEngine.close();
  }

  @AfterClass
  public static void tearDownSuite() {
    Application.shutdown();

    log.info(operationProcessTimeMarker, "Operation times have been:");
    for(String operationName : operationsProcessTime.keySet()) {
      long millisecondsElapsed = operationsProcessTime.get(operationName);
      log.info(operationProcessTimeMarker, operationName + " took " + (millisecondsElapsed / 1000) + "." + String.format("%03d", millisecondsElapsed).substring(0, 3) + " seconds");
    }
  }


  protected abstract ISearchEngine createSearchEngine();


  @Test
  public void getEntriesWithoutTags() {
    final List<Entry> searchResults = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.getEntriesWithoutTags(new SearchCompletedListener<Collection<Entry>>() {
      @Override
      public void completed(Collection<Entry> results) {
        logOperationProcessTime("getEntriesWithoutTags");
        searchResults.addAll(results);
        countDownLatch.countDown();
      }
    });

    try { countDownLatch.await(); } catch(Exception ex) { }

    for(Entry entryWithoutTag : searchResults)
      Assert.assertFalse(entryWithoutTag.hasTags());
    Assert.assertEquals(844, searchResults.size());
  }

  @Test
  public void filterTags() {
    final List<Tag> searchResults = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.filterTags(new FilterTagsSearch("Zeit", new SearchCompletedListener<FilterTagsSearchResults>() {
      @Override
      public void completed(FilterTagsSearchResults results) {
        searchResults.addAll(results.getAllMatches());
        logOperationProcessTime("filterTags");
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(); } catch(Exception ex) { }
    Assert.assertEquals(857, searchResults.size());
  }

  @Test
  public void filterForMultipleTags() {
    final FilterTagsSearchResults searchResults = new FilterTagsSearchResults();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.filterTags(new FilterTagsSearch("zeit,mario", new SearchCompletedListener<FilterTagsSearchResults>() { // TODO: use tags available in test database
      @Override
      public void completed(FilterTagsSearchResults results) {
        for (FilterTagsSearchResult result : results.getResults())
          searchResults.addSearchResult(result);
        logOperationProcessTime("filterForMultipleTags");
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(); } catch(Exception ex) { }

    Assert.assertEquals(3, searchResults.getResults().size());
    for(FilterTagsSearchResult result : searchResults.getResults()) {
      Assert.assertTrue(result.hasExactMatch() || result.getAllMatchesCount() == 1);
    }
    Assert.assertEquals(11, searchResults.getAllMatches().size());
  }


  @Test
  public void findAllEntriesHavingTheseTags_ArchäologieOnly() {
    List<Tag> tagsToFilterFor = new ArrayList<>();
    tagsToFilterFor.add(entityManager.getEntityById(Tag.class, 1L));

    Set<Entry> entriesHavingFilteredTags = new HashSet<>();
    final Set<Tag> tagsOnEntriesContainingFilteredTags = new HashSet<>();

    searchEngine.findAllEntriesHavingTheseTags(tagsToFilterFor, entriesHavingFilteredTags, tagsOnEntriesContainingFilteredTags);

    logOperationProcessTime("findAllEntriesHavingTheseTags_ArchäologieOnly");
    Assert.assertEquals(20, entriesHavingFilteredTags.size());
    Assert.assertEquals(43, tagsOnEntriesContainingFilteredTags.size());
  }

  @Test
  public void findAllEntriesHavingTheseTags_AustralopithecusAfricanusAndLatènezeit() {
    List<Tag> tagsToFilterFor = new ArrayList<>();
    tagsToFilterFor.add(entityManager.getEntityById(Tag.class, 41L));
    tagsToFilterFor.add(entityManager.getEntityById(Tag.class, 10L));

    Set<Entry> entriesHavingFilteredTags = new HashSet<>();
    final Set<Tag> tagsOnEntriesContainingFilteredTags = new HashSet<>();

    searchEngine.findAllEntriesHavingTheseTags(tagsToFilterFor, entriesHavingFilteredTags, tagsOnEntriesContainingFilteredTags);

    logOperationProcessTime("findAllEntriesHavingTheseTags_AustralopithecusAfricanusAndLatènezeit");
    Assert.assertEquals(2, entriesHavingFilteredTags.size());
    Assert.assertEquals(7, tagsOnEntriesContainingFilteredTags.size());
  }


  @Test
  public void filterPersons() {
    final CountDownLatch countDownLatch = new CountDownLatch(1);
    final List<Person> searchResults = new ArrayList<>();

    searchEngine.filterPersons(new Search<Person>("Dürer", new SearchCompletedListener<Collection<Person>>() {
      @Override
      public void completed(Collection<Person> results) {
        logOperationProcessTime("filterPersons");
        searchResults.addAll(results);
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(); } catch(Exception ex) { }

    Assert.assertEquals(1, searchResults.size());
  }

//  @Test
//  public void filterAllReferenceBases() {
//    final CountDownLatch countDownLatch = new CountDownLatch(1);
//    final List<ReferenceBase> searchResults = new ArrayList<>();
//
//    searchEngine.filterReferenceBases(new Search<ReferenceBase>("sz", new SearchCompletedListener<ReferenceBase>() {
//      @Override
//      public void completed(Collection<ReferenceBase> results) {
//        logOperationProcessTime("filterEachReferenceBaseWithSeparateFilter for sz");
//        searchResults.addAll(results);
//        countDownLatch.countDown();
//      }
//    }));
//
//    try { countDownLatch.await(); } catch(Exception ex) { }
//    startTime = new Date();
//
//    Assert.assertEquals(316, searchResults.size());
//
////    searchEngine.filterEachReferenceBaseWithSeparateFilter(new Search<>("Angkor", new SearchCompletedListener<ReferenceBase>() {
////      @Override
////      public void completed(Collection<ReferenceBase> results) {
////        logOperationProcessTime("filterEachReferenceBaseWithSeparateFilter for Angkor");
////        Assert.assertEquals(1, results.size());
////      }
////    }));
//  }
//
//  @Test
//  public void filterSeriesTitlesOnly_StartsWithTerm() {
//    final List<ReferenceBase> searchResults = new ArrayList<>();
//    final CountDownLatch countDownLatch = new CountDownLatch(1);
//
//    searchEngine.filterReferenceBases(new Search<ReferenceBase>("wiki,", new SearchCompletedListener<ReferenceBase>() {
//      @Override
//      public void completed(Collection<ReferenceBase> results) {
//        logOperationProcessTime("filterSeriesTitlesOnly_StartsWithTerm for wiki");
//        searchResults.addAll(results);
//        countDownLatch.countDown();
//      }
//    }));
//
//    try { countDownLatch.await(); } catch(Exception ex) { }
//
//    Assert.assertEquals(1, searchResults.size());
//  }
//
//  @Test
//  public void filterSeriesTitlesOnly_ContainsTerm() {
//    final List<ReferenceBase> searchResults = new ArrayList<>();
//    final CountDownLatch countDownLatch = new CountDownLatch(1);
//
//    searchEngine.filterReferenceBases(new Search<ReferenceBase>("iki,", new SearchCompletedListener<ReferenceBase>() {
//      @Override
//      public void completed(Collection<ReferenceBase> results) {
//        logOperationProcessTime("filterSeriesTitlesOnly_ContainsTerm for iki");
//        searchResults.addAll(results);
//        countDownLatch.countDown();
//      }
//    }));
//
//    try { countDownLatch.await(); } catch(Exception ex) { }
//
//    Assert.assertEquals(1, searchResults.size()); // must return the same amount of results as filterSeriesTitlesOnly_StartsWithTerm
//  }
//
//  @Test
//  public void filterReferencesOnly() {
//    final List<ReferenceBase> searchResults = new ArrayList<>();
//    final CountDownLatch countDownLatch = new CountDownLatch(1);
//
//    searchEngine.filterReferenceBases(new Search<ReferenceBase>(",zeit,", new SearchCompletedListener<ReferenceBase>() {
//      @Override
//      public void completed(Collection<ReferenceBase> results) {
//        logOperationProcessTime("filterReferencesOnly for zeit");
//        searchResults.addAll(results);
//        countDownLatch.countDown();
//      }
//    }));
//
//    try { countDownLatch.await(); } catch(Exception ex) { }
//
//    Assert.assertEquals(94, searchResults.size());
//  }
//
//  @Test
//  public void filterReferenceSubDivisionsOnly() {
//    final List<ReferenceBase> searchResults = new ArrayList<>();
//    final CountDownLatch countDownLatch = new CountDownLatch(1);
//
//    searchEngine.filterReferenceBases(new Search<ReferenceBase>(",,nsa", new SearchCompletedListener<ReferenceBase>() {
//      @Override
//      public void completed(Collection<ReferenceBase> results) {
//        logOperationProcessTime("filterReferenceSubDivisionsOnly for nsa");
//        searchResults.addAll(results);
//        countDownLatch.countDown();
//      }
//    }));
//
//    try { countDownLatch.await(); } catch(Exception ex) { }
//
//    Assert.assertEquals(14, searchResults.size());
//  }
//
//  @Test
//  public void filterReferenceBasesForSeriesTitleAndReference() {
//    final List<ReferenceBase> searchResults = new ArrayList<>();
//    final CountDownLatch countDownLatch = new CountDownLatch(1);
//
//    searchEngine.filterReferenceBases(new Search<ReferenceBase>("wiki,stein,", new SearchCompletedListener<ReferenceBase>() {
//      @Override
//      public void completed(Collection<ReferenceBase> results) {
//        logOperationProcessTime("filterReferenceBasesForSeriesTitleAndReference for wiki,zeit,");
//        searchResults.addAll(results);
//        countDownLatch.countDown();
//      }
//    }));
//
//    try { countDownLatch.await(); } catch(Exception ex) { }
//
//    Assert.assertEquals(101, searchResults.size());
//  }
//
//  @Test
//  public void filterReferenceBasesForSeriesTitleReferenceAndReferenceSubDivision() {
//    final List<ReferenceBase> searchResults = new ArrayList<>();
//    final CountDownLatch countDownLatch = new CountDownLatch(1);
//
//    searchEngine.filterReferenceBases(new Search<ReferenceBase>("sz,06,pharma", new SearchCompletedListener<ReferenceBase>() {
//      @Override
//      public void completed(Collection<ReferenceBase> results) {
//        logOperationProcessTime("filterReferenceBasesForSeriesTitleReferenceAndReferenceSubDivision for wiki,zeit,sz,06,pharma");
//        searchResults.addAll(results);
//        countDownLatch.countDown();
//      }
//    }));
//
//    try { countDownLatch.await(); } catch(Exception ex) { }
//
//    Assert.assertEquals(1, searchResults.size());
//  }

  protected void logOperationProcessTime(String operationName) {
    logOperationProcessTime(operationName, startTime.getTime());
  }

  protected static void logOperationProcessTime(String operationName, long startTime) {
    long millisecondsElapsed = (new Date().getTime() - startTime);
    operationsProcessTime.put(operationName, millisecondsElapsed);
    log.info(operationProcessTimeMarker, operationName + " took " + (millisecondsElapsed / 1000) + "." + String.format("%03d", millisecondsElapsed).substring(0, 3) + " seconds");
  }
}

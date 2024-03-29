package net.dankito.deepthought.data.search.comparison;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.TestApplicationConfiguration;
import net.dankito.deepthought.data.backup.IBackupManager;
import net.dankito.deepthought.data.helper.NoOperationBackupManager;
import net.dankito.deepthought.data.model.DeepThought;
import net.dankito.deepthought.data.model.Entry;
import net.dankito.deepthought.data.model.Person;
import net.dankito.deepthought.data.model.Tag;
import net.dankito.deepthought.data.persistence.EntityManagerConfiguration;
import net.dankito.deepthought.data.persistence.IEntityManager;
import net.dankito.deepthought.data.persistence.JavaCouchbaseLiteEntityManager;
import net.dankito.deepthought.data.search.ISearchEngine;
import net.dankito.deepthought.data.search.Search;
import net.dankito.deepthought.data.search.SearchCompletedListener;
import net.dankito.deepthought.data.search.specific.TagsSearch;
import net.dankito.deepthought.data.search.specific.TagsSearchResult;
import net.dankito.deepthought.data.search.specific.TagsSearchResults;
import net.dankito.deepthought.data.search.specific.FindAllEntriesHavingTheseTagsResult;

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

    Application.instantiate(new TestApplicationConfiguration("data/tests/big_data/") {
//    Application.instantiate(new estApplicationConfiguration("data/tests/big_data_3000_Entries/") {
//    Application.instantiate(new TestApplicationConfiguration("data/tests/big_data_tests/data/") {
      @Override
      public IEntityManager createEntityManager(EntityManagerConfiguration configuration) throws Exception {
        entityManager = new JavaCouchbaseLiteEntityManager(configuration);
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

    searchEngine.getEntriesForTagAsync(deepThought.EntriesWithoutTagsSystemTag(), new SearchCompletedListener<Collection<Entry>>() {
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

    searchEngine.searchTags(new TagsSearch("Zeit", new SearchCompletedListener<TagsSearchResults>() {
      @Override
      public void completed(TagsSearchResults results) {
        searchResults.addAll(results.getRelevantMatchesSorted());
        logOperationProcessTime("searchTags");
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(); } catch(Exception ex) { }
    Assert.assertEquals(857, searchResults.size());
  }

  @Test
  public void filterForMultipleTags() {
    String filter = "zeit,Geschich,hom";
    final TagsSearchResults searchResults = new TagsSearchResults(filter);
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.searchTags(new TagsSearch(filter, new SearchCompletedListener<TagsSearchResults>() { // TODO: use tags available in test database
      @Override
      public void completed(TagsSearchResults results) {
        for (TagsSearchResult result : results.getResults())
          searchResults.addSearchResult(result);
        logOperationProcessTime("filterForMultipleTags");
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(); } catch(Exception ex) { }

    Assert.assertEquals(3, searchResults.getResults().size());

    TagsSearchResult firstResult = searchResults.getResults().get(0);
    Assert.assertTrue(firstResult.hasExactMatch());
    Assert.assertEquals(857, firstResult.getAllMatchesCount());
    Assert.assertEquals("zeit", firstResult.getSearchTerm());

    TagsSearchResult secondResult = searchResults.getResults().get(1);
    Assert.assertFalse(secondResult.hasExactMatch());
    Assert.assertEquals(140, secondResult.getAllMatchesCount());
    Assert.assertEquals("geschich", secondResult.getSearchTerm());

    TagsSearchResult thirdResult = searchResults.getResults().get(2);
    Assert.assertFalse(thirdResult.hasExactMatch());
    Assert.assertEquals(133, thirdResult.getAllMatchesCount());
    Assert.assertEquals("hom", thirdResult.getSearchTerm());

    Assert.assertEquals(1130, searchResults.getAllMatches().size());
    Assert.assertEquals(274, searchResults.getRelevantMatchesSorted().size());
    Assert.assertEquals(firstResult.getAllMatchesCount() + secondResult.getAllMatchesCount() + thirdResult.getAllMatchesCount(), searchResults.getAllMatches().size());
    Assert.assertEquals(1 + secondResult.getAllMatchesCount() + thirdResult.getAllMatchesCount(), searchResults.getRelevantMatchesCount());
  }


  @Test
  public void findAllEntriesHavingTheseTags_ArchäologieOnly() {
    List<Tag> tagsToFilterFor = new ArrayList<>();
    tagsToFilterFor.add(entityManager.getEntityById(Tag.class, "1"));

    final Set<Entry> entriesHavingFilteredTags = new HashSet<>();
    final Set<Tag> tagsOnEntriesContainingFilteredTags = new HashSet<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.findAllEntriesHavingTheseTags(tagsToFilterFor, new SearchCompletedListener<FindAllEntriesHavingTheseTagsResult>() {
      @Override
      public void completed(FindAllEntriesHavingTheseTagsResult results) {
        entriesHavingFilteredTags.addAll(results.getEntriesHavingFilteredTags());
        tagsOnEntriesContainingFilteredTags.addAll(results.getTagsOnEntriesContainingFilteredTags());
        countDownLatch.countDown();
      }
    });

    try { countDownLatch.await(); } catch(Exception ex) { }

    logOperationProcessTime("findAllEntriesHavingTheseTags_ArchäologieOnly");
    Assert.assertEquals(20, entriesHavingFilteredTags.size());
    Assert.assertEquals(43, tagsOnEntriesContainingFilteredTags.size());
  }

  @Test
  public void findAllEntriesHavingTheseTags_AustralopithecusAfricanusAndLatènezeit() {
    List<Tag> tagsToFilterFor = new ArrayList<>();
    tagsToFilterFor.add(entityManager.getEntityById(Tag.class, "41"));
    tagsToFilterFor.add(entityManager.getEntityById(Tag.class, "10"));

    final Set<Entry> entriesHavingFilteredTags = new HashSet<>();
    final Set<Tag> tagsOnEntriesContainingFilteredTags = new HashSet<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.findAllEntriesHavingTheseTags(tagsToFilterFor, new SearchCompletedListener<FindAllEntriesHavingTheseTagsResult>() {
      @Override
      public void completed(FindAllEntriesHavingTheseTagsResult results) {
        entriesHavingFilteredTags.addAll(results.getEntriesHavingFilteredTags());
        tagsOnEntriesContainingFilteredTags.addAll(results.getTagsOnEntriesContainingFilteredTags());
        countDownLatch.countDown();
      }
    });

    try { countDownLatch.await(); } catch(Exception ex) { }

    logOperationProcessTime("findAllEntriesHavingTheseTags_AustralopithecusAfricanusAndLatènezeit");
    Assert.assertEquals(2, entriesHavingFilteredTags.size());
    Assert.assertEquals(7, tagsOnEntriesContainingFilteredTags.size());
  }


  @Test
  public void filterPersons() {
    final CountDownLatch countDownLatch = new CountDownLatch(1);
    final List<Person> searchResults = new ArrayList<>();

    searchEngine.searchPersons(new Search<Person>("Dürer", new SearchCompletedListener<Collection<Person>>() {
      @Override
      public void completed(Collection<Person> results) {
        logOperationProcessTime("searchPersons");
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
//    searchEngine.searchReferenceBases(new Search<ReferenceBase>("sz", new SearchCompletedListener<ReferenceBase>() {
//      @Override
//      public void completed(Collection<ReferenceBase> results) {
//        logOperationProcessTime("searchEachReferenceBaseWithSeparateSearchTerm for sz");
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
////    searchEngine.searchEachReferenceBaseWithSeparateSearchTerm(new Search<>("Angkor", new SearchCompletedListener<ReferenceBase>() {
////      @Override
////      public void completed(Collection<ReferenceBase> results) {
////        logOperationProcessTime("searchEachReferenceBaseWithSeparateSearchTerm for Angkor");
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
//    searchEngine.searchReferenceBases(new Search<ReferenceBase>("wiki,", new SearchCompletedListener<ReferenceBase>() {
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
//    searchEngine.searchReferenceBases(new Search<ReferenceBase>("iki,", new SearchCompletedListener<ReferenceBase>() {
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
//    searchEngine.searchReferenceBases(new Search<ReferenceBase>(",zeit,", new SearchCompletedListener<ReferenceBase>() {
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
//    searchEngine.searchReferenceBases(new Search<ReferenceBase>(",,nsa", new SearchCompletedListener<ReferenceBase>() {
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
//    searchEngine.searchReferenceBases(new Search<ReferenceBase>("wiki,stein,", new SearchCompletedListener<ReferenceBase>() {
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
//    searchEngine.searchReferenceBases(new Search<ReferenceBase>("sz,06,pharma", new SearchCompletedListener<ReferenceBase>() {
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

package net.deepthought.data.search;

import net.deepthought.Application;
import net.deepthought.data.TestApplicationConfiguration;
import net.deepthought.data.helper.TestDependencyResolver;
import net.deepthought.data.model.Category;
import net.deepthought.data.model.DeepThought;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.Person;
import net.deepthought.data.model.Reference;
import net.deepthought.data.model.ReferenceBase;
import net.deepthought.data.model.ReferenceSubDivision;
import net.deepthought.data.model.SeriesTitle;
import net.deepthought.data.model.Tag;
import net.deepthought.data.persistence.CombinedLazyLoadingList;
import net.deepthought.data.persistence.EntityManagerConfiguration;
import net.deepthought.data.persistence.IEntityManager;
import net.deepthought.javase.db.OrmLiteJavaSeEntityManager;

import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.WildcardQuery;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

/**
 * Created by ganymed on 17/03/15.
 */
public class LuceneSearchEngineTest {

  private final static Logger log = LoggerFactory.getLogger(LuceneSearchEngineTest.class);


  protected LuceneSearchEngine searchEngine;

  protected DeepThought deepThought;

  @Before
  public void setup() throws IOException {
//    Application.instantiate(new TestApplicationConfiguration(), new TestDependencyResolver(new MockEntityManager()) {
    Application.instantiate(new TestApplicationConfiguration(), new TestDependencyResolver() {
      @Override
      public IEntityManager createEntityManager(EntityManagerConfiguration configuration) throws Exception {
        return new OrmLiteJavaSeEntityManager(configuration);
      }

      @Override
      public ISearchEngine createSearchEngine() {
        try {
//          LuceneSearchEngineTest.this.searchEngine = new LuceneSearchEngine(new RAMDirectory());
          LuceneSearchEngineTest.this.searchEngine = new LuceneSearchEngine();
        } catch(Exception ex) {
          log.error("Could not create LuceneSearchEngine", ex);
        }
        return LuceneSearchEngineTest.this.searchEngine;
      }
    });

    deepThought = Application.getDeepThought();

//    searchEngine.deleteIndex();
    searchEngine.setIndexUpdatedEntitiesAfterMilliseconds(0);
  }

  @After
  public void tearDown() {
    searchEngine.close();
  }


  @Test
  public void addEntry_EntryGetsIndexed() {
    Entry newEntry = new Entry("Love");
    deepThought.addEntry(newEntry);

    final List<Entry> results = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.filterEntries(new FilterEntriesSearch("Love", true, false, new SearchCompletedListener<Collection<Entry>>() {
      @Override
      public void completed(Collection<Entry> result) {
        results.addAll(result);
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(newEntry, results.get(0));
  }

  @Test
  public void updateEntryContent_SearchFindsUpdatedEntry() {
    Entry newEntry = new Entry("Schaaf");
    deepThought.addEntry(newEntry);

    newEntry.setContent("Love");

    final List<Entry> results = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.filterEntries(new FilterEntriesSearch("love", true, false, new SearchCompletedListener<Collection<Entry>>() {
      @Override
      public void completed(Collection<Entry> result) {
        results.addAll(result);
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(newEntry, results.get(0));

    // ensure previous entry content cannot be found anymore
    results.clear();
    final CountDownLatch nextCountDownLatch = new CountDownLatch(1);

    searchEngine.filterEntries(new FilterEntriesSearch("schaaf", true, false, new SearchCompletedListener<Collection<Entry>>() {
      @Override
      public void completed(Collection<Entry> result) {
        results.addAll(result);
        nextCountDownLatch.countDown();
      }
    }));

    try { nextCountDownLatch.await(); } catch(Exception ex) { }

    Assert.assertEquals(0, results.size());
  }

  @Test
  public void updateEntryAbstract_SearchFindsUpdatedEntry() {
    Entry newEntry = new Entry("", "Egalité");
    deepThought.addEntry(newEntry);

    newEntry.setAbstract("Equality");

    final List<Entry> results = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.filterEntries(new FilterEntriesSearch("equ", false, true, new SearchCompletedListener<Collection<Entry>>() {
      @Override
      public void completed(Collection<Entry> result) {
        results.addAll(result);
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(newEntry, results.get(0));

    // ensure previous entry abstract cannot be found anymore
    results.clear();
    final CountDownLatch nextCountDownLatch = new CountDownLatch(1);

    searchEngine.filterEntries(new FilterEntriesSearch("ega", false, true, new SearchCompletedListener<Collection<Entry>>() {
      @Override
      public void completed(Collection<Entry> result) {
        results.addAll(result);
        nextCountDownLatch.countDown();
      }
    }));

    try { nextCountDownLatch.await(); } catch(Exception ex) { }

    Assert.assertEquals(0, results.size());
  }

  @Test
  public void updateEntryTags_SearchFindsUpdatedEntry() {
    Entry newEntry = new Entry("");
    deepThought.addEntry(newEntry);

    final Tag tag1 = new Tag("One");
    deepThought.addTag(tag1);
    final Tag tag2 = new Tag("Two");
    deepThought.addTag(tag2);

    newEntry.addTag(tag1);
    newEntry.addTag(tag2);

    newEntry.removeTag(tag1);

    List<Tag> tagTwoCollection = new ArrayList<Tag>() {{ add(tag2); }};
    List<Entry> entriesHavingTheseTags = new ArrayList<>();
    Set<Tag> notInterestedIn = new HashSet<>();

    searchEngine.findAllEntriesHavingTheseTags(tagTwoCollection, entriesHavingTheseTags, notInterestedIn);

    Assert.assertEquals(1, entriesHavingTheseTags.size());
    Assert.assertEquals(newEntry, entriesHavingTheseTags.get(0));

    // ensure tag1 cannot be found anymore
    List<Tag> tagOneCollection = new ArrayList<Tag>() {{ add(tag1); }};
    entriesHavingTheseTags.clear();

    searchEngine.findAllEntriesHavingTheseTags(tagOneCollection, entriesHavingTheseTags, notInterestedIn);

    Assert.assertEquals(0, entriesHavingTheseTags.size());
  }

  @Test
  public void updateEntryCategories_SearchFindsUpdatedEntry() {
    Entry newEntry = new Entry("");
    deepThought.addEntry(newEntry);

    final Category category1 = new Category("Quotations");
    deepThought.addCategory(category1);
    final Category category2 = new Category("Periodicals");
    deepThought.addCategory(category2);

    category1.addEntry(newEntry);
    category2.addEntry(newEntry);

    category1.removeEntry(newEntry);

    List<Category> categoryTwoCollection = new ArrayList<Category>() {{ add(category2); }};
    // TODO: no method to search for Entry Categories yet

    ScoreDoc[] hits = searchEngine.search(new TermQuery(new Term(FieldName.EntryCategories, "periodicals")));
    Assert.assertEquals(1, hits.length);
//    List<Entry> entriesHavingTheseTags = new ArrayList<>();
//    Set<Tag> notInterestedIn = new HashSet<>();
//
//    searchEngine.findAllEntriesHavingTheseTags(categoryTwoCollection, entriesHavingTheseTags, notInterestedIn);
//
//    Assert.assertEquals(1, entriesHavingTheseTags.size());
//    Assert.assertEquals(newEntry, entriesHavingTheseTags.get(0));

    // ensure category1 cannot be found anymore
    List<Category> categoryOneCollection = new ArrayList<Category>() {{ add(category1); }};
    // TODO as well
    ScoreDoc[] hits2 = searchEngine.search(new TermQuery(new Term(FieldName.EntryCategories, "quotations")));
    Assert.assertEquals(0, hits2.length);
//    entriesHavingTheseTags.clear();
//
//    searchEngine.findAllEntriesHavingTheseTags(categoryOneCollection, entriesHavingTheseTags, notInterestedIn);
//
//    Assert.assertEquals(0, entriesHavingTheseTags.size());
  }

  @Test
  public void updateEntryPersons_SearchFindsUpdatedEntry() {
    Entry newEntry = new Entry("");
    deepThought.addEntry(newEntry);

    final Person person1 = new Person("Nelson", "Mandela");
    deepThought.addPerson(person1);
    final Person person2 = new Person("Mahatma", "Gandhi");
    deepThought.addPerson(person2);

    newEntry.addPerson(person1);
    newEntry.addPerson(person2);

    newEntry.removePerson(person1);

    List<Person> personTwoCollection = new ArrayList<Person>() {{ add(person2); }};

    ScoreDoc[] hits = searchEngine.search(new WildcardQuery(new Term(FieldName.EntryPersons, "gand*")));
    Assert.assertEquals(1, hits.length);
    // TODO: no method to search for Entry Categories yet
//    List<Entry> entriesHavingTheseTags = new ArrayList<>();
//    Set<Tag> notInterestedIn = new HashSet<>();
//
//    searchEngine.findAllEntriesHavingTheseTags(personTwoCollection, entriesHavingTheseTags, notInterestedIn);
//
//    Assert.assertEquals(1, entriesHavingTheseTags.size());
//    Assert.assertEquals(newEntry, entriesHavingTheseTags.get(0));

    // ensure person1 cannot be found anymore
    ScoreDoc[] hits2 = searchEngine.search(new WildcardQuery(new Term(FieldName.EntryPersons, "mand*")));
    Assert.assertEquals(0, hits2.length);
    List<Person> personOneCollection = new ArrayList<Person>() {{ add(person1); }};
    // TODO as well
//    entriesHavingTheseTags.clear();
//
//    searchEngine.findAllEntriesHavingTheseTags(personOneCollection, entriesHavingTheseTags, notInterestedIn);
//
//    Assert.assertEquals(0, entriesHavingTheseTags.size());
  }

  @Test
  public void updateEntrySeries_SearchFindsUpdatedEntry() {
    Entry newEntry = new Entry("");
    deepThought.addEntry(newEntry);

    SeriesTitle series1 = new SeriesTitle("Urban Dictionary");
    deepThought.addSeriesTitle(series1);
    newEntry.setSeries(series1);

    SeriesTitle series2 = new SeriesTitle("Wikipedia");
    deepThought.addSeriesTitle(series2);
    newEntry.setSeries(series2);

    // TODO: now way to search for Entry's Series yet

    ScoreDoc[] hits = searchEngine.search(new WildcardQuery(new Term(FieldName.EntrySeries, "wikipedia")));
    Assert.assertEquals(1, hits.length);
//    final List<Entry> results = new ArrayList<>();
//    final CountDownLatch countDownLatch = new CountDownLatch(1);
//
//    searchEngine.filterEntries(new FilterEntriesSearch("love", true, false, new SearchCompletedListener<Entry>() {
//      @Override
//      public void completed(Collection<Entry> result) {
//        results.addAll(result);
//        countDownLatch.countDown();
//      }
//    }));
//
//    try { countDownLatch.await(); } catch(Exception ex) { }
//
//    Assert.assertEquals(1, results.size());
//    Assert.assertEquals(newEntry, results.get(0));
//
//    // ensure previous entry content cannot be found anymore
    ScoreDoc[] hits2 = searchEngine.search(new WildcardQuery(new Term(FieldName.EntrySeries, "Urban*")));
    Assert.assertEquals(0, hits2.length);
//    results.clear();
//    final CountDownLatch nextCountDownLatch = new CountDownLatch(1);
//
//    searchEngine.filterEntries(new FilterEntriesSearch("schaaf", true, false, new SearchCompletedListener<Entry>() {
//      @Override
//      public void completed(Collection<Entry> result) {
//        results.addAll(result);
//        nextCountDownLatch.countDown();
//      }
//    }));
//
//    try { nextCountDownLatch.await(); } catch(Exception ex) { }
//
//    Assert.assertEquals(0, results.size());
  }

  @Test
  public void updateEntryReference_SearchFindsUpdatedEntry() {
    Entry newEntry = new Entry("");
    deepThought.addEntry(newEntry);

    Reference reference1 = new Reference("Bible");
    deepThought.addReference(reference1);
    newEntry.setReference(reference1);

    Reference reference2 = new Reference("Koran");
    deepThought.addReference(reference2);
    newEntry.setReference(reference2);

    // TODO: now way to search for Entry's Reference yet

    ScoreDoc[] hits = searchEngine.search(new WildcardQuery(new Term(FieldName.EntryReference, "koran")));
    Assert.assertEquals(1, hits.length);
//    final List<Entry> results = new ArrayList<>();
//    final CountDownLatch countDownLatch = new CountDownLatch(1);
//
//    searchEngine.filterEntries(new FilterEntriesSearch("love", true, false, new SearchCompletedListener<Entry>() {
//      @Override
//      public void completed(Collection<Entry> result) {
//        results.addAll(result);
//        countDownLatch.countDown();
//      }
//    }));
//
//    try { countDownLatch.await(); } catch(Exception ex) { }
//
//    Assert.assertEquals(1, results.size());
//    Assert.assertEquals(newEntry, results.get(0));
//
//    // ensure previous entry content cannot be found anymore
    ScoreDoc[] hits2 = searchEngine.search(new TermQuery(new Term(FieldName.EntryReference, "bible")));
    Assert.assertEquals(0, hits2.length);
//    results.clear();
//    final CountDownLatch nextCountDownLatch = new CountDownLatch(1);
//
//    searchEngine.filterEntries(new FilterEntriesSearch("schaaf", true, false, new SearchCompletedListener<Entry>() {
//      @Override
//      public void completed(Collection<Entry> result) {
//        results.addAll(result);
//        nextCountDownLatch.countDown();
//      }
//    }));
//
//    try { nextCountDownLatch.await(); } catch(Exception ex) { }
//
//    Assert.assertEquals(0, results.size());
  }

  @Test
  public void updateEntryReferenceSubDivision_SearchFindsUpdatedEntry() {
    Entry newEntry = new Entry("");
    deepThought.addEntry(newEntry);

    ReferenceSubDivision subDivision1 = new ReferenceSubDivision("Hate");
    deepThought.addReferenceSubDivision(subDivision1);
    newEntry.setReferenceSubDivision(subDivision1);

    ReferenceSubDivision subDivision2 = new ReferenceSubDivision("Love");
    deepThought.addReferenceSubDivision(subDivision2);
    newEntry.setReferenceSubDivision(subDivision2);

    // TODO: now way to search for Entry's Reference yet

    ScoreDoc[] hits = searchEngine.search(new WildcardQuery(new Term(FieldName.EntryReferenceSubDivision, "love")));
    Assert.assertEquals(1, hits.length);
//    final List<Entry> results = new ArrayList<>();
//    final CountDownLatch countDownLatch = new CountDownLatch(1);
//
//    searchEngine.filterEntries(new FilterEntriesSearch("love", true, false, new SearchCompletedListener<Entry>() {
//      @Override
//      public void completed(Collection<Entry> result) {
//        results.addAll(result);
//        countDownLatch.countDown();
//      }
//    }));
//
//    try { countDownLatch.await(); } catch(Exception ex) { }
//
//    Assert.assertEquals(1, results.size());
//    Assert.assertEquals(newEntry, results.get(0));
//
//    // ensure previous entry content cannot be found anymore
    ScoreDoc[] hits2 = searchEngine.search(new TermQuery(new Term(FieldName.EntryReferenceSubDivision, "hate")));
    Assert.assertEquals(0, hits2.length);
//    results.clear();
//    final CountDownLatch nextCountDownLatch = new CountDownLatch(1);
//
//    searchEngine.filterEntries(new FilterEntriesSearch("schaaf", true, false, new SearchCompletedListener<Entry>() {
//      @Override
//      public void completed(Collection<Entry> result) {
//        results.addAll(result);
//        nextCountDownLatch.countDown();
//      }
//    }));
//
//    try { nextCountDownLatch.await(); } catch(Exception ex) { }
//
//    Assert.assertEquals(0, results.size());
  }

  @Test
  public void deleteEntry_SearchDoesNotFindEntryAnymore() {
    Entry newEntry = new Entry("Love");
    deepThought.addEntry(newEntry);

    deepThought.removeEntry(newEntry);

    final List<Entry> results = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.filterEntries(new FilterEntriesSearch("love", true, false, new SearchCompletedListener<Collection<Entry>>() {
      @Override
      public void completed(Collection<Entry> result) {
        results.addAll(result);
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(); } catch(Exception ex) { }

    Assert.assertEquals(0, results.size());
  }

  @Test
  public void searchOnlyForEntriesWithoutTags_OnlyEntryWithoutTagsGetsFound() {
    Entry entryWithTag = new Entry("Love");
    Entry entryWithoutTag = new Entry("Love"); // the both have the same content
    deepThought.addEntry(entryWithTag);
    deepThought.addEntry(entryWithoutTag);

    Tag tag = new Tag("tag");
    deepThought.addTag(tag);
    entryWithTag.addTag(tag);

    final List<Entry> results = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.filterEntries(new FilterEntriesSearch("love", true, false, true, new SearchCompletedListener<Collection<Entry>>() {
      @Override
      public void completed(Collection<Entry> result) {
        results.addAll(result);
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size()); // only that one without Tags is found
    Assert.assertEquals(entryWithoutTag, results.get(0));
  }

  @Test
  public void filterEntriesSpecifyWhichEntriesToFilter_OnlySpecifiedEntriesGetFound() {
    Entry entry1 = new Entry("Love");
    Entry entry2 = new Entry("Love");
    Entry entry3 = new Entry("Love");
    deepThought.addEntry(entry1);
    deepThought.addEntry(entry2);
    deepThought.addEntry(entry3);

    Collection<Entry> entriesToFilter = Arrays.asList(new Entry[] { entry1, entry3 });

    final List<Entry> results = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.filterEntries(new FilterEntriesSearch("love", true, false, entriesToFilter, new SearchCompletedListener<Collection<Entry>>() {
      @Override
      public void completed(Collection<Entry> result) {
        results.addAll(result);
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(); } catch(Exception ex) { }

    Assert.assertEquals(2, results.size()); // only that one without Tags is found
    Assert.assertTrue(results.contains(entry1));
    Assert.assertTrue(results.contains(entry3));
    Assert.assertFalse(results.contains(entry2));
  }


  @Test
  public void addTag_TagGetsIndexed() {
    Tag newTag = new Tag("tag");
    deepThought.addTag(newTag);

    final List<Tag> results = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.filterTags(new FilterTagsSearch("Tag", new SearchCompletedListener<FilterTagsSearchResults>() {
      @Override
      public void completed(FilterTagsSearchResults result) {
        results.addAll(result.getAllMatches());
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(newTag, results.get(0));
  }

  @Test
  public void updateTagName_SearchFindsUpdatedTag() {
    Tag newTag = new Tag("tag");
    deepThought.addTag(newTag);

    newTag.setName("Swag");

    final List<Tag> results = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.filterTags(new FilterTagsSearch("swag", new SearchCompletedListener<FilterTagsSearchResults>() {
      @Override
      public void completed(FilterTagsSearchResults result) {
        results.addAll(result.getAllMatches());
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(newTag, results.get(0));

    // ensure previous tag name cannot be found anymore
    results.clear();
    final CountDownLatch nextCountDownLatch = new CountDownLatch(1);

    searchEngine.filterTags(new FilterTagsSearch("tag", new SearchCompletedListener<FilterTagsSearchResults>() {
      @Override
      public void completed(FilterTagsSearchResults result) {
        results.addAll(result.getAllMatches());
        nextCountDownLatch.countDown();
      }
    }));

    try { nextCountDownLatch.await(); } catch(Exception ex) { }

    Assert.assertEquals(0, results.size());
  }

  @Test
  public void deleteTag_SearchDoesNotFindTagAnymore() {
    Tag newTag = new Tag("tag");
    deepThought.addTag(newTag);

    deepThought.removeTag(newTag);

    final List<Tag> results = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.filterTags(new FilterTagsSearch("Tag", new SearchCompletedListener<FilterTagsSearchResults>() {
      @Override
      public void completed(FilterTagsSearchResults result) {
        results.addAll(result.getAllMatches());
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(); } catch(Exception ex) { }

    Assert.assertEquals(0, results.size());
  }


//  @Test
//  public void addCategory_CategoryGetsIndexed() {
//    Category newCategory = new Category("category");
//    deepThought.addCategory(newCategory);
//
//    final List<Category> results = new ArrayList<>();
//    final CountDownLatch countDownLatch = new CountDownLatch(1);
//
//    searchEngine.filterCategories(new Search<Category>("Category", result -> {
//      results.addAll(result);
//      countDownLatch.countDown();
//    }));
//
//    try { countDownLatch.await(); } catch(Exception ex) { }
//
//    Assert.assertEquals(1, results.size());
//  }
//
//  @Test
//  public void updateCategory_SearchFindsUpdatedCategory() {
//    Tag newTag = new Tag("tag");
//    deepThought.addTag(newTag);
//
//    newTag.setName("Swag");
//
//    final List<Tag> results = new ArrayList<>();
//    final CountDownLatch countDownLatch = new CountDownLatch(1);
//
//    searchEngine.filterTags(new Search<Tag>("swag", result -> {
//      results.addAll(result);
//      countDownLatch.countDown();
//    }));
//
//    try { countDownLatch.await(); } catch(Exception ex) { }
//
//    Assert.assertEquals(1, results.size());
//
//    // ensure previous tag name cannot be found anymore
//    results.clear();
//    final CountDownLatch nextCountDownLatch = new CountDownLatch(1);
//
//    searchEngine.filterTags(new Search<Tag>("tag", result -> {
//      results.addAll(result);
//      nextCountDownLatch.countDown();
//    }));
//
//    try { nextCountDownLatch.await(); } catch(Exception ex) { }
//
//    Assert.assertEquals(0, results.size());
//  }


  @Test
  public void addPerson_PersonGetsIndexed() {
    Person newPerson = new Person("first", "last");
    deepThought.addPerson(newPerson);

    final List<Person> results = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.filterPersons(new Search<Person>("last", new SearchCompletedListener<Collection<Person>>() {
      @Override
      public void completed(Collection<Person> result) {
        results.addAll(result);
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(newPerson, results.get(0));
  }

  @Test
  public void updatePersonLastName_SearchFindsUpdatedPerson() {
    Person newPerson = new Person("", "last");
    deepThought.addPerson(newPerson);

    newPerson.setLastName("Zeus");

    final List<Person> results = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.filterPersons(new Search<Person>("zeus", new SearchCompletedListener<Collection<Person>>() {
      @Override
      public void completed(Collection<Person> result) {
        results.addAll(result);
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(newPerson, results.get(0));

    // ensure previous tag name cannot be found anymore
    results.clear();
    final CountDownLatch nextCountDownLatch = new CountDownLatch(1);

    searchEngine.filterPersons(new Search<Person>("last", new SearchCompletedListener<Collection<Person>>() {
      @Override
      public void completed(Collection<Person> result) {
        results.addAll(result);
        nextCountDownLatch.countDown();
      }
    }));

    try { nextCountDownLatch.await(); } catch(Exception ex) { }

    Assert.assertEquals(0, results.size());
  }

  @Test
  public void updatePersonFirstName_SearchFindsUpdatedPerson() {
    Person newPerson = new Person("first", "");
    deepThought.addPerson(newPerson);

    newPerson.setFirstName("Giovanni");

    final List<Person> results = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.filterPersons(new Search<Person>("giovanni", new SearchCompletedListener<Collection<Person>>() {
      @Override
      public void completed(Collection<Person> result) {
        results.addAll(result);
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(newPerson, results.get(0));

    // ensure previous tag name cannot be found anymore
    results.clear();
    final CountDownLatch nextCountDownLatch = new CountDownLatch(1);

    searchEngine.filterPersons(new Search<Person>("first", new SearchCompletedListener<Collection<Person>>() {
      @Override
      public void completed(Collection<Person> result) {
        results.addAll(result);
        nextCountDownLatch.countDown();
      }
    }));

    try { nextCountDownLatch.await(); } catch(Exception ex) { }

    Assert.assertEquals(0, results.size());
  }

  @Test
  public void filterPersonsForFirstAndLastName() {
    Person newPerson = new Person("Mahatma", "Gandhi");
    deepThought.addPerson(newPerson);
    deepThought.addPerson(new Person("dummy", ""));
    deepThought.addPerson(new Person("Arnold", "Schwarznegger"));

    final List<Person> results = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.filterPersons(new Search<Person>("gan, mah", new SearchCompletedListener<Collection<Person>>() {
      @Override
      public void completed(Collection<Person> result) {
        results.addAll(result);
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(newPerson, results.get(0));

    // ensure Person is not found if First name is wrong
    results.clear();
    final CountDownLatch nextCountDownLatch = new CountDownLatch(1);

    searchEngine.filterPersons(new Search<Person>("Gandhi, Mohandas", new SearchCompletedListener<Collection<Person>>() {
      @Override
      public void completed(Collection<Person> result) {
        results.addAll(result);
        nextCountDownLatch.countDown();
      }
    }));

    try { nextCountDownLatch.await(); } catch(Exception ex) { }

    Assert.assertEquals(0, results.size());
  }

  @Test
  public void deletePerson_SearchDoesNotFindPersonAnymore() {
    Person newPerson = new Person("first", "last");
    deepThought.addPerson(newPerson);

    deepThought.removePerson(newPerson);

    final List<Person> results = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.filterPersons(new Search<Person>("first", new SearchCompletedListener<Collection<Person>>() {
      @Override
      public void completed(Collection<Person> result) {
        results.addAll(result);
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(); } catch(Exception ex) { }

    Assert.assertEquals(0, results.size());
  }


  @Test
  public void addSeriesTitle_SeriesTitleGetsIndexed() {
    SeriesTitle newSeriesTitle = new SeriesTitle("series");
    deepThought.addSeriesTitle(newSeriesTitle);

    final List<SeriesTitle> results = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.filterReferenceBases(new Search<SeriesTitle>("Series", new SearchCompletedListener<Collection<SeriesTitle>>() {
      @Override
      public void completed(Collection<SeriesTitle> result) {
        results.addAll(result);
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(newSeriesTitle, results.get(0));
  }

  @Test
  public void updateSeriesTitleTitle_SearchFindsUpdatedSeriesTitle() {
    SeriesTitle newSeriesTitle = new SeriesTitle("series");
    deepThought.addSeriesTitle(newSeriesTitle);

    newSeriesTitle.setTitle("Aphrodite");

    final List<ReferenceBase> results = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.filterReferenceBases(new Search<ReferenceBase>("aphrodite", new SearchCompletedListener<Collection<ReferenceBase>>() {
      @Override
      public void completed(Collection<ReferenceBase> result) {
        results.addAll(result);
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(newSeriesTitle, results.get(0));

    // ensure previous tag name cannot be found anymore
    results.clear();
    final CountDownLatch nextCountDownLatch = new CountDownLatch(1);

    searchEngine.filterReferenceBases(new Search<ReferenceBase>("series", new SearchCompletedListener<Collection<ReferenceBase>>() {
      @Override
      public void completed(Collection<ReferenceBase> result) {
        results.addAll(result);
        nextCountDownLatch.countDown();
      }
    }));

    try { nextCountDownLatch.await(); } catch(Exception ex) { }

    Assert.assertEquals(0, results.size());
  }

  @Test
  public void updateSeriesTitleSubTitle_SearchFindsUpdatedSeriesTitle() {
    SeriesTitle newSeriesTitle = new SeriesTitle("series", "don't find me");
    deepThought.addSeriesTitle(newSeriesTitle);

    newSeriesTitle.setSubTitle("subtitle");

    final List<ReferenceBase> results = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.filterReferenceBases(new Search<ReferenceBase>("subtitle", new SearchCompletedListener<Collection<ReferenceBase>>() {
      @Override
      public void completed(Collection<ReferenceBase> result) {
        results.addAll(result);
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(newSeriesTitle, results.get(0));

    // ensure previous tag name cannot be found anymore
    results.clear();
    final CountDownLatch nextCountDownLatch = new CountDownLatch(1);

    searchEngine.filterReferenceBases(new Search<ReferenceBase>("find", new SearchCompletedListener<Collection<ReferenceBase>>() {
      @Override
      public void completed(Collection<ReferenceBase> result) {
        results.addAll(result);
        nextCountDownLatch.countDown();
      }
    }));

    try { nextCountDownLatch.await(); } catch(Exception ex) { }

    Assert.assertEquals(0, results.size());
  }

  @Test
  public void filterSeriesTitles() {
    SeriesTitle newSeriesTitle = new SeriesTitle("SZ", "Ich brauch irgend einen Untertitel");
    deepThought.addSeriesTitle(newSeriesTitle);
    deepThought.addSeriesTitle(new SeriesTitle("dummy"));
    deepThought.addSeriesTitle(new SeriesTitle("Bild 'Zeitung'"));

    final List<ReferenceBase> results = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.filterReferenceBases(new Search<ReferenceBase>("sz", new SearchCompletedListener<Collection<ReferenceBase>>() {
      @Override
      public void completed(Collection<ReferenceBase> result) {
        results.addAll(result);
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(newSeriesTitle, results.get(0));


    results.clear();
    final CountDownLatch countDownLatch2 = new CountDownLatch(1);

    searchEngine.filterReferenceBases(new Search<ReferenceBase>("untert", new SearchCompletedListener<Collection<ReferenceBase>>() {
      @Override
      public void completed(Collection<ReferenceBase> result) {
        results.addAll(result);
        countDownLatch2.countDown();
      }
    }));

    try { countDownLatch2.await(); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(newSeriesTitle, results.get(0));


    results.clear();
    final CountDownLatch countDownLatch3 = new CountDownLatch(1);

    searchEngine.filterReferenceBases(new Search<ReferenceBase>("unterd", new SearchCompletedListener<Collection<ReferenceBase>>() {
      @Override
      public void completed(Collection<ReferenceBase> result) {
        results.addAll(result);
        countDownLatch3.countDown();
      }
    }));

    try { countDownLatch3.await(); } catch(Exception ex) { }

    Assert.assertEquals(0, results.size());
  }


  @Test
  public void addReference_ReferenceGetsIndexed() {
    Reference newReference = new Reference("reference");
    deepThought.addReference(newReference);

    final List<Reference> results = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.filterReferenceBases(new Search<Reference>("Reference", new SearchCompletedListener<Collection<Reference>>() {
      @Override
      public void completed(Collection<Reference> result) {
        results.addAll(result);
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(newReference, results.get(0));
  }

  @Test
  public void updateReferenceTitle_SearchFindsUpdatedReference() {
    Reference newReference = new Reference("Hephaistos");
    deepThought.addReference(newReference);

    newReference.setTitle("Aphrodite");

    final List<Reference> results = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.filterReferenceBases(new Search<Reference>("aphrodite", new SearchCompletedListener<Collection<Reference>>() {
      @Override
      public void completed(Collection<Reference> result) {
        results.addAll(result);
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(newReference, results.get(0));

    // ensure previous reference title cannot be found anymore
    results.clear();
    final CountDownLatch nextCountDownLatch = new CountDownLatch(1);

    searchEngine.filterReferenceBases(new Search<Reference>("Hephaistos", new SearchCompletedListener<Collection<Reference>>() {
      @Override
      public void completed(Collection<Reference> result) {
        results.addAll(result);
        nextCountDownLatch.countDown();
      }
    }));

    try { nextCountDownLatch.await(); } catch(Exception ex) { }

    Assert.assertEquals(0, results.size());
  }

  @Test
  public void updateReferenceSubTitle_SearchFindsUpdatedReference() {
    Reference newReference = new Reference("reference", "don't find me");
    deepThought.addReference(newReference);

    newReference.setSubTitle("Hephaistos");

    final List<Reference> results = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.filterReferenceBases(new Search<Reference>("heph", new SearchCompletedListener<Collection<Reference>>() {
      @Override
      public void completed(Collection<Reference> result) {
        results.addAll(result);
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(newReference, results.get(0));

    // ensure previous tag name cannot be found anymore
    results.clear();
    final CountDownLatch nextCountDownLatch = new CountDownLatch(1);

    searchEngine.filterReferenceBases(new Search<Reference>("find", new SearchCompletedListener<Collection<Reference>>() {
      @Override
      public void completed(Collection<Reference> result) {
        results.addAll(result);
        nextCountDownLatch.countDown();
      }
    }));

    try { nextCountDownLatch.await(); } catch(Exception ex) { }

    Assert.assertEquals(0, results.size());
  }

  @Test
  public void updateReferenceIssueOrPublishingDate_SearchFindsUpdatedReference() {
    Reference newReference = new Reference("reference");
    deepThought.addReference(newReference);

    newReference.setIssueOrPublishingDate("03/2010");

    final List<Reference> results = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.filterReferenceBases(new Search<Reference>("2010", new SearchCompletedListener<Collection<Reference>>() {
      @Override
      public void completed(Collection<Reference> result) {
        results.addAll(result);
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(newReference, results.get(0));
  }

  @Test
  public void filterReferences() {
    Reference newReference = new Reference("Selbst Denken", "Anleitung zum Widerstand");
    newReference.setIssueOrPublishingDate("2012");
    deepThought.addReference(newReference);
    deepThought.addReference(new Reference("Befreiung vom Überfluss", "Auf dem Weg in die Postwachstumsökonomie"));
    deepThought.addReference(new Reference("Schulden", "Die ersten 5000 Jahre"));

    final List<Reference> results = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.filterReferenceBases(new Search<Reference>("denk", new SearchCompletedListener<Collection<Reference>>() {
      @Override
      public void completed(Collection<Reference> result) {
        results.addAll(result);
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(newReference, results.get(0));


    results.clear();
    final CountDownLatch countDownLatch2 = new CountDownLatch(1);

    searchEngine.filterReferenceBases(new Search<Reference>("wider", new SearchCompletedListener<Collection<Reference>>() {
      @Override
      public void completed(Collection<Reference> result) {
        results.addAll(result);
        countDownLatch2.countDown();
      }
    }));

    try { countDownLatch2.await(); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(newReference, results.get(0));


    results.clear();
    final CountDownLatch countDownLatch3 = new CountDownLatch(1);

    searchEngine.filterReferenceBases(new Search<Reference>("2012", new SearchCompletedListener<Collection<Reference>>() {
      @Override
      public void completed(Collection<Reference> result) {
        results.addAll(result);
        countDownLatch3.countDown();
      }
    }));

    try { countDownLatch3.await(); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(newReference, results.get(0));


    results.clear();
    final CountDownLatch countDownLatch4 = new CountDownLatch(1);

    searchEngine.filterReferenceBases(new Search<Reference>("Liebe", new SearchCompletedListener<Collection<Reference>>() {
      @Override
      public void completed(Collection<Reference> result) {
        results.addAll(result);
        countDownLatch4.countDown();
      }
    }));

    try { countDownLatch4.await(); } catch(Exception ex) { }

    Assert.assertEquals(0, results.size());
  }


  @Test
  public void addReferenceSubDivision_ReferenceSubDivisionGetsIndexed() {
    Reference newReference = new Reference("reference");
    deepThought.addReference(newReference);

    ReferenceSubDivision newSubDivision = new ReferenceSubDivision("subDivision");
    newReference.addSubDivision(newSubDivision);

    final List<ReferenceBase> results = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.filterReferenceBases(new Search<ReferenceBase>("SUB", new SearchCompletedListener<Collection<ReferenceBase>>() {
      @Override
      public void completed(Collection<ReferenceBase> result) {
        results.addAll(result);
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(newSubDivision, results.get(0));
  }

  @Test
  public void updateReferenceSubDivisionTitle_SearchFindsUpdatedReferenceSubDivision() {
    Reference newReference = new Reference("reference");
    deepThought.addReference(newReference);

    ReferenceSubDivision newSubDivision = new ReferenceSubDivision("subDivision");
    newReference.addSubDivision(newSubDivision);
    newSubDivision.setTitle("Aphrodite");

    final List<ReferenceBase> results = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.filterReferenceBases(new Search<ReferenceBase>("aphrodite", new SearchCompletedListener<Collection<ReferenceBase>>() {
      @Override
      public void completed(Collection<ReferenceBase> result) {
        results.addAll(result);
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(newSubDivision, results.get(0));

    // ensure previous tag name cannot be found anymore
    results.clear();
    final CountDownLatch nextCountDownLatch = new CountDownLatch(1);

    searchEngine.filterReferenceBases(new Search<Reference>("subDivision", new SearchCompletedListener<Collection<Reference>>() {
      @Override
      public void completed(Collection<Reference> result) {
        results.addAll(result);
        nextCountDownLatch.countDown();
      }
    }));

    try { nextCountDownLatch.await(); } catch(Exception ex) { }

    Assert.assertEquals(0, results.size());
  }

  @Test
  public void updateReferenceSubDivisionSubTitle_SearchFindsUpdatedReferenceSubDivision() {
    Reference newReference = new Reference("reference");
    deepThought.addReference(newReference);

    ReferenceSubDivision newSubDivision = new ReferenceSubDivision("subDivision", "don't find me");
    deepThought.addReferenceSubDivision(newSubDivision);
    newReference.addSubDivision(newSubDivision);
    newSubDivision.setSubTitle("Hephaistos");

    final List<ReferenceBase> results = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.filterReferenceBases(new Search<ReferenceBase>("heph", new SearchCompletedListener<Collection<ReferenceBase>>() {
      @Override
      public void completed(Collection<ReferenceBase> result) {
        results.addAll(result);
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(newSubDivision, results.get(0));

    // ensure previous tag name cannot be found anymore
    results.clear();
    final CountDownLatch nextCountDownLatch = new CountDownLatch(1);

    searchEngine.filterReferenceBases(new Search<Reference>("find", new SearchCompletedListener<Collection<Reference>>() {
      @Override
      public void completed(Collection<Reference> result) {
        results.addAll(result);
        nextCountDownLatch.countDown();
      }
    }));

    try { nextCountDownLatch.await(); } catch(Exception ex) { }

    Assert.assertEquals(0, results.size());
  }

  @Test
  public void filterReferenceSubDivisions() {
    Reference newReference = new Reference("SZ");
    deepThought.addReference(newReference);

    ReferenceSubDivision newSubDivision = new ReferenceSubDivision("Die Pharmaindustrie ist schlimmer als die Mafia", "Kritik an Arzneimittelherstellern");
    newReference.addSubDivision(newSubDivision);
    newReference.addSubDivision(new ReferenceSubDivision("BND versucht NSA-Aufklärer in die Falle zu locken"));
    newReference.addSubDivision(new ReferenceSubDivision("Privatsphäre kostet extra"));

    final List<Reference> results = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.filterReferenceBases(new Search<Reference>("phar", new SearchCompletedListener<Collection<Reference>>() {
      @Override
      public void completed(Collection<Reference> result) {
        results.addAll(result);
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(newSubDivision, results.get(0));


    results.clear();
    final CountDownLatch countDownLatch2 = new CountDownLatch(1);

    searchEngine.filterReferenceBases(new Search<Reference>("krit", new SearchCompletedListener<Collection<Reference>>() {
      @Override
      public void completed(Collection<Reference> result) {
        results.addAll(result);
        countDownLatch2.countDown();
      }
    }));

    try { countDownLatch2.await(); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(newSubDivision, results.get(0));

    results.clear();
    final CountDownLatch countDownLatch3 = new CountDownLatch(1);

    searchEngine.filterReferenceBases(new Search<Reference>("Liebe", new SearchCompletedListener<Collection<Reference>>() {
      @Override
      public void completed(Collection<Reference> result) {
        results.addAll(result);
        countDownLatch3.countDown();
      }
    }));

    try { countDownLatch3.await(); } catch(Exception ex) { }

    Assert.assertEquals(0, results.size());
  }


//  @Test
//  public void testSearchingInLongTexts() throws IOException, ParseException {
//    OpenOfficeDocumentsImporterExporter importer = new OpenOfficeDocumentsImporterExporter();
//    String documentContent = importer.extractPlainTextFromTextDocument
//        ("/run/media/ganymed/fast_data/coding/Android/self/DeepThought/libs/importer_exporter/OpenOfficeDocumentsImporterExporter/src/test/resources/Schneisen im Wald 4.odt");
//    searchEngine.index(documentContent);
//
//    searchEngine.search("Metallbörse", Entry.class);
//    searchEngine.search("leben", Entry.class);
//    searchEngine.search("run", Entry.class);
//
//    List<IndexTerm> terms = searchEngine.getAllTerms();
//  }

//  @Test
//  public void testSearchingInIndexWithManyDocuments() throws IOException, ParseException {
//    OpenOfficeDocumentsImporterExporter importer = new OpenOfficeDocumentsImporterExporter();
//    List<Entry> entries = importer.extractEntriesFromDankitosSchneisenImWald("/run/media/ganymed/fast_data/coding/Android/self/DeepThought/libs/importer_exporter/OpenOfficeDocumentsImporterExporter/src/test/resources/Schneisen im Wald 4.odt");
//    for(Entry entry : entries)
//      searchEngine.indexEntity(entry);
//
//    searchEngine.search("Metallbörse", Entry.class);
//    searchEngine.search("Überwachungsstaat", Entry.class);
//    searchEngine.search("Überwachung", Entry.class);
//    searchEngine.search("sucher", Entry.class);
//    searchEngine.search("liebe", Entry.class);
//
//    List<IndexTerm> terms = searchEngine.getAllTerms();
//  }


  @Test
  public void findAllEntriesHavingTheseTags() throws IOException, ParseException {
    Tag tag1 = new Tag("tag1");
    Tag tag2 = new Tag("tag2");
    Tag tag3 = new Tag("tag3");
    deepThought.addTag(tag1);
    deepThought.addTag(tag2);
    deepThought.addTag(tag3);

    Entry entryWithTags1 = new Entry("entry 1");
    Entry entryWithTags2 = new Entry("entry 2");
    deepThought.addEntry(entryWithTags1);
    deepThought.addEntry(entryWithTags2);

    entryWithTags1.addTag(tag1);
    entryWithTags1.addTag(tag2);
    entryWithTags2.addTag(tag2);
    entryWithTags2.addTag(tag3);

//    searchEngine.rebuildIndex();


    List<Tag> tagsToFilterFor = new ArrayList<>();
    tagsToFilterFor.add(tag1);
    tagsToFilterFor.add(tag2);

    Set<Entry> entriesHavingFilteredTags = new HashSet<>();
    final Set<Tag> tagsOnEntriesContainingFilteredTags = new HashSet<>();

    searchEngine.findAllEntriesHavingTheseTags(tagsToFilterFor, entriesHavingFilteredTags, tagsOnEntriesContainingFilteredTags);
    Assert.assertEquals(1, entriesHavingFilteredTags.size());
    Assert.assertEquals(entryWithTags1, new ArrayList<Entry>(entriesHavingFilteredTags).get(0));
    Assert.assertEquals(2, tagsOnEntriesContainingFilteredTags.size());

    tagsToFilterFor.clear();
    tagsToFilterFor.add(tag2);
    tagsToFilterFor.add(tag3);
    entriesHavingFilteredTags.clear();
    tagsOnEntriesContainingFilteredTags.clear();

    searchEngine.findAllEntriesHavingTheseTags(tagsToFilterFor, entriesHavingFilteredTags, tagsOnEntriesContainingFilteredTags);
    Assert.assertEquals(1, entriesHavingFilteredTags.size());
    Assert.assertEquals(entryWithTags2, new ArrayList<Entry>(entriesHavingFilteredTags).get(0));
    Assert.assertEquals(2, tagsOnEntriesContainingFilteredTags.size());
  }


  @Test
  public void findAllEntriesWithoutTags() throws IOException, ParseException {
    Tag tag1 = new Tag("tag1");
    Tag tag2 = new Tag("tag2");
    Tag tag3 = new Tag("tag3");
    deepThought.addTag(tag1);
    deepThought.addTag(tag2);
    deepThought.addTag(tag3);

    Entry entryWithoutTags1 = new Entry("withoutOne");
    Entry entryWithoutTags2 = new Entry("withoutTwo");
    Entry entryWithoutTags3 = new Entry("withoutThree");
    deepThought.addEntry(entryWithoutTags1);
    deepThought.addEntry(entryWithoutTags2);
    deepThought.addEntry(entryWithoutTags3);

    Entry entryWithTags1 = new Entry("withOne");
    Entry entryWithTags2 = new Entry("withTwo");
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


  @Test
  public void findEntriesWithTags() throws IOException, ParseException {
    Tag tag1 = new Tag("tag1");
    Tag tag2 = new Tag("tag2");
    Tag tag3 = new Tag("tag3");
    deepThought.addTag(tag1);
    deepThought.addTag(tag2);
    deepThought.addTag(tag3);

    Entry entryWithoutTags1 = new Entry("withoutOne");
    Entry entryWithoutTags2 = new Entry("withoutTwo");
    Entry entryWithoutTags3 = new Entry("withoutThree");
    deepThought.addEntry(entryWithoutTags1);
    deepThought.addEntry(entryWithoutTags2);
    deepThought.addEntry(entryWithoutTags3);

    Entry entryWithTags1 = new Entry("withOne");
    Entry entryWithTags2 = new Entry("withTwo");
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

    List<Entry> entriesWithTag1 = new ArrayList<>();
    Set<Tag> tagsOnEntriesWithTag1 = new HashSet<>();
    searchEngine.findAllEntriesHavingTheseTags(Arrays.asList(tag1), entriesWithTag1, tagsOnEntriesWithTag1);
    Assert.assertEquals(1, entriesWithTag1.size());

    Assert.assertTrue(entriesWithTag1.contains(entryWithTags1));
    Assert.assertFalse(entriesWithTag1.contains(entryWithTags2));
    Assert.assertFalse(entriesWithTag1.contains(entryWithoutTags1));
    Assert.assertFalse(entriesWithTag1.contains(entryWithoutTags2));
    Assert.assertFalse(entriesWithTag1.contains(entryWithoutTags3));

    Collection<Entry> entriesWithTag2 = new ArrayList<>();
    Set<Tag> tagsOnEntriesWithTag2 = new HashSet<>();
    searchEngine.findAllEntriesHavingTheseTags(Arrays.asList(tag2), entriesWithTag2, tagsOnEntriesWithTag2);
    Assert.assertEquals(2, entriesWithTag2.size());

    Assert.assertTrue(entriesWithTag2.contains(entryWithTags1));
    Assert.assertTrue(entriesWithTag2.contains(entryWithTags2));
    Assert.assertFalse(entriesWithTag2.contains(entryWithoutTags1));
    Assert.assertFalse(entriesWithTag2.contains(entryWithoutTags2));
    Assert.assertFalse(entriesWithTag2.contains(entryWithoutTags3));

    Collection<Entry> entriesWithTag3 = new ArrayList<>();
    Set<Tag> tagsOnEntriesWithTag3 = new HashSet<>();
    searchEngine.findAllEntriesHavingTheseTags(Arrays.asList(tag3), entriesWithTag3, tagsOnEntriesWithTag3);
    Assert.assertEquals(1, entriesWithTag3.size());

    Assert.assertFalse(entriesWithTag3.contains(entryWithTags1));
    Assert.assertTrue(entriesWithTag3.contains(entryWithTags2));
    Assert.assertFalse(entriesWithTag3.contains(entryWithoutTags1));
    Assert.assertFalse(entriesWithTag3.contains(entryWithoutTags2));
    Assert.assertFalse(entriesWithTag3.contains(entryWithoutTags3));

    Collection<Entry> entriesWithTags2And3 = new ArrayList<>();
    Set<Tag> tagsOnEntriesWithTag2And3 = new HashSet<>();
    searchEngine.findAllEntriesHavingTheseTags(Arrays.asList(tag2, tag3), entriesWithTags2And3, tagsOnEntriesWithTag2And3);
    Assert.assertEquals(1, entriesWithTags2And3.size());

    Assert.assertFalse(entriesWithTags2And3.contains(entryWithTags1));
    Assert.assertTrue(entriesWithTags2And3.contains(entryWithTags2));
    Assert.assertFalse(entriesWithTags2And3.contains(entryWithoutTags1));
    Assert.assertFalse(entriesWithTags2And3.contains(entryWithoutTags2));
    Assert.assertFalse(entriesWithTags2And3.contains(entryWithoutTags3));

  }

  @Test
  public void filterTestReferences() {
    final CombinedLazyLoadingList<ReferenceBase> results = new CombinedLazyLoadingList<ReferenceBase>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.filterAllReferenceBaseTypesForSameFilter(new Search<ReferenceBase>("wikip", new SearchCompletedListener<Collection<ReferenceBase>>() {
      @Override
      public void completed(Collection<ReferenceBase> result) {
        results.setUnderlyingCollection(result);
        countDownLatch.countDown();
      }
    }), "wikip");

    try { countDownLatch.await(); } catch(Exception ex) { }

    Assert.assertEquals(5, results.size());


    results.clear();
    final CountDownLatch countDownLatch2 = new CountDownLatch(1);

    searchEngine.filterAllReferenceBaseTypesForSameFilter(new Search<ReferenceBase>("sz", new SearchCompletedListener<Collection<ReferenceBase>>() {
      @Override
      public void completed(Collection<ReferenceBase> result) {
        results.setUnderlyingCollection(result);
        countDownLatch.countDown();
      }
    }), "sz");

    try { countDownLatch2.await(); } catch(Exception ex) { }

    Assert.assertEquals(468, results.size());


//    results.clear();
//    final CountDownLatch countDownLatch3 = new CountDownLatch(1);
//
//    searchEngine.filterReferenceBases(new Search<Reference>("2012", new SearchCompletedListener<Reference>() {
//      @Override
//      public void completed(Collection<Reference> result) {
//        results.addAll(result);
//        countDownLatch3.countDown();
//      }
//    }));
//
//    try { countDownLatch3.await(); } catch(Exception ex) { }
//
//    Assert.assertEquals(1, results.size());
//    Assert.assertEquals(newReference, results.get(0));
//
//
//    results.clear();
//    final CountDownLatch countDownLatch4 = new CountDownLatch(1);
//
//    searchEngine.filterReferenceBases(new Search<Reference>("Liebe", new SearchCompletedListener<Reference>() {
//      @Override
//      public void completed(Collection<Reference> result) {
//        results.addAll(result);
//        countDownLatch4.countDown();
//      }
//    }));
//
//    try { countDownLatch4.await(); } catch(Exception ex) { }
//
//    Assert.assertEquals(0, results.size());
  }

}

package net.dankito.deepthought.data.search;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.TestApplicationConfiguration;
import net.dankito.deepthought.data.model.Category;
import net.dankito.deepthought.data.model.DeepThought;
import net.dankito.deepthought.data.model.Entry;
import net.dankito.deepthought.data.model.FileLink;
import net.dankito.deepthought.data.model.Person;
import net.dankito.deepthought.data.model.Reference;
import net.dankito.deepthought.data.model.ReferenceBase;
import net.dankito.deepthought.data.model.ReferenceSubDivision;
import net.dankito.deepthought.data.model.SeriesTitle;
import net.dankito.deepthought.data.model.Tag;
import net.dankito.deepthought.data.search.specific.CategoriesSearch;
import net.dankito.deepthought.data.search.specific.EntriesSearch;
import net.dankito.deepthought.data.search.specific.FilesSearch;
import net.dankito.deepthought.data.search.specific.FindAllEntriesHavingTheseTagsResult;
import net.dankito.deepthought.data.search.specific.ReferenceBaseType;
import net.dankito.deepthought.data.search.specific.ReferenceBasesSearch;
import net.dankito.deepthought.data.search.specific.TagsSearch;
import net.dankito.deepthought.data.search.specific.TagsSearchResults;
import net.dankito.deepthought.util.localization.Localization;

import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.RAMDirectory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by ganymed on 17/03/15.
 */
public class LuceneSearchEngineTest {

  private final static Logger log = LoggerFactory.getLogger(LuceneSearchEngineTest.class);

  protected static final int DEFAULT_WAIT_TIME = 2;


  protected net.dankito.deepthought.data.search.LuceneSearchEngine searchEngine;

  protected DeepThought deepThought;

  @Before
  public void setup() throws IOException {
    Localization.setLanguageLocale(Locale.ENGLISH); // for date handling

//          FileUtils.deleteFile(new File(Application.getDataFolderPath(), "index"));
    searchEngine = new LuceneSearchEngine(new RAMDirectory());

    Application.instantiate(new TestApplicationConfiguration() {
//    Application.instantiate(new TestApplicationConfiguration(new OrmLiteJavaSeEntityManager()) {

      @Override
      public ISearchEngine createSearchEngine() {
        return LuceneSearchEngineTest.this.searchEngine;
      }
    });

    deepThought = Application.getDeepThought();

//    searchEngine.deleteIndex();
    searchEngine.setIndexUpdatedEntitiesAfterMilliseconds(0);
  }

  @After
  public void tearDown() {
//    searchEngine.close();
    Application.shutdown();
  }


  @Test
  public void addEntry_EntryGetsIndexed() {
    Entry newEntry = new Entry("Love");
    deepThought.addEntry(newEntry);

    final List<Entry> results = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.searchEntries(new EntriesSearch("Love", true, false, new SearchCompletedListener<Collection<Entry>>() {
      @Override
      public void completed(Collection<Entry> result) {
        results.addAll(result);
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }

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

    searchEngine.searchEntries(new EntriesSearch("love", true, false, new SearchCompletedListener<Collection<Entry>>() {
      @Override
      public void completed(Collection<Entry> result) {
        results.addAll(result);
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(newEntry, results.get(0));

    // ensure previous entry content cannot be found anymore
    results.clear();
    final CountDownLatch nextCountDownLatch = new CountDownLatch(1);

    searchEngine.searchEntries(new EntriesSearch("schaaf", true, false, new SearchCompletedListener<Collection<Entry>>() {
      @Override
      public void completed(Collection<Entry> result) {
        results.addAll(result);
        nextCountDownLatch.countDown();
      }
    }));

    try { nextCountDownLatch.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertEquals(0, results.size());
  }

  @Test
  public void updateEntryAbstract_SearchFindsUpdatedEntry() {
    Entry newEntry = new Entry("", "Egalité");
    deepThought.addEntry(newEntry);

    newEntry.setAbstract("Equality");

    final List<Entry> results = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.searchEntries(new EntriesSearch("equ", false, true, new SearchCompletedListener<Collection<Entry>>() {
      @Override
      public void completed(Collection<Entry> result) {
        results.addAll(result);
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(newEntry, results.get(0));

    // ensure previous entry abstract cannot be found anymore
    results.clear();
    final CountDownLatch nextCountDownLatch = new CountDownLatch(1);

    searchEngine.searchEntries(new EntriesSearch("ega", false, true, new SearchCompletedListener<Collection<Entry>>() {
      @Override
      public void completed(Collection<Entry> result) {
        results.addAll(result);
        nextCountDownLatch.countDown();
      }
    }));

    try { nextCountDownLatch.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }

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
    final List<Entry> entriesHavingTheseTags = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.findAllEntriesHavingTheseTags(tagTwoCollection, new SearchCompletedListener<FindAllEntriesHavingTheseTagsResult>() {
      @Override
      public void completed(FindAllEntriesHavingTheseTagsResult results) {
        entriesHavingTheseTags.addAll(results.getEntriesHavingFilteredTags());
        countDownLatch.countDown();
      }
    });

    try { countDownLatch.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertEquals(1, entriesHavingTheseTags.size());
    Assert.assertEquals(newEntry, entriesHavingTheseTags.get(0));

    // ensure tag1 cannot be found anymore
    List<Tag> tagOneCollection = new ArrayList<Tag>() {{ add(tag1); }};
    entriesHavingTheseTags.clear();
    final CountDownLatch countDownLatch2 = new CountDownLatch(1);

    searchEngine.findAllEntriesHavingTheseTags(tagOneCollection, new SearchCompletedListener<FindAllEntriesHavingTheseTagsResult>() {
      @Override
      public void completed(FindAllEntriesHavingTheseTagsResult results) {
        entriesHavingTheseTags.addAll(results.getEntriesHavingFilteredTags());
        countDownLatch2.countDown();
      }
    });

    try { countDownLatch2.await(); } catch(Exception ex) { }

    Assert.assertEquals(0, entriesHavingTheseTags.size());
  }

  // TODO: reactivate these Tests again when used on Entry

//  @Test
  public void updateEntryCategories_SearchFindsUpdatedEntry() {
    Entry newEntry = new Entry("");
    deepThought.addEntry(newEntry);

    final Category category1 = new Category("Quotations");
    deepThought.addCategory(category1);
    final Category category2 = new Category("Periodicals");
    deepThought.addCategory(category2);

    newEntry.addCategory(category1);
    newEntry.addCategory(category2);

    newEntry.removeCategory(category1);

    List<Category> categoryTwoCollection = new ArrayList<Category>() {{ add(category2); }};
    // TODO: no method to search for Entry Categories yet

    ScoreDoc[] hits = searchEngine.search(new TermQuery(new Term(net.dankito.deepthought.data.search.FieldName.EntryCategories, "periodicals")), Entry.class);
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
    ScoreDoc[] hits2 = searchEngine.search(new TermQuery(new Term(net.dankito.deepthought.data.search.FieldName.EntryCategories, "quotations")), Entry.class);
    Assert.assertEquals(0, hits2.length);
//    entriesHavingTheseTags.clear();
//
//    searchEngine.findAllEntriesHavingTheseTags(categoryOneCollection, entriesHavingTheseTags, notInterestedIn);
//
//    Assert.assertEquals(0, entriesHavingTheseTags.size());
  }

//  @Test
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

    ScoreDoc[] hits = searchEngine.search(new WildcardQuery(new Term(net.dankito.deepthought.data.search.FieldName.EntryPersons, "gand*")), Entry.class);
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
    ScoreDoc[] hits2 = searchEngine.search(new WildcardQuery(new Term(net.dankito.deepthought.data.search.FieldName.EntryPersons, "mand*")), Entry.class);
    Assert.assertEquals(0, hits2.length);
    List<Person> personOneCollection = new ArrayList<Person>() {{ add(person1); }};
    // TODO as well
//    entriesHavingTheseTags.clear();
//
//    searchEngine.findAllEntriesHavingTheseTags(personOneCollection, entriesHavingTheseTags, notInterestedIn);
//
//    Assert.assertEquals(0, entriesHavingTheseTags.size());
  }

//  @Test
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

    ScoreDoc[] hits = searchEngine.search(new WildcardQuery(new Term(net.dankito.deepthought.data.search.FieldName.EntrySeries, "wikipedia")), Entry.class);
    Assert.assertEquals(1, hits.length);
//    final List<Entry> results = new ArrayList<>();
//    final CountDownLatch countDownLatch = new CountDownLatch(1);
//
//    searchEngine.searchEntries(new FilterEntriesSearch("love", true, false, new SearchCompletedListener<Entry>() {
//      @Override
//      public void completed(Collection<Entry> result) {
//        results.addAll(result);
//        countDownLatch.countDown();
//      }
//    }));
//
//    try { countDownLatch.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }
//
//    Assert.assertEquals(1, results.size());
//    Assert.assertEquals(newEntry, results.get(0));
//
//    // ensure previous entry content cannot be found anymore
    ScoreDoc[] hits2 = searchEngine.search(new WildcardQuery(new Term(net.dankito.deepthought.data.search.FieldName.EntrySeries, "Urban*")), Entry.class);
    Assert.assertEquals(0, hits2.length);
//    results.clear();
//    final CountDownLatch nextCountDownLatch = new CountDownLatch(1);
//
//    searchEngine.searchEntries(new FilterEntriesSearch("schaaf", true, false, new SearchCompletedListener<Entry>() {
//      @Override
//      public void completed(Collection<Entry> result) {
//        results.addAll(result);
//        nextCountDownLatch.countDown();
//      }
//    }));
//
//    try { nextCountDownLatch.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }
//
//    Assert.assertEquals(0, results.size());
  }

//  @Test
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

    ScoreDoc[] hits = searchEngine.search(new WildcardQuery(new Term(net.dankito.deepthought.data.search.FieldName.EntryReference, "koran")), Entry.class);
    Assert.assertEquals(1, hits.length);
//    final List<Entry> results = new ArrayList<>();
//    final CountDownLatch countDownLatch = new CountDownLatch(1);
//
//    searchEngine.searchEntries(new FilterEntriesSearch("love", true, false, new SearchCompletedListener<Entry>() {
//      @Override
//      public void completed(Collection<Entry> result) {
//        results.addAll(result);
//        countDownLatch.countDown();
//      }
//    }));
//
//    try { countDownLatch.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }
//
//    Assert.assertEquals(1, results.size());
//    Assert.assertEquals(newEntry, results.get(0));
//
//    // ensure previous entry content cannot be found anymore
    ScoreDoc[] hits2 = searchEngine.search(new TermQuery(new Term(net.dankito.deepthought.data.search.FieldName.EntryReference, "bible")), Entry.class);
    Assert.assertEquals(0, hits2.length);
//    results.clear();
//    final CountDownLatch nextCountDownLatch = new CountDownLatch(1);
//
//    searchEngine.searchEntries(new FilterEntriesSearch("schaaf", true, false, new SearchCompletedListener<Entry>() {
//      @Override
//      public void completed(Collection<Entry> result) {
//        results.addAll(result);
//        nextCountDownLatch.countDown();
//      }
//    }));
//
//    try { nextCountDownLatch.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }
//
//    Assert.assertEquals(0, results.size());
  }

//  @Test
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

    ScoreDoc[] hits = searchEngine.search(new WildcardQuery(new Term(net.dankito.deepthought.data.search.FieldName.EntryReferenceSubDivision, "love")), Entry.class);
    Assert.assertEquals(1, hits.length);
//    final List<Entry> results = new ArrayList<>();
//    final CountDownLatch countDownLatch = new CountDownLatch(1);
//
//    searchEngine.searchEntries(new FilterEntriesSearch("love", true, false, new SearchCompletedListener<Entry>() {
//      @Override
//      public void completed(Collection<Entry> result) {
//        results.addAll(result);
//        countDownLatch.countDown();
//      }
//    }));
//
//    try { countDownLatch.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }
//
//    Assert.assertEquals(1, results.size());
//    Assert.assertEquals(newEntry, results.get(0));
//
//    // ensure previous entry content cannot be found anymore
    ScoreDoc[] hits2 = searchEngine.search(new TermQuery(new Term(net.dankito.deepthought.data.search.FieldName.EntryReferenceSubDivision, "hate")), Entry.class);
    Assert.assertEquals(0, hits2.length);
//    results.clear();
//    final CountDownLatch nextCountDownLatch = new CountDownLatch(1);
//
//    searchEngine.searchEntries(new FilterEntriesSearch("schaaf", true, false, new SearchCompletedListener<Entry>() {
//      @Override
//      public void completed(Collection<Entry> result) {
//        results.addAll(result);
//        nextCountDownLatch.countDown();
//      }
//    }));
//
//    try { nextCountDownLatch.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }
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

    searchEngine.searchEntries(new EntriesSearch("love", true, false, new SearchCompletedListener<Collection<Entry>>() {
      @Override
      public void completed(Collection<Entry> result) {
        results.addAll(result);
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }

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

    searchEngine.searchEntries(new EntriesSearch("love", true, false, true, new SearchCompletedListener<Collection<Entry>>() {
      @Override
      public void completed(Collection<Entry> result) {
        results.addAll(result);
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size()); // only that one without Tags is found
    Assert.assertEquals(entryWithoutTag, results.get(0));
  }

  @Test
  public void searchEntriesWithSpecificTag_OnlyEntriesWithSpecifiedTagGetFound() {
    Entry entry1 = new Entry("Love");
    Entry entry2 = new Entry("Love");
    Entry entry3 = new Entry("Love");
    deepThought.addEntry(entry1);
    deepThought.addEntry(entry2);
    deepThought.addEntry(entry3);

    Tag tagToFind = new Tag("Tag");
    deepThought.addTag(tagToFind);
    entry1.addTag(tagToFind);
    entry3.addTag(tagToFind);

    Collection<Tag> tagsEntriesMustHave = Arrays.asList(new Tag[] { tagToFind });

    final List<Entry> results = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.searchEntries(new EntriesSearch("love", true, false, tagsEntriesMustHave, new SearchCompletedListener<Collection<Entry>>() {
      @Override
      public void completed(Collection<Entry> result) {
        results.addAll(result);
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertEquals(2, results.size()); // only that one without Tags is found
    Assert.assertTrue(results.contains(entry1));
    Assert.assertTrue(results.contains(entry3));
    Assert.assertFalse(results.contains(entry2));
  }

  @Test
  public void searchEntriesContent_SpecifyMoreThanTwoSearchTerms_EntryWithBothSearchTermsGetsFound() {
    Entry entry1 = new Entry("Love");
    Entry entry2 = new Entry("Kill");
    Entry entry3 = new Entry("Love kills");
    deepThought.addEntry(entry1);
    deepThought.addEntry(entry2);
    deepThought.addEntry(entry3);

    final List<Entry> results = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.searchEntries(new EntriesSearch("love kill", true, false, new SearchCompletedListener<Collection<Entry>>() {
      @Override
      public void completed(Collection<Entry> result) {
        results.addAll(result);
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size()); // only that one without Tags is found
    Assert.assertFalse(results.contains(entry1));
    Assert.assertFalse(results.contains(entry2));
    Assert.assertTrue(results.contains(entry3));
  }

  @Test
  public void searchEntriesAbstract_SpecifyMoreThanTwoSearchTerms_EntryWithBothSearchTermsGetsFound() {
    Entry entry1 = new Entry("", "Love");
    Entry entry2 = new Entry("", "Kill");
    Entry entry3 = new Entry("", "Love kills");
    deepThought.addEntry(entry1);
    deepThought.addEntry(entry2);
    deepThought.addEntry(entry3);

    final List<Entry> results = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.searchEntries(new EntriesSearch("love kill", false, true, new SearchCompletedListener<Collection<Entry>>() {
      @Override
      public void completed(Collection<Entry> result) {
        results.addAll(result);
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size()); // only that one without Tags is found
    Assert.assertFalse(results.contains(entry1));
    Assert.assertFalse(results.contains(entry2));
    Assert.assertTrue(results.contains(entry3));
  }

  @Test
  public void searchEntries_SearchResultIsInCorrectOrder() {
    Entry entry1 = new Entry("", "Mandela");
    Entry entry2 = new Entry("", "Mother");
    Entry entry3 = new Entry("", "Mahatma");
    deepThought.addEntry(entry1);
    deepThought.addEntry(entry2);
    deepThought.addEntry(entry3);

    final List<Entry> results = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.searchEntries(new EntriesSearch("m", true, true, new SearchCompletedListener<Collection<Entry>>() {
      @Override
      public void completed(Collection<Entry> result) {
        results.addAll(result);
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertEquals(3, results.size());
    Assert.assertEquals(entry3, results.get(0)); // search results must be in reverse order than created order (newer ones being shown first)
    Assert.assertEquals(entry2, results.get(1));
    Assert.assertEquals(entry1, results.get(2));
  }

  @Test
  public void searchEntriesWithGermanUmlaut() {
    Entry entry = new Entry("", "Ägyptischer Journalist");
    deepThought.addEntry(entry);

    final List<Entry> results = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.searchEntries(new EntriesSearch("äg", true, true, new SearchCompletedListener<Collection<Entry>>() {
      @Override
      public void completed(Collection<Entry> result) {
        results.addAll(result);
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size()); // ensure a search term with a German Umlaut gets found
    Assert.assertTrue(results.contains(entry));

    results.clear();
    final CountDownLatch secondCountDownLatch = new CountDownLatch(1);

    searchEngine.searchEntries(new EntriesSearch("aeg", true, true, new SearchCompletedListener<Collection<Entry>>() {
      @Override
      public void completed(Collection<Entry> result) {
        results.addAll(result);
        secondCountDownLatch.countDown();
      }
    }));

    try { secondCountDownLatch.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertEquals(0, results.size()); // ensure a search term with its Umlaut equivalent doesn't get found
  }


  @Test
  public void addTag_TagGetsIndexed() {
    Tag newTag = new Tag("tag");
    deepThought.addTag(newTag);

    final List<Tag> results = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.searchTags(new TagsSearch("Tag", new SearchCompletedListener<TagsSearchResults>() {
      @Override
      public void completed(TagsSearchResults result) {
        results.addAll(result.getRelevantMatchesSorted());
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }

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

    searchEngine.searchTags(new TagsSearch("swag", new SearchCompletedListener<TagsSearchResults>() {
      @Override
      public void completed(TagsSearchResults result) {
        results.addAll(result.getRelevantMatchesSorted());
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(newTag, results.get(0));

    // ensure previous tag name cannot be found anymore
    results.clear();
    final CountDownLatch nextCountDownLatch = new CountDownLatch(1);

    searchEngine.searchTags(new TagsSearch("tag", new SearchCompletedListener<TagsSearchResults>() {
      @Override
      public void completed(TagsSearchResults result) {
        results.addAll(result.getRelevantMatchesSorted());
        nextCountDownLatch.countDown();
      }
    }));

    try { nextCountDownLatch.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertEquals(0, results.size());
  }

  @Test
  public void deleteTag_SearchDoesNotFindTagAnymore() {
    Tag newTag = new Tag("tag");
    deepThought.addTag(newTag);

    deepThought.removeTag(newTag);

    final List<Tag> results = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.searchTags(new TagsSearch("Tag", new SearchCompletedListener<TagsSearchResults>() {
      @Override
      public void completed(TagsSearchResults result) {
        results.addAll(result.getRelevantMatchesSorted());
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertEquals(0, results.size());
  }


  @Test
  public void addCategory_CategoryGetsIndexed() {
    Category newCategory = new Category("category");
    deepThought.addCategory(newCategory);

    final List<Category> results = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.searchCategories(new CategoriesSearch("Category", new SearchCompletedListener<Collection<Category>>() {
      @Override
      public void completed(Collection<Category> result) {
        results.addAll(result);
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(newCategory, results.get(0));
  }

  @Test
  public void updateCategory_SearchFindsUpdatedCategory() {
    Category newCategory = new Category("category");
    deepThought.addCategory(newCategory);

    newCategory.setName("Periodicals");

    final List<Category> results = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.searchCategories(new CategoriesSearch("perio", new SearchCompletedListener<Collection<Category>>() {
      @Override
      public void completed(Collection<Category> result) {
        results.addAll(result);
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(newCategory, results.get(0));

    // ensure previous tag name cannot be found anymore
    results.clear();
    final CountDownLatch nextCountDownLatch = new CountDownLatch(1);

    searchEngine.searchCategories(new CategoriesSearch("category", new SearchCompletedListener<Collection<Category>>() {
      @Override
      public void completed(Collection<Category> result) {
        results.addAll(result);
        nextCountDownLatch.countDown();
      }
    }));

    try { nextCountDownLatch.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertEquals(0, results.size());
  }

  @Test
  public void searchCategories_SearchResultsAreInCorrectAlphabeticalOrder() {
    Category category1 = new Category("Liebe");
    Category category2 = new Category("Hiebe");
    Category category3 = new Category("Siebe");
    Category category4 = new Category("Diebe");
    deepThought.addCategory(category1);
    deepThought.addCategory(category2);
    deepThought.addCategory(category3);
    deepThought.addCategory(category4);

    final List<Category> results = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.searchCategories(new CategoriesSearch("iebe", new SearchCompletedListener<Collection<Category>>() {
      @Override
      public void completed(Collection<Category> result) {
        results.addAll(result);
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertEquals(4, results.size());

    Assert.assertEquals(category4, results.get(0));
    Assert.assertEquals(category2, results.get(1));
    Assert.assertEquals(category1, results.get(2));
    Assert.assertEquals(category3, results.get(3));
  }

  @Test
  public void searchCategories_SearchForParentCategoriesOnly() {
    Category topLevelCategory1 = new Category("Category1");
    Category topLevelCategory2 = new Category("Category2");
    deepThought.addCategory(topLevelCategory1);
    deepThought.addCategory(topLevelCategory2);

    for(int i = 0; i < 3; i++) {
      Category subCategoryForCat1 = new Category("Category" + i);
      deepThought.addCategory(subCategoryForCat1);
      topLevelCategory1.addSubCategory(subCategoryForCat1);

      Category subCategoryForCat2 = new Category("Category" + i);
      deepThought.addCategory(subCategoryForCat2);
      topLevelCategory2.addSubCategory(subCategoryForCat2);
    }


    // Search for 'Category1' -> all three get found
    final List<Category> results = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.searchCategories(new CategoriesSearch("Category1", new SearchCompletedListener<Collection<Category>>() {
      @Override
      public void completed(Collection<Category> result) {
        results.addAll(result);
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertEquals(3, results.size());

    Assert.assertTrue(results.contains(topLevelCategory1));


    // now search for 'Category1' as TopLevelCategory -> only that one TopLevelCategory gets found
    results.clear();
    final CountDownLatch countDownLatch2 = new CountDownLatch(1);

    searchEngine.searchCategories(new CategoriesSearch("Category1", true, new SearchCompletedListener<Collection<Category>>() {
      @Override
      public void completed(Collection<Category> result) {
        results.addAll(result);
        countDownLatch2.countDown();
      }
    }));

    try { countDownLatch2.await(); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(topLevelCategory1, results.get(0));


    // now search for 'Category2' as TopLevelCategory -> only that one TopLevelCategory gets found
    results.clear();
    final CountDownLatch countDownLatch3 = new CountDownLatch(1);

    searchEngine.searchCategories(new CategoriesSearch("Category2", true, new SearchCompletedListener<Collection<Category>>() {
      @Override
      public void completed(Collection<Category> result) {
        results.addAll(result);
        countDownLatch3.countDown();
      }
    }));

    try { countDownLatch3.await(); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(topLevelCategory2, results.get(0));
  }

  @Test
  public void searchCategories_IncludeParentCategories() {
    Category topLevelCategory1 = new Category("TopLevelCategory1");
    Category topLevelCategory2 = new Category("TopLevelCategory2");
    deepThought.addCategory(topLevelCategory1);
    deepThought.addCategory(topLevelCategory2);

    for(int i = 0; i < 3; i++) {
      Category subCategoryForCat1 = new Category("SubCategory" + i);
      deepThought.addCategory(subCategoryForCat1);
      topLevelCategory1.addSubCategory(subCategoryForCat1);

      Category subCategoryForCat2 = new Category("SubCategory" + i);
      deepThought.addCategory(subCategoryForCat2);
      topLevelCategory2.addSubCategory(subCategoryForCat2);
    }

    String subCategoryNameToSearchFor = "Amore";

    Category subCategoryForCat1 = new Category(subCategoryNameToSearchFor);
    deepThought.addCategory(subCategoryForCat1);
    topLevelCategory1.addSubCategory(subCategoryForCat1);

    Category subCategoryForCat2 = new Category(subCategoryNameToSearchFor);
    deepThought.addCategory(subCategoryForCat2);
    topLevelCategory2.addSubCategory(subCategoryForCat2);


    // Search for Category without specifying Parent Category -> both Categories must be found
    final List<Category> results = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.searchCategories(new CategoriesSearch(subCategoryNameToSearchFor, new SearchCompletedListener<Collection<Category>>() {
      @Override
      public void completed(Collection<Category> result) {
        results.addAll(result);
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertEquals(2, results.size());

    Assert.assertEquals(subCategoryForCat1, results.get(0));
    Assert.assertEquals(subCategoryForCat2, results.get(1));


    // now search for same Category Name but as child of topLevelCategory1
    results.clear();
    final CountDownLatch countDownLatch2 = new CountDownLatch(1);

    searchEngine.searchCategories(new CategoriesSearch(subCategoryNameToSearchFor, topLevelCategory1, new SearchCompletedListener<Collection<Category>>() {
      @Override
      public void completed(Collection<Category> result) {
        results.addAll(result);
        countDownLatch2.countDown();
      }
    }));

    try { countDownLatch2.await(); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size());

    Assert.assertEquals(subCategoryForCat1, results.get(0));
    Assert.assertEquals(topLevelCategory1, results.get(0).getParentCategory());


    // and now search for same Category Name but as child of topLevelCategory2
    results.clear();
    final CountDownLatch countDownLatch3 = new CountDownLatch(1);

    searchEngine.searchCategories(new CategoriesSearch(subCategoryNameToSearchFor, topLevelCategory2, new SearchCompletedListener<Collection<Category>>() {
      @Override
      public void completed(Collection<Category> result) {
        results.addAll(result);
        countDownLatch3.countDown();
      }
    }));

    try { countDownLatch3.await(); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size());

    Assert.assertEquals(subCategoryForCat2, results.get(0));
    Assert.assertEquals(topLevelCategory2, results.get(0).getParentCategory());


    // remove SubCategory -> SubCategory doesn't get found anymore
    topLevelCategory1.removeSubCategory(subCategoryForCat1);
    results.clear();
    final CountDownLatch countDownLatch4 = new CountDownLatch(1);

    searchEngine.searchCategories(new CategoriesSearch(subCategoryNameToSearchFor, topLevelCategory1, new SearchCompletedListener<Collection<Category>>() {
      @Override
      public void completed(Collection<Category> result) {
        results.addAll(result);
        countDownLatch4.countDown();
      }
    }));

    try { countDownLatch4.await(); } catch(Exception ex) { }

    Assert.assertEquals(0, results.size());
  }

  @Test
  public void deleteCategory_SearchDoesNotFindCategoryAnymore() {
    Category newCategory = new Category("category");
    deepThought.addCategory(newCategory);

    deepThought.removeCategory(newCategory);

    final List<Category> results = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.searchCategories(new CategoriesSearch("category", new SearchCompletedListener<Collection<Category>>() {
      @Override
      public void completed(Collection<Category> result) {
        results.addAll(result);
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertEquals(0, results.size());
  }


  @Test
  public void addPerson_PersonGetsIndexed() {
    Person newPerson = new Person("first", "last");
    deepThought.addPerson(newPerson);

    final List<Person> results = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.searchPersons(new Search<Person>("last", new SearchCompletedListener<Collection<Person>>() {
      @Override
      public void completed(Collection<Person> result) {
        results.addAll(result);
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }

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

    searchEngine.searchPersons(new Search<Person>("zeus", new SearchCompletedListener<Collection<Person>>() {
      @Override
      public void completed(Collection<Person> result) {
        results.addAll(result);
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(newPerson, results.get(0));

    // ensure previous tag name cannot be found anymore
    results.clear();
    final CountDownLatch nextCountDownLatch = new CountDownLatch(1);

    searchEngine.searchPersons(new Search<Person>("last", new SearchCompletedListener<Collection<Person>>() {
      @Override
      public void completed(Collection<Person> result) {
        results.addAll(result);
        nextCountDownLatch.countDown();
      }
    }));

    try { nextCountDownLatch.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertEquals(0, results.size());
  }

  @Test
  public void updatePersonFirstName_SearchFindsUpdatedPerson() {
    Person newPerson = new Person("first", "");
    deepThought.addPerson(newPerson);

    newPerson.setFirstName("Giovanni");

    final List<Person> results = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.searchPersons(new Search<Person>("giovanni", new SearchCompletedListener<Collection<Person>>() {
      @Override
      public void completed(Collection<Person> result) {
        results.addAll(result);
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(newPerson, results.get(0));

    // ensure previous tag name cannot be found anymore
    results.clear();
    final CountDownLatch nextCountDownLatch = new CountDownLatch(1);

    searchEngine.searchPersons(new Search<Person>("first", new SearchCompletedListener<Collection<Person>>() {
      @Override
      public void completed(Collection<Person> result) {
        results.addAll(result);
        nextCountDownLatch.countDown();
      }
    }));

    try { nextCountDownLatch.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertEquals(0, results.size());
  }

  @Test
  public void searchPersonsForFirstAndLastName() {
    Person newPerson = new Person("Mahatma", "Gandhi");
    deepThought.addPerson(newPerson);
    deepThought.addPerson(new Person("dummy", ""));
    deepThought.addPerson(new Person("Arnold", "Schwarznegger"));

    final List<Person> results = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.searchPersons(new Search<Person>("gan, mah", new SearchCompletedListener<Collection<Person>>() {
      @Override
      public void completed(Collection<Person> result) {
        results.addAll(result);
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(newPerson, results.get(0));

    // ensure Person is not found if First name is wrong
    results.clear();
    final CountDownLatch nextCountDownLatch = new CountDownLatch(1);

    searchEngine.searchPersons(new Search<Person>("Gandhi, Mohandas", new SearchCompletedListener<Collection<Person>>() {
      @Override
      public void completed(Collection<Person> result) {
        results.addAll(result);
        nextCountDownLatch.countDown();
      }
    }));

    try { nextCountDownLatch.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertEquals(0, results.size());
  }

  @Test
  public void deletePerson_SearchDoesNotFindPersonAnymore() {
    Person newPerson = new Person("first", "last");
    deepThought.addPerson(newPerson);

    deepThought.removePerson(newPerson);

    final List<Person> results = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.searchPersons(new Search<Person>("first", new SearchCompletedListener<Collection<Person>>() {
      @Override
      public void completed(Collection<Person> result) {
        results.addAll(result);
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertEquals(0, results.size());
  }

  @Test
  public void searchPersons_SearchResultsAreInCorrectAlphabeticalOrder() {
    Person person1 = new Person("Mother", "Teresa");
    Person person2 = new Person("Mahatma", "Gandhi");
    Person person3 = new Person("Nelson", "Mandela");
    deepThought.addPerson(person1);
    deepThought.addPerson(person2);
    deepThought.addPerson(person3);

    final List<Person> results = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.searchPersons(new Search<Person>("m", new SearchCompletedListener<Collection<Person>>() {
      @Override
      public void completed(Collection<Person> result) {
        results.addAll(result);
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertEquals(3, results.size());
    Assert.assertEquals(person2, results.get(0));
    Assert.assertEquals(person3, results.get(1));
    Assert.assertEquals(person1, results.get(2));
  }

  @Test
  public void searchPersonsWithIdenticalLastNames_SearchResultsAreInCorrectAlphabeticalOrder() {
    Person person1 = new Person("Mohandas Karamchand", "Gandhi");
    Person person2 = new Person("Mahatma", "Gandhi");
    deepThought.addPerson(person1);
    deepThought.addPerson(person2);

    final List<Person> results = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.searchPersons(new Search<Person>("gand", new SearchCompletedListener<Collection<Person>>() {
      @Override
      public void completed(Collection<Person> result) {
        results.addAll(result);
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertEquals(2, results.size());
    Assert.assertEquals(person2, results.get(0));
    Assert.assertEquals(person1, results.get(1));
  }

  @Test
  public void searchPersonsForFirstAndLastName_SearchResultsAreInCorrectAlphabeticalOrder() {
    Person person1 = new Person("Mohandas Karamchand", "Gandhi");
    Person person2 = new Person("Mahatma", "Gandhi");
    deepThought.addPerson(person1);
    deepThought.addPerson(person2);

    final List<Person> results = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.searchPersons(new Search<Person>("gand, m", new SearchCompletedListener<Collection<Person>>() {
      @Override
      public void completed(Collection<Person> result) {
        results.addAll(result);
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertEquals(2, results.size());
    Assert.assertEquals(person2, results.get(0));
    Assert.assertEquals(person1, results.get(1));
  }

  @Test
  public void searchPersonWithTwoFirstNames_SearchingForSecondFirstName_PersonGetsFound() {
    Person person = new Person("Mohandas Karamchand", "Gandhi");
    deepThought.addPerson(person);

    final List<Person> results = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.searchPersons(new Search<Person>("karam", new SearchCompletedListener<Collection<Person>>() {
      @Override
      public void completed(Collection<Person> result) {
        results.addAll(result);
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(person, results.get(0));
  }


  @Test
  public void addSeriesTitle_SeriesTitleGetsIndexed() {
    SeriesTitle newSeriesTitle = new SeriesTitle("series");
    deepThought.addSeriesTitle(newSeriesTitle);

    final List<ReferenceBase> results = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.searchReferenceBases(new ReferenceBasesSearch("Series", ReferenceBaseType.SeriesTitle, new SearchCompletedListener<Collection<ReferenceBase>>() {
      @Override
      public void completed(Collection<ReferenceBase> result) {
        results.addAll(result);
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }

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

    searchEngine.searchReferenceBases(new ReferenceBasesSearch("aphrodite", new SearchCompletedListener<Collection<ReferenceBase>>() {
      @Override
      public void completed(Collection<ReferenceBase> result) {
        results.addAll(result);
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(newSeriesTitle, results.get(0));

    // ensure previous tag name cannot be found anymore
    results.clear();
    final CountDownLatch nextCountDownLatch = new CountDownLatch(1);

    searchEngine.searchReferenceBases(new ReferenceBasesSearch("series", new SearchCompletedListener<Collection<ReferenceBase>>() {
      @Override
      public void completed(Collection<ReferenceBase> result) {
        results.addAll(result);
        nextCountDownLatch.countDown();
      }
    }));

    try { nextCountDownLatch.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertEquals(0, results.size());
  }

  @Test
  public void updateSeriesTitleSubTitle_SearchFindsUpdatedSeriesTitle() {
    SeriesTitle newSeriesTitle = new SeriesTitle("series", "don't find me");
    deepThought.addSeriesTitle(newSeriesTitle);

    newSeriesTitle.setSubTitle("subtitle");

    final List<ReferenceBase> results = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.searchReferenceBases(new ReferenceBasesSearch("subtitle", new SearchCompletedListener<Collection<ReferenceBase>>() {
      @Override
      public void completed(Collection<ReferenceBase> result) {
        results.addAll(result);
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(newSeriesTitle, results.get(0));

    // ensure previous tag name cannot be found anymore
    results.clear();
    final CountDownLatch nextCountDownLatch = new CountDownLatch(1);

    searchEngine.searchReferenceBases(new ReferenceBasesSearch("find", new SearchCompletedListener<Collection<ReferenceBase>>() {
      @Override
      public void completed(Collection<ReferenceBase> result) {
        results.addAll(result);
        nextCountDownLatch.countDown();
      }
    }));

    try { nextCountDownLatch.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertEquals(0, results.size());
  }

  @Test
  public void searchSeriesTitles() {
    SeriesTitle newSeriesTitle = new SeriesTitle("SZ", "Ich brauch irgend einen Untertitel");
    deepThought.addSeriesTitle(newSeriesTitle);
    deepThought.addSeriesTitle(new SeriesTitle("dummy"));
    deepThought.addSeriesTitle(new SeriesTitle("Bild 'Zeitung'"));

    final List<ReferenceBase> results = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.searchReferenceBases(new ReferenceBasesSearch("sz", new SearchCompletedListener<Collection<ReferenceBase>>() {
      @Override
      public void completed(Collection<ReferenceBase> result) {
        results.addAll(result);
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(newSeriesTitle, results.get(0));


    results.clear();
    final CountDownLatch countDownLatch2 = new CountDownLatch(1);

    searchEngine.searchReferenceBases(new ReferenceBasesSearch("untert", new SearchCompletedListener<Collection<ReferenceBase>>() {
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

    searchEngine.searchReferenceBases(new ReferenceBasesSearch("unterd", new SearchCompletedListener<Collection<ReferenceBase>>() {
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

    final List<ReferenceBase> results = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.searchReferenceBases(new ReferenceBasesSearch("Reference", new SearchCompletedListener<Collection<ReferenceBase>>() {
      @Override
      public void completed(Collection<ReferenceBase> result) {
        results.addAll(result);
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(newReference, results.get(0));
  }

  @Test
  public void updateReferenceTitle_SearchFindsUpdatedReference() {
    Reference newReference = new Reference("Hephaistos");
    deepThought.addReference(newReference);

    newReference.setTitle("Aphrodite");

    final List<ReferenceBase> results = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.searchReferenceBases(new ReferenceBasesSearch("aphrodite", new SearchCompletedListener<Collection<ReferenceBase>>() {
      @Override
      public void completed(Collection<ReferenceBase> result) {
        results.addAll(result);
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(newReference, results.get(0));

    // ensure previous reference title cannot be found anymore
    results.clear();
    final CountDownLatch nextCountDownLatch = new CountDownLatch(1);

    searchEngine.searchReferenceBases(new ReferenceBasesSearch("Hephaistos", new SearchCompletedListener<Collection<ReferenceBase>>() {
      @Override
      public void completed(Collection<ReferenceBase> result) {
        results.addAll(result);
        nextCountDownLatch.countDown();
      }
    }));

    try { nextCountDownLatch.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertEquals(0, results.size());
  }

  @Test
  public void updateReferenceSubTitle_SearchFindsUpdatedReference() {
    Reference newReference = new Reference("reference", "don't find me");
    deepThought.addReference(newReference);

    newReference.setSubTitle("Hephaistos");

    final List<ReferenceBase> results = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.searchReferenceBases(new ReferenceBasesSearch("heph", new SearchCompletedListener<Collection<ReferenceBase>>() {
      @Override
      public void completed(Collection<ReferenceBase> result) {
        results.addAll(result);
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(newReference, results.get(0));

    // ensure previous tag name cannot be found anymore
    results.clear();
    final CountDownLatch nextCountDownLatch = new CountDownLatch(1);

    searchEngine.searchReferenceBases(new ReferenceBasesSearch("find", new SearchCompletedListener<Collection<ReferenceBase>>() {
      @Override
      public void completed(Collection<ReferenceBase> result) {
        results.addAll(result);
        nextCountDownLatch.countDown();
      }
    }));

    try { nextCountDownLatch.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertEquals(0, results.size());
  }

  @Test
  public void updateReferenceIssueOrPublishingDate_SearchFindsUpdatedReference() {
    Reference newReference = new Reference("reference");
    deepThought.addReference(newReference);

    newReference.setIssueOrPublishingDate("03/2010");

    final List<ReferenceBase> results = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.searchReferenceBases(new ReferenceBasesSearch("2010", new SearchCompletedListener<Collection<ReferenceBase>>() {
      @Override
      public void completed(Collection<ReferenceBase> result) {
        results.addAll(result);
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(newReference, results.get(0));
  }

  @Test
  public void searchReferences() {
    Reference newReference = new Reference("Selbst Denken", "Anleitung zum Widerstand");
    newReference.setIssueOrPublishingDate("2012");
    deepThought.addReference(newReference);
    deepThought.addReference(new Reference("Befreiung vom Überfluss", "Auf dem Weg in die Postwachstumsökonomie"));
    deepThought.addReference(new Reference("Schulden", "Die ersten 5000 Jahre"));

    final List<ReferenceBase> results = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.searchReferenceBases(new ReferenceBasesSearch("denk", new SearchCompletedListener<Collection<ReferenceBase>>() {
      @Override
      public void completed(Collection<ReferenceBase> result) {
        results.addAll(result);
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(newReference, results.get(0));


    results.clear();
    final CountDownLatch countDownLatch2 = new CountDownLatch(1);

    searchEngine.searchReferenceBases(new ReferenceBasesSearch("wider", new SearchCompletedListener<Collection<ReferenceBase>>() {
      @Override
      public void completed(Collection<ReferenceBase> result) {
        results.addAll(result);
        countDownLatch2.countDown();
      }
    }));

    try { countDownLatch2.await(); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(newReference, results.get(0));


    results.clear();
    final CountDownLatch countDownLatch3 = new CountDownLatch(1);

    searchEngine.searchReferenceBases(new ReferenceBasesSearch("2012", new SearchCompletedListener<Collection<ReferenceBase>>() {
      @Override
      public void completed(Collection<ReferenceBase> result) {
        results.addAll(result);
        countDownLatch3.countDown();
      }
    }));

    try { countDownLatch3.await(); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(newReference, results.get(0));


    results.clear();
    final CountDownLatch countDownLatch4 = new CountDownLatch(1);

    searchEngine.searchReferenceBases(new ReferenceBasesSearch("Liebe", new SearchCompletedListener<Collection<ReferenceBase>>() {
      @Override
      public void completed(Collection<ReferenceBase> result) {
        results.addAll(result);
        countDownLatch4.countDown();
      }
    }));

    try { countDownLatch4.await(); } catch(Exception ex) { }

    Assert.assertEquals(0, results.size());
  }

  @Test
  public void findReferenceOfDate() throws Exception {
    Localization.setLanguageLocale(Locale.GERMAN);
    SeriesTitle guardian = new SeriesTitle("The Guardian");
    SeriesTitle newYorkTimes = new SeriesTitle("New York Times");
    SeriesTitle sz = new SeriesTitle("SZ");
    deepThought.addSeriesTitle(guardian);
    deepThought.addSeriesTitle(newYorkTimes);
    deepThought.addSeriesTitle(sz);

    DateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy");
    String date27031988String = "27.03.1988";
    String date07051960String = "07.05.1960";
    Date date27031988 = dateFormatter.parse(date27031988String);
    Date date07051960 = dateFormatter.parse(date07051960String);

    Reference guardian27031988 = new Reference();
    guardian27031988.setSeries(guardian);
    guardian27031988.setPublishingDate(date27031988);
    deepThought.addReference(guardian27031988);

    Reference newYorkTimes27031988 = new Reference();
    newYorkTimes27031988.setSeries(newYorkTimes);
    newYorkTimes27031988.setPublishingDate(date27031988);
    deepThought.addReference(newYorkTimes27031988);

    Reference sz27031988 = new Reference();
    sz27031988.setSeries(sz);
    sz27031988.setPublishingDate(date27031988);
    deepThought.addReference(sz27031988);

    Reference guardian07051960 = new Reference();
    guardian07051960.setSeries(guardian);
    guardian07051960.setPublishingDate(date07051960);
    deepThought.addReference(guardian07051960);

    date27031988String = guardian27031988.getIssueOrPublishingDate(); // we cannot predict Locale (of Build Server, User in other countries, ...)
    date07051960String = guardian07051960.getIssueOrPublishingDate();

    final List<Reference> results = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.searchForReferenceOfDate(null, new Search<Reference>(date27031988String, new SearchCompletedListener<Collection<Reference>>() {
      @Override
      public void completed(Collection<Reference> result) {
        results.addAll(result);
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertEquals(3, results.size());
    Assert.assertTrue(results.contains(guardian27031988));
    Assert.assertTrue(results.contains(newYorkTimes27031988));
    Assert.assertTrue(results.contains(sz27031988));


    results.clear();
    final CountDownLatch countDownLatch2 = new CountDownLatch(1);

    searchEngine.searchForReferenceOfDate("The Guardian", new Search<Reference>(date27031988String, new SearchCompletedListener<Collection<Reference>>() {
      @Override
      public void completed(Collection<Reference> result) {
        results.addAll(result);
        countDownLatch2.countDown();
      }
    }));

    try { countDownLatch2.await(); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(guardian27031988, results.get(0));


    results.clear();
    final CountDownLatch countDownLatch3 = new CountDownLatch(1);

    searchEngine.searchForReferenceOfDate("New York Times", new Search<Reference>(date27031988String, new SearchCompletedListener<Collection<Reference>>() {
      @Override
      public void completed(Collection<Reference> result) {
        results.addAll(result);
        countDownLatch3.countDown();
      }
    }));

    try { countDownLatch3.await(); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(newYorkTimes27031988, results.get(0));


    results.clear();
    final CountDownLatch countDownLatch4 = new CountDownLatch(1);

    searchEngine.searchForReferenceOfDate("sz", new Search<Reference>(date27031988String, new SearchCompletedListener<Collection<Reference>>() {
      @Override
      public void completed(Collection<Reference> result) {
        results.addAll(result);
        countDownLatch4.countDown();
      }
    }));

    try { countDownLatch4.await(); } catch(Exception ex) { }


    Assert.assertEquals(1, results.size());
    Assert.assertEquals(sz27031988, results.get(0));


    results.clear();
    final CountDownLatch countDownLatch5 = new CountDownLatch(1);

    searchEngine.searchForReferenceOfDate(null, new Search<Reference>(date07051960String, new SearchCompletedListener<Collection<Reference>>() {
      @Override
      public void completed(Collection<Reference> result) {
        results.addAll(result);
        countDownLatch5.countDown();
      }
    }));

    try { countDownLatch5.await(); } catch(Exception ex) { }


    Assert.assertEquals(1, results.size());
    Assert.assertEquals(guardian07051960, results.get(0));


    results.clear();
    final CountDownLatch countDownLatch6 = new CountDownLatch(1);
    String unstoredDate = dateFormatter.format(dateFormatter.parse("01.01.1970"));

    searchEngine.searchForReferenceOfDate(null, new Search<Reference>(unstoredDate, new SearchCompletedListener<Collection<Reference>>() {
      @Override
      public void completed(Collection<Reference> result) {
        results.addAll(result);
        countDownLatch6.countDown();
      }
    }));

    try { countDownLatch6.await(); } catch(Exception ex) { }

    Assert.assertEquals(0, results.size());
  }


  @Test
  public void addReferenceSubDivision_ReferenceSubDivisionGetsIndexed() {
    Reference newReference = new Reference("reference");
    deepThought.addReference(newReference);

    ReferenceSubDivision newSubDivision = new ReferenceSubDivision("subDivision");
    deepThought.addReferenceSubDivision(newSubDivision);
    newReference.addSubDivision(newSubDivision);

    final List<ReferenceBase> results = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.searchReferenceBases(new ReferenceBasesSearch("SUB", new SearchCompletedListener<Collection<ReferenceBase>>() {
      @Override
      public void completed(Collection<ReferenceBase> result) {
        results.addAll(result);
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(newSubDivision, results.get(0));
  }

  @Test
  public void updateReferenceSubDivisionTitle_SearchFindsUpdatedReferenceSubDivision() {
    Reference newReference = new Reference("reference");
    deepThought.addReference(newReference);

    ReferenceSubDivision newSubDivision = new ReferenceSubDivision("subDivision");
    deepThought.addReferenceSubDivision(newSubDivision);
    newReference.addSubDivision(newSubDivision);
    newSubDivision.setTitle("Aphrodite");

    final List<ReferenceBase> results = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.searchReferenceBases(new ReferenceBasesSearch("aphrodite", new SearchCompletedListener<Collection<ReferenceBase>>() {
      @Override
      public void completed(Collection<ReferenceBase> result) {
        results.addAll(result);
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(newSubDivision, results.get(0));

    // ensure previous tag name cannot be found anymore
    results.clear();
    final CountDownLatch nextCountDownLatch = new CountDownLatch(1);

    searchEngine.searchReferenceBases(new ReferenceBasesSearch("subDivision", new SearchCompletedListener<Collection<ReferenceBase>>() {
      @Override
      public void completed(Collection<ReferenceBase> result) {
        results.addAll(result);
        nextCountDownLatch.countDown();
      }
    }));

    try { nextCountDownLatch.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }

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

    searchEngine.searchReferenceBases(new ReferenceBasesSearch("heph", new SearchCompletedListener<Collection<ReferenceBase>>() {
      @Override
      public void completed(Collection<ReferenceBase> result) {
        results.addAll(result);
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(newSubDivision, results.get(0));

    // ensure previous tag name cannot be found anymore
    results.clear();
    final CountDownLatch nextCountDownLatch = new CountDownLatch(1);

    searchEngine.searchReferenceBases(new ReferenceBasesSearch("find", new SearchCompletedListener<Collection<ReferenceBase>>() {
      @Override
      public void completed(Collection<ReferenceBase> result) {
        results.addAll(result);
        nextCountDownLatch.countDown();
      }
    }));

    try { nextCountDownLatch.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertEquals(0, results.size());
  }

  @Test
  public void searchReferenceSubDivisions() {
    Reference newReference = new Reference("SZ");
    deepThought.addReference(newReference);

    ReferenceSubDivision newSubDivision1 = new ReferenceSubDivision("Die Pharmaindustrie ist schlimmer als die Mafia", "Kritik an Arzneimittelherstellern");
    deepThought.addReferenceSubDivision(newSubDivision1);
    newReference.addSubDivision(newSubDivision1);
    ReferenceSubDivision newSubDivision2 = new ReferenceSubDivision("BND versucht NSA-Aufklärer in die Falle zu locken");
    deepThought.addReferenceSubDivision(newSubDivision2);
    newReference.addSubDivision(newSubDivision2);
    ReferenceSubDivision newSubDivision3 = new ReferenceSubDivision("Privatsphäre kostet extra");
    deepThought.addReferenceSubDivision(newSubDivision3);
    newReference.addSubDivision(newSubDivision3);

    final List<ReferenceBase> results = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.searchReferenceBases(new ReferenceBasesSearch("phar", new SearchCompletedListener<Collection<ReferenceBase>>() {
      @Override
      public void completed(Collection<ReferenceBase> result) {
        results.addAll(result);
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(newSubDivision1, results.get(0));


    results.clear();
    final CountDownLatch countDownLatch2 = new CountDownLatch(1);

    searchEngine.searchReferenceBases(new ReferenceBasesSearch("krit", new SearchCompletedListener<Collection<ReferenceBase>>() {
      @Override
      public void completed(Collection<ReferenceBase> result) {
        results.addAll(result);
        countDownLatch2.countDown();
      }
    }));

    try { countDownLatch2.await(); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(newSubDivision1, results.get(0));

    results.clear();
    final CountDownLatch countDownLatch3 = new CountDownLatch(1);

    searchEngine.searchReferenceBases(new ReferenceBasesSearch("Liebe", new SearchCompletedListener<Collection<ReferenceBase>>() {
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
  public void searchSeriesTitlesOnly_OtherReferenceBaseTypesDontGetFound() {
    SeriesTitle seriesTitle = new SeriesTitle("Test");
    deepThought.addSeriesTitle(seriesTitle);
    Reference reference = new Reference("Test");
    deepThought.addReference(reference);
    ReferenceSubDivision subDivision = new ReferenceSubDivision("Test");
    deepThought.addReferenceSubDivision(subDivision);

    final List<ReferenceBase> results = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.searchReferenceBases(new ReferenceBasesSearch("test", ReferenceBaseType.SeriesTitle, new SearchCompletedListener<Collection<ReferenceBase>>() {
      @Override
      public void completed(Collection<ReferenceBase> result) {
        results.addAll(result);
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(seriesTitle, results.get(0));
  }

  @Test
  public void searchSeriesTitlesOnly_SearchResultIsInCorrectOrder() {
    SeriesTitle seriesTitleGandhiMohandas = new SeriesTitle("Gandhi", "Mohandas");
    SeriesTitle seriesTitleTeresa = new SeriesTitle("Teresa");
    SeriesTitle seriesTitleGandhiMahatma = new SeriesTitle("Gandhi", "Mahatma");
    SeriesTitle seriesTitleMandela = new SeriesTitle("Mandela");
    deepThought.addSeriesTitle(seriesTitleGandhiMohandas);
    deepThought.addSeriesTitle(seriesTitleTeresa);
    deepThought.addSeriesTitle(seriesTitleGandhiMahatma);
    deepThought.addSeriesTitle(seriesTitleMandela);

    final List<ReferenceBase> results = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.searchReferenceBases(new ReferenceBasesSearch("a", ReferenceBaseType.SeriesTitle, new SearchCompletedListener<Collection<ReferenceBase>>() {
      @Override
      public void completed(Collection<ReferenceBase> result) {
        results.addAll(result);
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertEquals(4, results.size());
    Assert.assertEquals(seriesTitleGandhiMahatma, results.get(0));
    Assert.assertEquals(seriesTitleGandhiMohandas, results.get(1));
    Assert.assertEquals(seriesTitleMandela, results.get(2));
    Assert.assertEquals(seriesTitleTeresa, results.get(3));
  }

  @Test
  public void searchReferencesOnly_OtherReferenceBaseTypesDontGetFound() {
    SeriesTitle seriesTitle = new SeriesTitle("Test");
    deepThought.addSeriesTitle(seriesTitle);
    Reference reference = new Reference("Test");
    deepThought.addReference(reference);
    ReferenceSubDivision subDivision = new ReferenceSubDivision("Test");
    deepThought.addReferenceSubDivision(subDivision);

    final List<ReferenceBase> results = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.searchReferenceBases(new ReferenceBasesSearch("test", ReferenceBaseType.Reference, new SearchCompletedListener<Collection<ReferenceBase>>() {
      @Override
      public void completed(Collection<ReferenceBase> result) {
        results.addAll(result);
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(reference, results.get(0));
  }

  @Test
  public void searchReferencesOnly_SearchResultIsInCorrectOrder() {
    Reference referenceGandhiMohandas = new Reference("Gandhi", "Mohandas");
    Reference referenceTeresa = new Reference("Teresa");
    Reference referenceGandhiMahatma = new Reference("Gandhi", "Mahatma");
    Reference referenceMandela = new Reference("Mandela");
    deepThought.addReference(referenceGandhiMohandas);
    deepThought.addReference(referenceTeresa);
    deepThought.addReference(referenceGandhiMahatma);
    deepThought.addReference(referenceMandela);

    final List<ReferenceBase> results = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.searchReferenceBases(new ReferenceBasesSearch("a", ReferenceBaseType.Reference, new SearchCompletedListener<Collection<ReferenceBase>>() {
      @Override
      public void completed(Collection<ReferenceBase> result) {
        results.addAll(result);
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertEquals(4, results.size());
    Assert.assertEquals(referenceGandhiMahatma, results.get(0));
    Assert.assertEquals(referenceGandhiMohandas, results.get(1));
    Assert.assertEquals(referenceMandela, results.get(2));
    Assert.assertEquals(referenceTeresa, results.get(3));
  }

  @Test
  public void searchReferencesOnly_UnrecognizedDateFormatIsSetAsIssue_ReferenceGetsFoundAnyway() {
    Reference reference = new Reference("Test");
    reference.setIssueOrPublishingDate("04 / 2015");
    deepThought.addReference(reference);

    final List<ReferenceBase> results = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.searchReferenceBases(new ReferenceBasesSearch("04", ReferenceBaseType.Reference, new SearchCompletedListener<Collection<ReferenceBase>>() {
      @Override
      public void completed(Collection<ReferenceBase> result) {
        results.addAll(result);
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(reference, results.get(0));
  }

  @Test
  public void searchReferenceSubDivisionsOnly_OtherReferenceBaseTypesDontGetFound() {
    SeriesTitle seriesTitle = new SeriesTitle("Test");
    deepThought.addSeriesTitle(seriesTitle);
    Reference reference = new Reference("Test");
    deepThought.addReference(reference);
    ReferenceSubDivision subDivision = new ReferenceSubDivision("Test");
    deepThought.addReferenceSubDivision(subDivision);

    final List<ReferenceBase> results = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.searchReferenceBases(new ReferenceBasesSearch("test", ReferenceBaseType.ReferenceSubDivision, new SearchCompletedListener<Collection<ReferenceBase>>() {
      @Override
      public void completed(Collection<ReferenceBase> result) {
        results.addAll(result);
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(subDivision, results.get(0));
  }

  @Test
  public void searchReferenceSubDivisionsOnly_SearchResultIsInCorrectOrder() {
    ReferenceSubDivision subDivisionGandhiMohandas = new ReferenceSubDivision("Gandhi", "Mohandas");
    ReferenceSubDivision subDivisionTeresa = new ReferenceSubDivision("Teresa");
    ReferenceSubDivision subDivisionGandhiMahatma = new ReferenceSubDivision("Gandhi", "Mahatma");
    ReferenceSubDivision subDivisionMandela = new ReferenceSubDivision("Mandela");
    deepThought.addReferenceSubDivision(subDivisionGandhiMohandas);
    deepThought.addReferenceSubDivision(subDivisionTeresa);
    deepThought.addReferenceSubDivision(subDivisionGandhiMahatma);
    deepThought.addReferenceSubDivision(subDivisionMandela);

    final List<ReferenceBase> results = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.searchReferenceBases(new ReferenceBasesSearch("a", ReferenceBaseType.ReferenceSubDivision, new SearchCompletedListener<Collection<ReferenceBase>>() {
      @Override
      public void completed(Collection<ReferenceBase> result) {
        results.addAll(result);
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertEquals(4, results.size());
    Assert.assertEquals(subDivisionGandhiMahatma, results.get(0));
    Assert.assertEquals(subDivisionGandhiMohandas, results.get(1));
    Assert.assertEquals(subDivisionMandela, results.get(2));
    Assert.assertEquals(subDivisionTeresa, results.get(3));
  }

  @Test
  public void searchAllReferenceBaseTypes_AllGetFound() {
    SeriesTitle seriesTitle = new SeriesTitle("Test");
    deepThought.addSeriesTitle(seriesTitle);
    Reference reference = new Reference("Test");
    deepThought.addReference(reference);
    ReferenceSubDivision subDivision = new ReferenceSubDivision("Test");
    deepThought.addReferenceSubDivision(subDivision);

    final List<ReferenceBase> results = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.searchReferenceBases(new ReferenceBasesSearch("test", ReferenceBaseType.All, new SearchCompletedListener<Collection<ReferenceBase>>() {
      @Override
      public void completed(Collection<ReferenceBase> result) {
        results.addAll(result);
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertEquals(3, results.size());
    Assert.assertTrue(results.contains(seriesTitle));
    Assert.assertTrue(results.contains(reference));
    Assert.assertTrue(results.contains(subDivision));
  }

  @Test
  public void searchAllReferenceBaseTypes_SeriesTitlesHaveDifferentSubTitles_TheyAreSortedCorrectly() {
    SeriesTitle seriesTitleSzJetzt = new SeriesTitle("SZ", "Jetzt");
    SeriesTitle seriesTitleSzMagazin = new SeriesTitle("SZ", "Magazin");
    SeriesTitle seriesTitleSz = new SeriesTitle("SZ", "");
    deepThought.addSeriesTitle(seriesTitleSzJetzt);
    deepThought.addSeriesTitle(seriesTitleSzMagazin);
    deepThought.addSeriesTitle(seriesTitleSz);

    final List<ReferenceBase> results = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.searchReferenceBases(new ReferenceBasesSearch("sz", ReferenceBaseType.All, new SearchCompletedListener<Collection<ReferenceBase>>() {
      @Override
      public void completed(Collection<ReferenceBase> result) {
        results.addAll(result);
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertEquals(3, results.size());

    // the two SeriesTitle will be retrieved first, in correct alphabetical order
    Assert.assertEquals(seriesTitleSz, results.get(0));
    Assert.assertEquals(seriesTitleSzJetzt, results.get(1));
    Assert.assertEquals(seriesTitleSzMagazin, results.get(2));
  }

  @Test
  public void searchAllReferenceBaseTypes_ReferencesHaveDifferentSubTitles_TheyAreSortedCorrectly() {
    Reference referenceSzJetzt = new Reference("SZ", "Jetzt");
    Reference referenceSzMagazin = new Reference("SZ", "Magazin");
    Reference referenceSz = new Reference("SZ", "");
    deepThought.addReference(referenceSzJetzt);
    deepThought.addReference(referenceSzMagazin);
    deepThought.addReference(referenceSz);

    final List<ReferenceBase> results = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.searchReferenceBases(new ReferenceBasesSearch("sz", ReferenceBaseType.All, new SearchCompletedListener<Collection<ReferenceBase>>() {
      @Override
      public void completed(Collection<ReferenceBase> result) {
        results.addAll(result);
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertEquals(3, results.size());

    // the two SeriesTitle will be retrieved first, in correct alphabetical order
    Assert.assertEquals(referenceSz, results.get(0));
    Assert.assertEquals(referenceSzJetzt, results.get(1));
    Assert.assertEquals(referenceSzMagazin, results.get(2));
  }

  @Test
  public void searchAllReferenceBaseTypes_ReferenceSubDivisionsHaveDifferentSubTitles_TheyAreSortedCorrectly() {
    ReferenceSubDivision subDivisionSzJetzt = new ReferenceSubDivision("SZ", "Jetzt");
    ReferenceSubDivision subDivisionSzMagazin = new ReferenceSubDivision("SZ", "Magazin");
    ReferenceSubDivision subDivisionSz = new ReferenceSubDivision("SZ", "");
    deepThought.addReferenceSubDivision(subDivisionSzJetzt);
    deepThought.addReferenceSubDivision(subDivisionSzMagazin);
    deepThought.addReferenceSubDivision(subDivisionSz);

    final List<ReferenceBase> results = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.searchReferenceBases(new ReferenceBasesSearch("sz", ReferenceBaseType.All, new SearchCompletedListener<Collection<ReferenceBase>>() {
      @Override
      public void completed(Collection<ReferenceBase> result) {
        results.addAll(result);
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertEquals(3, results.size());

    // the two SeriesTitle will be retrieved first, in correct alphabetical order
    Assert.assertEquals(subDivisionSz, results.get(0));
    Assert.assertEquals(subDivisionSzJetzt, results.get(1));
    Assert.assertEquals(subDivisionSzMagazin, results.get(2));
  }

  @Test
  public void searchAllReferenceBaseTypes_TheyAreSortedCorrectly() {
    SeriesTitle seriesTitleSzMagazin = new SeriesTitle("SZ Magazin");
    SeriesTitle seriesTitleSz = new SeriesTitle("SZ");
    deepThought.addSeriesTitle(seriesTitleSzMagazin);
    deepThought.addSeriesTitle(seriesTitleSz);

    Reference reference_SZ_Magazin_1983_10_20 = new Reference();
    reference_SZ_Magazin_1983_10_20.setIssueOrPublishingDate("10/20/1983");
    Reference reference_SZ_1988_03_27 = new Reference();
    reference_SZ_1988_03_27.setIssueOrPublishingDate("03/27/1988");
    Reference reference_SZ_1983_10_20 = new Reference();
    reference_SZ_1983_10_20.setIssueOrPublishingDate("10/20/1983");
    Reference reference_SZ_Magazin_2222_12_31 = new Reference();
    reference_SZ_Magazin_2222_12_31.setIssueOrPublishingDate("12/31/2222");
    deepThought.addReference(reference_SZ_Magazin_1983_10_20);
    deepThought.addReference(reference_SZ_1988_03_27);
    deepThought.addReference(reference_SZ_1983_10_20);
    deepThought.addReference(reference_SZ_Magazin_2222_12_31);

    seriesTitleSz.addSerialPart(reference_SZ_1988_03_27);
    seriesTitleSz.addSerialPart(reference_SZ_1983_10_20);
    seriesTitleSzMagazin.addSerialPart(reference_SZ_Magazin_1983_10_20);
    seriesTitleSzMagazin.addSerialPart(reference_SZ_Magazin_2222_12_31); // a Date starting with 01. may not be ordered before 20. when it's in a later year

    ReferenceSubDivision subDivisionKarteDerSchande = new ReferenceSubDivision("Karte der Schande");
    ReferenceSubDivision subDivisionSkandal = new ReferenceSubDivision("Dieser Skandal sollte vertuscht werden");
    ReferenceSubDivision subDivisionPharmaindustrie = new ReferenceSubDivision("Die Pharmaindustrie ist schlimmer als die Mafia");
    ReferenceSubDivision subDivisionEier = new ReferenceSubDivision("Das Zerquetschen von Eiern");
    ReferenceSubDivision subDivisionFuture = new ReferenceSubDivision("The Future is now");
    deepThought.addReferenceSubDivision(subDivisionEier);
    deepThought.addReferenceSubDivision(subDivisionKarteDerSchande);
    deepThought.addReferenceSubDivision(subDivisionSkandal);
    deepThought.addReferenceSubDivision(subDivisionPharmaindustrie);
    deepThought.addReferenceSubDivision(subDivisionFuture);

    reference_SZ_1983_10_20.addSubDivision(subDivisionSkandal);
    reference_SZ_1983_10_20.addSubDivision(subDivisionPharmaindustrie);
    reference_SZ_1988_03_27.addSubDivision(subDivisionKarteDerSchande);
    reference_SZ_Magazin_1983_10_20.addSubDivision(subDivisionEier);
    reference_SZ_Magazin_2222_12_31.addSubDivision(subDivisionFuture);

    final List<ReferenceBase> results = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.searchReferenceBases(new ReferenceBasesSearch("sz", ReferenceBaseType.All, new SearchCompletedListener<Collection<ReferenceBase>>() {
      @Override
      public void completed(Collection<ReferenceBase> result) {
        results.addAll(result);
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertEquals(11, results.size());

    // the two SeriesTitle will be retrieved first, in correct alphabetical order
    Assert.assertEquals(seriesTitleSz, results.get(0));
    Assert.assertEquals(seriesTitleSzMagazin, results.get(1));

    // then the References will be retrieved, in correct alphabetical and temporal order
    Assert.assertEquals(reference_SZ_1988_03_27, results.get(2));
    Assert.assertEquals(reference_SZ_1983_10_20, results.get(3));
    Assert.assertEquals(reference_SZ_Magazin_2222_12_31, results.get(4));
    Assert.assertEquals(reference_SZ_Magazin_1983_10_20, results.get(5));

    // and at least the ReferenceSubDivisions, also in  correct alphabetical and temporal order
    Assert.assertEquals(subDivisionKarteDerSchande, results.get(6));
    Assert.assertEquals(subDivisionPharmaindustrie, results.get(7));
    Assert.assertEquals(subDivisionSkandal, results.get(8));
    Assert.assertEquals(subDivisionFuture, results.get(9));
    Assert.assertEquals(subDivisionEier, results.get(10));
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

    try { countDownLatch.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertEquals(1, entriesHavingFilteredTags.size());
    Assert.assertEquals(entryWithTags1, new ArrayList<Entry>(entriesHavingFilteredTags).get(0));
    Assert.assertEquals(2, tagsOnEntriesContainingFilteredTags.size());

    tagsToFilterFor.clear();
    tagsToFilterFor.add(tag2);
    tagsToFilterFor.add(tag3);
    entriesHavingFilteredTags.clear();
    tagsOnEntriesContainingFilteredTags.clear();
    final CountDownLatch countDownLatch2 = new CountDownLatch(1);

    searchEngine.findAllEntriesHavingTheseTags(tagsToFilterFor, new SearchCompletedListener<FindAllEntriesHavingTheseTagsResult>() {
      @Override
      public void completed(FindAllEntriesHavingTheseTagsResult results) {
        entriesHavingFilteredTags.addAll(results.getEntriesHavingFilteredTags());
        tagsOnEntriesContainingFilteredTags.addAll(results.getTagsOnEntriesContainingFilteredTags());
        countDownLatch2.countDown();
      }
    });

    try { countDownLatch2.await(); } catch(Exception ex) { }

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

    // TODO: is this really necessary?
//    searchEngine.indexEntity(entryWithoutTags1);
//    searchEngine.indexEntity(entryWithTags1);
//    searchEngine.indexEntity(entryWithoutTags2);
//    searchEngine.indexEntity(entryWithTags2);
//    searchEngine.indexEntity(entryWithoutTags3);

    final List<Entry> entriesWithoutTags = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.getEntriesWithTag(deepThought.EntriesWithoutTagsSystemTag(), new SearchCompletedListener<Collection<Entry>>() {
      @Override
      public void completed(Collection<Entry> results) {
        entriesWithoutTags.addAll(results);
        countDownLatch.countDown();
      }
    });

    try { countDownLatch.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }
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

    final List<Entry> entriesWithTag1 = new ArrayList<>();
    final Set<Tag> tagsOnEntriesWithTag1 = new HashSet<>();
    final CountDownLatch countDownLatch1 = new CountDownLatch(1);

    searchEngine.findAllEntriesHavingTheseTags(Arrays.asList(tag1), new SearchCompletedListener<FindAllEntriesHavingTheseTagsResult>() {
      @Override
      public void completed(FindAllEntriesHavingTheseTagsResult results) {
        entriesWithTag1.addAll(results.getEntriesHavingFilteredTags());
        tagsOnEntriesWithTag1.addAll(results.getTagsOnEntriesContainingFilteredTags());
        countDownLatch1.countDown();
      }
    });

    try { countDownLatch1.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertEquals(1, entriesWithTag1.size());

    Assert.assertTrue(entriesWithTag1.contains(entryWithTags1));
    Assert.assertFalse(entriesWithTag1.contains(entryWithTags2));
    Assert.assertFalse(entriesWithTag1.contains(entryWithoutTags1));
    Assert.assertFalse(entriesWithTag1.contains(entryWithoutTags2));
    Assert.assertFalse(entriesWithTag1.contains(entryWithoutTags3));

    final Collection<Entry> entriesWithTag2 = new ArrayList<>();
    final Set<Tag> tagsOnEntriesWithTag2 = new HashSet<>();
    final CountDownLatch countDownLatch2 = new CountDownLatch(1);

    searchEngine.findAllEntriesHavingTheseTags(Arrays.asList(tag2), new SearchCompletedListener<FindAllEntriesHavingTheseTagsResult>() {
      @Override
      public void completed(FindAllEntriesHavingTheseTagsResult results) {
        entriesWithTag2.addAll(results.getEntriesHavingFilteredTags());
        tagsOnEntriesWithTag2.addAll(results.getTagsOnEntriesContainingFilteredTags());
        countDownLatch2.countDown();
      }
    });

    try { countDownLatch2.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertEquals(2, entriesWithTag2.size());

    Assert.assertTrue(entriesWithTag2.contains(entryWithTags1));
    Assert.assertTrue(entriesWithTag2.contains(entryWithTags2));
    Assert.assertFalse(entriesWithTag2.contains(entryWithoutTags1));
    Assert.assertFalse(entriesWithTag2.contains(entryWithoutTags2));
    Assert.assertFalse(entriesWithTag2.contains(entryWithoutTags3));

    final Collection<Entry> entriesWithTag3 = new ArrayList<>();
    final Set<Tag> tagsOnEntriesWithTag3 = new HashSet<>();
    final CountDownLatch countDownLatch3 = new CountDownLatch(1);

    searchEngine.findAllEntriesHavingTheseTags(Arrays.asList(tag3), new SearchCompletedListener<FindAllEntriesHavingTheseTagsResult>() {
      @Override
      public void completed(FindAllEntriesHavingTheseTagsResult results) {
        entriesWithTag3.addAll(results.getEntriesHavingFilteredTags());
        tagsOnEntriesWithTag3.addAll(results.getTagsOnEntriesContainingFilteredTags());
        countDownLatch3.countDown();
      }
    });

    try { countDownLatch3.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertEquals(1, entriesWithTag3.size());

    Assert.assertFalse(entriesWithTag3.contains(entryWithTags1));
    Assert.assertTrue(entriesWithTag3.contains(entryWithTags2));
    Assert.assertFalse(entriesWithTag3.contains(entryWithoutTags1));
    Assert.assertFalse(entriesWithTag3.contains(entryWithoutTags2));
    Assert.assertFalse(entriesWithTag3.contains(entryWithoutTags3));

    final Collection<Entry> entriesWithTags2And3 = new ArrayList<>();
    final Set<Tag> tagsOnEntriesWithTag2And3 = new HashSet<>();
    final CountDownLatch countDownLatch4 = new CountDownLatch(1);

    searchEngine.findAllEntriesHavingTheseTags(Arrays.asList(tag2, tag3), new SearchCompletedListener<FindAllEntriesHavingTheseTagsResult>() {
      @Override
      public void completed(FindAllEntriesHavingTheseTagsResult results) {
        entriesWithTags2And3.addAll(results.getEntriesHavingFilteredTags());
        tagsOnEntriesWithTag2And3.addAll(results.getTagsOnEntriesContainingFilteredTags());
        countDownLatch4.countDown();
      }
    });

    try { countDownLatch4.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertEquals(1, entriesWithTags2And3.size());

    Assert.assertFalse(entriesWithTags2And3.contains(entryWithTags1));
    Assert.assertTrue(entriesWithTags2And3.contains(entryWithTags2));
    Assert.assertFalse(entriesWithTags2And3.contains(entryWithoutTags1));
    Assert.assertFalse(entriesWithTags2And3.contains(entryWithoutTags2));
    Assert.assertFalse(entriesWithTags2And3.contains(entryWithoutTags3));
  }

  @Test
  public void findEntriesWithTags_SearchTermIsSet() throws IOException, ParseException {
    Tag tag1 = new Tag("tag1");
    Tag tag2 = new Tag("tag2");
    Tag tag3 = new Tag("tag3");
    deepThought.addTag(tag1);
    deepThought.addTag(tag2);
    deepThought.addTag(tag3);

    Entry entry1 = new Entry("one");
    deepThought.addEntry(entry1);
    entry1.addTag(tag1);
    entry1.addTag(tag2);
    entry1.addTag(tag3);

    final Collection<Entry> entriesWithSearchTermTag = new ArrayList<>();
    final Set<Tag> tagsOnEntriesWithSearchTermTag = new HashSet<>();
    final CountDownLatch countDownLatch1 = new CountDownLatch(1);

    // must find all Tags as all contain search term 'tag'
    searchEngine.findAllEntriesHavingTheseTags(Arrays.asList(tag2, tag3), "tag", new SearchCompletedListener<FindAllEntriesHavingTheseTagsResult>() {
      @Override
      public void completed(FindAllEntriesHavingTheseTagsResult results) {
        entriesWithSearchTermTag.addAll(results.getEntriesHavingFilteredTags());
        tagsOnEntriesWithSearchTermTag.addAll(results.getTagsOnEntriesContainingFilteredTags());
        countDownLatch1.countDown();
      }
    });

    try { countDownLatch1.await(); } catch(Exception ex) { }

    Assert.assertEquals(3, tagsOnEntriesWithSearchTermTag.size());
    Assert.assertEquals(1, entriesWithSearchTermTag.size());


    final Collection<Entry> entriesWithSearchTermSwag = new ArrayList<>();
    final Set<Tag> tagsOnEntriesWithSearchTermSwag = new HashSet<>();
    final CountDownLatch countDownLatch2 = new CountDownLatch(1);

    // no entries and tags may be found as no tag contains search term 'swag'
    searchEngine.findAllEntriesHavingTheseTags(Arrays.asList(tag2, tag3), "swag", new SearchCompletedListener<FindAllEntriesHavingTheseTagsResult>() {
      @Override
      public void completed(FindAllEntriesHavingTheseTagsResult results) {
        entriesWithSearchTermSwag.addAll(results.getEntriesHavingFilteredTags());
        tagsOnEntriesWithSearchTermSwag.addAll(results.getTagsOnEntriesContainingFilteredTags());
        countDownLatch2.countDown();
      }
    });

    try { countDownLatch2.await(); } catch(Exception ex) { }

    Assert.assertEquals(0, tagsOnEntriesWithSearchTermSwag.size());
    Assert.assertEquals(1, entriesWithSearchTermSwag.size());
  }

  @Test
  public void findEntriesWithTags_TagIsOnEntryButDoesNotMatchSearchTerm() throws IOException, ParseException {
    Tag tag1 = new Tag("tag");
    Tag tag2 = new Tag("swag");
    deepThought.addTag(tag1);
    deepThought.addTag(tag2);

    Entry entry1 = new Entry("one");
    deepThought.addEntry(entry1);
    entry1.addTag(tag1);
    entry1.addTag(tag2);

    final Collection<Entry> entriesWithSearchTermTag = new ArrayList<>();
    final Set<Tag> tagsOnEntriesWithSearchTermTag = new HashSet<>();
    final CountDownLatch countDownLatch1 = new CountDownLatch(1);

    searchEngine.findAllEntriesHavingTheseTags(Arrays.asList(tag1, tag2), "tag", new SearchCompletedListener<FindAllEntriesHavingTheseTagsResult>() {
      @Override
      public void completed(FindAllEntriesHavingTheseTagsResult results) {
        entriesWithSearchTermTag.addAll(results.getEntriesHavingFilteredTags());
        tagsOnEntriesWithSearchTermTag.addAll(results.getTagsOnEntriesContainingFilteredTags());
        countDownLatch1.countDown();
      }
    });

    try { countDownLatch1.await(); } catch(Exception ex) { }

    Assert.assertEquals(1, tagsOnEntriesWithSearchTermTag.size()); // only tag1 must be found (tag2's name doesn't match search term 'tag')
    Assert.assertEquals(tag1, new ArrayList<Tag>(tagsOnEntriesWithSearchTermTag).get(0));
    Assert.assertEquals(1, entriesWithSearchTermTag.size());
  }

  @Test
  public void findEntriesWithTags_SearchTermIsSet_ButNotAllTagsMatchingSearchTermAreOnResultEntries() throws IOException, ParseException {
    Tag tag1 = new Tag("tag1");
    Tag tag2 = new Tag("tag2");
    Tag tag3 = new Tag("tag3");
    Tag tag4 = new Tag("tag4");
    deepThought.addTag(tag1);
    deepThought.addTag(tag2);
    deepThought.addTag(tag3);
    deepThought.addTag(tag4);

    Entry entry1 = new Entry("one");
    deepThought.addEntry(entry1);
    entry1.addTag(tag1);
    entry1.addTag(tag4); // only tag1 and tag4 are on result Entry

    final Collection<Entry> entriesWithSearchTermTag = new ArrayList<>();
    final List<Tag> tagsOnEntriesWithSearchTermTag = new ArrayList<>();
    final CountDownLatch countDownLatch1 = new CountDownLatch(1);

    searchEngine.findAllEntriesHavingTheseTags(Arrays.asList(tag1), "tag", new SearchCompletedListener<FindAllEntriesHavingTheseTagsResult>() {
      @Override
      public void completed(FindAllEntriesHavingTheseTagsResult results) {
        entriesWithSearchTermTag.addAll(results.getEntriesHavingFilteredTags());
        tagsOnEntriesWithSearchTermTag.addAll(results.getTagsOnEntriesContainingFilteredTags());
        countDownLatch1.countDown();
      }
    });

    try { countDownLatch1.await(); } catch(Exception ex) { }

    Assert.assertEquals(2, tagsOnEntriesWithSearchTermTag.size());
    Assert.assertEquals(1, entriesWithSearchTermTag.size());

    List<Tag> tagsResultList = new ArrayList<Tag>(tagsOnEntriesWithSearchTermTag);
    Assert.assertEquals(tag1, tagsResultList.get(0));
    Assert.assertEquals(tag4, tagsResultList.get(1));
  }

  @Test
  public void findEntriesWithTags_SearchTermIsSet_SearchResultsAreInCorrectAlphabeticalOrder() throws IOException, ParseException {
    Tag tag1 = new Tag("tag");
    Tag tag2 = new Tag("swag");
    Tag tag3 = new Tag("Ztag");
    Tag tag4 = new Tag("Atag");
    deepThought.addTag(tag1);
    deepThought.addTag(tag2);
    deepThought.addTag(tag3);
    deepThought.addTag(tag4);

    Entry entry1 = new Entry("one");
    deepThought.addEntry(entry1);
    entry1.addTag(tag1);
    entry1.addTag(tag2);
    entry1.addTag(tag3);
    entry1.addTag(tag4);

    final Collection<Entry> entriesWithSearchTermTag = new ArrayList<>();
    final List<Tag> tagsOnEntriesWithSearchTermTag = new ArrayList<>();
    final CountDownLatch countDownLatch1 = new CountDownLatch(1);

    searchEngine.findAllEntriesHavingTheseTags(Arrays.asList(tag1, tag2), "tag", new SearchCompletedListener<FindAllEntriesHavingTheseTagsResult>() {
      @Override
      public void completed(FindAllEntriesHavingTheseTagsResult results) {
        entriesWithSearchTermTag.addAll(results.getEntriesHavingFilteredTags());
        tagsOnEntriesWithSearchTermTag.addAll(results.getTagsOnEntriesContainingFilteredTags());
        countDownLatch1.countDown();
      }
    });

    try { countDownLatch1.await(); } catch(Exception ex) { }

    Assert.assertEquals(3, tagsOnEntriesWithSearchTermTag.size());
    Assert.assertEquals(1, entriesWithSearchTermTag.size());

    List<Tag> tagsResultList = new ArrayList<Tag>(tagsOnEntriesWithSearchTermTag);
    Assert.assertEquals(tag4, tagsResultList.get(0)); // Atag must be first result
    Assert.assertEquals(tag1, tagsResultList.get(1)); // tag second
    Assert.assertEquals(tag3, tagsResultList.get(2)); // and Ztag last
  }


  @Test
  public void addFile_FileGetsIndexed() {
    FileLink newFile = new FileLink("http://www.philosophy.com", "enlightenment");
    deepThought.addFile(newFile);

    final List<FileLink> results = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.searchFiles(new FilesSearch("enli", new SearchCompletedListener<Collection<FileLink>>() {
      @Override
      public void completed(Collection<FileLink> result) {
        results.addAll(result);
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(newFile, results.get(0));
  }

  @Test
  public void addFile_SearchOnlyFileName() {
    FileLink newFile = new FileLink("http://www.philosophy.com", "enlightenment");
    FileLink testDummy = new FileLink("http://www.enlightenment.com", "but not in name");
    testDummy.setDescription("enlightenment");
    deepThought.addFile(newFile);
    deepThought.addFile(testDummy);

    final List<FileLink> results = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.searchFiles(new FilesSearch("enli", true, false, false, new SearchCompletedListener<Collection<FileLink>>() {
      @Override
      public void completed(Collection<FileLink> result) {
        results.addAll(result);
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(newFile, results.get(0));
  }

  @Test
  public void addFile_SearchOnlyUri() {
    FileLink newFile = new FileLink("http://www.philosophy.com", "enlightenment");
    FileLink testDummy = new FileLink("http://www.enlightenment.com", "philosophy");
    testDummy.setDescription("philosophy");
    deepThought.addFile(newFile);
    deepThought.addFile(testDummy);

    final List<FileLink> results = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.searchFiles(new FilesSearch("philo", false, true, false, new SearchCompletedListener<Collection<FileLink>>() {
      @Override
      public void completed(Collection<FileLink> result) {
        results.addAll(result);
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(newFile, results.get(0));
  }

  @Test
  public void addFile_SearchOnlyFileDescripton() {
    FileLink newFile = new FileLink("http://www.philosophy.com", "enlightenment");
    newFile.setDescription("Sokrates");
    FileLink testDummy = new FileLink("http://www.sokrates.com", "Sokrates");
    testDummy.setDescription("I know my nothing knowing");
    deepThought.addFile(newFile);
    deepThought.addFile(testDummy);

    final List<FileLink> results = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.searchFiles(new FilesSearch("sokr", false, false, true, new SearchCompletedListener<Collection<FileLink>>() {
      @Override
      public void completed(Collection<FileLink> result) {
        results.addAll(result);
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(newFile, results.get(0));
  }

  @Test
  public void addFile_SearchOnlyEmbeddableInHtmlFiles() {
    FileLink senseOfLifeResultPic = new FileLink("SenseOfLifeResult.jpeg", "We finally found the Sense of Life, this picture shows it!"); // embeddable in HTML
    FileLink senseOfLifeResearchData = new FileLink("SenseOfLifeResearches.bin", "Researches for Sense of Life binary data");
    deepThought.addFile(senseOfLifeResultPic);
    deepThought.addFile(senseOfLifeResearchData);

    final List<FileLink> results = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    FilesSearch filesSearch = new FilesSearch("sense", new SearchCompletedListener<Collection<FileLink>>() {
      @Override
      public void completed(Collection<FileLink> result) {
        results.addAll(result);
        countDownLatch.countDown();
      }
    });
    filesSearch.setInHtmlEmbeddableFilesOnly(true);
    searchEngine.searchFiles(filesSearch);

    try { countDownLatch.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(senseOfLifeResultPic, results.get(0));
  }

  @Test
  public void updateFile_SearchFindsUpdatedFile() {
    FileLink newFile = new FileLink("http://www.philosophy.com", "Enlightenment");
    deepThought.addFile(newFile);

    newFile.setName("Sokrates");

    final List<FileLink> results = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.searchFiles(new FilesSearch("Sok", new SearchCompletedListener<Collection<FileLink>>() {
      @Override
      public void completed(Collection<FileLink> result) {
        results.addAll(result);
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(newFile, results.get(0));

    // ensure previous tag name cannot be found anymore
    results.clear();
    final CountDownLatch nextCountDownLatch = new CountDownLatch(1);

    searchEngine.searchFiles(new FilesSearch("enl", new SearchCompletedListener<Collection<FileLink>>() {
      @Override
      public void completed(Collection<FileLink> result) {
        results.addAll(result);
        nextCountDownLatch.countDown();
      }
    }));

    try { nextCountDownLatch.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertEquals(0, results.size());
  }

  @Test
  public void searchFiles_SearchResultsAreInCorrectAlphabeticalOrder() {
    FileLink file1 = new FileLink("/same/parent/directory/any", "Vain and insane");
    FileLink file2 = new FileLink("/same/parent/directory/different", "Same");
    FileLink file3 = new FileLink("/same/parent/directory/but", "Same");
    FileLink file4 = new FileLink("/same/parent/directory/but", "Not the Same");

    deepThought.addFile(file1);
    deepThought.addFile(file2);
    deepThought.addFile(file3);
    deepThought.addFile(file4);

    final List<FileLink> results = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.searchFiles(new FilesSearch("sa", new SearchCompletedListener<Collection<FileLink>>() {
      @Override
      public void completed(Collection<FileLink> result) {
        results.addAll(result);
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertEquals(4, results.size());

    Assert.assertEquals(file4, results.get(0));
    Assert.assertEquals(file3, results.get(1));
    Assert.assertEquals(file2, results.get(2));
    Assert.assertEquals(file1, results.get(3));
  }

  @Test
  public void deleteFile_SearchDoesNotFindFileAnymore() {
    FileLink newFile = new FileLink("http://www.philosophy.com", "enlightenment");
    deepThought.addFile(newFile);

    deepThought.removeFile(newFile);

    final List<FileLink> results = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.searchFiles(new FilesSearch("enli", new SearchCompletedListener<Collection<FileLink>>() {
      @Override
      public void completed(Collection<FileLink> result) {
        results.addAll(result);
        countDownLatch.countDown();
      }
    }));

    try { countDownLatch.await(DEFAULT_WAIT_TIME, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertEquals(0, results.size());
  }

}

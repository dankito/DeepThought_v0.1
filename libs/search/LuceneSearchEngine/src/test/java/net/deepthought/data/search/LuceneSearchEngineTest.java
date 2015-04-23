package net.deepthought.data.search;

import net.deepthought.Application;
import net.deepthought.data.TestApplicationConfiguration;
import net.deepthought.data.helper.MockEntityManager;
import net.deepthought.data.helper.TestDependencyResolver;
import net.deepthought.data.model.DeepThought;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.Person;
import net.deepthought.data.model.Reference;
import net.deepthought.data.model.ReferenceBase;
import net.deepthought.data.model.ReferenceSubDivision;
import net.deepthought.data.model.SeriesTitle;
import net.deepthought.data.model.Tag;

import org.apache.lucene.queryparser.classic.ParseException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
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
    Application.instantiate(new TestApplicationConfiguration(), new TestDependencyResolver(new MockEntityManager()) {
      @Override
      public ISearchEngine createSearchEngine() {
        try {
          LuceneSearchEngineTest.this.searchEngine = new LuceneSearchEngine();
        } catch(Exception ex) {
          log.error("Could not create LuceneSearchEngine", ex);
        }
        return LuceneSearchEngineTest.this.searchEngine;
      }
    });

    deepThought = Application.getDeepThought();

    searchEngine.deleteIndex();
  }

  @After
  public void tearDown() {
    searchEngine.close();
  }


  @Test
  public void addTag_TagGetsIndexed() {
    Tag newTag = new Tag("tag");
    deepThought.addTag(newTag);

    final List<Tag> results = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.filterTags(new Search<Tag>("Tag", result -> {
      results.addAll(result);
      countDownLatch.countDown();
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

    searchEngine.filterTags(new Search<Tag>("swag", result -> {
      results.addAll(result);
      countDownLatch.countDown();
    }));

    try { countDownLatch.await(); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(newTag, results.get(0));

    // ensure previous tag name cannot be found anymore
    results.clear();
    final CountDownLatch nextCountDownLatch = new CountDownLatch(1);

    searchEngine.filterTags(new Search<Tag>("tag", result -> {
      results.addAll(result);
      nextCountDownLatch.countDown();
    }));

    try { nextCountDownLatch.await(); } catch(Exception ex) { }

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

    searchEngine.filterPersons(new Search<Person>("last", result -> {
      results.addAll(result);
      countDownLatch.countDown();
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

    searchEngine.filterPersons(new Search<Person>("zeus", result -> {
      results.addAll(result);
      countDownLatch.countDown();
    }));

    try { countDownLatch.await(); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(newPerson, results.get(0));

    // ensure previous tag name cannot be found anymore
    results.clear();
    final CountDownLatch nextCountDownLatch = new CountDownLatch(1);

    searchEngine.filterPersons(new Search<Person>("last", result -> {
      results.addAll(result);
      nextCountDownLatch.countDown();
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

    searchEngine.filterPersons(new Search<Person>("giovanni", result -> {
      results.addAll(result);
      countDownLatch.countDown();
    }));

    try { countDownLatch.await(); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(newPerson, results.get(0));

    // ensure previous tag name cannot be found anymore
    results.clear();
    final CountDownLatch nextCountDownLatch = new CountDownLatch(1);

    searchEngine.filterPersons(new Search<Person>("first", result -> {
      results.addAll(result);
      nextCountDownLatch.countDown();
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

    searchEngine.filterPersons(new Search<Person>("gan, mah", result -> {
      results.addAll(result);
      countDownLatch.countDown();
    }));

    try { countDownLatch.await(); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(newPerson, results.get(0));

    // ensure Person is not found if First name is wrong
    results.clear();
    final CountDownLatch nextCountDownLatch = new CountDownLatch(1);

    searchEngine.filterPersons(new Search<Person>("Gandhi, Mohandas", result -> {
      results.addAll(result);
      nextCountDownLatch.countDown();
    }));

    try { nextCountDownLatch.await(); } catch(Exception ex) { }

    Assert.assertEquals(0, results.size());
  }


  @Test
  public void addSeriesTitle_SeriesTitleGetsIndexed() {
    SeriesTitle newSeriesTitle = new SeriesTitle("series");
    deepThought.addSeriesTitle(newSeriesTitle);

    final List<SeriesTitle> results = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.filterReferenceBases(new Search<SeriesTitle>("Series", result -> {
      results.addAll(result);
      countDownLatch.countDown();
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

    searchEngine.filterReferenceBases(new Search<ReferenceBase>("aphrodite", result -> {
      results.addAll(result);
      countDownLatch.countDown();
    }));

    try { countDownLatch.await(); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(newSeriesTitle, results.get(0));

    // ensure previous tag name cannot be found anymore
    results.clear();
    final CountDownLatch nextCountDownLatch = new CountDownLatch(1);

    searchEngine.filterReferenceBases(new Search<ReferenceBase>("series", result -> {
      results.addAll(result);
      nextCountDownLatch.countDown();
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

    searchEngine.filterReferenceBases(new Search<ReferenceBase>("subtitle", result -> {
      results.addAll(result);
      countDownLatch.countDown();
    }));

    try { countDownLatch.await(); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(newSeriesTitle, results.get(0));

    // ensure previous tag name cannot be found anymore
    results.clear();
    final CountDownLatch nextCountDownLatch = new CountDownLatch(1);

    searchEngine.filterReferenceBases(new Search<ReferenceBase>("find", result -> {
      results.addAll(result);
      nextCountDownLatch.countDown();
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

    searchEngine.filterReferenceBases(new Search<ReferenceBase>("sz", result -> {
      results.addAll(result);
      countDownLatch.countDown();
    }));

    try { countDownLatch.await(); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(newSeriesTitle, results.get(0));


    results.clear();
    final CountDownLatch countDownLatch2 = new CountDownLatch(1);

    searchEngine.filterReferenceBases(new Search<ReferenceBase>("untert", result -> {
      results.addAll(result);
      countDownLatch2.countDown();
    }));

    try { countDownLatch2.await(); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(newSeriesTitle, results.get(0));


    results.clear();
    final CountDownLatch countDownLatch3 = new CountDownLatch(1);

    searchEngine.filterReferenceBases(new Search<ReferenceBase>("unterd", result -> {
      results.addAll(result);
      countDownLatch3.countDown();
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

    searchEngine.filterReferenceBases(new Search<Reference>("Reference", result -> {
      results.addAll(result);
      countDownLatch.countDown();
    }));

    try { countDownLatch.await(); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(newReference, results.get(0));
  }

  @Test
  public void updateReferenceTitle_SearchFindsUpdatedReference() {
    Reference newReference = new Reference("reference");
    deepThought.addReference(newReference);

    newReference.setTitle("Aphrodite");

    final List<Reference> results = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.filterReferenceBases(new Search<Reference>("aphrodite", result -> {
      results.addAll(result);
      countDownLatch.countDown();
    }));

    try { countDownLatch.await(); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(newReference, results.get(0));

    // ensure previous tag name cannot be found anymore
    results.clear();
    final CountDownLatch nextCountDownLatch = new CountDownLatch(1);

    searchEngine.filterReferenceBases(new Search<Reference>("reference", result -> {
      results.addAll(result);
      nextCountDownLatch.countDown();
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

    searchEngine.filterReferenceBases(new Search<Reference>("heph", result -> {
      results.addAll(result);
      countDownLatch.countDown();
    }));

    try { countDownLatch.await(); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(newReference, results.get(0));

    // ensure previous tag name cannot be found anymore
    results.clear();
    final CountDownLatch nextCountDownLatch = new CountDownLatch(1);

    searchEngine.filterReferenceBases(new Search<Reference>("find", result -> {
      results.addAll(result);
      nextCountDownLatch.countDown();
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

    searchEngine.filterReferenceBases(new Search<Reference>("2010", result -> {
      results.addAll(result);
      countDownLatch.countDown();
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

    searchEngine.filterReferenceBases(new Search<Reference>("denk", result -> {
      results.addAll(result);
      countDownLatch.countDown();
    }));

    try { countDownLatch.await(); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(newReference, results.get(0));


    results.clear();
    final CountDownLatch countDownLatch2 = new CountDownLatch(1);

    searchEngine.filterReferenceBases(new Search<Reference>("wider", result -> {
      results.addAll(result);
      countDownLatch2.countDown();
    }));

    try { countDownLatch2.await(); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(newReference, results.get(0));


    results.clear();
    final CountDownLatch countDownLatch3 = new CountDownLatch(1);

    searchEngine.filterReferenceBases(new Search<Reference>("2012", result -> {
      results.addAll(result);
      countDownLatch3.countDown();
    }));

    try { countDownLatch3.await(); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(newReference, results.get(0));


    results.clear();
    final CountDownLatch countDownLatch4 = new CountDownLatch(1);

    searchEngine.filterReferenceBases(new Search<Reference>("Liebe", result -> {
      results.addAll(result);
      countDownLatch4.countDown();
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

    searchEngine.filterReferenceBases(new Search<ReferenceBase>("SUB", result -> {
      results.addAll(result);
      countDownLatch.countDown();
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

    searchEngine.filterReferenceBases(new Search<ReferenceBase>("aphrodite", result -> {
      results.addAll(result);
      countDownLatch.countDown();
    }));

    try { countDownLatch.await(); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(newSubDivision, results.get(0));

    // ensure previous tag name cannot be found anymore
    results.clear();
    final CountDownLatch nextCountDownLatch = new CountDownLatch(1);

    searchEngine.filterReferenceBases(new Search<Reference>("subDivision", result -> {
      results.addAll(result);
      nextCountDownLatch.countDown();
    }));

    try { nextCountDownLatch.await(); } catch(Exception ex) { }

    Assert.assertEquals(0, results.size());
  }

  @Test
  public void updateReferenceSubDivisionSubTitle_SearchFindsUpdatedReferenceSubDivision() {
    Reference newReference = new Reference("reference");
    deepThought.addReference(newReference);

    ReferenceSubDivision newSubDivision = new ReferenceSubDivision("subDivision", "don't find me");
    newReference.addSubDivision(newSubDivision);
    newSubDivision.setSubTitle("Hephaistos");

    final List<ReferenceBase> results = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    searchEngine.filterReferenceBases(new Search<ReferenceBase>("heph", result -> {
      results.addAll(result);
      countDownLatch.countDown();
    }));

    try { countDownLatch.await(); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(newSubDivision, results.get(0));

    // ensure previous tag name cannot be found anymore
    results.clear();
    final CountDownLatch nextCountDownLatch = new CountDownLatch(1);

    searchEngine.filterReferenceBases(new Search<Reference>("find", result -> {
      results.addAll(result);
      nextCountDownLatch.countDown();
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

    searchEngine.filterReferenceBases(new Search<Reference>("phar", result -> {
      results.addAll(result);
      countDownLatch.countDown();
    }));

    try { countDownLatch.await(); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(newSubDivision, results.get(0));


    results.clear();
    final CountDownLatch countDownLatch2 = new CountDownLatch(1);

    searchEngine.filterReferenceBases(new Search<Reference>("krit", result -> {
      results.addAll(result);
      countDownLatch2.countDown();
    }));

    try { countDownLatch2.await(); } catch(Exception ex) { }

    Assert.assertEquals(1, results.size());
    Assert.assertEquals(newSubDivision, results.get(0));

    results.clear();
    final CountDownLatch countDownLatch3 = new CountDownLatch(1);

    searchEngine.filterReferenceBases(new Search<Reference>("Liebe", result -> {
      results.addAll(result);
      countDownLatch3.countDown();
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

    Collection<Entry> entriesWithoutTags = searchEngine.getEntriesWithoutTags();
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

    Collection<Entry> entriesWithTag1 = searchEngine.findEntriesWithTags(new String[]{"tag1"});
    Assert.assertEquals(1, entriesWithTag1.size());

    Assert.assertTrue(entriesWithTag1.contains(entryWithTags1));
    Assert.assertFalse(entriesWithTag1.contains(entryWithTags2));
    Assert.assertFalse(entriesWithTag1.contains(entryWithoutTags1));
    Assert.assertFalse(entriesWithTag1.contains(entryWithoutTags2));
    Assert.assertFalse(entriesWithTag1.contains(entryWithoutTags3));

    Collection<Entry> entriesWithTag2 = searchEngine.findEntriesWithTags(new String[]{"tag2"});
    Assert.assertEquals(2, entriesWithTag2.size());

    Assert.assertTrue(entriesWithTag2.contains(entryWithTags1));
    Assert.assertTrue(entriesWithTag2.contains(entryWithTags2));
    Assert.assertFalse(entriesWithTag2.contains(entryWithoutTags1));
    Assert.assertFalse(entriesWithTag2.contains(entryWithoutTags2));
    Assert.assertFalse(entriesWithTag2.contains(entryWithoutTags3));

    Collection<Entry> entriesWithTag3 = searchEngine.findEntriesWithTags(new String[]{"tag3"});
    Assert.assertEquals(1, entriesWithTag3.size());

    Assert.assertFalse(entriesWithTag3.contains(entryWithTags1));
    Assert.assertTrue(entriesWithTag3.contains(entryWithTags2));
    Assert.assertFalse(entriesWithTag3.contains(entryWithoutTags1));
    Assert.assertFalse(entriesWithTag3.contains(entryWithoutTags2));
    Assert.assertFalse(entriesWithTag3.contains(entryWithoutTags3));

    Collection<Entry> entriesWithTags2And3 = searchEngine.findEntriesWithTags(new String[]{"tag2", "tag3"});
    Assert.assertEquals(1, entriesWithTags2And3.size());

    Assert.assertFalse(entriesWithTags2And3.contains(entryWithTags1));
    Assert.assertTrue(entriesWithTags2And3.contains(entryWithTags2));
    Assert.assertFalse(entriesWithTags2And3.contains(entryWithoutTags1));
    Assert.assertFalse(entriesWithTags2And3.contains(entryWithoutTags2));
    Assert.assertFalse(entriesWithTags2And3.contains(entryWithoutTags3));

  }

}

package net.deepthought.data.search;

import com.example.OpenOfficeDocumentsImporterExporter;

import net.deepthought.Application;
import net.deepthought.data.helper.MockEntityManager;
import net.deepthought.data.helper.TestDependencyResolver;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.Tag;
import net.deepthought.data.search.helper.IdSettableEntry;
import net.deepthought.data.search.helper.TestApplicationConfiguration;

import org.apache.lucene.queryparser.classic.ParseException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

/**
 * Created by ganymed on 17/03/15.
 */
public class LuceneSearchEngineTest {

  protected LuceneSearchEngine searchEngine;

  @Before
  public void setup() {
    searchEngine = new LuceneSearchEngine();

    Application.instantiate(new TestApplicationConfiguration(), new TestDependencyResolver(new MockEntityManager()));
  }


  @Test
  public void test() throws IOException, ParseException {
    searchEngine.index("Jetzt aber mal ein ganz ein langer Text, dessen Sprache zu identifizieren dir hoffentlich keine Mühe bereitet, liebes Tika");
    searchEngine.index("Entweder man lebt, oder man ist konsequent");
    searchEngine.index("Du hast nichts zu verlieren, aber eine Welt zu gewinnen");
    searchEngine.index("Die Welt hat genug für jedermanns Bedürfnisse, aber nicht für jedermanns Gier");
    searchEngine.index("Shit happens");
    searchEngine.index("In the long run we're all dead");
    searchEngine.index("Voulez vous coucher avec moi? - Va te faire enculer sale fils de pute!");
    searchEngine.index("Preferisco la dolce vita italiana");

    searchEngine.search("lebt", Entry.class);
    searchEngine.search("leben", Entry.class);
    searchEngine.search("run", Entry.class);

    List<IndexTerm> terms = searchEngine.getAllTerms();
  }

  @Test
  public void testLanguageDetection() throws IOException, ParseException {
    searchEngine.index("Jetzt aber mal ein ganz ein langer Text, dessen Sprache zu identifizieren dir hoffentlich keine Mühe bereitet, liebes Tika");
    searchEngine.index("Entweder man lebt, oder man ist konsequent");
    searchEngine.index("Du hast nichts zu verlieren, aber eine Welt zu gewinnen");
    searchEngine.index("Die Welt hat genug für jedermanns Bedürfnisse, aber nicht für jedermanns Gier");
    searchEngine.index("Shit happens");
    searchEngine.index("In the long run we're all dead");
    searchEngine.index("Voulez vous coucher avec moi? - Va te faire enculer sale fils de pute!");
    searchEngine.index("Preferisco la dolce vita italiana");
  }

  @Test
  public void testSearchingInLongTexts() throws IOException, ParseException {
    OpenOfficeDocumentsImporterExporter importer = new OpenOfficeDocumentsImporterExporter();
    String documentContent = importer.extractPlainTextFromTextDocument
        ("/run/media/ganymed/fast_data/coding/Android/self/DeepThought/libs/importer_exporter/OpenOfficeDocumentsImporterExporter/src/test/resources/Schneisen im Wald 4.odt");
    searchEngine.index(documentContent);

    searchEngine.search("Metallbörse", Entry.class);
    searchEngine.search("leben", Entry.class);
    searchEngine.search("run", Entry.class);

    List<IndexTerm> terms = searchEngine.getAllTerms();
  }

  @Test
  public void testSearchingInIndexWithManyDocuments() throws IOException, ParseException {
    OpenOfficeDocumentsImporterExporter importer = new OpenOfficeDocumentsImporterExporter();
    List<Entry> entries = importer.extractEntriesFromDankitosSchneisenImWald("/run/media/ganymed/fast_data/coding/Android/self/DeepThought/libs/importer_exporter/OpenOfficeDocumentsImporterExporter/src/test/resources/Schneisen im Wald 4.odt");
    for(Entry entry : entries)
      searchEngine.updateIndex(entry);

    searchEngine.search("Metallbörse", Entry.class);
    searchEngine.search("Überwachungsstaat", Entry.class);
    searchEngine.search("Überwachung", Entry.class);

    List<IndexTerm> terms = searchEngine.getAllTerms();
  }


  @Test
  public void findAllEntriesWithoutTags() throws IOException, ParseException {
    Tag tag1 = new Tag("tag1");
    Tag tag2 = new Tag("tag2");
    Tag tag3 = new Tag("tag3");

    Entry entryWithoutTags1 = new IdSettableEntry(1);
    Entry entryWithoutTags2 = new IdSettableEntry(3);
    Entry entryWithoutTags3 = new IdSettableEntry(5);

    Entry entryWithTags1 = new IdSettableEntry(2);
    Entry entryWithTags2 = new IdSettableEntry(4);

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


  @Test
  public void findEntriesWithTags() throws IOException, ParseException {
    Tag tag1 = new Tag("tag1");
    Tag tag2 = new Tag("tag2");
    Tag tag3 = new Tag("tag3");

    Entry entryWithoutTags1 = new IdSettableEntry(1);
    Entry entryWithoutTags2 = new IdSettableEntry(3);
    Entry entryWithoutTags3 = new IdSettableEntry(5);

    Entry entryWithTags1 = new IdSettableEntry(2);
    Entry entryWithTags2 = new IdSettableEntry(4);

    entryWithTags1.addTag(tag1);
    entryWithTags1.addTag(tag2);
    entryWithTags2.addTag(tag2);
    entryWithTags2.addTag(tag3);

    searchEngine.updateIndex(entryWithoutTags1);
    searchEngine.updateIndex(entryWithTags1);
    searchEngine.updateIndex(entryWithoutTags2);
    searchEngine.updateIndex(entryWithTags2);
    searchEngine.updateIndex(entryWithoutTags3);

    List<Long> entriesWithTag1 = searchEngine.fndEntriesWithTags(new String[] { "tag1"});
    Assert.assertEquals(1, entriesWithTag1.size());

    Assert.assertTrue(entriesWithTag1.contains(entryWithTags1.getId()));
    Assert.assertFalse(entriesWithTag1.contains(entryWithTags2.getId()));
    Assert.assertFalse(entriesWithTag1.contains(entryWithoutTags1.getId()));
    Assert.assertFalse(entriesWithTag1.contains(entryWithoutTags2.getId()));
    Assert.assertFalse(entriesWithTag1.contains(entryWithoutTags3.getId()));

    List<Long> entriesWithTag2 = searchEngine.fndEntriesWithTags(new String[] { "tag2"});
    Assert.assertEquals(2, entriesWithTag2.size());

    Assert.assertTrue(entriesWithTag2.contains(entryWithTags1.getId()));
    Assert.assertTrue(entriesWithTag2.contains(entryWithTags2.getId()));
    Assert.assertFalse(entriesWithTag2.contains(entryWithoutTags1.getId()));
    Assert.assertFalse(entriesWithTag2.contains(entryWithoutTags2.getId()));
    Assert.assertFalse(entriesWithTag2.contains(entryWithoutTags3.getId()));

    List<Long> entriesWithTag3 = searchEngine.fndEntriesWithTags(new String[] { "tag3"});
    Assert.assertEquals(1, entriesWithTag3.size());

    Assert.assertFalse(entriesWithTag3.contains(entryWithTags1.getId()));
    Assert.assertTrue(entriesWithTag3.contains(entryWithTags2.getId()));
    Assert.assertFalse(entriesWithTag3.contains(entryWithoutTags1.getId()));
    Assert.assertFalse(entriesWithTag3.contains(entryWithoutTags2.getId()));
    Assert.assertFalse(entriesWithTag3.contains(entryWithoutTags3.getId()));

    List<Long> entriesWithTags2And3 = searchEngine.fndEntriesWithTags(new String[] { "tag2", "tag3"});
    Assert.assertEquals(1, entriesWithTags2And3.size());

    Assert.assertFalse(entriesWithTags2And3.contains(entryWithTags1.getId()));
    Assert.assertTrue(entriesWithTags2And3.contains(entryWithTags2.getId()));
    Assert.assertFalse(entriesWithTags2And3.contains(entryWithoutTags1.getId()));
    Assert.assertFalse(entriesWithTags2And3.contains(entryWithoutTags2.getId()));
    Assert.assertFalse(entriesWithTags2And3.contains(entryWithoutTags3.getId()));

  }

}

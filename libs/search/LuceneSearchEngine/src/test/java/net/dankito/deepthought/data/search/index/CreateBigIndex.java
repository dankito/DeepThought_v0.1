package net.dankito.deepthought.data.search.index;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.TestApplicationConfiguration;
import net.dankito.deepthought.data.model.Category;
import net.dankito.deepthought.data.model.DeepThought;
import net.dankito.deepthought.data.model.Person;
import net.dankito.deepthought.data.model.Reference;
import net.dankito.deepthought.data.model.Tag;
import net.dankito.deepthought.data.persistence.EntityManagerConfiguration;
import net.dankito.deepthought.data.persistence.IEntityManager;
import net.dankito.deepthought.data.search.ISearchEngine;
import net.dankito.deepthought.data.search.LuceneSearchEngine;
import net.dankito.deepthought.javase.db.OrmLiteJavaSeEntityManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by ganymed on 16/04/15.
 */
public class CreateBigIndex {

  private final static Logger log = LoggerFactory.getLogger(CreateBigIndex.class);


  protected LuceneSearchEngine searchEngine = null;

  protected IEntityManager entityManager = null;

  protected DeepThought deepThought = null;


  @Before
  public void setup() throws SQLException {
    Application.instantiate(new TestApplicationConfiguration("data/tests/big_data_new/") {
      @Override
      public IEntityManager createEntityManager(EntityManagerConfiguration configuration) throws Exception {
        return new OrmLiteJavaSeEntityManager(configuration);
      }

      @Override
      public ISearchEngine createSearchEngine() {
        try {
          return new LuceneSearchEngine();
        } catch (Exception ex) {
        }
        return null;
      }
    });

    deepThought = Application.getDeepThought();
    entityManager = Application.getEntityManager();

    searchEngine = (LuceneSearchEngine)Application.getSearchEngine();
//    searchEngine.deleteIndex();
  }

  @After
  public void tearDown() {
//    Application.shutdown();
    searchEngine.close();
    entityManager.close();
  }


  @Test
  public void createBigIndex() {
//    importEntriesFromDankitoSchneisenImWald();
//    indexWikipediaArticles();
    createRandomTagsCategoriesPersonsAndReferences();
  }

  protected void createRandomTagsCategoriesPersonsAndReferences() {
    List<String> words = readWordList();

    for(String word : words) {
      deepThought.addTag(new Tag(word));
      deepThought.addCategory(new Category(word));
    }

    for(int i = 0; i < words.size() - 2; i+=2) {
      deepThought.addPerson(new Person(words.get(i), words.get(i + 1)));
    }

    for(int i = 0; i < words.size() - 6; i+=6) {
      deepThought.addReference(new Reference(words.get(i) + " " + words.get(i + 1) + " " + words.get(i + 2) + " " + words.get(i + 3),
          words.get(i + 4) + " " + words.get(i + 5)));
    }
  }

  protected List<String> readWordList() {
    List<String> wordList = new ArrayList<>();

    try {
      InputStream wordListStream = CreateBigIndex.class.getClassLoader().getResourceAsStream("openthesaurus.txt");
      InputStreamReader is = new InputStreamReader(wordListStream);
      BufferedReader bufferedReader = new BufferedReader(is);
      String line = null;

      while((line = bufferedReader.readLine()) != null) {
        if(line.startsWith("#") == false) { // filter out comments
          String[] words = line.split(";");
          wordList.addAll(Arrays.asList(words));
        }
      }
    } catch(Exception ex) { log.error("Could not read Word List", ex); }

    return wordList;
  }


  protected void indexWikipediaArticles() {
//    WikipediaImporter importer = new WikipediaImporter();
//
////    importer.getEnglishFeaturedArticles();
//
//    importer.getGermanGeschichtsPortalArticles();
  }


  protected void importEntriesFromDankitoSchneisenImWald() {
//    OpenOfficeDocumentsImporterExporter importer = new OpenOfficeDocumentsImporterExporter();
//
//    importer.extractEntriesFromDankitosSchneisenImWald("/run/media/ganymed/fast_data/coding/Android/self/DeepThought/libs/importer_exporter/OpenOfficeDocumentsImporterExporter/src/test/resources/Schneisen im Wald 4.odt");
  }

}

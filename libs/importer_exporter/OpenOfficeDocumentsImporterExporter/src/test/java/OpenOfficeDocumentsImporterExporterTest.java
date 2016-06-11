import com.example.OpenOfficeDocumentsImporterExporter;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.TestApplicationConfiguration;
import net.dankito.deepthought.data.model.DeepThought;
import net.dankito.deepthought.data.model.Entry;
import net.dankito.deepthought.data.persistence.EntityManagerConfiguration;
import net.dankito.deepthought.data.persistence.IEntityManager;
import net.dankito.deepthought.data.search.ISearchEngine;
import net.dankito.deepthought.data.search.LuceneAndDatabaseSearchEngine;
import net.dankito.deepthought.javase.db.OrmLiteJavaSeEntityManager;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * Created by ganymed on 19/04/15.
 */
public class OpenOfficeDocumentsImporterExporterTest {

  protected OpenOfficeDocumentsImporterExporter importer = null;

  protected DeepThought deepThought;

  @Before
  public void setup() {
    importer = new OpenOfficeDocumentsImporterExporter();

//    Application.instantiate(new TestApplicationConfiguration(new MockEntityManager()));

    Application.instantiate(new TestApplicationConfiguration("data/tests/manual/data/") {
      @Override
      public IEntityManager createEntityManager(EntityManagerConfiguration configuration) throws Exception {
        return new OrmLiteJavaSeEntityManager(configuration);
      }

      @Override
      public ISearchEngine createSearchEngine() {
//          return new LuceneSearchEngine();
        return new LuceneAndDatabaseSearchEngine();
      }
    });

    deepThought = Application.getDeepThought();
  }


//  @Test
//  public void importSchneisenImWald2() {
//    List<Entry> extractedEntries = importer.extractEntriesFromDankitosSchneisenImWald("/run/media/ganymed/data/tools/docs/Wissen/Schneisen im Wald 2.odt");
//
//    Assert.assertEquals(661, extractedEntries.size());
//    Assert.assertEquals(661, deepThought.getEntries().size());
//    Assert.assertEquals(4, deepThought.getSeriesTitles().size());
//    Assert.assertEquals(610, deepThought.getReferences().size());
//    Assert.assertEquals(596, deepThought.getReferenceSubDivisions().size());
//  }

//  @Test
//  public void importSchneisenImWald3() {
//    List<Entry> extractedEntries = importer.extractEntriesFromDankitosSchneisenImWald("/run/media/ganymed/data/tools/docs/Wissen/Schneisen im Wald 3.odt");
//
//    Assert.assertEquals(213, extractedEntries.size());
//    Assert.assertEquals(213, deepThought.getEntries().size());
//    Assert.assertEquals(5, deepThought.getSeriesTitles().size());
//    Assert.assertEquals(206, deepThought.getReferences().size());
//    Assert.assertEquals(304, deepThought.getReferenceSubDivisions().size());
//  }

//    @Test
//  public void importSchneisenImWald4() {
//      List<Entry> extractedEntries = importer.extractEntriesFromDankitosSchneisenImWald("/run/media/ganymed/data/tools/docs/Wissen/Schneisen im Wald 4.odt");
//
//    Assert.assertEquals(419, extractedEntries.size());
//    Assert.assertEquals(419, deepThought.getEntries().size());
//    Assert.assertEquals(14, deepThought.getSeriesTitles().size());
//    Assert.assertEquals(232, deepThought.getReferences().size()); // TODO: should be more
//      Assert.assertEquals(253, deepThought.getReferenceSubDivisions().size());
//      Assert.assertEquals(24, deepThought.getCategories().size());
//  }

//  @Test
//  public void importZitate() {
//    List<Entry> extractedEntries = importer.importDankitosZitate("/run/media/ganymed/data/tools/docs/zitate.odt");
//
//    Assert.assertEquals(313, extractedEntries.size());
//    Assert.assertEquals(313, deepThought.getEntries().size());
//    Assert.assertEquals(14, deepThought.getSeriesTitles().size());
//    Assert.assertEquals(193, deepThought.getReferences().size());
//    Assert.assertEquals(218, deepThought.getCategories().size());
//  }

  @Test
  public void importZitateAndAllSchneisen() {
    List<Entry> extractedEntries = importer.importDankitosZitate("/run/media/ganymed/data/tools/docs/zitate.odt");
    List<Entry> extractedEntries2 = importer.extractEntriesFromDankitosSchneisenImWald("/run/media/ganymed/data/tools/docs/Wissen/Schneisen im Wald 2.odt");
    List<Entry> extractedEntries3 = importer.extractEntriesFromDankitosSchneisenImWald("/run/media/ganymed/data/tools/docs/Wissen/Schneisen im Wald 3.odt");
    List<Entry> extractedEntries4 = importer.extractEntriesFromDankitosSchneisenImWald("/run/media/ganymed/data/tools/docs/Wissen/Schneisen im Wald 4.odt");

    Assert.assertEquals(419, extractedEntries.size());
    Assert.assertEquals(419, deepThought.getEntries().size());
    Assert.assertEquals(14, deepThought.getSeriesTitles().size());
    Assert.assertEquals(232, deepThought.getReferences().size());
    Assert.assertEquals(253, deepThought.getReferenceSubDivisions().size());
    Assert.assertEquals(24, deepThought.getCategories().size());
  }

}

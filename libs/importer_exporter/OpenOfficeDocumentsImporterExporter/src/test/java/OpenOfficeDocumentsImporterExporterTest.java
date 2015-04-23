import com.example.OpenOfficeDocumentsImporterExporter;

import net.deepthought.Application;
import net.deepthought.data.TestApplicationConfiguration;
import net.deepthought.data.helper.MockEntityManager;
import net.deepthought.data.helper.TestDependencyResolver;
import net.deepthought.data.model.DeepThought;
import net.deepthought.data.model.Entry;

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

    Application.instantiate(new TestApplicationConfiguration(), new TestDependencyResolver(new MockEntityManager()));

    deepThought = Application.getDeepThought();
  }


  @Test
  public void importSchneisenImWald4() {
    List<Entry> extractedEntries = importer.extractEntriesFromDankitosSchneisenImWald("/run/media/ganymed/fast_data/coding/Android/self/DeepThought/libs/importer_exporter/OpenOfficeDocumentsImporterExporter/src/test/resources/Schneisen im Wald 4.odt");

    Assert.assertEquals(314, extractedEntries.size());
    Assert.assertEquals(314, deepThought.getEntries().size());
    Assert.assertEquals(12, deepThought.getSeriesTitles().size());
    Assert.assertEquals(166, deepThought.getReferences().size()); // TODO: should be more
  }
}

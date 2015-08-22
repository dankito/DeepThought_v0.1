package net.deepthought.data.importer_exporter;

import net.deepthought.Application;
import net.deepthought.TestApplicationConfiguration;
import net.deepthought.data.model.DeepThought;

import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;

/**
 * Created by ganymed on 13/04/15.
 */
public class WikipediaImporterTest {

  protected WikipediaImporter importer = null;

  protected DeepThought deepThought;


  @Before
  public void setup() throws SQLException {
    Application.instantiate(new TestApplicationConfiguration());

    deepThought = Application.getDeepThought();

    importer = new WikipediaImporter();
  }


  @Test
  public void test() {
    importer.testImportWikipediaArticles();
  }

  @Test
  public void getEnglishFeaturedArticles() {
    importer.getEnglishFeaturedArticles();
  }

}

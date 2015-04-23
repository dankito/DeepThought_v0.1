package net.deepthought.data.importer_exporter;

import net.deepthought.Application;
import net.deepthought.data.TestApplicationConfiguration;
import net.deepthought.data.helper.MockEntityManager;
import net.deepthought.data.helper.TestDependencyResolver;
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
    importer = new WikipediaImporter();

    Application.instantiate(new TestApplicationConfiguration(), new TestDependencyResolver(new MockEntityManager()));

    deepThought = Application.getDeepThought();
  }


  @Test
  public void test() {
    importer.importWikipediaArticles();
  }

  @Test
  public void getEnglishFeaturedArticles() {
    importer.getEnglishFeaturedArticles();
  }

}

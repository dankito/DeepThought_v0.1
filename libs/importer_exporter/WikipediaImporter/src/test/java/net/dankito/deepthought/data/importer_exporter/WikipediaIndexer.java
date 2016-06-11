package net.dankito.deepthought.data.importer_exporter;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.TestApplicationConfiguration;
import net.dankito.deepthought.data.model.DeepThought;
import net.dankito.deepthought.data.persistence.EntityManagerConfiguration;
import net.dankito.deepthought.data.persistence.IEntityManager;
import net.dankito.deepthought.data.search.ISearchEngine;
import net.dankito.deepthought.data.search.LuceneSearchEngine;
import net.dankito.deepthought.javase.db.OrmLiteJavaSeEntityManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;

/**
 * Created by ganymed on 13/04/15.
 */
public class WikipediaIndexer {

  protected WikipediaImporter importer = null;

  protected IEntityManager entityManager = null;

  protected DeepThought deepThought = null;


  @Before
  public void setup() throws SQLException {
    importer = new WikipediaImporter();

    Application.instantiate(new TestApplicationConfiguration("data/tests/big_data/") {
      @Override
      public IEntityManager createEntityManager(EntityManagerConfiguration configuration) throws Exception {
        entityManager = new OrmLiteJavaSeEntityManager(configuration);
        WikipediaIndexer.this.entityManager = entityManager;
        return entityManager;
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
  }

  @After
  public void tearDown() {
    Application.shutdown();
  }


  @Test
  public void indexArticles() {
    importer = new WikipediaImporter();

    importer.getEnglishFeaturedArticles();

    importer.getGermanGeschichtsPortalArticles();
  }
}

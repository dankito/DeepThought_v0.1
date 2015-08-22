package net.deepthought.data.importer_exporter;

import net.deepthought.Application;
import net.deepthought.DefaultDependencyResolver;
import net.deepthought.data.model.DeepThought;
import net.deepthought.data.persistence.EntityManagerConfiguration;
import net.deepthought.data.persistence.IEntityManager;
import net.deepthought.data.search.ISearchEngine;
import net.deepthought.data.search.LuceneSearchEngine;
import net.deepthought.javase.db.OrmLiteJavaSeEntityManager;

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
  protected EntityManagerConfiguration configuration = null;

  protected DeepThought deepThought = null;


  @Before
  public void setup() throws SQLException {
    importer = new WikipediaImporter();

    configuration = new EntityManagerConfiguration("data/tests/big_data/");

    Application.instantiate(configuration, new DefaultDependencyResolver() {
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

package net.deepthought.data.contentextractor;

import net.deepthought.Application;
import net.deepthought.DefaultDependencyResolver;
import net.deepthought.data.TestApplicationConfiguration;
import net.deepthought.data.helper.MockEntityManager;
import net.deepthought.data.model.Category;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.Tag;
import net.deepthought.data.persistence.EntityManagerConfiguration;
import net.deepthought.data.persistence.IEntityManager;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

/**
 * Created by ganymed on 20/06/15.
 */
public class WikipediaOnlineContentExtractorTest {

  protected WikipediaOnlineContentExtractor wikipediaOnlineContentExtractor = null;


  @Before
  public void setup() {
    wikipediaOnlineContentExtractor = new WikipediaOnlineContentExtractor();

    Application.instantiate(new TestApplicationConfiguration(), new DefaultDependencyResolver() {
      @Override
      public IEntityManager createEntityManager(EntityManagerConfiguration configuration) throws Exception {
        return new MockEntityManager();
      }
    });
  }

  @After
  public void tearDown() {
    Application.shutdown();
  }


//  @Test
//  public void importGermanDukatMuenzeArticle() {
//    EntryCreationResult creationResult = wikipediaOnlineContentExtractor.createEntryFromArticle("https://de.wikipedia.org/wiki/Dukat_%28M%C3%BCnze%29");
//    testEntryCreation(creationResult, "Dukat (Münze)");
//  }
//
//  @Test
//  public void importGermanAttilaUniformArticle() {
//    EntryCreationResult creationResult = wikipediaOnlineContentExtractor.createEntryFromArticle("https://de.wikipedia.org/wiki/Attila_%28Uniform%29");
//    testEntryCreation(creationResult, "Attila (Uniform)");
//  }
//
//  @Test
//  public void importGermanFrondeArticle() {
//    EntryCreationResult creationResult = wikipediaOnlineContentExtractor.createEntryFromArticle("https://de.wikipedia.org/wiki/Fronde");
//    testEntryCreation(creationResult, "Fronde");
//  }

  @Test
  public void importGermanDragonerArticle() {
    EntryCreationResult creationResult = wikipediaOnlineContentExtractor.createEntryFromArticle("https://de.wikipedia.org/wiki/Dragoner");
    testEntryCreation(creationResult, "Dragoner");
  }

  @Test
  public void importGermanKuerassiereArticle() {
    EntryCreationResult creationResult = wikipediaOnlineContentExtractor.createEntryFromArticle("https://de.wikipedia.org/wiki/K%C3%BCrassiere");
    testEntryCreation(creationResult, "Kürassiere");
  }

  protected void testEntryCreation(EntryCreationResult creationResult, String articleTitle) {
    Assert.assertTrue(creationResult.successful());
    Assert.assertNotNull(creationResult.getCreatedEntry());

    Entry article = creationResult.getCreatedEntry();

    Assert.assertEquals(articleTitle, article.getAbstract());

    Assert.assertEquals(1, article.getTags().size());
    Assert.assertEquals("Wikipedia", new ArrayList<Tag>(article.getTags()).get(0).getName());

    Assert.assertEquals(1, article.getCategories().size());
    Assert.assertEquals("Wikipedia", new ArrayList<Category>(article.getCategories()).get(0).getName());

    Assert.assertNotNull(article.getSeries());
    Assert.assertEquals("Wikipedia", article.getSeries().getTitle());

    Assert.assertNotNull(article.getReference());
    Assert.assertEquals(articleTitle, article.getReference().getTitle());

    Assert.assertFalse(article.getContent().contains("Vorlage_Weiterleitungshinweis"));
    Assert.assertFalse(article.getContent().contains("Vorlage_Belege_fehlen"));
  }
}

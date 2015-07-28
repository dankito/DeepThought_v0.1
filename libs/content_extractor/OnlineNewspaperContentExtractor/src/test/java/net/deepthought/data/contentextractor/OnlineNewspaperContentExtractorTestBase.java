package net.deepthought.data.contentextractor;

import net.deepthought.Application;
import net.deepthought.data.TestApplicationConfiguration;
import net.deepthought.data.helper.MockEntityManager;
import net.deepthought.data.helper.TestDependencyResolver;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.Tag;
import net.deepthought.util.StringUtils;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

/**
 * Created by ganymed on 13/06/15.
 */
public abstract class OnlineNewspaperContentExtractorTestBase {

  protected OnlineNewspaperContentExtractorBase contentExtractor = null;


  protected abstract OnlineNewspaperContentExtractorBase createOnlineNewspaperContentExtractor();


  @Before
  public void setup() {
    contentExtractor = createOnlineNewspaperContentExtractor();

    Application.instantiate(new TestApplicationConfiguration(), new TestDependencyResolver(new MockEntityManager()));
  }

  @After
  public void tearDown() {
    Application.shutdown();
  }


  protected Entry testImportArticle(String articleUrl) {
    EntryCreationResult result = contentExtractor.createEntryFromArticle(articleUrl);
    Assert.assertTrue(result.successful);

    Entry importedEntry = result.getCreatedEntry();
    Assert.assertNotNull(importedEntry);
    Assert.assertTrue(StringUtils.isNotNullOrEmpty(importedEntry.getAbstract()));

    Assert.assertTrue(importedEntry.getTags().size() > 0);
    Tag periodicalTag = null;
    for(Tag tag : importedEntry.getTags()) {
      if(contentExtractor.getNewspaperName().equals(tag.getName()))
        periodicalTag = tag;
    }
    Assert.assertNotNull(periodicalTag);

    Assert.assertNotNull(importedEntry.getSeries());
    Assert.assertEquals(contentExtractor.getNewspaperName(), importedEntry.getSeries().getTitle());

    Assert.assertNotNull(importedEntry.getReference());
    Assert.assertNotNull(importedEntry.getReference().getIssueOrPublishingDate());
    Assert.assertNotNull(importedEntry.getReference().getPublishingDate());

    Assert.assertNotNull(importedEntry.getReferenceSubDivision());
    Assert.assertTrue(StringUtils.isNotNullOrEmpty(importedEntry.getReferenceSubDivision().getTitle()));
//    Assert.assertTrue(StringUtils.isNotNullOrEmpty(importedEntry.getReferenceSubDivision().getSubTitle())); // for Postillion articles sub title is null
    Assert.assertTrue(articleUrl.startsWith(importedEntry.getReferenceSubDivision().getOnlineAddress()) || importedEntry.getReferenceSubDivision().getOnlineAddress().startsWith
        (articleUrl)); // for Zeit multi page articles '/komplettansicht' will be added to url

    return importedEntry;
  }

  protected void testImportedArticleValues(Entry importedEntry, int contentLength, String issueOrPublishingDate, String referenceTitle, String referenceSubTitle,
                                           String abstractString) {
    Assert.assertEquals(issueOrPublishingDate, importedEntry.getReference().getIssueOrPublishingDate());

    Assert.assertEquals(referenceTitle, importedEntry.getReferenceSubDivision().getTitle());
    Assert.assertEquals(referenceTitle, importedEntry.getReferenceSubDivision().getTitle());

    Assert.assertEquals(abstractString, importedEntry.getAbstractAsPlainText());

    Assert.assertEquals(contentLength, importedEntry.getContent().length());
  }
}

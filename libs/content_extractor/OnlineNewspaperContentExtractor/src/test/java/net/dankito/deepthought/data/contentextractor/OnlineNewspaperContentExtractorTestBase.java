package net.dankito.deepthought.data.contentextractor;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.TestApplicationConfiguration;
import net.dankito.deepthought.data.model.Entry;
import net.dankito.deepthought.data.model.Tag;
import net.dankito.deepthought.util.StringUtils;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

/**
 * Created by ganymed on 13/06/15.
 */
public abstract class OnlineNewspaperContentExtractorTestBase {

  protected net.dankito.deepthought.data.contentextractor.OnlineNewspaperContentExtractorBase contentExtractor = null;


  protected abstract net.dankito.deepthought.data.contentextractor.OnlineNewspaperContentExtractorBase createOnlineNewspaperContentExtractor();


  @Before
  public void setup() {
    Application.instantiate(new TestApplicationConfiguration());

    contentExtractor = createOnlineNewspaperContentExtractor();
  }

  @After
  public void tearDown() {
    Application.shutdown();
  }


  protected EntryCreationResult testImportArticle(String articleUrl) {
    EntryCreationResult result = contentExtractor.createEntryFromArticle(articleUrl);
    Assert.assertTrue(result.successful);

    Entry importedEntry = result.getCreatedEntry();
    Assert.assertNotNull(importedEntry);
    Assert.assertTrue(StringUtils.isNotNullOrEmpty(importedEntry.getAbstract()));

    Assert.assertTrue(result.getTags().size() > 0);
    Tag periodicalTag = null;
    for(Tag tag : result.getTags()) {
      if(contentExtractor.getNewspaperName().equals(tag.getName()))
        periodicalTag = tag;
    }
    Assert.assertNotNull(periodicalTag);

    Assert.assertNotNull(result.getSeriesTitle());
    Assert.assertEquals(contentExtractor.getNewspaperName(), result.getSeriesTitle().getTitle());

    Assert.assertNotNull(result.getReference());
    Assert.assertNotNull(result.getReference().getIssueOrPublishingDate());
//    Assert.assertNotNull(result.getReference().getPublishingDate()); // can be null e.g. for Sueddeutsche Magazin: issue 32/2015 cannot be parsed to a Date

    Assert.assertNotNull(result.getReferenceSubDivision());
    Assert.assertTrue(StringUtils.isNotNullOrEmpty(result.getReferenceSubDivision().getTitle()));
//    Assert.assertTrue(StringUtils.isNotNullOrEmpty(importedEntry.getReferenceSubDivision().getSubTitle())); // for Postillion articles sub title is null
    Assert.assertTrue(articleUrl.startsWith(result.getReferenceSubDivision().getOnlineAddress()) || result.getReferenceSubDivision().getOnlineAddress().startsWith
        (articleUrl)); // for Zeit multi page articles '/komplettansicht' will be added to url

    return result;
  }

  protected void testImportedArticleValues(EntryCreationResult creationResult, int contentLength, String issueOrPublishingDate, String referenceTitle, String referenceSubTitle,
                                           String abstractString) {
    Assert.assertEquals(issueOrPublishingDate, creationResult.getReference().getIssueOrPublishingDate());

    Assert.assertEquals(referenceTitle, creationResult.getReferenceSubDivision().getTitle());
    Assert.assertEquals(referenceTitle, creationResult.getReferenceSubDivision().getTitle());

    Assert.assertEquals(abstractString, creationResult.getCreatedEntry().getAbstractAsPlainText());

    Assert.assertEquals(contentLength, creationResult.getCreatedEntry().getContent().length());
  }
}

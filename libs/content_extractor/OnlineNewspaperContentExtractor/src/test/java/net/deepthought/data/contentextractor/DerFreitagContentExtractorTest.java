package net.deepthought.data.contentextractor;

import net.deepthought.data.contentextractor.preview.ArticlesOverviewItem;
import net.deepthought.data.contentextractor.preview.ArticlesOverviewListener;

import org.junit.Assert;
import org.junit.Test;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Created by ganymed on 13/06/15.
 */
public class DerFreitagContentExtractorTest extends GermanOnlineNewspaperContentExtractorTestBase {

  protected DerFreitagContentExtractor underTest = null;


  @Override
  protected OnlineNewspaperContentExtractorBase createOnlineNewspaperContentExtractor() {
    underTest = new DerFreitagContentExtractor();
    return underTest;
  }


  @Test
  public void importKommunismusInOhioArticle() throws ParseException {
    EntryCreationResult creationResult = testImportArticle("https://www.freitag.de/autoren/florian-schmid/kommunismus-in-ohio");
    testImportedArticleValues(creationResult, 5875, "52/15", "Kommunismus in Ohio", "Reportage",
        "Rudolf Stumberger berichtet in seinem neuen Buch vom religiös-utopischen amerikanischen Landleben");

    assertThatPublishingDateEquals(creationResult, "05.01.2016");
  }

  @Test
  public void importBauerSuchtGeldArticle() throws ParseException {
    EntryCreationResult creationResult = testImportArticle("https://www.freitag.de/autoren/der-freitag/bauer-sucht-geld");
    testImportedArticleValues(creationResult, 6919, "02/16", "Bauer sucht Geld", "Tierschutz",
        "Ohne bessere Haltungsbedingungen schwindet die gesellschaftliche Akzeptanz der Landwirtschaft");

    assertThatPublishingDateEquals(creationResult, "15.01.2016");
  }


  protected void assertThatPublishingDateEquals(EntryCreationResult creationResult, String publishingDate) throws ParseException {
    Date dateToBe = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.GERMAN).parse(publishingDate);
    assertThat(creationResult.getReference().getPublishingDate(), is(dateToBe));
  }


  @Test
  public void testGetArticlesOverview() {
    final Set<ArticlesOverviewItem> allItems = new HashSet<>();
    final AtomicInteger partialItemsExtractionCall = new AtomicInteger();
    final CountDownLatch getArticlesOverviewLatch = new CountDownLatch(1);

    contentExtractor.getArticlesOverviewAsync(new ArticlesOverviewListener() {
      @Override
      public void overviewItemsRetrieved(IOnlineArticleContentExtractor contentExtractor, Collection<ArticlesOverviewItem> items, boolean isDone) {
        partialItemsExtractionCall.incrementAndGet();
        allItems.addAll(items);

        Assert.assertNotNull(contentExtractor);
        Assert.assertFalse(items.size() == 0);

        if (isDone)
          getArticlesOverviewLatch.countDown();
      }
    });

    try { getArticlesOverviewLatch.await(10, TimeUnit.MINUTES); } catch(Exception ex) { }

    Assert.assertEquals(2, partialItemsExtractionCall.get());
    Assert.assertTrue(allItems.size() >= 30);
  }


}

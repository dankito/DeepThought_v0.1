package net.dankito.deepthought.data.contentextractor;

import net.dankito.deepthought.data.contentextractor.preview.ArticlesOverviewItem;
import net.dankito.deepthought.data.contentextractor.preview.ArticlesOverviewListener;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by ganymed on 13/06/15.
 */
public class PostillonContentExtractorTest extends GermanOnlineNewspaperContentExtractorTestBase {

  protected net.dankito.deepthought.data.contentextractor.PostillonContentExtractor postillonContentExtractor = null;


  @Override
  protected OnlineNewspaperContentExtractorBase createOnlineNewspaperContentExtractor() {
    postillonContentExtractor = new net.dankito.deepthought.data.contentextractor.PostillonContentExtractor();
    return postillonContentExtractor;
  }


  @Test
  public void importKolonieDeutschSuedostEuropaArtilce() {
    EntryCreationResult creationResult = testImportArticle("http://www.der-postillon.com/2015/07/kolonie-deutsch-sudosteuropa.html");
    testImportedArticleValues(creationResult, 2513, "15.07.2015", "Kolonie Deutsch-Südosteuropa verabschiedet auf Geheiß Berlins neue Gesetze", "",
        "Kolonie Deutsch-Südosteuropa verabschiedet auf Geheiß Berlins neue Gesetze");
  }

  @Test
  public void importKleinerTimmyMussInsHeimWeilErAnKatzenhaarAllergieLeidetArtilce() {
    EntryCreationResult creationResult = testImportArticle("http://www.der-postillon.com/2015/07/kleiner-timmy-9-muss-ins-heim-weil.html");
    testImportedArticleValues(creationResult, 1535, "15.07.2015", "Kleiner Timmy (9) muss ins Heim, weil Familien-Katze an Kinderhaarallergie leidet", "",
        "Kleiner Timmy (9) muss ins Heim, weil Familien-Katze an Kinderhaarallergie leidet");
  }

  @Test
  public void importSchaeubleHatEigentlichKopfVonVaroufakisGefordertArtilce() {
    EntryCreationResult creationResult = testImportArticle("http://www.der-postillon.com/2015/07/schauble-ich-habe-ursprunglich-den-kopf.html");
    testImportedArticleValues(creationResult, 6180, "14.07.2015", "Schäuble: \"Ich habe eigentlich den Kopf von Varoufakis auf einem Silbertablett gefordert\"", "",
        "Schäuble: \"Ich habe eigentlich den Kopf von Varoufakis auf einem Silbertablett gefordert\"");
  }

  @Test
  public void importSonntagsFrageArtilce() {
    EntryCreationResult creationResult = testImportArticle("http://www.der-postillon.com/2015/08/sonntagsfrage-173-wie-finden-sie-dass.html");
    testImportedArticleValues(creationResult, 1626, "30.08.2015", "Sonntagsfrage (173): Wie denken Sie darüber, dass Obama Merkels Flüchtlingspolitik gelobt hat?", "",
        "Sonntagsfrage (173): Wie denken Sie darüber, dass Obama Merkels Flüchtlingspolitik gelobt hat?");
  }


  @Test
  public void testGetArticlesOverview() {
    final List<ArticlesOverviewItem> allItems = new ArrayList<>();
    final AtomicInteger partialItemsExtractionCall = new AtomicInteger();
    final CountDownLatch getArticlesOverviewLatch = new CountDownLatch(1);

    contentExtractor.getArticlesOverviewAsync(new ArticlesOverviewListener() {
      @Override
      public void overviewItemsRetrieved(IOnlineArticleContentExtractor contentExtractor, List<ArticlesOverviewItem> items, boolean isDone) {
        partialItemsExtractionCall.incrementAndGet();
        allItems.addAll(items);

        Assert.assertNotNull(contentExtractor);
        Assert.assertFalse(items.size() == 0);

        if (isDone)
          getArticlesOverviewLatch.countDown();
      }
    });

    try { getArticlesOverviewLatch.await(10, TimeUnit.MINUTES); } catch(Exception ex) { }

    Assert.assertTrue(partialItemsExtractionCall.get() == 2);
    Assert.assertTrue(allItems.size() == 12);

    // TODO: check if panorama teaser has been parsed correctly, all social module list items have been found and only visible Tile are parse
    // in order to do so: save HTML code of Sueddeutsche front page and parse that site
    // for checking if parsing is still appropriate for current Sueddeutsche web page of course also add online parsing with checks like these ones
  }

}

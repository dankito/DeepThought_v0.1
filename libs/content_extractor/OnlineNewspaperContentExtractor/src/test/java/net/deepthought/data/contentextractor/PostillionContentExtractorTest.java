package net.deepthought.data.contentextractor;

import net.deepthought.data.model.Entry;

import org.junit.Test;

/**
 * Created by ganymed on 13/06/15.
 */
public class PostillionContentExtractorTest extends OnlineNewspaperContentExtractorTestBase {

  protected PostillionContentExtractor postillionContentExtractor = null;


  @Override
  protected OnlineNewspaperContentExtractorBase createOnlineNewspaperContentExtractor() {
    postillionContentExtractor = new PostillionContentExtractor();
    return postillionContentExtractor;
  }


  @Test
  public void importKolonieDeutschSuedostEuropaArtilce() {
    Entry importedEntry = testImportArticle("http://www.der-postillon.com/2015/07/kolonie-deutsch-sudosteuropa.html");
    testImportedArticleValues(importedEntry, 2532, "15.07.2015", "Kolonie Deutsch-Südosteuropa verabschiedet auf Geheiß Berlins neue Gesetze", "",
        "Kolonie Deutsch-Südosteuropa verabschiedet auf Geheiß Berlins neue Gesetze");
  }

  @Test
  public void importKleinerTimmyMussInsHeimWeilErAnKatzenhaarAllergieLeidetArtilce() {
    Entry importedEntry = testImportArticle("http://www.der-postillon.com/2015/07/kleiner-timmy-9-muss-ins-heim-weil.html");
    testImportedArticleValues(importedEntry, 1554, "15.07.2015", "Kleiner Timmy (9) muss ins Heim, weil Familien-Katze an Kinderhaarallergie leidet", "",
        "Kleiner Timmy (9) muss ins Heim, weil Familien-Katze an Kinderhaarallergie leidet");
  }

  @Test
  public void importSchaeubleHatEigentlichKopfVonVaroufakisGefordertArtilce() {
    Entry importedEntry = testImportArticle("http://www.der-postillon.com/2015/07/schauble-ich-habe-ursprunglich-den-kopf.html");
    testImportedArticleValues(importedEntry, 6199, "14.07.2015", "Schäuble: \"Ich habe eigentlich den Kopf von Varoufakis auf einem Silbertablett gefordert\"", "",
        "Schäuble: \"Ich habe eigentlich den Kopf von Varoufakis auf einem Silbertablett gefordert\"");
  }

}

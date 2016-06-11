package net.dankito.deepthought.data.contentextractor;

import org.junit.Test;

/**
 * Created by ganymed on 13/06/15.
 */
public class ZeitContentExtractorTest extends GermanOnlineNewspaperContentExtractorTestBase {

  protected net.dankito.deepthought.data.contentextractor.ZeitContentExtractor zeitContentExtractor = null;


  @Override
  protected OnlineNewspaperContentExtractorBase createOnlineNewspaperContentExtractor() {
    zeitContentExtractor = new net.dankito.deepthought.data.contentextractor.ZeitContentExtractor();
    return zeitContentExtractor;
  }


  @Test
  public void importSinglePageArticle() {
    EntryCreationResult creationResult = testImportArticle("http://www.zeit.de/politik/ausland/2015-06/haftbefehl-aegypten-berlin-tegel-journalist-festnahme");
    testImportedArticleValues(creationResult, 3442, "20.06.2015", "Ägyptischer Journalist an Berliner Flughafen festgenommen", "Haftbefehl",
        "Die Polizei hat am Flughafen Tegel einen Al-Dschasira-Journalisten festgesetzt, die ägyptische Regierung wirft ihm Folter vor. Der Sender fühlt sich politisch vorfolgt.");
  }

  @Test
  public void importMultiPageArticle() {
    EntryCreationResult creationResult = testImportArticle("http://www.zeit.de/2014/51/schlachthof-niedersachsen-fleischwirtschaft-ausbeutung-arbeiter");
    testImportedArticleValues(creationResult, 37477, "17.12.2014", "Die Schlachtordnung", "Fleischwirtschaft",
        "In einer idyllischen Gegend in Niedersachsen wird im Sekundentakt geschlachtet, immer schneller, immer billiger, immer schmutziger. Erledigt wird das Gemetzel von einer Geisterarmee aus Osteuropa.");
  }

}

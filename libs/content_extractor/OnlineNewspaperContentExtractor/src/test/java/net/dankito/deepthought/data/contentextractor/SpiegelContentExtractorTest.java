package net.dankito.deepthought.data.contentextractor;

import org.junit.Test;

/**
 * Created by ganymed on 13/06/15.
 */
public class SpiegelContentExtractorTest extends GermanOnlineNewspaperContentExtractorTestBase {

  protected net.dankito.deepthought.data.contentextractor.SpiegelContentExtractor spiegelContentExtractor = null;


  @Override
  protected net.dankito.deepthought.data.contentextractor.OnlineNewspaperContentExtractorBase createOnlineNewspaperContentExtractor() {
    spiegelContentExtractor = new SpiegelContentExtractor();
    return spiegelContentExtractor;
  }

  @Test
  public void importWallStreetMogulSteveCohenArmerReicherHedgefondsKönigArticle() {
    EntryCreationResult creationResult = testImportArticle("http://www.spiegel.de/wirtschaft/unternehmen/wall-street-mogul-steve-cohen-armer-reicher-hedgefonds-koenig-a-700480.html");
    testImportedArticleValues(creationResult, 8626, "14.06.2010", "Armer, reicher Hedgefonds-König", "Wall-Street-Mogul Steve Cohen",
        "Er lebt weiter in Saus und Braus, als hätte es die Wirtschaftskrise nie gegeben: Steve Cohen ist einer der legendärsten Hedgefondsmanager der Wall Street - und der wohl meistgehasste. Jetzt offenbart der Milliardär in einem Interview erstmals sein Privatleben.");
  }

  @Test
  public void importArticleWithUnorderedList() {
    EntryCreationResult creationResult = testImportArticle("http://www.spiegel.de/wirtschaft/soziales/griechenland-so-gefaehrlich-waere-der-grexit-a-1038609.html");
    testImportedArticleValues(creationResult, 7891, "12.06.2015", "So gefährlich wäre Griechenlands Euro-Aus", "Drohender Staatsbankrott",
        "Der Grexit? Ein Kinderspiel. In Deutschland werden die Folgen eines griechischen Staatsbankrotts und Euro-Austritts kleingeredet. Doch die nervösen Reaktionen an den Börsen zeigen: Das Szenario wäre alles andere als harmlos.");
  }

//  @Test
//  public void importArticle() {
//    Entry importedEntry = testImportArticle("http://www.spiegel.de/panorama/polizisten-funken-goebbels-zitat-vor-g7-gipfel-a-1038563.html");
//    testImportedArticleValues(importedEntry, null, 8626, "14.06.2010", "Armer, reicher Hedgefonds-König", "Wall-Street-Mogul Steve Cohen",
//        "Er lebt weiter in Saus und Braus, als hätte es die Wirtschaftskrise nie gegeben: Steve Cohen ist einer der legendärsten Hedgefondsmanager der Wall Street - und der wohl meistgehasste. Jetzt offenbart der Milliardär in einem Interview erstmals sein Privatleben.");
//  }
}

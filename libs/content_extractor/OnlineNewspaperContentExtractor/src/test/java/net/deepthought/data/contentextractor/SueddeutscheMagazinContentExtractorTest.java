package net.deepthought.data.contentextractor;

import net.deepthought.data.model.Entry;

import org.junit.Test;

/**
 * Created by ganymed on 14/04/15.
 */
public class SueddeutscheMagazinContentExtractorTest extends OnlineNewspaperContentExtractorTestBase {

  protected SueddeutscheMagazinContentExtractor importer = null;


  @Override
  protected OnlineNewspaperContentExtractorBase createOnlineNewspaperContentExtractor() {
    importer = new SueddeutscheMagazinContentExtractor();
    return importer;
  }


  @Test
  public void importNummerEinsDerWocheDruckluftArticle() {
      Entry importedEntry = testImportArticle("http://sz-magazin.sueddeutsche.de/texte/anzeigen/43365");
      testImportedArticleValues(importedEntry, 3179, "22.07.2015", "Der Laden, in dem sich die Leute am liebsten aufhalten, ist laut einer aktuellen Umfrage: die Buchhandlung.", "Druckluft",
          "Der Laden, in dem sich die Leute am liebsten aufhalten, ist laut einer aktuellen Umfrage: die Buchhandlung. Und die Gelegenheit, bei der die Leute am liebsten lügen, ist erfahrungsgemäß: die Umfrage.");
  }

  @Test
  public void importDieSchoenstenStrandspaziergaengeDerWeltArticle() {
    Entry importedEntry = testImportArticle("http://sz-magazin.sueddeutsche.de/texte/anzeigen/43306/Deine-Spuren-im-Sand");
    testImportedArticleValues(importedEntry, 6452, "28/2015", "Die schönsten Strandspaziergänge der Welt", "Wellengang",
        "Die schönsten Strandspaziergänge der Welt: Denn nirgends kommt man besser zur Ruhe als am Ufer des Meeres.");
  }

  @Test
  public void importDasZerquetschenVonEiernArticle() {
    Entry importedEntry = testImportArticle("http://sz-magazin.sueddeutsche.de/texte/anzeigen/42288/Das-Zerquetschen-von-Eiern");
    testImportedArticleValues(importedEntry, 9043, "41/2014", "Straßenprostitution in Spanien - Fotostrecke von Txema Salvans", "Das Zerquetschen von Eiern",
        "Als unser Autor diese Fotos von spanischen Prostituierten sah, wurde ihm klar, wo das Problem mit Männern wie ihm liegt.");
  }
}

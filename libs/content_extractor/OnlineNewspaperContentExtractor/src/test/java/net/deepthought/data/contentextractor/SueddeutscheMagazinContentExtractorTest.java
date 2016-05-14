package net.deepthought.data.contentextractor;

import org.junit.Test;

/**
 * Created by ganymed on 14/04/15.
 */
public class SueddeutscheMagazinContentExtractorTest extends GermanOnlineNewspaperContentExtractorTestBase {

  protected SueddeutscheMagazinContentExtractor importer = null;


  @Override
  protected OnlineNewspaperContentExtractorBase createOnlineNewspaperContentExtractor() {
    importer = new SueddeutscheMagazinContentExtractor();
    return importer;
  }


  @Test
  public void importNummerEinsDerWocheDruckluftArticle() {
    EntryCreationResult creationResult = testImportArticle("http://sz-magazin.sueddeutsche.de/texte/anzeigen/43365");
      testImportedArticleValues(creationResult, 3179, "22.07.2015", "Der Laden, in dem sich die Leute am liebsten aufhalten, ist laut einer aktuellen Umfrage: die Buchhandlung.", "Druckluft",
          "Der Laden, in dem sich die Leute am liebsten aufhalten, ist laut einer aktuellen Umfrage: die Buchhandlung. Und die Gelegenheit, bei der die Leute am liebsten lügen, ist erfahrungsgemäß: die Umfrage.");
  }

  @Test
  public void importDieSchoenstenStrandspaziergaengeDerWeltArticle() {
    EntryCreationResult creationResult = testImportArticle("http://sz-magazin.sueddeutsche.de/texte/anzeigen/43306/Deine-Spuren-im-Sand");
    testImportedArticleValues(creationResult, 6452, "28/2015", "Die schönsten Strandspaziergänge der Welt", "Wellengang",
        "Die schönsten Strandspaziergänge der Welt: Denn nirgends kommt man besser zur Ruhe als am Ufer des Meeres.");
  }

  @Test
  public void importDasZerquetschenVonEiernArticle() {
    EntryCreationResult creationResult = testImportArticle("http://sz-magazin.sueddeutsche.de/texte/anzeigen/42288/Das-Zerquetschen-von-Eiern");
    testImportedArticleValues(creationResult, 9044, "41/2014", "Straßenprostitution in Spanien - Fotostrecke von Txema Salvans", "Das Zerquetschen von Eiern",
        "Als unser Autor diese Fotos von spanischen Prostituierten sah, wurde ihm klar, wo das Problem mit Männern wie ihm liegt.");
  }

  @Test
  public void importArticleWithTwoArtikelElements() {
    EntryCreationResult creationResult = testImportArticle("http://sz-magazin.sueddeutsche.de/texte/anzeigen/43533/Ein-Pausenknopf-fuer-das-Leben");
    testImportedArticleValues(creationResult, 7815, "04.09.2015", "Der britische Komponist Max Richter steht vor einer besonderen Uraufführung", "»Ein Pausenknopf für das Leben«",
        "Der britische Komponist Max Richter steht vor einer besonderen Uraufführung: Sein neuestes Werk »Sleep« dauert acht Stunden, im Zuschauerraum stehen 500 Feldbetten. Denn es ist zum Einschlafen.");
  }

  @Test
  public void freiheitIstKapitalistischerMainstream_DateGetsExtractedCorrectly() {
    EntryCreationResult creationResult = testImportArticle("http://sz-magazin.sueddeutsche.de/texte/anzeigen/43404/Freiheit-ist-kapitalistischer-Mainstream");
    testImportedArticleValues(creationResult, 11962, "32/2015", "Cornelia Koppetsch im Interview", "»Freiheit ist kapitalistischer Mainstream«",
        "Die Mittelschicht schafft sich ab, Bildungsabschlüsse verlieren an Wert, und der Neoliberalismus vereinnahmt selbst diejenigen, die ihn bekämpfen sollten – beste Voraussetzungen, um das ganze Gesellschaftssystem ins Wanken zu bringen, meint die Soziologin Cornelia Koppetsch.");
  }

  @Test
  public void selbstGemalteBilderVonFluechtlingskindern_PicturesGetImportedCorrectly() {
    EntryCreationResult creationResult = testImportArticle("http://sz-magazin.sueddeutsche.de/texte/anzeigen/44020/Ich-bin-die-Letzte-ganz-links-auf-dem-Bild");
    testImportedArticleValues(creationResult, 11855, "52/2015", "Selbst gemalte Bilder von Flüchtlingskindern", "»Ich bin die Letzte, ganz links auf dem Bild«",
        "Unter den 800.000 Flüchtlingen, die Deutschland in diesem Jahr erreicht haben, sind etwa 270.000 Kinder. Wir haben sechs von ihnen gebeten, von ihrer Flucht zu erzählen - und dazu ein Bild zu malen.");
  }

  @Test
  public void unfaelleBeimBegruessen_BildGalleryGetsExtractedCorrectly() {
    EntryCreationResult creationResult = testImportArticle("http://sz-magazin.sueddeutsche.de/texte/anzeigen/43797/Na-Servus");
    testImportedArticleValues(creationResult, 11855, "45/2015", "Selbst gemalte Bilder von Flüchtlingskindern", "»Ich bin die Letzte, ganz links auf dem Bild«",
        "Unter den 800.000 Flüchtlingen, die Deutschland in diesem Jahr erreicht haben, sind etwa 270.000 Kinder. Wir haben sechs von ihnen gebeten, von ihrer Flucht zu erzählen - und dazu ein Bild zu malen.");
  }

}

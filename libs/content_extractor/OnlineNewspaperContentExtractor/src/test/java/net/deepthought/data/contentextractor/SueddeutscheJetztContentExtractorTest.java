package net.deepthought.data.contentextractor;

import net.deepthought.data.model.Entry;

import org.junit.Test;

/**
 * Created by ganymed on 14/04/15.
 */
public class SueddeutscheJetztContentExtractorTest extends OnlineNewspaperContentExtractorTestBase {

  protected SueddeutscheJetztContentExtractor importer = null;


  @Override
  protected OnlineNewspaperContentExtractorBase createOnlineNewspaperContentExtractor() {
    importer = new SueddeutscheJetztContentExtractor();
    return importer;
  }


  @Test
  public void importArbeitSchlaegtStudiumArticle() {
      Entry importedEntry = testImportArticle("http://jetzt.sueddeutsche.de/texte/anzeigen/593621/Arbeit-schlaegt-Studium");
      testImportedArticleValues(importedEntry, 4934, "09.07.2015", "Arbeit schlägt Studium!", "",
          "Das Studium ist die beste Zeit des Lebens - diesen Satz hört man ziemlich oft. Dabei ist das totaler Quatsch. Richtig gut wird es erst, wenn man arbeitet.");
  }

  @Test
  public void importZeitMachtSchoenArticle() {
    Entry importedEntry = testImportArticle("http://jetzt.sueddeutsche.de/texte/anzeigen/593605/Zeit-macht-schoen");
    testImportedArticleValues(importedEntry, 5873, "08.07.2015", "Zeit macht schön", "",
        "Verliebt in jemanden, der heißer ist als du? Gute Nachrichten! Forscher haben das Geheimnis von unterschiedlich attraktiven Partnern gelüftet. Es lautet: Geduld.");
  }

  @Test
  public void importDerDuftDesHassesArticle() {
    Entry importedEntry = testImportArticle("http://jetzt.sueddeutsche.de/texte/anzeigen/593210/Der-Duft-des-Hasses");
    testImportedArticleValues(importedEntry, 6933, "11.06.2015", "Der Duft des Hasses", "",
        "\"Nur ein toter Moslem ist ein guter Moslem\": Eine Internetseite sammelt offen rechtsradikale Kommentare, die Sympathisanten der österreichischen FPÖ ins Netz stellen. Teilweise sind diese Inhalte strafbar - eine Verfolgung ihrer Verursacher ist trotzdem schwer.");
  }
}

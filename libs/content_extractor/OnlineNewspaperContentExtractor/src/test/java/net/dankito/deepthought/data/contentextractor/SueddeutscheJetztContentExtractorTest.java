package net.dankito.deepthought.data.contentextractor;

import org.junit.Test;

/**
 * Created by ganymed on 14/04/15.
 */
public class SueddeutscheJetztContentExtractorTest extends GermanOnlineNewspaperContentExtractorTestBase {

  protected net.dankito.deepthought.data.contentextractor.SueddeutscheJetztContentExtractor importer = null;


  @Override
  protected OnlineNewspaperContentExtractorBase createOnlineNewspaperContentExtractor() {
    importer = new net.dankito.deepthought.data.contentextractor.SueddeutscheJetztContentExtractor();
    return importer;
  }


  @Test
  public void importArbeitSchlaegtStudiumArticle() {
      EntryCreationResult creationResult = testImportArticle("http://jetzt.sueddeutsche.de/texte/anzeigen/593621/Arbeit-schlaegt-Studium");
      testImportedArticleValues(creationResult, 4934, "09.07.2015", "Arbeit schlägt Studium!", "",
          "Das Studium ist die beste Zeit des Lebens - diesen Satz hört man ziemlich oft. Dabei ist das totaler Quatsch. Richtig gut wird es erst, wenn man arbeitet.");
  }

  @Test
  public void importZeitMachtSchoenArticle() {
    EntryCreationResult creationResult = testImportArticle("http://jetzt.sueddeutsche.de/texte/anzeigen/593605/Zeit-macht-schoen");
    testImportedArticleValues(creationResult, 5873, "11.12.2015", "Zeit macht schön", "",
        "Verliebt in jemanden, der heißer ist als du? Gute Nachrichten! Forscher haben das Geheimnis von unterschiedlich attraktiven Partnern gelüftet. Es lautet: Geduld.");
  }

  @Test
  public void importDerDuftDesHassesArticle() {
    EntryCreationResult creationResult = testImportArticle("http://jetzt.sueddeutsche.de/texte/anzeigen/593210/Der-Duft-des-Hasses");
    testImportedArticleValues(creationResult, 6933, "11.06.2015", "Der Duft des Hasses", "",
        "\"Nur ein toter Moslem ist ein guter Moslem\": Eine Internetseite sammelt offen rechtsradikale Kommentare, die Sympathisanten der österreichischen FPÖ ins Netz stellen. Teilweise sind diese Inhalte strafbar - eine Verfolgung ihrer Verursacher ist trotzdem schwer.");
  }

  @Test
  public void importJederVonUnsHaelt60Sklaven() {
    EntryCreationResult creationResult = testImportArticle("http://www.jetzt.de/politik/interview-mit-einer-professorin-fuer-supply-management");
    testImportedArticleValues(creationResult, 8149, "22.02.2016", "Jeder von uns hält 60 Sklaven", "",
        "Und zwar durch ganz normalen Konsum. Eine BWL-Professorin erklärt, warum.");
  }

  @Test
  public void importPlastikflaschenverbotInMontreal() {
    EntryCreationResult creationResult = testImportArticle("http://www.jetzt.de/netzteil/plastikflaschenverbot");
    testImportedArticleValues(creationResult, 2341, "15.03.2016", "Montreal will Wasser aus Plastikflaschen verbieten", "",
        "Das Leitungswasser sei gut genug, sagt der Bürgermeister.");
  }

  @Test
  public void importHeuteKoennenWirUnsereNeurosenVielBesserAusleben() {
    EntryCreationResult creationResult = testImportArticle("http://www.jetzt.de/beziehungsunfaehig/eine-psychologin-erklaert-warum-wir-nicht-beziehungsunfaehig-sind");
    testImportedArticleValues(creationResult, 10456, "23.03.2016", "\"Heute können wir unsere Neurosen viel besser ausleben\"", "",
        "Die Psychologin Stefanie Stahl über den Mythos Beziehungsunfähigkeit.");
  }

}

package net.dankito.deepthought.data.contentextractor;

import org.junit.Test;

/**
 * Created by ganymed on 13/06/15.
 */
public class CtContentExtractorTest extends GermanOnlineNewspaperContentExtractorTestBase {

  protected CtContentExtractor ctContentExtractor = null;


  @Override
  protected OnlineNewspaperContentExtractorBase createOnlineNewspaperContentExtractor() {
    ctContentExtractor = new CtContentExtractor();
    return ctContentExtractor;
  }


  @Test
  public void importGefahrenDerTorNutzung() {
    EntryCreationResult creationResult = testImportArticle("http://www.heise.de/ct/ausgabe/2013-20-Gefahren-der-Tor-Nutzung-im-Alltag-2293262.html");
    testImportedArticleValues(creationResult, 8112, "07.09.13", "Eigen-Tor", "Gefahren der Tor-Nutzung im Alltag",
        "In der aktuellen Diskussion hört man häufig den Rat, für mehr Privatsphäre und Sicherheit solle man den Anonymisierungsdienst Tor nutzen. Tatsächlich ist dies jedoch ein sehr gefährlicher Tipp. Für normale Anwender erhöht er de facto das Risiko, tatsächlich überwacht und ausspioniert zu werden.");
  }

  @Test
  public void importHintergruendeDesPackstationHacks() {
    EntryCreationResult creationResult = testImportArticle("http://www.heise.de/ct/artikel/Hintergruende-des-Packstation-Hacks-3248029.html");
    testImportedArticleValues(creationResult, 12402, "24.06.16", "Hintergründe des Packstation-Hacks", "",
        "Mit gefälschter Kundenkarte und einer App konnten Angreifer DHL-Packstationen übernehmen. c't hat die Lücke nachvollzogen und zeigt, warum der Hack bis vor kurzem so leicht war.");
  }

  @Test
  public void importFragwuerdigerDatenschutzInPolizeisystemen() {
    EntryCreationResult creationResult = testImportArticle("http://www.heise.de/ct/ausgabe/2016-13-Fragwuerdiger-Datenschutz-in-Polizeisystemen-3227333.html");
    testImportedArticleValues(creationResult, 17533, "10.06.16", "Außer Kontrolle", "Fragwürdiger Datenschutz in Polizeisystemen",
        "Die Polizei speichert viele Datensätze über Verdächtige und deren Umfeld. Prüfungen in einigen Bundesländern erbrachten verheerende Ergebnisse: Die Einhaltung von Datenschutzvorschriften wird dabei lax gehandhabt.");
  }


}

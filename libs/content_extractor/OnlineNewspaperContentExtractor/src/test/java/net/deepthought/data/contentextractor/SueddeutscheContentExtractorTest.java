package net.deepthought.data.contentextractor;

import net.deepthought.data.contentextractor.preview.ArticlesOverview;
import net.deepthought.data.model.Entry;
import net.deepthought.util.StringUtils;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by ganymed on 14/04/15.
 */
public class SueddeutscheContentExtractorTest extends OnlineNewspaperContentExtractorTestBase {

  protected SueddeutscheContentExtractor importer = null;


  @Override
  protected OnlineNewspaperContentExtractorBase createOnlineNewspaperContentExtractor() {
    importer = new SueddeutscheContentExtractor();
    return importer;
  }


//  @Test
//  public void importArticleToAvoidOnlyTenArticlesAWeekLimit() {
//    Entry importedEntry = testImportArticle("http://www.sueddeutsche.de/karriere/frage-an-den-sz-jobcoach-finde-ich-keine-stelle-weil-ich-nicht-auf-facebook-bin-1.2437827");
//  }

  @Test
  public void importSoVermeidenSieFehlerImTestamentArticle() {
      Entry importedEntry = testImportArticle("http://www.sueddeutsche.de/geld/richtig-vererben-so-vermeiden-sie-fehler-im-testament-1.2406029");
      testImportedArticleValues(importedEntry, 4803, "14.04.2015", "So vermeiden Sie Fehler im Testament", "Richtig vererben",
          "Streit ums Erbe entsteht oft dann, wenn der letzte Wille des Erblassers nicht eindeutig oder am Ende gar unwirksam formuliert ist. Die " +
              "häufigsten Fehler beim Verfassen eines Testaments - und wie man den Nachkommen das Erben erleichtert.");
  }

  @Test
  public void importSiliconValleyGegenDieNSAArticle() {
    Entry importedEntry = testImportArticle("http://www.sueddeutsche.de/digital/kryptografie-debatte-silicon-valley-gegen-die-nsa-1.2432893");
    testImportedArticleValues(importedEntry, 4192, "13.04.2015", "Silicon Valley gegen die NSA", "Kryptografie-Debatte",
        "NSA-Chef Michael Rogers hat erklärt, wie er sich die Smartphone-Überwachung vorstellt. " +
            "Er fordert, dass die Schlüssel verteilt hinterlegt werden. " +
            "Sicherheitsexperten kritisieren den Vorschlag.");
  }

  @Test
  public void importDiePharmaindustrieIstSchlimmerAlsDieMafiaArticle() {
    Entry importedEntry = testImportArticle("http://www.sueddeutsche.de/gesundheit/kritik-an-arzneimittelherstellern-die-pharmaindustrie-ist-schlimmer-als-die-mafia-1.2267631");
    testImportedArticleValues(importedEntry, 19219, "06.02.2015", "\"Die Pharmaindustrie ist schlimmer als die Mafia\"", "Kritik an Arzneimittelherstellern",
        "Medikamente sollen uns ein langes, gesundes Leben bescheren. Doch die Pharmaindustrie bringt mehr Menschen um als die Mafia, sagt der dänische Mediziner Peter C. Gøtzsche - und fordert für die Branche eine Revolution.");
  }

  @Test
  public void importArticleWithMultiplePages() {
    Entry importedEntry = testImportArticle("http://www.sueddeutsche.de/kultur/reich-und-arm-willkommen-in-der-staendegesellschaft--1.2419985");
    testImportedArticleValues(importedEntry, 10643, "03.04.2015", "Dummköpfe ante portas", "Soziale Gerechtigkeit in Deutschland",
        "Vom Kindergarten bis zum Erbe - Ungerechtigkeit. Zwei Bücher beleuchten das deutsche Klassensystem aus unterschiedlicher Perspektive. Die Autoren kommen zum selben Ergebnis.");
  }

  @Test
  public void importArticleWithMultiplePagesAndStartImageForEach() {
    Entry importedEntry = testImportArticle("http://www.sueddeutsche.de/karriere/arbeitszeitmodelle-arbeiten-nach-dem-lustprinzip-1.2373716");
    testImportedArticleValues(importedEntry, 18237, "13.03.2015", "Arbeiten nach dem Lustprinzip", "Arbeitszeitmodelle",
        "Führungskraft in Teilzeit, Sparen für das Freizeit-Konto oder Rentnerin auf Abruf: Manche Firmen lassen ihre Beschäftigten arbeiten, wie sie wollen. Fünf Arbeitnehmer berichten.");
  }

  @Test
  public void importArticleWithGraphics() {
    Entry importedEntry = testImportArticle("http://www.sueddeutsche.de/politik/fluechtlingspolitik-so-viel-kostet-die-festung-europa-1.2516084");
    testImportedArticleValues(importedEntry, 7223, "18.06.2015", "So viel kostet die Festung Europa", "Flüchtlingspolitik",
        "Das Datenprojekt Migrants Files hat einen Teil der Kosten für die Abschottungspolitik der EU zusammengetragen. Die meisten Kosten entstehen bei der Abschiebung von Flüchtlingen. Zwischen 2000 und 2014 haben die Mitgliedsstaaten sowie Norwegen und die Schweiz dafür 11,3 Milliarden Euro ausgegeben. 1,6 Milliarden Euro entfallen auf Grenzschutzmaßnahmen.");

    Assert.assertEquals(4, StringUtils.getNumberOfOccurrences("<div class=\"basebox ", importedEntry.getContent()));
  }

  @Test
  public void importArticleWithImages() {
    Entry importedEntry = testImportArticle("http://www.sueddeutsche.de/muenchen/ebersberg/schloss-falkenberg-wo-der-pappenheimer-wohnte-1.2517879");
    testImportedArticleValues(importedEntry, 9944, "18.06.2015", "Wo der Pappenheimer wohnte", "Schloss Falkenberg",
        "Die Wittelsbacher ließen im frühen zwölften Jahrhundert in Falkenberg eine Burg errichten, auf deren Ruinen das spätere Schloss entstand. In den 436 Jahren seines Bestehens wurde das Schloss insgesamt 18 Mal verkauft, alleine zwölf Mal in den vergangenen 200 Jahren, nur acht Mal wurde es vererbt Viele Münchner kennen heute den Biergarten, nicht aber das Schloss.");

    Assert.assertEquals(2, StringUtils.getNumberOfOccurrences("<img ", importedEntry.getContent()));
  }

  @Test
  public void importImageGalleryArticle() {
    Entry importedEntry = testImportArticle("http://www.sueddeutsche.de/politik/jahre-schlacht-von-waterloo-blutrot-sind-hier-nur-die-uniformen-1.2528430");
    testImportedArticleValues(importedEntry, 9856, "19.06.2015", "Blutrot sind hier nur die Uniformen", "200 Jahre Schlacht von Waterloo",
        "Es ist ein Spiel, das mit großem Ernst betrieben wird: 5000 Geschichtsfans stellen in Belgien die Schlacht von Waterloo nach. Auf historische Authentizität legen sie großen Wert. Doch das klappt nicht immer.");

    Assert.assertEquals(18, StringUtils.getNumberOfOccurrences("<img ", importedEntry.getContent()));
  }

  @Test
  public void importArticleWithInlineImageGallery() {
    Entry importedEntry = testImportArticle("http://www.sueddeutsche.de/wirtschaft/preise-alles-wird-teurer-nicht-1.2539186");
    testImportedArticleValues(importedEntry, 2833, "27.06.2015", "Alles wird teurer. Nicht.", "Preise",
        "Ob im Supermarkt oder der Bäckerei, alles scheint teurer zu werden. Doch dieses Gefühl trügt. In Wahrheit sind viele Produkte heute viel schneller verdient als früher.");

    Assert.assertEquals(6, StringUtils.getNumberOfOccurrences("<img ", importedEntry.getContent()));
  }

  @Test
  public void importArticleWithInlineImageGalleryAndContent() {
    Entry importedEntry = testImportArticle("http://www.sueddeutsche.de/muenchen/stellwerk-in-tuessling-abschied-von-der-eisenbahn-nostalgie-1.2527297");
    testImportedArticleValues(importedEntry, 8879, "19.06.2015", "Abschied von der Eisenbahn-Nostalgie", "Stellwerk in Tüssling",
            "In einem Stellwerk in Tüßling werden Weichen und Signale noch per Hand gesteuert: Züge nach Mühldorf und Freilassing leitet ein Fahrdienstleister mit Hebeln und Drahtseilen. " +
            "Auch während der Fahrt mit einem ganz normalen Zug kann man dort Einblicke in die Historie der Eisenbahn erleben: Einige Streckenabschnitte funktionieren schon mit moderner Technik. " +
            "2016 sollen Stellwerk und Weichenwärterhäuschen abgerissen werden - die Modernisierung wird vor allem von den Industriebetrieben im bayerischen Chemiedreieck gefordert.");

    Assert.assertEquals(5, StringUtils.getNumberOfOccurrences("<img ", importedEntry.getContent()));
  }

  @Test
  public void checkIfImportingWrongAbstractBugHasBeenFixed() {
    Entry importedEntry = testImportArticle("http://www.sueddeutsche.de/politik/jahre-wiener-kongress-europa-feiernd-neu-ausgekungelt-1.2135536");
    testImportedArticleValues(importedEntry, 12459, "18.09.2014", "Europa feiernd neu ausgekungelt", "200 Jahre Wiener Kongress",
        "Am 18. September 1814 beginnen die Gegner Napoleons in Wien, Europa neu zu ordnen. Der Machtpoker gleicht einer Riesenparty, sorgt für kuriose Amouren und führt zu Entscheidungen, die teilweise 200 Jahre später nachwirken.");

    Assert.assertEquals(4, StringUtils.getNumberOfOccurrences("<img ", importedEntry.getContent()));
  }

  @Test
  public void testImportArticleUrlWithReducedEqualsTrue() {
    Entry importedEntry = testImportArticle("http://www.sueddeutsche.de/bayern/verbraucherschutz-aufklaerung-verzoegert-1.2525447?reduced=true");
    testImportedArticleValues(importedEntry, 4812, "17.06.2015", "\"Dieser Skandal sollte vertuscht werden\"", "Affäre um Bayern-Ei",
        "Die SPD wirft der bayerischen Verbraucherschutzministerin Ulrike Scharf schwere Versäumnisse im Skandal um die Firma Bayern-Ei vor. Ein Skandal sollte vertuscht werden, meint die Opposition. Die Ministerin weist die Vorwürfe zurück, auch ihr Amtsvorgänger Huber sieht keine Versäumnisse der Behörden.");

    Assert.assertFalse(importedEntry.getReferenceSubDivision().getOnlineAddress().contains("reduced=true"));
  }

  @Test
  public void summeryIsNotMarkedWithClassArticleEntrySummary() {
    Entry importedEntry = testImportArticle("http://www.sueddeutsche.de/politik/selektorenliste-der-nsa-was-die-wikileaks-dokumente-zeigen-1.2547250");
    testImportedArticleValues(importedEntry, 3572, "02.07.2015", "Was die Wikileaks-Dokumente zeigen", "Selektorenliste der NSA",
        "Anhand der Telefonnummern in dieser Selektorenliste wird deutlich, dass die Ausspähung durch die NSA Bundesminister, Referenten und sogar Faxgeräte von Ministerien umfasste.");

    Assert.assertFalse(importedEntry.getReferenceSubDivision().getOnlineAddress().contains("reduced=true"));
  }

  @Test
  public void questionsAreMissing() {
    Entry importedEntry = testImportArticle("http://www.sueddeutsche.de/wirtschaft/abschiebung-psychisch-kranker-es-ist-immer-noch-ueblich-patienten-sozial-zu-isolieren-1.2542962?reduced=true");
    testImportedArticleValues(importedEntry, 8158, "02.07.2015", "\"Es ist immer noch üblich, Patienten sozial zu isolieren\"", "Abschiebung psychisch Kranker",
        "Kaum etwas fürchten Menschen so sehr, wie nicht mehr gebraucht zu werden. Macht der Kapitalismus uns zum Wegwerfartikel? Ein Gespräch mit Klaus Dörner, einem großen Reformer der Psychiatrie.");

    Assert.assertFalse(importedEntry.getReferenceSubDivision().getOnlineAddress().contains("reduced=true"));
  }

  @Test
  public void testIfOnly10ArticlesPerWeekRestrictionWillBeCircumvented() {
    Entry zero = testImportArticle("http://www.sueddeutsche.de/politik/belauscht-in-vietnam-angela-merkels-reiselustiger-schatten-1.2546772");
    Entry one = testImportArticle("http://www.sueddeutsche.de/politik/missbrauch-durch-un-soldaten-was-der-krieg-mit-kindern-macht-1.2529407");
    Entry two = testImportArticle("http://www.sueddeutsche.de/panorama/kinder-verbot-in-duesseldorfer-biergarten-zehn-bis-prozent-der-heutigen-eltern-kotzen-mich-extremst-an-1.2529278");
    Entry three = testImportArticle("http://www.sueddeutsche.de/digital/mobilfunk-ueberwachung-was-sie-ueber-den-sim-karten-hack-wissen-muessen-1.2361115");
    Entry four = testImportArticle("http://www.sueddeutsche.de/politik/nsa-und-bnd-ein-schlechter-witz-der-bundesregierung-1.2512442");
    Entry five = testImportArticle("http://www.sueddeutsche.de/politik/berlin-bundestag-bekommt-hackerangriff-nicht-unter-kontrolle-1.2515345");
    Entry six = testImportArticle("http://www.sueddeutsche.de/bayern/amtsgericht-hof-waldlaeufer-muss-in-die-zelle-1.2514635");
    Entry seven = testImportArticle("http://www.sueddeutsche.de/politik/islamischer-staat-die-wichtigsten-fakten-zum-is-1.2540726");
    Entry eight = testImportArticle("http://www.sueddeutsche.de/muenchen/bauarbeiter-in-muenchen-schuften-zum-hungerlohn-1.2535853");
    Entry nine = testImportArticle("http://www.sueddeutsche.de/wirtschaft/diskussion-um-schuldenschnitt-varoufakis-hat-recht-1.2521596");
    Entry ten = testImportArticle("http://www.sueddeutsche.de/muenchen/videoueberwachung-in-muenchen-stadt-der-augen-1.2316618");
    Entry eleven = testImportArticle("http://www.sueddeutsche.de/politik/ausbeutung-durch-un-blauhelme-wenn-eine-frau-so-viel-kostet-wie-eine-flasche-wasser-1.2524555");

    // now check if the 10 + 1th article has been imported correctly
    Assert.assertFalse(eleven.getContentAsPlainText().endsWith(" ..."));
    testImportedArticleValues(eleven, 8888, "19.06.2015", "Wenn eine Frau so viel kostet wie eine Flasche Wasser", "Ausbeutung durch UN-Blauhelme",
        "Ein UN-Bericht zeigt, dass die Praxis, dass Blauhelmsoldaten Frauen und Kinder im Tausch gegen Waren zum Sex nötigen, weiter verbreitet ist, als bisher angenommen wurde. Viele Experten sind sich einig, dass es sich dabei auch um ein strukturelles Problem der UN-Friedensmissionen handelt. Bislang bleiben viele Vergehen folgenlos für die Täter. Aktivisten und Frauenrechtlerinnen fordern eine konsequentere Strafverfolgung der Blauhelmsoldaten.");
  }

  @Test
  public void testGetArticlesOverview() {
    ArticlesOverview articlesOverview = contentExtractor.getArticlesOverview();

    Assert.assertNotNull(articlesOverview);
    Assert.assertFalse(articlesOverview.getOverviewItems().size() == 0);
    Assert.assertTrue(articlesOverview.getOverviewItems().size() > 100);

    // TODO: check if panorama teaser has been parsed correctly, all social module list items have been found and only visible Tile are parse
    // in order to do so: save HTML code of Sueddeutsche front page and parse that site
    // for checking if parsing is still appropriate for current Sueddeutsche web page of course also add online parsing with checks like these ones
  }
}

package net.deepthought.language;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by ganymed on 12/04/15.
 */
public class LanguageDetectorTest {
  
  protected LanguageDetector detector = null;
  
  @Before
  public void setup() {
    detector = new LanguageDetector();
  }


  @Test
  public void detectLanguageOfGermanTexts() throws Exception {
    String language1 = detector.getLanguageTagOfText("Jetzt aber mal ein ganz ein langer Text, dessen Sprache zu identifizieren dir hoffentlich keine Mühe bereitet, liebes Tika");
    Assert.assertEquals("de", language1);

    String language2 = detector.getLanguageTagOfText("Entweder man lebt, oder man ist konsequent");
    Assert.assertEquals("de", language2);

    String language3 = detector.getLanguageTagOfText("Du hast nichts zu verlieren, aber eine Welt zu gewinnen");
    Assert.assertEquals("de", language3);

    String language4 = detector.getLanguageTagOfText("Die Welt hat genug für jedermanns Bedürfnisse, aber nicht für jedermanns Gier");
    Assert.assertEquals("de", language4);
  }

  @Test
  public void detectLanguageOfEnglishTexts() throws Exception {
    String language1 = detector.getLanguageTagOfText("Shit happens");
    Assert.assertEquals("en", language1);

    String language2 = detector.getLanguageTagOfText("In the long run we're all dead");
    Assert.assertEquals("en", language2);
  }

  @Test
  public void detectLanguageOfFrenchTexts() throws Exception {
    String language1 = detector.getLanguageTagOfText("Voulez vous coucher avec moi? - Va te faire enculer sale fils de pute!");
    Assert.assertEquals("fr", language1);
  }

  @Test
  public void detectLanguageOfItalianTexts() throws Exception {
    String language1 = detector.getLanguageTagOfText("Preferisco la dolce vita italiana");
    Assert.assertEquals("it", language1);

    String language2 = detector.getLanguageTagOfText("Prima si vota, poi si beve");
    Assert.assertEquals("it", language2);
  }

  @Test
  public void detectLanguageOfMixedGermanAndEnglishText() throws Exception {
    String germanWithEnglishWords = detector.getLanguageTagOfText("Mixed one: Deutsch mit Business English einstreusel, immer gut fuer Bullshit Bingo");

    Assert.assertEquals("de", germanWithEnglishWords); // nice one!
  }

  @Test
  public void detectLanguageOfVeryLongGermanText() {
    String longGermanTextLanguage = detector.getLanguageTagOfText("Jetzt mal was langes, fuer dich, mein Schatz: Wer wünscht sich nicht ein langes, " +
        "gesundes Leben? Die Pharmaindustrie entwickelt, testet und vertreibt die Mittel, die das gewährleisten sollen. Doch der Mediziner Peter C. Gøtzsche hält das gegenwärtige System für gescheitert. Der Däne hat selbst für Arzneimittelhersteller gearbeitet, dann die Seiten gewechselt und leitet heute das Nordic Cochrane Center in Kopenhagen. In seinem Buch \"Tödliche Medizin und organisierte Kriminalität\" übt er heftige Kritik an der Branche.\n" +
        "\n" +
        "SZ.de: Kürzlich ist aufgeflogen, dass eine Firma in Indien Daten gefälscht hat, um Studien für internationale Pharmakonzerne besser aussehen zu lassen. Sie behaupten, dass auch die Pharmaindustrie selbst Studien manipuliert. Aber Sie machen der Branche weitere schwere Vorwürfe. Sie sprechen sogar von organisierter Kriminalität und Mafia.\n" +
        "\n" +
        "Peter C. Gøtzsche: Ja, der weltweit größte Medikamentenhersteller Pfizer zum Beispiel hat in den USA 2009 nach einem Prozess wegen der illegalen Vermarktung von Arzneimitteln 2,3 Milliarden Dollar gezahlt. Das Unternehmen GlaxoSmithKline war 2011 sogar bereit, drei Milliarden Dollar zu zahlen, um einen Prozess wegen Arzneimittelbetrugs zu beenden. Bei Abbot waren es immerhin 1,5 Milliarden, Eli Lilly zahlte 1,4 Milliarden, Johnson & Johnson 1,1 Milliarden. Bei den anderen großen Unternehmen waren es Summen im zwei- und dreistelligen Millionenbereich. Immer ging es um Betrug und Irreführung, Bestechung oder Vermarktung nicht zugelassener Mittel.\n" +
        "\n" +
        "Diese Straftaten erfüllen die Kriterien für das organisierte Verbrechen, deshalb kann man von Mafia reden. In einem Prozess gegen Pfizer haben die Geschworenen 2010 ausdrücklich festgestellt, dass die Firma über einen Zeitraum von zehn Jahren gegen das sogenannte Rico-Gesetz gegen organisierte Kriminalität verstoßen hat.\n" +
        "\n" +
        "Was ist mit der Firma Roche? Die fehlt in Ihrer Aufzählung.\n" +
        "\n" +
        "Dieses Unternehmen hat 2009 den USA und europäischen Ländern für mehrere Milliarden Euro und Dollar das Grippemittel Tamiflu verkauft. Sie wollten sich mit diesen Vorräten gegen eine Grippe-Epidemie wappnen. Allerdings hatte Roche nur einen Teil der Studien zur Wirksamkeit veröffentlicht. Aufgrund des öffentlichen Druckes haben sie die Daten inzwischen zugänglich gemacht. Demnach nutzt das Mittel noch weniger als befürchtet, kann aber in einigen Fällen schwere Nebenwirkungen auslösen. Meiner Meinung nach hat die Firma so den größten Diebstahl aller Zeiten begangen.\n" +
        "\n" +
        "Sind das nicht Verstöße einzelner schwarzer Schafe in einigen Unternehmen? Und was ist mit kleineren Firmen?\n" +
        "\n" +
        "Ich habe bei meinen Recherchen nicht alle kleinen Firmen berücksichtigt, sondern die wichtigsten Unternehmen. Es arbeiten außerdem natürlich viele anständige Leute in der Pharmaindustrie. Es gibt sogar Kritiker innerhalb der Unternehmen. Aber das sind nicht die, die bestimmen, wo es langgeht. Mir geht es darum, dass das ganze System mit seiner Art, wie Medikamente produziert, vermarktet und überwacht werden, gescheitert ist.\n" +
        "\n" +
        "Sie werfen den Unternehmen vor, dass sie Mittel auf den Markt gedrückt haben, obwohl sie schädlich und für viele Patienten sogar tödlich waren.\n" +
        "\n" +
        "Dafür gibt es etliche Beispiele. Die Pharmaunternehmen sind deshalb sogar schlimmer als die Mafia. Sie bringen viel mehr Menschen um.\n" +
        "\n" +
        "Können Sie Beispiele nennen?\n" +
        "\n" +
        "Etwa Schmerzmittel wie Vioxx, von denen bekannt war, dass sie ein Herzinfarktrisiko darstellen und zum Tod führen können. Vioxx kam ohne ausreichende klinische Dokumentation auf den Markt, weshalb Merck vor Gericht stand und 2011 immerhin 950 Millionen Dollar zahlen musste.\n" +
        "\n" +
        "Bevor es vom Markt genommen wurde, wurde das Mittel bei Rückenschmerzen eingesetzt, bei Tennisarm, bei allen möglichen Leiden. Vielen Patienten wäre es aber schon mit Paracetamol oder auch ganz ohne Medikamente wieder gutgegangen - und jetzt sind sie tot. Das ist eine Tragödie.\n" +
        "\n" +
        "Wissenschaftler der Food and Drug Administration (FDA), also der US-Zulassungsbehörde, haben geschätzt, in den USA könnte Vioxx bis zu 56 000 Patienten getötet haben . . .\n" +
        "\n" +
        "Mit dem Mittel wurden mehr als 80 Millionen Menschen in mehr als 80 Ländern behandelt. Meinen Schätzungen zufolge sind es deshalb etwa 120 000 Todesopfer weltweit gewesen. Und Celebrex von Pfizer, das mit Vioxx vergleichbar ist, wurde dem Unternehmen zufolge bis 2004 weltweit 50 Millionen Menschen verabreicht. Es dürfte bis zu diesem Jahr also etwa 75 000 Patienten getötet haben. Das Mittel wird für einige Krankheiten noch immer verschrieben. Obwohl Pfizer Millionen Dollar zahlen musste, weil sie Studienergebnisse zur Sicherheit des Mittels falsch dargestellt hatten.\n" +
        "\n" +
        "Andere Beispiele für Mittel, die so auf den Markt gedrückt wurden, sind Schlankheitspillen wie Redux und Pondimin, das Epilepsie-Medikament Neurontin, das Antibiotikum Ketek oder das Diabetesmittel Avandia.\n" +
        "\n" +
        "In Ihrem Buch weisen Sie auch auf besondere Probleme mit Psychopharmaka hin.\n" +
        "\n" +
        "Ich schätze, dass allein das Antipsychotikum Zyprexa (Anm. d. Red.: Mittel zur Behandlung schizophrener Psychosen) von Eli Lilly etwa 200 000 der 20 Millionen Patienten, die das Mittel weltweit genommen haben, umgebracht hat. Denn Studien an Alzheimer-Patienten haben gezeigt, dass es unter hundert Patienten, die mit solchen atypischen Antipsychotika behandelt werden, zu einem zusätzlichen Todesfall kommt. Es handelte sich in den Studien zwar um ältere Patienten, die Untersuchungen dauerten aber meist auch nur zehn bis zwölf Wochen. Im realen Leben werden Patienten meist jahrelang behandelt. Außerdem wurde Zyprexa häufig Älteren verordnet, obwohl es etwa für Demenz, Alzheimer und Depressionen gar nicht zugelassen war. Deshalb musste das Unternehmen 1,4 Milliarden Dollar wegen illegaler Vertriebsmethoden bezahlen. Der Umsatz mit Zyprex lag zwischen 1996 und 2009 allerdings bei 39 Milliarden Dollar.\n" +
        "\n" +
        "Auch eine weitere Gruppe Psychopharmaka, die Antidepressiva, ist gefährlich. Ältere Patienten verkraften diese Mittel schlecht. Und es ist bekannt, dass Mittel wie Seroxat (Paxil) von GlaxoSmithKline unter Kindern und Jugendlichen das Suizidrisiko erhöht haben. Außerdem behaupteten die Autoren der wichtigsten Studie zu Seroxat bei schweren Depressionen bei Jugendlichen, das Mittel sei wirksam und sicher. Aber die Ergebnisse belegten das gar nicht, wie eine Überprüfung der Daten gezeigt hat.\n" +
        "\n" +
        "Die Firma hat es dann auch noch als Medikament für Kinder angepriesen, obwohl es dafür gar nicht zugelassen war. Das war einer der Gründe dafür, weshalb sie drei Milliarden Dollar zahlen musste.\n" +
        "\n" +
        "Es gibt Wissenschaftler, die heute wieder sagen, die Suizidgefahr für Kinder und Jugendliche würde nicht erhöht.\n" +
        "\n" +
        "Die FDA und andere Zulassungsbehörden weltweit haben sie offenbar nicht überzeugt, die warnen noch immer davor. Auch der letzte Review der Cochrane Collaboration zu diesen Mitteln bestätigt, dass es Hinweise auf ein erhöhtes Selbsttötungsrisiko gibt. Über neuere Studien wird diskutiert. Aber für mich gibt es keinen Zweifel, dass das Risiko erhöht ist.\n" +
        "\n" +
        "Sie sagen, Medikamente seien in Europa und den USA die dritthäufigste Todesursache nach Herzkrankheiten und Krebs. Das geht aus den Daten etwa des deutschen Statistischen Bundesamtes allerdings nicht hervor.\n" +
        "\n" +
        "Es gibt etliche Studien, die auf verschiedenen Wegen zu diesem Ergebnis kommen, dass es die dritthäufigste Todesursache ist. Für die USA zum Beispiel wird geschätzt, dass jährlich 100 000 Menschen aufgrund von korrekt eingenommenen Medikamenten sterben. Dazu kommen aber noch medizinische Irrtümer: versehentliche Überdosen oder die Mittel sind allein oder in Kombination mit anderen Arzneien für die Patienten gar nicht geeignet.\n" +
        "\n" +
        "Aber wir verdanken auch Medikamenten unsere gute Gesundheit und hohe Lebenserwartung.\n" +
        "\n" +
        "Natürlich gibt es Mittel, die mehr Nutzen als Schaden bieten. Medikamente haben zum Beispiel zu großen Erfolgen im Kampf gegen Infektionen, Herzkrankheiten, einigen Krebsarten und Diabetes vom Typ 1 geführt. Das ist bekannt. Aber im Verhältnis zu der Menge der Mittel, die verschrieben werden, profitieren nur wenige Menschen tatsächlich davon. Weil Kranken viel zu häufig Arzneien verschrieben werden. Weil die Firmen sogar wollen, dass auch gesunde Menschen ihre Mittel nehmen.\n" +
        "\n" +
        "Wie viele der Medikamente, die auf dem Markt sind, brauchen wir Ihrer Meinung nach tatsächlich?\n" +
        "\n" +
        "Ich gehe davon aus, dass wir uns 95 Prozent des Geldes sparen können, das wir für Arzneien ausgeben, ohne dass Patienten Schaden nehmen. Tatsächlich würden mehr Menschen ein längeres und glücklicheres Leben führen können.\n" +
        "\n" +
        "Wenn das stimmen sollte, wieso reagieren Ärzte, Patientenorganisationen und Gesundheitspolitiker nicht viel heftiger darauf?\n" +
        "\n" +
        "Ein Grund ist sicher, dass die Pharmaindustrie extrem mächtig und finanziell unglaublich gut ausgestattet ist. Sie nimmt auf allen Ebenen Einfluss. Zum Beispiel auf Ärzte, die dafür belohnt werden, bestimmte Mittel zu verschreiben - selbst wenn diese teurer als vergleichbare Medikamente sind. Viele Ärzte denken offenbar, sie könnten Geld oder Vergünstigungen von der Industrie akzeptieren und zugleich als Anwälte ihrer Patienten auftreten. Das können sie nicht.\n" +
        "\n" +
        "Außerdem, das belegen ja die Gerichtsverhandlungen eindringlich, verbreiten die Unternehmen immer wieder Geschichten darüber, wie wundervoll ihre Mittel angeblich wirken, und verschweigen zugleich, wie gefährlich sie sind. Die Menschen neigen dazu, ihnen zu glauben.\n" +
        "\n" +
        "Deutschlands Justizminister will jetzt Bestechung im Gesundheitswesen ahnden. Was ist mit den Zulassungsbehörden? Die sollen sicherstellen, dass nur nützliche Mittel auf den Markt kommen.\n" +
        "\n" +
        "Die machen einen ziemlich schlechten Job. Das ist vor allem von der Food and Drug Administration (FDA) in den USA bekannt. In dieser Behörde gibt es eine Menge Interessenkonflikte und Korruption. Im Zweifel entscheidet die Behörde deshalb eher zugunsten der Pharmaindustrie für Medikamente als zugunsten der Patienten dagegen. FDA-Wissenschaftler müssen immer wieder gegen ihre eigenen Vorgesetzten und die Beratungsgremien ankämpfen, wenn sie Kritik an Mitteln und dem Umgang damit üben.\n" +
        "\n" +
        "Darüber haben sich Experten der Behörde selbst immer wieder beschwert - sogar in einem Brief an das Wahlkampfteam von Barack Obama. Wegen ihrer Kritik hat die FDA sogar die privaten E-Mails von Wissenschaftlern, die sich an Kongress-Mitglieder, Anwälte oder Journalisten gewandt haben, überwacht.\n" +
        "\n" +
        "Ronald Kavanagh, ein FDA-Whistleblower, hat über seine Arbeit bei der Behörde berichtet, dass die Wissenschaftler manchmal geradezu angewiesen wurden, die Behauptungen der Pharmaunternehmen zu akzeptieren, ohne die Daten zu prüfen. Über die anderen Behörden wissen wir nicht so viel. Aber sie müssten viel kritischer sein. Die Regulierung von Medikamenten ist ja offensichtlich nicht effektiv.\n" +
        "\n" +
        "Noch einmal zu den klinischen Studien: Sie behaupten, die Studien der Pharmabranche taugen lediglich als Werbung für die Medikamente.\n" +
        "\n" +
        "Studien, die von den Unternehmen finanziert werden, haben häufiger Ergebnisse, die für diese vorteilhaft ausfallen. Das ist belegt. Der Industrie zu erlauben, ihre eigenen Medikamente zu testen, ist so, als dürfte ich in einem Prozess mein eigener Richter sein. Und Wissenschaftler, die an dem Design einer Studie zu viel Kritik üben, werden das nächste Mal nicht mehr gefragt. Das wissen die Betroffenen. Schon deshalb kommen sie den Wünschen der Industrie viel zu weit entgegen. Unerwünschte Ergebnisse werden außerdem gerne verschwiegen, während erwünschte veröffentlicht werden.\n" +
        "\n" +
        "Die Studien sollten deshalb nie von der Pharmaindustrie, sondern immer von unabhängigen Wissenschaftlern vorgenommen werden.\n" +
        "\n" +
        "Wissen die Fachjournale, in denen die Studien veröffentlicht werden, nicht, was gespielt wird? Müssten sie die Veröffentlichung von solchen Tests nicht verweigern?\n" +
        "\n" +
        "Die Journale sind auch Teil des Problems. Sie leiden unter erheblichen Interessenkonflikten. Die renommiertesten Fachmagazine verdienen zum Beispiel eine Menge Geld mit dem Verkauf von Sonderdrucken an Firmen, mit denen diese dann werben. Deshalb stehen die Journale unter Druck, Manuskripte der Pharmaindustrie zu akzeptieren. So kommt es, dass auch Studien mit falschen oder irreführenden Aussagen veröffentlicht werden. Dafür gibt es etliche Beispiele. Richard Smith, ein früherer Herausgeber des British Medical Journal, hat selbst einen ganzen Artikel veröffentlicht unter dem Titel: \"Medizinische Fachzeitschriften sind ein verlängerter Arm der Marketingabteilungen der Pharmafirmen\".\n" +
        "\n" +
        "Vor einigen Jahren hat ein Insider aus der Industrie dem Journal selbst gesteckt, es sei schwieriger, dort einen wohlwollenden Artikel zu veröffentlichen als in anderen Zeitungen. Aber wenn es gelänge, sei das für das Unternehmen 200 Millionen Pfund wert. Es gibt bei vielen Fachzeitungen aber inzwischen Bestrebungen, hier etwas zu ändern.\n" +
        "\n" +
        "Was müsste sich Ihrer Meinung sonst noch konkret ändern?\n" +
        "\n" +
        "Wir brauchen eine Revolution im Gesundheitswesen: Unabhängige Medikamenten-Tests, für die die Industrie weiterhin zahlen könnte. Sonst sollte sie absolut nichts damit zu tun haben. Alle Studiendaten müssen offengelegt werden - auch negative Ergebnisse. Als Ärzte müssen wir beginnen, Nein zu sagen zum Geld und zu anderen Gefälligkeiten der Pharmaindustrie.\n" +
        "\n" +
        "Außerdem sollte Werbung für Medikamente - auch innerhalb von Fachkreisen - verboten werden, genau wie bei Tabakprodukten. In beiden Fällen gibt es ein Gesundheits- und Todesrisiko. Und wenn ein Medikament gut ist, können wir sicher sein, dass Ärzte es einsetzen.\n" +
        "\n" +
        "Die Links in diesem Text weisen auf eine kleine Auswahl aus einer großen Anzahl von Quellen, mit denen Gøtzsche seine Kritik in seinem Buch begründet:\n" +
        "\n" +
        "Peter C. Gøtzsche: Tödliche Medizin und organisierte Kriminalität - Wie die Pharmaindustrie das Gesundheitswesen korrumpiert. riva Verlag München, 2014. 512 Seiten, " +
        "ISBN 978-3-86883-438-3, 24,99 €");

    Assert.assertEquals("de", longGermanTextLanguage);
  }
}

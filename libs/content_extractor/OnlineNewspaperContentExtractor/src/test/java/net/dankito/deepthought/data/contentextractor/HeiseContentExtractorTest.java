package net.dankito.deepthought.data.contentextractor;

import net.dankito.deepthought.data.contentextractor.preview.ArticlesOverviewItem;
import net.dankito.deepthought.data.contentextractor.preview.ArticlesOverviewListener;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by ganymed on 13/06/15.
 */
public class HeiseContentExtractorTest extends GermanOnlineNewspaperContentExtractorTestBase {

  protected net.dankito.deepthought.data.contentextractor.HeiseContentExtractor heiseContentExtractor = null;


  @Override
  protected net.dankito.deepthought.data.contentextractor.OnlineNewspaperContentExtractorBase createOnlineNewspaperContentExtractor() {
    heiseContentExtractor = new HeiseContentExtractor();
    return heiseContentExtractor;
  }


  @Test
  public void importMerkelAufDemItGipfelArticle() {
    EntryCreationResult creationResult = testImportArticle("http://www.heise.de/newsticker/meldung/Merkel-auf-dem-IT-Gipfel-Datenschutz-darf-Big-Data-nicht-verhindern-2980126.html");
    testImportedArticleValues(creationResult, 5685, "19.11.2015", "Datenschutz darf Big Data nicht verhindern", "Merkel auf dem IT-Gipfel",
        "Bundeskanzlerin Angela Merkel setzt sich dafür ein, Prinzipien wie Zweckbindung und Datensparsamkeit im EU-Datenschutzrecht auszuhebeln. Es sei \"existenziell notwendig\", den entsprechenden Kompromiss zu erhalten.");
  }

  @Test
  public void importArticleContainingImages() {
    EntryCreationResult creationResult = testImportArticle("http://www.heise.de/newsticker/meldung/IT-Gipfel-Gabriel-plaediert-fuer-Datensouveraenitaet-statt-Datenschutz-2966141.html");
    testImportedArticleValues(creationResult, 6027, "19.11.2015", "Gabriel plädiert für Datensouveränität statt Datenschutz", "IT-Gipfel",
        "\"Wir brauchen ein anderes Verständnis von Datenschutz\", verkündete Wirtschaftsminister Sigmar Gabriel zur Eröffnung des 9. Nationalen IT-Gipfels. Datensparsamkeit passe nicht zum Geschäftsmodell Big Data.");
  }

  @Test
  public void importArticleContainingVideo() {
    EntryCreationResult creationResult = testImportArticle("http://www.heise.de/newsticker/meldung/Hacker-starten-Stratosphaerenballon-um-Drohnen-Funk-mitzuschneiden-2823100.html");
    testImportedArticleValues(creationResult, 5514, "22.09.2015", "Hacker starten Stratosphärenballon, um Drohnen-Funk mitzuschneiden", "",
        "Mit der geeigneten Technik kann man große Teile des Spektrums scannen und Funkverkehr aufspüren – vom Boden aus fehlt aber das, was in großen Höhen gefunkt wird, zum Beispiel von Drohnen zu Satelliten.");
  }

  @Test
  public void importArticleContainingImageGallery() {
    EntryCreationResult creationResult = testImportArticle("http://www.heise.de/newsticker/meldung/heise-Foto-Galerie-Das-sind-die-Top-50-des-Jahres-2015-3044404.html");
    testImportedArticleValues(creationResult, 5514, "25.12.2015", "Das sind die Top 50 des Jahres 2015", "heise Foto Galerie",
        "Tausende neue Bilder, zehntausende Kommentare: Aber welches Bild hatte 2015 die Nase vorn? Wir haben unsere Statistik befragt: Hier sind die Top 50 der meistbewerteten Neuzugänge der vergangenen 365 Tage der heise Foto Galerie.");
  }


  @Test
  public void importPressePhotoDesJahres_ImageGalleryGetsImportedCorrectly() {
    EntryCreationResult creationResult = testImportArticle("http://www.heise.de/newsticker/meldung/Welt-Presse-Foto-des-Jahres-Das-Fluechtlingselend-im-Stacheldraht-3112090.html?hg=1&hgi=1&hgf=false");
    testImportedArticleValues(creationResult, 5514, "25.12.2015", "Das sind die Top 50 des Jahres 2015", "heise Foto Galerie",
        "Tausende neue Bilder, zehntausende Kommentare: Aber welches Bild hatte 2015 die Nase vorn? Wir haben unsere Statistik befragt: Hier sind die Top 50 der meistbewerteten Neuzugänge der vergangenen 365 Tage der heise Foto Galerie.");
  }


  @Test
  public void importHeiseSecurityArticle_AbstractAndReferenceGetImportedCorrectly() {
    EntryCreationResult creationResult = testImportArticle("http://www.heise.de/security/artikel/Der-WhatsApp-Verschluesselung-auf-die-Finger-geschaut-2629020.html");
    testImportedArticleValues(creationResult, 5514, "25.12.2015", "Das sind die Top 50 des Jahres 2015", "heise Foto Galerie",
        "Tausende neue Bilder, zehntausende Kommentare: Aber welches Bild hatte 2015 die Nase vorn? Wir haben unsere Statistik befragt: Hier sind die Top 50 der meistbewerteten Neuzugänge der vergangenen 365 Tage der heise Foto Galerie.");
  }


  @Test
  public void importKommentar_AbstractAndReferenceGetImportedCorrectly() {
    EntryCreationResult creationResult = testImportArticle("http://www.heise.de/security/artikel/Warum-Google-uns-echte-Verschluesselung-verweigert-2191797.html");
    testImportedArticleValues(creationResult, 5514, "25.12.2015", "Das sind die Top 50 des Jahres 2015", "heise Foto Galerie",
        "Tausende neue Bilder, zehntausende Kommentare: Aber welches Bild hatte 2015 die Nase vorn? Wir haben unsere Statistik befragt: Hier sind die Top 50 der meistbewerteten Neuzugänge der vergangenen 365 Tage der heise Foto Galerie.");
  }


  @Test
  public void importAndroidUpdate_EnumerationGetsImportedCorrectly() {
    EntryCreationResult creationResult = testImportArticle("http://www.heise.de/newsticker/meldung/Google-I-O-Hintergrund-Updates-von-Android-N-beheben-nicht-die-Update-Problematik-3211383.html");
    testImportedArticleValues(creationResult, 5514, "25.12.2015", "Das sind die Top 50 des Jahres 2015", "heise Foto Galerie",
        "Tausende neue Bilder, zehntausende Kommentare: Aber welches Bild hatte 2015 die Nase vorn? Wir haben unsere Statistik befragt: Hier sind die Top 50 der meistbewerteten Neuzugänge der vergangenen 365 Tage der heise Foto Galerie.");
  }


  @Test
  public void importIntelVerankertAntiExploitTechnik_CodeExampleGetsImportedCorrectly() {
    EntryCreationResult creationResult = testImportArticle("http://www.heise.de/newsticker/meldung/Intel-verankert-Anti-Exploit-Technik-in-CPU-Hardware-3236707.html");
    testImportedArticleValues(creationResult, 3243, "13.06.16", "Intel verankert Anti-Exploit-Technik in (CPU-)Hardware", "",
        "Mit der \"Control-flow Enforcement Technology\" will Intel dem Ausnutzen von Sicherheitslücken eine weitere Hürde in den Weg legen. Wann CET jedoch in Prozessoren debütiert, steht noch in den Sternen.");
  }


  @Test
  public void testGetArticlesOverview() {
    final List<ArticlesOverviewItem> allItems = new ArrayList<>();
    final AtomicInteger partialItemsExtractionCall = new AtomicInteger();
    final CountDownLatch getArticlesOverviewLatch = new CountDownLatch(1);

    contentExtractor.getArticlesOverviewAsync(new ArticlesOverviewListener() {
      @Override
      public void overviewItemsRetrieved(IOnlineArticleContentExtractor contentExtractor, Collection<ArticlesOverviewItem> items, boolean isDone) {
        partialItemsExtractionCall.incrementAndGet();
        allItems.addAll(items);

        Assert.assertNotNull(contentExtractor);
        Assert.assertFalse(items.size() == 0);

        if (isDone)
          getArticlesOverviewLatch.countDown();
      }
    });

    try { getArticlesOverviewLatch.await(10, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertEquals(2, partialItemsExtractionCall.get());
    Assert.assertTrue(allItems.size() > 10);
  }


}

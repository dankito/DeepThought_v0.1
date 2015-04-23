package net.deepthought.data.importer_exporter;

import net.deepthought.Application;
import net.deepthought.DefaultDependencyResolver;
import net.deepthought.data.TestApplicationConfiguration;
import net.deepthought.data.helper.MockEntityManager;
import net.deepthought.data.model.Entry;
import net.deepthought.data.persistence.EntityManagerConfiguration;
import net.deepthought.data.persistence.IEntityManager;
import net.deepthought.util.StringUtils;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by ganymed on 14/04/15.
 */
public class SueddeutscheImporterTest {

  protected SueddeutscheImporter importer = null;


  @Before
  public void setup() {
    importer = new SueddeutscheImporter();

    Application.instantiate(new TestApplicationConfiguration(), new DefaultDependencyResolver() {
      @Override
      public IEntityManager createEntityManager(EntityManagerConfiguration configuration) throws Exception {
        return new MockEntityManager();
      }
    });
  }

  @After
  public void tearDown() {
    Application.shutdown();
  }

  @Test
  public void importArticleToAvoidOnlyTenArticlesAWeekLimit() {
    Entry importedEntry = testImportArticle("http://www.sueddeutsche.de/karriere/frage-an-den-sz-jobcoach-finde-ich-keine-stelle-weil-ich-nicht-auf-facebook-bin-1.2437827");
  }

  @Test
  public void importSoVermeidenSieFehlerImTestamentArticle() {
      Entry importedEntry = testImportArticle("http://www.sueddeutsche.de/geld/richtig-vererben-so-vermeiden-sie-fehler-im-testament-1.2406029");
      testImportedArticleValues(importedEntry, null, "14.04.2015", "So vermeiden Sie Fehler im Testament", "Richtig vererben",
          "Streit ums Erbe entsteht oft dann, wenn der letzte Wille des Erblassers nicht eindeutig oder am Ende gar unwirksam formuliert ist. Die " +
              "häufigsten Fehler beim Verfassen eines Testaments - und wie man den Nachkommen das Erben erleichtert.");
  }

  @Test
  public void importSiliconValleyGegenDieNSAArticle() {
    Entry importedEntry = testImportArticle("http://www.sueddeutsche.de/digital/kryptografie-debatte-silicon-valley-gegen-die-nsa-1.2432893");
    testImportedArticleValues(importedEntry, null, "13.04.2015", "Silicon Valley gegen die NSA", "Kryptografie-Debatte",
        "<ul> \n" +
            " <li>NSA-Chef Michael Rogers hat erklärt, wie er sich die Smartphone-Überwachung vorstellt.</li> \n" +
            " <li>Er fordert, dass die Schlüssel verteilt hinterlegt werden.</li> \n" +
            " <li>Sicherheitsexperten kritisieren den Vorschlag.</li> \n" +
            "</ul>");
  }

  @Test
  public void importDiePharmaindustrieIstSchlimmerAlsDieMafiaArticle() {
    Entry importedEntry = testImportArticle("http://www.sueddeutsche.de/gesundheit/kritik-an-arzneimittelherstellern-die-pharmaindustrie-ist-schlimmer-als-die-mafia-1.2267631#");
    testImportedArticleValues(importedEntry, null, "06.02.2015", "\"Die Pharmaindustrie ist schlimmer als die Mafia\"", "Kritik an Arzneimittelherstellern",
        "Medikamente sollen uns ein langes, gesundes Leben bescheren. Doch die Pharmaindustrie bringt mehr Menschen um als die Mafia, sagt der dänische Mediziner Peter C. Gøtzsche - und fordert für die Branche eine Revolution.");
  }

  protected Entry testImportArticle(String articleUrl) {
    Entry importedEntry = importer.importArticle(articleUrl);

    Assert.assertNotNull(importedEntry);
    Assert.assertTrue(StringUtils.isNotNullOrEmpty(importedEntry.getAbstract()));

    Assert.assertNotNull(importedEntry.getSeries());
    Assert.assertEquals("SZ", importedEntry.getSeries().getTitle());

    Assert.assertNotNull(importedEntry.getReference());
    Assert.assertNotNull(importedEntry.getReference().getIssueOrPublishingDate());
    Assert.assertNotNull(importedEntry.getReference().getPublishingDate());

    Assert.assertNotNull(importedEntry.getReferenceSubDivision());
    Assert.assertTrue(StringUtils.isNotNullOrEmpty(importedEntry.getReferenceSubDivision().getTitle()));
    Assert.assertTrue(StringUtils.isNotNullOrEmpty(importedEntry.getReferenceSubDivision().getSubTitle()));
    Assert.assertEquals(articleUrl, importedEntry.getReferenceSubDivision().getOnlineAddress());

    return importedEntry;
  }

  protected void testImportedArticleValues(Entry importedEntry, String content, String issueOrPublishingDate, String referenceTitle, String referenceSubTitle, String abstractString) {
    if(content != null)
      Assert.assertEquals(content, importedEntry.getContent());

    Assert.assertEquals(issueOrPublishingDate, importedEntry.getReference().getIssueOrPublishingDate());

    Assert.assertEquals(referenceTitle, importedEntry.getReferenceSubDivision().getTitle());
    Assert.assertEquals(referenceTitle, importedEntry.getReferenceSubDivision().getTitle());

    Assert.assertEquals(abstractString, importedEntry.getAbstract());
  }
}

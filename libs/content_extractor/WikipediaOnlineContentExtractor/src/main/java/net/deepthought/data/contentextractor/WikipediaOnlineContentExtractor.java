package net.deepthought.data.contentextractor;

import net.deepthought.Application;
import net.deepthought.data.contentextractor.preview.ArticlesOverviewItem;
import net.deepthought.data.contentextractor.preview.ArticlesOverviewListener;
import net.deepthought.data.model.Category;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.Reference;
import net.deepthought.data.model.SeriesTitle;
import net.deepthought.util.DeepThoughtError;
import net.deepthought.util.Localization;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class WikipediaOnlineContentExtractor extends OnlineArticleContentExtractorBase {

  private final static Logger log = LoggerFactory.getLogger(WikipediaOnlineContentExtractor.class);


  @Override
  public String getName() {
    return Localization.getLocalizedStringForResourceKey("named.content.extractor", "Wikipedia");
  }

  @Override
  public String getSiteBaseUrl() {
    //return "Wikipedia.org";
    return "Wikipedia";
  }

  @Override
  public String getIconUrl() {
    return null; // TODO
  }

  @Override
  public boolean hasArticlesOverview() {
    return false;
  }

  @Override
  public void getArticlesOverviewAsync(ArticlesOverviewListener listener) {
    listener.overviewItemsRetrieved(this, new ArrayList<ArticlesOverviewItem>(), true);
  }

  @Override
  public boolean canCreateEntryFromUrl(String url) {
    return url.contains(".wikipedia.org/wiki/");
  }

  @Override
  protected EntryCreationResult parseHtmlToEntry(String articleUrl, Document document) {
    try {
      Element contentElement = document.body().getElementById("content");

      Reference reference = createReference(articleUrl, contentElement);

      Entry articleEntry = new Entry(extractContent(contentElement), extractTitle(contentElement));
      articleEntry.setReference(reference);

      articleEntry.addTag(Application.getDeepThought().findOrCreateTagForName("Wikipedia"));

      Category encyclopaediaeCategory = Application.getDeepThought().findOrCreateTopLevelCategoryForName("Lexika");
      Category wikipediaCategory = Application.getDeepThought().findOrCreateSubCategoryForName(encyclopaediaeCategory, "Wikipedia");
      wikipediaCategory.addEntry(articleEntry);

      return new EntryCreationResult(document.baseUri(), articleEntry);
    } catch(Exception ex) {
      return new EntryCreationResult(document.baseUri(), new DeepThoughtError(Localization.getLocalizedStringForResourceKey("could.not.create.entry.from.article.html"), ex));
    }
  }

  protected String extractContent(Element contentElement) {
    Element contentTextElement = contentElement.getElementById("mw-content-text");
    if(contentTextElement != null)
      return adjustContent(contentTextElement);

    log.error("Could not find 'mw-content-text' to extract article's content text");
    return "";
  }

  protected String adjustContent(Element contentTextElement) {
    Element redirectElement = contentTextElement.getElementById("Vorlage_Weiterleitungshinweis");
    if(redirectElement != null)
      redirectElement.remove();

    Elements referencesMissingElements = contentTextElement.getElementsByClass("Vorlage_Belege_fehlen");
    for(Element referencesMissingElement : referencesMissingElements)
      referencesMissingElement.remove();

    String contentHtml = contentTextElement.outerHtml();
    String baseUrl = contentTextElement.baseUri();
    baseUrl = baseUrl.substring(0, baseUrl.indexOf("/wiki/"));

    contentHtml = contentHtml.replace("\"//upload.", "\"https://upload.");
    contentHtml = contentHtml.replace("\"/wiki/", "\"" + baseUrl + "/wiki/");

    return contentHtml;
  }

  protected String extractTitle(Element contentElement) {
    Element headingElement = contentElement.getElementById("firstHeading");
    if(headingElement != null)
      return headingElement.text();

    log.error("Could not find 'firstHeading' to extract article's title");
    return null;
  }

  protected Reference createReference(String articleUrl, Element contentElement) {
    Reference articleReference = new Reference(extractTitle(contentElement));
    articleReference.setOnlineAddress(articleUrl);

    Element lastModifiedElement = contentElement.getElementById("footer-info-lastmod");
    // TODO: parse last modification date (will be different for every language!)
//    if(lastModifiedElement != null)
//      articleReference.setIssueOrPublishingDate(lastModifiedElement.);

    if(Application.getDeepThought() != null) {
      SeriesTitle wikipediaSeries = Application.getDeepThought().findOrCreateSeriesTitleForTitle("Wikipedia");
      wikipediaSeries.addSerialPart(articleReference);
    }

    return articleReference;
  }
}

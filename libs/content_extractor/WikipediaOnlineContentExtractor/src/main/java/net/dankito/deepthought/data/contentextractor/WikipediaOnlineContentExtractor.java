package net.dankito.deepthought.data.contentextractor;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.data.contentextractor.preview.ArticlesOverviewItem;
import net.dankito.deepthought.data.contentextractor.preview.ArticlesOverviewListener;
import net.dankito.deepthought.data.model.Category;
import net.dankito.deepthought.data.model.Entry;
import net.dankito.deepthought.data.model.Reference;
import net.dankito.deepthought.data.model.SeriesTitle;
import net.dankito.deepthought.plugin.IPlugin;
import net.dankito.deepthought.util.DeepThoughtError;
import net.dankito.deepthought.util.localization.Localization;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class WikipediaOnlineContentExtractor extends OnlineArticleContentExtractorBase implements IPlugin {

  private final static Logger log = LoggerFactory.getLogger(WikipediaOnlineContentExtractor.class);


  @Override
  public int getSupportedPluginSystemVersion() {
    return 1;
  }

  @Override
  public String getPluginVersion() {
    return "0.1";
  }


  @Override
  public String getName() {
    return Localization.getLocalizedString("named.content.extractor", "Wikipedia");
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
  public ContentExtractOptions createExtractOptionsForUrl(String url) {
    ContentExtractOptions options = new ContentExtractOptions(url, getSiteBaseUrl());

    options.addContentExtractOption(new ContentExtractOption(this, url, true, "create.entry.from.online.article", new ExtractContentAction() {
      @Override
      public void runExtraction(ContentExtractOption option, ExtractContentActionResultListener listener) {
        listener.extractingContentDone(createEntryFromArticle(option.getUrl()));
      }
    }));

    return options;
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

      Entry articleEntry = new Entry(extractContent(contentElement), extractTitle(contentElement));
      EntryCreationResult creationResult = new EntryCreationResult(articleUrl, articleEntry);

      createReference(creationResult, articleUrl, contentElement);

      creationResult.addTag(Application.getEntitiesSearcherAndCreator().findOrCreateTagForName("Wikipedia"));

      Category encyclopaediaeCategory = Application.getEntitiesSearcherAndCreator().findOrCreateTopLevelCategoryForName("Lexika");
      Category wikipediaCategory = Application.getEntitiesSearcherAndCreator().findOrCreateSubCategoryForName(encyclopaediaeCategory, "Wikipedia");
      creationResult.addCategory(wikipediaCategory);

      return creationResult;
    } catch(Exception ex) {
      return new EntryCreationResult(document.baseUri(), new DeepThoughtError(Localization.getLocalizedString("could.not.create.entry.from.article.html"), ex));
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

  protected Reference createReference(EntryCreationResult creationResult, String articleUrl, Element contentElement) {
    Reference articleReference = new Reference(extractTitle(contentElement));
    articleReference.setOnlineAddressAndLastAccessToCurrentDateTime(articleUrl);

    Element lastModifiedElement = contentElement.getElementById("footer-info-lastmod");
    // TODO: parse last modification date (will be different for every language!)
//    if(lastModifiedElement != null)
//      articleReference.setIssueOrPublishingDate(lastModifiedElement.);

    creationResult.setReference(articleReference);

    if(Application.getDeepThought() != null) {
      SeriesTitle wikipediaSeries = Application.getEntitiesSearcherAndCreator().findOrCreateSeriesTitleForTitle("Wikipedia");
      creationResult.setSeriesTitle(wikipediaSeries);
    }

    return articleReference;
  }
}

package net.dankito.deepthought.data.contentextractor;

import net.dankito.deepthought.data.contentextractor.preview.ArticlesOverviewItem;
import net.dankito.deepthought.data.contentextractor.preview.ArticlesOverviewListener;
import net.dankito.deepthought.data.model.Entry;
import net.dankito.deepthought.data.model.ReferenceSubDivision;
import net.dankito.deepthought.util.DeepThoughtError;
import net.dankito.deepthought.util.StringUtils;
import net.dankito.deepthought.util.localization.Localization;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by ganymed on 20/06/15.
 */
public class CtContentExtractor extends OnlineNewspaperContentExtractorBase {

  private final static Logger log = LoggerFactory.getLogger(CtContentExtractor.class);

  protected static final String LogoFileName = "heise_online_logo.png";


  @Override
  public int getSupportedPluginSystemVersion() {
    return 1;
  }

  @Override
  public String getPluginVersion() {
    return "0.1";
  }


  @Override
  public String getNewspaperName() {
    return "c't";
  }

  @Override
  public String getSiteBaseUrl() {
    return "heise.de/ct";
  }

  @Override
  protected String getUrlPrefixForMakingRelativeLinkAbsolute(String relativeUrl) {
    return "http://www.heise.de";
  }

  @Override
  public String getIconUrl() {
//    return "http://www.heise.de/icons/ho/apple-touch-icon-152.png";
//    return "http://www.heise.de/icons/ho/apple-touch-icon-120.png";
    return tryToLoadIconFile(LogoFileName);
  }


  @Override
  public boolean canCreateEntryFromUrl(String url) {
    return url.toLowerCase().contains("www.heise.de/ct/");
  }

  protected EntryCreationResult parseHtmlToEntry(String articleUrl, Document document) {
    try {
      Element sectionElement = document.body().select("main section").first();
      if(sectionElement == null)
        return new EntryCreationResult(articleUrl, new DeepThoughtError(Localization.getLocalizedString("could.not.create.entry.from.article.html")));

      Entry articleEntry = createEntry(sectionElement);
      EntryCreationResult creationResult = new EntryCreationResult(articleUrl, articleEntry);

      createReference(creationResult, articleUrl, sectionElement);

      findOrCreateTagAndAddToCreationResult(creationResult);
      addNewspaperCategory(creationResult, true);

      return creationResult;
    } catch(Exception ex) {
      return new EntryCreationResult(articleUrl, new DeepThoughtError(Localization.getLocalizedString("could.not.create.entry.from.article.html"), ex));
    }
  }

  protected Entry createEntry(Element sectionElement) {
    String abstractString = sectionElement.select("p.article_page_intro strong").html();

    String content = "";

    adjustLinkUrls(sectionElement);
    adjustSourceElements(sectionElement);

    Element pageImgElement = sectionElement.select("div.article_page_img img").first();
    if(pageImgElement != null) {
      content += pageImgElement.outerHtml();
    }

    Element articlePageTextElement = sectionElement.select("div.article_page_text").first();
    if(articlePageTextElement == null) {
      articlePageTextElement = sectionElement;
    }
    Element articlePageElement = articlePageTextElement.select("div.article_text").first();
    if(articlePageElement != null) {
      articlePageTextElement = articlePageElement;
    }

    for(Element childElement : articlePageTextElement.children()) {
      if(isArticleElement(childElement)) {
        content += childElement.outerHtml();
      }
    }

    content += extractPatchesIfAny(sectionElement);

    return new Entry(content, abstractString);
  }

  protected boolean isArticleElement(Element childElement) {
    return StringUtils.isNotNullOrEmpty(childElement.text());
  }

  protected String extractPatchesIfAny(Element sectionElement) {
    String patchesHtml = "";
    Element sectionElementParent = sectionElement.parent();

    Element patchTitleElement = sectionElementParent.select("p.article_page_patch_title").first();
    if(patchTitleElement != null) {
      patchesHtml += "<hr/>";
      patchesHtml += patchTitleElement.outerHtml();

      Element nextSibling = patchTitleElement.nextElementSibling();
      if(nextSibling != null && "p".equals(nextSibling.nodeName()) && nextSibling.classNames().size() == 1 && "".equals(nextSibling.className())) {
        patchesHtml += nextSibling.outerHtml();
      }
    }

    Element patchDateElement = sectionElementParent.select("p.article_page_patch_date").first();
    if(patchDateElement != null) {
      patchesHtml += patchDateElement.outerHtml();
    }

    Element patchTextElement = sectionElementParent.select("div.article_page_patch_text").first();
    if(patchTextElement != null) {
      patchesHtml += patchTextElement.outerHtml();
    }

    return patchesHtml;
  }


  protected ReferenceSubDivision createReference(EntryCreationResult creationResult, String articleUrl, Element sectionElement) {
    Element headerElement = sectionElement.select("header").first();

    String title = headerElement.select("h1").text();

    String subTitle = "";
    Element subTitleElement = headerElement.select("h2").first();
    if(subTitleElement != null) {
      subTitle = subTitleElement.text();
    }

    String publishingDateString = "";
    Element articleDateTimeElement = sectionElement.select("time").first();
    if(articleDateTimeElement != null) {
      publishingDateString = parseDate(articleDateTimeElement);
    }

    ReferenceSubDivision articleReference = new ReferenceSubDivision(title, subTitle);
    articleReference.setOnlineAddressAndLastAccessToCurrentDateTime(articleUrl);

    setArticleReference(creationResult, articleReference, publishingDateString);

    return articleReference;
  }


  protected DateFormat heiseDateTimeFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.GERMAN);

  protected String parseDate(Element articleDateTimeElement) {
    String publishingDateString = "";

    if(articleDateTimeElement.hasAttr("datetime")) {
      publishingDateString = parseIsoDateTimeWithoutTimezoneStringWithoutTimezone(articleDateTimeElement.attr("datetime"), publishingDateString);
    }

    if(StringUtils.isNullOrEmpty(publishingDateString)) {
      Date publishingDate = parseHeiseDateTimeFormat(articleDateTimeElement.text());
      if (publishingDate != null)
        publishingDateString = formatDateToDeepThoughtDateString(publishingDate);
    }

    return publishingDateString;
  }

  protected Date parseHeiseDateTimeFormat(String articleDateTime) {
    articleDateTime = articleDateTime.replace("&nbsp;", "");
    articleDateTime = articleDateTime.replace(" Uhr", "").trim();

    try {
      Date parsedDate = heiseDateTimeFormat.parse(articleDateTime);
      return parsedDate;
    } catch(Exception ex) { log.error("Could not parse Heise DateTime Format " + articleDateTime, ex); }

    return null;
  }


  @Override
  public boolean hasArticlesOverview() {
    return true;
  }

  @Override
  protected void getArticlesOverview(ArticlesOverviewListener listener) {
    extractArticlesOverviewFromFrontPage(listener);
  }


  protected void extractArticlesOverviewFromFrontPage(ArticlesOverviewListener listener) {
    try {
      Document frontPage = retrieveOnlineDocument("http://www.heise.de");
      extractArticlesOverviewItemsFromFrontPage(frontPage, listener);
    } catch(Exception ex) {
      log.error("Could not retrieve HTML code of Heise.de front page", ex);
    }
  }

  protected void extractArticlesOverviewItemsFromFrontPage(Document frontPage, ArticlesOverviewListener listener) {
    List<ArticlesOverviewItem> overviewItems = new ArrayList<>();
    extractTopTeaserItems(frontPage, overviewItems);
    listener.overviewItemsRetrieved(this, overviewItems, false);

    List<ArticlesOverviewItem> indexItems = new ArrayList<>();
    extractIndexItems(frontPage, indexItems);
    listener.overviewItemsRetrieved(this, indexItems, true);
  }

  protected void extractTopTeaserItems(Document frontPage, List<ArticlesOverviewItem> overviewItems) {
    for(Element teaserItem : frontPage.body().select(".topteaser_master")) {
      createOverviewItemFromTeaserItem(overviewItems, teaserItem);
    }
  }

  protected void createOverviewItemFromTeaserItem(List<ArticlesOverviewItem> overviewItems, Element teaserItem) {
    if(teaserItem.children().size() == 1 && "a".equals(teaserItem.child(0).tagName())) {
      overviewItems.add(createOverviewItemFromTeaserAnchorElement(teaserItem.child(0)));
    }
    else {
      createOverviewItemsFromMultipleElements(overviewItems, teaserItem);
    }
  }

  protected void createOverviewItemsFromMultipleElements(List<ArticlesOverviewItem> overviewItems, Element teaserItemWithMultipleElements) {
    for(Element teaserItemAnchor : teaserItemWithMultipleElements.select(".multiple a.the_content_url")) {
      overviewItems.add(createOverviewItemFromTeaserAnchorElement(teaserItemAnchor));
    }
  }

  protected ArticlesOverviewItem createOverviewItemFromTeaserAnchorElement(Element teaserItemAnchor) {
    String url = teaserItemAnchor.attr("href");
    url = makeLinkAbsolute(url);
    String subTitle = teaserItemAnchor.select("b.dachzeile").text();
    String title = teaserItemAnchor.select("h2").text();
    String summary = teaserItemAnchor.select("p").text();
    Element previewImageElement = teaserItemAnchor.select("div.img_clip img").first();
    String previewImageUrl = (previewImageElement != null && previewImageElement.hasAttr("src")) ?
        previewImageElement.attr("src") : "";
    previewImageUrl = makeLinkAbsolute(previewImageUrl);

    return new ArticlesOverviewItem(this, url, summary, title, subTitle, previewImageUrl);
  }


  protected void extractIndexItems(Document frontPage, List<ArticlesOverviewItem> overviewItems) {
    Element indexListElement = frontPage.body().select(".indexlist").first();
    if(indexListElement != null) {
      for(Element indexItem : indexListElement.select(".indexlist_item")) {
        overviewItems.add(createOverviewItemFromIndexListItem(indexItem));
      }
    }
  }

  protected ArticlesOverviewItem createOverviewItemFromIndexListItem(Element indexItem) {
    Element indexListAnchor = indexItem.select("a.indexlist_text").first();
    if(indexListAnchor != null) {
      return createOverviewItemFromIndexListAnchor(indexListAnchor, indexItem.select("header h3").text());
    }

    return null;
  }

  protected ArticlesOverviewItem createOverviewItemFromIndexListAnchor(Element indexListAnchor, String title) {
    String url = indexListAnchor.attr("href");
    url = makeLinkAbsolute(url);
    String summary = indexListAnchor.select("p").text();

    Element previewImageElement = indexListAnchor.select("figure img").first();
    String previewImageUrl = (previewImageElement != null && previewImageElement.hasAttr("src")) ? previewImageElement.attr("src") : "";
    previewImageUrl = makeLinkAbsolute(previewImageUrl);

    String subTitle = "";
    if(title.contains(":")) {
      int indexOfColon = title.indexOf(':');
      subTitle = title.substring(0, indexOfColon).trim();
      title = title.substring(indexOfColon + 1).trim();
    }

    return new ArticlesOverviewItem(this, url, summary, title, subTitle, previewImageUrl);
  }
}

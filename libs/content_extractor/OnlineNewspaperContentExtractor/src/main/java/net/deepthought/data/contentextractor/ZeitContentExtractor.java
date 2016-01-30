package net.deepthought.data.contentextractor;

import net.deepthought.Application;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.ReferenceSubDivision;
import net.deepthought.util.DeepThoughtError;
import net.deepthought.util.localization.Localization;
import net.deepthought.util.StringUtils;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by ganymed on 20/06/15.
 */
public class ZeitContentExtractor extends OnlineNewspaperContentExtractorBase {

  private final static Logger log = LoggerFactory.getLogger(ZeitContentExtractor.class);


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
    return "Zeit";
  }

  @Override
  public String getSiteBaseUrl() {
    return "Zeit.de";
  }

  @Override
  public boolean canCreateEntryFromUrl(String url) {
    return url.startsWith("http://www.zeit.de/") || url.startsWith("https://www.zeit.de/");
  }

  protected EntryCreationResult parseHtmlToEntry(String articleUrl, Document document) {
    try {
      Element articleElement = document.body().select("article").first();
      if(articleElement == null)
        return new EntryCreationResult(articleUrl, new DeepThoughtError(Localization.getLocalizedString("could.not.create.entry.from.article.html")));

      String multiPageArticleArticleOnOnePageUrl = getArticleOnOnePageUrlForMultiPageArticles(articleElement);
      if(multiPageArticleArticleOnOnePageUrl != null)
        return createEntryFromArticle(multiPageArticleArticleOnOnePageUrl);

      Entry articleEntry = createEntry(articleElement);
      EntryCreationResult creationResult = new EntryCreationResult(articleUrl, articleEntry);

      createReference(creationResult, articleUrl, articleElement);

      addTags(document.body(), creationResult);
      addNewspaperCategory(creationResult, true);

      return creationResult;
    } catch(Exception ex) {
      return new EntryCreationResult(articleUrl, new DeepThoughtError(Localization.getLocalizedString("could.not.create.entry.from.article.html"), ex));
    }
  }

  protected String getArticleOnOnePageUrlForMultiPageArticles(Element articleBodyElement) {
    Element articleTocOnesieElement = articleBodyElement.select(".article-toc__onesie").first();
    if(articleTocOnesieElement != null) {
      if("a".equals(articleTocOnesieElement.nodeName()))
        return articleTocOnesieElement.attr("href");
    }

    return null;
  }

  protected Entry createEntry(Element articleBodyElement) {
    String abstractString = articleBodyElement.select("div.summary").text();

    String content = "";
    for(Element articleElement : articleBodyElement.select("p.article__item")) { // articleBodyElement.select("p .paragraph .article__item")
      content += articleElement.outerHtml();
    }

    return new Entry(content, abstractString);
  }

  protected ReferenceSubDivision createReference(EntryCreationResult creationResult, String articleUrl, Element articleBodyElement) {
    String title = articleBodyElement.select(".article-heading__title").text();

    String subTitle = articleBodyElement.select(".article-heading__kicker").text();

    String publishingDateString = "";
    Element articleDateTimeElement = articleBodyElement.select(".metadata__date").first();
    if(articleDateTimeElement != null) {
      publishingDateString = parseDate(articleDateTimeElement);
    }

    ReferenceSubDivision articleReference = new ReferenceSubDivision(title, subTitle);
    articleReference.setOnlineAddressAndLastAccessToCurrentDateTime(articleUrl);

    setArticleReference(creationResult, articleReference, publishingDateString);

    return articleReference;
  }


  protected DateFormat zeitDateTimeFormat = new SimpleDateFormat("dd. MMMMM yyyy HH:mm", Locale.GERMAN);

  protected String parseDate(Element articleDateTimeElement) {
    String publishingDateString = "";

    if(articleDateTimeElement.hasAttr("datetime")) {
      publishingDateString = parseIsoDateTimeString(articleDateTimeElement.attr("datetime"), publishingDateString);
    }

    if(StringUtils.isNullOrEmpty(publishingDateString)) {
      Date publishingDate = parseZeitDateTimeFormat(articleDateTimeElement.text());
      if (publishingDate != null)
        publishingDateString = formatDateToDeepThoughtDateString(publishingDate);
    }

    return publishingDateString;
  }

  protected Date parseZeitDateTimeFormat(String articleDateTime) {
    articleDateTime = articleDateTime.replace("&nbsp;", "");
    articleDateTime = articleDateTime.replace(" Uhr", "").trim();

    try {
      Date parsedDate = zeitDateTimeFormat.parse(articleDateTime);
      return parsedDate;
    } catch(Exception ex) { log.error("Could not parse Zeit DateTime Format " + articleDateTime, ex); }

    return null;
  }

  protected void addTags(Element bodyElement, EntryCreationResult creationResult) {
    addNewspaperTag(creationResult);

    Elements tagsElements = bodyElement.getElementsByClass("tags");
    for(Element tagsElement : tagsElements) {
      if("li".equals(tagsElement.nodeName())) {
        addArticleTags(tagsElement, creationResult);
        break;
      }
    }
  }

  protected void addArticleTags(Element tagsElement, EntryCreationResult creationResult) {
    for(Element child : tagsElement.children()) {
      if("a".equals(child.nodeName())) {
        creationResult.addTag(Application.getDeepThought().findOrCreateTagForName(child.ownText()));
      }
    }
  }
}

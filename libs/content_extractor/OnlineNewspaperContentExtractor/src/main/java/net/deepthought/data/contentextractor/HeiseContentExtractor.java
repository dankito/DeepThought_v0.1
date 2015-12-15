package net.deepthought.data.contentextractor;

import net.deepthought.Application;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.ReferenceSubDivision;
import net.deepthought.util.DeepThoughtError;
import net.deepthought.util.Localization;
import net.deepthought.util.StringUtils;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
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
public class HeiseContentExtractor extends OnlineNewspaperContentExtractorBase {

  private final static Logger log = LoggerFactory.getLogger(HeiseContentExtractor.class);


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
    return "Heise";
  }

  @Override
  public String getSiteBaseUrl() {
    return "heise.de";
  }

  @Override
  public boolean canCreateEntryFromUrl(String url) {
    return url.toLowerCase().contains("www.heise.de/");
  }

  protected EntryCreationResult parseHtmlToEntry(String articleUrl, Document document) {
    try {
      Element articleElement = document.body().select("article").first();
      if(articleElement == null)
        return new EntryCreationResult(articleUrl, new DeepThoughtError(Localization.getLocalizedString("could.not.create.entry.from.article.html")));

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

  protected Entry createEntry(Element articleElement) {
    String abstractString = articleElement.select(".meldung_anrisstext strong").html();

    String content = "";
    Element meldungWrapperElement = articleElement.select(".meldung_wrapper").first();
    if(meldungWrapperElement == null) {
      meldungWrapperElement = articleElement;
    }

    adjustLinkUrls(meldungWrapperElement);
    adjustSourceElements(meldungWrapperElement);

    // if it doesn't have any class (= normal article paragraph) or has class subheading (= Sub Heading)
    for(Element paragraphElement : meldungWrapperElement.select("p:not([class]), h3.subheading, .yt-video-container")) {
      if(StringUtils.isNotNullOrEmpty(paragraphElement.text()) || "div".equals(paragraphElement.tagName())) {
        content += paragraphElement.outerHtml();
      }
    }

    return new Entry(content, abstractString);
  }

  protected void adjustLinkUrls(Element articleElement) {
    for(Element elementWithSrcAttribute : articleElement.select("[src]")) {
      String src = elementWithSrcAttribute.attr("src");
      src = makeLinkAbsolute(src, "http://www.heise.de");
      elementWithSrcAttribute.attr("src", src);
    }

    for(Element elementWithHrefAttribute : articleElement.select("[href]")) {
      String href = elementWithHrefAttribute.attr("href");
      href = makeLinkAbsolute(href, "http://www.heise.de");
      elementWithHrefAttribute.attr("href", href);
    }

    for(Element elementWithHrefAttribute : articleElement.select("[data-zoom-src]")) {
      String href = elementWithHrefAttribute.attr("data-zoom-src");
      href = makeLinkAbsolute(href, "http://www.heise.de");
      elementWithHrefAttribute.attr("data-zoom-src", href);
    }
  }

  protected void adjustSourceElements(Element articleElement) {
    for(Element sourceElement : articleElement.select("span.source")) {
      sourceElement.parent().appendChild(new Element(Tag.valueOf("br"), articleElement.baseUri()));
    }
  }

  private String makeLinkAbsolute(String link, String baseUrl) {
    if(link.startsWith("//")) {
      return "http:" + link;
    }
    else if(link.startsWith("/")) {
      return baseUrl + link;
    }

    return link;
  }

  protected ReferenceSubDivision createReference(EntryCreationResult creationResult, String articleUrl, Element articleElement) {
    String title = articleElement.select(".news_headline").text();
    String subTitle = "";

    if(title.contains(": ")) {
      int indexOfColon = title.indexOf(": ");
      subTitle = title.substring(0, indexOfColon);
      title = title.substring(indexOfColon + ": ".length());
    }

    String publishingDateString = "";
    Element articleDateTimeElement = articleElement.select("time").first();
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

package net.deepthought.data.contentextractor;

import net.deepthought.data.model.Entry;
import net.deepthought.data.model.ReferenceSubDivision;
import net.deepthought.util.DeepThoughtError;
import net.deepthought.util.Localization;
import net.deepthought.util.StringUtils;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SpiegelContentExtractor extends OnlineNewspaperContentExtractorBase {

  private final static Logger log = LoggerFactory.getLogger(SpiegelContentExtractor.class);


  @Override
  public String getNewspaperName() {
    return "Spiegel";
  }

  @Override
  public String getSiteBaseUrl() {
    return "Spiegel.de";
  }

  @Override
  public boolean canCreateEntryFromUrl(String url) {
    return url.startsWith("http://www.spiegel.de/") || url.startsWith("https://www.spiegel.de/");
  }

  protected EntryCreationResult parseHtmlToEntry(String articleUrl, Document document) {
    try {
      Element contentElement = document.body().getElementById("content-main");

      Elements articleSectionElements = document.body().getElementsByClass("article-section");
      Elements articleIntroElements = contentElement.getElementsByClass("article-intro");

      Entry articleEntry = createEntry(articleSectionElements, articleIntroElements);
      EntryCreationResult creationResult = new EntryCreationResult(articleUrl, articleEntry);

      createReference(creationResult, articleUrl, contentElement);

      addNewspaperTag(creationResult);
      addNewspaperCategory(creationResult, true);

      return creationResult;
    } catch(Exception ex) {
      return new EntryCreationResult(articleUrl, new DeepThoughtError(Localization.getLocalizedString("could.not.create.entry.from.article.html"), ex));
    }
  }

  protected Entry createEntry(Elements articleSectionElements, Elements articleIntroElements) {
    String content = extractContentFromArticleSection(articleSectionElements);
    String abstractString = extractAbstractFromArticleIntro(articleIntroElements);

    return new Entry(content, abstractString);
  }

  protected String extractContentFromArticleSection(Elements articleSectionElements) {
    for(Element articleSectionElement : articleSectionElements) {
      if("div".equals(articleSectionElement.nodeName())) {
        return extractContentFromArticleSection(articleSectionElement);
      }
    }

    return "";
  }

  protected String extractContentFromArticleSection(Element articleSectionElement) {
    String contentHtml = "";

    for(Node childNode : articleSectionElement.childNodes()) {
      if(childNode instanceof TextNode) {
        TextNode textNode = (TextNode)childNode;
        if(StringUtils.isNotNullOrEmpty(textNode.text().trim()))
          contentHtml += appendHtml(textNode.text().trim(), childNode);
      }
      else if(childNode instanceof Element) {
        Element childElement = (Element)childNode;
        if ("div".equals(childElement.nodeName()) == false && StringUtils.isNotNullOrEmpty(childElement.text().trim()) &&
            childElement.html().contains("<span class=\"spTextSmaller\">") == false) { // filters 'Wenig Zeit? Am Textende gibt's eine Zusammenfassung.'
          if("ul".equals(childElement.nodeName()))
            contentHtml += "<p>" + childElement.outerHtml() + "</p>";
          else if("a".equals(childElement.nodeName()))
            contentHtml += " " + childElement.outerHtml();
          else
//          if(childElement.outerHtml().contains("href=\"http://boersen.manager-magazin.de/mm/kurse_einzelkurs_suche.") == false) // TODO: filter Boersenkurse
          // TODO: filter 'Diese Meldung stammt aus dem SPIEGEL'
            contentHtml += appendHtml(childElement.html(), childNode);
        }
      }
    }

    contentHtml = resolveTextLinks(contentHtml);

    return contentHtml;
  }

  protected String appendHtml(String html, Node currentNode) {
    String resultingHtml = "";

    if("a".equals(currentNode.previousSibling().nodeName()) == false)
      resultingHtml += "<p>";
    else if(StringUtils.isNotNullOrEmpty(html) && Character.isAlphabetic(html.charAt(0))) // previous node has been an url. if now a text beginning with an alphanumeric character, add a white space before
      resultingHtml += " ";

    resultingHtml += html;

    if("a".equals(currentNode.nextSibling().nodeName()) == false)
      resultingHtml += "</p>";

    return resultingHtml;
  }

  protected String resolveTextLinks(String contentHtml) {
    return contentHtml.replace("<a href=\"/", "<a href=\"http:www.spiegel.de/");
  }

  protected String extractAbstractFromArticleIntro(Elements articleIntroElements) {
    for(Element articleIntroElement : articleIntroElements) {
      if("p".equals(articleIntroElement.nodeName())) {
        return extractAbstractFromArticleIntro(articleIntroElement);
      }
    }

    return null;
  }

  protected String extractAbstractFromArticleIntro(Element articleIntroElement) {
    String abstractString = "";
    for(Element child : articleIntroElement.children()) {
      if("strong".equals(child.nodeName()))
        abstractString += child.html();
      else
        abstractString += child.html();
    }

    return abstractString;
  }

  protected ReferenceSubDivision createReference(EntryCreationResult creationResult, String articleUrl, Element contentElement) {
    String title = extractTitle(contentElement);
    String subTitle = extractSubTitle(contentElement);

    String publishingDateString = extractPublishingDate(contentElement);

    ReferenceSubDivision articleReference = new ReferenceSubDivision(title, subTitle);
    articleReference.setOnlineAddress(articleUrl);

    setArticleReference(creationResult,articleReference, publishingDateString);

    return articleReference;
  }

  protected String extractTitle(Element contentElement) {
    String title = "";
    Elements headerElements = contentElement.getElementsByClass("headline");
    for(Element headerElement : headerElements) {
      if("span".equals(headerElement.nodeName())) {
        title = headerElement.text();
        break;
      }
    }
    return title;
  }

  protected String extractSubTitle(Element contentElement) {
    String subTitle = "";
    Elements headerIntroElements = contentElement.getElementsByClass("headline-intro");
    for(Element headerIntroElement : headerIntroElements) {
      if("span".equals(headerIntroElement.nodeName())) {
        subTitle = headerIntroElement.text();
        if(subTitle.endsWith(":"))
          subTitle = subTitle.substring(0, subTitle.length() - 1);
        break;
      }
    }
    return subTitle;
  }

  protected String extractPublishingDate(Element contentElement) {
    Date publishingDate = null;
    Elements timeFormatElements = contentElement.getElementsByClass("timeformat");
    for(Element timeFormatElement : timeFormatElements) {
      if("time".equals(timeFormatElement.nodeName()) && timeFormatElement.hasAttr("datetime")) {
        String spiegelTimeFormat = timeFormatElement.attr("datetime");
        publishingDate = parseSpiegelTimeFormat(spiegelTimeFormat);
        break;
      }
    }

    String publishingDateString = "";
    if(publishingDate != null)
      publishingDateString = formatDateToDeepThoughtDateString(publishingDate);
    return publishingDateString;
  }

  protected DateFormat spiegelTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

  protected Date parseSpiegelTimeFormat(String dateTime) {
    try {
      Date parsedDate = spiegelTimeFormat.parse(dateTime);
      return parsedDate;
    } catch(Exception ex) { log.error("Could not parse Spiegel Date Format " + dateTime, ex); }

    return null;
  }

}

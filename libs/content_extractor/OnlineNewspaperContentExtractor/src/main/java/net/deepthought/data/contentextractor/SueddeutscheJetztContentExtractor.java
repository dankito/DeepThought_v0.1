package net.deepthought.data.contentextractor;

import net.deepthought.Application;
import net.deepthought.data.model.Category;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.ReferenceSubDivision;
import net.deepthought.util.DeepThoughtError;
import net.deepthought.util.localization.Localization;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SueddeutscheJetztContentExtractor extends SueddeutscheContentExtractorBase {

  private final static Logger log = LoggerFactory.getLogger(SueddeutscheJetztContentExtractor.class);


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
    return "SZ Jetzt";
  }

  @Override
  public String getSiteBaseUrl() {
    return "Jetzt.de";
  }

  @Override
  public boolean canCreateEntryFromUrl(String url) {
    return url.contains("www.jetzt.de/") ||url.contains("//jetzt.sueddeutsche.de/");
  }


  protected EntryCreationResult parseHtmlToEntry(String articleUrl, Document document) {
    try {
      if(isOldJetztVersion(document)) {
        return parseHtmlToEntryForOldVersion(articleUrl, document);
      }

      return parseHtmlToEntryNewVersion(articleUrl, document);
    } catch(Exception ex) {
      return new EntryCreationResult(articleUrl, new DeepThoughtError(Localization.getLocalizedString("could.not.create.entry.from.article.html"), ex));
    }
  }


  /*        Parsing an SZ Jetzt article of new Homepage Style, introduced beginning 2016      */

  protected EntryCreationResult parseHtmlToEntryNewVersion(String articleUrl, Document document) {
    Element articleElement = getFirstElementWithNodeName(document.body(), "article");
    if(articleElement == null) {
      log.warn("Could not find <article> Element");
      return new EntryCreationResult(articleUrl, new DeepThoughtError(Localization.getLocalizedString("could.not.find.element.with.node.name", "article")));
    }

    Elements articleContentElements = articleElement.getElementsByClass("article__content");
    if(articleContentElements.size() == 0) {
      log.warn("Could not find Element with class 'article__content'");
      return new EntryCreationResult(articleUrl, new DeepThoughtError(Localization.getLocalizedString("could.not.find.element.of.class.to.extract.article", "article__content")));
    }

    String content = parseContent(articleContentElements.get(0));
    String abstractString = extractAbstract(articleElement);

    Entry articleEntry = new Entry(content, abstractString);
    EntryCreationResult creationResult = new EntryCreationResult(articleUrl, articleEntry);

    createReference(creationResult, articleElement, articleUrl);

    findOrCreateTagAndAddToCreationResult(creationResult);
    addNewspaperCategory(creationResult);

    return creationResult;
  }

  protected String extractAbstract(Element articleElement) {
    String abstractString = null;
    Element teaserElement = getElementByClassAndNodeName(articleElement, "div", "article__header-teaser");
    if(teaserElement != null)
      abstractString = teaserElement.html();
    else
      log.warn("Could not find teaser Element");
    return abstractString;
  }

  protected String parseContent(Element articleContentElement) {
    String content = "";

    Elements itemsContainers = articleContentElement.select(".apos-item[data-type=\"richText\"]");
    for(Element itemsContainer : itemsContainers) {
      Elements paragraphs = itemsContainer.getElementsByTag("p");
      for (Element paragraph : paragraphs) {
        content += parseParagraph(paragraph);
      }
    }

    return content;
  }

  protected String parseParagraph(Element paragraph) {
    if(isEmptyParagraph(paragraph)) {
      return "";
    }

    return paragraph.outerHtml();
  }

  protected boolean isEmptyParagraph(Element paragraph) {
    if(paragraph == null || paragraph.text() == null) {
      return true;
    }

    String text = paragraph.text().replace((char)160, ' '); // replace non breakable spaces
    return text.trim().length() == 0;
  }


  protected ReferenceSubDivision createReference(EntryCreationResult creationResult, Element articleElement, String articleUrl) {
    String title = extractTitle(articleElement);
    String publishingDate = extractPublishingDate(articleElement);

    if(title != null && publishingDate != null) {
      ReferenceSubDivision articleReference = new ReferenceSubDivision(title);
      articleReference.setOnlineAddressAndLastAccessToCurrentDateTime(articleUrl);
      setArticleReference(creationResult, articleReference, publishingDate);

      return articleReference;
    }

    return null;
  }

  protected String extractTitle(Element articleElement) {
    String title = null;

    Elements header2Elements = articleElement.getElementsByClass("article__header-title");
    if(header2Elements.size() > 0)
      title = header2Elements.get(0).text();
    else
      log.warn("Could not find h1 child Element of article Element with class 'article__header-title'");

    return title;
  }

  protected String extractPublishingDate(Element articleElement) {
    Elements headerDateElements = articleElement.getElementsByClass("article__header-date");
    if(headerDateElements.size() == 0) {
      log.warn("Could not find Element with class 'article__header-date', therefore cannot extract Article's publishing date");
      return null;
    }

    Element headerDateElement = headerDateElements.get(0);
    return tryToParseSueddeutscheJetztPublishingDate(headerDateElement.text());
  }


  protected static final DateFormat sueddeutscheJetztDateTimeFormat = new SimpleDateFormat("dd.MM.yyyy");

  protected String tryToParseSueddeutscheJetztPublishingDate(String publishingDate) {
    publishingDate = publishingDate.trim();

    try {
      Date parsedDate = sueddeutscheJetztDateTimeFormat.parse(publishingDate);
      return formatDateToDeepThoughtDateString(parsedDate);
    } catch(Exception ex) { log.error("Could not parse Sueddeutsche Jetzt Date " + publishingDate, ex); }
    return "";
  }



  /*        Parsing an SZ Jetzt article of old Homepage Style as it has been before beginning 2016      */

  protected boolean isOldJetztVersion(Document document) {
    return getElementByClassAndNodeName(document.body(), "div", "text") != null;
  }

  protected EntryCreationResult parseHtmlToEntryForOldVersion(String articleUrl, Document document) {
    try {
      Element textElement = getElementByClassAndNodeName(document.body(), "div", "text");
      if(textElement == null) {
        log.warn("Could not find text Element");
        return new EntryCreationResult(articleUrl, new DeepThoughtError(Localization.getLocalizedString("could.not.find.element.of.class.to.extract.article", "text")));
      }

      Element fliesstextElement = getElementByClassAndNodeName(textElement, "div", "fliesstext");
      if(fliesstextElement == null) {
        log.warn("Could not find fliesstext Element");
        return new EntryCreationResult(articleUrl, new DeepThoughtError(Localization.getLocalizedString("could.not.find.element.of.class.to.extract.article", "fliesstext")));
      }

      String content = parseContentForVersion(fliesstextElement);
      String abstractString = extractAbstractOldVersion(textElement);

      Entry articleEntry = new Entry(content, abstractString);
      EntryCreationResult creationResult = new EntryCreationResult(articleUrl, articleEntry);

      createReferenceOldVersion(creationResult, textElement, articleUrl);

      findOrCreateTagAndAddToCreationResult(creationResult);
      addNewspaperCategory(creationResult);

      return creationResult;
    } catch(Exception ex) {
      return new EntryCreationResult(articleUrl, new DeepThoughtError(Localization.getLocalizedString("could.not.create.entry.from.article.html"), ex));
    }
  }

  protected String extractAbstractOldVersion(Element textElement) {
    String abstractString = null;
    Element introElement = getElementByClassAndNodeName(textElement, "p", "intro");
    if(introElement != null)
      abstractString = introElement.html();
    else
      log.warn("Could not find intro Element");
    return abstractString;
  }

  protected String parseContentForVersion(Element fliesstextElement) {
    String content = "";

    for(Node child : fliesstextElement.childNodes()) {
        if("em".equals(child.nodeName()) && child.outerHtml().contains("&gt;&gt; ")) { // teaser what's on next page
          String childText = ((Element)child).text();
          int nextPageTeaserStart = childText.indexOf(">>");
          int nextPageTeaserEnd = childText.indexOf(">>", nextPageTeaserStart + 1);
          if(nextPageTeaserStart > -1 && nextPageTeaserEnd > 0)
            content += "<em>" + childText.substring(0, nextPageTeaserStart).trim() + childText.substring(nextPageTeaserEnd + 2) + "</em>";
          else
            content += child.outerHtml();
        }
        else if(child.outerHtml().contains("<img src=\"/"))
          content += child.outerHtml().replace("<img src=\"/", "<img src=\"http://jetzt.sueddeutsche.de/");
        else if("h2".equals(child.nodeName()) && content.endsWith("<br>")) {
          content = content.substring(0, content.length() - "<br>".length());
          content += child.outerHtml();
        }
        else if("br".equals(child.nodeName()) && (content.endsWith("</ul>") || content.endsWith("</h2>"))) {
          // don't add <br> element after an unordered list or an header
        }
        else {
          if (content.length() == 0 && ("br".equals(child.nodeName()) || child.outerHtml().trim().length() == 0)) // remove leading <br> elements and empty text nodes
            continue;

          content += child.outerHtml();
        }
//      }
    }

    content = content.trim();

    if(content.endsWith("><em><br></em></span>"))
      content = content.substring(0, content.length() - "<span style=\"color:#888888;\"><em><br></em></span>".length());

    content += parseContentOfNextPageOldVersion(fliesstextElement);

    while(content.endsWith("<br>")) // remove trailing <br> elements
      content = content.substring(0, content.length() - 4).trim();

    return content;
  }

  protected String parseContentOfNextPageOldVersion(Element artikelElement) {
    Element pagingElement = getElementByClassAndNodeName(artikelElement.parent(), "div", "paging");
    if(pagingElement != null) {
      int currentPageNumber = 0;

      for(Element child : pagingElement.children()) {
        if("a".equals(child.nodeName()) && child.hasClass("page")) {
          if(child.hasClass("current")) {
            try { currentPageNumber = Integer.parseInt(child.text()); } catch (Exception ex) { log.error("Could not parse current page number " + child.text() + " to an integer"); }
          }
          else if(currentPageNumber > 0) {
            try {
              int nextPageNumber = Integer.parseInt(child.text());
              if(nextPageNumber == currentPageNumber + 1) {
                return parseContentOfNextPageOldVersion(child.attr("href"));
              }
            } catch(Exception ex) { log.error("Could not parse next page number " + child.text() + " to an integer"); break; }
          }
        }
      }
    }

    return "";
  }

  protected String parseContentOfNextPageOldVersion(String nextPageUrl) {
    if(nextPageUrl.startsWith("http://jetzt.sueddeutsche.de") == false)
      nextPageUrl = "http://jetzt.sueddeutsche.de" + nextPageUrl;

    try {
      Document nextPageDocument = retrieveOnlineDocument(nextPageUrl);
      Element fliesstextElement = getElementByClassAndNodeName(nextPageDocument.body(), "div", "fliesstext");
      if(fliesstextElement != null)
        return parseContentForVersion(fliesstextElement);
    } catch(Exception ex) { log.error("Could not retrieve HTML code for next Page Url " + nextPageUrl, ex); }
    return "";
  }

  protected ReferenceSubDivision createReferenceOldVersion(EntryCreationResult creationResult, Element textElement, String articleUrl) {
    String title = extractTitleOldVersion(textElement);
    String publishingDate = extractPublishingDateOldVersion(textElement);

    if(title != null && publishingDate != null) {
      ReferenceSubDivision articleReference = new ReferenceSubDivision(title);
      articleReference.setOnlineAddressAndLastAccessToCurrentDateTime(articleUrl);
      setArticleReference(creationResult, articleReference, publishingDate);

      return articleReference;
    }

    return null;
  }

  protected String extractTitleOldVersion(Element textElement) {
    String title = null;

    Elements header1Elements = textElement.getElementsByTag("h1");
    if(header1Elements.size() > 0)
      title = header1Elements.get(0).text();
    else
      log.warn("Could not find h1 child Element of text Element");

    return title;
  }

  protected String extractPublishingDateOldVersion(Element textElement) {
    Element textDetailsElement = textElement.getElementById("text_details");
    if(textDetailsElement == null) {
      log.warn("Could not find Element with id text_details");
      return null;
    }

    for(Element detailsChild : textDetailsElement.children()) {
      if("p".equals(detailsChild.nodeName()) && detailsChild.hasClass("time")) {
        return tryToParseSueddeutscheJetztPublishingDateOldVersion(detailsChild.text());
      }
    }

    return null;
  }

  protected static final DateFormat sueddeutscheJetztDateTimeFormatOldVersion = new SimpleDateFormat("dd.MM.yyyy - HH:mm", Locale.GERMAN);

  protected String tryToParseSueddeutscheJetztPublishingDateOldVersion(String publishingDate) {
    publishingDate = publishingDate.replace("Uhr", "");
    publishingDate = publishingDate.trim();

    try {
      Date parsedDate = sueddeutscheJetztDateTimeFormatOldVersion.parse(publishingDate);
      return formatDateToDeepThoughtDateString(parsedDate);
    } catch(Exception ex) { log.error("Could not parse Sueddeutsche Jetzt Date " + publishingDate, ex); }
    return "";
  }


  @Override
  protected void findOrCreateTagAndAddToCreationResult(EntryCreationResult creationResult) {
    super.findOrCreateTagAndAddToCreationResult(creationResult);

    findOrCreateTagAndAddToCreationResult(creationResult, "SZ");
  }

  protected void addNewspaperCategory(EntryCreationResult creationResult) {
    // TODO: here sub categories getting added directly to their (may already saved) parent categories and so also get stored in database whether user likes to save this Article
    // or not, but i can live with that right now
    // Currently there's no other way to solve it as if parent category doesn't get set, on save it gets added to TopLevelCategory -> it will be added a lot of times
    Category periodicalsCategory = Application.getEntitiesSearcherAndCreator().findOrCreateTopLevelCategoryForName(Localization.getLocalizedString("periodicals"));
    if(periodicalsCategory.isPersisted() == false && Application.getDeepThought() != null) {
      Application.getDeepThought().addCategory(periodicalsCategory);
    }

    Category sueddeutscheCategory = Application.getEntitiesSearcherAndCreator().findOrCreateSubCategoryForName(periodicalsCategory, "SZ");
    if(sueddeutscheCategory.isPersisted() == false && Application.getDeepThought() != null) {
      Application.getDeepThought().addCategory(sueddeutscheCategory);
      periodicalsCategory.addSubCategory(sueddeutscheCategory);
    }

    Category szJetztCategory = Application.getEntitiesSearcherAndCreator().findOrCreateSubCategoryForName(sueddeutscheCategory, "jetzt");
    if(szJetztCategory.isPersisted() == false && Application.getDeepThought() != null) {
      Application.getDeepThought().addCategory(szJetztCategory);
      sueddeutscheCategory.addSubCategory(szJetztCategory);
    }

    creationResult.addCategory(szJetztCategory);
  }
}

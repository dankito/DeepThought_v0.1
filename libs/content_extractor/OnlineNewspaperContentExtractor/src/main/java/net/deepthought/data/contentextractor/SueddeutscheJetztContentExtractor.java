package net.deepthought.data.contentextractor;

import net.deepthought.Application;
import net.deepthought.data.model.Category;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.Reference;
import net.deepthought.data.model.ReferenceSubDivision;
import net.deepthought.util.DeepThoughtError;
import net.deepthought.util.Localization;

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
  public String getNewspaperName() {
    return "SZ Jetzt";
  }

  @Override
  public String getSiteBaseUrl() {
    return "Sueddeutsche.de";
  }

  @Override
  public boolean canCreateEntryFromUrl(String url) {
    return url.startsWith("http://jetzt.sueddeutsche.de/") || url.startsWith("https://jetzt.sueddeutsche.de/");
  }


  protected EntryCreationResult parseHtmlToEntry(String articleUrl, Document document) {
    try {
      Element textElement = getElementByClassAndNodeName(document.body(), "div", "text");
      if(textElement == null) {
        log.warn("Could not find text Element");
        return new EntryCreationResult(articleUrl, new DeepThoughtError(Localization.getLocalizedStringForResourceKey("could.not.find.element.of.class.to.extract.article", "text")));
      }

      ReferenceSubDivision articleReference = createReference(textElement, articleUrl);

      String abstractString = null;
      Element introElement = getElementByClassAndNodeName(textElement, "p", "intro");
      if(introElement != null)
        abstractString = introElement.html();
      else
        log.warn("Could not find intro Element");

      Element fliesstextElement = getElementByClassAndNodeName(textElement, "div", "fliesstext");
      if(fliesstextElement == null) {
        log.warn("Could not find fliesstext Element");
        return new EntryCreationResult(articleUrl, new DeepThoughtError(Localization.getLocalizedStringForResourceKey("could.not.find.element.of.class.to.extract.article", "fliesstext")));
      }

      String content = parseContent(fliesstextElement);

      Entry articleEntry = new Entry(content, abstractString);
      articleEntry.setReferenceSubDivision(articleReference);

      addNewspaperTag(articleEntry);
      addNewspaperCategory(articleEntry);

      return new EntryCreationResult(document.baseUri(), articleEntry);
    } catch(Exception ex) {
      return new EntryCreationResult(document.baseUri(), new DeepThoughtError(Localization.getLocalizedStringForResourceKey("could.not.create.entry.from.article.html"), ex));
    }
  }

  protected String parseContent(Element fliesstextElement) {
    String content = "";

    for(Node child : fliesstextElement.childNodes()) {
      // TODO: also extract instructional image with class image ?
//      if("div".equals(child.nodeName()) == false && child instanceof Comment == false) {
//        if(child instanceof Element) { // clear adbox Elements of children
//          Element adboxElement = getElementByClassAndNodeName((Element)child, "div", "adbox");
//          if(adboxElement != null)
//            adboxElement.remove();
//          for(Element childChild : ((Element) child).children()) {
//            if("br".equals(childChild.nodeName())) // remove leading <br > elements
//              childChild.remove();
//            else break;
//          }
//        }

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

    content += parseContentOfNextPage(fliesstextElement);

    while(content.endsWith("<br>")) // remove trailing <br> elements
      content = content.substring(0, content.length() - 4).trim();

    return content;
  }

  protected String parseContentOfNextPage(Element artikelElement) {
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
                return parseContentOfNextPage(child.attr("href"));
              }
            } catch(Exception ex) { log.error("Could not parse next page number " + child.text() + " to an integer"); break; }
          }
        }
      }
    }

    return "";
  }

  protected String parseContentOfNextPage(String nextPageUrl) {
    if(nextPageUrl.startsWith("http://jetzt.sueddeutsche.de") == false)
      nextPageUrl = "http://jetzt.sueddeutsche.de" + nextPageUrl;

    try {
      Document nextPageDocument = retrieveOnlineDocument(nextPageUrl);
      Element fliesstextElement = getElementByClassAndNodeName(nextPageDocument.body(), "div", "fliesstext");
      if(fliesstextElement != null)
        return parseContent(fliesstextElement);
    } catch(Exception ex) { log.error("Could not retrieve HTML code for next Page Url " + nextPageUrl, ex); }
    return "";
  }

  protected ReferenceSubDivision createReference(Element textElement, String articleUrl) {
    String publishingDate = null, title = null;

    Elements header1Elements = textElement.getElementsByTag("h1");
    if(header1Elements.size() > 0)
      title = header1Elements.get(0).text();
    else
      log.warn("Could not find h1 child Element of text Element");

    Element textDetailsElement = textElement.getElementById("text_details");
    if(textDetailsElement == null) {
      log.warn("Could not find Element with id text_details");
      return null;
    }

    for(Element detailsChild : textDetailsElement.children()) {
      if("p".equals(detailsChild.nodeName()) && detailsChild.hasClass("time")) {
        publishingDate = tryToParseSueddeutscheJetztPublishingDate(detailsChild.text());
      }
    }

    if(title != null && publishingDate != null) {
      Reference dateReference = findOrCreateReferenceForThatDate(publishingDate);

      ReferenceSubDivision articleReference = new ReferenceSubDivision(title);
      articleReference.setOnlineAddress(articleUrl);
      dateReference.addSubDivision(articleReference);

      return articleReference;
    }

    return null;
  }

  protected DateFormat sueddeutscheJetztDateTimeFormat = new SimpleDateFormat("dd.MM.yyyy - HH:mm", Locale.GERMAN);

  protected String tryToParseSueddeutscheJetztPublishingDate(String publishingDate) {
    publishingDate = publishingDate.replace("Uhr", "");
    publishingDate = publishingDate.trim();

    try {
      Date parsedDate = sueddeutscheJetztDateTimeFormat.parse(publishingDate);
      return formatDateToDeepThoughtDateString(parsedDate);
    } catch(Exception ex) { log.error("Could not parse Sueddeutsche Magazin Date " + publishingDate, ex); }
    return "";
  }

  @Override
  protected void addNewspaperTag(Entry articleEntry) {
    super.addNewspaperTag(articleEntry);

    addNewspaperTag(articleEntry, "SZ");
  }

  protected void addNewspaperCategory(Entry articleEntry) {
    Category periodicalsCategory = Application.getDeepThought().findOrCreateTopLevelCategoryForName(Localization.getLocalizedStringForResourceKey("periodicals"));
    Category sueddeutscheCategory = Application.getDeepThought().findOrCreateSubCategoryForName(periodicalsCategory, "SZ");
    Category szMagazinCategory = Application.getDeepThought().findOrCreateSubCategoryForName(sueddeutscheCategory, "jetzt");

    szMagazinCategory.addEntry(articleEntry);
  }
}

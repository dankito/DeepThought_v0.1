package net.deepthought.data.contentextractor;

import net.deepthought.data.model.Entry;
import net.deepthought.data.model.Reference;
import net.deepthought.data.model.ReferenceSubDivision;
import net.deepthought.util.DeepThoughtError;
import net.deepthought.util.Localization;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by ganymed on 16/07/15.
 */
public class PostillionContentExtractor extends OnlineNewspaperContentExtractorBase {

  private final static Logger log = LoggerFactory.getLogger(PostillionContentExtractor.class);


  @Override
  public String getNewspaperName() {
    return "Postillion";
  }

  @Override
  public String getSiteBaseUrl() {
    return "der-postillon.com";
  }

  @Override
  public boolean canCreateEntryFromUrl(String url) {
    return url.startsWith("http://www.der-postillon.com/") || url.startsWith("https://www.der-postillon.com/") ||
        url.startsWith("http://www.der-postillon.de/") || url.startsWith("https://www.der-postillon.de/");
  }

  @Override
  protected EntryCreationResult parseHtmlToEntry(String articleUrl, Document document) {
    Element postElement = getElementByClassAndNodeName(document.body(), "div", "post");
    if(postElement != null)
      return extractArticleFromPostDivElement(articleUrl, postElement);
    else
      log.error("Could not find div element with class post");

    return new EntryCreationResult(articleUrl, new DeepThoughtError("Could not find Post content. Maybe Page structure has changed. Please contact programmers.")); // TODO: translate error message
  }

  protected EntryCreationResult extractArticleFromPostDivElement(String articleUrl, Element postDivElement) {
    String title = null;
    String content = "";

    Element entryContentElement = getElementByClassAndNodeName(postDivElement, "div", "entry-content");
    if(entryContentElement != null) {
      for (Node child : entryContentElement.childNodes()) {
        // TODO: if also introducing image should be displayed, extract div element with class="separator" as well
//        if(child instanceof TextNode)
//          content += ((TextNode)child).text();
        if("div".equals(child.nodeName()) == false && "span".equals(child.nodeName()) == false)
          content += child.outerHtml();
      }
    }
    else {
      log.error("Could not find Div element with entry-content class");
      return new EntryCreationResult(articleUrl, new DeepThoughtError("Could not find entry content.. Maybe Page structure has changed. Please contact programmers.")); // TODO: translate error message
    }

    Entry entry = new Entry(content);
    ReferenceSubDivision articleReference = extractReferenceSubDivisionFromPostElement(articleUrl, postDivElement);
    entry.setReferenceSubDivision(articleReference);
    if(articleReference != null)
      entry.setAbstract(articleReference.getTitle());

    addNewspaperTag(entry);
    addNewspaperCategory(entry, false);

    return new EntryCreationResult(articleUrl, entry);
  }

  protected ReferenceSubDivision extractReferenceSubDivisionFromPostElement(String articleUrl, Element postDivElement) {
    Element dateHeaderDiv = getElementByClassAndNodeName(postDivElement, "div", "date-header");
    if(dateHeaderDiv != null) {
      String articleDate = dateHeaderDiv.text();
      Reference dateReference = findOrCreateReferenceForThatDate(parsePostillionDateFormat(articleDate));

      Element postTitleElement = getElementByClassAndNodeName(postDivElement, "h3", "post-title");
      if(postTitleElement != null) {
        ReferenceSubDivision articleReference = new ReferenceSubDivision(postTitleElement.text());
        articleReference.setOnlineAddress(articleUrl);
        dateReference.addSubDivision(articleReference);
        return articleReference;
      }
      else
        log.error("Could not find H3 node with post-title class");
    }
    else
      log.error("Could not find Div node with date-header class");

    return null;
  }

  protected DateFormat postillionDateFormat = new SimpleDateFormat("dd. MMMMM yyyy", Locale.GERMAN);
  protected DateFormat debugpostillionDateFormat = new SimpleDateFormat("EEEE, dd. MMMMM yyyy", Locale.GERMAN);

  protected String parsePostillionDateFormat(String articleDate) {
    try {
      Date parsedDate = debugpostillionDateFormat.parse(articleDate);
      return DateFormat.getDateInstance(DateFormat.MEDIUM, Localization.getLanguageLocale()).format(parsedDate);
    } catch(Exception ex) { log.error("Could not parse Postillion DateTime Format " + articleDate, ex); }

    int indexOfComma = articleDate.indexOf(',') + 1;
    articleDate = articleDate.substring(indexOfComma, articleDate.length());
    articleDate = articleDate.trim();

    try {
      Date parsedDate = postillionDateFormat.parse(articleDate);
      return DateFormat.getDateInstance(DateFormat.MEDIUM, Localization.getLanguageLocale()).format(parsedDate);
    } catch(Exception ex) { log.error("Could not parse Postillion DateTime Format " + articleDate, ex); }

    return null;
  }
}

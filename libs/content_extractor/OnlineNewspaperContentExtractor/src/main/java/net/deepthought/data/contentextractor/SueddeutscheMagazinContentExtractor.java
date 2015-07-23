package net.deepthought.data.contentextractor;

import net.deepthought.Application;
import net.deepthought.data.model.Category;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.Reference;
import net.deepthought.data.model.ReferenceSubDivision;
import net.deepthought.util.DeepThoughtError;
import net.deepthought.util.Localization;

import org.jsoup.nodes.Comment;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SueddeutscheMagazinContentExtractor extends SueddeutscheContentExtractorBase {

  private final static Logger log = LoggerFactory.getLogger(SueddeutscheMagazinContentExtractor.class);

  @Override
  public String getNewspaperName() {
    return "SZ Magazin";
  }

  @Override
  public String getSiteBaseUrl() {
    return "Sueddeutsche.de";
  }

  @Override
  public boolean canCreateEntryFromUrl(String url) {
    return url.startsWith("http://sz-magazin.sueddeutsche.de/") || url.startsWith("https://sz-magazin.sueddeutsche.de/");
  }


  protected EntryCreationResult parseHtmlToEntry(String articleUrl, Document document) {
    try {
      Element articleHeadElement = document.body().getElementById("artikelhead");
      if(articleHeadElement == null) {
        log.warn("Could not find artikelhead Element");
        return new EntryCreationResult(articleUrl, new DeepThoughtError(Localization.getLocalizedStringForResourceKey("could.not.find.element.of.id.to.extract.article", "articleHead")));
      }

      ReferenceSubDivision articleReference = createReference(articleHeadElement, articleUrl);

      String abstractString = null;
      Element vorspannElement = getElementByClassAndNodeName(articleHeadElement, "div", "vorspann");
      if(vorspannElement != null) {
        for(Element child : vorspannElement.children()) {
          if("p".equals(child.nodeName()))
            abstractString = child.html();
        }
      }

      Element artikelElement = getElementByClassAndNodeName(document.body(), "div", "artikel");
      if(artikelElement == null) {
        log.warn("Could not find artikelhead Element");
        return new EntryCreationResult(articleUrl, new DeepThoughtError(Localization.getLocalizedStringForResourceKey("could.not.find.element.of.class.to.extract.article", "artikel")));
      }

      String content = parseContent(artikelElement);

      Entry articleEntry = new Entry(content, abstractString);
      articleEntry.setReferenceSubDivision(articleReference);

      addNewspaperTag(articleEntry);
      addNewspaperCategory(articleEntry);

      return new EntryCreationResult(document.baseUri(), articleEntry);
    } catch(Exception ex) {
      return new EntryCreationResult(document.baseUri(), new DeepThoughtError(Localization.getLocalizedStringForResourceKey("could.not.create.entry.from.article.html"), ex));
    }
  }

  protected String parseContent(Element artikelElement) {
    String content = "";

    for(Node child : artikelElement.childNodes()) {
      // TODO: also extract instructional image with class text-image-container ?
      if("div".equals(child.nodeName()) == false && child instanceof Comment == false) {
        if(child instanceof Element) { // clear adbox Elements of children
          Element adboxElement = getElementByClassAndNodeName((Element)child, "div", "adbox");
          if(adboxElement != null)
            adboxElement.remove();
          for(Element childChild : ((Element) child).children()) {
            if("br".equals(childChild.nodeName())) // remove leading <br > elements
              childChild.remove();
            else break;
          }
        }

        if(content.length() == 0 && ("br".equals(child.nodeName()) || child.outerHtml().trim().length() == 0)) // remove leading <br> elements and empty text nodes
          continue;

        content += child.outerHtml();
      }
    }

    content += parseContentOfNextPage(artikelElement);
    content = content.trim();

    while(content.endsWith("<br>")) // remove trailing <br> elements
      content = content.substring(0, content.length() - 4).trim();

    return content;
  }

  protected String parseContentOfNextPage(Element artikelElement) {
    Element pagingElement = getElementByClassAndNodeName(artikelElement.parent(), "div", "paging");
    if(pagingElement != null) {
      int currentPageNumber = 0;

      for(Element child : pagingElement.children()) {
        if("span".equals(child.nodeName()) && child.hasClass("active-page")) {
          try { currentPageNumber = Integer.parseInt(child.text()); } catch(Exception ex) { log.error("Could not parse current page number " + child.text() + " to an integer"); }
        }
        else if("a".equals(child.nodeName())) {
          if(currentPageNumber > 0) {
            try {
              int nextPageNumber = Integer.parseInt(child.text());
              if(nextPageNumber == currentPageNumber + 1) {
                return parseContentOfNextPage(child.attr("href"));
              }
            } catch(Exception ex) { log.error("Could not parse next page number " + child.text() + " to an integer"); }
          }
        }
      }
    }

    return "";
  }

  protected String parseContentOfNextPage(String nextPageUrl) {
    if(nextPageUrl.startsWith("http://sz-magazin.sueddeutsche.de") == false)
      nextPageUrl = "http://sz-magazin.sueddeutsche.de" + nextPageUrl;

    try {
      Document nextPageDocument = retrieveOnlineDocument(nextPageUrl);
      Element artikelElement = getElementByClassAndNodeName(nextPageDocument.body(), "div", "artikel");
      if(artikelElement != null)
        return parseContent(artikelElement);
    } catch(Exception ex) { log.error("Could not retrieve HTML code for next Page Url " + nextPageUrl, ex); }
    return "";
  }

  protected ReferenceSubDivision createReference(Element articleHeadElement, String articleUrl) {
    String publishingDate = null, label = null, title = null, subTitle = null;

    for(Element headChild : articleHeadElement.children()) {
      if("p".equals(headChild.nodeName()) && headChild.hasClass("klassifizierung")) {
        for (Element child : headChild.children()) {
          if ("span".equals(child.nodeName())) {
            if (child.hasClass("heft")) {
              publishingDate = child.text();
              publishingDate = publishingDate.toLowerCase().replace("aus ", "").replace("heft ", "");
            } else if (child.hasClass("label"))
              label = child.text();
            else
              publishingDate = tryToParseSueddeutscheMagazinPublishingDate(child.text());
          }
        }
      }
      else if("div".equals(headChild.nodeName()) && headChild.hasClass("vorspann")) {
        for(Element child : headChild.children()) {
          if("h1".equals(child.nodeName()) && child.hasClass("pc"))
            subTitle = child.text();
        }
      }
    }

    for(Element headChild : articleHeadElement.ownerDocument().head().children()) {
      if("title".equals(headChild.nodeName())) {
        title = headChild.text();
        int indexOfLastDash = title.lastIndexOf('-');
        if(indexOfLastDash > 0)
          title = title.substring(0, indexOfLastDash).trim();
        break;
      }
    }

    if(title != null && publishingDate != null) {
      Reference dateReference = findOrCreateReferenceForThatDate(publishingDate);

      ReferenceSubDivision articleReference = new ReferenceSubDivision(title, subTitle);
      articleReference.setOnlineAddress(articleUrl);
      dateReference.addSubDivision(articleReference);

      return articleReference;
    }

    return null;
  }

  protected DateFormat sueddeutscheMagazinDateFormat = new SimpleDateFormat("dd. MMMMM yyyy", Locale.GERMAN);

  protected String tryToParseSueddeutscheMagazinPublishingDate(String publishingDate) {
    try {
      Date parsedDate = sueddeutscheMagazinDateFormat.parse(publishingDate);
      return DateFormat.getDateInstance(DateFormat.MEDIUM, Localization.getLanguageLocale()).format(parsedDate);
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
    Category szMagazinCategory = Application.getDeepThought().findOrCreateSubCategoryForName(sueddeutscheCategory, getNewspaperName());

    szMagazinCategory.addEntry(articleEntry);
  }
}

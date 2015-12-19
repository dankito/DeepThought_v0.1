package net.deepthought.data.contentextractor;

import net.deepthought.Application;
import net.deepthought.data.contentextractor.preview.ArticlesOverviewItem;
import net.deepthought.data.contentextractor.preview.ArticlesOverviewListener;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.ReferenceSubDivision;
import net.deepthought.util.DeepThoughtError;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by ganymed on 16/07/15.
 */
public class PostillonContentExtractor extends OnlineNewspaperContentExtractorBase {

  private final static Logger log = LoggerFactory.getLogger(PostillonContentExtractor.class);

  protected static final String LogoFileName = "der-postillon_logo.png";


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
    return "Postillon";
  }

  @Override
  public String getSiteBaseUrl() {
    return "der-postillon.com";
  }

  @Override
  public String getIconUrl() {
    try {
      URL url = PostillonContentExtractor.class.getClassLoader().getResource(LogoFileName);
      return url.toExternalForm();
    } catch(Exception ex) {
      String logoFile = tryToManuallyLoadIcon(PostillonContentExtractor.class, LogoFileName);
      if (logoFile != IOnlineArticleContentExtractor.NoIcon)
        return logoFile;
      else
        log.error("Could not load " + LogoFileName + " from Resources", ex);
    }

    return super.getIconUrl();
  }

  @Override
  public boolean hasArticlesOverview() {
    return true;
  }

  @Override
  protected void getArticlesOverview(ArticlesOverviewListener listener) {
    extractArticlesOverviewFromFrontPage(listener);
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
        if(isContentTextNode(child))
          content += child.outerHtml();
        else if("div".equals(child.nodeName())) {
          if(isDivisionSonntagsFrage(child))
            content += extractSonntagsFrage(child);
        }
      }
    }
    else {
      log.error("Could not find Div element with entry-content class");
      return new EntryCreationResult(articleUrl, new DeepThoughtError("Could not find entry content.. Maybe Page structure has changed. Please contact programmers.")); // TODO: translate error message
    }

    Entry entry = new Entry(content);
    EntryCreationResult creationResult = new EntryCreationResult(articleUrl, entry);

    ReferenceSubDivision articleReference = extractReferenceSubDivisionFromPostElement(creationResult, articleUrl, postDivElement);
    if(articleReference != null)
      entry.setAbstract(articleReference.getTitle());

    addNewspaperTag(creationResult);
    addNewspaperCategory(creationResult, false);

    return creationResult;
  }

  protected boolean isContentTextNode(Node child) {
    return "div".equals(child.nodeName()) == false &&
           "span".equals(child.nodeName()) == false &&
           ("a".equals(child.nodeName()) == false || child.hasAttr("name") == false || "more".equals(child.attr("name")) == false);
  }

  protected boolean isDivisionSonntagsFrage(Node node) {
    if(node instanceof Element) {
      Elements noScriptChildren = ((Element)node).getElementsByTag("noscript");
      if(noScriptChildren.size() > 0) {
        Element noScriptElement = noScriptChildren.first();
        return noScriptElement.html().contains("://polldaddy.com/poll/");
      }
    }

    return false;
  }

  protected String extractSonntagsFrage(Node sonntagsFrageElement) {
    String html = "<div align=\"center\">";

    for(Node child : sonntagsFrageElement.childNodes()) {
      if("noscript".equals(child.nodeName()) == false)
        html += child.outerHtml();
      else {
        Elements anchorChildren = ((Element)child).getElementsByTag("a");
        if(anchorChildren.size() > 0) {
          Element anchor = anchorChildren.first();
          html += readSonntagsFrageFromUrl(anchor.attr("href"));
        }
      }
    }

    return html + "</div>";
  }

  protected String readSonntagsFrageFromUrl(String url) {
    // download Sonntags Frage
//    try {
//      Document sonntagsFrageDoc = retrieveOnlineDocument(url);
//      Elements pollElements = sonntagsFrageDoc.body().getElementsByClass("poll");
//      if(pollElements.size() > 0) {
//        return pollElements.first().outerHtml();
//      }
//    } catch(Exception ex) { log.error("Could not download SonntagsFrage from Url " + url, ex ); }

    // show Sonntags Frage in an iFrame (but then there's also PollDaddy's website visible like the header, the Social Media shit, ...)
    // as well we cannot store its content locally in database
    return "<iframe src=\"" + url + "\" height=\"600\" width=\"100%\" />";
  }

  protected ReferenceSubDivision extractReferenceSubDivisionFromPostElement(EntryCreationResult creationResult, String articleUrl, Element postDivElement) {
    Element dateHeaderDiv = getElementByClassAndNodeName(postDivElement, "div", "date-header");
    if(dateHeaderDiv != null) {
      String articleDate = dateHeaderDiv.text();

      Element postTitleElement = getElementByClassAndNodeName(postDivElement, "h3", "post-title");
      if(postTitleElement != null) {
        ReferenceSubDivision articleReference = new ReferenceSubDivision(postTitleElement.text());
        articleReference.setOnlineAddressAndLastAccessToCurrentDateTime(articleUrl);

        setArticleReference(creationResult, articleReference, parsePostillionDateFormat(articleDate));

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
      return formatDateToDeepThoughtDateString(parsedDate);
    } catch(Exception ex) { log.error("Could not parse Postillion DateTime Format " + articleDate, ex); }

    int indexOfComma = articleDate.indexOf(',') + 1;
    articleDate = articleDate.substring(indexOfComma, articleDate.length());
    articleDate = articleDate.trim();

    try {
      Date parsedDate = postillionDateFormat.parse(articleDate);
      return formatDateToDeepThoughtDateString(parsedDate);
    } catch(Exception ex) { log.error("Could not parse Postillion DateTime Format " + articleDate, ex); }

    return null;
  }


  protected void extractArticlesOverviewFromFrontPage(ArticlesOverviewListener listener) {
    try {
      Document frontPage = retrieveOnlineDocument("http://www.der-postillon.com");
      extractArticlesOverviewItemsFromFrontPage(frontPage, listener);
    } catch(Exception ex) {
      log.error("Could not retrieve HTML code of Postillon front page", ex);
    }
  }

  protected void extractArticlesOverviewItemsFromFrontPage(Document frontPage, ArticlesOverviewListener listener) {
    extractPostArticles(frontPage, listener);

    extractArchiveArticles(frontPage, listener);
  }

  protected void extractPostArticles(Document frontPage, ArticlesOverviewListener listener) {
    List<ArticlesOverviewItem> items = new ArrayList<>();

    Elements postElements = frontPage.body().getElementsByClass("post");
    for(Element postElement : postElements) {
      ArticlesOverviewItem item = extractOverviewItemFromPostElement(postElement);
      if(item != null)
        items.add(item);
    }

    listener.overviewItemsRetrieved(this, items, false);
  }

  protected ArticlesOverviewItem extractOverviewItemFromPostElement(Element postElement) {
    ArticlesOverviewItem item = null;

    for(Element postChild : postElement.children()) {
      if("h3".equals(postChild.nodeName()) && postChild.hasClass("post-title")) {
        for(Element headerChild : postChild.children()) {
          if("a".equals(headerChild.nodeName()))
            item = new ArticlesOverviewItem(this, headerChild.attr("href"), "", headerChild.text());
        }
      }
      else if("div".equals(postChild.nodeName()) && postChild.hasClass("post-body")) {
        if(item != null) {
          for(Node divChild : postChild.childNodes()) {
            if("div".equals(divChild.nodeName())) {
              Element divChildElement = (Element)divChild;
              if(divChildElement.hasClass("separator")) {
                Elements imgElements = divChildElement.getElementsByTag("img");
                if (imgElements.size() > 0)
                  item.setPreviewImageUrl(imgElements.get(0).attr("src"));
              }
            }
            else {
              if("a".equals(divChild.nodeName()) == false || ((Element)divChild).hasClass("more-link") == false) {
                if(divChild instanceof TextNode)
                  item.setSummary(item.getSummary() + ((TextNode)divChild).text());
                else if(divChild instanceof Element) {
                  Element divChildElement = (Element) divChild;
                  if("br".equals(divChildElement.nodeName()))
                    item.setSummary(item.getSummary() + Application.getPlatformConfiguration().getLineSeparator());
                  else
                    item.setSummary(item.getSummary() + divChildElement.text());
                }
              }
            }
          }
        }
      }
    }

    item.setSummary(item.getSummary().trim());
    if(item.getTitle().startsWith("Newsticker (")) {
//      item.setCategories("Newsticker");
      item.setCategories(item.getTitle());
      item.setTitle(null);
      item.setSummary(" " + item.getSummary());
    }

    return item;
  }

  protected void extractArchiveArticles(Document frontPage, ArticlesOverviewListener listener) {
    List<ArticlesOverviewItem> items = new ArrayList<>();

    Elements archiveArticleElements = frontPage.body().getElementsByClass("archiv-artikel");
    for(Element postElement : archiveArticleElements) {
      ArticlesOverviewItem item = extractOverviewItemFromArchiveArticleElement(postElement);
      if(item != null)
        items.add(item);
    }

    listener.overviewItemsRetrieved(this, items, true);
  }

  protected ArticlesOverviewItem extractOverviewItemFromArchiveArticleElement(Element archiveArticleElement) {
    ArticlesOverviewItem item = null;

    Elements anchorElements = archiveArticleElement.getElementsByTag("a");
    if(anchorElements.size() > 0) {
      Element anchorElement = anchorElements.get(0);

      String url = anchorElement.attr("href");
      if(url.startsWith("/"))
        url = "http://www.der-postillon.com" + url;
      item = new ArticlesOverviewItem(this, url);
      item.setSubTitle("Archiv Artikel");

      for(Element anchorChild : anchorElement.children()) {
        if("div".equals(anchorChild.nodeName())) {
          if(anchorChild.hasClass("img-wrapper")) {
            Elements imgElements = anchorChild.getElementsByTag("img");
            if(imgElements.size() > 0)
              item.setPreviewImageUrl(imgElements.get(0).attr("src"));
          }
          else if(anchorChild.hasClass("text-wrapper"))
            item.setTitle(anchorChild.text());
        }
        else if("img".equals(anchorChild.nodeName()) && anchorChild.hasClass("embedded-video")) { // video article // TODO: YouTube video link is then in 'data-video' attribute
          item.setPreviewImageUrl(anchorChild.attr("src"));
        }
      }
    }

    return item;
  }

}

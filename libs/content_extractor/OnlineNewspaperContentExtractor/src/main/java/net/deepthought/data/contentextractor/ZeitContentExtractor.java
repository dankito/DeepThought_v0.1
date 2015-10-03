package net.deepthought.data.contentextractor;

import net.deepthought.Application;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.ReferenceSubDivision;
import net.deepthought.util.DeepThoughtError;
import net.deepthought.util.Localization;

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
    String multiPageArticleArticleOnOnePageUrl = getArticleOnOnePageUrlForMultiPageArticles(document);
    if(multiPageArticleArticleOnOnePageUrl != null)
      return createEntryFromArticle(multiPageArticleArticleOnOnePageUrl);

    try {
      Element articleBodyElement = getElementByClassAndNodeName(document.body(), "div", "article-body");
      if(articleBodyElement == null)
        return new EntryCreationResult(articleUrl, new DeepThoughtError(Localization.getLocalizedString("could.not.create.entry.from.article.html")));

      Entry articleEntry = createEntry(articleBodyElement);
      EntryCreationResult creationResult = new EntryCreationResult(articleUrl, articleEntry);

      createReference(creationResult, articleUrl, articleBodyElement);

      addTags(document.body(), creationResult);
      addNewspaperCategory(creationResult, true);

      return creationResult;
    } catch(Exception ex) {
      return new EntryCreationResult(articleUrl, new DeepThoughtError(Localization.getLocalizedString("could.not.create.entry.from.article.html"), ex));
    }
  }

  protected String getArticleOnOnePageUrlForMultiPageArticles(Document document) {
    Elements articleOnOnePageElements = document.body().getElementsByAttributeValue("title", "Auf einer Seite");
    if(articleOnOnePageElements.size() > 0) {
      for(Element articleOnOnePageElement : articleOnOnePageElements) {
        if("a".equals(articleOnOnePageElement.nodeName()))
          return articleOnOnePageElement.attr("href");
      }
    }

    return null;
  }

  protected Entry createEntry(Element articleBodyElement) {
    String abstractString = getElementOwnTextByClassAndNodeName(articleBodyElement, "p", "excerpt");

    Element elementBeforeContent = getElementByClassAndNodeName(articleBodyElement, "div", "zol_inarticletools");
    if(elementBeforeContent == null)
      elementBeforeContent = getElementByClassAndNodeName(articleBodyElement, "div", "articlemeta-clear");
    if(elementBeforeContent == null)
      elementBeforeContent = getElementByClassAndNodeName(articleBodyElement, "div", "articlemeta");

    int contentStartElementIndex = articleBodyElement.children().indexOf(elementBeforeContent) + 1;
    String content = extractContentFromArticleBodyChildren(articleBodyElement, contentStartElementIndex);

    return new Entry(content, abstractString);
  }

  protected String extractContentFromArticleBodyChildren(Element articleBodyElement, int contentStartElementIndex) {
    String content = "";

    for(int i = contentStartElementIndex; i < articleBodyElement.children().size(); i++) {
      Element contentParagraph = articleBodyElement.child(i);

      // TODO: filter <p><em>..</em></p> elements? (like: Haben Sie Informationen zu diesem Thema?)
      if("p".equals(contentParagraph.nodeName()) || ("div".equals(contentParagraph.nodeName()) && "block".equals(contentParagraph.className())))
        content += contentParagraph.outerHtml();
    }

    return content;
  }

  protected ReferenceSubDivision createReference(EntryCreationResult creationResult, String articleUrl, Element articleBodyElement) {
    String title = getElementOwnTextByClassAndNodeName(articleBodyElement, "span", "title");

    String subTitle = getElementOwnTextByClassAndNodeName(articleBodyElement, "span", "supertitle");

    String publishingDateString = "";
    Element articleDateTimeElement = getElementByClassAndNodeName(articleBodyElement, "span", "articlemeta-datetime");
    if(articleDateTimeElement != null) {
      Date publishingDate = parseZeitDateTimeFormat(articleDateTimeElement.html());
      if(publishingDate != null)
        publishingDateString = formatDateToDeepThoughtDateString(publishingDate);
    }

    ReferenceSubDivision articleReference = new ReferenceSubDivision(title, subTitle);
    articleReference.setOnlineAddress(articleUrl);

    setArticleReference(creationResult, articleReference, publishingDateString);

    return articleReference;
  }

  protected DateFormat zeitDateTimeFormat = new SimpleDateFormat("dd. MMMMM yyyy HH:mm", Locale.GERMAN);

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

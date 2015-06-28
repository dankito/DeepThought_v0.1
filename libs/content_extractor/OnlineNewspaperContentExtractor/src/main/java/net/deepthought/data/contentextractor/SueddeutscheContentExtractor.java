package net.deepthought.data.contentextractor;

import net.deepthought.data.model.Entry;
import net.deepthought.data.model.Reference;
import net.deepthought.data.model.ReferenceSubDivision;
import net.deepthought.util.DeepThoughtError;
import net.deepthought.util.Localization;
import net.deepthought.util.StringUtils;

import org.jsoup.Connection;
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
import java.util.HashMap;
import java.util.Map;

public class SueddeutscheContentExtractor extends OnlineNewspaperContentExtractorBase {

  private final static Logger log = LoggerFactory.getLogger(SueddeutscheContentExtractor.class);


  @Override
  public String getSiteBaseUrl() {
    return "Sueddeutsche.de";
  }

  @Override
  public boolean canCreateEntryFromUrl(String url) {
    return url.startsWith("http://www.sueddeutsche.de/") || url.startsWith("https://www.sueddeutsche.de/") /*|| url.startsWith("http://sz-magazin.sueddeutsche.de/")*/;
  }


  @Override
  public EntryCreationResult createEntryFromArticle(String articleUrl) {
    if(articleUrl.contains("?reduced=true"))
      articleUrl = articleUrl.replace("?reduced=true", "");
    return super.createEntryFromArticle(articleUrl);
  }

  protected EntryCreationResult parseHtmlToEntry(String articleUrl, Document document) {
    try {
      // TODO: implement Sueddeutsche Magazin articles like http://sz-magazin.sueddeutsche.de/texte/anzeigen/42288/Das-Zerquetschen-von-Eiern
      Element articleElement = null;
      Elements articleElements = document.body().getElementsByTag("article");
      if (articleElements.size() == 1)
        articleElement = articleElements.get(0);
      else
        articleElement = document.body().getElementById("sitecontent");

      if(articleElement.getElementsByClass("article-paging").size() > 0) { // an article with multiple pages
        try {
          Map<String, String> data = new HashMap<>();
          data.put("article.singlePage", "true");
          return parseHtmlToEntry(articleUrl, retrieveOnlineDocument(articleUrl, DefaultUserAgent, data, Connection.Method.POST));
        } catch(Exception ex) { log.error("Could not get Html Document for Sueddeutsche Article with multiple pages of Url " + articleUrl, ex); }
      }

      ReferenceSubDivision reference = createReference(articleUrl, articleElement);

      Entry articleEntry = createEntry(articleElement);
      if(reference != null)
        articleEntry.setReferenceSubDivision(reference);

      addTags(articleEntry, "SZ");
      addCategory(articleEntry, "SZ", true);

      return new EntryCreationResult(document.baseUri(), articleEntry);
    } catch(Exception ex) {
      return new EntryCreationResult(document.baseUri(), new DeepThoughtError(Localization.getLocalizedStringForResourceKey("could.not.create.entry.from.article.html"), ex));
    }
  }

  protected Entry createEntry(Element articleElement) throws Exception {
    Elements bodyClassElements = articleElement.getElementsByClass("body");
    Element bodySection = null;
    for(Element element : bodyClassElements) {
      if("section".equals(element.tagName())) {
        bodySection = element;
        break;
      }
      else if("div".equals(element.tagName()) && articleElement.classNames().contains("gallery"))
        return createEntryFromImageGalleryArticle(articleElement, element);
    }

    if(bodySection == null) {
      log.error("Could not find Article Body section for Sueddeutsche Article " + articleElement.baseUri());
      throw new Exception(Localization.getLocalizedStringForResourceKey("could.not.find.sueddeutsche.article.body.section", articleElement.baseUri()));
    }

    return extractEntryFromBodySection(bodySection);
  }

  protected Entry extractEntryFromBodySection(Element bodySection) throws Exception {
    Entry entry = new Entry();
    String content = "";

    content += extractInlineGalleryHtmlIfAny(bodySection);

    content += extractTextFromElementChildren(bodySection, entry);

    if(content.length() == 0) {
      log.error("Could not extract content from Body section for Sueddeutsche Article " + bodySection.baseUri());
      throw new Exception(Localization.getLocalizedStringForResourceKey("could.not.extract.content.from.sueddeutsche.article.body.section", bodySection.baseUri()));
    }

    entry.setContent(content.replaceAll("\u00A0", " ")); // Converting nbsp entities
//    Application.getDeepThought().addEntry(entry);

    return entry;
  }

  protected String extractTextFromElementChildren(Element parentElement, Entry entry) {
    String content = "";

    // body section contains a lot of stuff we don't need but luckily all article (text) data is given in Paragraph elements
    for(Element bodyChild : parentElement.children()) {
      if("p".equals(bodyChild.tagName())) { // so check if child element is a Paragraph element or a (for us useless) other element
        if("article entry-summary".equals(bodyChild.className())) // there's only one special Paragraph element, the first one, with the article summary
          entry.setAbstract(bodyChild.text().trim());
        else
          content += "<p>" + bodyChild.html().trim() + "</p>";
      }
      else if("ul".endsWith(bodyChild.tagName())) {
        if(entry != null && StringUtils.isNullOrEmpty(entry.getAbstract()) && bodyChild.siblingIndex() <= 6) // the abstract as an unordered list; it's not marked as 'article entry-summary' and is in 5th position
          entry.setAbstract(bodyChild.outerHtml().replaceAll("\u00A0", " "));
        else  // else wise the unordered list belongs to the article content
          content += bodyChild.outerHtml().replaceAll("\u00A0", " ");
      }
      // TODO: what about Headers?
      else if("figure".equals(bodyChild.tagName())) {
        if(bodyChild.hasClass("gallery") /*&& bodyChild.hasClass("inline")*/)
          content += parseInlineImageGallery(bodyChild);
        else
          content += extractImageFromFigureNode(bodyChild);
      }
      else if(("div".equals(bodyChild.tagName()) && bodyChild.hasClass("basebox"))) {
        if(bodyChild.hasClass("embed"))
          content += bodyChild.outerHtml().replace(" src=\"//", " src=\"http://");
        else if(bodyChild.hasClass("include"))
          content += bodyChild.outerHtml();
      }
    }

    return content;
  }

  protected String extractInlineGalleryHtmlIfAny(Element bodySection) {
    Elements topEnrichmentElements = bodySection.parent().getElementsByClass("topenrichment");
    if(topEnrichmentElements.size() > 0) {
      Element imageGalleryElement = getElementByClassAndNodeName(topEnrichmentElements.get(0), "figure", "gallery");
      if(imageGalleryElement != null)
        return parseInlineImageGallery(topEnrichmentElements.get(0));
    }

    return "";
  }

  protected String parseInlineImageGallery(Element inlineImageGalleryNode) {
    String galleryHtml = "";

    Elements imageDivisions = inlineImageGalleryNode.getElementsByClass("image");
    for(Element imageDivision : imageDivisions) {
      if("div".equals(imageDivision.nodeName()))
        galleryHtml += extractImageFromFigureNode(imageDivision.parent());
    }

    return galleryHtml;
  }

  protected String extractImageFromFigureNode(Element figureNode) {
    if(figureNode.hasClass("teaser"))
      return "";

    String imageHtml = "";

    Elements imgElements = figureNode.getElementsByTag("img");
    if(imgElements.size() > 0)
      imageHtml += imgElements.get(0).outerHtml();

    Elements descriptionElements = figureNode.getElementsByClass("entry-title");
    if(descriptionElements.size() > 0)
      imageHtml += "<br />" + descriptionElements.get(0).html();
    else {
      descriptionElements = figureNode.getElementsByClass("caption");
      if(descriptionElements.size() > 0) {
        Element textElement = getElementByClassAndNodeName(descriptionElements.get(0), "div", "text");
        if(textElement != null)
          imageHtml += "<br />" + textElement.html();
        else // inline image gallery
          imageHtml += extractTextFromElementChildren(descriptionElements.get(0), null);
      }
    }

    if(imageHtml.length() > 0)
      imageHtml = "<p>" + imageHtml + "</p>";
    else
      log.warn("Could not extract Image from figure node " + figureNode.outerHtml());

    return imageHtml;
  }

  protected Entry createEntryFromImageGalleryArticle(Element articleElement, Element articleBodyElement) {
    String abstractString = getElementOwnTextByClassAndNodeName(articleElement, "p", "entry-summary");

    String content = readHtmlOfAllImagesInGallery(articleBodyElement);

    Entry createdEntry = new Entry(content, abstractString);
    createdEntry.setReferenceSubDivision(createReferenceForImageGallery(articleElement, articleBodyElement));

    return createdEntry;
  }

  protected String readHtmlOfAllImagesInGallery(Element articleBodyElement) {
    if(articleBodyElement == null)
      return "";

    String content = "";
    Elements figureElements = articleBodyElement.getElementsByTag("figure");
    if(figureElements.size() > 0)
      content += extractImageFromFigureNode(figureElements.get(0));
    else  // end of Image Gallery
      return "";

    String url = getUrlOfNextImageInGallery(articleBodyElement);
    if(url != null) {
      try {
        Document document = retrieveOnlineDocument(url);
        content += readHtmlOfAllImagesInGallery(getElementByClassAndNodeName(document.body(), "div", "body"));
      } catch (Exception ex) {
        log.error("Could not retrieve Html Document for next image in Gallery. Next Image Url was " + url + System.lineSeparator() + "Current image article body element: " + articleBodyElement.outerHtml(), ex);
      }
    }


    return content;
  }

  protected String getUrlOfNextImageInGallery(Element articleBodyElement) {
    Elements anchorElements = articleBodyElement.getElementsByTag("a");
    for(Element anchor : anchorElements) {
      if(anchor.text().contains("Nächstes Bild"))
        return anchor.attr("href");
    }

    log.warn("Could not find url for next Image in Gallery. Article Body Element: " + articleBodyElement.outerHtml());
    return null;
  }

  protected ReferenceSubDivision createReferenceForImageGallery(Element articleElement, Element articleBodyElement) {
    Element sourceElement = getElementByClassAndNodeName(articleBodyElement, "span", "source");
    if(sourceElement != null) {
      String offscreenElementText = getElementOwnTextByClassAndNodeName(sourceElement, "span", "offscreen");
      if(StringUtils.isNotNullOrEmpty(offscreenElementText))
        return createReferenceForImageGallery(articleElement, offscreenElementText);
    }
    else
      log.warn("Could not find Span with class 'source' to get publishing date of Image Gallery. Article Body Element was: " + articleBodyElement.outerHtml());

    return null;
  }

  protected ReferenceSubDivision createReferenceForImageGallery(Element articleElement, String rawPublishingDateString) {
    String publishingDate = parseSueddeutscheHeaderDate(rawPublishingDateString);
    if(StringUtils.isNotNullOrEmpty(publishingDate)) {
      Element headerElement = getElementByClassAndNodeName(articleElement, "div", "header");
      if(headerElement != null) {
        ReferenceSubDivision articleReference = extractReferenceSubDivisionFromHeaderSection(articleElement.baseUri(), headerElement);

        Reference sueddeutscheDateReference = findOrCreateReferenceForThatDate("SZ", publishingDate);
        sueddeutscheDateReference.addSubDivision(articleReference);
        return articleReference;
      }
    }

    return null;
  }

  protected ReferenceSubDivision createReference(String articleUrl, Element articleElement) {
    Elements headerClassElements = articleElement.getElementsByClass("header");
    Element headerSection = null;
    for(Element element : headerClassElements) {
      if("section".equals(element.tagName())) {
        headerSection = element;
        break;
      }
    }

    if(headerSection == null) {
      log.error("Could not find Article Header section for Sueddeutsche Article " + articleElement.baseUri());
      return null;
    }

    return extractReferenceFromHeaderSection(articleUrl, headerSection);
  }

  protected ReferenceSubDivision extractReferenceFromHeaderSection(String articleUrl, Element headerSection) {
    ReferenceSubDivision articleReference = extractReferenceSubDivisionFromHeaderSection(articleUrl, headerSection);
    String articleDate = "";

// Header section has two children: time containing publishing time and a h2 element contain article title and subtitle
//    for(Element headerSectionChild : headerSection.children()) {
//      if("time".equals(headerSectionChild.tagName()))
//        articleDate = parseSueddeutscheHeaderDate(headerSectionChild.attributes().get("datetime"));
//    }
    Elements timeElements = headerSection.getElementsByTag("time");
    if(timeElements.size() > 0)
      articleDate = parseSueddeutscheHeaderDate(timeElements.get(0).attributes().get("datetime"));

    Reference sueddeutscheDateReference = findOrCreateReferenceForThatDate("SZ", articleDate);
    sueddeutscheDateReference.addSubDivision(articleReference);

    return articleReference;
  }

  protected ReferenceSubDivision extractReferenceSubDivisionFromHeaderSection(String articleUrl, Element headerSection) {
    ReferenceSubDivision articleReference = new ReferenceSubDivision();
    articleReference.setOnlineAddress(articleUrl);

    for(Element headerSectionChild : headerSection.children()) { // Header section has two children: time containing publishing time and a h2 element contain article title and subtitle
      if(headerSectionChild.tagName().startsWith("h")) {
        for(Node headerChild : headerSectionChild.childNodes()) {
          if(headerChild instanceof Element && "strong".equals(headerChild.nodeName()))
            articleReference.setSubTitle(((Element) headerChild).text().trim());
          else if(headerChild instanceof TextNode && StringUtils.isNotNullOrEmpty(headerChild.outerHtml().trim()))
            articleReference.setTitle(((TextNode)headerChild).text().trim());
        }
      }
    }

    return articleReference;
  }

  protected DateFormat sueddeutscheHeaderDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

  protected String parseSueddeutscheHeaderDate(String datetime) {
    try {
      Date parsedDate = sueddeutscheHeaderDateFormat.parse(datetime);
      return DateFormat.getDateInstance(DateFormat.MEDIUM, Localization.getLanguageLocale()).format(parsedDate);
    } catch(Exception ex) { log.error("Could not parse Sueddeutsche Header Date " + datetime, ex); }
    return "";
  }

}
package net.deepthought.data.contentextractor;

import net.deepthought.Application;
import net.deepthought.data.contentextractor.preview.ArticlesOverview;
import net.deepthought.data.contentextractor.preview.ArticlesOverviewItem;
import net.deepthought.data.contentextractor.preview.ArticlesOverviewListener;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.ReferenceSubDivision;
import net.deepthought.data.model.Tag;
import net.deepthought.util.DeepThoughtError;
import net.deepthought.util.StringUtils;
import net.deepthought.util.localization.Localization;

import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SueddeutscheContentExtractor extends SueddeutscheContentExtractorBase {

  private final static Logger log = LoggerFactory.getLogger(SueddeutscheContentExtractor.class);

  // TODO: Improve architecture so that calls for Article extraction of SZ Magazin articles land here
  protected SueddeutscheMagazinContentExtractor szMagazinContentExtractor = new SueddeutscheMagazinContentExtractor();

  protected SueddeutscheJetztContentExtractor jetztContentExtractor = new SueddeutscheJetztContentExtractor();


  @Override
  public int getSupportedPluginSystemVersion() {
    return 1;
  }

  @Override
  public String getPluginVersion() {
    return "0.1";
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
    return url.startsWith("http://www.sueddeutsche.de/") || url.startsWith("https://www.sueddeutsche.de/") ||
        szMagazinContentExtractor.canCreateEntryFromUrl(url) || jetztContentExtractor.canCreateEntryFromUrl(url);
  }


  @Override
  public EntryCreationResult createEntryFromArticle(String articleUrl) {
    if(szMagazinContentExtractor.canCreateEntryFromUrl(articleUrl))
      return szMagazinContentExtractor.createEntryFromArticle(articleUrl);
    else if(jetztContentExtractor.canCreateEntryFromUrl(articleUrl))
      return jetztContentExtractor.createEntryFromArticle(articleUrl);

    if(articleUrl.contains("?reduced=true"))
      articleUrl = articleUrl.replace("?reduced=true", "");
    return super.createEntryFromArticle(articleUrl);
  }

  protected EntryCreationResult parseHtmlToEntry(String articleUrl, Document document) {
    try {
      // TODO: implement Sueddeutsche Magazin articles like http://sz-magazin.sueddeutsche.de/texte/anzeigen/42288/Das-Zerquetschen-von-Eiern
      if(document.body().getElementById("singlePageForm") != null) { // an article with multiple pages
        try {
          Map<String, String> data = new HashMap<>();
          data.put("article.singlePage", "true");
          return parseHtmlToEntry(articleUrl, retrieveOnlineDocument(articleUrl, DefaultUserAgent, data, Connection.Method.POST));
        } catch(Exception ex) { log.error("Could not get Html Document for Sueddeutsche Article with multiple pages of Url " + articleUrl, ex); }
      }

      Element articleElement = null;
      Elements articleElements = document.body().getElementsByTag("article");
      if (articleElements.size() == 1)
        articleElement = articleElements.get(0);
      else
        articleElement = document.body().getElementById("sitecontent");

      EntryCreationResult creationResult = createEntry(articleUrl, articleElement);

      if(creationResult.isAReferenceSet() == false) {
        createReference(creationResult, articleUrl, articleElement);
      }

      findOrCreateTagAndAddToCreationResult(creationResult);
      addNewspaperCategory(creationResult, true);

      return creationResult;
    } catch(Exception ex) {
      return new EntryCreationResult(articleUrl, new DeepThoughtError(Localization.getLocalizedString("could.not.create.entry.from.article.html"), ex));
    }
  }

  protected EntryCreationResult createEntry(String articleUrl, Element articleElement) throws Exception {
    Elements bodyClassElements = articleElement.getElementsByClass("body");
    Element bodySection = null;
    for(Element element : bodyClassElements) {
      if("section".equals(element.tagName())) {
        bodySection = element;
        break;
      }
      else if("div".equals(element.tagName()) && articleElement.classNames().contains("gallery"))
        return createEntryFromImageGalleryArticle(articleUrl, articleElement, element);
    }

    if(bodySection != null) {
      Entry articleEntry = extractEntryFromBodySection(bodySection);
      return new EntryCreationResult(articleUrl, articleEntry);
    }
    else if(articleElement.hasClass("video-article")) {
      return extractVideoArticle(articleUrl, articleElement);
    }
    else {
      log.error("Could not find Article Body section for Sueddeutsche Article " + articleElement.baseUri());
      throw new Exception(Localization.getLocalizedString("could.not.find.sueddeutsche.article.body.section", articleElement.baseUri()));
    }
  }

  protected Entry extractEntryFromBodySection(Element bodySection) throws Exception {
    Entry entry = new Entry();
    String content = "";

    content += extractUsefulTopEnrichmentElements(bodySection);

    content += extractTextFromElementChildren(bodySection, entry);

    if(content.length() == 0) {
      log.error("Could not extract content from Body section for Sueddeutsche Article " + bodySection.baseUri());
      throw new Exception(Localization.getLocalizedString("could.not.extract.content.from.sueddeutsche.article.body.section", bodySection.baseUri()));
    }

    entry.setContent(content.replaceAll("\u00A0", " ")); // Converting nbsp entities

    return entry;
  }

  protected String extractTextFromElementChildren(Element parentElement, Entry entry) {
    String content = "";
    boolean isInterviewArticle = false;

    // body section contains a lot of stuff we don't need but luckily all article (text) data is given in Paragraph elements
    for(Element bodyChild : parentElement.children()) {
      if("p".equals(bodyChild.tagName())) { // so check if child element is a Paragraph element or a (for us useless) other element
        Element nextElementSibling = bodyChild.nextElementSibling();
        if(bodyChild.hasClass("entry-summary") || // there's only one special Paragraph element, the first one, with the article summary
            (nextElementSibling != null && "section".equals(nextElementSibling.tagName()) && nextElementSibling.hasClass("authors")))
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
      else if("h3".equals(bodyChild.tagName())) {
        if(bodyChild.text().startsWith("SZ: "))
          isInterviewArticle = true;
        if(isInterviewArticle)
          content += bodyChild.outerHtml();
      }
      else if("figure".equals(bodyChild.tagName())) {
        if(bodyChild.hasClass("gallery")) {
          // not checking for class 'inline' would cause articles like http://www.sueddeutsche.de/bayern/oberfranken-wanderer-halten-fluechtlingsheim-fuer-gasthof-und-werden-bewirtet-1.2789250
          // loading the next article
          if(bodyChild.hasClass("inline")) {
            content += parseInlineImageGallery(bodyChild);
          }
        }
        else
          content += extractImageFromFigureNode(bodyChild);
      }
      else if(("div".equals(bodyChild.tagName()) && bodyChild.hasClass("basebox"))) {
        if(bodyChild.hasClass("embed"))
          content += bodyChild.outerHtml().replace(" src=\"//", " src=\"http://");
        else if(bodyChild.hasClass("include")) {
          makeLazyLoadingChildrenEagerLoading(bodyChild);
          content += bodyChild.outerHtml();
        }
      }
    }

    return content;
  }

  protected void makeLazyLoadingChildrenEagerLoading(Element bodyChild) {
    Elements lazyLoadChildren = bodyChild.getElementsByClass("lazyload");
    if(lazyLoadChildren.size() > 0) {
      for(Element lazyLoadChild : lazyLoadChildren) {
        makeLazyLoadingElementEagerLoading(lazyLoadChild);
      }
    }
  }

  protected String extractUsefulTopEnrichmentElements(Element bodySection) {
    Elements topEnrichmentElements = bodySection.parent().getElementsByClass("topenrichment");
    if(topEnrichmentElements.size() > 0) {
      Element imageGalleryElement = getElementByClassAndNodeName(topEnrichmentElements.get(0), "figure", "gallery");
      if(imageGalleryElement != null)
        return parseInlineImageGallery(topEnrichmentElements.get(0));

      Element includeElement = getElementByClassAndNodeName(topEnrichmentElements.get(0), "div", "include");
      if(includeElement != null && includeElement.hasClass("basebox")) {
        makeLazyLoadingChildrenEagerLoading(includeElement);
        return includeElement.outerHtml();
      }
    }

    return "";
  }

  protected String parseInlineImageGallery(Element inlineImageGalleryNode) {
    String galleryHtml = "";

    Elements imageDivisions = inlineImageGalleryNode.getElementsByClass("image");
    for(Element imageDivision : imageDivisions) {
      if("div".equals(imageDivision.nodeName()))
        galleryHtml += extractImageFromFigureNode(imageDivision.parent());
      else if("figure".equals(imageDivision.nodeName()))
        galleryHtml += parseInlineGalleryFromFigureNode(imageDivision);
    }

    return galleryHtml;
  }

  protected String parseInlineGalleryFromFigureNode(Element figureNode) {
    Elements anchors = figureNode.getElementsByTag("a");
    for(Element anchor : anchors) {
      if(anchor.hasAttr("data-pagetype") && anchor.attr("data-pagetype").contains("image") && anchor.attr("data-pagetype").contains("gallery")) {
        String galleryUrl = anchor.attr("href");
        try {
          return readHtmlOfAllImagesInGalleryOfUrl(galleryUrl);
        } catch(Exception ex) {
          log.error("Could not read Inline Gallery from Url " + galleryUrl, ex);
        }
      }
    }

    return "";
  }

  protected String extractImageFromFigureNode(Element figureNode) {
    if(figureNode.hasClass("teaser"))
      return "";

    String imageHtml = "";

    Elements imgElements = figureNode.getElementsByTag("img");
    if(imgElements.size() > 0)
      imageHtml += extractImgElementHtml(imgElements.get(0));

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

  protected EntryCreationResult createEntryFromImageGalleryArticle(String articleUrl, Element articleElement, Element articleBodyElement) {
    String abstractString = getElementOwnTextByClassAndNodeName(articleElement, "p", "entry-summary");

    String content = readHtmlOfAllImagesInGallery(articleBodyElement);

    Entry createdEntry = new Entry(content, abstractString);
    EntryCreationResult creationResult = new EntryCreationResult(articleUrl, createdEntry);

    createReferenceForImageGallery(creationResult, articleElement, articleBodyElement);

    return creationResult;
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
        content += readHtmlOfAllImagesInGalleryOfUrl(url);
      } catch (Exception ex) {
        log.error("Could not retrieve Html Document for next image in Gallery. Next Image Url was " + url + Application.getPlatformConfiguration().getLineSeparator() + "Current image article body element: " + articleBodyElement.outerHtml(), ex);
      }
    }


    return content;
  }

  protected String readHtmlOfAllImagesInGalleryOfUrl(String url) throws IOException {
    String content = "";

    Document document = retrieveOnlineDocument(url);
    content += readHtmlOfAllImagesInGallery(getElementByClassAndNodeName(document.body(), "div", "body"));

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


  protected EntryCreationResult extractVideoArticle(String articleUrl, Element articleElement) {
    Element videoPlayerElement = articleElement.select(".video-article__player").first();
    if(videoPlayerElement != null) {
      Entry entry = new Entry(videoPlayerElement.outerHtml());
      EntryCreationResult result = new EntryCreationResult(articleUrl, entry);

      Element articleInfoElement = articleElement.select("#article-body").first();
      if (articleInfoElement != null) {
        Element descriptionElement = articleInfoElement.getElementsByClass("article-info__description").first();
        if(descriptionElement != null) {
          entry.setAbstract(descriptionElement.text());
        }

        createReferenceForVideoArticle(result, articleUrl, articleElement, articleInfoElement);
      }

      findOrCreateTagAndAddToCreationResult(result, Localization.getLocalizedString("video"));

      return result;
    }

    return null;
  }

  protected void createReferenceForVideoArticle(EntryCreationResult result, String articleUrl, Element articleElement, Element articleInfoElement) {
    Element captionTitleElement = articleInfoElement.getElementsByClass("caption__title").first();
    if(captionTitleElement != null) {
      ReferenceSubDivision subDivision = new ReferenceSubDivision(captionTitleElement.text());

      Element captionOverlineElement = articleInfoElement.getElementsByClass("caption__overline").first();
      if(captionOverlineElement != null) {
        subDivision.setSubTitle(captionOverlineElement.text());
      }

      extractReferenceFromHeaderSection(result, subDivision, articleUrl, articleElement);
    }
    else {
      log.error("Could not create Reference from Video Article, Element with class 'caption__title' not found");
    }
  }


  protected ReferenceSubDivision createReferenceForImageGallery(EntryCreationResult creationResult, Element articleElement, Element articleBodyElement) {
    Element sourceElement = getElementByClassAndNodeName(articleBodyElement, "span", "source");
    if(sourceElement != null) {
      String offscreenElementText = getElementOwnTextByClassAndNodeName(sourceElement, "span", "offscreen");
      if(StringUtils.isNotNullOrEmpty(offscreenElementText))
        return createReferenceForImageGallery(creationResult, articleElement, offscreenElementText);
    }
    else
      log.warn("Could not find Span with class 'source' to get publishing date of Image Gallery. Article Body Element was: " + articleBodyElement.outerHtml());

    return null;
  }

  protected ReferenceSubDivision createReferenceForImageGallery(EntryCreationResult creationResult, Element articleElement, String rawPublishingDateString) {
    String publishingDate = parseSueddeutscheHeaderDate(rawPublishingDateString);
    if(StringUtils.isNotNullOrEmpty(publishingDate)) {
      Element headerElement = getElementByClassAndNodeName(articleElement, "div", "header");
      if(headerElement != null) {
        ReferenceSubDivision articleReference = extractReferenceSubDivisionFromHeaderSection(articleElement.baseUri(), headerElement);
        setArticleReference(creationResult, articleReference, publishingDate);

        return articleReference;
      }
    }

    return null;
  }

  protected ReferenceSubDivision createReference(EntryCreationResult creationResult, String articleUrl, Element articleElement) {
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

    return extractReferenceAndSubDivisionFromHeaderSection(creationResult, articleUrl, headerSection);
  }

  protected ReferenceSubDivision extractReferenceAndSubDivisionFromHeaderSection(EntryCreationResult creationResult, String articleUrl, Element headerSection) {
    ReferenceSubDivision articleReferenceSubDivision = extractReferenceSubDivisionFromHeaderSection(articleUrl, headerSection);

    return extractReferenceFromHeaderSection(creationResult, articleReferenceSubDivision, articleUrl, headerSection);
  }

  protected ReferenceSubDivision extractReferenceFromHeaderSection(EntryCreationResult creationResult, ReferenceSubDivision articleReferenceSubDivision, String articleUrl, Element headerSection) {
    String articleDate = "";
    Elements timeElements = headerSection.getElementsByTag("time");
    if(timeElements.size() > 0)
      articleDate = parseSueddeutscheHeaderDate(timeElements.get(0).attributes().get("datetime"));

    setArticleReference(creationResult, articleReferenceSubDivision, articleDate);

    return articleReferenceSubDivision;
  }

  protected ReferenceSubDivision extractReferenceSubDivisionFromHeaderSection(String articleUrl, Element headerSection) {
    ReferenceSubDivision articleReference = new ReferenceSubDivision();
    articleReference.setOnlineAddressAndLastAccessToCurrentDateTime(articleUrl);

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
      return formatDateToDeepThoughtDateString(parsedDate);
    } catch(Exception ex) { log.error("Could not parse Sueddeutsche Header Date " + datetime, ex); }
    return "";
  }


  protected ArticlesOverview extractArticlesOverviewFromFrontPage(ArticlesOverviewListener listener) {
    try {
      Document frontPage = retrieveOnlineDocument("http://www.sueddeutsche.de");
      List<ArticlesOverviewItem> items = extractArticlesOverviewItemsFromFrontPage(frontPage, listener);
      return new ArticlesOverview(items);
    } catch(Exception ex) {
      log.error("Could not retrieve HTML code of Sueddeutsche front page", ex);
    }
    return null;
  }

  protected List<ArticlesOverviewItem> extractArticlesOverviewItemsFromFrontPage(Document frontPage, ArticlesOverviewListener listener) {
    List<ArticlesOverviewItem> items = new ArrayList<>();

//    listener.overviewItemsRetrieved(this, extractSocialModuleItems(frontPage), false);
    listener.overviewItemsRetrieved(this, extractTeaserItems(frontPage), false);
    listener.overviewItemsRetrieved(this, extractTileItems(frontPage), false);
    listener.overviewItemsRetrieved(this, extractTeaserElementsItems(frontPage), true);
    // TODO: also parse flyout teasers (<ul class="flyout-teasers">) ?

    return items;
  }

  protected Collection<ArticlesOverviewItem> extractTeaserItems(Document frontPage) {
    List<ArticlesOverviewItem> items = new ArrayList<>();

    Elements teasers = frontPage.body().getElementsByClass("teaser");
    for (Element teaser : teasers) {
      if(teaser.classNames().size() > 1) { // there are some Teasers like 'Stellenangebote' which are more or less Ads. These have only one class
        ArticlesOverviewItem teaserItem = extractItemFromTeaser(teaser);
        if (teaserItem != null) {
          items.add(teaserItem);
        }

        items.addAll(extractOneLinerTeaserItemsFromTeaserElement(teaser));
      }
    }

    return items;
  }

  protected ArticlesOverviewItem extractItemFromTeaser(Element teaser) {
    ArticlesOverviewItem item = null;

    for(Element child : teaser.children()) {
      if("a".equals(child.nodeName())) {
        item = createOverviewItemFromAnchorElement(child);
      }
      else if("p".equals(child.nodeName()) && child.hasClass("entry-summary")) {
        if(item != null) {
          item.setSummary(child.ownText());
          tryToExtractLabel(item, child);
        }
      }
    }

    return item;
  }


  protected Collection<ArticlesOverviewItem> extractTeaserElementsItems(Document frontPage) {
    List<ArticlesOverviewItem> items = new ArrayList<>();

    Elements teaserElements = frontPage.body().getElementsByClass("teaserElement");
    for (Element teaser : teaserElements) {
      ArticlesOverviewItem teaserItem = extractItemFromTeaserElement(teaser);
      if (teaserItem != null)
        items.add(teaserItem);

      items.addAll(extractOneLinerTeaserItemsFromTeaserElement(teaser));
    }

    return items;
  }

  protected ArticlesOverviewItem extractItemFromTeaserElement(Element teaser) {
    ArticlesOverviewItem item = null;

    for(Element child : teaser.children()) {
      if("a".equals(child.nodeName())) {
        item = createOverviewItemFromAnchorElement(child);
      }
      else if("p".equals(child.nodeName()) && child.hasClass("teaserElement__text")) {
        if(item != null) {
          item.setSummary(child.ownText());
          tryToExtractLabel(item, child);
        }
      }
    }

    return item;
  }

  protected ArticlesOverviewItem createOverviewItemFromAnchorElement(Element anchorElement) {
    ArticlesOverviewItem item = new ArticlesOverviewItem(this, anchorElement.attr("href"));

    Element img = anchorElement.select("img").first();
    if(img != null) {
      item.setPreviewImageUrl(extractImageUrlFromImgElement(img));
    }

    Element em = anchorElement.select("em").first();
    if(em != null) {
      item.setTitle(em.text().trim());
    }

    tryToExtractLabel(item, anchorElement);

    return item;
  }

  protected String extractImageUrlFromImgElement(Element imgElement) {
    if(imgElement.hasClass("lazyload") && imgElement.hasAttr("data-src"))
      return imgElement.attr("data-src");
    return imgElement.attr("src");
  }

  protected String extractImgElementHtml(Element imgElement) {
    makeLazyLoadingElementEagerLoading(imgElement);

    return imgElement.outerHtml();
  }

  protected void makeLazyLoadingElementEagerLoading(Element lazyLoadElement) {
    if(lazyLoadElement.hasClass("lazyload")) {
      if(lazyLoadElement.hasAttr("data-src")) {
        lazyLoadElement.attr("src", lazyLoadElement.attr("data-src"));
        lazyLoadElement.removeAttr("data-src");
      }

//      lazyLoadElement.removeClass("lazyload");
    }
  }

  protected List<ArticlesOverviewItem> extractOneLinerTeaserItemsFromTeaserElement(Element teaser) {
    List<ArticlesOverviewItem> allExtractedItems = new ArrayList<>();

    for(Element oneLiner : teaser.getElementsByClass("oneliner")) {
      if("ul".equals(oneLiner.nodeName())) {
        for(Element listItem : oneLiner.children()) {
          ArticlesOverviewItem oneLinerItem = null;

          for (Element child : listItem.children()) {
            if ("a".equals(child.nodeName())) {
              oneLinerItem = new ArticlesOverviewItem(this, child.attr("href"));
              for (Element anchorChild : child.children()) {
                if ("div".equals(anchorChild.nodeName())) {
//                  if ("strong".equals(anchorChild.nodeName())) // actually div as a span child and that again has a strong child
                    oneLinerItem.setSubTitle(anchorChild.text());
                }
                else if ("em".equals(anchorChild.nodeName()))
                  oneLinerItem.setTitle(anchorChild.text());
              }

              tryToExtractLabel(oneLinerItem, child);
            }
          }

          if (oneLinerItem != null) {
            allExtractedItems.add(oneLinerItem);
          }
        }
      }
    }

    return allExtractedItems;
  }

  protected void tryToExtractLabel(ArticlesOverviewItem item, Element itemElement) {
    Elements labelElements = itemElement.getElementsByClass("teaserlabel");
    if(labelElements.size() > 0) {
      item.setLabel(labelElements.get(0).text());
    }
    else {
      labelElements = itemElement.select(".flyout-teaser-label");
      if(labelElements.size() > 0) {
        item.setLabel(labelElements.get(0).text());
      }
    }
  }

  protected Collection<ArticlesOverviewItem> extractTileItems(Document frontPage) {
    List<ArticlesOverviewItem> extractedItems = new ArrayList<>();

    Elements tileElements = frontPage.body().getElementsByClass("tile-teaser-content");
    for(Element tileElement : tileElements) {
      if("div".equals(tileElement.nodeName()) && (tileElement.parent().hasAttr("data-status") == false || "hidden".equals(tileElement.parent().attr("data-status")) == false)) { // don't parse hidden Tiles
        ArticlesOverviewItem item = null;

        for(Element tileChild : tileElement.children()) {
          if("a".equals(tileChild.nodeName())) {
            item = createOverviewItemFromAnchorElement(tileChild);
          }
          else if(tileChild.hasClass("tile-teaser-text")) {
            if(item != null)
              item.setSummary(tileChild.text());
          }
        }

        if(item != null) {
          extractedItems.add(item);
          tryToExtractLabel(item, tileElement);
        }
      }
    }

    return extractedItems;
  }

  protected Collection<ArticlesOverviewItem> extractSocialModuleItems(Document frontPage) {
    List<ArticlesOverviewItem> extractedItems = new ArrayList<>();

    Elements socialModuleElements = frontPage.body().getElementsByClass("socialmodule-mainslot");
    for(Element socialModuleElement : socialModuleElements) {
      if(socialModuleElement.hasClass("visits") || socialModuleElement.hasClass("social")) {
        for(Element orderedListElement : socialModuleElement.getElementsByTag("ol")) {
          for(Element listItem : orderedListElement.children()) {
            for(Element listItemChild : listItem.children()) {
              if("a".equals(listItemChild.nodeName())) {
                ArticlesOverviewItem item = new ArticlesOverviewItem(this, listItemChild.attr("href"));
                item.setSubTitle(listItemChild.ownText());

                for(Element anchorChild : listItemChild.children()) {
                  if("strong".equals(anchorChild.nodeName()))
                    item.setTitle(anchorChild.text());
                }

                extractedItems.add(item);
              }
            }
          }
        }
      }
    }

    return extractedItems;
  }

}

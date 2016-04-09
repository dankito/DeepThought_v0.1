package net.deepthought.data.contentextractor;

import net.deepthought.data.contentextractor.preview.ArticlesOverviewItem;
import net.deepthought.data.contentextractor.preview.ArticlesOverviewListener;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.FileLink;
import net.deepthought.data.model.Reference;
import net.deepthought.data.model.ReferenceSubDivision;
import net.deepthought.util.DeepThoughtError;
import net.deepthought.util.localization.Localization;
import net.deepthought.util.StringUtils;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Created by ganymed on 20/06/15.
 */
public class DerFreitagContentExtractor extends OnlineNewspaperContentExtractorBase {

  private final static Logger log = LoggerFactory.getLogger(DerFreitagContentExtractor.class);

//  protected static final String LogoFileName = "heise_online_logo.png";


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
    return "Der Freitag";
  }

  @Override
  public String getSiteBaseUrl() {
    return "freitag.de";
  }

  @Override
  protected String getUrlPrefixForMakingRelativeLinkAbsolute(String relativeUrl) {
    return "https://www.freitag.de";
  }

  //  @Override
//  public String getIconUrl() {
//    return tryToLoadIconFile(LogoFileName);
//  }


  @Override
  public boolean canCreateEntryFromUrl(String url) {
    return url.toLowerCase().contains("www.freitag.de/");
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
    String abstractString = extractAbstract(articleElement);

    String content = extractContent(articleElement);

    return new Entry(content, abstractString);
  }

  protected String extractAbstract(Element articleElement) {
    String abstractString = "";
    Element descriptionElement = articleElement.select(".running-text .description").first();
    if(descriptionElement != null) {
      Elements children = descriptionElement.children();
      descriptionElement.children().remove();

      abstractString = descriptionElement.text();
      descriptionElement.children().addAll(children);
    }
    return abstractString;
  }

  protected String extractContent(Element articleElement) {
    String content = "";
    Element textElement = articleElement.select(".text").first();
    if(textElement != null) {
      adjustLinkUrls(textElement);
      adjustSourceElements(textElement);

      content = textElement.outerHtml();
    } return content;
  }

  protected String extractImageGallery(Element imageGalleryElement) {
    String content = "<div>";
    content+= imageGalleryElement.select("h2").outerHtml();

    content+= extractAllImagesOfGallery(imageGalleryElement);

    return content + "</div>";
  }

  protected String extractAllImagesOfGallery(Element imageGalleryElement) {
    return "";
  }

  protected ReferenceSubDivision createReference(EntryCreationResult creationResult, String articleUrl, Element articleElement) {
    String title = "";
    String subTitle = "";

    Element runningTextElement = articleElement.select(".running-text").first();
    if(runningTextElement != null) {
      Element titleHeaderElement = runningTextElement.select("h1").first();
      if(titleHeaderElement != null) {
        title = titleHeaderElement.text();
      }
    }

    // TODO: may use Catchwords like 'Reportage', 'Programmatik', ... as sub title
    Element descriptionElement = articleElement.select(".description").first();
    if(descriptionElement != null) {
      Element catchWordElement = descriptionElement.select(".catchword").first();
      if(catchWordElement != null) {
        subTitle = catchWordElement.text();
      }
    }

    // TODO: may use image from #main-image as Reference PreviewImage

    Date publishingDate = null;
    String issue = "";
    Element additionalInfoElement = articleElement.select("header .additional-info").first();
    if(additionalInfoElement != null) {
      publishingDate = parseDate(additionalInfoElement);
      issue = tryToGetIssue(articleElement);
    }

    ReferenceSubDivision articleReference = new ReferenceSubDivision(title, subTitle);
    articleReference.setOnlineAddressAndLastAccessToCurrentDateTime(articleUrl);

    setArticleReference(creationResult, articleReference, publishingDate, issue);

    tryToExtractPreviewImage(articleElement, articleReference);

    return articleReference;
  }

  protected void tryToExtractPreviewImage(Element articleElement, ReferenceSubDivision articleReference) {
    Element mainImageElement = articleElement.getElementById("main-image");
    if(mainImageElement != null) {
      Element imgElement = mainImageElement.select("img").first();
      if(imgElement != null) {
        FileLink previewImage = new FileLink(imgElement.attr("src"));

        tryToFindPreviewImageName(mainImageElement, imgElement, previewImage);

        articleReference.setPreviewImage(previewImage);
      }
    }
  }

  protected void tryToFindPreviewImageName(Element mainImageElement, Element imgElement, FileLink previewImage) {
    String caption = imgElement.attr("alt");

    Element captionElement = mainImageElement.select(".caption").first();
    if(captionElement != null) {
      caption += (StringUtils.isNotNullOrEmpty(caption) ? ": " : "") + captionElement.text();
    }

    if(StringUtils.isNotNullOrEmpty(caption)) {
      previewImage.setName(caption.trim());
    }
  }

  protected void setArticleReference(EntryCreationResult creationResult, ReferenceSubDivision articleReference, Date articleDate, String issue) {
    setArticleReference(creationResult, articleReference, formatDateToDeepThoughtDateString(articleDate));

    if(creationResult.isAReferenceSet()) {
      Reference reference = creationResult.getReference();

      if(StringUtils.isNotNullOrEmpty(issue)) {
        reference.setIssueOrPublishingDate(issue);
      }

      if(articleDate != null) {
        reference.setPublishingDate(articleDate);
      }
    }
  }

  protected String tryToGetIssue(Element articleElement) {
    Element newspaperIssueElement = articleElement.select(".newspaper-issue").first();
    if(newspaperIssueElement != null) {
      String issue = newspaperIssueElement.text();

      if (issue != null && issue.contains("Ausgabe ")) { // remove 'Ausgabe ' and insert '/'
        issue = issue.replace("Ausgabe ", "");
        if (issue.length() == 4) {
          issue = issue.substring(0, 2) + "/" + issue.substring(2);
        }
      }

      return issue;
    }

    return "";
  }


  protected DateFormat derFreitagDateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN);

  protected Date parseDate(Element additionalInfoElement) {
    Element dateElement = additionalInfoElement.select(".date").first();
    if(dateElement != null) {
      String publishingDateString = dateElement.text();

      if (StringUtils.isNotNullOrEmpty(publishingDateString)) {
        try {
          Date publishingDate = derFreitagDateFormat.parse(publishingDateString);
          return publishingDate;
        } catch (Exception ex) {
          log.error("Could not parse Der Freitag DateTime Format " + publishingDateString, ex);
        }
      }
    }

    return null;
  }


  protected void addTags(Element bodyElement, EntryCreationResult creationResult) {
    addNewspaperTag(creationResult);

    Element tagsElement = bodyElement.select("#article-keywords").first();
    // TODO: may extract Article Tags
  }


  @Override
  public boolean hasArticlesOverview() {
    return true;
  }

  @Override
  protected void getArticlesOverview(ArticlesOverviewListener listener) {
    extractArticlesOverviewFromFrontPage(listener);
  }


  protected void extractArticlesOverviewFromFrontPage(ArticlesOverviewListener listener) {
    try {
      Document frontPage = retrieveOnlineDocument("http://www.freitag.de");
      extractArticlesOverviewItemsFromFrontPage(frontPage, listener);
    } catch(Exception ex) {
      log.error("Could not retrieve HTML code of freitag.de front page", ex);
    }
  }

  protected void extractArticlesOverviewItemsFromFrontPage(Document frontPage, ArticlesOverviewListener listener) {
    List<ArticlesOverviewItem> overviewItems = new ArrayList<>();
    Set<String> extractedArticleUrls = new HashSet<>();

    extractProductTeasersItems(frontPage, overviewItems, extractedArticleUrls);
    listener.overviewItemsRetrieved(this, overviewItems, false);

    extractClusterArticleItems(frontPage, overviewItems, extractedArticleUrls);
    listener.overviewItemsRetrieved(this, overviewItems, false);

    extractLinkCycleArticles(frontPage, overviewItems, extractedArticleUrls);
    listener.overviewItemsRetrieved(this, overviewItems, true);
  }


  protected void extractProductTeasersItems(Document frontPage, List<ArticlesOverviewItem> overviewItems, Set<String> extractedArticleUrls) {
    Elements productTeaserElements = frontPage.body().select("aside#product-teasers .product");
    for(Element productTeaserElement : productTeaserElements) {
      extractProductTeaserOverviewItem(productTeaserElement, overviewItems, extractedArticleUrls);
    }
  }

  protected void extractProductTeaserOverviewItem(Element productTeaserElement, List<ArticlesOverviewItem> overviewItems, Set<String> extractedArticleUrls) {
    Element headerAnchor = productTeaserElement.select("header a").first();
    if(headerAnchor != null) {
      String articleUrl = headerAnchor.attr("href");
      if(extractedArticleUrls.contains(articleUrl)) {
        return;
      }
      extractedArticleUrls.add(articleUrl);

      ArticlesOverviewItem item = new ArticlesOverviewItem(this, articleUrl);
      item.setLabel(headerAnchor.text());
      overviewItems.add(item);

      extractProductTeaserTitleAndSummary(productTeaserElement, item);
    }
  }

  protected void extractProductTeaserTitleAndSummary(Element productTeaserElement, ArticlesOverviewItem item) {
    Element innerDiv = productTeaserElement.select("div.inner").first();
    if(innerDiv != null) {
      Element imgElement = innerDiv.select("div.image img").first();
      if(imgElement != null) {
        item.setPreviewImageUrl(imgElement.attr("src"));
        item.setTitle(imgElement.attr("title"));
      }

      Element artistElement = innerDiv.select("div.artist span").first();
      if(artistElement != null) {
        item.setSubTitle(artistElement.text());
      }

      Element descriptionElement = innerDiv.select("div.description a").first();
      if(descriptionElement != null) {
        item.setSummary(descriptionElement.text());
      }
    }
  }


  protected void extractClusterArticleItems(Document frontPage, List<ArticlesOverviewItem> overviewItems, Set<String> extractedArticleUrls) {
    Elements clusterElements = frontPage.body().select(".cluster, .additional-links");

    for(Element clusterElement : clusterElements) {
      extractOverviewItemsFromCluster(overviewItems, clusterElement, extractedArticleUrls);
    }
  }

  protected void extractOverviewItemsFromCluster(List<ArticlesOverviewItem> overviewItems, Element clusterElement, Set<String> extractedArticleUrls) {
    String category = clusterElement.attr("id");
    if(category != null) {
      category = category.replace("cluster-prefix-", "");
      if(category.contains("-")) {
        category = category.substring(0, category.indexOf('-'));
      }
    }

    Element clusterMainArticle = clusterElement.select("article").first();
    if(clusterMainArticle != null) {
      extractOverviewItemFromArticleElement(overviewItems, clusterMainArticle, extractedArticleUrls);
    }

    Elements additionalLinksElements = clusterElement.select(".additional-links article");
    for(Element additionalLinkClusterElement : additionalLinksElements) {
      extractOverviewItemFromArticleElement(overviewItems, additionalLinkClusterElement, extractedArticleUrls);
    }
  }


  protected void extractLinkCycleArticles(Document frontPage, List<ArticlesOverviewItem> overviewItems, Set<String> extractedArticleUrls) {
    Elements linkCycleElements = frontPage.body().select(".linkcycle");
    for(Element listingElement : linkCycleElements) {
      String category = null;
      Element headerElement = listingElement.select("header").first();
      if(headerElement != null) {
        category = headerElement.text();
      }

      for(Element linkCycleArticle : listingElement.select("article")) {
        extractOverviewItemFromArticleElement(overviewItems, linkCycleArticle, extractedArticleUrls);
      }
    }
  }


  protected void extractOverviewItemFromArticleElement(List<ArticlesOverviewItem> overviewItems, Element articleElement, Set<String> extractedArticleUrls) {
    Element anchorElement = articleElement.select("h2 a").first();
    if(anchorElement != null) {
      String articleUrl = anchorElement.attr("href");
      if(extractedArticleUrls.contains(articleUrl)) {
        return;
      }
      extractedArticleUrls.add(articleUrl);
      
      ArticlesOverviewItem item = new ArticlesOverviewItem(this, articleUrl);
      overviewItems.add(item);

      TextAndCatchWord title = tryToExtractTextAndCatchWordFromElement(anchorElement);
      item.setTitle(title.getText());
      if(title.isCatchWordSet()) {
        item.setSubTitle(title.getCatchWord());
      }

      mayExtractImageAndSummary(articleElement, item);
    }
  }

  protected void mayExtractImageAndSummary(Element articleElement, ArticlesOverviewItem item) {
    Element imageBox = articleElement.select("div.imagebox").first();
    if(imageBox != null) {
      Element imgElement = imageBox.select("div.image img").first();
      if(imgElement != null) {
        item.setPreviewImageUrl(imgElement.attr("src"));
      }

      Element descriptionElement = imageBox.select("div.box a.description").first();
      if(descriptionElement != null) {
        TextAndCatchWord summary = tryToExtractTextAndCatchWordFromElement(descriptionElement);
        item.setSummary(summary.getText());

        if(summary.isCatchWordSet()) {
          item.setSubTitle(summary.getCatchWord());
        }
      }
    }
  }


  protected TextAndCatchWord tryToExtractTextAndCatchWordFromElement(Element element) {
    Element spanChildElement = element.select("span.catchword").first();
    if(spanChildElement != null) { // an Element with a <span> child containing Catchword
      spanChildElement.remove();
      return new TextAndCatchWord(element.text(), spanChildElement.text());
    }

    return new TextAndCatchWord(element.text());
  }

  protected class TextAndCatchWord {
    public String text;
    public String catchWord;

    public TextAndCatchWord(String text) {
      this.text = text;
    }

    public TextAndCatchWord(String text, String catchWord) {
      this.text = text;
      this.catchWord = catchWord;
    }


    public String getText() {
      return text;
    }

    public void setText(String text) {
      this.text = text;
    }

    public boolean isCatchWordSet() {
      return catchWord != null;
    }

    public String getCatchWord() {
      return catchWord;
    }

    public void setCatchWord(String catchWord) {
      this.catchWord = catchWord;
    }


    @Override
    public String toString() {
      String description = text;
      if(isCatchWordSet()) {
        description += " (" + catchWord + ")";
      }
      return description;
    }
  }

}

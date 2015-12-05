package net.deepthought.data.contentextractor;

import net.deepthought.data.contentextractor.preview.ArticlesOverviewListener;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.Reference;
import net.deepthought.util.Localization;
import net.deepthought.util.StringUtils;
import net.deepthought.util.file.FileUtils;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * <p>Simply downloads a Web Page's HTML code.</p>
 * <p>
 *   No Images, Videos, Scripts, CSS etc. are downloaded.<br />
 *   In order to do that see for example https://github.com/JonasCz/save-for-offline/blob/master/app/src/main/java/jonas/tool/saveForOffline/PageSaver.java or
 *   http://java-source.net/open-source/crawlers
 * </p>
 * Created by ganymed on 05/12/15.
 */
public class BasicWebPageContentExtractor extends OnlineArticleContentExtractorBase implements IOnlineArticleContentExtractor {

  protected String lastPositivelyCheckedUrl = null;

  protected String baseUrlOfLastPositivelyCheckedUrl = null;

  protected String iconUrlOfLastPositivelyCheckedUrl = null;

  protected void setLastPositivelyCheckedUrl(String url) {
    this.lastPositivelyCheckedUrl = url;

    setBaseUrlForLastPositivelyCheckedUrl(lastPositivelyCheckedUrl);

    setIconUrlForLastPositivelyCheckedUrl(baseUrlOfLastPositivelyCheckedUrl);
  }

  protected void setBaseUrlForLastPositivelyCheckedUrl(String lastPositivelyCheckedUrl) {
    if(StringUtils.getNumberOfOccurrences("/", lastPositivelyCheckedUrl) > 2) {
      int indexOfFirstSlash = lastPositivelyCheckedUrl.indexOf('/');
      int indexOfThirdSlash = lastPositivelyCheckedUrl.indexOf('/', indexOfFirstSlash + 2);
      this.baseUrlOfLastPositivelyCheckedUrl = lastPositivelyCheckedUrl.substring(0, indexOfThirdSlash);
    }
    else {
      this.baseUrlOfLastPositivelyCheckedUrl = lastPositivelyCheckedUrl;
    }
  }

  public void setIconUrlForLastPositivelyCheckedUrl(String baseUrlOfLastPositivelyCheckedUrl) {
    this.iconUrlOfLastPositivelyCheckedUrl = FileUtils.contactPathElements(baseUrlOfLastPositivelyCheckedUrl, "favicon.ico");
  }

  @Override
  public int getSupportedPluginSystemVersion() {
    return 1;
  }

  @Override
  public String getName() {
    return Localization.getLocalizedString("web.page.content.extractor");
  }

  @Override
  public String getPluginVersion() {
    return "0.1";
  }


  @Override
  public String getSiteBaseUrl() {
    return baseUrlOfLastPositivelyCheckedUrl;
  }

  @Override
  public String getIconUrl() {
    return iconUrlOfLastPositivelyCheckedUrl;
  }

  @Override
  public boolean hasArticlesOverview() {
    return false;
  }

  @Override
  public void getArticlesOverviewAsync(ArticlesOverviewListener listener) {

  }


  @Override
  public boolean canCreateEntryFromUrl(String url) {
    boolean isOnlineWebPage = FileUtils.isOnlineWebPage(url);

    if(isOnlineWebPage) {
      setLastPositivelyCheckedUrl(url);
    }

    return isOnlineWebPage;
  }


  @Override
  protected EntryCreationResult parseHtmlToEntry(String articleUrl, Document document) {
    Entry entry = new Entry(makeRelativeUrlsAbsolute(document).outerHtml());

    Reference reference = new Reference(document.title());
    reference.setOnlineAddress(articleUrl);
    reference.setLastAccessDate(reference.getCreatedOn());

    EntryCreationResult result = new EntryCreationResult(articleUrl, entry);
    result.setReference(reference);
    return result;
  }

  protected Document makeRelativeUrlsAbsolute(Document document) {
    makeAttributeValuesAbsolute("src", document);
    makeAttributeValuesAbsolute("href", document);

    makeRelativeStyleImportsAbsolute(document);

    return document;
  }

  protected void makeAttributeValuesAbsolute(String attributeName, Document document) {
    for(Element element : document.select("[" + attributeName + "]")) {
      String value = element.attr(attributeName);
      if(FileUtils.isRelativeUrl(value)) {
        element.attr(attributeName, makeUrlAbsolute(value));
      }
    }
  }

  private String makeUrlAbsolute(String url) {
    String absoluteUrl = url;

    if(absoluteUrl.startsWith("//")) {
      absoluteUrl = "http:" + absoluteUrl;
    }
    else {
      absoluteUrl = FileUtils.contactPathElements(getSiteBaseUrl(), absoluteUrl);
    }

    return absoluteUrl;
  }

  protected void makeRelativeStyleImportsAbsolute(Document document) {
    for(Element element : document.select("style")) {
      String html = element.html();
      if(StringUtils.isNotNullOrEmpty(html) && html.startsWith("@import url(")) {
        String styleSheetUrl = html.substring("@import url(".length());
        styleSheetUrl = styleSheetUrl.substring(0, styleSheetUrl.indexOf(")"));

        if(FileUtils.isRelativeUrl(styleSheetUrl)) {
          html = html.replace(styleSheetUrl, FileUtils.contactPathElements(getSiteBaseUrl(), styleSheetUrl));
          element.html(html);
        }
      }
    }
  }

}

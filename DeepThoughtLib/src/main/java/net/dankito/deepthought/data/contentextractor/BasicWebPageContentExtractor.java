package net.dankito.deepthought.data.contentextractor;

import net.dankito.deepthought.data.html.WebPageExtractionResult;
import net.dankito.deepthought.data.model.Entry;
import net.dankito.deepthought.data.contentextractor.preview.ArticlesOverviewListener;
import net.dankito.deepthought.data.html.IHtmlHelper;
import net.dankito.deepthought.data.model.Reference;
import net.dankito.deepthought.util.DeepThoughtError;
import net.dankito.deepthought.util.OsHelper;
import net.dankito.deepthought.util.localization.Localization;
import net.dankito.deepthought.util.StringUtils;
import net.dankito.deepthought.util.file.FileUtils;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

  private static final Logger log = LoggerFactory.getLogger(BasicWebPageContentExtractor.class);


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


  public BasicWebPageContentExtractor() {

  }

  public BasicWebPageContentExtractor(IHtmlHelper htmlHelper) {
    super(htmlHelper);
  }


  public String getName() {
    return Localization.getLocalizedString("web.page.content.extractor");
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
  public void createEntryFromUrl(String url, CreateEntryListener listener) {
    if(OsHelper.isRunningOnAndroid() == false) { // TODO: also implement Boilerpipe for Android
      if (listener != null) {
        listener.entryCreated(createEntryCreationResultFromPageHtmlTryToRemoveClutter(url, htmlHelper));
      }
    }
    else {
      super.createEntryFromUrl(url, listener);
    }
  }


  public ContentExtractOptions createExtractOptionsForUrl(String url) {
    ContentExtractOptions options = new ContentExtractOptions(url, getSiteBaseUrl());

    options.addContentExtractOption(new ContentExtractOption(this, url, true, "content.extractor.extract.whole.web.page", new ExtractContentAction() {
      @Override
      public void runExtraction(ContentExtractOption option, ExtractContentActionResultListener listener) {
        setWebPageAsEntryContent(option, listener);
      }
    }));

    if(htmlHelper.canRemoveClutterFromHtml()) {
      options.addContentExtractOption(new ContentExtractOption(this, url, true, "content.extractor.try.to.extract.important.web.page.parts", new ExtractContentAction() {
        @Override
        public void runExtraction(ContentExtractOption option, ExtractContentActionResultListener listener) {
          setWebPageAsEntryContentTryToRemoveClutter(option, htmlHelper, listener);
        }
      }));
    }

    if(htmlHelper.canExtractPlainText()) {
      options.addContentExtractOption(new ContentExtractOption(this, url, true, "content.extractor.extract.plain.text.only", new ExtractContentAction() {
        @Override
        public void runExtraction(ContentExtractOption option, ExtractContentActionResultListener listener) {
          setWebPagePlainTextAsEntryContent(option, htmlHelper, listener);
        }
      }));
    }

    return options;
  }

  protected void setWebPageAsEntryContent(ContentExtractOption option, ExtractContentActionResultListener listener) {
    EntryCreationResult result = createEntryCreationResultFromPageHtml(option.getUrl());
    dispatchResult(result, listener);
  }

  protected EntryCreationResult createEntryCreationResultFromPageHtml(String webPageUrl) {
    try {
      Document document = retrieveOnlineDocument(webPageUrl);
      return parseHtmlToEntry(webPageUrl, document);
    } catch(Exception ex) {
      log.error("Could not retrieve WebPage's HTML Code for Url " + webPageUrl, ex);
      return new EntryCreationResult(webPageUrl, new DeepThoughtError(Localization.getLocalizedString("could.not.retrieve.articles.html.code", webPageUrl), ex));
    }
  }

  protected void setWebPageAsEntryContentTryToRemoveClutter(ContentExtractOption option, IHtmlHelper htmlHelper, ExtractContentActionResultListener listener) {
    EntryCreationResult result = createEntryCreationResultFromPageHtmlTryToRemoveClutter(option.getUrl(), htmlHelper);
    dispatchResult(result, listener);
  }

  protected EntryCreationResult createEntryCreationResultFromPageHtmlTryToRemoveClutter(String webPageUrl, IHtmlHelper htmlHelper) {
    try {
      WebPageExtractionResult extractionResult = htmlHelper.tryToRemoveClutter(webPageUrl);

      return createEntryCreationResultFromWebPageExtractionResult(webPageUrl, extractionResult);
    } catch(Exception ex) {
      log.error("Could not retrieve WebPage's HTML Code for Url " + webPageUrl, ex);
      return new EntryCreationResult(webPageUrl, new DeepThoughtError(Localization.getLocalizedString("could.not.retrieve.articles.html.code", webPageUrl), ex));
    }
  }

  private EntryCreationResult createEntryCreationResultFromWebPageExtractionResult(String webPageUrl, WebPageExtractionResult extractionResult) {
    String content = extractionResult.getExtractedText();
    Entry entry = new Entry(content);
    EntryCreationResult result = new EntryCreationResult(webPageUrl, entry);

    result.setReference(createReferenceForUrl(webPageUrl, extractionResult.getWebPageTitle()));

    return result;
  }

  protected void setWebPagePlainTextAsEntryContent(ContentExtractOption option, IHtmlHelper htmlHelper, ExtractContentActionResultListener listener) {
    EntryCreationResult result = createEntryCreationResultFromPagePlainText(option.getUrl(), htmlHelper);
    dispatchResult(result, listener);
  }

  protected EntryCreationResult createEntryCreationResultFromPagePlainText(String webPageUrl, IHtmlHelper htmlHelper) {
    try {
      WebPageExtractionResult extractionResult = htmlHelper.extractPlainText(webPageUrl);

      return createEntryCreationResultFromWebPageExtractionResult(webPageUrl, extractionResult);
    } catch(Exception ex) {
      log.error("Could not retrieve WebPage's HTML Code for Url " + webPageUrl, ex);
      return new EntryCreationResult(webPageUrl, new DeepThoughtError(Localization.getLocalizedString("could.not.retrieve.articles.html.code", webPageUrl), ex));
    }
  }

  protected void dispatchResult(EntryCreationResult result, ExtractContentActionResultListener listener) {
    if(listener != null) {
      listener.extractingContentDone(result);
    }
  }


  @Override
  protected EntryCreationResult parseHtmlToEntry(String articleUrl, Document document) {
    Entry entry = new Entry(makeRelativeUrlsAbsolute(document).outerHtml());

    Reference reference = createReferenceForUrl(articleUrl, document.title());

    EntryCreationResult result = new EntryCreationResult(articleUrl, entry);
    result.setReference(reference);
    return result;
  }

  protected Reference createReferenceForUrl(String articleUrl) {
    return createReferenceForUrl(articleUrl, articleUrl);
  }

  protected Reference createReferenceForUrl(String articleUrl, String title) {
    Reference reference = new Reference(title);
    reference.setOnlineAddress(articleUrl);
    reference.setLastAccessDate(reference.getCreatedOn());
    return reference;
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

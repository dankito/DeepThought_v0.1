package net.dankito.deepthought.data.contentextractor;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.data.html.IHtmlHelper;
import net.dankito.deepthought.util.DeepThoughtError;
import net.dankito.deepthought.util.localization.Localization;
import net.dankito.deepthought.util.OsHelper;

import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.Map;

/**
 * Created by ganymed on 20/06/15.
 */
public abstract class OnlineArticleContentExtractorBase implements IOnlineArticleContentExtractor {

  private final static Logger log = LoggerFactory.getLogger(OnlineArticleContentExtractorBase.class);


  static {
    if(OsHelper.isRunningOnJavaSeOrOnAndroidApiLevelAtLeastOf(9))
      CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_NONE)); // maybe it helps so that Sueddeutsche cookies don't get set
  }


  protected IHtmlHelper htmlHelper = null;


  public OnlineArticleContentExtractorBase() {
    this(Application.getHtmlHelper());
  }

  public OnlineArticleContentExtractorBase(IHtmlHelper htmlHelper) {
    this.htmlHelper = htmlHelper;
  }


  @Override
  public void createEntryFromUrlAsync(final String url, final CreateEntryListener listener) {
    if(canCreateEntryFromUrl(url) == false) {
      if(listener != null)
        listener.entryCreated(new EntryCreationResult(url, new DeepThoughtError(Localization.getLocalizedString("can.not.create.entry.from.url"))));
    }
    else {
      Application.getThreadPool().runTaskAsync(new Runnable() {
        @Override
        public void run() {
          createEntryFromUrl(url, listener);
        }
      });
    }
  }

  public void createEntryFromUrl(String url, CreateEntryListener listener) {
    if(listener != null)
      listener.entryCreated(createEntryFromArticle(url));
  }


  public EntryCreationResult createEntryFromArticle(String articleUrl) {
    try {
      Document document = retrieveOnlineDocument(articleUrl);
      return parseHtmlToEntry(articleUrl, document);
    } catch(Exception ex) {
      log.error("Could not retrieve Article's HTML Code from Url " + articleUrl, ex);
      return new EntryCreationResult(articleUrl, new DeepThoughtError(Localization.getLocalizedString("could.not.retrieve.articles.html.code", articleUrl), ex));
    }
  }

  protected Document retrieveOnlineDocument(String articleUrl) throws IOException {
    return htmlHelper.retrieveOnlineDocument(articleUrl);
  }

  protected Document retrieveOnlineDocument(String articleUrl, String userAgent, Map<String, String> data, Connection.Method method) throws IOException {
    return htmlHelper.retrieveOnlineDocument(articleUrl, userAgent, data, method);
  }

  protected abstract EntryCreationResult parseHtmlToEntry(String articleUrl, Document document);


  /**
   * May be overwritten in sub classes.
   * Should return the URL prefix (like http://<domain>) to make a relative URL absolute
   */
  protected String getUrlPrefixForMakingRelativeLinkAbsolute(String relativeUrl) {
    return "";
  }


  protected void adjustLinkUrls(Element articleElement) {
    for(Element elementWithSrcAttribute : articleElement.select("[src]")) {
      String src = elementWithSrcAttribute.attr("src");
      src = makeLinkAbsolute(src);
      elementWithSrcAttribute.attr("src", src);
    }

    for(Element elementWithHrefAttribute : articleElement.select("[href]")) {
      String href = elementWithHrefAttribute.attr("href");
      href = makeLinkAbsolute(href);
      elementWithHrefAttribute.attr("href", href);
    }

    for(Element elementWithHrefAttribute : articleElement.select("[data-zoom-src]")) {
      String href = elementWithHrefAttribute.attr("data-zoom-src");
      href = makeLinkAbsolute(href);
      elementWithHrefAttribute.attr("data-zoom-src", href);
    }
  }

  protected void adjustSourceElements(Element articleElement) {
    for(Element sourceElement : articleElement.select("span.source")) {
      sourceElement.parent().appendChild(new Element(org.jsoup.parser.Tag.valueOf("br"), articleElement.baseUri()));
    }
  }

  /**
   * Override {@link #getUrlPrefixForMakingRelativeLinkAbsolute(String)} so that method gets the URL prefix for making link absolute.
   */
  protected String makeLinkAbsolute(String link) {
    return makeLinkAbsolute(link, getUrlPrefixForMakingRelativeLinkAbsolute(link));
  }

  protected String makeLinkAbsolute(String link, String baseUrl) {
    if(link.startsWith("//")) {
      return "http:" + link;
    }
    else if(link.startsWith("/")) {
      return baseUrl + link;
    }

    return link;
  }

}

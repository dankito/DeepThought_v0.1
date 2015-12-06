package net.deepthought.data.contentextractor;

import net.deepthought.Application;
import net.deepthought.util.DeepThoughtError;
import net.deepthought.util.Localization;
import net.deepthought.util.OsHelper;

import org.jsoup.Connection;
import org.jsoup.nodes.Document;
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

  @Override
  public ContentExtractOption canCreateEntryFromClipboardContent(ClipboardContent clipboardContent) {
    if(clipboardContent.hasUrl()) {
      if(canCreateEntryFromUrl(clipboardContent.getUrl()))
        return new ContentExtractOption(this, clipboardContent.getUrl(), true);
    }
    else if(clipboardContent.hasString()) {
      if(canCreateEntryFromUrl(clipboardContent.getString()))
        return new ContentExtractOption(this, clipboardContent.getString(), true);
    }

    return ContentExtractOption.CanNotExtractContent;
  }

  @Override
  public void createEntryFromClipboardContentAsync(ContentExtractOption contentExtractOption, CreateEntryListener listener) {
    if(contentExtractOption.isUrl())
      createEntryFromUrlAsync(contentExtractOption.getUrl(), listener);
    else if(listener != null)
      listener.entryCreated(new EntryCreationResult(contentExtractOption, new DeepThoughtError(Localization.getLocalizedString("can.not.create.entry.from.clipboard.content"))));

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
    return Application.getHtmlHelper().retrieveOnlineDocument(articleUrl);
  }

  protected Document retrieveOnlineDocument(String articleUrl, String userAgent, Map<String, String> data, Connection.Method method) throws IOException {
    return Application.getHtmlHelper().retrieveOnlineDocument(articleUrl, userAgent, data, method);
  }

  protected abstract EntryCreationResult parseHtmlToEntry(String articleUrl, Document document);

}

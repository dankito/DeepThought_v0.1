package net.deepthought.data.contentextractor;

import net.deepthought.util.DeepThoughtError;
import net.deepthought.util.Localization;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ganymed on 20/06/15.
 */
public abstract class OnlineArticleContentExtractorBase implements IOnlineArticleContentExtractor {

  public final static String DefaultUserAgent = "Mozilla/5.0 (Windows NT 6.3; rv:36.0) Gecko/20100101 Firefox/36.0";


  private final static Logger log = LoggerFactory.getLogger(OnlineArticleContentExtractorBase.class);



  @Override
  public void createEntryFromUrlAsync(final String url, final CreateEntryListener listener) {
    if(canCreateEntryFromUrl(url) == false) {
      if(listener != null)
        listener.entryCreated(new EntryCreationResult(url, new DeepThoughtError(Localization.getLocalizedStringForResourceKey("can.not.create.entry.from.url"))));
    }
    else {
      new Thread(new Runnable() {
        @Override
        public void run() {
          createEntryFromUrl(url, listener);
        }
      }).start();
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
      listener.entryCreated(new EntryCreationResult(contentExtractOption, new DeepThoughtError(Localization.getLocalizedStringForResourceKey("can.not.create.entry.from.clipboard.content"))));

  }


  public EntryCreationResult createEntryFromArticle(String articleUrl) {
    try {
      Document document = retrieveOnlineDocument(articleUrl);
      return parseHtmlToEntry(articleUrl, document);
    } catch(Exception ex) {
      log.error("Could not retrieve Article's HTML Code from Url " + articleUrl, ex);
      return new EntryCreationResult(articleUrl, new DeepThoughtError(Localization.getLocalizedStringForResourceKey("could.not.retrieve.articles.html.code", articleUrl), ex));
    }
  }

  protected Document retrieveOnlineDocument(String articleUrl) throws IOException {
    return retrieveOnlineDocument(articleUrl, DefaultUserAgent, new HashMap<String, String>(), Connection.Method.GET);
  }

  protected Document retrieveOnlineDocument(String articleUrl, String userAgent, Map<String, String> data, Connection.Method method) throws IOException {
    Connection connection = Jsoup.connect(articleUrl);
    connection.header("user-agent", userAgent);
    connection.data(data);
    connection.method(method);
    Connection.Response response = connection.execute();

    removeAllCookies(response);

    if(articleUrl.equals(response.url().toString()) == false)
      return retrieveOnlineDocument(response.url().toString(), userAgent, data, method);
    return response.parse();
  }

  protected void removeAllCookies(Connection.Response response) {
    List<String> cookieNames = new ArrayList<>(response.cookies().keySet()); // make a copy of, otherwise a ConcurrentModificationException will be thrown
    for(String name : cookieNames)
      response.removeCookie(name);
  }

  protected abstract EntryCreationResult parseHtmlToEntry(String articleUrl, Document document);

}

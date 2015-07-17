package net.deepthought.data.contentextractor;

import net.deepthought.util.DeepThoughtError;
import net.deepthought.util.Localization;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ganymed on 20/06/15.
 */
public abstract class OnlineArticleContentExtractorBase implements IOnlineArticleContentExtractor {

  public final static String DefaultUserAgent = "Mozilla/5.0 (Windows NT 6.3; rv:36.0) Gecko/20100101 Firefox/36.0";


  private final static Logger log = LoggerFactory.getLogger(OnlineArticleContentExtractorBase.class);


  static {
    CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_NONE)); // maybe it helps so that Sueddeutsche cookies don't get set
  }


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
    CookieStore cookieStore = new BasicCookieStore();
    HttpClient httpclient = createHttpClient(userAgent, cookieStore);

    HttpRequestBase request = createHttpRequest(articleUrl, data, method);

    HttpResponse response = httpclient.execute(request);
    HttpEntity entity = response.getEntity();
    log.debug("Request Handled for url " + articleUrl + " ?: " + response.getStatusLine());
    //InputStream in = entity.getContent();
    String html = EntityUtils.toString(entity);
    httpclient.getConnectionManager().shutdown();
    cookieStore.clear();

    return Jsoup.parse(html, articleUrl);

//    String originalProxyHost = System.getProperty("http.proxyHost");
//    String originalProxyPort = System.getProperty("http.proxyPort");
//    System.setProperty("http.proxyHost", "127.0.0.1");  //set proxy host
//    System.setProperty("http.proxyPort", "8889");  //set proxy port
//
//    Connection connection = Jsoup.connect(articleUrl);
//    connection.header("user-agent", userAgent);
//    connection.data(data);
//    connection.method(method);
//    connection.cookies(new HashMap<String, String>()); // maybe that helps to avoid that Jsoup sends cookies along
////    CookieHandler cookieHandler = java.net.CookieManager.getDefault();
////    cookieHandler.get()
//
//    Connection.Response response = connection.execute();
//    System.setProperty("http.proxyHost", originalProxyHost == null ? "" : originalProxyHost);  // unset proxy host
//    System.setProperty("http.proxyPort", originalProxyPort == null ? "" : originalProxyPort);  // unset proxy port
//
//    if(articleUrl.equals(response.url().toString()) == false)
//      return retrieveOnlineDocument(response.url().toString(), userAgent, data, method);
//    return response.parse();
  }

  protected HttpClient createHttpClient(String userAgent, CookieStore cookieStore) {
    DefaultHttpClient httpclient = new DefaultHttpClient();
    httpclient.getParams().setParameter(CoreProtocolPNames.USER_AGENT, userAgent);
    httpclient.setCookieStore(cookieStore);

//    HttpHost proxy = new HttpHost("127.0.0.1", 8889);
//    httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);

    return httpclient;
  }

  protected HttpRequestBase createHttpRequest(String articleUrl, Map<String, String> data, Connection.Method method) {
    HttpRequestBase request = null;

    if(method == Connection.Method.GET)
      request = new HttpGet(articleUrl);
    else if(method == Connection.Method.POST) {
      request = new HttpPost(articleUrl);
      ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
      for(String key : data.keySet())
        nameValuePairs.add(new BasicNameValuePair(key, data.get(key)));

      try {
        ((HttpPost) request).setEntity(new UrlEncodedFormEntity(nameValuePairs));
      } catch(Exception ex) {
        log.error("Could not set HttpPost's Post Body", ex);
      }
    }

    return request;
  }

  protected abstract EntryCreationResult parseHtmlToEntry(String articleUrl, Document document);

}

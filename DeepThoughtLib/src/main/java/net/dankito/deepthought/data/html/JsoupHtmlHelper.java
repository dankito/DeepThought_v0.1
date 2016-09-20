package net.dankito.deepthought.data.html;

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
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ganymed on 07/04/15.
 */
public class JsoupHtmlHelper implements IHtmlHelper {

  public final static String DefaultUserAgent = "Mozilla/5.0 (Windows NT 6.3; rv:36.0) Gecko/20100101 Firefox/36.0";


  private final static Logger log = LoggerFactory.getLogger(JsoupHtmlHelper.class);


  @Override
  public boolean canExtractPlainText() {
    return true;
  }

  @Override
  public boolean canRemoveClutterFromHtml() {
    return false;
  }


  @Override
  public Document retrieveOnlineDocument(String webPageUrl) throws IOException {
    return retrieveOnlineDocument(webPageUrl, DefaultUserAgent, new HashMap<String, String>(), Connection.Method.GET);
  }

  @Override
  public Document retrieveOnlineDocument(String webPageUrl, String userAgent, Map<String, String> data, Connection.Method method) throws IOException {
    CookieStore cookieStore = new BasicCookieStore();
    HttpClient httpclient = createHttpClient(userAgent, cookieStore);

    HttpRequestBase request = createHttpRequest(webPageUrl, data, method);

    HttpResponse response = httpclient.execute(request);
    HttpEntity entity = response.getEntity();
    log.debug("Request Handled for url " + webPageUrl + " ?: " + response.getStatusLine());

    String html = EntityUtils.toString(entity);
    httpclient.getConnectionManager().shutdown();
    cookieStore.clear();

    return Jsoup.parse(html, webPageUrl);
  }

  protected HttpClient createHttpClient(String userAgent, CookieStore cookieStore) {
    DefaultHttpClient httpclient = new DefaultHttpClient();
    httpclient.getParams().setParameter(CoreProtocolPNames.USER_AGENT, userAgent);
    httpclient.setCookieStore(cookieStore);

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


  public String getWebsiteTitle(String webPageUrl) throws Exception {
    return retrieveOnlineDocument(webPageUrl).title();
  }


  @Override
  public String extractPlainTextFromHtmlBody(String html) {
    try {
      Document doc = Jsoup.parseBodyFragment(html);
      // Trick for better formatting
      doc.body().wrap("<pre></pre>");

      String text = doc.text();
      // Converting nbsp entities
      text = text.replaceAll("\u00A0", " ");

      return text;
    } catch(Exception ex) {
      log.error("Could not parse html " + html, ex);
    }

    return html;
  }

  @Override
  public List<ImageElementData> extractAllImageElementsFromHtml(String html) {
    List<ImageElementData> imgElementsData = new ArrayList<>();

    Document doc = Jsoup.parseBodyFragment(html);
    Elements imgElements = doc.body().getElementsByTag("img");

    for(Element imgElement : imgElements) {
      imgElementsData.add(parseImgElement(html, imgElement));
    }

    return imgElementsData;
  }

  protected ImageElementData parseImgElement(String html, Element imgElement) {
    ImageElementData elementData = new ImageElementData();

    if(imgElement.hasAttr(ImageElementData.SourceAttributeName))
      elementData.setSource(imgElement.attr(ImageElementData.SourceAttributeName));
    if(imgElement.hasAttr(ImageElementData.AltAttributeName))
      elementData.setAlt(imgElement.attr(ImageElementData.AltAttributeName));

    if(imgElement.hasAttr(ImageElementData.ImageIdAttributeName))
      elementData.setFileId(imgElement.attr(ImageElementData.ImageIdAttributeName));
    if(imgElement.hasAttr(ImageElementData.EmbeddingIdAttributeName))
      elementData.setEmbeddingId(Long.parseLong(imgElement.attr(ImageElementData.EmbeddingIdAttributeName)));

    if(imgElement.hasAttr(ImageElementData.WidthAttributeName))
      elementData.setWidth(parseImageMetricToInt(imgElement, ImageElementData.WidthAttributeName));
    if(imgElement.hasAttr(ImageElementData.HeightAttributeName))
      elementData.setHeight(parseImageMetricToInt(imgElement, ImageElementData.HeightAttributeName));
    tryToExtractOriginalImgElementHtmlCode(html, imgElement, elementData);


    return elementData;
  }

  protected int parseImageMetricToInt(Element imgElement, String attributeName) {
    // TODO: what to do with percentage values, e.g. 100% ?
    String attributeValue = imgElement.attr(attributeName);
    try {
      return Integer.parseInt(attributeValue);
    } catch(Exception e) { log.error("Could not parse attributeValue " + attributeValue + " to an Integer", e); }

    return 0;
  }

  protected void tryToExtractOriginalImgElementHtmlCode(String html, Element imgElement, ImageElementData elementData) {
    // there is a bug in JSoup: calling imgElement.outerHtml() returns not real Element's Html. In this case <img> element ends with ' />', but outerHtml() returns '>'
    String outerHtml = imgElement.outerHtml();
    int startIndex = html.indexOf(outerHtml.substring(0, outerHtml.length() - 4));
    String debug = outerHtml.substring(0, outerHtml.length() - 4);

    // TODO: sometimes the substring for startIndex cannot be found (even on - in my eyes - two identical <img> elements) -> find reason
    if(startIndex >= 0) {
      int endIndex = html.indexOf(">", startIndex + 1) + 1;

      String elementHtml = html.substring(startIndex, endIndex);
      elementData.setOriginalImgElementHtmlCode(elementHtml);
    }
  }


  @Override
  public WebPageExtractionResult extractPlainText(String webPageUrl) throws Exception {
    Document document = retrieveOnlineDocument(webPageUrl);
    return new WebPageExtractionResult(document.body().text(), document.title());
  }

  @Override
  public WebPageExtractionResult tryToRemoveClutter(String webPageUrl) throws Exception {
    // not possible without Boilerpipe which doesn't compile on Android
    return extractPlainText(webPageUrl);
  }

}

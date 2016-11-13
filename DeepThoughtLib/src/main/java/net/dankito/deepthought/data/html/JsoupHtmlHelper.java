package net.dankito.deepthought.data.html;

import net.dankito.deepthought.util.web.HttpMethod;
import net.dankito.deepthought.util.web.IWebClient;
import net.dankito.deepthought.util.web.RequestParameters;
import net.dankito.deepthought.util.web.responses.WebClientResponse;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ganymed on 07/04/15.
 */
public class JsoupHtmlHelper implements IHtmlHelper {

  public static final String DefaultUserAgent = "Mozilla/5.0 (Windows NT 6.3; rv:36.0) Gecko/20100101 Firefox/36.0";

  protected static final int DEFAULT_CONNECTION_TIMEOUT_MILLIS = 4000;


  private final static Logger log = LoggerFactory.getLogger(JsoupHtmlHelper.class);


  protected IWebClient webClient;


  public JsoupHtmlHelper(IWebClient webClient) {
    this.webClient = webClient;
  }


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
    return retrieveOnlineDocument(webPageUrl, DefaultUserAgent, null, HttpMethod.GET);
  }

  @Override
  public Document retrieveOnlineDocument(final String webPageUrl, String userAgent, String body, HttpMethod method) throws IOException {
    RequestParameters parameters = new RequestParameters(webPageUrl);
    parameters.setUserAgent(userAgent);
    parameters.setConnectionTimeoutMillis(DEFAULT_CONNECTION_TIMEOUT_MILLIS);
    parameters.setCountConnectionRetries(2);

    if(body != null) {
      parameters.setBody(body);
    }

    WebClientResponse response = null;

    if(method == HttpMethod.GET) {
      response = webClient.get(parameters);
    }
    else if(method == HttpMethod.POST) {
      response = webClient.post(parameters);
    }

    if(response != null && response.isSuccessful()) {
      return Jsoup.parse(response.getBody(), webPageUrl);
    }
    else {
      throw new IOException(response != null ? response.getError() : "Could not get Html for Url " + webPageUrl);
    }
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

package net.deepthought.data.html;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ganymed on 07/04/15.
 */
public class JsoupHtmlHelper implements IHtmlHelper {

  private final static Logger log = LoggerFactory.getLogger(JsoupHtmlHelper.class);


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
      elementData.setFileId(Long.parseLong(imgElement.attr(ImageElementData.ImageIdAttributeName)));
    if(imgElement.hasAttr(ImageElementData.EmbeddingIdAttributeName))
      elementData.setEmbeddingId(Long.parseLong(imgElement.attr(ImageElementData.EmbeddingIdAttributeName)));

    if(imgElement.hasAttr(ImageElementData.WidthAttributeName))
      elementData.setWidth(Integer.parseInt(imgElement.attr(ImageElementData.WidthAttributeName)));
    if(imgElement.hasAttr(ImageElementData.HeightAttributeName))
      elementData.setHeight(Integer.parseInt(imgElement.attr(ImageElementData.HeightAttributeName)));

    // there is a bug in JSoup: calling imgElement.outerHtml() returns not real Element's Html. In this case <img> element ends with ' />', but outerHtml() returns '>'
    String outerHtml = imgElement.outerHtml();
    int startIndex = html.indexOf(outerHtml.substring(0, outerHtml.length() - 4));
    int endIndex = html.indexOf(">", startIndex + 1) + 1;

    String elementHtml = html.substring(startIndex, endIndex);
    elementData.setOriginalImgElementHtmlCode(elementHtml);

    return elementData;
  }
}

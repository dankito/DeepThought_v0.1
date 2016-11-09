package net.dankito.deepthought.data.html;

import net.dankito.deepthought.util.web.HttpMethod;

import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.List;

/**
 * Created by ganymed on 07/04/15.
 */
public interface IHtmlHelper {

  boolean canRemoveClutterFromHtml();

  boolean canExtractPlainText();

  Document retrieveOnlineDocument(String webPageUrl) throws IOException;
  Document retrieveOnlineDocument(String webPageUrl, String userAgent, String body, HttpMethod method) throws IOException;

  String extractPlainTextFromHtmlBody(String html);

  List<ImageElementData> extractAllImageElementsFromHtml(String html);


  WebPageExtractionResult extractPlainText(String webPageUrl) throws Exception;

  WebPageExtractionResult tryToRemoveClutter(String webPageUrl) throws Exception;

}

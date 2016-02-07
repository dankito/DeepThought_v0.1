package net.deepthought.data.html;

import org.jsoup.Connection;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by ganymed on 07/04/15.
 */
public interface IHtmlHelper {

  Document retrieveOnlineDocument(String articleUrl) throws IOException;
  Document retrieveOnlineDocument(String articleUrl, String userAgent, Map<String, String> data, Connection.Method method) throws IOException;

  String extractPlainTextFromHtmlBody(String html);

  List<ImageElementData> extractAllImageElementsFromHtml(String html);

}

package net.deepthought.data.html;

import java.util.List;

/**
 * Created by ganymed on 07/04/15.
 */
public interface IHtmlHelper {

  String extractPlainTextFromHtmlBody(String html);

  List<ImageElementData> extractAllImageElementsFromHtml(String html);

}

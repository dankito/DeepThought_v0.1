package net.deepthought.data.html;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
}

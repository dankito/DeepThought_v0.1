package net.dankito.deepthought.data.html;

import com.kohlschutter.boilerpipe.extractors.CanolaExtractor;
import com.kohlschutter.boilerpipe.extractors.ExtractorBase;
import com.kohlschutter.boilerpipe.sax.HTMLHighlighter;

import org.jsoup.nodes.Document;

import java.net.URL;

/**
 * Created by ganymed on 27/06/16.
 */
public class JsoupAndBoilerpipeHtmlHelper extends JsoupHtmlHelper {

  protected HTMLHighlighter htmlHighlighter = null;

  protected ExtractorBase extractor = null;

  
  @Override
  public boolean canExtractPlainText() {
    return true;
  }

  @Override
  public boolean canRemoveClutterFromHtml() {
    return true;
  }


  @Override
  public String extractPlainText(String webPageUrl) throws Exception {
    Document document = retrieveOnlineDocument(webPageUrl);
    String plainText = CanolaExtractor.INSTANCE.getText(document.outerHtml());
    String formattedPlainText = "";

    for(String paragraph : plainText.split("\n")) {
      formattedPlainText += "<p>" + paragraph + "</p>";
    }

    return formattedPlainText;
  }

  @Override
  public String tryToRemoveClutter(String webPageUrl) throws Exception {
    HTMLHighlighter htmlExtractor = getHtmlHighlighter();
    ExtractorBase extractor = getExtractor();
    return htmlExtractor.process(new URL(webPageUrl), extractor);
  }

  protected HTMLHighlighter getHtmlHighlighter() {
    if(htmlHighlighter == null) {
      htmlHighlighter = HTMLHighlighter.newExtractingInstance();
    }

    return htmlHighlighter;
  }

  protected ExtractorBase getExtractor() {
    if(extractor == null) {
      extractor = CanolaExtractor.INSTANCE;
    }

    return extractor;
  }

}

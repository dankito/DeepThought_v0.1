package net.dankito.deepthought.data.html;

import com.kohlschutter.boilerpipe.extractors.CanolaExtractor;
import com.kohlschutter.boilerpipe.extractors.ExtractorBase;
import com.kohlschutter.boilerpipe.sax.HTMLHighlighter;

import net.dankito.deepthought.util.web.IWebClient;

import org.jsoup.nodes.Document;

import java.net.URL;

/**
 * Created by ganymed on 27/06/16.
 */
public class JsoupAndBoilerpipeHtmlHelper extends JsoupHtmlHelper {

  protected HTMLHighlighter htmlHighlighter = null;

  protected ExtractorBase extractor = null;


  public JsoupAndBoilerpipeHtmlHelper(IWebClient webClient) {
    super(webClient);
  }


  @Override
  public boolean canExtractPlainText() {
    return true;
  }

  @Override
  public boolean canRemoveClutterFromHtml() {
    return true;
  }


  @Override
  public WebPageExtractionResult extractPlainText(String webPageUrl) throws Exception {
    Document document = retrieveOnlineDocument(webPageUrl);
    String plainText = CanolaExtractor.INSTANCE.getText(document.outerHtml());
    String formattedPlainText = "";

    for(String paragraph : plainText.split("\n")) {
      formattedPlainText += "<p>" + paragraph + "</p>";
    }

    return new WebPageExtractionResult(formattedPlainText, document.title());
  }

  @Override
  public WebPageExtractionResult tryToRemoveClutter(String webPageUrl) throws Exception {
    HTMLHighlighter htmlExtractor = getHtmlHighlighter();
    ExtractorBase extractor = getExtractor();
    String extractedText = htmlExtractor.process(new URL(webPageUrl), extractor);

    String title = "";
    try {
      title = retrieveOnlineDocument(webPageUrl).title();
    } catch(Exception ignored) { } // title is not that important, just eat exceptions

    return new WebPageExtractionResult(extractedText, title);
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

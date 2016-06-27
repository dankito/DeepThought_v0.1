package net.dankito.deepthought.data.html;

/**
 * Created by ganymed on 27/06/16.
 */
public class WebPageExtractionResult {

  protected String extractedText;

  protected String webPageTitle;


  public WebPageExtractionResult(String extractedText) {
    this(extractedText, "");
  }

  public WebPageExtractionResult(String extractedText, String webPageTitle) {
    this.extractedText = extractedText;
    this.webPageTitle = webPageTitle;
  }


  public String getExtractedText() {
    return extractedText;
  }

  public String getWebPageTitle() {
    return webPageTitle;
  }


  @Override
  public String toString() {
    return extractedText;
  }

}

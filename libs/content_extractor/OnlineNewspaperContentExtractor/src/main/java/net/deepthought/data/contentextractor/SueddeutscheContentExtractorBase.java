package net.deepthought.data.contentextractor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

public abstract class SueddeutscheContentExtractorBase extends OnlineNewspaperContentExtractorBase {

  private final static Logger log = LoggerFactory.getLogger(SueddeutscheContentExtractorBase.class);

  @Override
  public String getNewspaperName() {
    return "SZ";
  }

  @Override
  public String getIconUrl() {
    try {
      URL url = SueddeutscheContentExtractorBase.class.getClassLoader().getResource("sz_icon.png");
      return url.toExternalForm();
      //return url.toString();
    } catch(Exception ex) {
      String iconFile = tryToManuallyLoadIcon(SueddeutscheContentExtractorBase.class, "sz_icon.png");
      if (iconFile != IOnlineArticleContentExtractor.NoIcon)
        return iconFile;
      else
        log.error("Could not load sz_icon.png from Resources", ex);
    }

    return super.getIconUrl();
  }

  @Override
  public boolean hasArticlesOverview() {
    return false;
  }

  @Override
  public String getSiteBaseUrl() {
    return "Sueddeutsche.de";
  }

}

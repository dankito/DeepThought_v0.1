package net.deepthought.data.contentextractor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

public abstract class SueddeutscheContentExtractorBase extends OnlineNewspaperContentExtractorBase {

  private final static Logger log = LoggerFactory.getLogger(SueddeutscheContentExtractorBase.class);

  protected static final String LogoFileName = "sz_logo.png";

  @Override
  public String getNewspaperName() {
    return "SZ";
  }

  @Override
  public String getIconUrl() {
    try {
      URL url = SueddeutscheContentExtractorBase.class.getClassLoader().getResource(LogoFileName);
      return url.toExternalForm();
      //return url.toString();
    } catch(Exception ex) {
      String iconFile = tryToManuallyLoadIcon(SueddeutscheContentExtractorBase.class, LogoFileName);
      if (iconFile != IOnlineArticleContentExtractor.NoIcon)
        return iconFile;
      else
        log.error("Could not load " + LogoFileName + " from Resources", ex);
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

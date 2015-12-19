package net.deepthought.data.contentextractor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SueddeutscheContentExtractorBase extends OnlineNewspaperContentExtractorBase {

  private final static Logger log = LoggerFactory.getLogger(SueddeutscheContentExtractorBase.class);

  protected static final String LogoFileName = "sz_logo.png";

  @Override
  public String getNewspaperName() {
    return "SZ";
  }

  @Override
  public String getIconUrl() {
    return tryToLoadIconFile(LogoFileName);
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

package net.deepthought.data.contentextractor;

public abstract class SueddeutscheContentExtractorBase extends OnlineNewspaperContentExtractorBase {

  protected static final String LogoFileName = "sz_logo.png";

  @Override
  public String getNewspaperName() {
    return "SZ";
  }

  @Override
  public String getIconUrl() {
//    return "http://polpix.sueddeutsche.com/staticassets/img/touch-icon-ipad-retina.png";
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

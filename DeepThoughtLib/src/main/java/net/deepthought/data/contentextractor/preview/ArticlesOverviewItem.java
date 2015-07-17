package net.deepthought.data.contentextractor.preview;

/**
 * Created by ganymed on 17/07/15.
 */
public class ArticlesOverviewItem {

  protected String url;

  protected String title = null;

  protected String subTitle = null;

  protected String abstractString = null;

  protected String previewImageUrl = null;


  public ArticlesOverviewItem(String url) {
    this.url = url;
  }

  public ArticlesOverviewItem(String url, String abstractString) {
    this(url);
    this.abstractString = abstractString;
  }

  public ArticlesOverviewItem(String url, String abstractString, String title) {
    this(url, abstractString);
    this.title = title;
  }

  public ArticlesOverviewItem(String url, String abstractString, String title, String subTitle) {
    this(url, abstractString, title);
    this.subTitle = subTitle;
  }

  public ArticlesOverviewItem(String url, String abstractString, String title, String subTitle, String previewImageUrl) {
    this(url, abstractString, title, subTitle);
    this.previewImageUrl = previewImageUrl;
  }


  public String getUrl() {
    return url;
  }

  public boolean hasTitle() {
    return title != null;
  }

  public String getTitle() {
    return title;
  }

  public boolean hasSubTitle() {
    return subTitle != null;
  }

  public String getSubTitle() {
    return subTitle;
  }

  public boolean hasAbstract() {
    return abstractString != null;
  }

  public String getAbstract() {
    return abstractString;
  }

  public boolean hasPreviewImageUrl() {
    return previewImageUrl != null;
  }

  public String getPreviewImageUrl() {
    return previewImageUrl;
  }


  @Override
  public String toString() {
    String description = url;

    if(hasAbstract())
      description = abstractString + " (" + description + ")";

    if(hasTitle())
      description = title + ": " + description;

    return description;
  }
}

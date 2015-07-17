package net.deepthought.data.contentextractor.preview;

/**
 * Created by ganymed on 17/07/15.
 */
public class ArticlesOverviewItem {

  protected String url;

  protected String title = null;

  protected String subTitle = null;

  protected String summary = null;

  protected String previewImageUrl = null;

  protected String label = null;


  public ArticlesOverviewItem(String url) {
    this.url = url;
  }

  public ArticlesOverviewItem(String url, String summary) {
    this(url);
    this.summary = summary;
  }

  public ArticlesOverviewItem(String url, String summary, String title) {
    this(url, summary);
    this.title = title;
  }

  public ArticlesOverviewItem(String url, String summary, String title, String subTitle) {
    this(url, summary, title);
    this.subTitle = subTitle;
  }

  public ArticlesOverviewItem(String url, String summary, String title, String subTitle, String previewImageUrl) {
    this(url, summary, title, subTitle);
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

  public void setTitle(String title) {
    this.title = title;
  }

  public boolean hasSubTitle() {
    return subTitle != null;
  }

  public String getSubTitle() {
    return subTitle;
  }

  public void setSubTitle(String subTitle) {
    this.subTitle = subTitle;
  }

  public boolean hasSummary() {
    return summary != null;
  }

  public String getSummary() {
    return summary;
  }

  public void setSummary(String summary) {
    this.summary = summary;
  }

  public boolean hasPreviewImageUrl() {
    return previewImageUrl != null;
  }

  public String getPreviewImageUrl() {
    return previewImageUrl;
  }

  public void setPreviewImageUrl(String previewImageUrl) {
    this.previewImageUrl = previewImageUrl;
  }

  public boolean hasLabel() {
    return label != null;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }


  @Override
  public String toString() {
    String description = url;

    if(hasSummary())
      description = summary + " (" + description + ")";

    if(hasTitle())
      description = title + ": " + description;

    if(hasSubTitle())
      description = subTitle + " - " + description;

    return description;
  }
}

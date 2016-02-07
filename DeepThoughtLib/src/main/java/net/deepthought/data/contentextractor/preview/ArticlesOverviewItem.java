package net.deepthought.data.contentextractor.preview;

import net.deepthought.data.contentextractor.IOnlineArticleContentExtractor;
import net.deepthought.util.StringUtils;

/**
 * Created by ganymed on 17/07/15.
 */
public class ArticlesOverviewItem {

  protected IOnlineArticleContentExtractor articleContentExtractor = null;

  protected String url;

  protected String title = null;

  protected String subTitle = null;

  protected String summary = null;

  protected String previewImageUrl = null;

  protected String categories = null;

  protected String label = null;


  public ArticlesOverviewItem(IOnlineArticleContentExtractor articleContentExtractor, String url) {
    this.articleContentExtractor = articleContentExtractor;
    this.url = url;
  }

  public ArticlesOverviewItem(IOnlineArticleContentExtractor articleContentExtractor, String url, String summary) {
    this(articleContentExtractor, url);
    this.summary = summary;
  }

  public ArticlesOverviewItem(IOnlineArticleContentExtractor articleContentExtractor, String url, String summary, String title) {
    this(articleContentExtractor, url, summary);
    this.title = title;
  }

  public ArticlesOverviewItem(IOnlineArticleContentExtractor articleContentExtractor, String url, String summary, String title, String subTitle) {
    this(articleContentExtractor, url, summary, title);
    this.subTitle = subTitle;
  }

  public ArticlesOverviewItem(IOnlineArticleContentExtractor articleContentExtractor, String url, String summary, String title, String subTitle, String previewImageUrl) {
    this(articleContentExtractor, url, summary, title, subTitle);
    this.previewImageUrl = previewImageUrl;
  }


  public IOnlineArticleContentExtractor getArticleContentExtractor() {
    return articleContentExtractor;
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
    return StringUtils.isNotNullOrEmpty(previewImageUrl);
  }

  public String getPreviewImageUrl() {
    return previewImageUrl;
  }

  public void setPreviewImageUrl(String previewImageUrl) {
    this.previewImageUrl = previewImageUrl;
  }

  public boolean hasCategories() {
    return categories != null;
  }

  public String getCategories() {
    return categories;
  }

  public void setCategories(String categories) {
    this.categories = categories;
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

  public String getCategoriesAndLabel() {
    if(hasCategories() == true && hasLabel() == false)
      return getCategories();
    else if(hasCategories() == false && hasLabel() == true)
      return getLabel();

    return categories + ", " + label;
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

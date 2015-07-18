package net.deepthought.data.contentextractor;

import net.deepthought.data.contentextractor.preview.ArticlesOverviewListener;

/**
 * Created by ganymed on 25/04/15.
 */
public interface IOnlineArticleContentExtractor extends IContentExtractor {

  public final static String NoIcon = "No_Icon";


  public String getSiteBaseUrl();

  public String getIconUrl();

  public boolean hasArticlesOverview();

  public void getArticlesOverviewAsync(ArticlesOverviewListener listener);

}

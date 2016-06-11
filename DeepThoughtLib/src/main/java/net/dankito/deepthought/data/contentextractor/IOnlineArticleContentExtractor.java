package net.dankito.deepthought.data.contentextractor;

import net.dankito.deepthought.data.contentextractor.preview.ArticlesOverviewListener;

/**
 * Created by ganymed on 25/04/15.
 */
public interface IOnlineArticleContentExtractor extends IContentExtractor {

  String NoIcon = "No_Icon";


  String getSiteBaseUrl();

  String getIconUrl();

  void createEntryFromUrlAsync(String url, CreateEntryListener listener);

  boolean hasArticlesOverview();

  void getArticlesOverviewAsync(ArticlesOverviewListener listener);

}

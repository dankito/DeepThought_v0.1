package net.deepthought.data.contentextractor.preview;

import net.deepthought.data.contentextractor.IOnlineArticleContentExtractor;

import java.util.Collection;

/**
 * Created by ganymed on 17/07/15.
 */
public interface ArticlesOverviewListener {

  public void overviewItemsRetrieved(IOnlineArticleContentExtractor contentExtractor, Collection<ArticlesOverviewItem> items, boolean isDone);

}

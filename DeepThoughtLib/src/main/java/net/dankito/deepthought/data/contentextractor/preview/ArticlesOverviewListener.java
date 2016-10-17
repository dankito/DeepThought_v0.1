package net.dankito.deepthought.data.contentextractor.preview;

import net.dankito.deepthought.data.contentextractor.IOnlineArticleContentExtractor;

import java.util.List;

/**
 * Created by ganymed on 17/07/15.
 */
public interface ArticlesOverviewListener {

  void overviewItemsRetrieved(IOnlineArticleContentExtractor contentExtractor, List<ArticlesOverviewItem> items, boolean isDone);

}

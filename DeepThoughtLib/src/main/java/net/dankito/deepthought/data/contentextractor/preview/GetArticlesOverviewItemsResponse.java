package net.dankito.deepthought.data.contentextractor.preview;

import net.dankito.deepthought.data.contentextractor.IOnlineArticleContentExtractor;
import net.dankito.deepthought.util.web.responses.ResponseBase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ganymed on 13/11/16.
 */

public class GetArticlesOverviewItemsResponse extends ResponseBase {

  protected IOnlineArticleContentExtractor contentExtractor;

  protected List<ArticlesOverviewItem> items = new ArrayList<>();

  protected boolean isDone = true;


  public GetArticlesOverviewItemsResponse(IOnlineArticleContentExtractor contentExtractor, String error) {
    super(error);
    this.contentExtractor = contentExtractor;
  }

  public GetArticlesOverviewItemsResponse(IOnlineArticleContentExtractor contentExtractor, List<ArticlesOverviewItem> items, boolean isDone) {
    super(true);
    this.contentExtractor = contentExtractor;
    this.items = items;
    this.isDone = isDone;
  }


  public IOnlineArticleContentExtractor getContentExtractor() {
    return contentExtractor;
  }

  public List<ArticlesOverviewItem> getItems() {
    return items;
  }

  public boolean isDone() {
    return isDone;
  }

}

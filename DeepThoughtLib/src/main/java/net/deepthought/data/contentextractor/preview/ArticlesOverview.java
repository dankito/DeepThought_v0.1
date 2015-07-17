package net.deepthought.data.contentextractor.preview;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ganymed on 17/07/15.
 */
public class ArticlesOverview {

  protected List<ArticlesOverviewItem> overviewItems = new ArrayList<>();


  public List<ArticlesOverviewItem> getOverviewItems() {
    return overviewItems;
  }

  public boolean addOverviewItem(ArticlesOverviewItem item) {
    return overviewItems.add(item);
  }


  @Override
  public String toString() {
    return overviewItems.size() + " items";
  }

}

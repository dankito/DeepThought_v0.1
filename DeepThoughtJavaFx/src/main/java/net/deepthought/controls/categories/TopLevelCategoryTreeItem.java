package net.deepthought.controls.categories;

import net.deepthought.Application;
import net.deepthought.data.model.Entry;

/**
 * Created by ganymed on 03/02/15.
 */
public class TopLevelCategoryTreeItem extends EntryCategoryTreeItem {


  public TopLevelCategoryTreeItem(Entry entry) {
    super(entry, Application.getDeepThought().getTopLevelCategory());

    setExpanded(true);
  }

}

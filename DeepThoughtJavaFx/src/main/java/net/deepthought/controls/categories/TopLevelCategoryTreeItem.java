package net.deepthought.controls.categories;

import net.deepthought.Application;

/**
 * Created by ganymed on 03/02/15.
 */
public class TopLevelCategoryTreeItem extends EntryCategoryTreeItem {


  public TopLevelCategoryTreeItem() {
    super(Application.getDeepThought().getTopLevelCategory());

    setExpanded(true);
  }

}

package net.dankito.deepthought.controls.categories;

import net.dankito.deepthought.Application;

/**
 * Created by ganymed on 03/02/15.
 */
public class TopLevelCategoryTreeItem extends EntryCategoryTreeItem {


  public TopLevelCategoryTreeItem() {
    super(Application.getDeepThought().getTopLevelCategory());

    setExpanded(true);
  }

}

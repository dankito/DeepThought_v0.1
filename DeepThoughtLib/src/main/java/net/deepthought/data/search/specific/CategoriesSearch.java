package net.deepthought.data.search.specific;

import net.deepthought.data.model.Category;
import net.deepthought.data.search.Search;
import net.deepthought.data.search.SearchCompletedListener;

import java.util.Collection;

/**
 * Created by ganymed on 27/07/15.
 */
public class CategoriesSearch extends Search<Category> {

  protected Long parentCategoryId = null;


  public CategoriesSearch(String searchTerm, SearchCompletedListener<Collection<Category>> completedListener) {
    super(searchTerm, completedListener);
  }

  public CategoriesSearch(String searchTerm, Long parentCategoryId, SearchCompletedListener<Collection<Category>> completedListener) {
    this(searchTerm, completedListener);
    this.parentCategoryId = parentCategoryId;
  }


  public boolean isParentCategoryIdSet() {
    return parentCategoryId != null;
  }

  public Long getParentCategoryId() {
    return parentCategoryId;
  }

}

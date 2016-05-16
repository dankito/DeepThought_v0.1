package net.deepthought.data.search.specific;

import net.deepthought.data.model.Category;
import net.deepthought.data.search.Search;
import net.deepthought.data.search.SearchCompletedListener;

import java.util.Collection;

/**
 * Created by ganymed on 27/07/15.
 */
public class CategoriesSearch extends Search<Category> {

  protected boolean topLevelCategoriesOnly = false;

  protected Category parentCategory = null;


  public CategoriesSearch(String searchTerm, SearchCompletedListener<Collection<Category>> completedListener) {
    super(searchTerm, completedListener);
  }

  /**
   * If {@code topLevelCategoriesOnly} is set to true, searches only for Top Level Categories (= Categories that have no Parent Category).
   */
  public CategoriesSearch(String searchTerm, boolean topLevelCategoriesOnly, SearchCompletedListener<Collection<Category>> completedListener) {
    this(searchTerm, completedListener);
    this.topLevelCategoriesOnly = topLevelCategoriesOnly;
  }

  public CategoriesSearch(String searchTerm, Category parentCategory, SearchCompletedListener<Collection<Category>> completedListener) {
    this(searchTerm, completedListener);
    this.parentCategory = parentCategory;
  }


  public boolean topLevelCategoriesOnly() {
    return topLevelCategoriesOnly;
  }

  public boolean isParentCategorySet() {
    return parentCategory != null;
  }

  public Category getParentCategory() {
    return parentCategory;
  }

}

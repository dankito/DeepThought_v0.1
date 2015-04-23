package net.deepthought.data.search;

import net.deepthought.data.model.Entry;

/**
 * Created by ganymed on 12/04/15.
 */
public class FilterEntriesSearch extends Search<Entry> {

  protected boolean filterAbstract;

  protected boolean filterContent;


  public FilterEntriesSearch(String searchTerm, boolean filterContent, boolean filterAbstract) {
    super(searchTerm);

    this.filterContent = filterContent;
    this.filterAbstract = filterAbstract;
  }

  public FilterEntriesSearch(String searchTerm, boolean filterContent, boolean filterAbstract, SearchCompletedListener<Entry> completedListener) {
    super(searchTerm, completedListener);

    this.filterContent = filterContent;
    this.filterAbstract = filterAbstract;
  }


  public boolean filterAbstract() {
    return filterAbstract;
  }

  public boolean filterContent() {
    return filterContent;
  }

}

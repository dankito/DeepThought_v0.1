package net.deepthought.data.search;

import net.deepthought.data.model.Entry;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by ganymed on 12/04/15.
 */
public class FilterEntriesSearch extends Search<Entry> {

  protected boolean filterAbstract;

  protected boolean filterContent;

  protected Collection<Entry> entriesToFilter = new ArrayList<>();

  protected boolean filterOnlyEntriesWithoutTags = false;


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

  public FilterEntriesSearch(String searchTerm, boolean filterContent, boolean filterAbstract, boolean filterOnlyEntriesWithoutTags, SearchCompletedListener<Entry> completedListener) {
    this(searchTerm, filterContent, filterAbstract, completedListener);

    this.filterOnlyEntriesWithoutTags = filterOnlyEntriesWithoutTags;
  }

  public FilterEntriesSearch(String searchTerm, boolean filterContent, boolean filterAbstract, Collection<Entry> entriesToFilter, SearchCompletedListener<Entry> completedListener) {
    this(searchTerm, filterContent, filterAbstract, completedListener);

    this.entriesToFilter = entriesToFilter;
  }


  public boolean filterAbstract() {
    return filterAbstract;
  }

  public void setFilterAbstract(boolean filterAbstract) {
    this.filterAbstract = filterAbstract;
  }

  public boolean filterContent() {
    return filterContent;
  }

  public void setFilterContent(boolean filterContent) {
    this.filterContent = filterContent;
  }

  public Collection<Entry> getEntriesToFilter() {
    return entriesToFilter;
  }

  public void setEntriesToFilter(Collection<Entry> entriesToFilter) {
    this.entriesToFilter = entriesToFilter;
  }

  public boolean filterOnlyEntriesWithoutTags() {
    return filterOnlyEntriesWithoutTags;
  }

  public void setFilterOnlyEntriesWithoutTags(boolean filterOnlyEntriesWithoutTags) {
    this.filterOnlyEntriesWithoutTags = filterOnlyEntriesWithoutTags;
  }
}

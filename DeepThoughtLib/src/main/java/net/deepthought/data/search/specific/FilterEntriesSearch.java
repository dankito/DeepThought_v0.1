package net.deepthought.data.search.specific;

import net.deepthought.data.model.Entry;
import net.deepthought.data.model.Tag;
import net.deepthought.data.search.Search;
import net.deepthought.data.search.SearchCompletedListener;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by ganymed on 12/04/15.
 */
public class FilterEntriesSearch extends Search<Entry> {

  protected boolean filterAbstract;

  protected boolean filterContent;

  protected Collection<Tag> entriesMustHaveTheseTags = new ArrayList<>();

  protected Collection<Entry> entriesToFilter = new ArrayList<>();

  protected boolean filterOnlyEntriesWithoutTags = false;


  public FilterEntriesSearch(String searchTerm, boolean filterContent, boolean filterAbstract) {
    super(searchTerm);

    this.filterContent = filterContent;
    this.filterAbstract = filterAbstract;
  }

  public FilterEntriesSearch(String searchTerm, boolean filterContent, boolean filterAbstract, SearchCompletedListener<Collection<Entry>> completedListener) {
    super(searchTerm, completedListener);

    this.filterContent = filterContent;
    this.filterAbstract = filterAbstract;
  }

  public FilterEntriesSearch(String searchTerm, boolean filterContent, boolean filterAbstract, boolean filterOnlyEntriesWithoutTags, SearchCompletedListener<Collection<Entry>> completedListener) {
    this(searchTerm, filterContent, filterAbstract, completedListener);

    this.filterOnlyEntriesWithoutTags = filterOnlyEntriesWithoutTags;
  }

  public FilterEntriesSearch(String searchTerm, boolean filterContent, boolean filterAbstract, Collection<Tag> entriesMustHaveTheseTags, SearchCompletedListener<Collection<Entry>> completedListener) {
    this(searchTerm, filterContent, filterAbstract, completedListener);

    this.entriesMustHaveTheseTags = entriesMustHaveTheseTags;
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

  public Collection<Tag> getEntriesMustHaveTheseTags() {
    return entriesMustHaveTheseTags;
  }

  public void setEntriesMustHaveTheseTags(Collection<Tag> entriesMustHaveTheseTags) {
    this.entriesMustHaveTheseTags = entriesMustHaveTheseTags;
  }

  public boolean addTagEntriesMustHave(Tag tag) {
    return entriesMustHaveTheseTags.add(tag);
  }

  public boolean filterOnlyEntriesWithoutTags() {
    return filterOnlyEntriesWithoutTags;
  }

  public void setSearchOnlyEntriesWithoutTags(boolean filterOnlyEntriesWithoutTags) {
    this.filterOnlyEntriesWithoutTags = filterOnlyEntriesWithoutTags;
  }
}

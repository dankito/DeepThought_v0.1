package net.deepthought.data.search.specific;

import net.deepthought.data.model.Tag;
import net.deepthought.data.search.SearchBase;
import net.deepthought.data.search.SearchCompletedListener;

import java.util.Collection;

/**
 * Created by ganymed on 27/07/15.
 */
public class TagsSearch extends SearchBase {

  protected TagsSearchResults results = null;

  protected SearchCompletedListener<TagsSearchResults> completedListener = null;


  public TagsSearch(String searchTerm) {
    super(searchTerm);
    results = new TagsSearchResults(searchTerm);
  }

  public TagsSearch(String searchTerm, SearchCompletedListener<TagsSearchResults> completedListener) {
    this(searchTerm);
    this.completedListener = completedListener;
  }


  protected void callCompletedListener() {
    if(completedListener != null)
      completedListener.completed(results);
  }

  @Override
  protected int getResultsCount() {
    return results.getRelevantMatchesCount();
  }


  public boolean addResult(TagsSearchResult result) {
    return this.results.addSearchResult(result);
  }

  public void setHasEmptySearchTerm(boolean hasEmptySearchTerm) {
    this.results.setHasEmptySearchTerm(hasEmptySearchTerm);
  }

  public void setRelevantMatchesSorted(Collection<Tag> allMatchesSorted) {
    this.results.setRelevantMatchesSorted(allMatchesSorted);
  }

  public TagsSearchResults getResults() {
    return results;
  }
}

package net.deepthought.data.search.specific;

import net.deepthought.data.model.Tag;
import net.deepthought.data.search.SearchBase;
import net.deepthought.data.search.SearchCompletedListener;

import java.util.Collection;

/**
 * Created by ganymed on 27/07/15.
 */
public class FilterTagsSearch extends SearchBase {

  public final static String EmptySearchTerm = "";


  protected FilterTagsSearchResults results = null;

  protected SearchCompletedListener<FilterTagsSearchResults> completedListener = null;


  public FilterTagsSearch(String searchTerm) {
    super(searchTerm);
    results = new FilterTagsSearchResults(searchTerm);
  }

  public FilterTagsSearch(String searchTerm, SearchCompletedListener<FilterTagsSearchResults> completedListener) {
    this(searchTerm);
    this.completedListener = completedListener;
  }


  public void setResults(FilterTagsSearchResults results) {
    this.results = results;
  }

  protected void callCompletedListener() {
    if(completedListener != null)
      completedListener.completed(results);
  }

  @Override
  protected int getResultsCount() {
    return results.getAllMatches().size();
  }


  public boolean addResult(FilterTagsSearchResult result) {
    return this.results.addSearchResult(result);
  }

  public void setHasEmptySearchTerm(boolean hasEmptySearchTerm) {
    this.results.setHasEmptySearchTerm(hasEmptySearchTerm);
  }

  public void setAllMatchesSorted(Collection<Tag> allMatchesSorted) {
    this.results.setAllMatchesSorted(allMatchesSorted);
  }

  public FilterTagsSearchResults getResults() {
    return results;
  }
}

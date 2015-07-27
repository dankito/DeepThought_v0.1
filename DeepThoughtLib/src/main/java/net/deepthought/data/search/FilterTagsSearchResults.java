package net.deepthought.data.search;

import net.deepthought.Application;
import net.deepthought.data.model.Tag;
import net.deepthought.data.persistence.CombinedLazyLoadingList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by ganymed on 27/07/15.
 */
public class FilterTagsSearchResults {

  protected String overAllSearchTerm;

  protected List<FilterTagsSearchResult> results = new ArrayList<>();

  protected Collection<Tag> allMatches = null;

  protected Collection<Tag> relevantMatches = null;

  protected Collection<Tag> exactMatches = null;
  protected Collection<Tag> singleMatchesOfASearchTerm = null;


  public FilterTagsSearchResults() {

  }

  public FilterTagsSearchResults(String overAllSearchTerm) {
    this();
    this.overAllSearchTerm = overAllSearchTerm;
  }


  public boolean addSearchResult(FilterTagsSearchResult result) {
    allMatches = relevantMatches = exactMatches = singleMatchesOfASearchTerm = null;
    return results.add(result);
  }


  public Collection<Tag> getAllMatches() {
    if(allMatches == null)
      determineMatchCategories();

    return allMatches;
  }

  public boolean isMatch(Tag tag) {
    return getAllMatches().contains(tag);
  }


  public Collection<Tag> getRelevantMatches() {
    if(relevantMatches == null)
      determineMatchCategories();

    return relevantMatches;
  }

  public boolean isRelevantMatch(Tag tag) {
    return getRelevantMatches().contains(tag);
  }



  public Collection<Tag> getExactMatches() {
    if(exactMatches == null)
      determineMatchCategories();

    return exactMatches;
  }

  public boolean isExactMatch(Tag tag) {
    return getExactMatches().contains(tag);
  }

  public Collection<Tag> getSingleMatchesOfASearchTerm() {
    if(singleMatchesOfASearchTerm == null)
      determineMatchCategories();

    return singleMatchesOfASearchTerm;
  }

  public boolean isSingleMatchOfASearchTerm(Tag tag) {
    return getSingleMatchesOfASearchTerm().contains(tag);
  }


  public boolean isRelevantMatchOfLastSearchTerm(Tag tag) {
    if(results.size() > 0) {
      FilterTagsSearchResult lastResult = getLastResult();
      return tag != null && lastResult.getAllMatches().contains(tag);
    }

    return false;
  }

  public boolean isExactMatchOfLastSearchTerm(Tag tag) {
    if(results.size() > 0) {
      FilterTagsSearchResult lastResult = getLastResult();
      return lastResult.hasExactMatch() && tag != null && tag.equals(lastResult.getExactMatch());
    }

    return false;
  }


  protected void determineMatchCategories() {
    allMatches = new CombinedLazyLoadingList<>();
    relevantMatches = new ArrayList<>();
    exactMatches = new ArrayList<Tag>();
    singleMatchesOfASearchTerm = new ArrayList<>();

    for(int i = 0; i < results.size() - 1; i++) {
      FilterTagsSearchResult result = results.get(i);
      allMatches.addAll(result.getAllMatches());

      if(result.hasExactMatch()) {
        relevantMatches.add(result.getExactMatch());
        exactMatches.add(result.getExactMatch());
      }
      else
        relevantMatches.addAll(result.getAllMatches());

      if(result.hasExactMatch() == false && result.getAllMatchesCount() == 1) // FilterTagsSearchResult has only a single match
        singleMatchesOfASearchTerm.addAll(result.getAllMatches());
    }

    if(results.size() > 0) {
      FilterTagsSearchResult lastResult = getLastResult();
      allMatches.addAll(lastResult.getAllMatches());

      if(lastResult.hasExactMatch())
        exactMatches.add(lastResult.getExactMatch());

      if(overAllSearchTerm.endsWith(",") && lastResult.hasExactMatch())
        relevantMatches.add(lastResult.getExactMatch());
      else
        relevantMatches.addAll(lastResult.getAllMatches());
    }
  }

  protected FilterTagsSearchResult getLastResult() {
    return results.get(results.size() - 1);
  }


  public List<FilterTagsSearchResult> getResults() {
    return results;
  }

  public final static FilterTagsSearchResults NoFilterSearchResults = new FilterTagsSearchResults() {

    @Override
    public Collection<Tag> getAllMatches() {
      return Application.getDeepThought().getSortedTags();
    }

    @Override
    public boolean isMatch(Tag tag) {
      return true;
    }

    @Override
    public boolean isRelevantMatch(Tag tag) {
      return true;
    }

    @Override
    public boolean isExactMatch(Tag tag) {
      return false;
    }

    @Override
    public boolean isExactMatchOfLastSearchTerm(Tag tag) {
      return false;
    }

    @Override
    public boolean isRelevantMatchOfLastSearchTerm(Tag tag) {
      return false;
    }

    @Override
    public boolean isSingleMatchOfASearchTerm(Tag tag) {
      return false;
    }
  };
}

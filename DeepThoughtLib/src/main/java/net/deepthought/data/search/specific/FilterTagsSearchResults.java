package net.deepthought.data.search.specific;

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

  protected boolean hasEmptySearchTerm = false;

  protected List<FilterTagsSearchResult> results = new ArrayList<>();

  protected Collection<Tag> allMatches = null;
  protected Collection<Tag> allMatchesSorted = null;

  protected Collection<Tag> relevantMatches = null;

  protected Collection<Tag> exactMatches = null;
  protected Collection<Tag> singleMatchesOfASearchTerm = null;

  protected Collection<Tag> exactOrSingleMatchesNotOfLastResult = null;

  protected Collection<Tag> matchesButOfLastResult = null;


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

  public Collection<Tag> getAllMatchesSorted() {
    return allMatchesSorted;
  }

  public void setAllMatchesSorted(Collection<Tag> allMatchesSorted) {
    this.allMatchesSorted = allMatchesSorted;
  }


  public Collection<Tag> getAllMatches() {
    if(allMatchesSorted != null)
      return allMatchesSorted;

    if(allMatches == null)
      determineMatchCategories();

    return allMatches;
  }

  public boolean isMatch(Tag tag) {
    return getAllMatches().contains(tag);
  }


  public Collection<Tag> getRelevantMatches() {
    if(allMatchesSorted != null)
      return allMatchesSorted;

    if(relevantMatches == null)
      determineMatchCategories();

    return relevantMatches;
  }

  public boolean isRelevantMatch(Tag tag) {
    return getRelevantMatches().contains(tag);
  }



  public Collection<Tag> getExactMatches() {
    if(exactMatches == null)
      exactMatches = determineExactMatches();

    return exactMatches;
  }

  protected Collection<Tag> determineExactMatches() {
    Collection<Tag> exactMatches = new ArrayList<>();

    for(FilterTagsSearchResult result : getResults()) {
      if(result.hasExactMatch())
        exactMatches.add(result.getExactMatch());
    }

    return exactMatches;
  }


  public boolean isExactOrSingleMatchButNotOfLastResult(Tag tag) {
    if(hasEmptySearchTerm()) // no exact or relevant matches
      return false;
    if(results.size() < 2) // no or only one (= last) result
      return false;

    return getExactOrSingleMatchesNotOfLastResult().contains(tag);
  }

  public boolean isMatchButNotOfLastResult(Tag tag) {
    if(hasEmptySearchTerm()) // no exact or relevant matches
      return false;
    if(results.size() < 2) // no or only one (= last) result
      return false;

    return getMatchesNotOfLastResult().contains(tag);
  }

  public boolean isExactMatchOfLastResult(Tag tag) {
    if(hasEmptySearchTerm()) // no exact or relevant matches
      return false;
    if(hasLastResult() == false)
      return false;

    FilterTagsSearchResult lastResult = getLastResult();
    return lastResult.hasExactMatch() && lastResult.getExactMatch().equals(tag);
  }

  public boolean isSingleMatchOfLastResult(Tag tag) {
    if(hasEmptySearchTerm()) // no exact or relevant matches
      return false;
    if(hasLastResult() == false)
      return false;

    FilterTagsSearchResult lastResult = getLastResult();
    return lastResult.hasSingleMatch() && lastResult.getSingleMatch().equals(tag);
  }

  public boolean isMatchOfLastResult(Tag tag) {
    if(hasEmptySearchTerm()) // no exact or relevant matches
      return false;
    if(hasLastResult())
      return false;

    return getLastResult().getAllMatches().contains(tag);
  }


  protected Collection<Tag> getExactOrSingleMatchesNotOfLastResult() {
    if(exactOrSingleMatchesNotOfLastResult == null)
      exactOrSingleMatchesNotOfLastResult = determineExactOrSingleMatchesNotOfLastResult();
    return exactOrSingleMatchesNotOfLastResult;
  }

  protected Collection<Tag> determineExactOrSingleMatchesNotOfLastResult() {
    List<Tag> nonLastResultExactOrSingleMatches = new ArrayList<>();

    for(int i = 0; i < results.size() - 1; i++) {
      FilterTagsSearchResult result = results.get(i);
      if(result.hasExactMatch())
        nonLastResultExactOrSingleMatches.add(result.getExactMatch());
      else if(result.hasSingleMatch())
        nonLastResultExactOrSingleMatches.add(result.getSingleMatch());
    }

    return nonLastResultExactOrSingleMatches;
  }

  protected Collection<Tag> getMatchesNotOfLastResult() {
    if(matchesButOfLastResult == null)
      matchesButOfLastResult = determineMatchesNotOfLastResult();
    return matchesButOfLastResult;
  }

  protected Collection<Tag> determineMatchesNotOfLastResult() {
    List<Tag> nonLastResultNotExactOrSingleMatches = new ArrayList<>();

    for(int i = 0; i < results.size() - 1; i++) {
      FilterTagsSearchResult result = results.get(i);
      if(result.hasExactMatch() == false && result.hasSingleMatch() == false)
        nonLastResultNotExactOrSingleMatches.addAll(result.getAllMatches());
    }

    return nonLastResultNotExactOrSingleMatches;
  }


  protected void determineMatchCategories() {
    allMatches = new CombinedLazyLoadingList<>();
    relevantMatches = new CombinedLazyLoadingList<>();
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

      if(lastResult.hasExactMatch() == false && lastResult.getAllMatchesCount() == 1) // FilterTagsSearchResult has only a single match
        singleMatchesOfASearchTerm.addAll(lastResult.getAllMatches());
    }
  }


  protected boolean hasLastResult() {
    return results.size() > 0; // no result (and therefore not last result) at all
  }

  public FilterTagsSearchResult getLastResult() {
    return results.get(results.size() - 1);
  }


  public List<FilterTagsSearchResult> getResults() {
    return results;
  }

  public String getOverAllSearchTerm() {
    return overAllSearchTerm;
  }

  public boolean hasEmptySearchTerm() {
    return hasEmptySearchTerm;
  }

  public void setHasEmptySearchTerm(boolean hasEmptySearchTerm) {
    this.hasEmptySearchTerm = hasEmptySearchTerm;
  }


  @Override
  public String toString() {
    return overAllSearchTerm + " has " + getAllMatches().size() + " results";
  }


  // TODO: remove NoFilterSearchResults

  public final static FilterTagsSearchResults NoFilterSearchResults = new FilterTagsSearchResults() {

    @Override
    public Collection<Tag> getAllMatches() {
      if(Application.getDeepThought() != null)
        return Application.getDeepThought().getSortedTags();
      return new ArrayList<>();
    }

    @Override
    public boolean isMatch(Tag tag) {
      return true;
    }

    @Override
    public boolean isRelevantMatch(Tag tag) {
      return true;
    }
  };
}

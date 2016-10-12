package net.dankito.deepthought.data.search.ui;

import net.dankito.deepthought.data.model.Tag;
import net.dankito.deepthought.data.search.ISearchEngine;
import net.dankito.deepthought.data.search.SearchBase;
import net.dankito.deepthought.data.search.SearchCompletedListener;
import net.dankito.deepthought.data.search.specific.FindAllEntriesHavingTheseTagsResult;
import net.dankito.deepthought.data.search.specific.TagsSearch;
import net.dankito.deepthought.data.search.specific.TagsSearchResults;
import net.dankito.deepthought.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by ganymed on 22/09/16.
 */
public class TagsSearcher {

  protected ISearchEngine searchEngine;

  protected TagsSearch tagsSearch = null;
  protected String lastSearchTerm = SearchBase.EmptySearchTerm;

  protected List<Tag> searchResults = new ArrayList<>();
  protected TagsSearchResults lastSearchTagsResult = null;

  protected List<Tag> tagsFilter = new ArrayList<>();
  protected FindAllEntriesHavingTheseTagsResult lastFilterTagsResult = null;


  public TagsSearcher(ISearchEngine searchEngine) {
    this.searchEngine = searchEngine;
  }


  public void researchTagsWithLastSearchTerm(TagsSearchResultListener listener) {
    search(lastSearchTerm, listener);
  }

  public void search(String searchTerm, final TagsSearchResultListener listener) {
    this.lastSearchTerm = searchTerm;

    if(isTagsFilterApplied()) {
      filterTags(listener);
    }
    else {
      if(tagsSearch != null && tagsSearch.isCompleted() == false) {
        tagsSearch.interrupt();
      }

      tagsSearch = new TagsSearch(searchTerm, new SearchCompletedListener<TagsSearchResults>() {
        @Override
        public void completed(TagsSearchResults results) {
          lastSearchTagsResult = results;
          lastFilterTagsResult = null;

          List<Tag> searchResults = createListFromCollection(results.getRelevantMatchesSorted());
          listener.completed(searchResults);
        }
      });

      searchEngine.searchTags(tagsSearch);
    }
  }

  protected boolean isTagsFilterApplied() {
//    return tagsFilter.size() > 0;
    return false; // TODO:
  }

  protected void filterTags(final TagsSearchResultListener listener) {
    if(isTagsFilterApplied() == false) {
      researchTagsWithLastSearchTerm(listener);
    }
    else {
      // TODO: there is currently no way to interrupt findAllEntriesHavingTheseTags()
      searchEngine.findAllEntriesHavingTheseTags(tagsFilter, lastSearchTerm, new SearchCompletedListener<FindAllEntriesHavingTheseTagsResult>() {
        @Override
        public void completed(FindAllEntriesHavingTheseTagsResult results) {
          lastFilterTagsResult = results;
          lastSearchTagsResult = null;

          List<Tag> searchResults = createListFromCollection(results.getTagsOnEntriesContainingFilteredTags());
          listener.completed(searchResults);
        }
      });
    }
  }


  public TagSearchResultState getTagSearchResultState(Tag tag) {
    if(lastSearchTagsResult != null && lastSearchTagsResult.getResults().size() > 0) {
      if(lastSearchTagsResult.isExactOrSingleMatchButNotOfLastResult(tag)) {
        return TagSearchResultState.EXACT_OR_SINGLE_MATCH_BUT_NOT_OF_LAST_RESULT;
      }
      else if(lastSearchTagsResult.isMatchButNotOfLastResult(tag)) {
        return TagSearchResultState.MATCH_BUT_NOT_OF_LAST_RESULT;
      }
      else if(lastSearchTagsResult.isExactMatchOfLastResult(tag)) {
        return TagSearchResultState.EXACT_MATCH_OF_LAST_RESULT;
      }
      else if(lastSearchTagsResult.isSingleMatchOfLastResult(tag)) {
        return TagSearchResultState.SINGLE_MATCH_OF_LAST_RESULT;
      }
    }

    return TagSearchResultState.DEFAULT;
  }

  public TagsSearcherButtonState getButtonStateForSearchResult() {
    if(StringUtils.isNullOrEmpty(lastSearchTerm)) {
      return TagsSearcherButtonState.DISABLED;
    }
    else if(lastSearchTagsResult != null) {
      if(lastSearchTagsResult.hasLastResultExactMatch()) {
        return TagsSearcherButtonState.TOGGLE_TAGS;
      }
      if(lastSearchTagsResult.getResults().size() > 1 || lastSearchTerm.endsWith(",")) {
        return TagsSearcherButtonState.TOGGLE_TAGS;
      }
    }
    else if(lastFilterTagsResult != null) {
      // TODO: implement
    }

    return TagsSearcherButtonState.CREATE_TAG;
  }


  protected <T> List<T> createListFromCollection(Collection<T> tagCollection) {
    List<T> tagList;

    if(tagCollection instanceof List) {
      tagList = (List<T>)tagCollection;
    }
    else {
      tagList = new ArrayList<T>(tagCollection); // TODO: use lazy loading list
    }

    return tagList;
  }

}

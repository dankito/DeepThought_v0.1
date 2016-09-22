package net.dankito.deepthought.data.search.ui;

import net.dankito.deepthought.data.model.Entry;
import net.dankito.deepthought.data.model.Tag;
import net.dankito.deepthought.data.search.ISearchEngine;
import net.dankito.deepthought.data.search.SearchBase;
import net.dankito.deepthought.data.search.SearchCompletedListener;
import net.dankito.deepthought.data.search.specific.FindAllEntriesHavingTheseTagsResult;
import net.dankito.deepthought.data.search.specific.TagsSearch;
import net.dankito.deepthought.data.search.specific.TagsSearchResults;

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


  public void getTagSearchResultState(Tag tag) {
    // TODO:
  }

  public List<Entry> getSearchResultEntriesForTag(Tag tag) {
    if(lastFilterTagsResult != null) {
      return createListFromCollection(lastFilterTagsResult.getEntriesHavingFilteredTags());
    }

    return createListFromCollection(tag.getEntries());
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

package net.deepthought.data.search;

import net.deepthought.Application;
import net.deepthought.data.listener.ApplicationListener;
import net.deepthought.data.model.DeepThought;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.Person;
import net.deepthought.data.model.Tag;
import net.deepthought.data.persistence.CombinedLazyLoadingList;
import net.deepthought.data.search.specific.FilterEntriesSearch;
import net.deepthought.data.search.specific.FilterReferenceBasesSearch;
import net.deepthought.data.search.specific.FilterTagsSearch;
import net.deepthought.data.search.specific.FilterTagsSearchResults;
import net.deepthought.data.search.specific.FindAllEntriesHavingTheseTagsResult;
import net.deepthought.data.search.specific.ReferenceBaseType;
import net.deepthought.util.Notification;
import net.deepthought.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by ganymed on 12/04/15.
 */
public abstract class SearchEngineBase implements ISearchEngine {

  protected DeepThought deepThought = null;


  public SearchEngineBase() {
    Application.addApplicationListener(applicationListener);
    this.deepThought = Application.getDeepThought();
    if(deepThought != null)
      deepThoughtChanged(null, deepThought);
  }


  public void close() {
    Application.removeApplicationListener(applicationListener);
    // nothing to do here, maybe in sub classes
  }


  @Override
  public void getEntriesWithoutTags(final SearchCompletedListener<Collection<Entry>> listener) {
    Application.getThreadPool().runTaskAsync(new Runnable() {
      @Override
      public void run() {
        List<Entry> entriesWithoutTags = new ArrayList<>();

        for (Entry entry : Application.getDeepThought().getEntries()) {
          if (entry.hasTags() == false)
            entriesWithoutTags.add(entry);
        }

        listener.completed(entriesWithoutTags);
      }
    });
  }

  @Override
  public void filterTags(final net.deepthought.data.search.specific.FilterTagsSearch search) {
    if(StringUtils.isNullOrEmpty(search.getSearchTerm())) { // no filter term specified -> return all Tags
      search.setResults(FilterTagsSearchResults.NoFilterSearchResults);
      search.fireSearchCompleted();
      return;
    }

    String lowerCaseFilter = search.getSearchTerm().toLowerCase();
    final String[] tagNamesToFilterFor = lowerCaseFilter.split(",");
    for(int i = 0; i < tagNamesToFilterFor.length; i++)
      tagNamesToFilterFor[i] = tagNamesToFilterFor[i].trim();

    Application.getThreadPool().runTaskAsync(new Runnable() {
      @Override
      public void run() {
        filterTags(search, tagNamesToFilterFor);
      }
    });
  }

  protected abstract void filterTags(FilterTagsSearch search, String[] tagNamesToFilterFor);

  @Override
  public void findAllEntriesHavingTheseTags(final Collection<Tag> tagsToFilterFor, final SearchCompletedListener<net.deepthought.data.search.specific.FindAllEntriesHavingTheseTagsResult> listener) {
    Application.getThreadPool().runTaskAsync(new Runnable() {
      @Override
      public void run() {
        findAllEntriesHavingTheseTagsAsync(tagsToFilterFor, listener);
      }
    });
  }

  protected abstract void findAllEntriesHavingTheseTagsAsync(Collection<Tag> tagsToFilterFor, SearchCompletedListener<FindAllEntriesHavingTheseTagsResult> listener);

  @Override
  public void filterEntries(final FilterEntriesSearch search) {
    if(StringUtils.isNullOrEmpty(search.getSearchTerm())) {
      search.setResults(Application.getDeepThought().getEntries());
      search.fireSearchCompleted();
      return;
    }

    String lowerCaseFilter = search.getSearchTerm().toLowerCase();
    // manually replacing accents as for such short text language often can't be identified correctly (LanguageAnalyzer normalize accents exactly like this)
    // TODO: àáâãä and å.
    lowerCaseFilter = lowerCaseFilter.replace('ä', 'a').replace('ö', 'o').replace('ü', 'u').replace("ß", "ss");
    lowerCaseFilter = lowerCaseFilter.replace('á', 'a').replace('à', 'o').replace('é', 'e').replace('è', 'e').replace('ô', 'o').replace('ç', 'c');

    final String[] termsToFilterFor = lowerCaseFilter.split(" ");

    Application.getThreadPool().runTaskAsync(new Runnable() {
      @Override
      public void run() {
        filterEntries(search, termsToFilterFor);
      }
    });
  }

  protected abstract void filterEntries(FilterEntriesSearch search, String[] termsToFilterFor);


  @Override
  public void filterReferenceBases(final FilterReferenceBasesSearch search) {
    if(StringUtils.isNullOrEmpty(search.getSearchTerm().trim())) { // no filter term specified -> return all ReferenceBases
      setReferenceBasesEmptyFilterSearchResult(search);
      return;
    }


    final String lowerCaseFilter = search.getSearchTerm().toLowerCase();
    final boolean filterForReferenceHierarchy = lowerCaseFilter.contains(",");

    if(search.getType() != ReferenceBaseType.All)
      filterOnlyOneTypeOfReferenceBase(search, lowerCaseFilter);
    else if(filterForReferenceHierarchy == false) {
      Application.getThreadPool().runTaskAsync(new Runnable() {
        @Override
        public void run() {
          filterAllReferenceBaseTypesForSameFilter(search, lowerCaseFilter);
        }
      });
    }
    else {
      filterEachReferenceBaseWithSeparateFilter(search, lowerCaseFilter);
    }
  }

  protected void filterOnlyOneTypeOfReferenceBase(FilterReferenceBasesSearch search, String lowerCaseFilter) {
    if(search.getType() == ReferenceBaseType.SeriesTitle)
      filterEachReferenceBaseWithSeparateFilter(search, lowerCaseFilter, null, null);
    else if(search.getType() == ReferenceBaseType.Reference)
      filterEachReferenceBaseWithSeparateFilter(search, null, lowerCaseFilter, null);
    else if(search.getType() == ReferenceBaseType.ReferenceSubDivision)
      filterEachReferenceBaseWithSeparateFilter(search, null, null, lowerCaseFilter);
  }

  protected void setReferenceBasesEmptyFilterSearchResult(FilterReferenceBasesSearch search) {
    if(search.getType() == ReferenceBaseType.SeriesTitle)
      search.setResults(new CombinedLazyLoadingList(Application.getDeepThought().getSeriesTitles()));
    else if(search.getType() == ReferenceBaseType.Reference)
      search.setResults(new CombinedLazyLoadingList(Application.getDeepThought().getReferences()));
    else if(search.getType() == ReferenceBaseType.ReferenceSubDivision)
      search.setResults(new CombinedLazyLoadingList(Application.getDeepThought().getReferenceSubDivisions()));
    else if(search.getType() == ReferenceBaseType.All)
      search.setResults(new CombinedLazyLoadingList(Application.getDeepThought().getSeriesTitles(), Application.getDeepThought().getReferences(),
        Application.getDeepThought().getReferenceSubDivisions()));

    search.fireSearchCompleted();
  }

  protected void filterEachReferenceBaseWithSeparateFilter(final FilterReferenceBasesSearch search, String lowerCaseFilter) {
    String seriesTitleFilter = null, referenceFilter = null, referenceSubDivisionFilter = null;
    String[] parts = lowerCaseFilter.split(",");

    seriesTitleFilter = parts[0].trim();
    if(seriesTitleFilter.length() == 0) seriesTitleFilter = null;

    if(parts.length > 1) {
      referenceFilter = parts[1].trim();
      if(referenceFilter.length() == 0) referenceFilter = null;
    }

    if(parts.length > 2) {
      referenceSubDivisionFilter = parts[2].trim();
      if(referenceSubDivisionFilter.length() == 0) referenceSubDivisionFilter = null;
    }

    final String finalSeriesTitleFilter = seriesTitleFilter;
    final String finalReferenceFilter = referenceFilter;
    final String finalReferenceSubDivisionFilter = referenceSubDivisionFilter;

    Application.getThreadPool().runTaskAsync(new Runnable() {
      @Override
      public void run() {
        filterEachReferenceBaseWithSeparateFilter(search, finalSeriesTitleFilter, finalReferenceFilter, finalReferenceSubDivisionFilter);
      }
    });
  }

  protected abstract void filterAllReferenceBaseTypesForSameFilter(FilterReferenceBasesSearch search, String referenceBaseFilter);

  protected abstract void filterEachReferenceBaseWithSeparateFilter(FilterReferenceBasesSearch search, String seriesTitleFilter, String referenceFilter, String FilterReferenceBasesreferenceSubDivisionFilter);

  @Override
  public void filterPersons(final Search<Person> search) {
    if(StringUtils.isNullOrEmpty(search.getSearchTerm())) {
      search.setResults(Application.getDeepThought().getPersons());
      search.fireSearchCompleted();
      return;
    }

    final String lowerCaseFilter = search.getSearchTerm().toLowerCase();
    final boolean filterForFirstAndLastName = lowerCaseFilter.contains(",");

    if(filterForFirstAndLastName == false) {
      Application.getThreadPool().runTaskAsync(new Runnable() {
        @Override
        public void run() {
          filterPersons(search, lowerCaseFilter);
        }
      });
    }
    else {
      final String lastNameFilter = lowerCaseFilter.substring(0, lowerCaseFilter.indexOf(",")).trim();
      final String firstNameFilter = lowerCaseFilter.substring(lowerCaseFilter.indexOf(",") + 1).trim();

      Application.getThreadPool().runTaskAsync(new Runnable() {
        @Override
        public void run() {
          filterPersons(search, lastNameFilter, firstNameFilter);
        }
      });
    }

//    search.fireSearchCompleted();
  }

  protected abstract void filterPersons(Search<Person> search, String personFilter);

  protected abstract void filterPersons(Search<Person> search, String lastNameFilter, String firstNameFilter);


  protected ApplicationListener applicationListener = new ApplicationListener() {
    @Override
    public void deepThoughtChanged(DeepThought deepThought) {
      DeepThought previousDeepThought = SearchEngineBase.this.deepThought;
      SearchEngineBase.this.deepThought = deepThought;
      SearchEngineBase.this.deepThoughtChanged(previousDeepThought, deepThought);
    }

    @Override
    public void notification(Notification notification) {

    }
  };

  protected void deepThoughtChanged(DeepThought previousDeepThought, DeepThought newDeepThought) {

  }

}

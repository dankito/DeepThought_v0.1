package net.deepthought.data.search;

import net.deepthought.Application;
import net.deepthought.data.listener.ApplicationListener;
import net.deepthought.data.model.DeepThought;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.Person;
import net.deepthought.data.model.Tag;
import net.deepthought.data.persistence.CombinedLazyLoadingList;
import net.deepthought.util.Notification;
import net.deepthought.util.StringUtils;

import java.util.ArrayList;
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
  public void getEntriesWithoutTags(final SearchCompletedListener<Entry> listener) {
    new Thread(new Runnable() {
      @Override
      public void run() {
        List<Entry> entriesWithoutTags = new ArrayList<>();

        for(Entry entry : Application.getDeepThought().getEntries()) {
          if(entry.hasTags() == false)
            entriesWithoutTags.add(entry);
        }

        listener.completed(entriesWithoutTags);
      }
    }).start();
  }

  @Override
  public void filterTags(final Search<Tag> search) {
    if(StringUtils.isNullOrEmpty(search.getSearchTerm())) { // no filter term specified -> return all Tags
      search.setResults(Application.getDeepThought().getTags());
      search.fireSearchCompleted();
      return;
    }

    String lowerCaseFilter = search.getSearchTerm().toLowerCase();
    final String[] tagNamesToFilterFor = lowerCaseFilter.split(",");
    for(int i = 0; i < tagNamesToFilterFor.length; i++)
      tagNamesToFilterFor[i] = tagNamesToFilterFor[i].trim();

    new Thread(new Runnable() {
      @Override
      public void run() {
        filterTags(search, tagNamesToFilterFor);
      }
    }).start();
  }

  protected abstract void filterTags(Search<Tag> search, String[] tagNamesToFilterFor);

  @Override
  public void filterEntries(final FilterEntriesSearch search) {
    if(StringUtils.isNullOrEmpty(search.getSearchTerm())) {
      search.setResults(Application.getDeepThought().getEntries());
      search.fireSearchCompleted();
      return;
    }

    String lowerCaseFilter = search.getSearchTerm().toLowerCase();
    final String contentFilter = search.filterContent() ? lowerCaseFilter : null;
    final String abstractFilter = search.filterAbstract() ? lowerCaseFilter : null;

    new Thread(new Runnable() {
      @Override
      public void run() {
        filterEntries(search, contentFilter, abstractFilter);
      }
    }).start();
  }

  protected abstract void filterEntries(FilterEntriesSearch search, String contentFilter, String abstractFilter);


  @Override
  public void filterReferenceBases(final Search search) {
    if(StringUtils.isNullOrEmpty(search.getSearchTerm()) || StringUtils.isNullOrEmpty(search.getSearchTerm().replace("", ""))) { // no filter term specified -> return all  ReferenceBases
      search.setResults(new CombinedLazyLoadingList(Application.getDeepThought().getSeriesTitles(), Application.getDeepThought().getReferences(),
          Application.getDeepThought().getReferenceSubDivisions()));

      search.fireSearchCompleted();
      return;
    }


    final String lowerCaseFilter = search.getSearchTerm().toLowerCase();
    final boolean filterForReferenceHierarchy = lowerCaseFilter.contains(",");

    if(filterForReferenceHierarchy == false) {
      new Thread(new Runnable() {
        @Override
        public void run() {
          filterAllReferenceBaseTypesForSameFilter(search, lowerCaseFilter);
        }
      }).start();
    }
    else {
      filterEachReferenceBaseWithSeparateFilter(search, lowerCaseFilter);
    }
  }

  protected void filterEachReferenceBaseWithSeparateFilter(final Search search, String lowerCaseFilter) {
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

    new Thread(new Runnable() {
      @Override
      public void run() {
        filterEachReferenceBaseWithSeparateFilter(search, finalSeriesTitleFilter, finalReferenceFilter, finalReferenceSubDivisionFilter);
      }
    }).start();
  }

  protected abstract void filterAllReferenceBaseTypesForSameFilter(Search search, String referenceBaseFilter);

  protected abstract void filterEachReferenceBaseWithSeparateFilter(Search search, String seriesTitleFilter, String referenceFilter, String referenceSubDivisionFilter);

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
      new Thread(new Runnable() {
        @Override
        public void run() {
          filterPersons(search, lowerCaseFilter);
        }
      }).start();
    }
    else {
      final String lastNameFilter = lowerCaseFilter.substring(0, lowerCaseFilter.indexOf(",")).trim();
      final String firstNameFilter = lowerCaseFilter.substring(lowerCaseFilter.indexOf(",") + 1).trim();

      new Thread(new Runnable() {
        @Override
        public void run() {
          filterPersons(search, lastNameFilter, firstNameFilter);
        }
      }).start();
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

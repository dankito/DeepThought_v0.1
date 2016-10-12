package net.dankito.deepthought.data.search;

import net.dankito.deepthought.data.model.Entry;
import net.dankito.deepthought.data.model.Person;
import net.dankito.deepthought.data.model.SeriesTitle;
import net.dankito.deepthought.data.model.ui.EntriesWithoutTagsSystemTag;
import net.dankito.deepthought.data.search.specific.CategoriesSearch;
import net.dankito.deepthought.data.search.specific.FilesSearch;
import net.dankito.deepthought.Application;
import net.dankito.deepthought.data.model.Reference;
import net.dankito.deepthought.data.model.ReferenceSubDivision;
import net.dankito.deepthought.data.model.Tag;
import net.dankito.deepthought.data.persistence.CombinedLazyLoadingList;
import net.dankito.deepthought.data.persistence.LazyLoadingList;
import net.dankito.deepthought.data.search.specific.EntriesSearch;
import net.dankito.deepthought.data.search.specific.ReferenceBasesSearch;
import net.dankito.deepthought.data.search.specific.TagsSearch;
import net.dankito.deepthought.data.search.specific.FindAllEntriesHavingTheseTagsResult;
import net.dankito.deepthought.data.search.specific.ReferenceBaseType;
import net.dankito.deepthought.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by ganymed on 12/04/15.
 */
public class InMemorySearchEngine extends SearchEngineBase {

  @Override
  public void getEntriesWithTag(Tag tag, final SearchCompletedListener<Collection<Entry>> listener) {
    if(tag instanceof EntriesWithoutTagsSystemTag) {
      getEntriesWithoutTags(listener);
    }
    else {
      // TODO
    }
  }

  protected void getEntriesWithoutTags(final SearchCompletedListener<Collection<Entry>> listener) {
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


  protected void filterTags(TagsSearch search, String[] tagNamesToFilterFor) {
    // TODO: may implement one day (no need for it right now)
//    for(Tag tag : Application.getDeepThought().getTags()) {
//      if(search.isInterrupted())
//        return;
//
//      String lowerCaseTagName = tag.getName().toLowerCase();
//
//      for (String part : tagNamesToFilterFor) {
//        if (lowerCaseTagName.contains(part)) {
//          search.addResult(tag); // Filter matches Tag's name
//          break;
//        }
//      }
//    }

    search.fireSearchCompleted();
  }

  protected void findAllEntriesHavingTheseTagsAsync(Collection<Tag> tagsToFilterFor, String[] tagNamesToFilterFor, SearchCompletedListener<FindAllEntriesHavingTheseTagsResult> listener) {
    Collection<Entry> entriesHavingFilteredTags = new LazyLoadingList<Entry>(Entry.class);
    Set<Tag> tagsOnEntriesContainingFilteredTags = new HashSet<>();

    for (Tag filteredTag : tagsToFilterFor) {
      for (Entry entry : filteredTag.getEntries()) {
        if (entry.hasTags(tagsToFilterFor)) {
          entriesHavingFilteredTags.add(entry);
          tagsOnEntriesContainingFilteredTags.addAll(entry.getTags());
        }
      }
    }

    listener.completed(new FindAllEntriesHavingTheseTagsResult(entriesHavingFilteredTags, tagsOnEntriesContainingFilteredTags));
  }

  @Override
  protected void filterEntries(EntriesSearch search, String[] termsToFilterFor) {
    for(Entry entry : Application.getDeepThought().getEntries()) {
      if(search.isInterrupted())
        return;

      for(String term : termsToFilterFor) {
        if ((search.filterContent() && entry.getContentAsPlainText().toLowerCase().contains(term)) ||
            (search.filterAbstract() && entry.getAbstractAsPlainText().toLowerCase().contains(term)))
          search.addResult(entry);
      }
    }

    search.fireSearchCompleted();
  }

  @Override
  public void searchCategories(CategoriesSearch search) {
    // TODO
    search.fireSearchCompleted();
  }

  @Override
  public void searchFiles(FilesSearch search) {
    // TODO
    search.fireSearchCompleted();
  }

  @Override
  protected void searchAllReferenceBaseTypesForSameFilter(ReferenceBasesSearch search, String referenceBaseFilter) {
    if(StringUtils.isNullOrEmpty(search.getSearchTerm().trim())) {
      setReferenceBasesEmptyFilterSearchResult(search);
      return;
    }

    for(SeriesTitle seriesTitle : Application.getDeepThought().getSeriesTitles()) {
      if(search.isInterrupted())
        return;

      if(seriesTitle.getTextRepresentation().toLowerCase().contains(referenceBaseFilter))
        search.addResult(seriesTitle);
    }

    for(Reference reference : Application.getDeepThought().getReferences()) {
      if(search.isInterrupted())
        return;

      if(reference.getTextRepresentation() != null && reference.getTextRepresentation().toLowerCase().contains(referenceBaseFilter))
        search.addResult(reference);

      for(ReferenceSubDivision subDivision : reference.getSubDivisions()) {
        if(search.isInterrupted())
          return;

        if(subDivision.getTextRepresentation().toLowerCase().contains(referenceBaseFilter))
          search.addResult(subDivision);
      }
    }

    search.fireSearchCompleted();
  }

  @Override
  protected void searchEachReferenceBaseWithSeparateSearchTerm(ReferenceBasesSearch search, String seriesTitleFilter, String referenceFilter, String referenceSubDivisionFilter) {
    if(StringUtils.isNullOrEmpty(search.getSearchTerm().trim())) {
      setReferenceBasesEmptyFilterSearchResult(search);
      return;
    }

    if(seriesTitleFilter != null &&
        referenceFilter == null && referenceSubDivisionFilter == null) // cannot fulfill all filters
      filterSeriesTitles(search, seriesTitleFilter);

    if(referenceFilter != null)
      filterReferences(search, seriesTitleFilter, referenceFilter, referenceSubDivisionFilter);

    search.fireSearchCompleted();
  }

  protected void filterSeriesTitles(ReferenceBasesSearch search, String seriesTitleFilter) {
    for(SeriesTitle seriesTitle : Application.getDeepThought().getSeriesTitles()) {
      if(search.isInterrupted())
        return;

      if(seriesTitle.getTextRepresentation().toLowerCase().contains(seriesTitleFilter))
        search.addResult(seriesTitle);
    }

    search.fireSearchCompleted();
  }

  protected void filterReferences(ReferenceBasesSearch search, String seriesTitleFilter, String referenceFilter, String referenceSubDivisionFilter) {
    for(Reference reference : Application.getDeepThought().getReferences()) {
      if(search.isInterrupted())
        return;

      if(referenceSubDivisionFilter == null && reference.getTextRepresentation().toLowerCase().contains(referenceFilter) && // cannot fulfill all filters as ReferenceSubDivisionFilter is set and it isn't a ReferenceSubDivision
          ((seriesTitleFilter == null && reference.getSeries() == null) ||
              seriesTitleFilter != null && reference.getSeries() != null && reference.getSeries().getTextRepresentation().toLowerCase().contains(seriesTitleFilter)))
        search.addResult(reference);

      if(referenceSubDivisionFilter != null)
        filterReferenceSubDivisions(search, reference, seriesTitleFilter, referenceFilter, referenceSubDivisionFilter);
    }

    search.fireSearchCompleted();
  }

  protected void filterReferenceSubDivisions(ReferenceBasesSearch search, Reference reference, String seriesTitleFilter, String referenceFilter, String referenceSubDivisionFilter) {
    for(ReferenceSubDivision subDivision : reference.getSubDivisions()) {
      if(search.isInterrupted())
        return;

      if(subDivision.getTextRepresentation().toLowerCase().contains(referenceSubDivisionFilter) &&
          ((referenceFilter == null && subDivision.getReference() == null) ||
              (referenceFilter != null && subDivision.getReference() != null && subDivision.getReference().getTextRepresentation().toLowerCase().contains(referenceFilter))) &&
          ((seriesTitleFilter == null && (subDivision.getReference() == null || subDivision.getReference().getSeries() == null)) ||
              (seriesTitleFilter != null && subDivision.getReference() != null && subDivision.getReference().getSeries() != null &&
                  subDivision.getReference().getSeries().getTextRepresentation().toLowerCase().contains(seriesTitleFilter))))
        search.addResult(subDivision);
    }

    search.fireSearchCompleted();
  }

  protected void setReferenceBasesEmptyFilterSearchResult(ReferenceBasesSearch search) {
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

  public void searchForReferenceOfDate(String optionalSeriesTitleTitle, Search<Reference> search) {
    // TODO:
    search.callCompletedListener();
  }


  @Override
  protected void searchPersons(Search<Person> search, String personFilter) {
    for(Person person : Application.getDeepThought().getPersons()) {
      if(search.isInterrupted())
        return;

      if(person.getLastName().toLowerCase().contains(personFilter) || person.getFirstName().toLowerCase().contains(personFilter)) {
        search.addResult(person);
      }
    }

    search.fireSearchCompleted();
  }

  @Override
  protected void searchPersons(Search<Person> search, String lastNameFilter, String firstNameFilter) {
    for(Person person : Application.getDeepThought().getPersons()) {
      if(search.isInterrupted())
        return;

      if((lastNameFilter.length() == 0 || person.getLastName().toLowerCase().contains(lastNameFilter)) &&
          (firstNameFilter.length() == 0 || person.getFirstName().toLowerCase().contains(firstNameFilter))) {
        search.addResult(person);
      }
    }

    search.fireSearchCompleted();
  }

}

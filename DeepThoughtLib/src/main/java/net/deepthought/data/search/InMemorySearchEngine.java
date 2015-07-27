package net.deepthought.data.search;

import net.deepthought.Application;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.Person;
import net.deepthought.data.model.Reference;
import net.deepthought.data.model.ReferenceSubDivision;
import net.deepthought.data.model.SeriesTitle;
import net.deepthought.data.model.Tag;

import java.util.Collection;
import java.util.Set;

/**
 * Created by ganymed on 12/04/15.
 */
public class InMemorySearchEngine extends SearchEngineBase {

  protected void filterTags(FilterTagsSearch search, String[] tagNamesToFilterFor) {
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

  public void findAllEntriesHavingTheseTags(Collection<Tag> tagsToFilterFor, Collection<Entry> entriesHavingFilteredTags, Set<Tag> tagsOnEntriesContainingFilteredTags) {
    for (Tag filteredTag : tagsToFilterFor) {
      for (Entry entry : filteredTag.getEntries()) {
        if (entry.hasTags(tagsToFilterFor)) {
          entriesHavingFilteredTags.add(entry);
          tagsOnEntriesContainingFilteredTags.addAll(entry.getTags());
        }
      }
    }
  }

  @Override
  protected void filterEntries(FilterEntriesSearch search, String contentFilter, String abstractFilter) {
    for(Entry entry : Application.getDeepThought().getEntries()) {
      if(search.isInterrupted())
        return;

      if((search.filterContent() && entry.getContentAsPlainText().toLowerCase().contains(contentFilter)) ||
          (search.filterAbstract() && entry.getAbstractAsPlainText().toLowerCase().contains(abstractFilter)))
        search.addResult(entry);
    }

    search.fireSearchCompleted();
  }

  @Override
  protected void filterAllReferenceBaseTypesForSameFilter(Search search, String referenceBaseFilter) {
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
  protected void filterEachReferenceBaseWithSeparateFilter(Search search, String seriesTitleFilter, String referenceFilter, String referenceSubDivisionFilter) {
    if(seriesTitleFilter != null &&
        referenceFilter == null && referenceSubDivisionFilter == null) // cannot fulfill all filters
      filterSeriesTitles(search, seriesTitleFilter);

    if(referenceFilter != null)
      filterReferences(search, seriesTitleFilter, referenceFilter, referenceSubDivisionFilter);

    search.fireSearchCompleted();
  }

  protected void filterSeriesTitles(Search search, String seriesTitleFilter) {
    for(SeriesTitle seriesTitle : Application.getDeepThought().getSeriesTitles()) {
      if(search.isInterrupted())
        return;

      if(seriesTitle.getTextRepresentation().toLowerCase().contains(seriesTitleFilter))
        search.addResult(seriesTitle);
    }

    search.fireSearchCompleted();
  }

  protected void filterReferences(Search search, String seriesTitleFilter, String referenceFilter, String referenceSubDivisionFilter) {
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

  protected void filterReferenceSubDivisions(Search search, Reference reference, String seriesTitleFilter, String referenceFilter, String referenceSubDivisionFilter) {
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

  @Override
  protected void filterPersons(Search<Person> search, String personFilter) {
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
  protected void filterPersons(Search<Person> search, String lastNameFilter, String firstNameFilter) {
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

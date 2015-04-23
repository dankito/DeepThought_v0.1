package net.deepthought.data.search;

import net.deepthought.Application;
import net.deepthought.data.model.Category;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.Person;
import net.deepthought.data.model.Reference;
import net.deepthought.data.model.ReferenceSubDivision;
import net.deepthought.data.model.SeriesTitle;
import net.deepthought.data.model.Tag;
import net.deepthought.data.persistence.db.UserDataEntity;
import net.deepthought.util.StringUtils;

/**
 * Created by ganymed on 12/04/15.
 */
public abstract class AbstractSearchEngine implements ISearchEngine {

  public void close() {
    // nothing to do
  }


  @Override
  public void indexEntity(UserDataEntity entity) {
    if(entity instanceof Entry)
      indexEntry((Entry)entity);
    else if(entity instanceof Tag)
      indexTag((Tag)entity);
    else if(entity instanceof Category)
      indexCategory((Category)entity);
    else if(entity instanceof Person)
      indexPerson((Person)entity);
    else if(entity instanceof SeriesTitle)
      indexSeriesTitle((SeriesTitle)entity);
    else if(entity instanceof Reference)
      indexReference((Reference)entity);
    else if(entity instanceof ReferenceSubDivision)
      indexReferenceSubDivision((ReferenceSubDivision)entity);
  }

  @Override
  public void updateIndexForEntity(UserDataEntity entity, String propertyName) {
    if(entity instanceof Entry)
      updateIndexForEntry((Entry)entity, propertyName);
    else if(entity instanceof Tag)
      updateIndexForTag((Tag)entity, propertyName);
    else if(entity instanceof Category)
      updateIndexForCategory((Category)entity, propertyName);
    else if(entity instanceof Person)
      updateIndexForPerson((Person)entity, propertyName);
    else if(entity instanceof SeriesTitle)
      updateIndexForSeriesTitle((SeriesTitle)entity, propertyName);
    else if(entity instanceof Reference)
      updateIndexForReference((Reference)entity, propertyName);
    else if(entity instanceof ReferenceSubDivision)
      updateIndexForReferenceSubDivision((ReferenceSubDivision)entity, propertyName);
  }

  protected void indexEntry(Entry entry) { /* may be overwritten in sub class */ }
  protected void updateIndexForEntry(Entry entry, String propertyName) { /* may be overwritten in sub class */ }

  protected void indexTag(Tag tag) { /* may be overwritten in sub class */ }
  protected void updateIndexForTag(Tag tag, String propertyName) { /* may be overwritten in sub class */ }

  protected void indexCategory(Category category) { /* may be overwritten in sub class */ }
  protected void updateIndexForCategory(Category category, String propertyName) { /* may be overwritten in sub class */ }

  protected void indexPerson(Person person) { /* may be overwritten in sub class */ }
  protected void updateIndexForPerson(Person person, String propertyName) { /* may be overwritten in sub class */ }

  protected void indexSeriesTitle(SeriesTitle seriesTitle) { /* may be overwritten in sub class */ }
  protected void updateIndexForSeriesTitle(SeriesTitle seriesTitle, String propertyName) { /* may be overwritten in sub class */ }

  protected void indexReference(Reference reference) { /* may be overwritten in sub class */ }
  protected void updateIndexForReference(Reference reference, String propertyName) { /* may be overwritten in sub class */ }

  protected void indexReferenceSubDivision(ReferenceSubDivision subDivision) { /* may be overwritten in sub class */ }
  protected void updateIndexForReferenceSubDivision(ReferenceSubDivision subDivision, String propertyName) { /* may be overwritten in sub class */ }


  @Override
  public void filterTags(Search<Tag> search) {
    if(StringUtils.isNullOrEmpty(search.getSearchTerm())) { // no filter term specified -> return all Tags
      search.addResults(Application.getDeepThought().getTags());
      search.fireSearchCompleted();
      return;
    }

    String lowerCaseFilter = search.getSearchTerm().toLowerCase();
    String[] tagNamesToFilterFor = lowerCaseFilter.split(",");
    for(int i = 0; i < tagNamesToFilterFor.length; i++)
      tagNamesToFilterFor[i] = tagNamesToFilterFor[i].trim();

    filterTags(search, tagNamesToFilterFor);
  }

  protected abstract void filterTags(Search<Tag> search, String[] tagNamesToFilterFor);

  @Override
  public void filterEntries(FilterEntriesSearch search) {
    if(StringUtils.isNullOrEmpty(search.getSearchTerm())) {
      search.addResults(Application.getDeepThought().getEntries());
      search.fireSearchCompleted();
      return;
    }

    String lowerCaseFilter = search.getSearchTerm().toLowerCase();
    String contentFilter = search.filterContent() ? lowerCaseFilter : null;
    String abstractFilter = search.filterAbstract() ? lowerCaseFilter : null;
    filterEntries(search, contentFilter, abstractFilter);
  }

  protected abstract void filterEntries(FilterEntriesSearch search, String contentFilter, String abstractFilter);


  @Override
  public void filterReferenceBases(Search search) {
    if(StringUtils.isNullOrEmpty(search.getSearchTerm()) || ",".equals(search.getSearchTerm())) { // no filter term specified -> return all ReferenceBases
      search.addResults(Application.getDeepThought().getSeriesTitles());
      for(Reference reference : Application.getDeepThought().getReferences()) {
        search.addResult(reference);
        search.addResults(reference.getSubDivisions());
      }

      search.fireSearchCompleted();
      return;
    }


    String lowerCaseFilter = search.getSearchTerm().toLowerCase();
    final boolean filterForReferenceHierarchy = lowerCaseFilter.contains(",");

    if(filterForReferenceHierarchy == false)
      filterAllReferenceBasesForSameFilter(search, lowerCaseFilter);
    else {
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

      filterReferenceBases(search, seriesTitleFilter, referenceFilter, referenceSubDivisionFilter);
    }

//    search.fireSearchCompleted();
  }

  protected abstract void filterAllReferenceBasesForSameFilter(Search search, String referenceBaseFilter);

  protected abstract void filterReferenceBases(Search search, String seriesTitleFilter, String referenceFilter, String referenceSubDivisionFilter);

  @Override
  public void filterPersons(Search<Person> search) {
    if(StringUtils.isNullOrEmpty(search.getSearchTerm())) {
      search.addResults(Application.getDeepThought().getPersons());
      search.fireSearchCompleted();
      return;
    }

    String lowerCaseFilter = search.getSearchTerm().toLowerCase();
    final boolean filterForFirstAndLastName = lowerCaseFilter.contains(",");
    String lastNameFilter, firstNameFilter = null;

    if(filterForFirstAndLastName == false)
      filterPersons(search, lowerCaseFilter);
    else {
      lastNameFilter = lowerCaseFilter.substring(0, lowerCaseFilter.indexOf(",")).trim();
      firstNameFilter = lowerCaseFilter.substring(lowerCaseFilter.indexOf(","));
      firstNameFilter = firstNameFilter.substring(1).trim();

      filterPersons(search, lastNameFilter, firstNameFilter);
    }

//    search.fireSearchCompleted();
  }

  protected abstract void filterPersons(Search<Person> search, String personFilter);

  protected abstract void filterPersons(Search<Person> search, String lastNameFilter, String firstNameFilter);

}

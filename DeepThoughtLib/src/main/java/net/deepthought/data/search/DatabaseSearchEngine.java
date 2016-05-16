package net.deepthought.data.search;

import net.deepthought.Application;
import net.deepthought.data.model.Category;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.Person;
import net.deepthought.data.model.Reference;
import net.deepthought.data.model.ReferenceBase;
import net.deepthought.data.model.Tag;
import net.deepthought.data.persistence.IEntityManager;
import net.deepthought.data.persistence.LazyLoadingList;
import net.deepthought.data.persistence.db.TableConfig;
import net.deepthought.data.search.specific.CategoriesSearch;
import net.deepthought.data.search.specific.EntriesSearch;
import net.deepthought.data.search.specific.FilesSearch;
import net.deepthought.data.search.specific.ReferenceBasesSearch;
import net.deepthought.data.search.specific.TagsSearch;
import net.deepthought.data.search.specific.TagsSearchResult;
import net.deepthought.data.search.specific.FindAllEntriesHavingTheseTagsResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by ganymed on 04/05/15.
 */
public class DatabaseSearchEngine extends SearchEngineBase {

  private final static Logger log = LoggerFactory.getLogger(DatabaseSearchEngine.class);


  @Override
  public void getEntriesWithoutTags(SearchCompletedListener<Collection<Entry>> listener) {
//    IEntityManager entityManager = Application.getEntityManager();

//    try {
////      List result = entityManager.doNativeQuery("SELECT * from " + TableConfig.EntryTableName + " WHERE " + TableConfig.BaseEntityIdColumnName + " NOT IN " +
////          "(SELECT DISTINCT " + TableConfig.EntryTagJoinTableEntryIdColumnName + " FROM " + TableConfig.EntryTagJoinTableName + ")");
////      listener.completed(Arrays.asList(new Entry[result.size()]));
//
//      List<Entry> result = entityManager.queryEntities(Entry.class, TableConfig.BaseEntityIdColumnName + " NOT IN " +
//          "(SELECT DISTINCT " + TableConfig.EntryTagJoinTableEntryIdColumnName + " FROM " + TableConfig.EntryTagJoinTableName + ")");
//      listener.completed(result);
//    } catch(Exception ex) {
//      log.error("Could not query for Entries without Tags", ex);
//    }

    try {
      Collection<Long> entriesWithoutTagsIds = new ArrayList<>();
      List<String[]> result = Application.getEntityManager().doNativeQuery("SELECT " + TableConfig.BaseEntityIdColumnName + " from " + TableConfig.EntryTableName +
          " WHERE " + TableConfig.EntryDeepThoughtJoinColumnName + " = " + Application.getDeepThought().getId() + " AND " +
          TableConfig.BaseEntityIdColumnName + " NOT IN " +
          "(SELECT DISTINCT " + TableConfig.EntryTagJoinTableEntryIdColumnName + " FROM " + TableConfig.EntryTagJoinTableName + ")" +
          " ORDER BY " + TableConfig.BaseEntityIdColumnName + " DESC");
      for(String[] resultEntry : result)
        entriesWithoutTagsIds.add(Long.parseLong(resultEntry[0]));
      listener.completed(new LazyLoadingList<Entry>(Entry.class, entriesWithoutTagsIds));
    } catch(Exception ex) {
      log.error("Could not retrieve IDs of Entries without Tags", ex);
    }
  }

  @Override
  protected void filterTags(TagsSearch search, String[] tagNamesToFilterFor) {
    IEntityManager entityManager = Application.getEntityManager();
    List<TagsSearchResult> results = new ArrayList<>();

    String queryPrefix = "SELECT " + TableConfig.BaseEntityIdColumnName + " FROM " + TableConfig.TagTableName + " WHERE " +
        TableConfig.TagDeepThoughtJoinColumnName + " = " + deepThought.getId() + " AND (" + TableConfig.TagNameColumnName + " LIKE '%";

    String querySuffix = "%'" + ") " + "ORDER BY " + TableConfig.TagNameColumnName + " COLLATE NOCASE";

    for(int i = 0; i < tagNamesToFilterFor.length; i++) {
      if(search.isInterrupted())
        return;

      try {
        String query = queryPrefix + tagNamesToFilterFor[i] + querySuffix;
        if(search.isInterrupted())
          return;

        log.debug("Searching Tags for " + search.getSearchTerm() + " with query: " + query);
        List<String[]> queryResults = entityManager.doNativeQuery(query);
        List<Long> ids = new ArrayList<>(queryResults.size());
        for(String[] result : queryResults) {
          ids.add(Long.parseLong(result[0]));
        }

        if(search.isInterrupted())
          return;

        search.addResult(new TagsSearchResult(tagNamesToFilterFor[i], new LazyLoadingList<Tag>(Tag.class, ids)));
      } catch(Exception ex) {
        log.error("Could not query for Entries without Tags", ex);
      }
    }

    search.fireSearchCompleted();
  }

  @Override
  protected void filterEntries(EntriesSearch search, String[] termsToFilterFor) {
    search.fireSearchCompleted(); // TODO
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
    IEntityManager entityManager = Application.getEntityManager();

    String query = "SELECT " + TableConfig.BaseEntityIdColumnName + " FROM " + TableConfig.ReferenceBaseTableName + " WHERE (" +
        TableConfig.ReferenceBaseTitleColumnName + " LIKE '%" + referenceBaseFilter + "%' OR " + TableConfig.ReferenceBaseSubTitleColumnName + " LIKE '%" + referenceBaseFilter + "%')";

    try {
      List<String[]> results = entityManager.doNativeQuery(query);
      List<Long> ids = new ArrayList<>(results.size());
      for(String[] result : results) {
        ids.add(Long.parseLong(result[0]));
      }
      search.setResults(new LazyLoadingList<ReferenceBase>(ReferenceBase.class, ids));
    } catch(Exception ex) {
      log.error("Could not query for Entries without Tags", ex);
    }

    search.fireSearchCompleted();
  }

  // there's no way to complete this search with SQL -> do it in memory (very slow for a large amount of ReferenceBases) or with Lucene
  @Override
  protected void searchEachReferenceBaseWithSeparateSearchTerm(ReferenceBasesSearch search, String seriesTitleFilter, String referenceFilter, String referenceSubDivisionFilter) {
//    IEntityManager entityManager = Application.getEntityManager();
//
//    String query = "SELECT " + TableConfig.BaseEntityIdColumnName + " FROM " + TableConfig.ReferenceBaseTableName + " WHERE (";
//
//    if(seriesTitleFilter != null)
//      query += "(" + TableConfig.ReferenceBaseDiscriminatorColumnName + " = '" + TableConfig.SeriesTitleDiscriminatorValue + "' AND (" +
//            TableConfig.ReferenceBaseTitleColumnName + " LIKE '%" + seriesTitleFilter + "%' OR " + TableConfig.ReferenceBaseSubTitleColumnName + " LIKE '%" + seriesTitleFilter + "%'))";
//
//    if(referenceFilter != null) {
//      if(seriesTitleFilter != null)
//        query += " AND ";
//      query += "(" + TableConfig.ReferenceBaseDiscriminatorColumnName + " = '" + TableConfig.ReferenceDiscriminatorValue + "' AND (" +
//          TableConfig.ReferenceBaseTitleColumnName + " LIKE '%" + referenceFilter + "%' OR " + TableConfig.ReferenceBaseSubTitleColumnName + " LIKE '%" + referenceFilter + "%'))";
//    }
//
//    if(referenceSubDivisionFilter != null) {
//      if(seriesTitleFilter != null || referenceFilter != null)
//        query += " AND ";
//      query += "(" + TableConfig.ReferenceBaseDiscriminatorColumnName + " = '" + TableConfig.ReferenceSubDivisionDiscriminatorValue + "' AND (" +
//          TableConfig.ReferenceBaseTitleColumnName + " LIKE '%" + referenceSubDivisionFilter + "%' OR " +
//          TableConfig.ReferenceBaseSubTitleColumnName + " LIKE '%" + referenceSubDivisionFilter + "%'))";
//    }
//
//    query += " )";
//
//    try {
//      List<String[]> results = entityManager.doNativeQuery(query);
//      for(String[] result : results) {
//        Long id = Long.parseLong(result[0]);
//        search.addResult(entityManager.getEntityById(ReferenceBase.class, id));
//      }
//    } catch(Exception ex) {
//      log.error("Could not query for Entries without Tags", ex);
//    }

    search.fireSearchCompleted();
  }

  public void searchForReferenceOfDate(String optionalSeriesTitleTitle, Search<Reference> search) {
    // TODO:
    search.callCompletedListener();
  }

  @Override
  protected void searchPersons(Search<Person> search, String personFilter) {
    searchPersons(search, personFilter, personFilter, false);
  }

  @Override
  protected void searchPersons(Search<Person> search, String lastNameFilter, String firstNameFilter) {
    searchPersons(search, lastNameFilter, firstNameFilter, true);
  }

  protected void searchPersons(Search<Person> search, String lastNameFilter, String firstNameFilter, boolean mustFitBothFilters) {
    // TODO: escape lastNameFilter and firstNameFilter (e.g. if they contain: ' )
    IEntityManager entityManager = Application.getEntityManager();

    String query = "SELECT " + TableConfig.BaseEntityIdColumnName + " FROM " + TableConfig.PersonTableName + " WHERE " +
        TableConfig.PersonDeepThoughtJoinColumnName + " = " + deepThought.getId() + " AND (";

    query += TableConfig.PersonLastNameColumnName + " LIKE '%" + lastNameFilter + "%' ";

    if(mustFitBothFilters)
      query += "AND ";
    else
      query += "OR ";

    query += TableConfig.PersonFirstNameColumnName + " LIKE '%" + firstNameFilter + "%') ";

    query += "ORDER BY " + TableConfig.PersonLastNameColumnName + ", " + TableConfig.PersonFirstNameColumnName;

    if(search.isInterrupted())
      return;

    try {
      List<String[]> results = entityManager.doNativeQuery(query);
      List<Long> ids = new ArrayList<>(results.size());
      for(String[] result : results) {
        ids.add(Long.parseLong(result[0]));
      }

      if(search.isInterrupted())
        return;

      search.setResults(new LazyLoadingList<Person>(Person.class, ids));
    } catch(Exception ex) {
      log.error("Could not query for Entries without Tags", ex);
    }

    search.fireSearchCompleted(); // TODO
  }

  @Override
  protected void findAllEntriesHavingTheseTagsAsync(Collection<Tag> tagsToFilterFor, String[] tagNamesToFilterFor, SearchCompletedListener<FindAllEntriesHavingTheseTagsResult> listener) {
    Collection<Entry> entriesHavingFilteredTags = new LazyLoadingList<Entry>(Entry.class);
    Set<Tag> tagsOnEntriesContainingFilteredTags = new HashSet<>();
    IEntityManager entityManager = Application.getEntityManager();

    String whereStatement =  TableConfig.EntryDeepThoughtJoinColumnName + " = " + deepThought.getId();
    for(Tag tag : tagsToFilterFor) {
      whereStatement += " AND " + TableConfig.BaseEntityIdColumnName + " IN " + "(SELECT " + TableConfig.EntryTagJoinTableEntryIdColumnName + " FROM " +
          TableConfig.EntryTagJoinTableName + " WHERE " + TableConfig.EntryTagJoinTableTagIdColumnName + " = " + tag.getId() + ")";
    }

    try {
      entriesHavingFilteredTags.addAll(entityManager.queryEntities(Entry.class, whereStatement));
      for(Entry foundEntry : entriesHavingFilteredTags) // TODO for each loops loads Entries from Database -> find a solution without loading Entries from Database
        tagsOnEntriesContainingFilteredTags.addAll(foundEntry.getTags());
    } catch(Exception ex) {
      log.error("Could not query for Entries with having specific Tags", ex);
    }

    listener.completed(new FindAllEntriesHavingTheseTagsResult(entriesHavingFilteredTags, tagsOnEntriesContainingFilteredTags));
  }
}

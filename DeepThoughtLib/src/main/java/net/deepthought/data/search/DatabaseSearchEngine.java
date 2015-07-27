package net.deepthought.data.search;

import net.deepthought.Application;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.Person;
import net.deepthought.data.model.Tag;
import net.deepthought.data.persistence.IEntityManager;
import net.deepthought.data.persistence.LazyLoadingList;
import net.deepthought.data.persistence.db.TableConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
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
  protected void filterTags(FilterTagsSearch search, String[] tagNamesToFilterFor) {
    IEntityManager entityManager = Application.getEntityManager();
    List<FilterTagsSearchResult> results = new ArrayList<>();

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

        List<String[]> queryResults = entityManager.doNativeQuery(query);
        List<Long> ids = new ArrayList<>(queryResults.size());
        for(String[] result : queryResults) {
          ids.add(Long.parseLong(result[0]));
        }

        if(search.isInterrupted())
          return;

        search.addResult(new FilterTagsSearchResult(tagNamesToFilterFor[i], new LazyLoadingList<Tag>(Tag.class, ids)));
      } catch(Exception ex) {
        log.error("Could not query for Entries without Tags", ex);
      }
    }

    search.fireSearchCompleted();
  }

  @Override
  protected void filterEntries(FilterEntriesSearch search, String contentFilter, String abstractFilter) {
    search.fireSearchCompleted(); // TODO
  }

  @Override
  protected void filterAllReferenceBaseTypesForSameFilter(Search search, String referenceBaseFilter) {
    IEntityManager entityManager = Application.getEntityManager();

    String query = "SELECT " + TableConfig.BaseEntityIdColumnName + " FROM " + TableConfig.ReferenceBaseTableName + " WHERE (" +
        TableConfig.ReferenceBaseTitleColumnName + " LIKE '%" + referenceBaseFilter + "%' OR " + TableConfig.ReferenceBaseSubTitleColumnName + " LIKE '%" + referenceBaseFilter + "%')";

    try {
      List<String[]> results = entityManager.doNativeQuery(query);
      List<Long> ids = new ArrayList<>(results.size());
      for(String[] result : results) {
        ids.add(Long.parseLong(result[0]));
      }
      search.setResults(new LazyLoadingList<Person>(Person.class, ids));
    } catch(Exception ex) {
      log.error("Could not query for Entries without Tags", ex);
    }

    search.fireSearchCompleted();
  }

  // there's no way to complete this search with SQL -> do it in memory (very slow for a large amount of ReferenceBases) or with Lucene
  @Override
  protected void filterEachReferenceBaseWithSeparateFilter(Search search, String seriesTitleFilter, String referenceFilter, String referenceSubDivisionFilter) {
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

  @Override
  protected void filterPersons(Search<Person> search, String personFilter) {
    filterPersons(search, personFilter, personFilter, false);
  }

  @Override
  protected void filterPersons(Search<Person> search, String lastNameFilter, String firstNameFilter) {
    filterPersons(search, lastNameFilter, firstNameFilter, true);
  }

  protected void filterPersons(Search<Person> search, String lastNameFilter, String firstNameFilter, boolean mustFitBothFilters) {
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
  public void findAllEntriesHavingTheseTags(Collection<Tag> tagsToFilterFor, Collection<Entry> entriesHavingFilteredTags, Set<Tag> tagsOnEntriesContainingFilteredTags) {
    IEntityManager entityManager = Application.getEntityManager();
//    List<Tag> tagsToFilterForList = new ArrayList<>(tagsToFilterFor);

//    String entriesQuery = "SELECT " + TableConfig.BaseEntityIdColumnName + " FROM " + TableConfig.EntryTableName +
//        " JOIN " + TableConfig.EntryTagJoinTableName + " t ON " + TableConfig.BaseEntityIdColumnName + " = " + TableConfig.EntryTagJoinTableEntryIdColumnName + " WHERE (";
//
//    if(tagsToFilterForList.size() > 0)
//      entriesQuery += "t." + TableConfig.EntryTagJoinTableTagIdColumnName + " = " + tagsToFilterForList.get(0).getId();
//    for(int i = 1; i < tagsToFilterForList.size(); i++)
//      entriesQuery += " OR t." + TableConfig.EntryTagJoinTableTagIdColumnName + " = " + tagsToFilterForList.get(i).getId();
//
//    entriesQuery += ")";

//    String entriesQuery = "SELECT " + TableConfig.BaseEntityIdColumnName + " FROM " + TableConfig.EntryTableName + " WHERE ";
//
//    int i = 0;
//    for(Tag tag : tagsToFilterFor) {
//      if(i > 0) entriesQuery += " AND ";
//
//      entriesQuery += TableConfig.BaseEntityIdColumnName + " IN " + "(SELECT " + TableConfig.EntryTagJoinTableEntryIdColumnName + " FROM " +
//          TableConfig.EntryTagJoinTableName + " WHERE " + TableConfig.EntryTagJoinTableTagIdColumnName + " = " + tag.getId() + ")";
//
//      i++;
//    }

    String whereStatement =  TableConfig.EntryDeepThoughtJoinColumnName + " = " + deepThought.getId();
    for(Tag tag : tagsToFilterFor) {
      whereStatement += " AND " + TableConfig.BaseEntityIdColumnName + " IN " + "(SELECT " + TableConfig.EntryTagJoinTableEntryIdColumnName + " FROM " +
          TableConfig.EntryTagJoinTableName + " WHERE " + TableConfig.EntryTagJoinTableTagIdColumnName + " = " + tag.getId() + ")";
    }

    try {
//      List<String[]> results = entityManager.doNativeQuery(entriesQuery);
//      for(String[] result : results) {
//        Long id = Long.parseLong(result[0]);
//        Entry entry = entityManager.getEntityById(Entry.class, id);
//        entriesHavingFilteredTags.add(entry);
//
//        tagsOnEntriesContainingFilteredTags.addAll(entry.getTags());
//      }

      entriesHavingFilteredTags.addAll(entityManager.queryEntities(Entry.class, whereStatement));
      for(Entry foundEntry : entriesHavingFilteredTags)
        tagsOnEntriesContainingFilteredTags.addAll(foundEntry.getTags());
    } catch(Exception ex) {
      log.error("Could not query for Entries with having specific Tags", ex);
    }
  }
}

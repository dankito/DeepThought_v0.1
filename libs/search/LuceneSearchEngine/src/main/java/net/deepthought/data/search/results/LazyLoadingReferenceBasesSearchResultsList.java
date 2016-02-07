package net.deepthought.data.search.results;

import net.deepthought.Application;
import net.deepthought.data.model.ReferenceBase;
import net.deepthought.data.persistence.db.TableConfig;
import net.deepthought.data.search.FieldName;
import net.deepthought.data.search.SortOrder;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by ganymed on 29/05/15.
 */
public class LazyLoadingReferenceBasesSearchResultsList extends LazyLoadingLuceneSearchResultsList<ReferenceBase> {

  private final static Logger log = LoggerFactory.getLogger(LazyLoadingReferenceBasesSearchResultsList.class);


  public LazyLoadingReferenceBasesSearchResultsList(IndexSearcher searcher, Query query, int countTopNHits) {
    super(searcher, query, ReferenceBase.class, FieldName.ReferenceBaseId, countTopNHits);
  }

  @Override
  protected Collection<Long> applySorting(SortOrder sortOrder, Collection<Long> resultIds) {
    if(resultIds.size() == 0)
      return resultIds;

    String query = "SELECT b." + TableConfig.BaseEntityIdColumnName + " FROM " +
        TableConfig.ReferenceBaseTableName + " b " +
        ", " + TableConfig.ReferenceTableName + " r " +
        "WHERE b." + TableConfig.BaseEntityIdColumnName + " IN (";

    for(Long id : resultIds)
      query += id + ", ";
    query = query.substring(0, query.length() - ", ".length()) + ") ";

    query += "ORDER BY CASE " + TableConfig.ReferenceBaseDiscriminatorColumnName +
        " WHEN '" + TableConfig.SeriesTitleDiscriminatorValue + "' THEN 1" +
        " WHEN '" + TableConfig.ReferenceDiscriminatorValue + "' THEN 2" +
        " ELSE 4 END, " +
        "b." + TableConfig.ReferenceBaseTitleColumnName
        + ", b." + TableConfig.ReferenceBaseSubTitleColumnName
        + ", cast(r." + TableConfig.ReferencePublishingDateColumnName + " as date)"
        + ", r." + TableConfig.ReferenceIssueOrPublishingDateColumnName
    ;

    try {
      List sortedIds = Application.getEntityManager().doNativeQuery(query);
      if(sortedIds.size() == resultIds.size()) {
        this.entityIds = new ArrayList<>(sortedIds.size());
        for(String[] id : (List<String[]>)sortedIds)
          entityIds.add(Long.parseLong(id[0]));
        return entityIds;
      }
    } catch(Exception ex) {
      log.error("Could not sort ReferenceBases search result IDs", ex);
    }

    return super.applySorting(sortOrder, resultIds);
  }
}

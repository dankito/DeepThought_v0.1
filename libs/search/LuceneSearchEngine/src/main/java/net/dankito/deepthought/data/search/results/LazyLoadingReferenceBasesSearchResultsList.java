package net.dankito.deepthought.data.search.results;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.data.model.ReferenceBase;
import net.dankito.deepthought.data.search.FieldName;
import net.dankito.deepthought.data.search.SortOrder;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * Created by ganymed on 29/05/15.
 */
public class LazyLoadingReferenceBasesSearchResultsList extends LazyLoadingLuceneSearchResultsList<ReferenceBase> {

  private final static Logger log = LoggerFactory.getLogger(LazyLoadingReferenceBasesSearchResultsList.class);


  public LazyLoadingReferenceBasesSearchResultsList(IndexSearcher searcher, Query query, int countTopNHits) {
    super(searcher, query, ReferenceBase.class, FieldName.ReferenceBaseId, countTopNHits);
  }

  @Override
  protected Collection<String> applySorting(SortOrder sortOrder, Collection<String> resultIds) {
    if(resultIds.size() == 0)
      return resultIds;

    try {
      entityIds = Application.getEntityManager().sortReferenceBaseIds(resultIds);
    } catch(Exception ex) {
      log.error("Could not sort ReferenceBases search result IDs", ex);
    }

    return super.applySorting(sortOrder, resultIds);
  }
}

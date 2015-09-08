package net.deepthought.data.search.results;

import net.deepthought.data.persistence.LazyLoadingList;
import net.deepthought.data.persistence.db.BaseEntity;
import net.deepthought.data.search.FieldName;
import net.deepthought.data.search.SortOrder;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by ganymed on 23/05/15.
 */
public class LazyLoadingLuceneSearchResultsList<T extends BaseEntity> extends LazyLoadingList<T> {


  private final Logger log = LoggerFactory.getLogger(LazyLoadingLuceneSearchResultsList.class);


  protected IndexSearcher searcher;

  protected ScoreDoc[] hits;

  protected String idFieldName;


  public LazyLoadingLuceneSearchResultsList(IndexSearcher searcher, Query query, Class<T> resultType, String idFieldName) {
    this(searcher, query, resultType, idFieldName, 1000);
  }

  public LazyLoadingLuceneSearchResultsList(IndexSearcher searcher, Query query, Class<T> resultType, String idFieldName, int countTopNHits) {
    this(searcher, query, resultType, idFieldName, 1000, SortOrder.Unsorted, new String[0]);
  }

  public LazyLoadingLuceneSearchResultsList(IndexSearcher searcher, Query query, Class<T> resultType, String idFieldName, int countTopNHits, SortOrder sortOrder, String... sortFieldNames) {
    super(resultType);
    this.searcher = searcher;
    this.idFieldName = idFieldName;

    try {
      // ID fields are stored, but not indexed. In order to be sortable for Lucene, a field must be indexed and may not have been tokenized
      boolean sortForIdField = sortOrder != SortOrder.Unsorted && sortFieldNames.length == 1 && sortFieldNames[0].endsWith("_id");
      Sort sort = getSorting(sortOrder, sortFieldNames, sortForIdField);
      this.hits = searcher.search(query, countTopNHits, sort).scoreDocs;

      this.entityIds = getEntityIds();

      if(sortForIdField)
        this.entityIds = applySorting(sortOrder, this.entityIds);
    } catch(Exception ex) {
      log.error("Could not execute Query " + query, ex);
    }
  }

  protected Sort getSorting(SortOrder sortOrder, String[] sortFieldNames, boolean sortForIdField) {
    Sort sort = new Sort();

    if(sortOrder != SortOrder.Unsorted && sortForIdField == false) {
      boolean reverse = sortOrder == SortOrder.Descending;

      SortField[] sortFields = new SortField[sortFieldNames.length];
      for (int i = 0; i < sortFieldNames.length; i++) {
        if(FieldName.ReferenceBaseType.equals(sortFieldNames[i]))
          sortFields[i] = new SortField(sortFieldNames[i], SortField.Type.INT, false);
        else if(sortFieldNames[i].endsWith("_publishing_date"))
          sortFields[i] = new SortField(sortFieldNames[i], SortField.Type.LONG, true);
        else
          sortFields[i] = new SortField(sortFieldNames[i], getFieldType(sortFieldNames[i]), reverse);
      }

      sort.setSort(sortFields);
    }

    return sort;
  }

  protected SortField.Type getFieldType(String sortFieldName) {
    if(sortFieldName.endsWith("_id"))
      return SortField.Type.LONG;
//    else if(FieldName.ReferencePublishingDate.equals(sortFieldName))
//      return SortField.Type.LONG;

    return SortField.Type.STRING; // TODO: or STRING_VAL?
  }

  protected Collection<Long> applySorting(SortOrder sortOrder, Collection<Long> resultIds) {
    if(sortOrder == SortOrder.Ascending) {
      ArrayList<Long> sortedList = new ArrayList(resultIds);
      Collections.sort(sortedList);
      return sortedList;
    }
    else if(sortOrder == SortOrder.Descending) {
      ArrayList<Long> sortedList = new ArrayList(resultIds);
      Collections.sort(sortedList, new Comparator<Long>() {
        @Override
        public int compare(Long o1, Long o2) {
          return o2.compareTo(o1);
        }
      });
      return sortedList;
    }

    return this.entityIds;
  }

//  protected Long getEntityIdForIndex(int index) throws IOException {
//    Document hitDoc = searcher.doc(hits[index].doc);
//    return hitDoc.getField(idFieldName).numericValue().longValue();
//  }

  @Override
  public Collection<Long> getEntityIds() {
//    Set<Long> ids = new TreeSet<Long>(new Comparator<Long>() {
//      @Override
//      public int compare(Long o1, Long o2) {
//        return o2.compareTo(o1);
//      }
//    });
//    Set<Long> ids = new HashSet<>();
    List<Long> ids = new ArrayList<>();

    try {
      for (int index = 0; index < hits.length; index++) {
//        ids.add(getEntityIdForIndex(index));
        Document hitDoc = searcher.doc(hits[index].doc);
        ids.add(hitDoc.getField(idFieldName).numericValue().longValue());
      }
    } catch(Exception ex) {
      log.error("Could not get all Entity IDs from Lucene Search Result", ex);
    }

    return ids;
  }
}

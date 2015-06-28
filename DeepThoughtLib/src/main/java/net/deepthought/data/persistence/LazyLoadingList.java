package net.deepthought.data.persistence;

import net.deepthought.Application;
import net.deepthought.data.persistence.db.BaseEntity;
import net.deepthought.data.persistence.db.TableConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * Created by ganymed on 23/05/15.
 */
public class LazyLoadingList<T extends BaseEntity> extends AbstractList<T> {

  private final Logger log = LoggerFactory.getLogger(LazyLoadingList.class);



  protected Class<T> resultType;

  protected Collection<Long> entityIds;

  protected Map<Integer, T> cachedResults = new HashMap<>();

  protected int countEntitiesToQueryOnDatabaseAccess = 20;


  public LazyLoadingList(Class<T> resultType) {
    this(resultType, new HashSet<Long>());
  }

  public LazyLoadingList(Class<T> resultType, Collection<Long> entityIds) {
    this.resultType = resultType;
    this.entityIds = entityIds;
  }


  @Override
  public int size() {
    return entityIds.size();
  }

  @Override
  public T get(int index) {
    if(cachedResults.containsKey(index))
      return cachedResults.get(index);

    try {
//      Long id = getEntityIdForIndex(index);
//
//      T item = Application.getEntityManager().getEntityById(resultType, id);
//      cachedResults.put(index, item);
//
//      return item;

      long startTime = new Date().getTime();
      List<Long> idsOfNextEntities = getNextEntityIdsForIndex(index, countEntitiesToQueryOnDatabaseAccess);
      List<BaseEntity> loadedEntities = (List<BaseEntity>)Application.getEntityManager().getEntitiesById(resultType, idsOfNextEntities);

      for(int i = 0; i < idsOfNextEntities.size(); i++ ) {
        T item = findItemById((List<T>)loadedEntities, idsOfNextEntities.get(i));
        if(item != null)
          cachedResults.put(index + i, item);
      }

      long elapsed = new Date().getTime() - startTime;
      log.debug("Preloaded {} Entities in {} milliseconds", idsOfNextEntities.size(), elapsed);
      return cachedResults.get(index);
    } catch(Exception ex) {
      log.error("Could not load Result of type " + resultType + " from Lucene search results", ex);
    }

    return null;
  }

  protected T findItemById(List<T> entities, Long id) {
    for(BaseEntity entity : entities) {
      if(id.equals(entity.getId()))
        return (T)entity;
    }

    return null;
  }

  protected Long getEntityIdForIndex(int index) {
    if(entityIds instanceof List == true)
      return ((List<Long>)entityIds).get(index);

    Iterator<Long> iterator = entityIds.iterator();
    int i = 0;
    while(iterator.hasNext()) {
      if(i == index)
        return iterator.next();

      i++;
      iterator.next();
    }

    entityIds = new ArrayList<Long>(entityIds); // last resort: quite a bad solution as in this way all items of entityIds will be traverse (and therefor loaded if it's a lazy  loading list
    return ((List<Long>)entityIds).get(index);
  }

  protected List<Long> getNextEntityIdsForIndex(int index, int maxCountIdsToReturn) {
    List<Long> ids = new ArrayList<>();

    for(int i = index; i < (index + maxCountIdsToReturn < size() ? index + maxCountIdsToReturn : size()); i++)
      ids.add(getEntityIdForIndex(i));

    return ids;
  }

  @Override
  public Iterator<T> iterator() {
    loadAllResults();
    return super.iterator();
  }

  @Override
  public ListIterator<T> listIterator(int index) {
    loadAllResults();
    return super.listIterator(index);
  }

  protected void loadAllResults() {
//    log.debug("An iterator has been called on LazyLoadingList with " + entityIds.size() + " Entity IDs, therefor all Entities will now be loaded");
//    try { throw new Exception(); } catch(Exception ex) { log.debug("Stacktrace is:", ex); }

    if (cachedResults.size() < size()) {
      try {
        String whereStatement = TableConfig.BaseEntityIdColumnName + " IN (";
        for(Long id : entityIds)
          whereStatement += id + ", ";
        whereStatement = whereStatement.substring(0, whereStatement.length() - ", ".length()) + ") ";

        whereStatement += "ORDER BY instr(',";
        for(Long id : entityIds)
          whereStatement += id + ",";
        //whereStatement = whereStatement.substring(0, whereStatement.length() - ", ".length());
        whereStatement += "', ',' || id || ',')";

        List<T> allItems = Application.getEntityManager().queryEntities(resultType, whereStatement);
//        List<T> allItems = Application.getEntityManager().getEntitiesById(resultType, getEntityIds());
//        List<Long> ids = entityIds instanceof List ? (List<Long>)entityIds : new ArrayList<>(entityIds);
//        for (int i = 0; i < allItems.size(); i++) {
//          T item = findItemById(allItems, ids.get(i));
//          cachedResults.put(i, item);
//        }
        int i = 0;
        for(T item : allItems) {
          cachedResults.put(i, item);
          i++;
        }
      } catch (Exception ex) {
        log.error("Could not retrieve all result items from Lucene search result for result type " + resultType, ex);
      }
    }
  }

  @Override
  public void add(int index, T element) {
    if(entityIds instanceof List) {
      ((List)entityIds).add(index, element.getId());
      cachedResults.put(index, element);
    }
    else {
      entityIds.add(element.getId());
      cachedResults.put(cachedResults.size(), element);
    }
  }

  @Override
  public boolean remove(Object element) {
    if(cachedResults.containsValue(element)) {
      for(Map.Entry<Integer, T> entry : cachedResults.entrySet()) {
        if(element.equals(entry.getValue())) {
          Integer index = entry.getKey();
          cachedResults.remove(index);
          break;
        }
      }
    }

    try {
      return entityIds.remove(((T)element).getId());
    } catch(Exception ex) {

    }

    return false;
  }

  public Collection<Long> getEntityIds() {
    return entityIds;
  }
}

package com.j256.ormlite.dao.cda;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.jpa.EntityConfig;
import com.j256.ormlite.jpa.PropertyConfig;
import com.j256.ormlite.jpa.relationconfig.AssociationConfig;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.stmt.mapped.MappedPreparedStmt;
import com.j256.ormlite.stmt.query.OrderBy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.sql.SQLException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.persistence.InheritanceType;

/**
 * Created by ganymed on 13/10/14.
 */
public class EntitiesCollection<T> extends AbstractList<T> implements Set<T> {

  protected final static String CouldNotExtractEntityId = "CouldNotExtractEntityId";

  private final static Logger log = LoggerFactory.getLogger(EntitiesCollection.class);


  protected PropertyConfig propertyConfig;

  protected Dao<T, ?> dao;

  protected List<T> entities = new CopyOnWriteArrayList<>();
  protected boolean hasRetrievingTableDataBeenSuccessful = false;

  protected PropertyConfig foreignPropertyConfig;
  protected transient final Object parentId;
  protected Object parent = null;
  protected AssociationConfig config = null;

  // TODO: may implement (getQueryForRelatedEntities)
//  protected transient final String orderColumn;
//  protected transient final boolean orderAscending;

  protected List<EntitiesCollectionListener<T>> listeners = new ArrayList<>();


  public EntitiesCollection(PropertyConfig propertyConfig, Object parentId, Object parent, AssociationConfig config, boolean queryForExistingCollectionItems) throws SQLException {
    this.propertyConfig = propertyConfig;
    this.dao = (Dao<T, ?>)propertyConfig.getForeignDao();
    this.foreignPropertyConfig = propertyConfig.getTargetPropertyConfig();
    this.parentId = parentId;
    this.parent = parent;

    this.config = config;

    if(queryForExistingCollectionItems)
     getEntities();
  }

  public EntitiesCollection(Dao<T, ?> entityDao, PropertyConfig foreignPropertyConfig, Object parentId, Object parent, AssociationConfig config) {
    this.dao = entityDao;
    this.foreignPropertyConfig = foreignPropertyConfig;
    this.parentId = parentId;
    this.parent = parent;

    this.config = config;

    getEntities();
  }


  public void refresh() {
    clearMappedEntities();
    getEntities();
  }

  protected void clearMappedEntities() {
    List<T> cachedEntitiesCopy = new ArrayList<T>(entities);
    entities.clear();
    hasRetrievingTableDataBeenSuccessful = false;
    callCacheClearedListeners(cachedEntitiesCopy);
  }

  protected void getEntities() {
    try {
      List<T> mappedEntities = getMappedEntities();
      hasRetrievingTableDataBeenSuccessful = true;

      entities.clear();
      entities.addAll(mappedEntities);
    } catch(Exception ex) {
      log.error("Could not query Dao for Table data. Parent = " + parent + ", foreignPropertyConfig = " + foreignPropertyConfig, ex);
    }
  }

  protected List<T> getMappedEntities() throws SQLException {
    PreparedQuery query = getQueryForRelatedEntities(parentId);
    List<T> mappedEntities = dao.query(query);
    query = null;
    return mappedEntities;

//    GenericRawResults<String[]> rawResults = dao.queryRaw(query);
//    List<String[]> results = rawResults.getResults();
//    query = null;
//
//    List<T> mappedEntities = new ArrayList<>();
//
//    int idColumnIndex = 0;
//    String idColumnName = dao.getTableInfo().getIdProperty().getColumnName();
//    String[] columnNames = rawResults.getColumnNames();
//    for(int i = 0; i < columnNames.length; i++) {
//      if(idColumnName.equals(columnNames[i])) {
//        idColumnIndex = i;
//        break;
//      }
//    }
//
//    for(int i = 0; i < results.size(); i++) {
//      mappedEntities.add(mapTableValues(results.get(i), columnNames, results.get(i)[idColumnIndex]));
//    }
//
//    return mappedEntities;
  }

  protected T mapTableValues(String[] values, String[] columnNames, String entityId) throws SQLException {
    T entity = null;

    Object idInEntityFormat = dao.getEntityConfig().getIdProperty().getDataPersister().convertIdNumber(Long.parseLong(entityId)); // TODO: on non-numberic IDs this has to crash
    if(dao.getObjectCache() != null)
      entity = (T)dao.getObjectCache().get(foreignPropertyConfig.getField().getDeclaringClass(), idInEntityFormat);
    if(entity == null) {
      entity = dao.getRawRowMapper().mapRow(columnNames, values);
      if(dao.getObjectCache() != null)
        dao.getObjectCache().put((Class)entity.getClass(), idInEntityFormat, entity);
    }

    return entity;
  }

  protected PreparedQuery<T> getQueryForRelatedEntities(Object parentId) throws SQLException {
    if (dao == null) {
      return null;
    }

    PreparedQuery preparedQuery = null;

    String foreignEntityIdColumnName = dao.getEntityConfig().getIdProperty().getColumnName();

    SelectArg fieldArg = new SelectArg();
    fieldArg.setValue(parentId);
    QueryBuilder<T, ?> queryBuilder = dao.queryBuilder();

    if(propertyConfig != null) {
      for (OrderBy orderBy : propertyConfig.getOrderColumns())
        queryBuilder.orderBy(orderBy.getColumnName(), orderBy.isAscending());
    }

    if(foreignPropertyConfig.getEntityConfig().getInheritance() == InheritanceType.JOINED) {
      EntityConfig parentEntity = foreignPropertyConfig.getEntityConfig().getParentEntityConfig();

      while(parentEntity != null) {
        QueryBuilder parentQueryBuilder = new QueryBuilder(parentEntity.getDatabaseType(), parentEntity, parentEntity.getDao() != null ? parentEntity.getDao() : dao);
        queryBuilder.join(foreignEntityIdColumnName, foreignEntityIdColumnName, parentQueryBuilder, QueryBuilder.JoinType.LEFT, QueryBuilder.JoinWhereOperation.AND);
        parentEntity = parentEntity.getParentEntityConfig();
      }

//      recursivelyAddChildEntitiesJoins(entityConfig, joiningEntityQueryBuilder, idColumnName); // TODO: needed?

//      preparedQueryForAll = joiningEntityQueryBuilder.prepare();
    }

    if (foreignPropertyConfig != null)
      preparedQuery = queryBuilder.where().eq(foreignPropertyConfig.getColumnName(), fieldArg).prepare();

    if (preparedQuery instanceof MappedPreparedStmt) {
      @SuppressWarnings("unchecked")
      MappedPreparedStmt<T, Object> mappedStmt = ((MappedPreparedStmt<T, Object>) preparedQuery);
      mappedStmt.setParentInformation(parent, parentId);
    }

    return preparedQuery;
  }


  @Override
  public int size() {
    return entities.size();
  }

  @Override
  public T get(int index) {
    return entities.get(index);
  }

  @Override
  public boolean add(T object) {
    int size = size();
    add(size, object);
    return size < size();
  }

  @Override
  public void add(int index, T object) {
    boolean success = false;

    try {
//      tryAssignForeignField(object);

//      if(config.cascadePersist()) {
//        T persistedEntity = dao.createIfNotExists(object);
//        success = persistedEntity != null;
//      }

      itemAdded(index, object);

      callItemAddedListeners(object);
    } catch(Exception ex) {
      log.error("Could not persist object " + object + " in database", ex);
    }

//      return success;
  }

  protected void itemAdded(int index, T entity) {
    entities.add(index, entity);
  }

  protected void tryAssignForeignField(T data) throws SQLException {
    if (parent != null && foreignPropertyConfig.getFieldValueIfNotDefault(data) == null) {
      foreignPropertyConfig.assignField(data, parent, true, null);
    }
  }

  @Override
  public boolean remove(Object object) {
    try {
      T entity = (T)object;
      String id = getEntityId(entity);
//      tryRemoveForeignField(entity);

      boolean success = true;
//      if(config.cascadeRemove())
//        success = dao.delete(entity) == 1;

      itemRemoved(entity, id);

      callItemRemovedListeners(entity);
      return success;
    } catch(Exception ex) {
      log.error("Could not delete object " + object + " in database", ex);
    }

    return false;
  }

  protected String getEntityId(T entity) {
    try {
      return dao.extractId(entity).toString();
    } catch(Exception ex) {
//      log.error("Could not extract id for entity " + entity, ex);
    }

    return CouldNotExtractEntityId;
  }

  protected void tryRemoveForeignField(T data) throws SQLException {
    foreignPropertyConfig.assignField(data, null, true, null);
  }

  protected void itemRemoved(T entity, String entityId) {
    entities.remove(entity);
  }

  @Override
  public Object[] toArray() {
    Object[] result = new Object[size()];
    for(int i = 0; i < size(); i++)
      result[i] = get(i);
    return result;
  }

  @Override
  public <T> T[] toArray(T[] contents) {
    T[] result = (T[]) Array.newInstance(contents.getClass().getComponentType(), size());
    for(int i = 0; i < size(); i++)
      result[i] = (T)get(i);
    return result;
  }


  public boolean addEntitiesCollectionListener(EntitiesCollectionListener<T> listener) {
    return listeners.add(listener);
  }

  public boolean removeEntitiesCollectionListener(EntitiesCollectionListener<T> listener) {
    return listeners.remove(listener);
  }

  protected void callItemAddedListeners(T item) {
    for(EntitiesCollectionListener<T> listener : listeners)
      listener.itemAdded(item);
  }

  protected void callItemRemovedListeners(T item) {
    for(EntitiesCollectionListener<T> listener : listeners)
      listener.itemRemoved(item);
  }

  protected void callEntityMappedListeners(T entity) {
    for(EntitiesCollectionListener<T> listener : listeners)
      listener.entityMapped(entity);
  }

  protected void callCacheClearedListeners(List<T> cachedEntities) {
    for(EntitiesCollectionListener<T> listener : listeners)
      listener.cacheCleared(cachedEntities);
  }

}

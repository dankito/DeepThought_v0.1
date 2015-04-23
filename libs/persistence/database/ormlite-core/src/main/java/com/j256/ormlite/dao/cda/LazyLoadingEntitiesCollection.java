package com.j256.ormlite.dao.cda;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.jpa.EntityConfig;
import com.j256.ormlite.jpa.PropertyConfig;
import com.j256.ormlite.jpa.relationconfig.AssociationConfig;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.stmt.mapped.MappedPreparedStmt;
import com.j256.ormlite.stmt.mapped.MappedQueryForId;
import com.j256.ormlite.stmt.query.OrderBy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.persistence.InheritanceType;

/**
 * Created by ganymed on 13/10/14.
 */
public class LazyLoadingEntitiesCollection<T> extends EntitiesCollection<T> {

  private final static Logger log = LoggerFactory.getLogger(LazyLoadingEntitiesCollection.class);


  protected List<String> retrievedIndices = new CopyOnWriteArrayList<>();
  protected Map<T, String> uniqueIdsForItemsWithoutIds = new HashMap<>();

  protected boolean cacheEntities = false;
  protected Map<String, T> cachedEntities = new HashMap<>();

  protected MappedQueryForId queryForTargetEntityId = null;


  public LazyLoadingEntitiesCollection(PropertyConfig propertyConfig, Object parentId, Object parent, AssociationConfig config, boolean queryForExistingCollectionItems) throws SQLException {
    super((Dao<T, ?>)propertyConfig.getForeignDao(), propertyConfig.getTargetPropertyConfig(), parentId, parent, config);

    this.propertyConfig = propertyConfig;
    this.cacheEntities = true;
    try {
      queryForTargetEntityId = MappedQueryForId.build(dao.getConnectionSource().getDatabaseType(), propertyConfig.getTargetEntityConfig(), null);
    } catch(Exception ex) {
      log.error("Could not build MappedQueryForId for Entity " + propertyConfig.getEntityConfig());
    }

//    if(queryForExistingCollectionItems)
//      getEntities();
  }

  public LazyLoadingEntitiesCollection(Dao<T, ?> entityDao, PropertyConfig foreignPropertyConfig, Object parentId, Object parent, AssociationConfig config) {
    this(entityDao, foreignPropertyConfig, parentId, parent, config, entityDao.getObjectCache() != null);
  }

  public LazyLoadingEntitiesCollection(Dao<T, ?> entityDao, PropertyConfig foreignPropertyConfig, Object parentId, Object parent, AssociationConfig config, boolean cacheEntities) {
    super(entityDao, foreignPropertyConfig, parentId, parent, config);
    this.cacheEntities = cacheEntities;
    try {
      queryForTargetEntityId = MappedQueryForId.build(entityDao.getConnectionSource().getDatabaseType(), entityDao.getEntityConfig(), null);
    } catch(Exception ex) {
      log.error("Could not build MappedQueryForId for TableInfo " + entityDao.getTableConfig());
    }

    getEntities();
  }


  protected void getEntities() {
    if(retrievedIndices == null) // when null this method gets called from parent's constructor; class not initialized yet, return
      return;

    try {
      retrievedIndices = retrieveForeignIds();
    } catch(Exception ex) {
      log.error("Could not query Dao for Table data", ex);
    }
  }

  protected List<String> retrieveForeignIds() throws SQLException {
    PreparedQuery query = getQueryForEntitiesIds(parentId);
    GenericRawResults<String[]> tableResults = dao.queryRaw(query);
    foreignEntitiesIdsRetrieved(query, tableResults);

    return retrievedIndices;
  }

  protected void foreignEntitiesIdsRetrieved(PreparedQuery query, GenericRawResults<String[]> tableResults) throws SQLException {
    retrievedIndices.clear();

    for(String[] row : tableResults.getResults()) {
      if(row.length > 0)
        retrievedIndices.add(row[0]);
      else
        log.warn("Result row for query " + query.getStatement() + " for parent " + parent + " returned a row without column values. Column names where " + tableResults.getColumnNames());
    }

    hasRetrievingTableDataBeenSuccessful = true;
    query = null;
  }

  protected PreparedQuery<T> getQueryForEntitiesIds(Object parentId) throws SQLException {
    if (dao == null) {
      return null;
    }

    PreparedQuery preparedQuery = null;

    String foreignEntityIdColumnName = dao.getEntityConfig().getIdProperty().getColumnName();

    SelectArg fieldArg = new SelectArg();
    fieldArg.setValue(parentId);
    QueryBuilder<T, ?> queryBuilder = dao.queryBuilder();

    if (propertyConfig != null) {
      for (OrderBy orderBy : propertyConfig.getOrderColumns())
        queryBuilder.orderBy(orderBy.getColumnName(), orderBy.isAscending());
    }

    if(foreignPropertyConfig.getEntityConfig().getInheritance() == InheritanceType.JOINED) {
//      QueryBuilder joiningEntityQueryBuilder = null;
//      String idColumnName = entityConfig.getInheritanceTopLevelEntityConfig().getIdProperty().getColumnName();
//
//      joiningEntityQueryBuilder = new QueryBuilder(databaseType, entityConfig, entityConfig.getDao() != null ? entityConfig.getDao() : dao);
      EntityConfig parentEntity = foreignPropertyConfig.getEntityConfig().getParentEntityConfig();

      while(parentEntity != null) {
        QueryBuilder parentQueryBuilder = new QueryBuilder(parentEntity.getDatabaseType(), parentEntity, parentEntity.getDao() != null ? parentEntity.getDao() : dao);
//        joiningEntityQueryBuilder.join(idColumnName, idColumnName, parentQueryBuilder, QueryBuilder.JoinType.LEFT, QueryBuilder.JoinWhereOperation.AND);
        queryBuilder.join(foreignEntityIdColumnName, foreignEntityIdColumnName, parentQueryBuilder, QueryBuilder.JoinType.LEFT, QueryBuilder.JoinWhereOperation.AND);
        parentEntity = parentEntity.getParentEntityConfig();
      }

//      recursivelyAddChildEntitiesJoins(entityConfig, joiningEntityQueryBuilder, idColumnName); // TODO: needed?

//      preparedQueryForAll = joiningEntityQueryBuilder.prepare();
    }

    if (foreignPropertyConfig != null) // how should that ever be null?
      preparedQuery = queryBuilder.selectColumns(foreignEntityIdColumnName).where().eq(foreignPropertyConfig.getColumnName(), fieldArg).prepare();

    if (preparedQuery instanceof MappedPreparedStmt) {
      @SuppressWarnings("unchecked")
      MappedPreparedStmt<T, Object> mappedStmt = ((MappedPreparedStmt<T, Object>) preparedQuery);
      mappedStmt.setParentInformation(parent, parentId);
    }

    return preparedQuery;
  }

//
//  protected void findIdColumnIndex() {
//    for (int i = 0; i < dao.getTableInfo().getFieldTypes().length; i++) {
//      FieldType fieldType = dao.getTableInfo().getFieldTypes()[i];
//      if(fieldType.equals(dao.getTableInfo().getIdProperty())) {
//        idColumnIndex = i;
//        hasDeterminingIdColumnBeenSuccessful = true;
//        break;
//      }
//    }
//  }

  protected void clearMappedEntities() {
    List<T> cachedEntitiesCopy = new ArrayList<T>(cachedEntities.values());
    cachedEntities.clear();
    hasRetrievingTableDataBeenSuccessful = false;
    callCacheClearedListeners(cachedEntitiesCopy);
  }


  @Override
  public int size() {
    if(retrievedIndices == null || hasRetrievingTableDataBeenSuccessful == false) {
      try {
        retrievedIndices = retrieveForeignIds();
      } catch(Exception ex) {
        log.error("Could not query Dao for Table data", ex);
      }
    }

    return retrievedIndices.size();
  }

  @Override
  public T get(int index) {
    String id = retrievedIndices.get(index);
    if(cachedEntities.containsKey(id)) {
      return cachedEntities.get(id);
    }

    return retrieveEntityAndMayCache(id);


//    String id = getIdAtIndex(index);
//    if(cachedEntities.containsKey(id) == false) {
//      mapAndCacheEntity(results.get(index));
//    }
//
//    return super.get(index);
  }

  protected T retrieveEntityAndMayCache(String entityId) {
    try {
      T entity = (T) queryForTargetEntityId.execute(dao.getConnectionSource().getReadOnlyConnection(), entityId, dao.getObjectCache());
      if(entity != null) {
        cacheEntity(entity);
      }

      return entity;

//      PreparedQuery query = getQueryForEntityData(entityId);
//      GenericRawResults<String[]> rawResults = dao.queryRaw(query);
//      List<String[]> results = rawResults.getResults();
//      query = null;
//
//      if(results.size() == 0) {
//        log.warn("No entity found in table " + dao.getTableInfo().getTableName() + " for id " + entityId);
//        GenericRawResults<String[]> debugRawResults = dao.queryRaw("SELECT * FROM " + dao.getTableInfo().getTableName());
//        List<String[]> debugResults = debugRawResults.getResults();
//        log.warn("Column names are " + debugRawResults.getColumnNames() + ", data table is " + debugResults);
//      }
//      else if(results.size() == 1) {
//        T entity = mapTableValues(results.get(0), rawResults.getColumnNames(), entityId);
//        cacheEntity(entity);
//        return entity;
//      }
//      else
//        log.error("Should never be the case: More than one entity has been returned from table " + dao.getTableInfo().getTableName() + " for id " + entityId);
    } catch(Exception ex) {
      log.error("Could not retrieve table data from table " + dao.getEntityConfig().getTableName() + " for id " + entityId, ex);
    }

    return null;
  }

  protected PreparedQuery<T> getQueryForEntityData(Object entityId) throws SQLException {
    if (dao == null) {
      return null;
    }

    PreparedQuery preparedQuery = null;

    Object foreignEntityIdColumnName = dao.getEntityConfig().getIdProperty().getColumnName();

    SelectArg fieldArg = new SelectArg();
    fieldArg.setValue(entityId);
    QueryBuilder<T, ?> qb = dao.queryBuilder();

//      if (orderColumn != null) {
//        qb.orderBy(orderColumn, orderAscending);
//      }
    if (foreignPropertyConfig != null)
      preparedQuery = qb.where().eq(foreignEntityIdColumnName.toString(), fieldArg).prepare();

    if (preparedQuery instanceof MappedPreparedStmt) {
      @SuppressWarnings("unchecked")
      MappedPreparedStmt<T, Object> mappedStmt = ((MappedPreparedStmt<T, Object>) preparedQuery);
      mappedStmt.setParentInformation(parent, entityId);
    }

    return preparedQuery;
  }

  protected void itemAdded(int index, T entity) {
    //refresh();

    String id = getEntityId(entity);
    if(id == CouldNotExtractEntityId) // entity not saved in database yet
      id = createUniqueIdForItem(entity);
//    if(getIndexFromId(id) == IndexNotFound) // no database values cached yet for this entity
//      results.add(getEntityColumnValues(entitiy));
    if(retrievedIndices.contains(id) == false)
      retrievedIndices.add(index, id);
    cacheEntity(entity, id);
  }

  private String createUniqueIdForItem(T entity) {
    String uniqueId = UUID.randomUUID().toString();
    uniqueIdsForItemsWithoutIds.put(entity, uniqueId);

    return uniqueId;
  }

  protected void itemRemoved(T entity, String entityId) {
    //refresh();

    if(uniqueIdsForItemsWithoutIds.containsKey(entity)) {
      entityId = uniqueIdsForItemsWithoutIds.get(entity);
      uniqueIdsForItemsWithoutIds.remove(entity);
    }
    else if(entityId == CouldNotExtractEntityId) {
//      if(cachedEntities.containsValue(entity))
      for(Map.Entry<String, T> cachedEntry : cachedEntities.entrySet()) {
        if(entity.equals(cachedEntry.getValue())) {
          entityId = cachedEntry.getKey();
          break;
        }
      }
    }

    retrievedIndices.remove(entityId);
    cachedEntities.remove(entityId);
  }

//  public T getEntityWithId(String id) {
//    if(cachedEntities.containsKey(id) == false) {
//      int index = getIndexFromId(id);
//      if(index == IndexNotFound)
//        return null;
//
//      return get(index);
//    }
//
//    return cachedEntities.get(id);
//  }

//  protected void mapAndCacheEntity(int index) {
//    results.get(index)
//  }

//  protected String[] getEntityColumnValues(T entity) {
//    String[] result = new String[columnNames.length];
//
////    FieldType[] fieldTypes = dao.getTableInfo().getFieldTypes();
////    for(int i = 0; i < fieldTypes.length; i++) {
////      try {
////        result[i] = fieldTypes[i].extractJavaFieldValue(entity).toString();
////      } catch(Exception ex) { log.error("Could not extract field value for fieldType " + fieldTypes[i] + " on entity " + entity, ex); }
////    }
//
////    for(int i = 0; i < result.length; i++) {
////      try {
////        Field field = dao.getTableInfo().getDataClass().getDeclaredField(columnNames[i]);
////        result[i] = field.get(entity).toString();
////      } catch(Exception ex) { log.error("Could not extract field value for column " + columnNames[i] + " on entity " + entity, ex); }
////    }
//
//    for(FieldType fieldType : dao.getTableInfo().getFieldTypesWithoutForeignCollections()) {
//      try {
//        String fieldColumnName = fieldType.getColumnName().toLowerCase();
//        for(int j = 0; j < columnNames.length; j++) {
//          if(columnNames[j].toLowerCase().equals(fieldColumnName)) {
//            try {
//              Object fieldValue = fieldType.getField().get(entity);
//              if(fieldValue == null)
//                result[j] = "";
//              else
//                result[j] = fieldValue.toString();
//              break;
//            } catch(Exception ex) {
//              log.error("Could not extract field value for column " + columnNames[j] + " on entity " + entity, ex); }
//          }
//        }
//      } catch(Exception ex) { log.error("Could not extract field value for fieldType " + fieldType + " on entity " + entity, ex); }
//    }
//
//    return result;
//  }

  protected void cacheEntity(T entity) {
    if(entity != null && cacheEntities == true) {
      try {
        cacheEntity(entity, dao.extractId(entity).toString());
      } catch(Exception ex) { log.error("Could not get ID for entity " + entity, ex); }
    }
  }

  protected void cacheEntity(T entity, String id) {
    cachedEntities.put(id, entity);
  }

//  protected String getIdAtIndex(int index) {
//    String[] values = results.get(index);
//    return values[idColumnIndex];
//  }
//
//  protected int getIndexFromId(String id) {
//    for(int i = 0; i < results.size(); i++) {
//      String[] values = results.get(i);
//
//      if(values.length == 1) {
//        if(values[0].equals(id))
//          return i;
//      }
//      else if(values[idColumnIndex].equals(id))
//        return i;
//    }
//
//    return IndexNotFound;
//  }



  public boolean cacheEntities() {
    return cacheEntities;
  }

  public void setCacheEntities(boolean cacheEntities) {
    this.cacheEntities = cacheEntities;
  }

}

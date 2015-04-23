package com.j256.ormlite.dao.cda;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.dao.cda.jointable.JoinTableConfig;
import com.j256.ormlite.dao.cda.jointable.JoinTableDao;
import com.j256.ormlite.jpa.PropertyConfig;
import com.j256.ormlite.jpa.relationconfig.ManyToManyConfig;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.stmt.Where;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by ganymed on 03/11/14.
 */
public class ManyToManyEntitiesCollection<T> extends EntitiesCollection<T> {

  private final static Logger log = LoggerFactory.getLogger(ManyToManyEntitiesCollection.class);


  protected JoinTableDao joinTableDao;
  protected boolean isOwningSide;

  protected String idColumnName = "id";

  protected boolean isAddingToForeignCollection = false; // on item add flag to indicate that we're adding parent to other side's collection; without we would produce an infinite loop
  protected boolean isRemovingFromForeignCollection = false;


  public ManyToManyEntitiesCollection(PropertyConfig propertyConfig, Object parentId, Object parent, boolean queryForExistingCollectionItems) throws SQLException {
    super((Dao<T, ?>)propertyConfig.getForeignDao(), propertyConfig.getTargetPropertyConfig(), parentId, parent, propertyConfig.getManyToManyConfig());
    this.propertyConfig = propertyConfig;
    this.isOwningSide = propertyConfig.isOwningSide();

    try {
      idColumnName = dao.getEntityConfig().getIdProperty().getColumnName();
      JoinTableConfig joinTable = propertyConfig.getJoinTable();
      this.joinTableDao = joinTable.getJoinTableDao();
      if(queryForExistingCollectionItems)
        getEntities();
    } catch(Exception ex) {
      log.error("Could not retrieve JoinTable results", ex);
    }
  }

  public ManyToManyEntitiesCollection(Dao<T, ?> entityDao, PropertyConfig foreignPropertyConfig, Object parentId, Object parent, ManyToManyConfig manyToManyConfig) {
    super(entityDao, foreignPropertyConfig, parentId, parent, manyToManyConfig);
    this.isOwningSide = manyToManyConfig.isOwningSideField(foreignPropertyConfig.getField()) == false;

    try {
      idColumnName = dao.getEntityConfig().getIdProperty().getColumnName();
//      this.joinTableDao = JoinTableDaoRegistry.getInstance().getJoinTableDaoForManyToManyRelation(manyToManyConfig, entityDao.getConnectionSource());
      JoinTableConfig joinTable = foreignPropertyConfig.getJoinTable();
      this.joinTableDao = joinTable.getJoinTableDao();
      getEntities();
    } catch(Exception ex) {
      log.error("Could not retrieve JoinTable results", ex);
    }
  }

  protected void getEntities() {
    if(joinTableDao == null) // when null this method gets called from parent's constructor; class not initialized yet, return
      return;

    super.getEntities();
  }

  @Override
  protected List<T> getMappedEntities() throws SQLException {
    // the process for a many to many relation differs in that we first have to query join table for foreign ids
    String[] joinedEntitiesIds = joinTableDao.getJoinedEntitiesIds(parentId, isOwningSide);

    if(joinedEntitiesIds.length > 0) {
      PreparedQuery query = getQueryForJoinedEntitiesFromTargetTable(joinedEntitiesIds);
//      List<T> mappedEntities = dao.query(query);
      GenericRawResults<String[]> rawResults = dao.queryRaw(query);
      List<String[]> results = rawResults.getResults();
      query = null;

      Integer idColumnIndex = getIdColumnIndex(rawResults);
      if(idColumnIndex == null)
        throw new SQLException("Could not find Id column from SQL query result column names " + rawResults.getColumnNames());

      List<T> mappedEntities = new ArrayList<>();
      if(((Integer)joinedEntitiesIds.length).equals(results.size())) {
        for(int i = 0; i < results.size(); i++) {
//          mappedEntities.add(mapTableValues(results.get(i), rawResults.getColumnNames(), joinedEntitiesIds[i])); // the order of joinedEntitiesIds and results can differ,
// so that mapped Entity gets cached for a wrong Id from joinedEntitiesIds.
          mappedEntities.add(mapTableValues(results.get(i), rawResults.getColumnNames(), results.get(i)[idColumnIndex]));
        }
      }
      else
        log.error("Results size " + results.size() + " doesn't equals JoinTable Entities IDs length " + joinedEntitiesIds.length);

      return mappedEntities;
    }

    return new ArrayList<T>();
  }

  protected Integer getIdColumnIndex(GenericRawResults<String[]> rawResults) {
    String idColumnName = foreignPropertyConfig.getEntityConfig().getIdProperty().getColumnName().toUpperCase();
    Integer idColumnIndex = null;
    for(int i = 0; i < rawResults.getColumnNames().length; i++) {
      if(rawResults.getColumnNames()[i].toUpperCase().equals(idColumnName)) {
        idColumnIndex = i;
        break;
      }
    }
    return idColumnIndex;
  }

  @Override
  protected void tryAssignForeignField(T data) throws SQLException {
    // there's no foreign field any more where we could assign our parent instance to
  }

  protected void tryAddToForeignCollection(T data) throws SQLException {
    if(parent != null && isAddingToForeignCollection == false) { // if not setting a flag like isAddingToForeignCollection we would have an infinite loop
      isAddingToForeignCollection = true;
      try {
        Object foreignFieldValue = foreignPropertyConfig.getField().get(data);
        if(foreignFieldValue instanceof Collection) {
          Collection foreignCollection = (Collection)foreignFieldValue;
          if(foreignCollection.contains(parent) == false)
            foreignCollection.add(parent);
        }
      } catch(Exception ex) {
        log.error("Could not set object '" + data + "' to foreign ManyToManyCollection", ex);
        throw new SQLException("Could not set object '" + data + "' to foreign ManyToManyCollection", ex);
      }

      isAddingToForeignCollection = false;
    }
  }

  @Override
  protected void itemAdded(int index, T entity) {
    super.itemAdded(index, entity);

//    try {
//      Object targetEntityId = dao.extractId(entity);
//
//      if(targetEntityId == null) // entity perhaps not saved to database yet
//        log.warn("Could not extract ID from entity {}", entity);
//
//      if(targetEntityId != null && joinTableDao.doesJoinTableEntryExist(parentId, isOwningSide, targetEntityId) == false) {
//        joinTableDao.insertJoinEntry(parentId, isOwningSide, targetEntityId);
//      }
//
//      tryAddToForeignCollection(entity);
//    } catch(Exception ex) {
//      log.error("Could not insert entry in Join Table", ex);
//    }

    // copied from LazyManyToManyCollection - does it work?

    try {
      Object targetEntityId = dao.extractId(entity);
      if(config.cascadePersist() || targetEntityId != null) { // add Join Table entry if target entity is already persisted or cascade includes persist // TODO: is this correct?
        if(targetEntityId == null) // cascade persist
          dao.create(entity);
        if (joinTableDao.doesJoinTableEntryExist(parentId, isOwningSide, targetEntityId) == false) {
          joinTableDao.insertJoinEntry(parentId, isOwningSide, targetEntityId);
        }
      }

      tryAddToForeignCollection(entity);
    } catch(Exception ex) {
      log.error("Could not insert entry in Join Table", ex);
    }
  }

  @Override
  public boolean remove(Object object) {
    try {
      T entity = (T)object;
      Object targetEntityId = dao.extractId(entity);

      boolean success = true;
      if((targetEntityId != null && targetEntityId.getClass().isPrimitive() == false) || ((Integer)0).equals(targetEntityId) == false) {
        joinTableDao.deleteJoinEntry(parentId, isOwningSide, targetEntityId);
        if(config.cascadeRemove())
          success = dao.delete(entity) == 1;
      }

      itemRemoved(entity, targetEntityId.toString());
      tryRemoveFromForeignCollection(entity);

      callItemRemovedListeners(entity);
      return success;
    } catch(Exception ex) {
      log.error("Could not delete object " + object + " in database", ex);
    }

    return false;
  }

  protected void tryRemoveForeignField(T data) throws SQLException {
    // nothing to do here
  }

  @Override
  protected void itemRemoved(T entity, String entityId) {
    super.itemRemoved(entity, entityId);

    try {
//      Object targetEntityId = dao.extractId(entity);
//      if(joinTableDao.doesJoinTableEntryExist(parentId, isOwningSide, targetEntityId) == true) {
//        joinTableDao.deleteJoinEntry(parentId, isOwningSide, targetEntityId);
//      }

    } catch(Exception ex) {
      log.error("Could not insert entry in Join Table", ex);
    }
  }

  protected void tryRemoveFromForeignCollection(T data) /*throws SQLException*/ {
    if(parent != null && isRemovingFromForeignCollection == false) { // if not setting a flag like isAddingToForeignCollection we would have an infinite loop
      isRemovingFromForeignCollection = true;
      try {
        Object foreignFieldValue = foreignPropertyConfig.getField().get(data);
        if(foreignFieldValue instanceof Collection) {
          Collection foreignCollection = (Collection)foreignFieldValue;
          if(foreignCollection.contains(parent) == true)
            foreignCollection.remove(parent);
        }
      } catch(Exception ex) {
        log.error("Could not remove object '" + data + "' from foreign ManyToManyCollection", ex);
//        throw new SQLException("Could not remove object '" + data + "' from foreign ManyToManyCollection", ex);
      }

      isRemovingFromForeignCollection = false;
    }
  }

  protected PreparedQuery<T> getQueryForJoinedEntitiesFromTargetTable(Object[] targetEntityIds) throws SQLException {
    if (dao == null) {
      return null;
    }

    PreparedQuery preparedQuery = null;
    QueryBuilder<T, ?> qb = dao.queryBuilder();
    Where<T, ?> where = qb.where();

    if(targetEntityIds.length > 0) {
      SelectArg fieldArg = new SelectArg();
      fieldArg.setValue(targetEntityIds[0]);
      where.eq(idColumnName, fieldArg);

      for(int i = 1; i < targetEntityIds.length; i++) {
        SelectArg fieldArgNext = new SelectArg();
        fieldArgNext.setValue(targetEntityIds[i]);
        where.or().eq(idColumnName, fieldArgNext);
      }

      // TODO: do i need this?
//      if (preparedQuery instanceof MappedPreparedStmt) {
//        @SuppressWarnings("unchecked")
//        MappedPreparedStmt<T, Object> mappedStmt = ((MappedPreparedStmt<T, Object>) preparedQuery);
//        mappedStmt.setParentInformation(parent, targetEntityId);
//      }
    }

    preparedQuery = where.prepare();
    return preparedQuery;
  }
}

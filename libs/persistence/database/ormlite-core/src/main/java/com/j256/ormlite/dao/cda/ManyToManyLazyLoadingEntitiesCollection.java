package com.j256.ormlite.dao.cda;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.cda.jointable.JoinTableConfig;
import com.j256.ormlite.dao.cda.jointable.JoinTableDao;
import com.j256.ormlite.jpa.PropertyConfig;
import com.j256.ormlite.jpa.relationconfig.ManyToManyConfig;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.stmt.query.OrderBy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

/**
 * Created by ganymed on 16/10/14.
 */
public class ManyToManyLazyLoadingEntitiesCollection<T> extends LazyLoadingEntitiesCollection<T> {

  private final static Logger log = LoggerFactory.getLogger(ManyToManyLazyLoadingEntitiesCollection.class);


  protected JoinTableDao joinTableDao;
  protected boolean isOwningSide;

  protected String idColumnName = "id";

  protected boolean isAddingToForeignCollection = false; // on item add flag to indicate that we're adding parent to other side's collection; without we would produce an infinite loop
  protected boolean isRemovingFromForeignCollection = false;


  public ManyToManyLazyLoadingEntitiesCollection(PropertyConfig propertyConfig, Object parentId, Object parent, boolean queryForExistingCollectionItems) throws SQLException {
    super((Dao<T, ?>)propertyConfig.getForeignDao(), propertyConfig.getTargetPropertyConfig(), parentId, parent, propertyConfig.getManyToManyConfig());

    this.propertyConfig = propertyConfig;
    this.isOwningSide = propertyConfig.isOwningSide();

    try {
      idColumnName = dao.getEntityConfig().getIdProperty().getColumnName();
      JoinTableConfig joinTable = propertyConfig.getJoinTable();
      this.joinTableDao = joinTable.getJoinTableDao();
//      if(queryForExistingCollectionItems)
//        retrievedIndices = retrieveForeignIds();
    } catch(Exception ex) {
      log.error("Could not retrieve JoinTable results", ex);
    }
  }

  public ManyToManyLazyLoadingEntitiesCollection(Dao<T, ?> entityDao, PropertyConfig foreignPropertyConfig, Object parentId, Object parent, ManyToManyConfig manyToManyConfig) {
    super(entityDao, foreignPropertyConfig, parentId, parent, manyToManyConfig);
//    this.isOwningSide = manyToManyConfig.isOwningSideField(foreignPropertyConfig.getField()) == false;
    this.isOwningSide = foreignPropertyConfig.isOwningSide() == false; // if target side is not the owning side, then we are the owning side

    try {
      idColumnName = dao.getEntityConfig().getIdProperty().getColumnName();
//      this.joinTableDao = JoinTableDaoRegistry.getInstance().getJoinTableDaoForManyToManyRelation(manyToManyConfig, entityDao.getConnectionSource());
      JoinTableConfig joinTable = foreignPropertyConfig.getJoinTable();
      this.joinTableDao = joinTable.getJoinTableDao();
      retrievedIndices = retrieveForeignIds();
    } catch(Exception ex) {
      log.error("Could not retrieve JoinTable results", ex);
    }
  }


  @Override
  protected List<String> retrieveForeignIds() throws SQLException {
    if(joinTableDao == null) // when called from parent's constructor joinTableDao is null
      return retrievedIndices;

    String[] joinedEntitiesIds = null;

    if(propertyConfig != null && propertyConfig.hasOrderColumns())
      joinedEntitiesIds = joinTableDao.getJoinedEntitiesIdsOrdered(parentId, isOwningSide, foreignPropertyConfig.getEntityConfig().getTableName(), "id", // TODO: make generic by adding TableConfig to FieldType
          propertyConfig.getOrderColumns());
    else
      joinedEntitiesIds = joinTableDao.getJoinedEntitiesIds(parentId, isOwningSide);

//    if(joinedEntitiesIds.length != 0) {
//      PreparedQuery query = getQueryForJoinedEntitiesFromTargetTable(joinedEntitiesIds);
//      GenericRawResults<String[]> tableResults = dao.queryRaw(query);
//      foreignEntitiesIdsRetrieved(query, tableResults);
//    }

    retrievedIndices.clear();
    for(String id : joinedEntitiesIds)
      retrievedIndices.add(id);

    hasRetrievingTableDataBeenSuccessful = true;

    return retrievedIndices;
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

      boolean success = joinTableDao.deleteJoinEntry(parentId, isOwningSide, targetEntityId);
      if(config.cascadeRemove())
        success = dao.delete(entity) == 1;

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

      if(propertyConfig != null) {
        for (OrderBy orderBy : propertyConfig.getOrderColumns())
          qb.orderBy(orderBy.getColumnName(), orderBy.isAscending());
      }

      preparedQuery = where.prepare();

      // TODO: do i need this?
//      if (preparedQuery instanceof MappedPreparedStmt) {
//        @SuppressWarnings("unchecked")
//        MappedPreparedStmt<T, Object> mappedStmt = ((MappedPreparedStmt<T, Object>) preparedQuery);
//        mappedStmt.setParentInformation(parent, targetEntityId);
//      }
    }

    return preparedQuery;
  }


}

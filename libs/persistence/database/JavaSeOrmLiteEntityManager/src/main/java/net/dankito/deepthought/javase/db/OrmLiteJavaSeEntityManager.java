package net.dankito.deepthought.javase.db;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.instances.Instances;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.jpa.EntityConfig;
import com.j256.ormlite.jpa.Registry;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.StatementExecutor;
import com.j256.ormlite.table.TableUtils;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.data.model.Entry;
import net.dankito.deepthought.data.model.Tag;
import net.dankito.deepthought.data.persistence.EntityManagerConfiguration;
import net.dankito.deepthought.data.persistence.IEntityManager;
import net.dankito.deepthought.data.persistence.db.BaseEntity;
import net.dankito.deepthought.data.persistence.db.TableConfig;
import net.dankito.deepthought.db.EntitiesConfigurator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by ganymed on 11/10/14.
 */
public class OrmLiteJavaSeEntityManager implements IEntityManager {

  private final static Logger log = LoggerFactory.getLogger(OrmLiteJavaSeEntityManager.class);


  protected JdbcConnectionSource connectionSource = null;


  protected String databasePath = null;

  protected Map<Class, Dao> mapEntityClassesToDaos = new HashMap<>();

  protected StatementExecutor statementExecutor;


  public OrmLiteJavaSeEntityManager(EntityManagerConfiguration configuration) throws SQLException {
    connectionSource = new JdbcConnectionSource(configuration.getDatabasePathIncludingDriverUrl());
    this.databasePath = configuration.getDataCollectionPersistencePath();

    EntitiesConfigurator configurator = new EntitiesConfigurator();
    EntityConfig[] entities = configurator.readEntityConfiguration(configuration, connectionSource);

    getDaosAndMayCreateTables(configuration, entities);

    if (configuration.getDataBaseCurrentDataModelVersion() < configuration.getApplicationDataModelVersion())
      Application.getPreferencesStore().setDatabaseDataModelVersion(configuration.getApplicationDataModelVersion());

    statementExecutor = new StatementExecutor(connectionSource.getDatabaseType(), entities[0], entities[0].getDao());
  }

  protected void getDaosAndMayCreateTables(EntityManagerConfiguration configuration, EntityConfig[] entities) throws SQLException {
    for(EntityConfig entity : entities) {
      mapEntityClassesToDaos.put(entity.getEntityClass(), entity.getDao());

      if (configuration.getDataBaseCurrentDataModelVersion() == 0)
        TableUtils.createTableIfNotExists(this.connectionSource, entity.getEntityClass());
    }
  }


  /**
   * Close the database connections and clear any cached DAOs.
   */
  @Override
  public void close() {
    for(Dao dao : mapEntityClassesToDaos.values()) {
      dao.clearObjectCache();
    }
    mapEntityClassesToDaos.clear();

    try { connectionSource.close(); } catch(Exception ex) {
      log.error("Could not close database connection", ex);
    }

    // TODO: try to get rid of these static Registries and Managers, they are only causing troubles (e.g. on Unit Testing where a lot of EntityManager instances are created)
    Registry.setupRegistry(null, null);
    Instances.setDaoManager(null);
    Instances.setFieldTypeCreator(null);
  }


  public boolean persistEntity(BaseEntity entity) {
    log.debug("Going to create Entity " + entity);
    return createOrUpdateEntity(entity);
  }

  @Override
  public boolean updateEntities(List<BaseEntity> entities) {
    boolean result = true;

    for(BaseEntity entity : entities)
      result &= updateEntity(entity);

    return result;
  }

  @Override
  public <T extends BaseEntity> T getEntityById(Class<T> entityClass, String id) {
    try {
      Dao dao = getDaoForClass(entityClass);

      if(dao != null) {
        return (T)dao.queryForId(id);
      }
    } catch(Exception ex) {
      log.error("Could not get Entity of ID " + id + " for Type " + entityClass, ex); }

    return null;
  }

  @Override
  public <T extends BaseEntity> List<T> getEntitiesById(Class<T> entityClass, Collection<String> ids, boolean keepOrderingOfIds) {
    try {
      Dao dao = getDaoForClass(entityClass);

      if(dao != null) {
        QueryBuilder queryBuilder = dao.queryBuilder();
        queryBuilder.where().in(dao.getEntityConfig().getIdProperty().getColumnName(), ids);

        if(keepOrderingOfIds) {
          queryBuilder.orderByRaw(createOrderByStatementForGetEntitiesById(dao, ids));
        }

        return (List<T>) queryBuilder.query();
      }
    } catch(Exception ex) {
      log.error("Could not get Entities for Type " + entityClass, ex); }

    return new ArrayList<>();
  }

  protected String createOrderByStatementForGetEntitiesById(Dao dao, Collection<String> entityIds) {
    String orderBy = "instr(',";
    for(String id : entityIds) {
      orderBy += id + ",";
    }

    orderBy += "', ',' || ";

    // add id column name
    if(dao.getEntityConfig().getInheritance() != null) { // for inheritance entities we have to explicitly add entity's table name
      orderBy += dao.getEntityConfig().getTableName() + ".";
    }
    orderBy += dao.getEntityConfig().getIdProperty().getColumnName();

    orderBy += " || ',')";

    return orderBy;
  }

  @Override
  public boolean updateEntity(BaseEntity entity) {
    log.debug("Going to update Entity " + entity);
    return createOrUpdateEntity(entity);
  }

  protected boolean createOrUpdateEntity(BaseEntity entity) {
    try {
      Dao dao = getDaoForClass(entity.getClass());

      if(dao != null) {
        Dao.CreateOrUpdateStatus status = dao.createOrUpdate(entity);
        return status.getNumLinesChanged() > 0;
      }
    } catch(Exception ex) { log.error("Could not persisted created or updated entity " + entity, ex); }

    return false;
  }

  @Override
  public boolean deleteEntity(BaseEntity entity) {
    log.debug("Going to delete Entity " + entity);

    try {
      Dao dao = getDaoForClass(entity.getClass());

      if(dao != null) {
        int rowsAffected = dao.delete(entity);
        return rowsAffected > 0;
      }
    } catch(Exception ex) { log.error("Could not delete entity " + entity, ex); }

    return false;
  }


  private Dao getDaoForClass(Class entityClass) {
    if(mapEntityClassesToDaos.containsKey(entityClass) == false) {
      log.error("It was requested to persist or update Entity of type " + entityClass + ", but mapEntityClassesToDaos does not contain an Entry for this Entity");
      return null;
    }

    Dao dao = mapEntityClassesToDaos.get(entityClass);
    if(dao == null) {
      log.error("Dao for Entity of type " + entityClass + "is null even thought createAllDaos() has been called.");
    }

    return dao;
  }

  @Override
  public <T extends BaseEntity> List<T> getAllEntitiesOfType(Class<T> entityClass) {
    try {
      Dao dao = getDaoForClass(entityClass);

      if(dao != null) {
        return dao.queryForAll(); // TODO: add: WHERE DELETED=0, so that only undeleted entities will be retrieved
      }
    } catch(Exception ex) {
      log.error("Could not get all Entities for Type " + entityClass, ex); }

    return new ArrayList<>();
  }

  @Override
  public <T> Collection<T> sortReferenceBaseIds(Collection<T> referenceBaseIds) {
    // TODO: this is the same code as in OrmLiteAndroidEntityManager
    List<Long> resultIds = new ArrayList<>();

    try {
      String query = "SELECT b." + TableConfig.BaseEntityIdColumnName + " FROM " +
          TableConfig.ReferenceBaseTableName + " b " +
          ", " + TableConfig.ReferenceTableName + " r " +
          "WHERE b." + TableConfig.BaseEntityIdColumnName + " IN (";

      for (T id : referenceBaseIds)
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

      List<String[]> sortedIds = (List<String[]>) doNativeQuery(query);
      if (sortedIds.size() == resultIds.size()) {
        for (String[] id : sortedIds) {
          resultIds.add(Long.parseLong(id[0]));
        }
      }
      else {
        return referenceBaseIds;
      }
    } catch(Exception e) {
      log.error("Could not sort ReferenceBases search result IDs", e);
    }

    return (List<T>)resultIds;
  }

  @Override
  public Collection<Entry> findEntriesHavingTheseTags(Collection<Tag> tagsToFilterFor) {
    try {
      Dao dao = getDaoForClass(Entry.class);

      if(dao != null) {
        QueryBuilder queryBuilder = dao.queryBuilder();
        queryBuilder.where().raw(createWhereStatementForEntriesHavingTheseTags(tagsToFilterFor));
        return (List<Entry>) queryBuilder.query();
      }
    } catch(Exception ex) {
      log.error("Could not query for Entries to get all Entries having these Tags", ex); }

    return new ArrayList<>();
  }

  protected String createWhereStatementForEntriesHavingTheseTags(Collection<Tag> tagsToFilterFor) {
    String whereStatement =  TableConfig.EntryDeepThoughtJoinColumnName + " = " + Application.getDeepThought().getId();

    for(Tag tag : tagsToFilterFor) {
      whereStatement += " AND " + TableConfig.BaseEntityIdColumnName + " IN " + "(SELECT " + TableConfig.EntryTagJoinTableEntryIdColumnName + " FROM " +
          TableConfig.EntryTagJoinTableName + " WHERE " + TableConfig.EntryTagJoinTableTagIdColumnName + " = " + tag.getId() + ")";
    }

    return whereStatement;
  }

  @Override
  public void resolveAllLazyRelations(BaseEntity entity) throws Exception {

  }

  @Override
  public List doNativeQuery(String query) throws SQLException {
//    throw new UnsupportedOperationException("TODO: implement doNativeQuery() for OrmLite");

//    GenericRowMapper<Object> mapper = new GenericRowMapper<Object>() {
//      @Override
//      public Object mapRow(DatabaseResults results) throws SQLException {
//        int columnN = results.getColumnCount();
//        String[] result = new String[columnN];
//        for (int colC = 0; colC < columnN; colC++) {
//          result[colC] = results.(colC);
//        }
//        return result;
//      }
//    };

    GenericRawResults<String[]> rawResults = statementExecutor.queryRaw(connectionSource, query, new String[0], null);
    List results = rawResults.getResults();
    try { rawResults.close(); } catch(Exception ex) {
      log.warn("Could not close GenericRawResults object for query " + query, ex);
    }

    return results;
  }

  public String getDatabasePath() {
    return databasePath;
  }

}

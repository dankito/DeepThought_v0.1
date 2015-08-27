package net.deepthought.javase.db;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.jpa.EntityConfig;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.StatementExecutor;
import com.j256.ormlite.table.TableUtils;

import net.deepthought.Application;
import net.deepthought.data.model.Person;
import net.deepthought.data.persistence.EntityManagerConfiguration;
import net.deepthought.data.persistence.IEntityManager;
import net.deepthought.data.persistence.db.BaseEntity;
import net.deepthought.db.EntitiesConfigurator;

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

    statementExecutor = new StatementExecutor(connectionSource.getDatabaseType(), entities[0], entities[0].getDao());
  }

  protected void getDaosAndMayCreateTables(EntityManagerConfiguration configuration, EntityConfig[] entities) throws SQLException {
    for(EntityConfig entity : entities) {
      mapEntityClassesToDaos.put(entity.getEntityClass(), entity.getDao());

      if (configuration.getDataBaseCurrentDataModelVersion() == 0)
        TableUtils.createTableIfNotExists(this.connectionSource, entity.getEntityClass());
    }

    if (configuration.getDataBaseCurrentDataModelVersion() == 0)
      Application.getPreferencesStore().setDatabaseDataModelVersion(configuration.getApplicationDataModelVersion());
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

    mapEntityClassesToDaos.clear();
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
  public <T extends BaseEntity> T getEntityById(Class<T> entityClass, Long id) {
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
  public <T extends BaseEntity> List<T> getEntitiesById(Class<T> entityClass, Collection<Long> ids) {
    try {
      Dao dao = getDaoForClass(entityClass);

      if(dao != null) {
        QueryBuilder queryBuilder = dao.queryBuilder();
        queryBuilder.where().in(dao.getEntityConfig().getIdProperty().getColumnName(), ids);
        return (List<T>) queryBuilder.query();
      }
    } catch(Exception ex) {
      log.error("Could not get Entities for Type " + entityClass, ex); }

    return new ArrayList<>();
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
  public void resolveAllLazyRelations(BaseEntity entity) throws Exception {
     if(Application.getDeepThought() != null) {
       for(Person person : Application.getDeepThought().getPersons())
         person.getTextRepresentation();
     }
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

  @Override
  public <T extends BaseEntity> List<T> queryEntities(Class<T> entityClass, String whereStatement) throws SQLException {
    try {
      Dao dao = getDaoForClass(entityClass);

      if(dao != null) {
        QueryBuilder queryBuilder = dao.queryBuilder();
        queryBuilder.where().raw(whereStatement);
        return (List<T>) queryBuilder.query();
      }
    } catch(Exception ex) {
      log.error("Could not query for Entities for Type " + entityClass + " with where statement " + whereStatement, ex); }

    return new ArrayList<>();
  }

  public String getDatabasePath() {
    return databasePath;
  }

}

package net.deepthought.javase.db;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.jpa.EntityConfig;
import com.j256.ormlite.jpa.JpaEntityConfigurationReader;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.StatementExecutor;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import net.deepthought.Application;
import net.deepthought.data.model.Person;
import net.deepthought.data.persistence.EntityManagerConfiguration;
import net.deepthought.data.persistence.IEntityManager;
import net.deepthought.data.persistence.db.BaseEntity;

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

  protected final static String DbUpgradeBackupTablePrefix = "bak_";


  protected String databasePath = null;

//  // the DAO objects we use to access the DeepThought table
  protected Map<Class, Dao> mapEntityClassesToDaos = new HashMap<>();

  protected StatementExecutor statementExecutor;


  public OrmLiteJavaSeEntityManager(EntityManagerConfiguration configuration) throws SQLException {
    connectionSource = new JdbcConnectionSource(configuration.getDatabasePathIncludingDriverUrl());
    this.databasePath = configuration.getDataCollectionPersistencePath();

    EntityConfig[] entities = new JpaEntityConfigurationReader(connectionSource).readConfigurationAndCreateTablesIfNotExists(configuration.getEntityClasses());
    for(EntityConfig entity : entities)
      mapEntityClassesToDaos.put(entity.getEntityClass(), entity.getDao());

    statementExecutor = new StatementExecutor(connectionSource.getDatabaseType(), entities[0], entities[0].getDao());
  }

//  /**
//   * This is called when the database is first created. Usually you should call createTable statements here to create
//   * the tables that will store your data.
//   */
//  @Override
//  public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
//    try {
//      Log.i(OrmLiteAndroidEntityManager.class.getName(), "onCreate");
//
//      createAllTables(connectionSource);
//    } catch (SQLException e) {
//      Log.e(OrmLiteAndroidEntityManager.class.getName(), "Can't create database", e);
//      throw new RuntimeException(e);
//    }
////// here we try inserting data in the on-create as a test
////    RuntimeExceptionDao<Entry, Long> dao = getSimpleDataDao();
////// create some deepThought in the onCreate
////    Entry testEntry = new Entry("Test Entry", "Lorem ipsum");
//////    DeepThought deepThought = new DeepThought();
//////    deepThought.addAsAuthorOnEntry(testEntry);
//////    dao.create(deepThought);
////    dao.create(testEntry);
////    Log.i(OrmLiteDeepThoughtPersistenceManager.class.getName(), "created new deepThought in onCreate");
//  }
//
//  /**
//   * This is called when your application is upgraded and it has a higher version number. This allows you to adjust
//   * the various data to match the new version number.
//   */
//  @Override
//  public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {
//    // TODO: check which tables from oldVersion to newVersion really changed and only upgrade that ones
//    try {
//      Log.i(OrmLiteAndroidEntityManager.class.getName(), "onUpgrade");
//
//      List<String> tableNames = new ArrayList<>();
//      for(Class<? extends BaseEntity> entityClass : mapEntityClassesToDaos.keySet()) {
//        tableNames.add(TableConfig.getTableNameForClass(entityClass));
//      }
//
//      Map<String, List<String>> tablesColumnNames = backupTables(db, tableNames);
//
//      dropAllTables(connectionSource);
//
//// after we drop the old databases, we create the new ones
//      onCreate(db, connectionSource);
//      copyDataFromBackupTablesAndDropBackups(db, tablesColumnNames);
//    } catch (SQLException e) {
//      Log.e(OrmLiteAndroidEntityManager.class.getName(), "Can't drop databases", e);
//      throw new RuntimeException(e);
//    }
//  }

  protected void createAllTables(ConnectionSource connectionSource) throws SQLException {
    for(Class<? extends BaseEntity> entityClass : mapEntityClassesToDaos.keySet())
      TableUtils.createTable(connectionSource, entityClass);
  }

  protected void dropAllTables(ConnectionSource connectionSource) throws SQLException {
    for(Class<? extends BaseEntity> entityClass : mapEntityClassesToDaos.keySet()) {
      try {
        TableUtils.dropTable(connectionSource, entityClass, true);
      } catch(Exception ex) { log.error("Could not drop table for class " + entityClass, ex); }
    }
  }

//  protected Map<String, List<String>> backupTables(SQLiteDatabase db, List<String> tableNames) {
//    Map<String, List<String>> tablesColumnNames = new HashMap<>();
//
//    for(String tableName : tableNames) {
//      try {
//        List<String> tableColumnNames = backupTable(db, tableName);
//        tablesColumnNames.put(tableName, tableColumnNames); }
//      catch(Exception ex) { log.error("Could not backup table " + tableName, ex); }
//    }
//
//    return tablesColumnNames;
//  }
//
//  protected List<String> backupTable(SQLiteDatabase db, String tableName) {
//    List<String> columns = GetColumns(db, tableName);
//
//    String backupDatabaseStatement = "ALTER table " + tableName + " RENAME TO '" + DbUpgradeBackupTablePrefix + tableName + "'";
//    db.execSQL(backupDatabaseStatement);
//
//    return columns;
//  }
//
//  protected void copyDataFromBackupTablesAndDropBackups(SQLiteDatabase db, Map<String, List<String>> previousTablesColumnNames) {
//    for(String tableName : previousTablesColumnNames.keySet()) {
//      try {
//        copyDataFromBackupTableAndDrop(db, previousTablesColumnNames, tableName);
//      } catch(Exception ex) { log.error("Could not copyFile data from backup table " + tableName, ex); }
//    }
//  }
//
//  protected void copyDataFromBackupTableAndDrop(SQLiteDatabase db, Map<String, List<String>> previousTablesColumnNames, String tableName) {
//    List<String> tablePreviousColumns = previousTablesColumnNames.get(tableName);
//    tablePreviousColumns.retainAll(GetColumns(db, tableName)); // intersect previous and new column names
//
//    String columnsString = join(tablePreviousColumns, ",");
//    db.execSQL(String.format( "INSERT INTO %s (%s) SELECT %s from " + DbUpgradeBackupTablePrefix + "%s", tableName, columnsString, columnsString, tableName));
//
//    db.execSQL("DROP table " + DbUpgradeBackupTablePrefix + tableName);
//  }
//
//  public static List<String> GetColumns(SQLiteDatabase db, String tableName) {
//    List<String> ar = null;
//    Cursor c = null;
//    try {
//      c = db.rawQuery("select * from " + tableName + " limit 1", null);
//      if (c != null) {
//        ar = new ArrayList<String>(Arrays.asList(c.getColumnNames()));
//      }
//    } catch (Exception e) {
//      Log.v(tableName, e.getMessage(), e);
//      e.printStackTrace();
//    } finally {
//      if (c != null)
//        c.close();
//    }
//    return ar;
//  }
//
//  public static String join(List<String> list, String delim) {
//    StringBuilder buf = new StringBuilder();
//    int num = list.size();
//    for (int i = 0; i < num; i++) {
//      if (i != 0)
//        buf.append(delim);
//      buf.append((String) list.get(i));
//    }
//    return buf.toString();
//  }


  public void clearData() {
    try {
      dropAllTables(connectionSource);
      createAllTables(connectionSource);
    } catch (SQLException ex) {
      log.error("Could not clear data (drop all tables)", ex);
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


  /*      Versuch ein Backup der Datenbank zu erstellen      */

//  @Override
//  public void onOpen(SQLiteDatabase db) {
////    try {
////      makeBackupOfDatabase(db);
////    } catch(Exception ex) { log.error("Could not create a backup of database file " + db.getPath(), ex); }
//
//    this.databasePath = db.getPath();
//
//    super.onOpen(db);
//  }

//  protected void makeBackupOfDatabase(SQLiteDatabase db) {
//    String databasePath = db.getPath();
//
//    File maxBackupFile = new File(databasePath + DatabaseBackupDatabaseNameSuffix + MaxDatabaseBackupsToKeep);
//    if(maxBackupFile.exists())
//      maxBackupFile.delete();
//
//    for(int i = MaxDatabaseBackupsToKeep - 1; i > 0; i--) {
//      File backupFile = new File(databasePath + DatabaseBackupDatabaseNameSuffix + i);
//      if(backupFile.exists())
//        backupFile.renameTo(new File(databasePath + DatabaseBackupDatabaseNameSuffix + (i + 1)));
//    }
//
//    File currentDatabaseFile = new File(databasePath);
//    if(currentDatabaseFile.exists())
//      copyFile(currentDatabaseFile, new File(databasePath + DatabaseBackupDatabaseNameSuffix + 1));
//
//    tryToCopyToExternalStorage(db);
//  }

  public String getDatabasePath() {
    return databasePath;
  }

  private static String getDatabasePath(String databaseName) {
    return databaseName;
  }
}

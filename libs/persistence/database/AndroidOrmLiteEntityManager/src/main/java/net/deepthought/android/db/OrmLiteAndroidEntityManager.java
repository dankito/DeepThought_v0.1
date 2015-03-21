package net.deepthought.android.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.jpa.EntityConfig;
import com.j256.ormlite.jpa.JpaEntityConfigurationReader;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import net.deepthought.Application;
import net.deepthought.data.persistence.EntityManagerConfiguration;
import net.deepthought.data.persistence.IEntityManager;
import net.deepthought.data.persistence.db.BaseEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ganymed on 11/10/14.
 */
public class OrmLiteAndroidEntityManager extends OrmLiteSqliteOpenHelper implements IEntityManager {

  private final static Logger log = LoggerFactory.getLogger(OrmLiteAndroidEntityManager.class);

//  protected final static String DatabaseBackupDatabaseNameSuffix = "_bak";
//  protected final static int MaxDatabaseBackupsToKeep = 3;

  protected final static String DbUpgradeBackupTablePrefix = "bak_";


  // name of the database file for your application -- change to something appropriate for your app
  public static final String DATABASE_NAME = "DeepThought.db";


  protected String databasePath = null;

//  // the DAO objects we use to access the DeepThought table
  protected Map<Class, Dao> mapEntityClassesToDaos = new HashMap<>();


//  public OrmLiteAndroidEntityManager(Context context) throws SQLException {
//    super(context, getDatabasePath(DATABASE_NAME), null, Application.CurrentDataModelVersion/*, R.raw.ormlite_config*/);
//
//    this.databasePath = getDatabasePath(DATABASE_NAME);
//    setupEntities();
//
////    EntityConfig[] entities = new JpaEntityConfigurationReader(connectionSource).readConfiguration(new ArrayList<Class>(mapEntityClassesToDaos.keySet()).toArray(new Class[mapEntityClassesToDaos.size()]));
//
//    Instances.setFieldTypeCreator(new RelationFieldTypeCreator());
//    TableInfoRegistry.getInstance().createTableInfos(connectionSource, new ArrayList<Class>(mapEntityClassesToDaos.keySet()).toArray(new Class[mapEntityClassesToDaos.size()]));
//  }

  public OrmLiteAndroidEntityManager(Context context, EntityManagerConfiguration configuration) throws SQLException {
    super(context, getDatabasePath(DATABASE_NAME), null, Application.CurrentDataModelVersion/*, R.raw.ormlite_config*/); // TODO: get real database path (e.g. on SD Card) according to EntityManagerConfiguration

    this.databasePath = getDatabasePath(DATABASE_NAME);

//    EntityConfig[] entities = new JpaEntityConfigurationReader(connectionSource).readConfigurationAndCreateTablesIfNotExists(configuration.getEntityClasses());
    EntityConfig[] entities = new JpaEntityConfigurationReader(connectionSource).readConfiguration(configuration.getEntityClasses());
    for(EntityConfig entity : entities) {
      entity.setDao(new BaseDaoImpl(entity, connectionSource) { }); // TODO: create a new Dao only in one place in code
      mapEntityClassesToDaos.put(entity.getEntityClass(), entity.getDao());
    }
  }

//  protected void setupEntities() {
//    mapEntityClassesToDaos.put(DeepThoughtApplication.class, null);
////    mapEntityClassesToDaos.put(AppSettings.class, null);
//
//    mapEntityClassesToDaos.put(User.class, null);
//    mapEntityClassesToDaos.put(Device.class, null);
//    mapEntityClassesToDaos.put(Group.class, null);
//
//    mapEntityClassesToDaos.put(DeepThought.class, null);
//    mapEntityClassesToDaos.put(Category.class, null);
//    mapEntityClassesToDaos.put(Entry.class, null);
//    mapEntityClassesToDaos.put(EntriesLinkGroup.class, null);
//    mapEntityClassesToDaos.put(Tag.class, null);
//    mapEntityClassesToDaos.put(IndexTerm.class, null);
//    mapEntityClassesToDaos.put(Person.class, null);
//    mapEntityClassesToDaos.put(PersonRole.class, null);
//    mapEntityClassesToDaos.put(EntryPersonAssociation.class, null);
//
//    mapEntityClassesToDaos.put(Note.class, null);
//    mapEntityClassesToDaos.put(NoteType.class, null);
//    mapEntityClassesToDaos.put(FileLink.class, null);
//    mapEntityClassesToDaos.put(EntriesLinkGroup.class, null);
//
//    mapEntityClassesToDaos.put(ReferenceBase.class, null);
//    mapEntityClassesToDaos.put(ReferenceBasePersonAssociation.class, null);
//    mapEntityClassesToDaos.put(Reference.class, null);
//    mapEntityClassesToDaos.put(ReferenceCategory.class, null);
//    mapEntityClassesToDaos.put(ReferenceSubDivision.class, null);
//    mapEntityClassesToDaos.put(ReferenceSubDivisionCategory.class, null);
//    mapEntityClassesToDaos.put(SeriesTitle.class, null);
//    mapEntityClassesToDaos.put(SeriesTitleCategory.class, null);
//    mapEntityClassesToDaos.put(ReferenceIndicationUnit.class, null);
//    mapEntityClassesToDaos.put(Publisher.class, null);
//
//    mapEntityClassesToDaos.put(ApplicationLanguage.class, null);
//    mapEntityClassesToDaos.put(Language.class, null);
//    mapEntityClassesToDaos.put(BackupFileServiceType.class, null);
//  }

  /**
   * This is called when the database is first created. Usually you should call createTable statements here to create
   * the tables that will store your data.
   */
  @Override
  public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
    Log.i(OrmLiteAndroidEntityManager.class.getName(), "onCreate");

    for(Class entityClass : mapEntityClassesToDaos.keySet()) {
      try {
        TableUtils.createTableIfNotExists(connectionSource, entityClass);
      } catch(Exception ex) {
        log.error("Could not create Table for Entity " + entityClass);
      }
    }

//    try {
//      createAllTables(connectionSource);
//    } catch (SQLException e) {
//      Log.e(OrmLiteAndroidEntityManager.class.getName(), "Can't create database", e);
//      throw new RuntimeException(e);
//    }

//// here we try inserting data in the on-create as a test
//    RuntimeExceptionDao<Entry, Long> dao = getSimpleDataDao();
//// create some deepThought in the onCreate
//    Entry testEntry = new Entry("Test Entry", "Lorem ipsum");
////    DeepThought deepThought = new DeepThought();
////    deepThought.addAsAuthorOnEntry(testEntry);
////    dao.create(deepThought);
//    dao.create(testEntry);
//    Log.i(OrmLiteDeepThoughtPersistenceManager.class.getName(), "created new deepThought in onCreate");
  }

  /**
   * This is called when your application is upgraded and it has a higher version number. This allows you to adjust
   * the various data to match the new version number.
   */
  @Override
  public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {
    Log.i(OrmLiteAndroidEntityManager.class.getName(), "onUpgrade");

//    // TODO: check which tables from oldVersion to newVersion really changed and only upgrade that ones
//    try {
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
  }

//  protected void createAllTables(ConnectionSource connectionSource) throws SQLException {
//    for(Class<? extends BaseEntity> entityClass : mapEntityClassesToDaos.keySet())
//      TableUtils.createTable(connectionSource, entityClass);
//  }
//
//  protected void dropAllTables(ConnectionSource connectionSource) throws SQLException {
//    for(Class<? extends BaseEntity> entityClass : mapEntityClassesToDaos.keySet()) {
//      try {
//        TableUtils.dropTable(connectionSource, entityClass, true);
//      } catch(Exception ex) { log.error("Could not drop table for class " + entityClass, ex); }
//    }
//  }

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
//
//  protected void createAllDaos() throws SQLException {
//    boolean successful = true;
//
//    for(Class<? extends BaseEntity> entityClass : mapEntityClassesToDaos.keySet()) {
//      successful &= createDao(entityClass);
//    }
//  }

//  protected boolean createDao(Class<? extends BaseEntity> entityClass) throws SQLException {
//    try {
//      Dao dao = getDao(entityClass);
//      dao.setObjectCache(true);
////      dao.setAutoCommit(getConnectionSource().getReadWriteConnection(), true); // TODO: is this of any use (does it do anything)?
////      ((BaseDaoImpl)dao).initialize(); // TODO: why do i have to call this now, worked till now without
//
//      mapEntityClassesToDaos.put(entityClass, dao);
//      return true;
//    } catch(Exception ex) {
//      log.error("Could not create Dao for Class " + entityClass, ex);
//    }
//
//    return false;
//  }

//  /**
//   * Returns the RuntimeExceptionDao (Database Access Object) version of a Dao for our DeepThought class. It will
//   * create it or just give the cached value. RuntimeExceptionDao only through RuntimeExceptions.
//   */
//  public RuntimeExceptionDao<DeepThought, Long> getSimpleDataDao() {
//    if (deepThoughtRuntimeDao == null) {
//      deepThoughtRuntimeDao = getRuntimeExceptionDao(DeepThought.class);
//    }
//    return deepThoughtRuntimeDao;
//  }
//
//  public Dao<Category, Long> getCategoryDao() throws SQLException {
//    if (categoryDao == null) {
//      createAllDaos();
//    }
//
//    return categoryDao;
//  }
//
//  public Dao<Entry, Long> getEntryDao() throws SQLException {
//    if (entryDao == null) {
//      createAllDaos();
//    }
//
//    return entryDao;
//  }
//
//  public Dao<Tag, Long> getTagDao() throws SQLException {
//    if (tagDao == null) {
//      createAllDaos();
//    }
//
//    return tagDao;
//  }


//  public void clearData() {
//    try {
//      dropAllTables(getConnectionSource());
//      createAllTables(getConnectionSource());
//    } catch (SQLException ex) {
//      log.error("Could not clear data (drop all tables)", ex);
//    }
//  }

  /**
   * Close the database connections and clear any cached DAOs.
   */
  @Override
  public void close() {
    super.close();

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
//    if(dao == null) {
//      try {
//        createAllDaos();
//      } catch(Exception ex) {
//        log.error("Could not create all Daos", ex);
//      }
//    }
//
//    dao = mapEntityClassesToDaos.get(entityClass);

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
        return dao.queryForAll();
      }
    } catch(Exception ex) {
      log.error("Could not get all Entities for Type " + entityClass, ex); }

    return new ArrayList<>();
  }

  @Override
  public void resolveAllLazyRelations(BaseEntity entity) throws Exception {

  }

  @Override
  public List doNativeQuery(String query) {
    SQLiteDatabase readableDatabase = getReadableDatabase();
    Cursor cursor = readableDatabase.rawQuery(query, new String[0]);

    List rows = new ArrayList<String[]>();

    while(cursor.moveToNext()) {
      String[] row = new String[cursor.getColumnCount()];
      for(int i = 0; i < row.length; i++)
        row[i] = cursor.getString(i);
      rows.add(row);
    }

//    readableDatabase.close();

    return rows;
  }


  /*      Versuch ein Backup der Datenbank zu erstellen      */

  @Override
  public void onOpen(SQLiteDatabase db) {
//    try {
//      makeBackupOfDatabase(db);
//    } catch(Exception ex) { log.error("Could not create a backup of database file " + db.getPath(), ex); }

    this.databasePath = db.getPath();

    super.onOpen(db);
  }

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

  private void tryToCopyToExternalStorage(SQLiteDatabase db) {
    try {
      if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
        String filename = new File(db.getPath()).getName();
        DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd_HH:mm:ss");
        filename = filename.replace(".db", "_" + dateFormat.format(new Date()) + ".db");

        File folder = Environment.getExternalStorageDirectory();
        folder = new File(folder, "DeepThought");
        if(folder.exists() == false)
          folder.mkdir();

        folder = new File(folder, "db_backups");
        if(folder.exists() == false)
          folder.mkdir();

        copyFile(new File(db.getPath()), new File(folder, filename));
      }
    } catch(Exception ex) { log.error("Could not copyFile a Database Backup to External Storage", ex); }
  }

  public void copyFile(File source, File destination) {
    try {
      InputStream inputStream = new FileInputStream(source);
      OutputStream outputStream = new FileOutputStream(destination);

      // Transfer bytes from inputStream to outputStream
      byte[] buf = new byte[10 * 1024];
      int len;
      while ((len = inputStream.read(buf)) > 0) {
        outputStream.write(buf, 0, len);
      }

      inputStream.close();
      outputStream.close();
    } catch(Exception ex) { log.error("Could not copyFile file " + source + " to " + destination, ex); }
  }

  public String getDatabasePath() {
    return databasePath;
  }

  private static String getDatabasePath(String databaseName) {
    return databaseName;
  }
}

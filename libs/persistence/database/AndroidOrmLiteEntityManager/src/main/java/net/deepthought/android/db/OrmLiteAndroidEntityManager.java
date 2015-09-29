package net.deepthought.android.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.instances.Instances;
import com.j256.ormlite.jpa.EntityConfig;
import com.j256.ormlite.jpa.Registry;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import net.deepthought.Application;
import net.deepthought.data.persistence.EntityManagerConfiguration;
import net.deepthought.data.persistence.IEntityManager;
import net.deepthought.data.persistence.db.BaseEntity;
import net.deepthought.db.EntitiesConfigurator;
import net.deepthought.util.file.FileUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
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


  public OrmLiteAndroidEntityManager(Context context, EntityManagerConfiguration configuration) throws SQLException {
    super(context, getDatabasePath(DATABASE_NAME), null, Application.CurrentDataModelVersion/*, R.raw.ormlite_config*/); // TODO: get real database path (e.g. on SD Card) according to EntityManagerConfiguration

    this.databasePath = getDatabasePath(DATABASE_NAME);

//    deleteRegisteredDevice();

//    insertMissingColumns();

    EntitiesConfigurator configurator = new EntitiesConfigurator();
    EntityConfig[] entities = configurator.readEntityConfiguration(configuration, connectionSource);

    getDaosAndMayCreateTables(configuration, entities);

    if (configuration.getDataBaseCurrentDataModelVersion() != configuration.getApplicationDataModelVersion())
      Application.getPreferencesStore().setDatabaseDataModelVersion(configuration.getApplicationDataModelVersion());
  }

  protected void getDaosAndMayCreateTables(EntityManagerConfiguration configuration, EntityConfig[] entities) throws SQLException {
    for(EntityConfig entity : entities) {
      mapEntityClassesToDaos.put(entity.getEntityClass(), entity.getDao());
    }
  }

  protected void deleteRegisteredDevice() {
    try {
      SQLiteDatabase writableDatabase = getWritableDatabase();
      Cursor cursor = writableDatabase.rawQuery("SELECT * from device", new String[0]);
      List rows = new ArrayList<String[]>();

      while(cursor.moveToNext()) {
        String[] row = new String[cursor.getColumnCount()];
        for(int i = 0; i < row.length; i++)
          row[i] = cursor.getString(i);
        rows.add(row);
      }

      int countDevices = rows.size();
//      writableDatabase.execSQL("DELETE FROM user_device_join_table WHERE device_id = 2");
    } catch(Exception ex) {
      String error = ex.getMessage();
    }
  }

  protected void insertMissingColumns() {
    SQLiteDatabase writableDatabase = getWritableDatabase();

    try {
      String query = "ALTER TABLE user_dt ADD default_group BIGINT";
      writableDatabase.execSQL(query);
    } catch(Exception ex) {
      String error = ex.getMessage();
    }

    try {
      String query = "UPDATE user_dt SET default_group = 1 where id = 1";
      writableDatabase.execSQL(query);
    } catch(Exception ex) {
      String error = ex.getMessage();
    }

    try {
      String query = "ALTER TABLE device ADD device_icon longvarbinary";
      writableDatabase.execSQL(query);
    } catch(Exception ex) {
      String error = ex.getMessage();
    }

    try {
      String query = "ALTER TABLE device ADD platform_architecture VARCHAR";
      writableDatabase.execSQL(query);
    } catch(Exception ex) {
      String error = ex.getMessage();
    }
  }


  @Override
  public void onOpen(SQLiteDatabase db) {
    this.databasePath = db.getPath();

    super.onOpen(db);
  }

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
  }

  /**
   * This is called when your application is upgraded and it has a higher version number. This allows you to adjust
   * the various data to match the new version number.
   */
  @Override
  public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {
    Log.i(OrmLiteAndroidEntityManager.class.getName(), "onUpgrade");

//    // TODO: check which tables from oldVersion to newVersion really changed and only upgrade that ones
  }

  /**
   * Close the database connections and clear any cached DAOs.
   */
  @Override
  public void close() {
    super.close();

    for(Dao dao : mapEntityClassesToDaos.values()) {
      dao.clearObjectCache();
    }
    mapEntityClassesToDaos.clear();

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
    List<T> result = new ArrayList<>();

    try {
      Dao dao = getDaoForClass(entityClass);

      if(dao != null) {
        QueryBuilder queryBuilder = dao.queryBuilder();
        queryBuilder.where().in(dao.getEntityConfig().getIdProperty().getColumnName(), ids);
        List result2 = queryBuilder.query();
        return (List<T>)result2;
      }
    } catch(Exception ex) {
      log.error("Could not get Entities for Type " + entityClass, ex); }

    return result;
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
    FileUtils.copyFile(source, destination);
  }

  public String getDatabasePath() {
    return databasePath;
  }

  private static String getDatabasePath(String databaseName) {
    return databaseName;
  }
}

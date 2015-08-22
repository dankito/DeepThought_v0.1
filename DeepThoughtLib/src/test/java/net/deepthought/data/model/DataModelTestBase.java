package net.deepthought.data.model;

import net.deepthought.Application;
import net.deepthought.TestApplicationConfiguration;
import net.deepthought.data.helper.DatabaseHelper;
import net.deepthought.data.persistence.EntityManagerConfiguration;
import net.deepthought.data.persistence.IEntityManager;
import net.deepthought.data.persistence.db.TableConfig;
import net.deepthought.util.file.FileUtils;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import java.io.File;
import java.math.BigInteger;
import java.sql.Clob;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Vector;

/**
 * Created by ganymed on 09/11/14.
 */
public abstract class DataModelTestBase {

  public final static String StringWithMoreThan255CharactersLength = "üäöß_$§%&/()!°⁰^éè#'.:,;-_?+*~<>|\"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
  public final static String StringWithMoreThan2048CharactersLength = StringWithMoreThan255CharactersLength + StringWithMoreThan255CharactersLength + StringWithMoreThan255CharactersLength + StringWithMoreThan255CharactersLength + StringWithMoreThan255CharactersLength + StringWithMoreThan255CharactersLength + StringWithMoreThan255CharactersLength + StringWithMoreThan255CharactersLength + StringWithMoreThan255CharactersLength + StringWithMoreThan255CharactersLength;

  protected IEntityManager entityManager = null;
  protected EntityManagerConfiguration configuration = null;

  @Before
  public void setup() throws Exception {
//    configuration = EntityManagerConfiguration.createTestConfiguration(true);
//    FileUtils.deleteFile(configuration.getDataCollectionPersistencePath());
//    configuration.setCreateDatabase(true);

//    FileUtils.deleteFile(EntityManagerConfiguration.createDefaultConfiguration(new TestApplicationConfiguration()).getDataCollectionPersistencePath());

//    entityManager = getEntityManager(configuration);

    Application.instantiate(new TestApplicationConfiguration() {
      @Override
      public IEntityManager createEntityManager(EntityManagerConfiguration configuration) throws Exception {
        DataModelTestBase.this.configuration = configuration;
        return getEntityManager(configuration);
      }
    });

    entityManager = Application.getEntityManager();
  }

  @After
  public void tearDown() {
    entityManager.deleteEntity(Application.getApplication()); // damn, why doesn't it close the db properly? So next try: delete DeepThoughtApplication object
    entityManager.close();
    String databasePath = Application.getDataManager().getDataCollectionSavePath();
    Application.shutdown();

    boolean deletionResult = FileUtils.deleteFile(entityManager.getDatabasePath());
    Assert.assertTrue("Could not delete Test Database file", deletionResult);
  }


  protected abstract IEntityManager getEntityManager(EntityManagerConfiguration configuration) throws Exception;


  protected void deleteDatabaseFile() {
    File databaseFile = new File(Application.getDataManager().getDataCollectionSavePath());
  }

//  protected void clearDatabase() {
//    clearTable(TableConfig.AppSettingsTableName);
//    clearTable(TableConfig.UserTableName);
//    clearTable(TableConfig.DeepThoughtTableName);
//    clearTable(TableConfig.CategoryTableName);
//    clearTable(TableConfig.EntryTableName);
//    clearTable(TableConfig.EntryTableName);
//    clearTable(TableConfig.TagTableName);
//    clearTable(TableConfig.IndexTermTableName);
//    clearTable(TableConfig.DeepThoughtFavoriteEntryTemplateJoinTableName);
//  }

//  protected void clearTable(String tableName) {
//    entityManager.doNativeExecute("DROP TABLE " + tableName);
//  }


  protected Object[] getRowFromTable(String tableName, Long entityId) throws SQLException {
    return getRowFromTable(tableName, entityId, TableConfig.BaseEntityIdColumnName);
  }

  protected Object[] getRowFromTable(String tableName, Long entityId, String idColumnName) throws SQLException {
    List queryResult = entityManager.doNativeQuery("SELECT * FROM " + tableName + " WHERE " + idColumnName + "=" + entityId);
    Assert.assertEquals(1, queryResult.size()); // only one row fetched

    if(queryResult.get(0) instanceof Object[]) // H2
      return (Object[])queryResult.get(0);

    queryResult = (List)queryResult.get(0);
    Assert.assertNotEquals(0, queryResult.size()); // at least one field fetched
    return queryResult.toArray(new Object[queryResult.size()]);
  }

  protected String getClobFromTable(String tableName, String columnName, Long entityId) throws SQLException {
    Object rawObject = getValueFromTable(tableName, columnName, entityId, TableConfig.BaseEntityIdColumnName);

    String actual = null;
    if(rawObject instanceof Clob) {
      Clob clob = (Clob)rawObject;
      actual = clob.getSubString(1, (int)clob.length());
    }
    else if(rawObject != null)
      actual = rawObject.toString();

    return actual;
  }

  protected Object getValueFromTable(String tableName, String columnName, Long entityId) throws SQLException {
    return getValueFromTable(tableName, columnName, entityId, TableConfig.BaseEntityIdColumnName);
  }

  protected Object getValueFromTable(String tableName, String columnName, Long entityId, String idColumnName) throws SQLException {
    return DatabaseHelper.getValueFromTable(entityManager, tableName, columnName, entityId, idColumnName);
  }

  protected boolean doesJoinTableEntryExist(String tableName, String owningSideColumnName, Long owningSideId, String inverseSideColumnName, Long inverseSideId) throws SQLException {
    List<Object[]> result = entityManager.doNativeQuery("SELECT * FROM " + tableName + " WHERE " + owningSideColumnName + "=" + owningSideId +
                                                             " AND " + inverseSideColumnName + "=" + inverseSideId);
    return result.size() == 1;
  }


  protected static final DateFormat defaultDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");

  protected Date getDateValueFromTable(String tableName, String columnName, Long entityId) throws SQLException, ParseException {
    Object storedValue = getValueFromTable(tableName, columnName, entityId, TableConfig.BaseEntityIdColumnName);

    if(storedValue instanceof String) {
      return defaultDateFormat.parse((String)storedValue);
    }
    else if(storedValue instanceof Long)
      return new Date((Long)storedValue);

    return (Date)storedValue;
  }

  protected List getJoinTableEntries(String tableName, String entityColumnName, Long entityId, String otherSideColumnName) throws SQLException {
    return entityManager.doNativeQuery("SELECT " + otherSideColumnName + " FROM " + tableName + " WHERE " + entityColumnName + "=" + entityId);
  }

  protected boolean joinTableEntriesContainEntityId(Long entityId, List<Object> joinTableEntries) {
    for(Object id : joinTableEntries) {
      if(id instanceof Vector) // Toplink
        id = ((Vector)id).get(0);
      else if(id instanceof String[]) // OrmLite
        id = Long.parseLong(((String[])id)[0]);

      if(doIdsEqual(entityId, id))
        return true;
    }

    return false;
  }

  protected boolean doIdsEqual(Long entityId, Object id) {
    if(id instanceof BigInteger && entityId.equals(((BigInteger)id).longValue()))
      return true;
    if(id instanceof Integer && entityId.equals(((Integer)id).longValue()))
      return true;
    else if(entityId.equals(id))
      return true;

    return false;
  }

  protected String getClobString(String tableName, String columnName, Long entityId) throws Exception {
    Object storedValue = getValueFromTable(tableName, columnName, entityId);

    if(storedValue instanceof Clob) {
      Clob clob = (Clob) storedValue;
      return clob.getSubString(1, (int) clob.length());
    }

    return storedValue.toString();
  }

  protected void compareBoolValue(boolean expectedValue, Object storedValue) {
    if(storedValue instanceof Integer) {
      if(expectedValue == true)
        Assert.assertEquals(1, storedValue);
      else
        Assert.assertEquals(0, storedValue);
    }
    else
      Assert.assertEquals(expectedValue, storedValue);
  }

}

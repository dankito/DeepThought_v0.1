package net.deepthought.data.model;

import net.deepthought.Application;
import net.deepthought.data.helper.DatabaseHelper;
import net.deepthought.data.helper.TestDependencyResolver;
import net.deepthought.data.persistence.EntityManagerConfiguration;
import net.deepthought.data.persistence.IEntityManager;
import net.deepthought.data.persistence.db.TableConfig;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import java.io.File;
import java.math.BigInteger;
import java.sql.Clob;
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
    configuration = EntityManagerConfiguration.createTestConfiguration(true);
//    FileUtils.deleteFile(configuration.getDataCollectionPersistencePath());
//    configuration.setCreateDatabase(true);

    entityManager = getEntityManager(configuration);

    Application.instantiate(new TestDependencyResolver(entityManager));
  }

  @After
  public void tearDown() {
//    String databasePath = Application.getDataManager().getDataCollectionSavePath();
    Application.shutdown();
//    FileUtils.deleteFile(databasePath);
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


  protected Object[] getRowFromTable(String tableName, Long entityId) {
    return getRowFromTable(tableName, entityId, TableConfig.BaseEntityIdColumnName);
  }

  protected Object[] getRowFromTable(String tableName, Long entityId, String idColumnName) {
    List queryResult = entityManager.doNativeQuery("SELECT * FROM " + tableName + " WHERE " + idColumnName + "=" + entityId);
    Assert.assertEquals(1, queryResult.size()); // only one row fetched

    if(queryResult.get(0) instanceof Object[]) // H2
      return (Object[])queryResult.get(0);

    queryResult = (List)queryResult.get(0);
    Assert.assertNotEquals(0, queryResult.size()); // at least one field fetched
    return queryResult.toArray(new Object[queryResult.size()]);
  }

  protected Object getValueFromTable(String tableName, String columnName, Long entityId) {
    return getValueFromTable(tableName, columnName, entityId, TableConfig.BaseEntityIdColumnName);
  }

  protected Object getValueFromTable(String tableName, String columnName, Long entityId, String idColumnName) {
    return DatabaseHelper.getValueFromTable(entityManager, tableName, columnName, entityId, idColumnName);
  }

  protected boolean doesJoinTableEntryExist(String tableName, String owningSideColumnName, Long owningSideId, String inverseSideColumnName, Long inverseSideId) {
    List<Object[]> result = entityManager.doNativeQuery("SELECT * FROM " + tableName + " WHERE " + owningSideColumnName + "=" + owningSideId +
                                                             " AND " + inverseSideColumnName + "=" + inverseSideId);
    return result.size() == 1;
  }

  protected List<Object> getJoinTableEntries(String tableName, String entityColumnName, Long entityId, String otherSideColumnName) {
    return entityManager.doNativeQuery("SELECT " + otherSideColumnName + " FROM " + tableName + " WHERE " + entityColumnName + "=" + entityId);
  }

  protected boolean joinTableEntriesContainEntityId(Long entityId, List<Object> joinTableEntries) {
    for(Object id : joinTableEntries) {
      if(id instanceof Vector) // Toplink
        id = ((Vector)id).get(0);

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
    Clob clob = (Clob)getValueFromTable(tableName, columnName, entityId);
    return clob.getSubString(1, (int)clob.length());
  }

}

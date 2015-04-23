package com.j256.ormlite.dao.cda.inheritance;

import com.j256.ormlite.BaseCoreTest;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.dao.cda.inheritance.helper.TableHelper;
import com.j256.ormlite.dao.cda.testmodel.InheritanceModel;
import com.j256.ormlite.misc.TableInfoRegistry;
import com.j256.ormlite.support.ConnectionSource;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

/**
 * Created by ganymed on 17/11/14.
 */
public class SingleTableTest extends BaseCoreTest {

  public final static Class[] singleTableEntities = {
      InheritanceModel.SingleTableInheritanceBaseEntity.class, InheritanceModel.SingleTableFirstDirectSubEntity.class,
      InheritanceModel.SingleTableSecondDirectSubEntity.class, InheritanceModel.SingleTableFirstDirectSubEntityChild.class
  };


  @BeforeClass
  public static void setupTestSuite() throws Exception {
    ConnectionSource connectionSource = createConnection();
    TableHelper.deleteAllTables(connectionSource);
    connectionSource.close();
  }

  @Before
  public void before() throws Exception {
    super.before();
  }

  @AfterClass
  public static void afterTestSuite() throws Exception {
    ConnectionSource connectionSource = createConnection();
    TableHelper.deleteAllTables(connectionSource);
    connectionSource.close();
  }


  @Test
  public void singleTableEntitiesHierarchy_OnlyOneTableGetsCreated() throws Exception {
    TableInfoRegistry.getInstance().createTableInfos(connectionSource, singleTableEntities);
    Dao<InheritanceModel.SingleTableInheritanceBaseEntity, Long> baseEntityDao = createDao(InheritanceModel.SingleTableInheritanceBaseEntity.class, true);
    Dao<InheritanceModel.SingleTableFirstDirectSubEntity, Long> firstDirectSubEntityDao = createDao(InheritanceModel.SingleTableFirstDirectSubEntity.class, true);
    Dao<InheritanceModel.SingleTableFirstDirectSubEntityChild, Long> firstDirectSubEntityChildDao = createDao(InheritanceModel.SingleTableFirstDirectSubEntityChild.class, true);
    Dao<InheritanceModel.SingleTableSecondDirectSubEntity, Long> secondDirectSubEntityDao = createDao(InheritanceModel.SingleTableSecondDirectSubEntity.class, true);

    GenericRawResults<String[]> rawResults = baseEntityDao.queryRaw("SELECT TABLE_NAME FROM information_schema.tables WHERE TABLE_TYPE='TABLE' AND TABLE_SCHEMA='PUBLIC'"); // query for user ('PUBLIC') tables without System Tables
    List<String[]> results = rawResults.getResults();
    Assert.assertEquals(1, results.size()); // assert that only one table has been created, not 4
    Assert.assertEquals(InheritanceModel.SingleTableInheritanceBaseEntityTableName, results.get(0)[0]);
  }

  @Test
  public void singleTableEntitiesHierarchy_TableContainsAllSubEntitiesFields() throws Exception {
    TableInfoRegistry.getInstance().createTableInfos(connectionSource, singleTableEntities);
    Dao<InheritanceModel.SingleTableInheritanceBaseEntity, Long> baseEntityDao = createDao(InheritanceModel.SingleTableInheritanceBaseEntity.class, true);
    Dao<InheritanceModel.SingleTableFirstDirectSubEntity, Long> firstDirectSubEntityDao = createDao(InheritanceModel.SingleTableFirstDirectSubEntity.class, true);
    Dao<InheritanceModel.SingleTableFirstDirectSubEntityChild, Long> firstDirectSubEntityChildDao = createDao(InheritanceModel.SingleTableFirstDirectSubEntityChild.class, true);
    Dao<InheritanceModel.SingleTableSecondDirectSubEntity, Long> secondDirectSubEntityDao = createDao(InheritanceModel.SingleTableSecondDirectSubEntity.class, true);

    GenericRawResults<String[]> rawResults = baseEntityDao.queryRaw("SELECT * FROM " + InheritanceModel.SingleTableInheritanceBaseEntityTableName);
    String[] columnNames = rawResults.getColumnNames();
    Assert.assertEquals(5, columnNames.length);
    rawResults.getResults(); // to close wrapped connection
  }

  @Test
  public void insertEntitiesIntoSingleTableInheritanceHierarchy_AllEntitiesGetPersisted() throws Exception {
    TableInfoRegistry.getInstance().createTableInfos(connectionSource, singleTableEntities);
    Dao<InheritanceModel.SingleTableInheritanceBaseEntity, Long> baseEntityDao = createDao(InheritanceModel.SingleTableInheritanceBaseEntity.class, true);
    Dao<InheritanceModel.SingleTableFirstDirectSubEntity, Long> firstDirectSubEntityDao = createDao(InheritanceModel.SingleTableFirstDirectSubEntity.class, true);
    Dao<InheritanceModel.SingleTableFirstDirectSubEntityChild, Long> firstDirectSubEntityChildDao = createDao(InheritanceModel.SingleTableFirstDirectSubEntityChild.class, true);
    Dao<InheritanceModel.SingleTableSecondDirectSubEntity, Long> secondDirectSubEntityDao = createDao(InheritanceModel.SingleTableSecondDirectSubEntity.class, true);

    InheritanceModel.SingleTableInheritanceBaseEntity baseEntity = new InheritanceModel.SingleTableInheritanceBaseEntity("Sehr basal");
    baseEntityDao.create(baseEntity);

    Assert.assertNotNull(baseEntity.getId());

    InheritanceModel.SingleTableFirstDirectSubEntity firstDirectSubEntity = new InheritanceModel.SingleTableFirstDirectSubEntity("First Sub");
    firstDirectSubEntityDao.create(firstDirectSubEntity);

    Assert.assertNotNull(firstDirectSubEntity.getId());

    InheritanceModel.SingleTableSecondDirectSubEntity secondDirectSubEntity = new InheritanceModel.SingleTableSecondDirectSubEntity("Second Sub");
    secondDirectSubEntityDao.create(secondDirectSubEntity);

    Assert.assertNotNull(secondDirectSubEntity.getId());

    InheritanceModel.SingleTableFirstDirectSubEntityChild firstDirectSubEntityChild = new InheritanceModel.SingleTableFirstDirectSubEntityChild("First Sub Child");
    firstDirectSubEntityChildDao.create(firstDirectSubEntityChild);

    Assert.assertNotNull(firstDirectSubEntityChild.getId());

    baseEntityDao.clearObjectCache();

    GenericRawResults<String[]> rawResults = baseEntityDao.queryRaw("SELECT * FROM " + InheritanceModel.SingleTableInheritanceBaseEntityTableName);
    List<String[]> results = rawResults.getResults();
    Assert.assertEquals(4, results.size());

    List<InheritanceModel.SingleTableInheritanceBaseEntity> persistedEntities = baseEntityDao.queryForAll();
    Assert.assertEquals(4, persistedEntities.size());
  }
}

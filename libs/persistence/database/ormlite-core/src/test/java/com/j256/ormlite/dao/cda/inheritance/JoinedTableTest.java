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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by ganymed on 17/11/14.
 */
public class JoinedTableTest extends BaseCoreTest {

  private final static Logger log = LoggerFactory.getLogger(JoinedTableTest.class);


  public final static Class[] joinedTableEntities = {
      InheritanceModel.JoinedTableInheritanceBaseEntity.class, InheritanceModel.JoinedTableFirstDirectSubEntity.class,
      InheritanceModel.JoinedTableSecondDirectSubEntity.class, InheritanceModel.JoinedTableFirstDirectSubEntityChild.class
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
  public void joinedTableEntitiesHierarchy_AllFourTablesGetCreated() throws Exception {
    TableInfoRegistry.getInstance().createTableInfos(connectionSource, joinedTableEntities);
    Dao<InheritanceModel.JoinedTableInheritanceBaseEntity, Long> baseEntityDao = createDao(InheritanceModel.JoinedTableInheritanceBaseEntity.class, true);
    Dao<InheritanceModel.JoinedTableFirstDirectSubEntity, Long> firstDirectSubEntityDao = createDao(InheritanceModel.JoinedTableFirstDirectSubEntity.class, true);
    Dao<InheritanceModel.JoinedTableFirstDirectSubEntityChild, Long> firstDirectSubEntityChildDao = createDao(InheritanceModel.JoinedTableFirstDirectSubEntityChild.class, true);
    Dao<InheritanceModel.JoinedTableSecondDirectSubEntity, Long> secondDirectSubEntityDao = createDao(InheritanceModel.JoinedTableSecondDirectSubEntity.class, true);

    GenericRawResults<String[]> rawResults = baseEntityDao.queryRaw("SELECT TABLE_NAME FROM information_schema.tables WHERE TABLE_TYPE='TABLE' AND TABLE_SCHEMA='PUBLIC'"); // query for user ('PUBLIC') tables without System Tables
    List<String[]> results = rawResults.getResults();
    Assert.assertEquals(joinedTableEntities.length, results.size()); // assert that 4 table have been created
  }

  @Test
  public void joinedTableEntitiesHierarchy_BaseEntityTableContainsOnlyItsFieldsIncludingDiscriminatorColumn() throws Exception {
    TableInfoRegistry.getInstance().createTableInfos(connectionSource, joinedTableEntities);
    Dao<InheritanceModel.JoinedTableInheritanceBaseEntity, Long> baseEntityDao = createDao(InheritanceModel.JoinedTableInheritanceBaseEntity.class, true);

    GenericRawResults<String[]> rawResults = baseEntityDao.queryRaw("SELECT * FROM " + InheritanceModel.JoinedTableInheritanceBaseEntityTableName);
    String[] columnNames = rawResults.getColumnNames();
    Assert.assertEquals(InheritanceModel.JoinedTableInheritanceBaseEntityColumnsCount + 1, columnNames.length); // + 1 for Discriminator column
    rawResults.getResults(); // to close wrapped connection
  }

  @Test
  public void joinedTableEntitiesHierarchy_FirstDirectSubEntityTableContainsOnlyItsFieldsIncludingIdColumn() throws Exception {
    TableInfoRegistry.getInstance().createTableInfos(connectionSource, joinedTableEntities);
    Dao<InheritanceModel.JoinedTableFirstDirectSubEntity, Long> firstDirectSubEntityDao = createDao(InheritanceModel.JoinedTableFirstDirectSubEntity.class, true);

    GenericRawResults<String[]> rawResults = firstDirectSubEntityDao.queryRaw("SELECT * FROM " + InheritanceModel.JoinedTableInheritanceFirstDirectSubEntityTableName);
    String[] columnNames = rawResults.getColumnNames();
    Assert.assertEquals(InheritanceModel.JoinedTableInheritanceFirstDirectSubEntityColumnsCount + 1, columnNames.length); // + 1 for id column
    rawResults.getResults(); // to close wrapped connection
  }

  @Test
  public void joinedTableEntitiesHierarchy_FirstDirectSubChildEntityTableContainsOnlyItsFieldsIncludingIdColumn() throws Exception {
    TableInfoRegistry.getInstance().createTableInfos(connectionSource, joinedTableEntities);
    Dao<InheritanceModel.JoinedTableFirstDirectSubEntityChild, Long> firstDirectSubChildEntityDao = createDao(InheritanceModel.JoinedTableFirstDirectSubEntityChild.class, true);

    GenericRawResults<String[]> rawResults = firstDirectSubChildEntityDao.queryRaw("SELECT * FROM " + InheritanceModel.JoinedTableInheritanceFirstDirectSubChildEntityTableName);
    String[] columnNames = rawResults.getColumnNames();
    Assert.assertEquals(InheritanceModel.JoinedTableInheritanceFirstDirectSubChildEntityColumnsCount + 1, columnNames.length); // + 1 for id column
    rawResults.getResults(); // to close wrapped connection
  }

  @Test
  public void joinedTableEntitiesHierarchy_SecondDirectSubEntityTableContainsOnlyItsFieldsIncludingIdColumn() throws Exception {
    TableInfoRegistry.getInstance().createTableInfos(connectionSource, joinedTableEntities);
    Dao<InheritanceModel.JoinedTableSecondDirectSubEntity, Long> secondDirectSubEntityDao = createDao(InheritanceModel.JoinedTableSecondDirectSubEntity.class, true);

    GenericRawResults<String[]> rawResults = secondDirectSubEntityDao.queryRaw("SELECT * FROM " + InheritanceModel.JoinedTableInheritanceSecondDirectSubEntityTableName);
    String[] columnNames = rawResults.getColumnNames();
    Assert.assertEquals(InheritanceModel.JoinedTableInheritanceSecondDirectSubEntityColumnsCount + 1, columnNames.length); // + 1 for id column
    rawResults.getResults(); // to close wrapped connection
  }

  @Test
  public void insertEntitiesIntoJoinedTableInheritanceHierarchy_AllEntitiesGetPersistedWithUniqueIds() throws Exception {
    TableInfoRegistry.getInstance().createTableInfos(connectionSource, joinedTableEntities);
    Dao<InheritanceModel.JoinedTableInheritanceBaseEntity, Long> baseEntityDao = createDao(InheritanceModel.JoinedTableInheritanceBaseEntity.class, true);
    Dao<InheritanceModel.JoinedTableFirstDirectSubEntity, Long> firstDirectSubEntityDao = createDao(InheritanceModel.JoinedTableFirstDirectSubEntity.class, true);
    Dao<InheritanceModel.JoinedTableFirstDirectSubEntityChild, Long> firstDirectSubEntityChildDao = createDao(InheritanceModel.JoinedTableFirstDirectSubEntityChild.class, true);
    Dao<InheritanceModel.JoinedTableSecondDirectSubEntity, Long> secondDirectSubEntityDao = createDao(InheritanceModel.JoinedTableSecondDirectSubEntity.class, true);

    Set<Long> uniqueIds = new HashSet<>();

    InheritanceModel.JoinedTableInheritanceBaseEntity baseEntity = new InheritanceModel.JoinedTableInheritanceBaseEntity("Sehr basal");
    baseEntityDao.create(baseEntity);

    Assert.assertNotNull(baseEntity.getId());
    uniqueIds.add(baseEntity.getId());

    InheritanceModel.JoinedTableFirstDirectSubEntity firstDirectSubEntity = new InheritanceModel.JoinedTableFirstDirectSubEntity("First Sub");
    firstDirectSubEntityDao.create(firstDirectSubEntity);

    Assert.assertNotNull(firstDirectSubEntity.getId());
    uniqueIds.add(firstDirectSubEntity.getId()); // check if a unique ID has been created for firstDirectSubEntity
    Assert.assertEquals(2, uniqueIds.size());

    InheritanceModel.JoinedTableSecondDirectSubEntity secondDirectSubEntity = new InheritanceModel.JoinedTableSecondDirectSubEntity("Second Sub");
    secondDirectSubEntityDao.create(secondDirectSubEntity);

    Assert.assertNotNull(secondDirectSubEntity.getId());
    uniqueIds.add(secondDirectSubEntity.getId()); // check if a unique ID has been created for secondDirectSubEntity
    Assert.assertEquals(3, uniqueIds.size());

    InheritanceModel.JoinedTableFirstDirectSubEntityChild firstDirectSubEntityChild = new InheritanceModel.JoinedTableFirstDirectSubEntityChild("First Sub Child");
    firstDirectSubEntityChildDao.create(firstDirectSubEntityChild);

    Assert.assertNotNull(firstDirectSubEntityChild.getId());
    uniqueIds.add(firstDirectSubEntityChild.getId()); // check if a unique ID has been created for firstDirectSubEntityChild
    Assert.assertEquals(4, uniqueIds.size());
  }

  @Test
  public void insertEntitiesIntoJoinedTableInheritanceHierarchy_DiscriminatorColumnsHaveBeenSetCorrectly() throws Exception {
    TableInfoRegistry.getInstance().createTableInfos(connectionSource, joinedTableEntities);
    Dao<InheritanceModel.JoinedTableInheritanceBaseEntity, Long> baseEntityDao = createDao(InheritanceModel.JoinedTableInheritanceBaseEntity.class, true);
    // Daos below are not needed for inserting or reading, but for creating tables
    Dao<InheritanceModel.JoinedTableFirstDirectSubEntity, Long> firstDirectSubEntityDao = createDao(InheritanceModel.JoinedTableFirstDirectSubEntity.class, true);
    Dao<InheritanceModel.JoinedTableFirstDirectSubEntityChild, Long> firstDirectSubEntityChildDao = createDao(InheritanceModel.JoinedTableFirstDirectSubEntityChild.class, true);
    Dao<InheritanceModel.JoinedTableSecondDirectSubEntity, Long> secondDirectSubEntityDao = createDao(InheritanceModel.JoinedTableSecondDirectSubEntity.class, true);

    baseEntityDao.create(new InheritanceModel.JoinedTableInheritanceBaseEntity("Sehr basal"));
    baseEntityDao.create(new InheritanceModel.JoinedTableFirstDirectSubEntity("First Sub"));
    baseEntityDao.create(new InheritanceModel.JoinedTableSecondDirectSubEntity("Second Sub"));
    baseEntityDao.create(new InheritanceModel.JoinedTableFirstDirectSubEntityChild("First Sub Child"));

    baseEntityDao.clearObjectCache();

    GenericRawResults<String[]> rawResults = baseEntityDao.queryRaw("SELECT * FROM " + InheritanceModel.JoinedTableInheritanceBaseEntityTableName);
    List<String[]> results = rawResults.getResults();
    Assert.assertEquals(4, results.size());

    int discriminatorColumnIndex = -1;
    for(int i = 0; i < rawResults.getColumnNames().length; i++) {
      if(InheritanceModel.DiscriminatorColumnName.equals(rawResults.getColumnNames()[i])) {
        discriminatorColumnIndex = i;
        break;
      }
    }

    Assert.assertNotSame(-1, discriminatorColumnIndex); // assert Discriminator column has been added

    Assert.assertEquals(InheritanceModel.JoinedTableInheritanceBaseEntityDiscriminatorValue, results.get(0)[discriminatorColumnIndex]);
    Assert.assertEquals(InheritanceModel.JoinedTableInheritanceFirstDirectSubEntityDiscriminatorValue, results.get(1)[discriminatorColumnIndex]);
    Assert.assertEquals(InheritanceModel.JoinedTableInheritanceSecondDirectSubEntityDiscriminatorValue, results.get(2)[discriminatorColumnIndex]);
    Assert.assertEquals(InheritanceModel.JoinedTableInheritanceFirstDirectSubChildEntityDiscriminatorValue, results.get(3)[discriminatorColumnIndex]);
  }

  @Test
  public void queryForInsertEntities_CorrectClassesGetCreated() throws Exception {
    TableInfoRegistry.getInstance().createTableInfos(connectionSource, joinedTableEntities);
    Dao<InheritanceModel.JoinedTableInheritanceBaseEntity, Long> baseEntityDao = createDao(InheritanceModel.JoinedTableInheritanceBaseEntity.class, true);
    // Daos below are not needed for inserting or reading, but for creating tables
    Dao<InheritanceModel.JoinedTableFirstDirectSubEntity, Long> firstDirectSubEntityDao = createDao(InheritanceModel.JoinedTableFirstDirectSubEntity.class, true);
    Dao<InheritanceModel.JoinedTableFirstDirectSubEntityChild, Long> firstDirectSubEntityChildDao = createDao(InheritanceModel.JoinedTableFirstDirectSubEntityChild.class, true);
    Dao<InheritanceModel.JoinedTableSecondDirectSubEntity, Long> secondDirectSubEntityDao = createDao(InheritanceModel.JoinedTableSecondDirectSubEntity.class, true);

    baseEntityDao.create(new InheritanceModel.JoinedTableInheritanceBaseEntity("Sehr basal"));
    baseEntityDao.create(new InheritanceModel.JoinedTableFirstDirectSubEntity("First Sub"));
    baseEntityDao.create(new InheritanceModel.JoinedTableSecondDirectSubEntity("Second Sub", "Ich titutliere mich nicht"));
    baseEntityDao.create(new InheritanceModel.JoinedTableFirstDirectSubEntityChild("First Sub Child", 42));

    baseEntityDao.clearObjectCache();

    List<InheritanceModel.JoinedTableInheritanceBaseEntity> persistedEntities = baseEntityDao.queryForAll();
    Assert.assertEquals(4, persistedEntities.size());

    Assert.assertTrue(persistedEntities.get(0) instanceof InheritanceModel.JoinedTableInheritanceBaseEntity);
    Assert.assertTrue(persistedEntities.get(1) instanceof InheritanceModel.JoinedTableFirstDirectSubEntity);
    Assert.assertTrue(persistedEntities.get(2) instanceof InheritanceModel.JoinedTableSecondDirectSubEntity);
    Assert.assertTrue(persistedEntities.get(3) instanceof InheritanceModel.JoinedTableFirstDirectSubEntityChild);
  }
}

package com.j256.ormlite.jpa.crud.query;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.jpa.EntityConfig;
import com.j256.ormlite.jpa.crud.JpaCrudTestBase;

import org.junit.Assert;
import org.junit.Test;

import java.sql.SQLException;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

/**
 * Created by ganymed on 11/03/15.
 */
public class QueryInheritanceTest extends JpaCrudTestBase {

  private final static String TestName = "test";


  @Entity
  @Inheritance(strategy = InheritanceType.JOINED)
  static class JoinedBase {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    protected String firstName = TestName;
  }

  @Entity
  static class JoinedDirectSubEntity1 extends JoinedBase {
    protected String lastName = TestName;
  }

  @Entity
  static class JoinedDirectSubEntity2 extends JoinedBase {
    protected String middleName = TestName;
  }

  @Test
  public void queryEmptyJoinedSubEntityTable() throws SQLException {
    EntityConfig[] entities = entityConfigurationReader.readConfigurationAndCreateTablesIfNotExists(JoinedBase.class, JoinedDirectSubEntity1.class, JoinedDirectSubEntity2.class);

    Dao subEntity1Dao = entities[1].getDao();
    List<JoinedDirectSubEntity1> storedPojos1 = subEntity1Dao.queryForAll();
    Assert.assertEquals(0, storedPojos1.size());

    Dao subEntity2Dao = entities[2].getDao();
    List<JoinedDirectSubEntity2> storedPojos2 = subEntity2Dao.queryForAll();
    Assert.assertEquals(0, storedPojos2.size());
  }

  @Test
  public void insertObjectsAndThenQueryJoinedSubEntityTable() throws SQLException {
    EntityConfig[] entities = entityConfigurationReader.readConfigurationAndCreateTablesIfNotExists(JoinedBase.class, JoinedDirectSubEntity1.class, JoinedDirectSubEntity2.class);

    Dao subEntity1Dao = entities[1].getDao();
    subEntity1Dao.setObjectCache(false);
    Dao subEntity2Dao = entities[2].getDao();
    subEntity2Dao.setObjectCache(false);

    JoinedDirectSubEntity1 sub1_1 = new JoinedDirectSubEntity1();
    JoinedDirectSubEntity1 sub1_2 = new JoinedDirectSubEntity1();
    int result1_1 = subEntity1Dao.create(sub1_1);
    int result1_2 = subEntity1Dao.create(sub1_2);

    JoinedDirectSubEntity2 sub2_1 = new JoinedDirectSubEntity2();
    JoinedDirectSubEntity2 sub2_2 = new JoinedDirectSubEntity2();
    int result2_1 = subEntity2Dao.create(sub2_1);
    int result2_2 = subEntity2Dao.create(sub2_2);

    Assert.assertEquals(1, result1_1);
    Assert.assertEquals(1, result1_2);
    Assert.assertEquals(1, result2_1);
    Assert.assertEquals(1, result2_2);

    List<JoinedDirectSubEntity1> storedPojos1 = subEntity1Dao.queryForAll();
    Assert.assertEquals(2, storedPojos1.size());

    List<JoinedDirectSubEntity2> storedPojos2 = subEntity2Dao.queryForAll();
    Assert.assertEquals(2, storedPojos2.size());

    JoinedDirectSubEntity1 storedPojo1_1 = storedPojos1.get(0);
    Assert.assertNotSame(sub1_1, storedPojo1_1); // assert that Objects really have been read from Db
    Assert.assertEquals(sub1_1.id, storedPojo1_1.id);
    Assert.assertEquals(sub1_1.firstName, storedPojo1_1.firstName);
    Assert.assertEquals(sub1_1.lastName, storedPojo1_1.lastName);

    JoinedDirectSubEntity1 storedPojo1_2 = storedPojos1.get(1);
    Assert.assertNotSame(sub1_2, storedPojo1_2); // assert that Objects really have been read from Db
    Assert.assertEquals(sub1_2.id, storedPojo1_2.id);
    Assert.assertEquals(sub1_2.firstName, storedPojo1_2.firstName);
    Assert.assertEquals(sub1_2.lastName, storedPojo1_2.lastName);

    JoinedDirectSubEntity2 storedPojo2_1 = storedPojos2.get(0);
    Assert.assertNotSame(sub2_1, storedPojo2_1); // assert that Objects really have been read from Db
    Assert.assertEquals(sub2_1.id, storedPojo2_1.id);
    Assert.assertEquals(sub2_1.firstName, storedPojo2_1.firstName);
    Assert.assertEquals(sub2_1.middleName, storedPojo2_1.middleName);

    JoinedDirectSubEntity2 storedPojo2_2 = storedPojos2.get(1);
    Assert.assertNotSame(sub2_2, storedPojo2_2); // assert that Objects really have been read from Db
    Assert.assertEquals(sub2_2.id, storedPojo2_2.id);
    Assert.assertEquals(sub2_2.firstName, storedPojo2_2.firstName);
    Assert.assertEquals(sub2_2.middleName, storedPojo2_2.middleName);
  }


}

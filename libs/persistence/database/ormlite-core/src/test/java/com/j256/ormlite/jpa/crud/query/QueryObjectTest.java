package com.j256.ormlite.jpa.crud.query;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.jpa.crud.JpaCrudTestBase;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Created by ganymed on 11/03/15.
 */
public class QueryObjectTest extends JpaCrudTestBase {

  @Entity
  static class TestPojo {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    protected String firstName;
    protected String lastName;

    public TestPojo() {
      this("test", "test");
    }

    public TestPojo(String firstName, String lastName) {
      this.firstName = firstName;
      this.lastName = lastName;
    }

    public Long getId() {
      return id;
    }

    public String getFirstName() {
      return firstName;
    }

    public void setFirstName(String firstName) {
      this.firstName = firstName;
    }

    public String getLastName() {
      return lastName;
    }

    public void setLastName(String lastName) {
      this.lastName = lastName;
    }
  }

  @Test
  public void queryEmptyTestPojoTable() throws SQLException {
    Dao pojoDao = buildConfigurationAndGetDao(TestPojo.class);
    List<TestPojo> storedPojos = pojoDao.queryForAll();

    Assert.assertEquals(0, storedPojos.size());
  }

  @Test
  public void insertObjectsAndThenQueryTestPojoTable() throws SQLException {
    Dao pojoDao = buildConfigurationAndGetDao(TestPojo.class);
    pojoDao.setObjectCache(false); // really query, so turn off cache

    TestPojo pojo1 = new TestPojo("test", "one");
    TestPojo pojo2 = new TestPojo("test", "two");

    int result1 = pojoDao.create(pojo1);
    int result2 = pojoDao.create(pojo2);

    Assert.assertEquals(1, result1);
    Assert.assertEquals(1, result2);
    Assert.assertNotNull(pojo1.getId());
    Assert.assertNotNull(pojo2.getId());

    List<TestPojo> storedPojos = pojoDao.queryForAll();

    Assert.assertEquals(2, storedPojos.size());
    Assert.assertNotSame(pojo1, storedPojos.get(0)); // assert that Objects really have been read from Db
    Assert.assertNotSame(pojo1, storedPojos.get(1));

    Assert.assertEquals("test", storedPojos.get(0).getFirstName());
    Assert.assertEquals("one", storedPojos.get(0).getLastName());
    Assert.assertEquals("test", storedPojos.get(1).getFirstName());
    Assert.assertEquals("two", storedPojos.get(1).getLastName());
  }


  public enum TestEnum { One, Two }

  @Entity
  static class TestPojoWithAllPossibleNonRelationDataTypes {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    protected boolean booleanPrimitive = true;
    protected Boolean booleanObj = true;
    // TODO: boolean int and boolean string

    protected byte bytePrimitive = 125;
    protected Byte byteObj = 125;
    protected byte[] bytePrimitiveArray = new byte[] { 56, 57, 58, 59, 60 };

    protected short shortPrimitive = 4711;
    protected Short shortObj = 4711;

    protected int intPrimitive = 90210;
    protected Integer integerObj = 90210;

    protected long longPrimitive = 10119700000L;
    protected Long longObj = 10119700000L;

    protected float floatPrimitive = 3.14F;
    protected Float floatObj = 3.14F;

    protected double doublePrimitive = 3.14;
    protected Double doubleObj = 3.14;

    protected char charPrimitive = 'C';
    protected Character characterObj = 'D';

    protected String stringObj = "test";

    protected TestEnum enumUnAnnotated = TestEnum.Two;
    @Enumerated(EnumType.ORDINAL)
    protected TestEnum enumAnnotatedWithOrdinal = TestEnum.One;
    @Enumerated(EnumType.STRING)
    protected TestEnum enumAnnotatedWithString = TestEnum.Two;

    @Temporal(TemporalType.DATE)
    protected Date dateDate = new Date(70, 0, 1, 0, 0, 0); // Unix epoch of course
    @Temporal(TemporalType.TIME)
    protected Date dateTime = new Date(70, 0, 1, 0, 0, 0);
    @Temporal(TemporalType.TIMESTAMP)
    protected Date dateTimestamp = new Date(70, 0, 1, 0, 0, 0);

    protected BigInteger bigInteger = new BigInteger("10119700000");

    protected BigDecimal bigDecimalUnAnnotated = new BigDecimal(doublePrimitive);
//    @Column(precision = 3, scale = 2) // TODO
    protected BigDecimal bigDecimalAnnotatedWithPrecisionAndScale = new BigDecimal(doublePrimitive);

    protected UUID uuid = new UUID(1, 1);

    public TestPojoWithAllPossibleNonRelationDataTypes() {

    }

    public Long getId() {
      return id;
    }
  }

  @Test
  public void queryEmptyTestPojoWithAllPossibleNonRelationDataTypesTable() throws SQLException {
    Dao pojoDao = buildConfigurationAndGetDao(TestPojoWithAllPossibleNonRelationDataTypes.class);
    List<TestPojo> storedPojos = pojoDao.queryForAll();

    Assert.assertEquals(0, storedPojos.size());
  }

  @Test
  public void insertObjectsAndThenQueryTestPojoWithAllPossibleNonRelationDataTypesTable() throws SQLException {
    Dao pojoDao = buildConfigurationAndGetDao(TestPojoWithAllPossibleNonRelationDataTypes.class);
    pojoDao.setObjectCache(false);

    TestPojoWithAllPossibleNonRelationDataTypes pojo1 = new TestPojoWithAllPossibleNonRelationDataTypes();
    TestPojoWithAllPossibleNonRelationDataTypes pojo2 = new TestPojoWithAllPossibleNonRelationDataTypes();

    int result1 = pojoDao.create(pojo1);
    int result2 = pojoDao.create(pojo2);

    Assert.assertEquals(1, result1);
    Assert.assertEquals(1, result2);
    Assert.assertNotNull(pojo1.getId());
    Assert.assertNotNull(pojo2.getId());

    List<TestPojoWithAllPossibleNonRelationDataTypes> storedPojos = pojoDao.queryForAll();

    Assert.assertEquals(2, storedPojos.size());
    Assert.assertNotSame(pojo1, storedPojos.get(0)); // assert that Objects really have been read from Db
    Assert.assertNotSame(pojo1, storedPojos.get(1));

    for(TestPojoWithAllPossibleNonRelationDataTypes storedPojo : storedPojos) {
      Assert.assertEquals(pojo1.booleanPrimitive, storedPojo.booleanPrimitive);
      Assert.assertEquals(pojo1.booleanObj, storedPojo.booleanObj);

      Assert.assertEquals(pojo1.bytePrimitive, storedPojo.bytePrimitive);
      Assert.assertEquals(pojo1.byteObj, storedPojo.byteObj);
      for(int i = 0; i < storedPojo.bytePrimitiveArray.length; i++)
        Assert.assertEquals(pojo1.bytePrimitiveArray[i], storedPojo.bytePrimitiveArray[i]);

      Assert.assertEquals(pojo1.shortPrimitive, storedPojo.shortPrimitive);
      Assert.assertEquals(pojo1.shortObj, storedPojo.shortObj);

      Assert.assertEquals(pojo1.intPrimitive, storedPojo.intPrimitive);
      Assert.assertEquals(pojo1.integerObj, storedPojo.integerObj);

      Assert.assertEquals(pojo1.longPrimitive, storedPojo.longPrimitive);
      Assert.assertEquals(pojo1.longObj, storedPojo.longObj);

      Assert.assertEquals(pojo1.floatPrimitive, storedPojo.floatPrimitive, 0);
      Assert.assertEquals(pojo1.floatObj, storedPojo.floatObj);

      Assert.assertEquals(pojo1.doublePrimitive, storedPojo.doublePrimitive, 0);
      Assert.assertEquals(pojo1.doubleObj, storedPojo.doubleObj);

      Assert.assertEquals(pojo1.charPrimitive, storedPojo.charPrimitive);
      Assert.assertEquals(pojo1.characterObj, storedPojo.characterObj);

      Assert.assertEquals(pojo1.stringObj, storedPojo.stringObj);

      Assert.assertEquals(pojo1.enumUnAnnotated, storedPojo.enumUnAnnotated);
      Assert.assertEquals(pojo1.enumAnnotatedWithOrdinal, storedPojo.enumAnnotatedWithOrdinal);
      Assert.assertEquals(pojo1.enumAnnotatedWithString, storedPojo.enumAnnotatedWithString);

      Assert.assertEquals(pojo1.dateDate, storedPojo.dateDate);
      Assert.assertEquals(pojo1.dateTime, storedPojo.dateTime);
      Assert.assertEquals(pojo1.dateTimestamp, storedPojo.dateTimestamp);

      Assert.assertEquals(pojo1.bigInteger, storedPojo.bigInteger);
      Assert.assertEquals(pojo1.bigDecimalUnAnnotated, storedPojo.bigDecimalUnAnnotated);
      Assert.assertEquals(pojo1.bigDecimalAnnotatedWithPrecisionAndScale, storedPojo.bigDecimalAnnotatedWithPrecisionAndScale);

      Assert.assertEquals(pojo1.uuid, storedPojo.uuid);
    }
  }
}

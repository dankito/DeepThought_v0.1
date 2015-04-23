package com.j256.ormlite.jpa.configuration.reader.annotations.entity;

import com.j256.ormlite.jpa.EntityConfig;
import com.j256.ormlite.jpa.JpaEntityConfigurationReader;
import com.j256.ormlite.jpa.configuration.reader.JpaConfigurationReaderTestBase;

import org.junit.Assert;
import org.junit.Test;

import java.sql.SQLException;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * Created by ganymed on 07/03/15.
 */
public class TableAnnotationTest extends JpaConfigurationReaderTestBase {

  private final static String TestEntityName = "test_entity";
  private final static String TestTableName = "test_table";
  private final static String TestCatalogName = "test_catalog";
  private final static String TestSchemaName = "test_schema";


  @Entity(name = TestEntityName)
  @Table(name = TestTableName)
  static class TestEntity { @Id protected Long id; }

  @Test
  public void nameAttributeIsSet_TableNameEqualsAnnotationNameAttribute() throws SQLException {
    EntityConfig[] configs = entityConfigurationReader.readConfiguration(new Class[] { TestEntity.class });

    Assert.assertEquals(TestTableName.toUpperCase(), configs[0].getTableName());
  }


  @Entity(name = TestEntityName)
  @Table(catalog = TestCatalogName)
  static class TestEntityWithCatalog { @Id protected Long id; }

  @Test(expected = SQLException.class)
  public void catalogAttributeIsSet_TableNameEqualsAnnotationNameAttribute() throws SQLException {
    try {
      EntityConfig[] configs = entityConfigurationReader.readConfiguration(new Class[] { TestEntityWithCatalog.class });

//      Assert.assertEquals(TestCatalogName, configs[0].getCatalogName());
    } catch(Exception ex) {
      Assert.assertTrue(ex.getMessage().endsWith(JpaEntityConfigurationReader.NotSupportedExceptionTrailMessage));
      throw ex;
    }

    Assert.fail("Should never come here as Exception must be thrown");
  }


  @Entity(name = TestEntityName)
  @Table(schema = TestSchemaName)
  static class TestEntityWithSchema { @Id protected Long id; }

  @Test(expected = SQLException.class)
  public void schemaAttributeIsSet_TableNameEqualsAnnotationNameAttribute() throws SQLException {
    try {
      EntityConfig[] configs = entityConfigurationReader.readConfiguration(new Class[] { TestEntityWithSchema.class });

//      Assert.assertEquals(TestSchemaName, configs[0].getSchemaName());
    } catch(Exception ex) {
      Assert.assertTrue(ex.getMessage().endsWith(JpaEntityConfigurationReader.NotSupportedExceptionTrailMessage));
      throw ex;
    }

    Assert.fail("Should never come here as Exception must be thrown");
  }


  @Entity(name = TestEntityName)
  @Table(uniqueConstraints = {
      @UniqueConstraint(columnNames = { "FirstName", "LastName" }),
      @UniqueConstraint(columnNames = { "LastName", "AccountNumber" })
  })
  static class TestEntityWithUniqueConstraints { @Id protected Long id; }

  @Test(expected = SQLException.class)
  public void uniqueConstraintsSet_ThrowsAnSQLExceptionCurrentlyNotSupported() throws SQLException {
    try {
      entityConfigurationReader.readConfiguration(new Class[]{TestEntityWithUniqueConstraints.class});
    } catch(Exception ex) {
      Assert.assertTrue(ex.getMessage().endsWith(JpaEntityConfigurationReader.NotSupportedExceptionTrailMessage));
      throw ex;
    }

    Assert.fail("Should never come here as Exception must be thrown");
  }


  @Entity(name = TestEntityName)
  @Table(indexes = {
      @Index(columnList = "FirstName"),
      @Index(columnList = "LastName")
  })
  static class TestEntityWithIndexes { @Id protected Long id; }

  @Test(expected = SQLException.class)
  public void indexesSet_ThrowsAnSQLExceptionCurrentlyNotSupported() throws SQLException {
    try {
      entityConfigurationReader.readConfiguration(new Class[]{TestEntityWithIndexes.class});
    } catch(Exception ex) {
      Assert.assertTrue(ex.getMessage().endsWith(JpaEntityConfigurationReader.NotSupportedExceptionTrailMessage));
      throw ex;
    }

    Assert.fail("Should never come here as Exception must be thrown");
  }

}

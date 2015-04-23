package com.j256.ormlite.jpa.configuration.reader.annotations.property;

import com.j256.ormlite.jpa.PropertyConfig;
import com.j256.ormlite.jpa.configuration.reader.JpaConfigurationReaderTestBase;

import org.junit.Assert;
import org.junit.Test;

import java.sql.SQLException;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by ganymed on 07/03/15.
 */
public class ColumnAnnotationTest extends JpaConfigurationReaderTestBase {

  private final static String TestColumnName = "last_name";
  private final static String TestColumnDefinition = "varchar(255)";


  @Entity
  static class EntityWithoutColumnAnnotation {
    @Id protected Long id;

    protected String lastName;
  }

  @Test
  public void columnAnnotationNotSet_ColumnNameEqualsFieldName() throws SQLException, NoSuchFieldException {
    entityConfigurationReader.readConfiguration(new Class[]{ EntityWithoutColumnAnnotation.class });
    PropertyConfig propertyConfig = getPropertyConfigurationForField(EntityWithoutColumnAnnotation.class, "lastName");

    Assert.assertEquals("lastName".toUpperCase(), propertyConfig.getColumnName());
  }


  @Entity
  static class EntityWithColumnAnnotationNameSetToTestColumnName {
    @Id protected Long id;

    @Column(name = TestColumnName)
    protected String lastName;
  }

  @Test
  public void columnAnnotationNameSet_ColumnNameEqualsTestColumnName() throws SQLException, NoSuchFieldException {
    entityConfigurationReader.readConfiguration(new Class[]{ EntityWithColumnAnnotationNameSetToTestColumnName.class });
    PropertyConfig propertyConfig = getPropertyConfigurationForField(EntityWithColumnAnnotationNameSetToTestColumnName.class, "lastName");

    Assert.assertEquals(TestColumnName.toUpperCase(), propertyConfig.getColumnName());
  }


  @Entity
  static class EntityWithColumnAnnotationColumnDefinitionSetToTestColumnName {
    @Id protected Long id;

    @Column(columnDefinition = TestColumnDefinition)
    protected String lastName;
  }

  @Test
  public void columnAnnotationColumnDefinitionSet_ColumnDefinitionEqualsTestColumnDefinition() throws SQLException, NoSuchFieldException {
    entityConfigurationReader.readConfiguration(new Class[]{ EntityWithColumnAnnotationColumnDefinitionSetToTestColumnName.class });
    PropertyConfig propertyConfig = getPropertyConfigurationForField(EntityWithColumnAnnotationColumnDefinitionSetToTestColumnName.class, "lastName");

    Assert.assertEquals(TestColumnDefinition, propertyConfig.getColumnDefinition());
  }


  @Entity
  static class EntityWithColumnAnnotationUniqueSetToTrue {
    @Id protected Long id;

    @Column(unique = true)
    protected String lastName;
  }

  @Test
  public void columnAnnotationUniqueSet_UniqueIsTrue() throws SQLException, NoSuchFieldException {
    entityConfigurationReader.readConfiguration(new Class[]{ EntityWithColumnAnnotationUniqueSetToTrue.class });
    PropertyConfig propertyConfig = getPropertyConfigurationForField(EntityWithColumnAnnotationUniqueSetToTrue.class, "lastName");

    Assert.assertEquals(true, propertyConfig.isUnique());
  }


  @Entity
  static class EntityWithColumnAnnotationNullableSetToFalse {
    @Id protected Long id;

    @Column(nullable = false)
    protected String lastName;
  }

  @Test
  public void columnAnnotationNullableSet_CanBeNullIsFalse() throws SQLException, NoSuchFieldException {
    entityConfigurationReader.readConfiguration(new Class[]{ EntityWithColumnAnnotationNullableSetToFalse.class });
    PropertyConfig propertyConfig = getPropertyConfigurationForField(EntityWithColumnAnnotationNullableSetToFalse.class, "lastName");

    Assert.assertEquals(false, propertyConfig.canBeNull());
  }


  @Entity
  static class EntityWithColumnAnnotationInsertableSetToFalse {
    @Id protected Long id;

    @Column(insertable = false)
    protected String lastName;
  }

  @Test
  public void columnAnnotationInsertableSet_InsertableIsFalse() throws SQLException, NoSuchFieldException {
    entityConfigurationReader.readConfiguration(new Class[]{ EntityWithColumnAnnotationInsertableSetToFalse.class });
    PropertyConfig propertyConfig = getPropertyConfigurationForField(EntityWithColumnAnnotationInsertableSetToFalse.class, "lastName");

    Assert.assertEquals(false, propertyConfig.isInsertable());
  }


  @Entity
  static class EntityWithColumnAnnotationUpdatableSetToFalse {
    @Id protected Long id;

    @Column(updatable = false)
    protected String lastName;
  }

  @Test
  public void columnAnnotationUpdatableSet_UpdatableIsFalse() throws SQLException, NoSuchFieldException {
    entityConfigurationReader.readConfiguration(new Class[]{ EntityWithColumnAnnotationUpdatableSetToFalse.class });
    PropertyConfig propertyConfig = getPropertyConfigurationForField(EntityWithColumnAnnotationUpdatableSetToFalse.class, "lastName");

    Assert.assertEquals(false, propertyConfig.isUpdatable());
  }


  @Entity
  static class EntityWithColumnAnnotationLengthSetTo31 {
    @Id protected Long id;

    @Column(length = 31)
    protected String lastName;
  }

  @Test
  public void columnAnnotationLengthSet_LengthIs31() throws SQLException, NoSuchFieldException {
    entityConfigurationReader.readConfiguration(new Class[]{ EntityWithColumnAnnotationLengthSetTo31.class });
    PropertyConfig propertyConfig = getPropertyConfigurationForField(EntityWithColumnAnnotationLengthSetTo31.class, "lastName");

    Assert.assertEquals(31, propertyConfig.getLength());
  }

}

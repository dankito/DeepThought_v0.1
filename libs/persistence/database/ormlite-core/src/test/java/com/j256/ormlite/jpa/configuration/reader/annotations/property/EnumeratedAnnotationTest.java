package com.j256.ormlite.jpa.configuration.reader.annotations.property;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.jpa.PropertyConfig;
import com.j256.ormlite.jpa.configuration.reader.JpaConfigurationReaderTestBase;

import org.junit.Assert;
import org.junit.Test;

import java.sql.SQLException;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;

/**
 * Created by ganymed on 07/03/15.
 */
public class EnumeratedAnnotationTest extends JpaConfigurationReaderTestBase {

  public enum TestEnum { One, Two }


  @Entity
  static class EntityWithoutEnumeratedAnnotation {
    @Id protected Long id;

    protected TestEnum enumeration;
  }

  @Test
  public void enumeratedAnnotationNotSet_SqlTypeIsSetToInteger() throws SQLException, NoSuchFieldException {
    entityConfigurationReader.readConfiguration(new Class[]{ EntityWithoutEnumeratedAnnotation.class });
    PropertyConfig propertyConfig = getPropertyConfigurationForField(EntityWithoutEnumeratedAnnotation.class, "enumeration");

//    Assert.assertEquals(Integer.class, propertyConfig.getSqlType());
    Assert.assertEquals(DataType.ENUM_INTEGER, propertyConfig.getDataType());
  }


  @Entity
  static class EntityWithEnumeratedAnnotationSetToOrdinal {
    @Id protected Long id;

    @Enumerated(EnumType.ORDINAL) protected TestEnum enumeration;
  }

  @Test
  public void enumeratedAnnotationSetToOrdinal_SqlTypeIsSetToInteger() throws SQLException, NoSuchFieldException {
    entityConfigurationReader.readConfiguration(new Class[]{ EntityWithEnumeratedAnnotationSetToOrdinal.class });
    PropertyConfig propertyConfig = getPropertyConfigurationForField(EntityWithEnumeratedAnnotationSetToOrdinal.class, "enumeration");

//    Assert.assertEquals(Integer.class, propertyConfig.getSqlType());
    Assert.assertEquals(DataType.ENUM_INTEGER, propertyConfig.getDataType());
  }


  @Entity
  static class EntityWithEnumeratedAnnotationSetToString {
    @Id protected Long id;

    @Enumerated(EnumType.STRING) protected TestEnum enumeration;
  }

  @Test
  public void enumeratedAnnotationSetToString_SqlTypeIsSetToString() throws SQLException, NoSuchFieldException {
    entityConfigurationReader.readConfiguration(new Class[]{ EntityWithEnumeratedAnnotationSetToString.class });
    PropertyConfig propertyConfig = getPropertyConfigurationForField(EntityWithEnumeratedAnnotationSetToString.class, "enumeration");

//    Assert.assertEquals(String.class, propertyConfig.getSqlType());
    Assert.assertEquals(DataType.ENUM_STRING, propertyConfig.getDataType());
  }

}

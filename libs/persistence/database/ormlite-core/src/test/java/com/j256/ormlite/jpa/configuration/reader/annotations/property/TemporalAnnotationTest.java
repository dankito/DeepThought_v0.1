package com.j256.ormlite.jpa.configuration.reader.annotations.property;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.jpa.PropertyConfig;
import com.j256.ormlite.jpa.configuration.reader.JpaConfigurationReaderTestBase;

import org.junit.Assert;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Created by ganymed on 07/03/15.
 */
public class TemporalAnnotationTest extends JpaConfigurationReaderTestBase {

  @Entity
  static class EntityWithoutTemporalAnnotation {
    @Id protected Long id;

    protected Date date;
    protected Calendar calendar;
  }

  @Test
  public void temporalAnnotationNotSet_SqlTypeIsSetToSqlTimestamp() throws SQLException, NoSuchFieldException {
    entityConfigurationReader.readConfiguration(new Class[]{ EntityWithoutTemporalAnnotation.class });
    PropertyConfig datePropertyConfig = getPropertyConfigurationForField(EntityWithoutTemporalAnnotation.class, "date");
    PropertyConfig calendarPropertyConfig = getPropertyConfigurationForField(EntityWithoutTemporalAnnotation.class, "calendar");

//    Assert.assertEquals(java.sql.Timestamp.class, datePropertyConfig.getSqlType());
//    Assert.assertEquals(java.sql.Timestamp.class, calendarPropertyConfig.getSqlType());
    Assert.assertEquals(DataType.DATE_LONG, datePropertyConfig.getDataType());
    Assert.assertEquals(DataType.DATE_LONG, calendarPropertyConfig.getDataType());
  }


  @Entity
  static class EntityWithoutTemporalSetToDate {
    @Id protected Long id;

    @Temporal(TemporalType.DATE) protected Date date;
    @Temporal(TemporalType.DATE) protected Calendar calendar;
  }

  @Test
  public void temporalAnnotationSetToDate_SqlTypeIsSetToSqlDate() throws SQLException, NoSuchFieldException {
    entityConfigurationReader.readConfiguration(new Class[]{ EntityWithoutTemporalSetToDate.class });
    PropertyConfig datePropertyConfig = getPropertyConfigurationForField(EntityWithoutTemporalSetToDate.class, "date");
    PropertyConfig calendarPropertyConfig = getPropertyConfigurationForField(EntityWithoutTemporalSetToDate.class, "calendar");

//    Assert.assertEquals(java.sql.Date.class, datePropertyConfig.getSqlType());
//    Assert.assertEquals(java.sql.Date.class, calendarPropertyConfig.getSqlType());
    Assert.assertEquals(DataType.DATE, datePropertyConfig.getDataType());
    Assert.assertEquals(DataType.DATE, calendarPropertyConfig.getDataType());
  }


  @Entity
  static class EntityWithoutTemporalSetToTime {
    @Id protected Long id;

    @Temporal(TemporalType.TIME) protected Date date;
    @Temporal(TemporalType.TIME) protected Calendar calendar;
  }

  @Test
  public void temporalAnnotationSetToTime_SqlTypeIsSetToSqlTime() throws SQLException, NoSuchFieldException {
    entityConfigurationReader.readConfiguration(new Class[]{ EntityWithoutTemporalSetToTime.class });
    PropertyConfig datePropertyConfig = getPropertyConfigurationForField(EntityWithoutTemporalSetToTime.class, "date");
    PropertyConfig calendarPropertyConfig = getPropertyConfigurationForField(EntityWithoutTemporalSetToTime.class, "calendar");

//    Assert.assertEquals(java.sql.Time.class, datePropertyConfig.getSqlType());
//    Assert.assertEquals(java.sql.Time.class, calendarPropertyConfig.getSqlType());
    Assert.assertEquals(DataType.DATE_LONG, datePropertyConfig.getDataType());
    Assert.assertEquals(DataType.DATE_LONG, calendarPropertyConfig.getDataType());
  }


  @Entity
  static class EntityWithoutTemporalSetToTimestamp {
    @Id protected Long id;

    @Temporal(TemporalType.TIMESTAMP) protected Date date;
    @Temporal(TemporalType.TIMESTAMP) protected Calendar calendar;
  }

  @Test
  public void temporalAnnotationSetToTimestamp_SqlTypeIsSetToSqlTimestamp() throws SQLException, NoSuchFieldException {
    entityConfigurationReader.readConfiguration(new Class[]{ EntityWithoutTemporalSetToTimestamp.class });
    PropertyConfig datePropertyConfig = getPropertyConfigurationForField(EntityWithoutTemporalSetToTimestamp.class, "date");
    PropertyConfig calendarPropertyConfig = getPropertyConfigurationForField(EntityWithoutTemporalSetToTimestamp.class, "calendar");

//    Assert.assertEquals(java.sql.Timestamp.class, datePropertyConfig.getSqlType());
//    Assert.assertEquals(java.sql.Timestamp.class, calendarPropertyConfig.getSqlType());
    Assert.assertEquals(DataType.DATE_LONG, datePropertyConfig.getDataType());
    Assert.assertEquals(DataType.DATE_LONG, calendarPropertyConfig.getDataType());
  }

}

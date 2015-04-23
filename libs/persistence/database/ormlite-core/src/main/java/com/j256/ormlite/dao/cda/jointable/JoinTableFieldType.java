package com.j256.ormlite.dao.cda.jointable;

import com.j256.ormlite.field.DatabaseFieldConfig;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.jpa.PropertyConfig;
import com.j256.ormlite.jpa.relationconfig.ManyToManyConfig;
import com.j256.ormlite.support.ConnectionSource;

import java.lang.reflect.Field;
import java.sql.SQLException;

/**
 * Created by ganymed on 02/11/14.
 */
public class JoinTableFieldType extends PropertyConfig {

  protected Class dataType;


  public JoinTableFieldType(ManyToManyConfig manyToManyConfig, String fieldName, Class dataType) throws SQLException {
    this(manyToManyConfig.getJoinTableName(), fieldName, dataType,
        new JoinTableDatabaseFieldConfig(manyToManyConfig, fieldName, dataType));
  }

  public JoinTableFieldType(ManyToManyConfig manyToManyConfig, String fieldName, Class dataType, boolean isIdColumn, boolean autogenerateIds) throws SQLException {
    this(manyToManyConfig.getJoinTableName(), fieldName, dataType,
        new JoinTableDatabaseFieldConfig(manyToManyConfig, fieldName, dataType, isIdColumn, autogenerateIds));
  }

  protected JoinTableFieldType(String tableName, String fieldName, Class dataType, JoinTableDatabaseFieldConfig fieldConfig) throws
      SQLException {
    super(null, tableName, null, fieldConfig, null);
    setColumnName(fieldName);
    this.dataType = dataType;
    dataPersister = fieldConfig.getDataPersister();
  }

  @Override
  protected void setupFieldType(ConnectionSource connectionSource, String tableName, Field field, DatabaseFieldConfig fieldConfig) throws SQLException {
    // avoid that super.setupFieldType() gets called, would result in a NullPointerException
  }

  @Override
  public void configDaoInformation(ConnectionSource connectionSource, Class<?> parentClass) throws SQLException {
    // avoid that super.configDaoInformation() gets called, would result in a NullPointerException
  }

  @Override
  public Class<?> getType() {
    return dataType;
  }

  @Override
  public SqlType getSqlTypeOfFieldConverter() {
    return dataPersister.getSqlType();
  }

  @Override
  public String toString() {
    return columnName;
  }
}

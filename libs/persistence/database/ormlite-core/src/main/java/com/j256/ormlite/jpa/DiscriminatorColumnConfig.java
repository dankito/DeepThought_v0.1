package com.j256.ormlite.jpa;

import com.j256.ormlite.dao.ObjectCache;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseFieldConfig;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.jpa.inheritance.InheritanceEntityConfig;
import com.j256.ormlite.support.ConnectionSource;

import java.lang.reflect.Field;
import java.sql.SQLException;

import javax.persistence.DiscriminatorType;

public class DiscriminatorColumnConfig extends PropertyConfig {

//  protected Class dataType;


  public DiscriminatorColumnConfig(InheritanceEntityConfig entityConfig, String columnName, DiscriminatorType discriminatorType, int length, String columnDefinition) {
    super(entityConfig, columnName);

    setLength(length);
    setColumnDefinition(columnDefinition);

    switch(discriminatorType) {
      case INTEGER:
        this.type = Integer.class;
        setDataType(DataType.INTEGER);
        break;
      case CHAR:
        this.type = Character.class;
        setDataType(DataType.CHAR);
        break;
      default:
        this.type = String.class;
        setDataType(DataType.STRING);
        break;
    }
  }

//  public DiscriminatorColumnConfig(String tableName, ConnectionSource connectionSource, String discriminatorColumnName, DiscriminatorType type, int length) throws SQLException {
//    this(tableName, connectionSource, discriminatorColumnName, type, length, null);
//  }
//
//  public DiscriminatorColumnConfig(String tableName, ConnectionSource connectionSource, String discriminatorColumnName, DiscriminatorType type, int length, String columnDefinition) throws SQLException {
//    super(connectionSource, tableName, null, new DiscriminatorColumnFieldConfig(discriminatorColumnName, type, length, columnDefinition), null);
//    setColumnName(discriminatorColumnName);
//    this.dataPersister = fieldConfig.getDataPersister();
//    this.fieldConverter = connectionSource.getDatabaseType().getFieldConverter(dataPersister, this);
//
//    if(type == DiscriminatorType.STRING)
//      dataType = String.class;
//    else if(type == DiscriminatorType.CHAR)
//      dataType = Character.class;
//    else
//      dataType = Integer.class;
//  }

  @Override
  protected void setupFieldType(ConnectionSource connectionSource, String tableName, Field field, DatabaseFieldConfig fieldConfig) throws SQLException {
    // avoid that super.setupFieldType() gets called, would result in a NullPointerException
  }

  @Override
  public void configDaoInformation(ConnectionSource connectionSource, Class<?> parentClass) throws SQLException {
    // avoid that super.configDaoInformation() gets called, would result in a NullPointerException
  }

  @Override
  public void assignField(Object data, Object val, boolean parentObject, ObjectCache objectCache) throws SQLException {
    // no need to set discriminator field value (is of none interest for Entity object)
  }

  @Override
  public <FV> FV extractRawJavaFieldValue(Object object) throws SQLException {
//    return (FV)JavaxPersistenceImpl.GetEntityDiscriminatorValue(object.getClass());
    return (FV)((InheritanceEntityConfig)this.entityConfig).getDiscriminatorValueForEntity(Registry.getEntityRegistry().getEntityConfiguration(object.getClass()));
  }

//  @Override
//  public Class<?> getType() {
//    return dataType;
//  }

  @Override
  public SqlType getSqlTypeOfFieldConverter() {
    return dataPersister.getSqlType();
  }

  @Override
  public String toString() {
    return columnName;
  }
}

package com.j256.ormlite.jpa;

import com.j256.ormlite.dao.ObjectCache;
import com.j256.ormlite.field.DataPersister;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseFieldConfig;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.jpa.inheritance.InheritanceEntityConfig;
import com.j256.ormlite.support.ConnectionSource;

import java.lang.reflect.Field;
import java.sql.SQLException;

public class InheritanceSubTableIdPropertyConfig extends PropertyConfig {

  protected Class tableClass;
  protected PropertyConfig inheritanceHierarchyTopLevelIdProperty;

  protected EntityConfig inheritanceHierarchyEntity;

  public InheritanceSubTableIdPropertyConfig(EntityConfig subEntity, InheritanceEntityConfig inheritanceHierarchyEntity) {
    super(subEntity, "");

    this.inheritanceHierarchyEntity = inheritanceHierarchyEntity;
  }

  public InheritanceSubTableIdPropertyConfig(Class tableClass, String tableName, ConnectionSource connectionSource, PropertyConfig inheritanceHierarchyTopLevelIdProperty) throws SQLException {
    super(connectionSource, tableName, inheritanceHierarchyTopLevelIdProperty.getField(), inheritanceHierarchyTopLevelIdProperty.fieldConfig, tableClass);
    this.inheritanceHierarchyTopLevelIdProperty = inheritanceHierarchyTopLevelIdProperty;
    this.tableClass = tableClass;
    setColumnName(inheritanceHierarchyTopLevelIdProperty.getColumnName());
    this.dataPersister = inheritanceHierarchyTopLevelIdProperty.dataPersister;
    this.fieldConverter = inheritanceHierarchyTopLevelIdProperty.fieldConverter;

    mappedQueryForId = inheritanceHierarchyTopLevelIdProperty.mappedQueryForId;
    fieldGetMethod = inheritanceHierarchyTopLevelIdProperty.fieldGetMethod;
    fieldSetMethod = inheritanceHierarchyTopLevelIdProperty.fieldSetMethod;
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
  public void assignField(Object data, Object val, boolean parentObject, ObjectCache objectCache) throws SQLException {
    // no need to set id field value, it's already set on parent object
//    inheritanceHierarchyTopLevelIdProperty.assignField(data, val, parentObject, objectCache);
  }

  @Override
  public <FV> FV extractRawJavaFieldValue(Object object) throws SQLException {
    return getInheritanceHierarchyTopLevelIdProperty().extractRawJavaFieldValue(object);
  }

  @Override
  public Object convertJavaFieldToSqlArgValue(Object fieldVal) throws SQLException {
    return getInheritanceHierarchyTopLevelIdProperty().convertJavaFieldToSqlArgValue(fieldVal);
  }

  @Override
  public boolean isGeneratedId() {
    return false;
  } // TODO: return parent value?

  @Override
  public String getColumnName() {
    if(getInheritanceHierarchyTopLevelIdProperty() == null)
      return "id"; // TODO: this is a very bad work around (as on addProperty top level id property is forseeable still null), try to find a better solution
    return getInheritanceHierarchyTopLevelIdProperty().getColumnName();
  }

  @Override
  public String getColumnDefinition() {
    return getInheritanceHierarchyTopLevelIdProperty().getColumnDefinition();
  }

  @Override
  public Class<?> getType() {
    return getInheritanceHierarchyTopLevelIdProperty().getType();
  }

  @Override
  public SqlType getSqlTypeOfFieldConverter() {
    return getInheritanceHierarchyTopLevelIdProperty().getSqlTypeOfFieldConverter();
  }

  @Override
  public DataType getDataType() {
    return getInheritanceHierarchyTopLevelIdProperty().getDataType();
  }

  @Override
  public DataPersister getDataPersister() {
    if(dataPersister == null)
      dataPersister = getInheritanceHierarchyTopLevelIdProperty().getDataPersister();
    return dataPersister;
  }

  @Override
  public boolean isId() {
//    return getInheritanceHierarchyTopLevelIdProperty().isId();
    return true;
  }

  @Override
  public boolean isGeneratedIdSequence() {
    return getInheritanceHierarchyTopLevelIdProperty().isGeneratedIdSequence();
  }

  @Override
  public String getGeneratedIdSequence() {
    return getInheritanceHierarchyTopLevelIdProperty().getGeneratedIdSequence();
  }

//  public Class<?> getParentClass() {
//    return tableClass;
//  }

  @Override
  public Object getDefaultValue() {
    return getInheritanceHierarchyTopLevelIdProperty().getDefaultValue();
  }

  @Override
  public Object getJavaDefaultValueDefault() {
    return getInheritanceHierarchyTopLevelIdProperty().getJavaDefaultValueDefault();
  }

  @Override
  public Object getDataTypeConfigObj() {
    return getInheritanceHierarchyTopLevelIdProperty().getDataTypeConfigObj();
  }


  public PropertyConfig getInheritanceHierarchyTopLevelIdProperty() {
    if(inheritanceHierarchyTopLevelIdProperty == null)
      this.inheritanceHierarchyTopLevelIdProperty = inheritanceHierarchyEntity.getIdProperty();
    return inheritanceHierarchyTopLevelIdProperty;
  }

  @Override
  public String toString() {
    return "Inherited ID column for Entity " + getEntityConfig();
  }
}

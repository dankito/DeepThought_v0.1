package com.j256.ormlite.jpa.relationconfig;

import com.j256.ormlite.dao.cda.jointable.JoinTableConfig;
import com.j256.ormlite.dao.cda.jointable.JoinTableFieldType;
import com.j256.ormlite.jpa.EntityConfig;
import com.j256.ormlite.jpa.Property;
import com.j256.ormlite.jpa.PropertyConfig;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;

import java.lang.reflect.Field;
import java.sql.SQLException;

import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.JoinTable;

/**
 * Created by ganymed on 01/11/14.
 */
public class ManyToManyConfig extends AssociationConfig {

  protected Class owningSideClass;
  protected Field owningSideField;
  protected Property owningSideProperty;

  protected Class inverseSideClass;
  protected Field inverseSideField;
  protected Property inverseSideProperty;

  protected String joinTableName = null;
  protected String joinTableIdColumnName = "id";
  protected String joinTableOwningSideColumnName = null;
  protected String joinTableInverseSideColumnName = null;

  protected JoinTableFieldType idFieldType = null;
  protected JoinTableFieldType owningSideFieldType = null;
  protected JoinTableFieldType inverseSideFieldType = null;
  protected JoinTableFieldType[] fieldTypes = null;

  protected ConnectionSource connectionSource = null;
  protected DatabaseTableConfig tableConfig = null;
  protected EntityConfig entityConfig = null;


  public ManyToManyConfig(Field owningSideField, Field inverseSideField, FetchType fetch, CascadeType[] cascade) {
    this(owningSideField.getDeclaringClass(), owningSideField, inverseSideField.getDeclaringClass(), inverseSideField, fetch, cascade);
  }

  public ManyToManyConfig(Class owningSideClass, Field owningSideField, Class inverseSideClass, Field inverseSideField, FetchType fetch, CascadeType[] cascade) {
    super(fetch, cascade, true);

    this.owningSideClass = owningSideClass;
    this.owningSideField = owningSideField;
    this.inverseSideClass = inverseSideClass;
    this.inverseSideField = inverseSideField;

    this.isBidirectional = true;
  }

  public ManyToManyConfig(Property owningSideProperty, Class inverseSideClass, FetchType fetch, CascadeType[] cascade) {
    this(owningSideProperty.getDeclaringClass(), owningSideProperty.getField(), inverseSideClass, null, fetch, cascade);

    this.owningSideProperty = owningSideProperty;

    this.isBidirectional = false;
  }

  public ManyToManyConfig(Property owningSideProperty, Property inverseSideProperty, FetchType fetch, CascadeType[] cascade) {
    this(owningSideProperty.getDeclaringClass(), owningSideProperty.getField(), inverseSideProperty.getDeclaringClass(), inverseSideProperty.getField(), fetch, cascade);

    this.owningSideProperty = owningSideProperty;
    this.inverseSideProperty = inverseSideProperty;

    this.isBidirectional = true;
  }


  public boolean isOwningSideField(Field field) {
    return owningSideField.equals(field);
  }

  public Class getOtherSideClass(Field thisSide) {
    if(owningSideClass.equals(thisSide.getDeclaringClass()))
      return inverseSideClass;
    return owningSideClass;
  }

  public Field getOtherSideField(Field thisSide) {
    if(owningSideClass.equals(thisSide.getDeclaringClass()))
      return inverseSideField;
    return owningSideField;
  }

  public String getThisSideColumnName(Field thisSide) {
    if(owningSideClass.equals(thisSide.getDeclaringClass()))
      return getJoinTableOwningSideColumnName();
    return getJoinTableInverseSideColumnName();
  }

  public String getOtherSideColumnName(Field thisSide) {
    if(owningSideClass.equals(thisSide.getDeclaringClass()))
      return getJoinTableInverseSideColumnName();
    return getJoinTableOwningSideColumnName();
  }


  public Class getOwningSideClass() {
    return owningSideClass;
  }

  public Field getOwningSideField() {
    return owningSideField;
  }

  public Class getInverseSideClass() {
    return inverseSideClass;
  }

  public Field getInverseSideField() {
    return inverseSideField;
  }

  public String getJoinTableName() {
    if(joinTableName == null) {
      determineJoinTableSettings();
    }
    return joinTableName;
  }

  public String getJoinTableIdColumnName() {
    return joinTableIdColumnName;
  }

  public String getJoinTableOwningSideColumnName() {
    if(joinTableOwningSideColumnName == null)
      determineJoinTableSettings();

    return joinTableOwningSideColumnName;
  }

  public String getJoinTableInverseSideColumnName() {
    if(joinTableInverseSideColumnName == null)
      determineJoinTableSettings();

    return joinTableInverseSideColumnName;
  }

  protected void determineJoinTableSettings() {
    if(owningSideField.isAnnotationPresent(JoinTable.class)) {
      JoinTable joinTableAnnotation = owningSideField.getAnnotation(JoinTable.class);
      joinTableName = joinTableAnnotation.name();
      if(joinTableAnnotation.joinColumns().length != 0)
        joinTableOwningSideColumnName = joinTableAnnotation.joinColumns()[0].name();
      if(joinTableAnnotation.inverseJoinColumns().length != 0)
        joinTableInverseSideColumnName = joinTableAnnotation.inverseJoinColumns()[0].name();
    }
    else
      joinTableName = owningSideClass.getSimpleName().toLowerCase() + "_" + inverseSideClass.getSimpleName().toLowerCase() + "_join_table";

    if(joinTableOwningSideColumnName == null)
      joinTableOwningSideColumnName = owningSideClass.getSimpleName().toLowerCase() + "_id";
    if(joinTableInverseSideColumnName == null)
      joinTableInverseSideColumnName = inverseSideClass.getSimpleName().toLowerCase() + "_id";
  }

  public JoinTableFieldType getIdFieldType() throws SQLException {
    if(idFieldType == null)
      idFieldType = new JoinTableFieldType(this, getJoinTableIdColumnName(), Long.class, true, true);
    return idFieldType;
  }

  public JoinTableFieldType getOwningSideFieldType() throws SQLException {
    if(owningSideFieldType == null)
      owningSideFieldType = new JoinTableFieldType(this, getJoinTableOwningSideColumnName(), Long.class);
    return owningSideFieldType;
  }

  public JoinTableFieldType getInverseSideFieldType() throws SQLException {
    if(inverseSideFieldType == null)
      inverseSideFieldType = new JoinTableFieldType(this, getJoinTableInverseSideColumnName(), Long.class);
    return inverseSideFieldType;
  }

  public PropertyConfig[] getFieldTypes() throws SQLException {
    if(fieldTypes == null)
      fieldTypes = new JoinTableFieldType[] { getIdFieldType(), getOwningSideFieldType(), getInverseSideFieldType() };
    return fieldTypes;
  }

  public ConnectionSource getConnectionSource() {
    return connectionSource;
  }

  public void setConnectionSource(ConnectionSource connectionSource) {
    this.connectionSource = connectionSource;
  }

//  public DatabaseTableConfig getTableConfig() {
//    return tableConfig;
//  }
//
//  public void setTableConfig(DatabaseTableConfig tableConfig) {
//    this.tableConfig = tableConfig;
//  }

  public EntityConfig getEntityConfig() throws SQLException {
    if(entityConfig == null)
      entityConfig = new JoinTableConfig(this);
    return entityConfig;
  }

//  public void setTableInfo(TableInfo tableInfo) {
//    this.tableInfo = tableInfo;
//  }


  @Override
  public String toString() {
    return getJoinTableName();
  }
}

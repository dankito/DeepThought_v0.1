package com.j256.ormlite.instances;

import com.j256.ormlite.field.DatabaseFieldConfig;
import com.j256.ormlite.jpa.PropertyConfig;
import com.j256.ormlite.support.ConnectionSource;

import java.lang.reflect.Field;
import java.sql.SQLException;

/**
 * Created by ganymed on 15/10/14.
 */
public interface FieldTypeCreator {

  public PropertyConfig createFieldType(ConnectionSource connectionSource, String tableName, Field field, Class<?> parentClass) throws SQLException;

  public PropertyConfig createFieldType(ConnectionSource connectionSource, String tableName, Field field, DatabaseFieldConfig fieldConfig, Class<?> parentClass) throws SQLException;

  public boolean foreignCollectionCanBeAssignedToField(Class<?> fieldType);

}

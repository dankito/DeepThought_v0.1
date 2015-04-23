package com.j256.ormlite.instances;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.field.DatabaseFieldConfig;
import com.j256.ormlite.jpa.PropertyConfig;
import com.j256.ormlite.support.ConnectionSource;

import java.lang.reflect.Field;
import java.sql.SQLException;

/**
 * Just wraps OrmLite's default FieldType creation.
 *
 * Created by ganymed on 15/10/14.
 */
public class FieldTypeCreatorDefaultImpl implements FieldTypeCreator {

  @Override
  public PropertyConfig createFieldType(ConnectionSource connectionSource, String tableName, Field field, Class<?> parentClass) throws SQLException {
    DatabaseType databaseType = connectionSource.getDatabaseType();
    DatabaseFieldConfig fieldConfig = DatabaseFieldConfig.fromField(databaseType, tableName, field);
    if (fieldConfig == null) {
      return null;
    } else {
      return createFieldType(connectionSource, tableName, field, fieldConfig, parentClass);
    }
  }

  @Override
  public PropertyConfig createFieldType(ConnectionSource connectionSource, String tableName, Field field, DatabaseFieldConfig fieldConfig, Class<?> parentClass) throws SQLException {
    return new PropertyConfig(connectionSource, tableName, field, fieldConfig, parentClass);
  }

  @Override
  public boolean foreignCollectionCanBeAssignedToField(Class<?> fieldType) {
    return ForeignCollection.class.isAssignableFrom(fieldType);
  }

}

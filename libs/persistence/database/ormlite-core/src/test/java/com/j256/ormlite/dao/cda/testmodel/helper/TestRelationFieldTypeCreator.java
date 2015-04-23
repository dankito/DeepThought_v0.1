package com.j256.ormlite.dao.cda.testmodel.helper;

import com.j256.ormlite.field.DatabaseFieldConfig;
import com.j256.ormlite.instances.RelationFieldTypeCreator;
import com.j256.ormlite.jpa.PropertyConfig;
import com.j256.ormlite.support.ConnectionSource;

import java.lang.reflect.Field;
import java.sql.SQLException;

/**
 * Created by ganymed on 05/11/14.
 */
public class TestRelationFieldTypeCreator extends RelationFieldTypeCreator {

  @Override
  public PropertyConfig createFieldType(ConnectionSource connectionSource, String tableName, Field field, DatabaseFieldConfig fieldConfig, Class<?> parentClass) throws SQLException {
    return new TestRelationFieldType(connectionSource, tableName, field, fieldConfig, parentClass);
  }
}

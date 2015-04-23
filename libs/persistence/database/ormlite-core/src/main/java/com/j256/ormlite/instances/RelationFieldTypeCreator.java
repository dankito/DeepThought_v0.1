package com.j256.ormlite.instances;

import com.j256.ormlite.dao.cda.EntitiesCollection;
import com.j256.ormlite.field.DatabaseFieldConfig;
import com.j256.ormlite.jpa.PropertyConfig;
import com.j256.ormlite.jpa.RelationFieldType;
import com.j256.ormlite.support.ConnectionSource;

import java.lang.reflect.Field;
import java.sql.SQLException;

/**
 * Logic to create FieldTypes for my own ForeignCollection implementation.
 *
 * Created by ganymed on 15/10/14.
 */
public class RelationFieldTypeCreator extends FieldTypeCreatorDefaultImpl implements FieldTypeCreator {

  public RelationFieldTypeCreator() {

  }


  @Override
  public PropertyConfig createFieldType(ConnectionSource connectionSource, String tableName, Field field, DatabaseFieldConfig fieldConfig, Class<?> parentClass) throws SQLException {
    if(fieldConfig.isManyToManyField()) {
      return new RelationFieldType(connectionSource, tableName, field, fieldConfig, parentClass);
    }
    else if(fieldConfig.isForeignCollection())
      return new RelationFieldType(connectionSource, tableName, field, fieldConfig, parentClass);

    return super.createFieldType(connectionSource, tableName, field, fieldConfig, parentClass);
  }

  @Override
  public boolean foreignCollectionCanBeAssignedToField(Class<?> fieldType) {
    //return EntitiesCollection.class.isAssignableFrom(fieldType);
    return fieldType.isAssignableFrom(EntitiesCollection.class);
  }
}

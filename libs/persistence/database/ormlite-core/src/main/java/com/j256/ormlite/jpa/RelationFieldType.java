package com.j256.ormlite.jpa;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.cda.EntitiesCollection;
import com.j256.ormlite.dao.cda.LazyLoadingEntitiesCollection;
import com.j256.ormlite.dao.cda.ManyToManyEntitiesCollection;
import com.j256.ormlite.dao.cda.ManyToManyLazyLoadingEntitiesCollection;
import com.j256.ormlite.field.DatabaseFieldConfig;
import com.j256.ormlite.support.ConnectionSource;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Collection;

/**
 * Created by ganymed on 15/10/14.
 */
public class RelationFieldType extends PropertyConfig {

  /**
   *
   * @param connectionSource
   * @param tableName
   * @param field
   * @param fieldConfig
   * @param parentClass
   */
  public RelationFieldType(ConnectionSource connectionSource, String tableName, Field field, DatabaseFieldConfig fieldConfig, Class<?> parentClass) throws SQLException {
    super(connectionSource, tableName, field, fieldConfig, parentClass);
  }


  @Override
  protected void checkIfForeignCollectionTargetClassHasBeenFound(Field field, DatabaseFieldConfig fieldConfig) throws SQLException {
    if(fieldConfig.isManyToManyField() && fieldConfig.getManyToManyConfig() != null)
      return;

    super.checkIfForeignCollectionTargetClassHasBeenFound(field, fieldConfig);
  }

  @Override
  protected <FT, FID> Collection<FT> createLazyLoadingCollection(Object parent, FID id, Dao<FT, FID> dao, boolean queryForExistingCollectionItems) {
    if(fieldConfig.isManyToManyField())
      return new ManyToManyLazyLoadingEntitiesCollection<FT>(dao, foreignPropertyConfig, id, parent, fieldConfig.getManyToManyConfig());
    return new LazyLoadingEntitiesCollection<FT>(dao, foreignPropertyConfig, id, parent, fieldConfig.getOneToManyConfig());
  }

  @Override
  protected <FT, FID> Collection<FT> createEagerLoadingCollection(Object parent, FID id, Dao<FT, FID> dao, boolean queryForExistingCollectionItems) throws SQLException {
    if(fieldConfig.isManyToManyField())
      return new ManyToManyEntitiesCollection<FT>(dao, foreignPropertyConfig, id, parent, fieldConfig.getManyToManyConfig());
    return new EntitiesCollection<FT>(dao, foreignPropertyConfig, id, parent, fieldConfig.getOneToManyConfig());
  }

  @Override
  public boolean isForeignCollectionInstance(Object fieldInstance) {
    return fieldInstance instanceof EntitiesCollection;
  }
}

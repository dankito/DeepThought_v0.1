package com.j256.ormlite.dao.cda.testmodel.helper;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.cda.ManyToManyLazyLoadingEntitiesCollection;
import com.j256.ormlite.field.DatabaseFieldConfig;
import com.j256.ormlite.jpa.RelationFieldType;
import com.j256.ormlite.support.ConnectionSource;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Collection;

/**
 * Created by ganymed on 05/11/14.
 */
public class TestRelationFieldType extends RelationFieldType {
  /**
   * @param connectionSource
   * @param tableName
   * @param field
   * @param fieldConfig
   * @param parentClass
   */
  public TestRelationFieldType(ConnectionSource connectionSource, String tableName, Field field, DatabaseFieldConfig fieldConfig, Class<?> parentClass) throws SQLException {
    super(connectionSource, tableName, field, fieldConfig, parentClass);
  }

  @Override
  protected <FT, FID> Collection<FT> createLazyLoadingCollection(Object parent, FID id, Dao<FT, FID> dao, boolean queryForExistingCollectionItems) {
    if(fieldConfig.isManyToManyField())
      return new ManyToManyLazyLoadingEntitiesCollection<>(dao, foreignPropertyConfig, id, parent, fieldConfig.getManyToManyConfig());
    return new OpenLazyLoadingEntitiesCollection<FT>(dao, foreignPropertyConfig, id, parent, fieldConfig.getOneToManyConfig());
  }
}

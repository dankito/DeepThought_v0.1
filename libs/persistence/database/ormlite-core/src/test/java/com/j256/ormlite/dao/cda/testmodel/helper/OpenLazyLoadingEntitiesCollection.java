package com.j256.ormlite.dao.cda.testmodel.helper;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.cda.LazyLoadingEntitiesCollection;
import com.j256.ormlite.jpa.PropertyConfig;
import com.j256.ormlite.jpa.relationconfig.OneToManyConfig;

import java.util.List;
import java.util.Map;

/**
 * Created by ganymed on 05/11/14.
 */
public class OpenLazyLoadingEntitiesCollection<T> extends LazyLoadingEntitiesCollection<T> {

  public OpenLazyLoadingEntitiesCollection(Dao entityDao, PropertyConfig foreignPropertyConfig, Object parentId, Object parent, OneToManyConfig config) {
    super(entityDao, foreignPropertyConfig, parentId, parent, config);
  }


  public List<String> getRetrievedIndices() {
    return retrievedIndices;
  }

  public Map<String, T> getCachedEntities() {
    return cachedEntities;
  }

}

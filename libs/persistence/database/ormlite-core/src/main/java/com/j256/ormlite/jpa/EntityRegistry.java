package com.j256.ormlite.jpa;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ganymed on 07/03/15.
 */
public class EntityRegistry {

  protected Map<Class, EntityConfig> mapClassToTableInfo = new HashMap<>();

  public boolean hasEntityConfiguration(Class entityClass) {
    return mapClassToTableInfo.containsKey(entityClass);
  }

  public boolean registerEntityConfiguration(Class entityClass, EntityConfig entityConfiguration) {
    if(hasEntityConfiguration(entityClass) == false) {
      mapClassToTableInfo.put(entityClass, entityConfiguration);
      return true;
    }

    return false;
  }

  public EntityConfig getEntityConfiguration(Class entityClass) {
    return mapClassToTableInfo.get(entityClass);
  }

  public void clear() { // TODO: only needed for testing purposes, try to remove again
    mapClassToTableInfo.clear();
  }


  @Override
  public String toString() {
    return mapClassToTableInfo.size() + " entities registered";
  }
}

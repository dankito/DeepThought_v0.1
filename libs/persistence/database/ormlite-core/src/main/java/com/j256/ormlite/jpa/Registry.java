package com.j256.ormlite.jpa;

/**
 * Created by ganymed on 07/03/15.
 */
public class Registry {

  protected static EntityRegistry entityRegistry = null;

  protected static PropertyRegistry propertyRegistry = null;


  public static void setupRegistry(EntityRegistry entityRegistry, PropertyRegistry propertyRegistry) {
    Registry.entityRegistry = entityRegistry;
    Registry.propertyRegistry = propertyRegistry;
  }


  public static EntityRegistry getEntityRegistry() {
    if(entityRegistry == null)
      entityRegistry = new EntityRegistry();
    return entityRegistry;
  }

  public static void setEntityRegistry(EntityRegistry entityRegistry) {
    Registry.entityRegistry = entityRegistry;
  }

  public static PropertyRegistry getPropertyRegistry() {
    if(propertyRegistry == null)
      propertyRegistry = new PropertyRegistry();
    return propertyRegistry;
  }

  public static void setPropertyRegistry(PropertyRegistry propertyRegistry) {
    Registry.propertyRegistry = propertyRegistry;
  }
}

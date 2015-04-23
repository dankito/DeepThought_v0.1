package com.j256.ormlite.instances;

import com.j256.ormlite.dao.DaoManager;

/**
 * This is a very bad (but simple) way to make previous static classes configurable.
 *
 * Created by ganymed on 15/10/14.
 */
public class Instances {

  private static DaoManager daoManager = null;

  public static DaoManager getDaoManager() {
    if(daoManager == null)
      daoManager = new DaoManager(); // set to default value
    return daoManager;
  }

  public static void setDaoManager(DaoManager daoManagerInstance) {
    daoManager = daoManagerInstance;
  }

  private static FieldTypeCreator fieldTypeCreator = null;

  public static FieldTypeCreator getFieldTypeCreator() {
    if(fieldTypeCreator == null)
      fieldTypeCreator = new FieldTypeCreatorDefaultImpl(); // set to default value
    return fieldTypeCreator;
  }

  public static void setFieldTypeCreator(FieldTypeCreator fieldTypeCreatorInstance) {
    fieldTypeCreator = fieldTypeCreatorInstance;
  }

}

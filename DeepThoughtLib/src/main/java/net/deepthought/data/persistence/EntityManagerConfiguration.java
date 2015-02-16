package net.deepthought.data.persistence;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ganymed on 03/01/15.
 */
public class EntityManagerConfiguration {

  protected Class<? extends IEntityManager> entityManagerClass;

  protected String dataFolder = null;
  protected String dataCollectionFileName = null;
  protected String dataCollectionPersistencePath = null;
  protected boolean createDatabase = false;

  protected String databaseDriverUrl = null;
  protected String databaseDriver = null;

  protected String ddlGeneration = null;
  protected boolean createTables = false;
  protected boolean dropTables = false;


  public EntityManagerConfiguration() {

  }

  public EntityManagerConfiguration(Class<? extends IEntityManager> entityManagerClass) {
    this.entityManagerClass = entityManagerClass;
  }


  public Map<String, String> getEntityManagerConfiguration() {
    Map<String, String> properties = new HashMap<>();

    if(dataCollectionPersistencePath != null && getDatabaseDriverUrl() != null) {
      String url = dataCollectionPersistencePath;

      if(getDatabaseDriverUrl().toLowerCase().contains("h2")) {
        if (url.startsWith("/") == false && url.startsWith("./") == false) // relative paths not allowed for H2
          url = "./" + url;
        if(url.endsWith(".mv.db"))
          url = url.substring(0, url.length() - 6);
      }

      url = getDatabaseDriverUrl() + url;
      if(createDatabase == true && getDatabaseDriver().contains("derby"))
        url += ";create=true";

      properties.put("javax.persistence.jdbc.url", url);
      properties.put("eclipselink.jdbc.jdbc.url", url);
    }

    if(getDatabaseDriver() != null) {
      properties.put("javax.persistence.jdbc.driver", getDatabaseDriver());
      properties.put("eclipselink.jdbc.driver", getDatabaseDriver());
    }

    // TODO: for createDatabase, isn't there a hbm2ddl.auto value of create?
    if(createTables() == true) {
      if(dropTables() == true) {
        properties.put("hibernate.hbm2ddl.auto", "create-drop");
        properties.put("eclipselink.ddl-generation", "drop-and-create-tables");
      }
      else {
//        properties.put("hibernate.hbm2ddl.auto", "create-tables");
        properties.put("hibernate.hbm2ddl.auto", "update");
        properties.put("eclipselink.ddl-generation", "create-tables");
      }
    }

    return properties;
  }


  public EntityManagerConfiguration copy() {
    EntityManagerConfiguration copy = new EntityManagerConfiguration();
    copy.setDataFolder(dataFolder);
    copy.setDataCollectionFileName(dataCollectionFileName);

    copy.setDatabaseDriver(databaseDriver);
    copy.setDatabaseDriverUrl(databaseDriverUrl);

    copy.setCreateDatabase(createDatabase);
    copy.setCreateTables(createTables);
    copy.setDropTables(dropTables);

    return copy;
  }

  public Class<? extends IEntityManager> getEntityManagerClass() {
    return entityManagerClass;
  }

  public void setEntityManagerClass(Class<? extends IEntityManager> entityManagerClass) {
    this.entityManagerClass = entityManagerClass;
  }

  public String getDataFolder() {
    return dataFolder;
  }

  public void setDataFolder(String dataFolder) {
    this.dataFolder = dataFolder;
    this.dataCollectionPersistencePath = dataFolder + dataCollectionFileName;
  }

  public String getDataCollectionFileName() {
    return dataCollectionFileName;
  }

  public void setDataCollectionFileName(String dataCollectionFileName) {
    this.dataCollectionFileName = dataCollectionFileName;
    this.dataCollectionPersistencePath = dataFolder + dataCollectionFileName;
  }

  public String getDataCollectionPersistencePath() {
    if(databaseDriver != null && databaseDriver.toLowerCase().contains("h2") && dataCollectionPersistencePath.endsWith(".mv.db") == false)
      return dataCollectionPersistencePath + ".mv.db"; // H2 adds a '.mv.db' at end of file path
    return dataCollectionPersistencePath;
  }

  public void setDataCollectionPersistencePath(String dataCollectionPersistencePath) {
    this.dataCollectionPersistencePath = dataCollectionPersistencePath;
  }

  public boolean createDatabase() {
    return createDatabase;
  }

  public void setCreateDatabase(boolean createDatabase) {
    this.createDatabase = createDatabase;
  }

  public String getDatabaseDriverUrl() {
    return databaseDriverUrl;
  }

  public void setDatabaseDriverUrl(String databaseDriverUrl) {
    this.databaseDriverUrl = databaseDriverUrl;
  }

  public String getDatabaseDriver() {
    return databaseDriver;
  }

  public void setDatabaseDriver(String databaseDriver) {
    this.databaseDriver = databaseDriver;
  }

//  public String getDdlGeneration() {
//    return ddlGeneration;
//  }
//
//  public void setDdlGeneration(String ddlGeneration) {
//    this.ddlGeneration = ddlGeneration;
//  }


  public boolean createTables() {
    return createTables;
  }

  public void setCreateTables(boolean createTables) {
    this.createTables = createTables;
  }

  public boolean dropTables() {
    return dropTables;
  }

  public void setDropTables(boolean dropTables) {
    this.dropTables = dropTables;
  }


  public static EntityManagerConfiguration createDefaultConfiguration(String dataFolder) {
    return createDefaultConfiguration(dataFolder, false);
  }

  public static EntityManagerConfiguration createDefaultConfiguration(String dataFolder, boolean createTables) {
    EntityManagerConfiguration configuration = new EntityManagerConfiguration();

    // TODO: at this point of time Application.getDataFolderPath()  is for sure not set yet -> find a solution for that
    configuration.setDataFolder(dataFolder);

//    String databaseName = "DeepThoughtDb_SQLite.db";
//    String databaseName = "DeepThoughtDb_Derby";
    String databaseName = "DeepThoughtDb_H2.mv.db";
//    String databaseName = "DeepThoughtDb_HSQL/db";
//
    configuration.setDataCollectionFileName(databaseName);

//    configuration.setDatabaseDriverUrl("jdbc:sqlite:");
//    configuration.setDatabaseDriver("org.sqlite.JDBC");
//    configuration.setDatabaseDriverUrl("jdbc:derby:");
//    configuration.setDatabaseDriver("org.apache.derby.jdbc.EmbeddedDriver");
    configuration.setDatabaseDriverUrl("jdbc:h2:");
    configuration.setDatabaseDriver("org.h2.Driver");
//    configuration.setDatabaseDriverUrl("jdbc:hsqldb:file:");
//    configuration.setDatabaseDriver("org.hsqldb.jdbcDriver");

    if(new File(configuration.getDataCollectionPersistencePath()).exists() == false) { // TODO: apply the same for createDefaultConfiguration()
      configuration.setCreateDatabase(true);
      configuration.setCreateTables(true);
    }

    if(createTables)
      configuration.setCreateTables(true);

    return configuration;
  }

  public static EntityManagerConfiguration createTestConfiguration() {
    return createTestConfiguration("data/tests/");
  }

  public static EntityManagerConfiguration createTestConfiguration(String dataFolder) {
    return createTestConfiguration(dataFolder, false);
  }

  public static EntityManagerConfiguration createTestConfiguration(boolean createTables) {
    return createTestConfiguration("data/tests/", createTables);
  }

  public static EntityManagerConfiguration createTestConfiguration(String dataFolder, boolean createTables) {
    EntityManagerConfiguration configuration = createDefaultConfiguration(dataFolder, createTables);

//    String databaseName = "DeepThoughtDb_Tests_SQLite.db";
//    String databaseName = "DeepThoughtDb_Tests_Derby";
//    String databaseName = "DeepThoughtDb_Tests_H2";
    String databaseName = "DeepThoughtDb_Tests_H2.mv.db";
//    String databaseName = "DeepThoughtDb_HSQL_Tests";
//
    configuration.setDataCollectionFileName(databaseName);

//    configuration.setDatabaseDriverUrl("jdbc:h2:");
    configuration.setDatabaseDriverUrl("jdbc:h2:mem:");
    configuration.setDatabaseDriver("org.h2.Driver");

    configuration.setCreateDatabase(true);
    configuration.setCreateTables(true);
//    configuration.setDropTables(true);

    return configuration;
  }

}

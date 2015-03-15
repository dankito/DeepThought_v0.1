package net.deepthought.data.persistence;

import net.deepthought.data.model.Category;
import net.deepthought.data.model.DeepThought;
import net.deepthought.data.model.DeepThoughtApplication;
import net.deepthought.data.model.Device;
import net.deepthought.data.model.EntriesLinkGroup;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.EntryPersonAssociation;
import net.deepthought.data.model.FileLink;
import net.deepthought.data.model.Group;
import net.deepthought.data.model.IndexTerm;
import net.deepthought.data.model.Note;
import net.deepthought.data.model.Person;
import net.deepthought.data.model.Publisher;
import net.deepthought.data.model.Reference;
import net.deepthought.data.model.ReferenceBase;
import net.deepthought.data.model.ReferenceBasePersonAssociation;
import net.deepthought.data.model.ReferenceSubDivision;
import net.deepthought.data.model.SeriesTitle;
import net.deepthought.data.model.Tag;
import net.deepthought.data.model.User;
import net.deepthought.data.model.enums.ApplicationLanguage;
import net.deepthought.data.model.enums.BackupFileServiceType;
import net.deepthought.data.model.enums.Language;
import net.deepthought.data.model.enums.NoteType;
import net.deepthought.data.model.enums.PersonRole;
import net.deepthought.data.model.enums.ReferenceCategory;
import net.deepthought.data.model.enums.ReferenceIndicationUnit;
import net.deepthought.data.model.enums.ReferenceSubDivisionCategory;
import net.deepthought.data.model.enums.SeriesTitleCategory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ganymed on 03/01/15.
 */
public class EntityManagerConfiguration {
  
  public enum DatabaseType { SQLite, H2Embedded, H2Mem, Derby, HSQLDB }
  

  protected Class<? extends IEntityManager> entityManagerClass;
  
  protected Class[] entityClasses = new Class[0];

  protected String dataFolder = null;
  protected String dataCollectionFileName = null;
  protected String dataCollectionPersistencePath = null;
  protected boolean createDatabase = false;

  protected DatabaseType databaseType;
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

    if(getDatabasePathIncludingDriverUrl() != null) {
      properties.put("javax.persistence.jdbc.url", getDatabasePathIncludingDriverUrl());
      properties.put("eclipselink.jdbc.jdbc.url", getDatabasePathIncludingDriverUrl());
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

  public String getDatabasePathIncludingDriverUrl() {
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

      return url;
    }

    return null;
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

  public Class[] getEntityClasses() {
    return entityClasses;
  }

  public void setEntityClasses(Class[] entityClasses) {
    this.entityClasses = entityClasses;
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

  public DatabaseType getDatabaseType() {
    return databaseType;
  }

  public void setDatabaseType(DatabaseType databaseType) {
    this.databaseType = databaseType;
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
    return createDefaultConfiguration(dataFolder, createTables, DatabaseType.SQLite);
  }

  public static EntityManagerConfiguration createDefaultConfiguration(String dataFolder, boolean createTables, DatabaseType databaseType) {
    EntityManagerConfiguration configuration = new EntityManagerConfiguration();
    configuration.setDatabaseType(databaseType);

    // TODO: at this point of time Application.getDataFolderPath()  is for sure not set yet -> find a solution for that
    configuration.setDataFolder(dataFolder);

    switch(configuration.getDatabaseType()) {
      case SQLite:
        configuration.setDataCollectionFileName("DeepThoughtDb_SQLite.db");
        configuration.setDatabaseDriverUrl("jdbc:sqlite:");
        configuration.setDatabaseDriver("org.sqlite.JDBC");
        break;
      case H2Embedded:
        configuration.setDataCollectionFileName("DeepThoughtDb_H2.mv.db");
        configuration.setDatabaseDriverUrl("jdbc:h2:");
        configuration.setDatabaseDriver("org.h2.Driver");
        break;
      case H2Mem:
        configuration.setDataCollectionFileName("DeepThoughtDb_H2.mv.db");
        configuration.setDatabaseDriverUrl("jdbc:h2:mem:");
        configuration.setDatabaseDriver("org.h2.Driver");
        break;
      case Derby:
        configuration.setDataCollectionFileName("DeepThoughtDb_Derby");
        configuration.setDatabaseDriverUrl("jdbc:derby:");
        configuration.setDatabaseDriver("org.apache.derby.jdbc.EmbeddedDriver");
        break;
      case HSQLDB:
        configuration.setDataCollectionFileName("DeepThoughtDb_HSQL/db");
        configuration.setDatabaseDriverUrl("jdbc:hsqldb:file:");
        configuration.setDatabaseDriver("org.hsqldb.jdbcDriver");
        break;
    }

    if(new File(configuration.getDataCollectionPersistencePath()).exists() == false) { // TODO: apply the same for createDefaultConfiguration()
      configuration.setCreateDatabase(true);
      configuration.setCreateTables(true);
    }

    if(createTables)
      configuration.setCreateTables(true);
    
    addEntities(configuration);

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
    EntityManagerConfiguration configuration = createDefaultConfiguration(dataFolder, createTables, DatabaseType.H2Mem);

    switch(configuration.getDatabaseType()) {
      case SQLite:
        configuration.setDataCollectionFileName("DeepThoughtDb_Tests_SQLite.db");
        break;
      case H2Embedded:
      case H2Mem:
        configuration.setDataCollectionFileName("DeepThoughtDb_Tests_H2.mv.db");
        break;
      case Derby:
        configuration.setDataCollectionFileName("DeepThoughtDb_Tests_Derby");
        break;
      case HSQLDB:
        configuration.setDataCollectionFileName("DeepThoughtDb_HSQL_Tests");
        break;
    }

    configuration.setCreateDatabase(true);
    configuration.setCreateTables(true);
//    configuration.setDropTables(true);

    return configuration;
  }


  protected static void addEntities(EntityManagerConfiguration configuration) {
    Class[] entities = new Class[] {

        DeepThoughtApplication.class,

        User.class,
        Device.class,
        Group.class,
    
        DeepThought.class,
        Category.class,
        Entry.class,
        EntriesLinkGroup.class,
        Tag.class,
        IndexTerm.class,
        Person.class,
        PersonRole.class,
        EntryPersonAssociation.class,
    
        Note.class,
        NoteType.class,
        FileLink.class,
        EntriesLinkGroup.class,
    
        ReferenceBase.class,
        ReferenceBasePersonAssociation.class,
        Reference.class,
        ReferenceCategory.class,
        ReferenceSubDivision.class,
        ReferenceSubDivisionCategory.class,
        SeriesTitle.class,
        SeriesTitleCategory.class,
        ReferenceIndicationUnit.class,
        Publisher.class,
    
        ApplicationLanguage.class,
        Language.class,
        BackupFileServiceType.class

    };
    
    configuration.setEntityClasses(entities);
  }

}

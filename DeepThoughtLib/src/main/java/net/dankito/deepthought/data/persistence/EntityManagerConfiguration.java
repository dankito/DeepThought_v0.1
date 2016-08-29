package net.dankito.deepthought.data.persistence;

import net.dankito.deepthought.data.model.Category;
import net.dankito.deepthought.data.model.DeepThoughtApplication;
import net.dankito.deepthought.data.model.Device;
import net.dankito.deepthought.data.model.EntriesLinkGroup;
import net.dankito.deepthought.data.model.Entry;
import net.dankito.deepthought.data.model.EntryPersonAssociation;
import net.dankito.deepthought.data.model.FileLink;
import net.dankito.deepthought.data.model.Group;
import net.dankito.deepthought.data.model.Note;
import net.dankito.deepthought.data.model.Person;
import net.dankito.deepthought.data.model.Reference;
import net.dankito.deepthought.data.model.ReferenceBasePersonAssociation;
import net.dankito.deepthought.data.model.ReferenceSubDivision;
import net.dankito.deepthought.data.model.SeriesTitle;
import net.dankito.deepthought.data.model.Tag;
import net.dankito.deepthought.data.model.User;
import net.dankito.deepthought.data.model.enums.ApplicationLanguage;
import net.dankito.deepthought.data.model.enums.BackupFileServiceType;
import net.dankito.deepthought.data.model.enums.FileType;
import net.dankito.deepthought.data.model.enums.Language;
import net.dankito.deepthought.data.model.enums.NoteType;
import net.dankito.deepthought.Application;
import net.dankito.deepthought.data.model.DeepThought;
import net.dankito.deepthought.data.model.ReferenceBase;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ganymed on 03/01/15.
 */
public class EntityManagerConfiguration {
  
  public enum DatabaseType { SQLite, H2Embedded, H2Mem, Derby, HSQLDB, CouchbaseLite }
  

  protected Class<? extends IEntityManager> entityManagerClass;
  
  protected Class[] entityClasses = new Class[0];

  protected String dataFolder = null;
  protected String dataCollectionFileName = null;
  protected String dataCollectionPersistencePath = null;

  protected int dataBaseCurrentDataModelVersion;
  protected int applicationDataModelVersion;
  protected boolean createDatabase = false;

  protected DatabaseType databaseType;
  protected String databaseDriverUrl = null;
  protected String databaseDriver = null;

  protected String ddlGeneration = null;
  protected boolean createTables = false;
  protected boolean dropTables = false;



  public EntityManagerConfiguration(String dataFolder) {
    this(dataFolder, DatabaseType.CouchbaseLite);
  }

  public EntityManagerConfiguration(String dataFolder, int databaseCurrentDataModelVersion) {
    this(dataFolder);
    setDataBaseCurrentDataModelVersion(databaseCurrentDataModelVersion);
  }

  public EntityManagerConfiguration(String dataFolder, DatabaseType databaseType) {
    this(dataFolder, databaseType, false);
  }

  public EntityManagerConfiguration(String dataFolder, DatabaseType databaseType, boolean createTables) {
    setDatabaseType(databaseType);
    setDataFolder(dataFolder);
    setApplicationDataModelVersion(Application.CurrentDataModelVersion);

    setDatabaseConfiguration(databaseType, createTables);

    addEntities();
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
    EntityManagerConfiguration copy = new EntityManagerConfiguration(dataFolder);
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

  public int getDataBaseCurrentDataModelVersion() {
    return dataBaseCurrentDataModelVersion;
  }

  public void setDataBaseCurrentDataModelVersion(int dataBaseCurrentDataModelVersion) {
    this.dataBaseCurrentDataModelVersion = dataBaseCurrentDataModelVersion;
  }

  public int getApplicationDataModelVersion() {
    return applicationDataModelVersion;
  }

  public void setApplicationDataModelVersion(int applicationDataModelVersion) {
    this.applicationDataModelVersion = applicationDataModelVersion;
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


  protected void setDatabaseConfiguration(DatabaseType databaseType, boolean createTables) {
    switch(databaseType) {
      case SQLite:
        setDataCollectionFileName("DeepThoughtDb_SQLite.db");
        setDatabaseDriverUrl("jdbc:sqlite:");
        setDatabaseDriver("org.sqlite.JDBC");
        break;
      case H2Embedded:
        setDataCollectionFileName("DeepThoughtDb_H2.mv.db");
        setDatabaseDriverUrl("jdbc:h2:");
        setDatabaseDriver("org.h2.Driver");
        break;
      case H2Mem:
        setDataCollectionFileName("DeepThoughtDb_H2.mv.db");
        setDatabaseDriverUrl("jdbc:h2:mem:");
        setDatabaseDriver("org.h2.Driver");
        break;
      case Derby:
        setDataCollectionFileName("DeepThoughtDb_Derby");
        setDatabaseDriverUrl("jdbc:derby:");
        setDatabaseDriver("org.apache.derby.jdbc.EmbeddedDriver");
        break;
      case HSQLDB:
        setDataCollectionFileName("DeepThoughtDb_HSQL/db");
        setDatabaseDriverUrl("jdbc:hsqldb:file:");
        setDatabaseDriver("org.hsqldb.jdbcDriver");
        break;
      case CouchbaseLite:
        setDataCollectionFileName("deep_thought");
        break;
    }

    if(new File(getDataCollectionPersistencePath()).exists() == false) { // TODO: apply the same for createDefaultConfiguration()
      setCreateDatabase(true);
      setCreateTables(true);
    }

    if(createTables)
      setCreateTables(true);
  }


  protected void addEntities() {
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
        Person.class,
        EntryPersonAssociation.class,
    
        Note.class,
        NoteType.class,
        FileLink.class,
        FileType.class,
        EntriesLinkGroup.class,
    
        ReferenceBase.class,
        ReferenceBasePersonAssociation.class,
        SeriesTitle.class,
        Reference.class,
        ReferenceSubDivision.class,
    
        ApplicationLanguage.class,
        Language.class,
        BackupFileServiceType.class,

    };
    
    setEntityClasses(entities);
  }

}

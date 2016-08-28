package net.dankito.deepthought;

import net.dankito.deepthought.data.persistence.EntityManagerConfiguration;

/**
 * Created by ganymed on 22/08/15.
 */
public class TestEntityManagerConfiguration extends EntityManagerConfiguration {


  public TestEntityManagerConfiguration() {
    this("data/tests/");
  }

  public TestEntityManagerConfiguration(String dataFolder) {
    super(dataFolder, 0);
  }

  public TestEntityManagerConfiguration(boolean createTables) {
    super("data/tests/", DatabaseType.CouchbaseLite, createTables);
  }


  @Override
  protected void setDatabaseConfiguration(DatabaseType databaseType, boolean createTables) {
    super.setDatabaseConfiguration(databaseType, createTables);

    switch(databaseType) {
      case SQLite:
        setDataCollectionFileName("DeepThoughtDb_SQLite.db");
        break;
      case H2Embedded:
      case H2Mem:
        setDataCollectionFileName("DeepThoughtDb_Tests_H2.mv.db");
        break;
      case Derby:
        setDataCollectionFileName("DeepThoughtDb_Tests_Derby");
        break;
      case HSQLDB:
        setDataCollectionFileName("DeepThoughtDb_HSQL_Tests");
        break;
      case CouchbaseLite:
        setDataCollectionFileName("deep_thought_db_couchbase_lite_tests");
        break;
    }

    setCreateDatabase(true);
    setCreateTables(true);
//    setDropTables(true);
  }
}

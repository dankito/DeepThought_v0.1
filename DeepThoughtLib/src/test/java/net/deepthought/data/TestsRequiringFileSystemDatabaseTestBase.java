package net.deepthought.data;

import net.deepthought.data.persistence.EntityManagerConfiguration;

import org.junit.Before;
import org.junit.BeforeClass;

/**
 * Created by ganymed on 05/01/15.
 */
public abstract class TestsRequiringFileSystemDatabaseTestBase {


  protected EntityManagerConfiguration databaseOnFileSystemConfiguration = null;


  @BeforeClass
  public static void suiteSetup() {

  }

  @Before
  public void setup() throws Exception {
    databaseOnFileSystemConfiguration = EntityManagerConfiguration.createTestConfiguration();
    if(databaseOnFileSystemConfiguration.getDatabaseDriverUrl().contains("mem:"))
      databaseOnFileSystemConfiguration.setDatabaseDriverUrl(databaseOnFileSystemConfiguration.getDatabaseDriverUrl().replace("mem:", ""));
  }

}

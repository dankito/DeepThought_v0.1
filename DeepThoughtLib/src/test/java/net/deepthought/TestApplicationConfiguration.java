package net.deepthought;

import net.deepthought.data.IDataManager;
import net.deepthought.data.backup.IBackupManager;
import net.deepthought.data.helper.MockEntityManager;
import net.deepthought.data.helper.NoOperationBackupManager;
import net.deepthought.data.helper.TestDataManager;
import net.deepthought.data.persistence.EntityManagerConfiguration;
import net.deepthought.data.persistence.IEntityManager;
import net.deepthought.platform.IPlatformConfiguration;
import net.deepthought.platform.IPreferencesStore;
import net.deepthought.platform.PreferencesStoreBase;
import net.deepthought.plugin.IPlugin;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by ganymed on 22/08/15.
 */
public class TestApplicationConfiguration extends DependencyResolverBase implements IApplicationConfiguration {


  protected EntityManagerConfiguration entityManagerConfiguration;

  protected String defaultDataFolder = "data/tests/";


  public TestApplicationConfiguration() {

  }

  public TestApplicationConfiguration(String dataFolder) {
    this.defaultDataFolder = dataFolder;
    this.entityManagerConfiguration = new TestEntityManagerConfiguration(dataFolder);
  }

  public TestApplicationConfiguration(IEntityManager entityManager) {
    super(entityManager);
  }

  public TestApplicationConfiguration(IEntityManager entityManager, IBackupManager backupManager) {
    super(entityManager, backupManager);
  }


  @Override
  public IEntityManager createEntityManager(EntityManagerConfiguration configuration) throws Exception {
    if(entityManager != null)
      return entityManager;
    return new MockEntityManager();
  }

  @Override
  public EntityManagerConfiguration getEntityManagerConfiguration() {
    if(entityManagerConfiguration == null)
      entityManagerConfiguration = new TestEntityManagerConfiguration();
    return entityManagerConfiguration;
  }

  @Override
  public Collection<IPlugin> getStaticallyLinkedPlugins() {
    return new ArrayList<>();
  }


  @Override
  public IDataManager createDataManager(IEntityManager entityManager) {
    return new TestDataManager(entityManager);
  }

  @Override
  public IBackupManager createBackupManager() {
    if(backupManager != null)
      return backupManager;
    return new NoOperationBackupManager();
  }

  @Override
  public IPlatformConfiguration getPlatformConfiguration() {
    return new IPlatformConfiguration() {
      @Override
      public String getUserName() {
        return "test_user";
      }

      @Override
      public String getPlatformName() {
        return "TestPlatform";
      }

      @Override
      public int getOsVersion() {
        return -1;
      }

      @Override
      public String getOsVersionString() {
        return "-1 preAlpha";
      }

      @Override
      public boolean hasCaptureDevice() {
        return false;
      }

      @Override
      public String getLineSeparator() {
        return System.lineSeparator();
      }
    };
  }

  @Override
  public IPreferencesStore getPreferencesStore() {
    return new PreferencesStoreBase() {
      @Override
      public String getDataFolder() {
        return defaultDataFolder;
      }

      @Override
      protected String readValueFromStore(String key, String defaultValue) {
        return "";
      }

      @Override
      protected void saveValueToStore(String key, String value) {

      }

      @Override
      protected boolean doesValueExist(String key) {
        return false;
      }
    };
  }

}

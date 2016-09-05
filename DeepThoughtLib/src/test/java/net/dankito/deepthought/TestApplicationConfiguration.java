package net.dankito.deepthought;

import net.dankito.deepthought.communication.connected_device.IConnectedDevicesListenerManager;
import net.dankito.deepthought.data.IDataManager;
import net.dankito.deepthought.data.backup.IBackupManager;
import net.dankito.deepthought.data.helper.MockEntityManager;
import net.dankito.deepthought.data.helper.NoOperationBackupManager;
import net.dankito.deepthought.data.helper.TestDataManager;
import net.dankito.deepthought.data.persistence.EntityManagerConfiguration;
import net.dankito.deepthought.data.persistence.IEntityManager;
import net.dankito.deepthought.data.sync.IDeepThoughtSyncManager;
import net.dankito.deepthought.platform.IPlatformConfiguration;
import net.dankito.deepthought.platform.IPreferencesStore;
import net.dankito.deepthought.platform.PreferencesStoreBase;
import net.dankito.deepthought.plugin.IPlugin;
import net.dankito.deepthought.util.IThreadPool;

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
  public IDeepThoughtSyncManager createSyncManager(IConnectedDevicesListenerManager connectedDevicesListenerManager, IThreadPool threadPool) {
    return null;
  }

  @Override
  public IPlatformConfiguration getPlatformConfiguration() {
    return new IPlatformConfiguration() {
      @Override
      public String getUserName() {
        return "test_user";
      }

      @Override
      public String getDeviceName() {
        return null;
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
      public boolean isRunningInEmulator() {
        return false; // only needed on Android
      }

      @Override
      public boolean hasCaptureDevice() {
        return false;
      }

      @Override
      public boolean canScanBarcodes() {
        return false;
      }

      @Override
      public String getLineSeparator() {
        return System.lineSeparator();
      }

      @Override
      public String getTempDir() {
        return System.getProperty("java.io.tmpdir");
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

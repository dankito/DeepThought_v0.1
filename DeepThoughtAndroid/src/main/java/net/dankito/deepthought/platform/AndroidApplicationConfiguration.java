package net.dankito.deepthought.platform;

import android.content.Context;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.DependencyResolverBase;
import net.dankito.deepthought.IApplicationConfiguration;
import net.dankito.deepthought.application.IApplicationLifeCycleService;
import net.dankito.deepthought.clipboard.IClipboardHelper;
import net.dankito.deepthought.communication.connected_device.IConnectedDevicesListenerManager;
import net.dankito.deepthought.data.AndroidDataManager;
import net.dankito.deepthought.data.IDataManager;
import net.dankito.deepthought.data.contentextractor.CtContentExtractor;
import net.dankito.deepthought.data.contentextractor.DerFreitagContentExtractor;
import net.dankito.deepthought.data.contentextractor.HeiseContentExtractor;
import net.dankito.deepthought.data.contentextractor.PostillonContentExtractor;
import net.dankito.deepthought.data.contentextractor.SpiegelContentExtractor;
import net.dankito.deepthought.data.contentextractor.SueddeutscheContentExtractor;
import net.dankito.deepthought.data.contentextractor.SueddeutscheJetztContentExtractor;
import net.dankito.deepthought.data.contentextractor.SueddeutscheMagazinContentExtractor;
import net.dankito.deepthought.data.contentextractor.ZeitContentExtractor;
import net.dankito.deepthought.data.persistence.AndroidCouchbaseLiteEntityManager;
import net.dankito.deepthought.data.persistence.CouchbaseLiteEntityManagerBase;
import net.dankito.deepthought.data.persistence.EntityManagerConfiguration;
import net.dankito.deepthought.data.persistence.IEntityManager;
import net.dankito.deepthought.data.search.ISearchEngine;
import net.dankito.deepthought.data.search.InMemorySearchEngine;
import net.dankito.deepthought.data.search.LuceneSearchEngine;
import net.dankito.deepthought.data.sync.CouchbaseLiteSyncManager;
import net.dankito.deepthought.data.sync.IDeepThoughtSyncManager;
import net.dankito.deepthought.plugin.AndroidPluginManager;
import net.dankito.deepthought.plugin.IPlugin;
import net.dankito.deepthought.plugin.IPluginManager;
import net.dankito.deepthought.util.AndroidClipboardHelper;
import net.dankito.deepthought.util.IThreadPool;
import net.dankito.deepthought.util.OsHelper;
import net.dankito.deepthought.util.file.FileUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;

/**
 * Created by ganymed on 22/08/15.
 */
public class AndroidApplicationConfiguration extends DependencyResolverBase implements IApplicationConfiguration {

  private final static Logger log = LoggerFactory.getLogger(AndroidApplicationConfiguration.class);


  protected Context context;

  protected EntityManagerConfiguration entityManagerConfiguration;

  protected IPreferencesStore preferencesStore;

  protected IPlatformConfiguration platformConfiguration;

  protected IApplicationLifeCycleService lifeCycleService;


  public AndroidApplicationConfiguration(Context context, IApplicationLifeCycleService lifeCycleService) {
    this.context = context;
    this.lifeCycleService = lifeCycleService;

    this.preferencesStore = new AndroidPreferencesStore(context);
    this.platformConfiguration = new AndroidPlatformConfiguration(context);
    this.entityManagerConfiguration = new EntityManagerConfiguration(preferencesStore.getDataFolder(), preferencesStore.getDatabaseDataModelVersion());

    // if App has been uninstalled and not gets reinstalled data folder on SD card may still exists (doesn't get deleted on Uninstall)
    // -> it still contains data, especially the Lucene search index which points to not anymore existing Entities
    // TODO: may also save Database and Android Preferences in data folder so that after uninstalling complete data can be restored
    try {
      if (preferencesStore.getDatabaseDataModelVersion() == 0 && FileUtils.doesFileExist(preferencesStore.getDataFolder()))
        FileUtils.deleteFile(preferencesStore.getDataFolder());
    } catch(Exception ex) {
      log.error("Could not delete previous' installation data", ex);
    }
  }


  @Override
  public EntityManagerConfiguration getEntityManagerConfiguration() {
    return entityManagerConfiguration;
  }

  @Override
  public Collection<IPlugin> getStaticallyLinkedPlugins() {
//    return new ArrayList<>();
    return Arrays.asList(new IPlugin[]{
        new SueddeutscheMagazinContentExtractor(), new SueddeutscheJetztContentExtractor(), new SueddeutscheContentExtractor(),
        new CtContentExtractor(), new HeiseContentExtractor(), new DerFreitagContentExtractor(), new PostillonContentExtractor(), new ZeitContentExtractor(),
        new SpiegelContentExtractor()
    });
  }

  @Override
  public IEntityManager createEntityManager(EntityManagerConfiguration configuration) throws SQLException {
//    return new OrmLiteAndroidEntityManager(context, configuration);

    try {
      return new AndroidCouchbaseLiteEntityManager(context, configuration);
    } catch(Exception e) { throw new SQLException("Could not create AndroidCouchbaseLiteEntityManager", e); }
  }

  @Override
  public IDataManager createDataManager(IEntityManager entityManager) {
    return new AndroidDataManager(entityManager);
  }

  @Override
  public IPlatformTools createPlatformTools() {
    return new AndroidPlatformTools();
  }

  @Override
  public ISearchEngine createSearchEngine() {
    try {
      if(OsHelper.isRunningOnJavaSeOrOnAndroidApiLevelAtLeastOf(9)) {
          return new LuceneSearchEngine();
//        return new LuceneAndDatabaseSearchEngine();
      }
    } catch (Exception ex) {
      log.error("Could not initialize LuceneSearchEngine", ex);
    }

    // TODO: implement InMemorySearchEngine
    return new InMemorySearchEngine();  // TODO: abort application?
  }

  @Override
  public IPluginManager createPluginManager() {
    return new AndroidPluginManager(context);
  }

  @Override
  public IApplicationLifeCycleService createApplicationLifeCycleService() {
    return lifeCycleService;
  }

  @Override
  public IDeepThoughtSyncManager createSyncManager(IConnectedDevicesListenerManager connectedDevicesListenerManager, IThreadPool threadPool) {
    return new CouchbaseLiteSyncManager((CouchbaseLiteEntityManagerBase) Application.getEntityManager(), threadPool, connectedDevicesListenerManager);
  }

  @Override
  public IPreferencesStore getPreferencesStore() {
    return preferencesStore;
  }

  @Override
  public IPlatformConfiguration getPlatformConfiguration() {
    return platformConfiguration;
  }

  @Override
  public IClipboardHelper createClipboardHelper() {
    return new AndroidClipboardHelper(context);
  }

}

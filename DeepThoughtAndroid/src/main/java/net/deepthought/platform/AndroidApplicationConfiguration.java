package net.deepthought.platform;

import android.content.Context;

import net.deepthought.DependencyResolverBase;
import net.deepthought.IApplicationConfiguration;
import net.deepthought.android.db.OrmLiteAndroidEntityManager;
import net.deepthought.data.AndroidDataManager;
import net.deepthought.data.IDataManager;
import net.deepthought.data.persistence.EntityManagerConfiguration;
import net.deepthought.data.persistence.IEntityManager;
import net.deepthought.data.search.ISearchEngine;
import net.deepthought.data.search.LuceneAndDatabaseSearchEngine;
import net.deepthought.plugin.AndroidPluginManager;
import net.deepthought.plugin.IPluginManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ganymed on 22/08/15.
 */
public class AndroidApplicationConfiguration extends DependencyResolverBase implements IApplicationConfiguration {

  private final static Logger log = LoggerFactory.getLogger(AndroidApplicationConfiguration.class);


  protected Context context;

  protected EntityManagerConfiguration entityManagerConfiguration;

  protected IPreferencesStore preferencesStore;

  protected IPlatformConfiguration platformConfiguration;


  public AndroidApplicationConfiguration(Context context) {
    this.context = context;

    this.preferencesStore = new AndroidPreferencesStore(context);
    this.platformConfiguration = new AndroidPlatformConfiguration(context);
    this.entityManagerConfiguration = new EntityManagerConfiguration(preferencesStore.getDataFolder());
  }


  @Override
  public EntityManagerConfiguration getEntityManagerConfiguration() {
    return entityManagerConfiguration;
  }

  @Override
  public IEntityManager createEntityManager(EntityManagerConfiguration configuration) {
    try {
      return new OrmLiteAndroidEntityManager(context, configuration);
    } catch (Exception ex) {
      log.error("Could not create OrmLiteAndroidEntityManager", ex);
    }

    return null; // TODO: what to do in this case?
  }

  @Override
  public IDataManager createDataManager(IEntityManager entityManager) {
    return new AndroidDataManager(entityManager);
  }

  @Override
  public ISearchEngine createSearchEngine() {
    try {
//          return new LuceneSearchEngine();
      return new LuceneAndDatabaseSearchEngine();
    } catch (Exception ex) {
      log.error("Could not initialize LuceneSearchEngine", ex);
    }
    return null; // TODO: abort application?
  }

  @Override
  public IPluginManager createPluginManager() {
    return new AndroidPluginManager(context);
  }

  @Override
  public IPreferencesStore getPreferencesStore() {
    return preferencesStore;
  }

  @Override
  public IPlatformConfiguration getPlatformConfiguration() {
    return platformConfiguration;
  }
}

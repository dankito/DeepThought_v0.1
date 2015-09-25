package net.deepthought.platform;

import android.content.Context;

import net.deepthought.DependencyResolverBase;
import net.deepthought.IApplicationConfiguration;
import net.deepthought.android.db.OrmLiteAndroidEntityManager;
import net.deepthought.data.AndroidDataManager;
import net.deepthought.data.IDataManager;
import net.deepthought.data.contentextractor.PostillonContentExtractor;
import net.deepthought.data.contentextractor.SpiegelContentExtractor;
import net.deepthought.data.contentextractor.SueddeutscheContentExtractor;
import net.deepthought.data.contentextractor.SueddeutscheJetztContentExtractor;
import net.deepthought.data.contentextractor.SueddeutscheMagazinContentExtractor;
import net.deepthought.data.contentextractor.ZeitContentExtractor;
import net.deepthought.data.persistence.EntityManagerConfiguration;
import net.deepthought.data.persistence.IEntityManager;
import net.deepthought.data.search.ISearchEngine;
import net.deepthought.data.search.LuceneAndDatabaseSearchEngine;
import net.deepthought.plugin.AndroidPluginManager;
import net.deepthought.plugin.IPlugin;
import net.deepthought.plugin.IPluginManager;

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
  public Collection<IPlugin> getStaticallyLinkedPlugins() {
//    return new ArrayList<>();
    return Arrays.asList(new IPlugin[]{ new SueddeutscheContentExtractor(), new SueddeutscheMagazinContentExtractor(), new SueddeutscheJetztContentExtractor(),
                                        new PostillonContentExtractor(), new ZeitContentExtractor(), new SpiegelContentExtractor() } );
  }

  @Override
  public IEntityManager createEntityManager(EntityManagerConfiguration configuration) throws SQLException {
    return new OrmLiteAndroidEntityManager(context, configuration);
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

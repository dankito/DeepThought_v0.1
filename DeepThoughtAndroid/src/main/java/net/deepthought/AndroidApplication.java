package net.deepthought;

import android.util.Log;

import net.deepthought.android.db.OrmLiteAndroidEntityManager;
import net.deepthought.data.AndroidDataManager;
import net.deepthought.data.DeepThoughtAndroidApplicationConfiguration;
import net.deepthought.data.IDataManager;
import net.deepthought.data.persistence.EntityManagerConfiguration;
import net.deepthought.data.persistence.IEntityManager;
import net.deepthought.data.search.ISearchEngine;
import net.deepthought.data.search.LuceneSearchEngine;
import net.deepthought.plugin.AndroidPluginManager;
import net.deepthought.plugin.IPluginManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ganymed on 18/08/15.
 */
public class AndroidApplication extends android.app.Application {

  private final static Logger log = LoggerFactory.getLogger(AndroidApplication.class);


  @Override
  public void onCreate() {
    super.onCreate();

    instantiateDeepThought();
  }

  protected void instantiateDeepThought() {
    // TODO: create Android Dependency Resolver
    Application.instantiateAsync(new DeepThoughtAndroidApplicationConfiguration(this), new DefaultDependencyResolver() {
      @Override
      public IEntityManager createEntityManager(EntityManagerConfiguration configuration) {
        try {
          return new OrmLiteAndroidEntityManager(AndroidApplication.this.getApplicationContext(), configuration);
        } catch (Exception ex) {
          Log.e("MainActivity", "Could not create OrmLiteAndroidEntityManager", ex);
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
          return new LuceneSearchEngine();
        } catch (Exception ex) {
          log.error("Could not initialize LuceneSearchEngine", ex);
        }
        return null; // TODO: abort application?
      }

      @Override
      public IPluginManager createPluginManager() {
        return new AndroidPluginManager(AndroidApplication.this);
      }
    });
  }

  @Override
  public void onTerminate() {
    Application.shutdown();
    super.onTerminate();
  }
}

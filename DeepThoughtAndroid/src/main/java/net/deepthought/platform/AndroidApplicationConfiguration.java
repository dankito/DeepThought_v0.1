package net.deepthought.platform;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

import net.deepthought.DependencyResolverBase;
import net.deepthought.IApplicationConfiguration;
import net.deepthought.android.db.OrmLiteAndroidEntityManager;
import net.deepthought.data.AndroidDataManager;
import net.deepthought.data.IDataManager;
import net.deepthought.data.persistence.EntityManagerConfiguration;
import net.deepthought.data.persistence.IEntityManager;
import net.deepthought.data.search.ISearchEngine;
import net.deepthought.data.search.LuceneSearchEngine;
import net.deepthought.plugin.AndroidPluginManager;
import net.deepthought.plugin.IPluginManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Created by ganymed on 22/08/15.
 */
public class AndroidApplicationConfiguration extends DependencyResolverBase implements IApplicationConfiguration {

  private final static Logger log = LoggerFactory.getLogger(AndroidApplicationConfiguration.class);

  protected final static String DataFolderKey = "data.folder";


  protected Context context;

  protected EntityManagerConfiguration entityManagerConfiguration;

  protected SharedPreferences preferences = null;


  public AndroidApplicationConfiguration(Context context) {
    this.context = context;

    this.entityManagerConfiguration = new EntityManagerConfiguration(getDataFolder());
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
      return new LuceneSearchEngine();
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
  public IPlatformConfiguration getPlatformConfiguration() {
    return new AndroidPlatformConfiguration(context);
  }


  protected String getDataFolder() {
    this.preferences = context.getSharedPreferences("DeepThoughtAndroidApplicationConfiguration", Context.MODE_PRIVATE);

    if(preferences.contains(DataFolderKey))
      return preferences.getString(DataFolderKey, "");
    else { // data folder not yet set via shared preferences
      String defaultDataFolder = getDefaultDataFolder();
      saveDataFolder(defaultDataFolder);
      return defaultDataFolder;
    }
  }

  public boolean saveDataFolder(String dataFolder) {
    SharedPreferences.Editor editor = preferences.edit();
    editor.putString(DataFolderKey, dataFolder);

    return editor.commit();
  }

  protected String getDefaultDataFolder() {
    if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
      File folder = Environment.getExternalStorageDirectory();
      folder = new File(folder, "DeepThought");
      if (folder.exists() == false)
        folder.mkdir();
      return folder.getAbsolutePath() + "/";
    }
    else {
      File dataFolderFile = new File(Environment.getDataDirectory(), "data");
      dataFolderFile = new File(dataFolderFile, "net.deepthought");
      dataFolderFile = new File(dataFolderFile, "data");
      if (dataFolderFile.exists() == false) {
        dataFolderFile.mkdirs();
        dataFolderFile.mkdir();
      }
      return dataFolderFile.getAbsolutePath() + "/";
    }
  }
}

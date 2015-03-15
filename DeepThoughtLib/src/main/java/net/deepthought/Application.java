package net.deepthought;

import net.deepthought.data.IDataManager;
import net.deepthought.data.backup.IBackupManager;
import net.deepthought.data.compare.IDataComparer;
import net.deepthought.data.listener.ApplicationListener;
import net.deepthought.data.merger.IDataMerger;
import net.deepthought.data.model.DeepThought;
import net.deepthought.data.model.DeepThoughtApplication;
import net.deepthought.data.model.User;
import net.deepthought.data.model.settings.UserDeviceSettings;
import net.deepthought.data.persistence.EntityManagerConfiguration;
import net.deepthought.data.persistence.IEntityManager;
import net.deepthought.util.DeepThoughtError;
import net.deepthought.util.DeepThoughtProperties;
import net.deepthought.util.Localization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by ganymed on 13/10/14.
 */
public class Application {

  public final static int CurrentDataModelVersion = 1;

  public final static String CouldNotGetDataFolderPath = "Could not get data folder path";


  private final static Logger log = LoggerFactory.getLogger(Application.class);


  protected static IDependencyResolver dependencyResolver = null;
  protected static EntityManagerConfiguration entityManagerConfiguration = null;
  protected static IEntityManager entityManager = null;

  protected static IDataManager dataManager = null;
  protected static IBackupManager backupManager = null;

  protected static IDataComparer dataComparer = null;
  protected static IDataMerger dataMerger = null;

  protected static Set<ApplicationListener> listeners = new HashSet<>();



  public static void instantiateAsync(final IDependencyResolver dependencyResolver) {
    new Thread(new Runnable() {
      @Override
      public void run() {
        instantiate(dependencyResolver);
      }
    }).start();

//    ForkJoinPool pool = new ForkJoinPool();
//    pool.execute(new Runnable() {
//      @Override
//      public void run() {
//        instantiate(dependencyResolver);
//      }
//    });
  }

  public static void instantiate(IDependencyResolver dependencyResolver) {
    EntityManagerConfiguration configuration = EntityManagerConfiguration.createDefaultConfiguration(DeepThoughtProperties.getDataFolderOrCreateDefaultValuesOnNull());

    instantiate(dependencyResolver, configuration);
  }

  public static void instantiate(IDependencyResolver dependencyResolver, EntityManagerConfiguration configuration) {
    Date startTime = new Date();
    log.info("Starting to resolve dependencies ...");

    Application.dependencyResolver = dependencyResolver;
    Application.entityManagerConfiguration = configuration;

    if(openDatabase(dependencyResolver) == false)
      return;

    logResolvingDependencyDuration("EntityManager", startTime);

    if(retrieveData(dependencyResolver) == false)
      return;

    try {
//      startTime = new Date();
      Application.backupManager = dependencyResolver.createBackupManager();
//      logResolvingDependencyDuration("BackupManager", startTime);

//      startTime = new Date();
      Application.dataComparer = dependencyResolver.createDataComparer();
//      logResolvingDependencyDuration("DataComparer", startTime);

//      startTime = new Date();
      Application.dataMerger = dependencyResolver.createDataMerger();
//      logResolvingDependencyDuration("DataMerger", startTime);
    } catch(Exception ex) {
      log.error("Could not resolve a Manager dependency", ex);
      callErrorOccurredListeners(new DeepThoughtError(Localization.getLocalizedStringForResourceKey("alert.message.message.a.severe.error.occurred.resolving.a.manager.instance"), ex, true,
          Localization.getLocalizedStringForResourceKey("alert.message.title.a.severe.error.occurred.resolving.a.manager.instance")));
    }
  }

  protected static boolean openDatabase(IDependencyResolver dependencyResolver) {
    try {
      Application.entityManager = dependencyResolver.createEntityManager(entityManagerConfiguration);
    } catch(Exception ex) {
      log.error("Could not resolve EntityManager dependency", ex);
      if(isDatabaseAlreadyInUseException(ex))
        callErrorOccurredListeners(new DeepThoughtError(Localization.getLocalizedStringForResourceKey("alert.message.message.database.already.in.use"), ex, true,
            Localization.getLocalizedStringForResourceKey("alert.message.title.database.already.in.use")));
      else
        callErrorOccurredListeners(new DeepThoughtError(Localization.getLocalizedStringForResourceKey("alert.message.message.a.severe.error.occurred.opening.database"), ex, true,
            Localization.getLocalizedStringForResourceKey("alert.message.title.a.severe.error.occurred.opening.database")));

      return false;
    }

    return true;
  }

  private static boolean isDatabaseAlreadyInUseException(Throwable ex) {
    Throwable exception = ex;
    while(exception != null && exception.getMessage() != null) {
      if(exception.getMessage().toLowerCase().contains("already in use"))
        return true;
      exception = exception.getCause();
    }

    return false;
  }

  protected static boolean retrieveData(IDependencyResolver dependencyResolver) {
    Date startTime = new Date();

    try {
      Application.dataManager = dependencyResolver.createDataManager(Application.entityManager);
      Application.dataManager.addApplicationListener(dataManagerListener);
      Application.dataManager.retrieveDeepThoughtApplication();
    } catch(Exception ex) {
      log.error("Could not retrieve data", ex);
      callErrorOccurredListeners(new DeepThoughtError(Localization.getLocalizedStringForResourceKey("alert.message.message.a.severe.error.occurred.retrieving.data"), ex, true,
          Localization.getLocalizedStringForResourceKey("alert.message.title.a.severe.error.occurred.retrieving.data")));

      return false;
    }
    finally {
      logResolvingDependencyDuration("DataManager", startTime);
    }

    return true;
  }

  protected static void logResolvingDependencyDuration(String dependencyName, Date startTime) {
    long millisecondsElapsed = (new Date().getTime() - startTime.getTime());
    log.info("Resolving " + dependencyName + " dependency took " + (millisecondsElapsed / 1000) + "." + String.format("%03d", millisecondsElapsed).substring(0, 3) + " seconds");
  }

  public static void shutdown() {
    if(dataManager != null) {
      dataManager.close();
    }

    dataManager = null;

    entityManager = null;
  }


  public static boolean addApplicationListener(ApplicationListener listener) {
    return listeners.add(listener);
  }

  public static boolean removeApplicationListener(ApplicationListener listener) {
    return listeners.remove(listener);
  }

  protected static void callDeepThoughtChangedListeners(DeepThought deepThought) {
    for(ApplicationListener listener : listeners)
      listener.deepThoughtChanged(deepThought);
  }

  protected static void callErrorOccurredListeners(DeepThoughtError error) {
    for(ApplicationListener listener : listeners)
      listener.errorOccurred(error);
  }

  protected static ApplicationListener dataManagerListener = new ApplicationListener() {
    @Override
    public void deepThoughtChanged(DeepThought deepThought) {
      callDeepThoughtChangedListeners(deepThought);
    }

    @Override
    public void errorOccurred(DeepThoughtError error) {
      callErrorOccurredListeners(error);
    }
  };


  public static IDependencyResolver getDependencyResolver() {
    return dependencyResolver;
  }

  public static EntityManagerConfiguration getEntityManagerConfiguration() {
    return entityManagerConfiguration;
  }

  public static IEntityManager getEntityManager() {
    if(dataManager != null)
      return dataManager.getEntityManager();
    return null;
  }

  public static IDataManager getDataManager() {
    return dataManager;
  }

  public static IBackupManager getBackupManager() {
    return backupManager;
  }

  public static IDataComparer getDataComparer() {
    return dataComparer;
  }

  public static IDataMerger getDataMerger() {
    return dataMerger;
  }

  public static DeepThoughtApplication getApplication() {
    if(dataManager != null)
      return dataManager.getApplication();
    return null;
  }

//  public static AppSettings getSettings() {
//    if(dataManager != null)
//      return dataManager.getSettings();
//    return null;
//  }

  public static UserDeviceSettings getSettings() {
    if(dataManager != null && dataManager.getLoggedOnUser() != null)
      return dataManager.getLoggedOnUser().getSettings();
    return null;
  }

  public static User getLoggedOnUser() {
    if(dataManager != null)
      return dataManager.getLoggedOnUser();
    return null;
  }

  public static DeepThought getDeepThought() {
    if(dataManager != null)
      return dataManager.getDeepThought();
    return null;
  }

  public static String getDataCollectionSavePath() {
    if(dataManager != null)
      return dataManager.getDataCollectionSavePath();
    return null;
  }

  public static String getDataFolderPath() {
    if(dataManager != null)
      return dataManager.getDataFolderPath();
    return CouldNotGetDataFolderPath;
  }

}

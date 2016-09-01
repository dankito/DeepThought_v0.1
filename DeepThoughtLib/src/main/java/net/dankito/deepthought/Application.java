package net.dankito.deepthought;

import net.dankito.deepthought.application.IApplicationLifeCycleService;
import net.dankito.deepthought.clipboard.IClipboardHelper;
import net.dankito.deepthought.communication.IDeepThoughtConnector;
import net.dankito.deepthought.communication.IDevicesFinder;
import net.dankito.deepthought.controls.html.HtmlEditor;
import net.dankito.deepthought.controls.html.IHtmlEditorPool;
import net.dankito.deepthought.data.IDataManager;
import net.dankito.deepthought.data.backup.IBackupManager;
import net.dankito.deepthought.data.compare.IDataComparer;
import net.dankito.deepthought.data.contentextractor.IContentExtractorManager;
import net.dankito.deepthought.data.download.IFileDownloader;
import net.dankito.deepthought.data.html.IHtmlHelper;
import net.dankito.deepthought.data.listener.ApplicationListener;
import net.dankito.deepthought.data.merger.IDataMerger;
import net.dankito.deepthought.data.model.DeepThought;
import net.dankito.deepthought.data.model.DeepThoughtApplication;
import net.dankito.deepthought.data.model.User;
import net.dankito.deepthought.data.persistence.EntityManagerConfiguration;
import net.dankito.deepthought.data.persistence.IEntityManager;
import net.dankito.deepthought.data.search.IEntitiesSearcherAndCreator;
import net.dankito.deepthought.data.search.ISearchEngine;
import net.dankito.deepthought.data.sync.IDeepThoughtSyncManager;
import net.dankito.deepthought.language.ILanguageDetector;
import net.dankito.deepthought.platform.IPlatformConfiguration;
import net.dankito.deepthought.platform.IPlatformTools;
import net.dankito.deepthought.platform.IPreferencesStore;
import net.dankito.deepthought.plugin.IPluginManager;
import net.dankito.deepthought.util.DeepThoughtError;
import net.dankito.deepthought.util.IThreadPool;
import net.dankito.deepthought.util.LogHelper;
import net.dankito.deepthought.util.NotificationType;
import net.dankito.deepthought.util.file.FileUtils;
import net.dankito.deepthought.util.isbn.IIsbnResolver;
import net.dankito.deepthought.data.model.settings.UserDeviceSettings;
import net.dankito.deepthought.util.Notification;
import net.dankito.deepthought.util.localization.Localization;

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

  public final static int CurrentPluginSystemVersion = 1;


  public final static String CouldNotGetDataFolderPath = "Could not get data folder path";


  private final static Logger log = LoggerFactory.getLogger(Application.class);


  protected static String dataFolderPath = CouldNotGetDataFolderPath;

  protected static IDependencyResolver dependencyResolver = null;
  protected static IPreferencesStore preferencesStore;
  protected static IPlatformConfiguration platformConfiguration;
  protected static IPlatformTools platformTools;
  protected static IApplicationLifeCycleService lifeCycleService;
  protected static IThreadPool threadPool;

  protected static EntityManagerConfiguration entityManagerConfiguration = null;
  protected static IEntityManager entityManager = null;

  protected static IDataManager dataManager = null;
  protected static IBackupManager backupManager = null;

  protected static IDataComparer dataComparer = null;
  protected static IDataMerger dataMerger = null;

  protected static ILanguageDetector languageDetector = null;
  protected static ISearchEngine searchEngine = null;

  protected static IEntitiesSearcherAndCreator entitiesSearcherAndCreator = null;

  protected static IHtmlHelper htmlHelper = null;

  protected static IFileDownloader downloader = null;

  protected static IPluginManager pluginManager = null;

  protected static IContentExtractorManager contentExtractorManager = null;

  protected static IDevicesFinder devicesFinder = null;

  protected static IDeepThoughtConnector deepThoughtConnector = null;

  protected static IDeepThoughtSyncManager syncManager = null;

  protected static IIsbnResolver isbnResolver;

  protected static IHtmlEditorPool htmlEditorPool;

  protected static IClipboardHelper clipboardHelper;

  protected static boolean isInstantiated = false;

  protected static boolean hasOnlyReadOnlyAccess = false;

  protected static Set<ApplicationListener> listeners = new HashSet<>();


  public static void instantiateAsync(final IApplicationConfiguration applicationConfiguration) {
    threadPool = applicationConfiguration.createThreadPool();
    threadPool.runTaskAsync(new Runnable() {
      @Override
      public void run() {
        instantiate(applicationConfiguration);
      }
    });
  }

  public static void instantiate(IApplicationConfiguration applicationConfiguration) {
    Application.preferencesStore = applicationConfiguration.getPreferencesStore();
    Application.dataFolderPath = preferencesStore.getDataFolder();

    Date startTime = new Date();
    log.info("Starting to resolve dependencies ...");

    Application.dependencyResolver = applicationConfiguration;
    if(threadPool == null)
      threadPool = dependencyResolver.createThreadPool();
    Application.platformConfiguration = applicationConfiguration.getPlatformConfiguration();
    Application.platformTools = applicationConfiguration.createPlatformTools();
    Application.lifeCycleService = applicationConfiguration.createApplicationLifeCycleService();

    HtmlEditor.extractHtmlEditorIfNeededAsync();

    Application.entityManagerConfiguration = applicationConfiguration.getEntityManagerConfiguration();

    Application.htmlHelper = dependencyResolver.createHtmlHelper();
    Application.downloader = dependencyResolver.createDownloader();

    Application.contentExtractorManager = dependencyResolver.createContentExtractorManager(); // ContentExtractorManager needs HtmlHelper to be created

    Application.languageDetector = dependencyResolver.createLanguageDetector();

    Application.searchEngine = dependencyResolver.createSearchEngine();
    Application.entitiesSearcherAndCreator = dependencyResolver.createEntitiesSearcherAndCreator();

    if(openDatabase(dependencyResolver) == false)
      return;

    logResolvingDependencyDuration("EntityManager", startTime);

    if(retrieveData(dependencyResolver) == false)
      return;

    try {
      Application.backupManager = dependencyResolver.createBackupManager();

      Application.dataComparer = dependencyResolver.createDataComparer();

      Application.dataMerger = dependencyResolver.createDataMerger();

      Application.pluginManager = dependencyResolver.createPluginManager();
      pluginManager.loadPluginsAsync(applicationConfiguration.getStaticallyLinkedPlugins());

      Application.devicesFinder = dependencyResolver.createDevicesFinder(threadPool);

      Application.deepThoughtConnector = dependencyResolver.createDeepThoughtConnector(devicesFinder, threadPool);
      deepThoughtConnector.runAsync();

      Application.syncManager = dependencyResolver.createSyncManager(deepThoughtConnector);

      Application.isbnResolver = dependencyResolver.createIsbnResolver(htmlHelper, threadPool);

      Application.htmlEditorPool = dependencyResolver.createHtmlEditorPool();

      Application.clipboardHelper = dependencyResolver.createClipboardHelper();

      isInstantiated = true;
      callNotificationListeners(new Notification(NotificationType.ApplicationInstantiated));
    } catch(Exception ex) {
      log.error("Could not resolve a Manager dependency", ex);
      callNotificationListeners(new DeepThoughtError(Localization.getLocalizedString("alert.message.message.a.severe.error.occurred.resolving.a.manager.instance"), ex, true,
          Localization.getLocalizedString("alert.message.title.a.severe.error.occurred.resolving.a.manager.instance")));
    }
  }

  protected static boolean openDatabase(IDependencyResolver dependencyResolver) {
    try {
      FileUtils.ensureFolderExists(entityManagerConfiguration.getDataFolder());
      Application.entityManager = dependencyResolver.createEntityManager(entityManagerConfiguration);
    } catch(Exception ex) {
      log.error("Could not resolve EntityManager dependency", ex);
      if(isDatabaseAlreadyInUseException(ex))
        callNotificationListeners(createDataIsReadonlyNotification(ex));
      else
        callNotificationListeners(new DeepThoughtError(Localization.getLocalizedString("alert.message.message.a.severe.error.occurred.opening.database"), ex, true,
            Localization.getLocalizedString("alert.message.title.a.severe.error.occurred.opening.database")));

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
      callNotificationListeners(new DeepThoughtError(Localization.getLocalizedString("alert.message.message.a.severe.error.occurred.retrieving.data"), ex, true,
          Localization.getLocalizedString("alert.message.title.a.severe.error.occurred.retrieving.data")));

      return false;
    }
    finally {
      logResolvingDependencyDuration("DataManager", startTime);
    }

    return true;
  }

  protected static void logResolvingDependencyDuration(String dependencyName, Date startTime) {
    long millisecondsElapsed = (new Date().getTime() - startTime.getTime());
    log.info("Resolving " + dependencyName + " dependency took " + LogHelper.createTimeElapsedString(millisecondsElapsed) + " seconds");
  }

  public static void shutdown() {
    if(dataManager != null) {
      dataManager.close();
    }
    dataManager = null;

    threadPool.shutDown();
    threadPool = null;

    if(entityManager != null)
      entityManager.close();
    entityManager = null;
    entityManagerConfiguration = null;
    dataFolderPath = CouldNotGetDataFolderPath;

    if(searchEngine != null)
      searchEngine.close();
    searchEngine = null;

    languageDetector = null;

    htmlHelper = null;

    dependencyResolver = null;

    if(pluginManager != null) {
      pluginManager = null;
    }

    if(backupManager != null) {
      backupManager = null;
    }

    dataComparer = null;
    dataMerger = null;

    contentExtractorManager = null;

    downloader = null;

    if(deepThoughtConnector != null) {
      deepThoughtConnector.shutDown();
      deepThoughtConnector = null;
    }

    isbnResolver = null;

    if(htmlEditorPool != null) {
      htmlEditorPool.cleanUp();
    }
    htmlEditorPool = null;

    listeners.clear();

    isInstantiated = false;
    hasOnlyReadOnlyAccess = false;
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

  protected static void callNotificationListeners(Notification notification) {
    for(ApplicationListener listener : listeners)
      listener.notification(notification);
  }

  protected static DeepThoughtError createDataIsReadonlyNotification(Exception ex) {
    return new DeepThoughtError(Localization.getLocalizedString("alert.message.message.database.already.in.use"), ex, true,
        Localization.getLocalizedString("alert.message.title.database.already.in.use"));
  }

  public static void notifyUser(Notification notification) {
    if(notification.getType() == NotificationType.HasOnlyReadOnlyAccessToData) {
      hasOnlyReadOnlyAccess = true;
      callNotificationListeners(createDataIsReadonlyNotification(null));
    }
    else
      callNotificationListeners(notification);
  }

  protected static ApplicationListener dataManagerListener = new ApplicationListener() {
    @Override
    public void deepThoughtChanged(DeepThought deepThought) {
      callDeepThoughtChangedListeners(deepThought);
    }

    @Override
    public void notification(Notification notification) {
      callNotificationListeners(notification);
    }
  };


  public static IDependencyResolver getDependencyResolver() {
    return dependencyResolver;
  }

  public static IPreferencesStore getPreferencesStore() {
    return preferencesStore;
  }

  public static IPlatformConfiguration getPlatformConfiguration() {
    return platformConfiguration;
  }

  public static IPlatformTools getPlatformTools() {
    return platformTools;
  }

  public static IApplicationLifeCycleService getLifeCycleService() {
    return lifeCycleService;
  }

  public static IThreadPool getThreadPool() {
    return threadPool;
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

  public static ILanguageDetector getLanguageDetector() {
    return languageDetector;
  }

  public static ISearchEngine getSearchEngine() {
    return searchEngine;
  }

  public static IEntitiesSearcherAndCreator getEntitiesSearcherAndCreator() {
    return entitiesSearcherAndCreator;
  }

  public static IHtmlHelper getHtmlHelper() {
    return htmlHelper;
  }

  public static IFileDownloader getDownloader() {
    return downloader;
  }

  public static IPluginManager getPluginManager() {
    return pluginManager;
  }

  public static IContentExtractorManager getContentExtractorManager() {
    return contentExtractorManager;
  }

  public static IDevicesFinder getDevicesFinder() {
    return devicesFinder;
  }

  public static IDeepThoughtConnector getDeepThoughtConnector() {
    return deepThoughtConnector;
  }

  public static IDeepThoughtSyncManager getSyncManager() {
    return syncManager;
  }

  public static IIsbnResolver getIsbnResolver() {
    return isbnResolver;
  }

  public static IHtmlEditorPool getHtmlEditorPool() {
    return htmlEditorPool;
  }

  public static IClipboardHelper getClipboardHelper() {
    return clipboardHelper;
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
    return dataFolderPath;
  }

  public static boolean isInstantiated() {
    return isInstantiated;
  }

  public static boolean hasOnlyReadOnlyAccess() {
    return hasOnlyReadOnlyAccess;
  }

  public static void setIsInstantiated(boolean isInstantiated) {
    Application.isInstantiated = isInstantiated;
  }
}

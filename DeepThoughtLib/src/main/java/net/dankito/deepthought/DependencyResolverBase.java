package net.dankito.deepthought;

import net.dankito.deepthought.application.IApplicationLifeCycleService;
import net.dankito.deepthought.application.NoOperationApplicationLifeCycleService;
import net.dankito.deepthought.clipboard.IClipboardHelper;
import net.dankito.deepthought.clipboard.NoOpClipboardHelper;
import net.dankito.deepthought.communication.DeepThoughtConnector;
import net.dankito.deepthought.communication.IDeepThoughtConnector;
import net.dankito.deepthought.communication.IDevicesFinder;
import net.dankito.deepthought.communication.UdpDevicesFinder;
import net.dankito.deepthought.controls.html.IHtmlEditorPool;
import net.dankito.deepthought.data.DefaultDataManager;
import net.dankito.deepthought.data.IDataManager;
import net.dankito.deepthought.data.backup.DefaultBackupManager;
import net.dankito.deepthought.data.backup.IBackupManager;
import net.dankito.deepthought.data.compare.DefaultDataComparer;
import net.dankito.deepthought.data.compare.IDataComparer;
import net.dankito.deepthought.data.contentextractor.ContentExtractorManager;
import net.dankito.deepthought.data.contentextractor.IContentExtractorManager;
import net.dankito.deepthought.data.download.IFileDownloader;
import net.dankito.deepthought.data.download.NoOpFileDownloader;
import net.dankito.deepthought.data.html.IHtmlHelper;
import net.dankito.deepthought.data.html.JsoupHtmlHelper;
import net.dankito.deepthought.data.listener.IExternalCallableEntityChangesService;
import net.dankito.deepthought.data.merger.DefaultDataMerger;
import net.dankito.deepthought.data.merger.IDataMerger;
import net.dankito.deepthought.data.persistence.EntityManagerConfiguration;
import net.dankito.deepthought.data.persistence.IEntityManager;
import net.dankito.deepthought.data.search.EntitiesSearcherAndCreator;
import net.dankito.deepthought.data.search.IEntitiesSearcherAndCreator;
import net.dankito.deepthought.data.search.ISearchEngine;
import net.dankito.deepthought.data.search.InMemorySearchEngine;
import net.dankito.deepthought.language.ILanguageDetector;
import net.dankito.deepthought.platform.IPlatformTools;
import net.dankito.deepthought.plugin.DefaultPluginManager;
import net.dankito.deepthought.plugin.IPluginManager;
import net.dankito.deepthought.util.IThreadPool;
import net.dankito.deepthought.util.isbn.IIsbnResolver;
import net.dankito.deepthought.util.isbn.MultiImplementationsIsbnResolver;
import net.dankito.deepthought.language.NoOpLanguageDetector;
import net.dankito.deepthought.util.ThreadPool;

/**
 * Created by ganymed on 05/01/15.
 */
public abstract class DependencyResolverBase<THtmlEditor> implements IDependencyResolver<THtmlEditor> {

  protected IEntityManager entityManager = null;

  protected IBackupManager backupManager = null;

  protected IDataComparer dataComparer = null;

  protected IDataMerger dataMerger = null;

  protected ILanguageDetector languageDetector = null;

  protected ISearchEngine searchEngine = null;

  protected IHtmlHelper htmlHelper = null;


  public DependencyResolverBase() {

  }

  public DependencyResolverBase(IEntityManager entityManager) {
    this();
    this.entityManager = entityManager;
  }

  public DependencyResolverBase(IEntityManager entityManager, IBackupManager backupManager) {
    this.entityManager = entityManager;
    this.backupManager = backupManager;
  }


  @Override
  public IThreadPool createThreadPool() {
    return new ThreadPool();
  }

  @Override
  public IEntityManager createEntityManager(EntityManagerConfiguration configuration) throws Exception {
    return entityManager;
  }

  @Override
  public IDataManager createDataManager(IEntityManager entityManager, IExternalCallableEntityChangesService entityChangesService) {
    return new DefaultDataManager(entityManager, entityChangesService);
  }

  @Override
  public IPlatformTools createPlatformTools() {
    return null;
  }

  @Override
  public IApplicationLifeCycleService createApplicationLifeCycleService() {
    return new NoOperationApplicationLifeCycleService();
  }

  @Override
  public IBackupManager createBackupManager() {
    if(backupManager == null)
      backupManager = new DefaultBackupManager();
    return backupManager;
  }

  @Override
  public IDataComparer createDataComparer() {
    if(dataComparer == null)
      dataComparer = new DefaultDataComparer();
    return dataComparer;
  }

  @Override
  public IDataMerger createDataMerger() {
    if(dataMerger == null)
      return dataMerger = new DefaultDataMerger();
    return dataMerger;
  }

  @Override
  public ILanguageDetector createLanguageDetector() {
    if(languageDetector == null)
//      languageDetector = new LanguageDetector();
      languageDetector = new NoOpLanguageDetector();
    return languageDetector;
  }

  @Override
  public ISearchEngine createSearchEngine() {
    if(searchEngine == null)
      searchEngine = new InMemorySearchEngine();
    return searchEngine;
  }

  @Override
  public IEntitiesSearcherAndCreator createEntitiesSearcherAndCreator() {
    return new EntitiesSearcherAndCreator();
  }

  @Override
  public IHtmlHelper createHtmlHelper() {
    if(htmlHelper == null)
      htmlHelper = new JsoupHtmlHelper();
    return htmlHelper;
  }

  public IFileDownloader createDownloader() {
    return new NoOpFileDownloader();
  }

  @Override
  public IPluginManager createPluginManager() {
    return new DefaultPluginManager();
  }

  @Override
  public IContentExtractorManager createContentExtractorManager() {
    return new ContentExtractorManager();
  }

  @Override
  public IDevicesFinder createDevicesFinder(IThreadPool threadPool) {
    return new UdpDevicesFinder(threadPool);
  }

  @Override
  public IDeepThoughtConnector createDeepThoughtConnector(IDevicesFinder devicesFinder, IThreadPool threadPool) {
    return new DeepThoughtConnector(devicesFinder, threadPool);
  }

  @Override
  public IIsbnResolver createIsbnResolver(IHtmlHelper htmlHelper, IThreadPool threadPool) {
    return new MultiImplementationsIsbnResolver(htmlHelper, threadPool);
  }

  @Override
  public IHtmlEditorPool createHtmlEditorPool() {
    return null;
  }

  @Override
  public IClipboardHelper createClipboardHelper() {
    return new NoOpClipboardHelper();
  }
}

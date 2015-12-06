package net.deepthought;

import net.deepthought.communication.DeepThoughtsConnector;
import net.deepthought.communication.IDeepThoughtsConnector;
import net.deepthought.data.DefaultDataManager;
import net.deepthought.data.IDataManager;
import net.deepthought.data.backup.DefaultBackupManager;
import net.deepthought.data.backup.IBackupManager;
import net.deepthought.data.compare.DefaultDataComparer;
import net.deepthought.data.compare.IDataComparer;
import net.deepthought.data.contentextractor.DefaultContentExtractorManager;
import net.deepthought.data.contentextractor.IContentExtractorManager;
import net.deepthought.data.download.IFileDownloader;
import net.deepthought.data.download.NoOpFileDownloader;
import net.deepthought.data.html.IHtmlHelper;
import net.deepthought.data.html.JsoupHtmlHelper;
import net.deepthought.data.merger.DefaultDataMerger;
import net.deepthought.data.merger.IDataMerger;
import net.deepthought.data.persistence.EntityManagerConfiguration;
import net.deepthought.data.persistence.IEntityManager;
import net.deepthought.data.search.ISearchEngine;
import net.deepthought.data.search.InMemorySearchEngine;
import net.deepthought.language.ILanguageDetector;
import net.deepthought.language.NoOpLanguageDetector;
import net.deepthought.platform.IPlatformTools;
import net.deepthought.plugin.DefaultPluginManager;
import net.deepthought.plugin.IPluginManager;
import net.deepthought.util.IThreadPool;
import net.deepthought.util.ThreadPool;
import net.deepthought.util.isbn.IIsbnResolver;
import net.deepthought.util.isbn.MultiImplementationsIsbnResolver;

/**
 * Created by ganymed on 05/01/15.
 */
public abstract class DependencyResolverBase implements IDependencyResolver {

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
  public IDataManager createDataManager(IEntityManager entityManager) {
    return new DefaultDataManager(entityManager);
  }

  @Override
  public IPlatformTools createPlatformTools() {
    return null;
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
    return new DefaultContentExtractorManager();
  }

  @Override
  public IDeepThoughtsConnector createDeepThoughtsConnector() {
    return new DeepThoughtsConnector();
  }

  @Override
  public IIsbnResolver createIsbnResolver(IHtmlHelper htmlHelper, IThreadPool threadPool) {
    return new MultiImplementationsIsbnResolver(htmlHelper, threadPool);
  }

}

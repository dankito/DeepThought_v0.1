package net.deepthought;

import net.deepthought.data.DefaultDataManager;
import net.deepthought.data.IDataManager;
import net.deepthought.data.backup.DefaultBackupManager;
import net.deepthought.data.backup.IBackupManager;
import net.deepthought.data.compare.DefaultDataComparer;
import net.deepthought.data.compare.IDataComparer;
import net.deepthought.data.html.IHtmlHelper;
import net.deepthought.data.html.JsoupHtmlHelper;
import net.deepthought.data.merger.DefaultDataMerger;
import net.deepthought.data.merger.IDataMerger;
import net.deepthought.data.persistence.EntityManagerConfiguration;
import net.deepthought.data.persistence.IEntityManager;
import net.deepthought.data.search.DefaultSearchEngine;
import net.deepthought.data.search.ISearchEngine;
import net.deepthought.language.ILanguageDetector;
import net.deepthought.language.NoOpLanguageDetector;

/**
 * Created by ganymed on 05/01/15.
 */
public class DefaultDependencyResolver implements IDependencyResolver {

  protected IEntityManager entityManager;

  protected IBackupManager backupManager = null;

  protected IDataComparer dataComparer = null;

  protected IDataMerger dataMerger = null;

  protected ILanguageDetector languageDetector = null;

  protected ISearchEngine searchEngine = null;

  protected IHtmlHelper htmlHelper = null;


  public DefaultDependencyResolver() {
    this(null);
  }

  public DefaultDependencyResolver(IEntityManager entityManager) {
    this.entityManager = entityManager;
  }

  public DefaultDependencyResolver(IEntityManager entityManager, IBackupManager backupManager) {
    this.entityManager = entityManager;
    this.backupManager = backupManager;
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
      searchEngine = new DefaultSearchEngine();
    return searchEngine;
  }

  @Override
  public IHtmlHelper createHtmlHelper() {
    if(htmlHelper == null)
      htmlHelper = new JsoupHtmlHelper();
    return htmlHelper;
  }

}

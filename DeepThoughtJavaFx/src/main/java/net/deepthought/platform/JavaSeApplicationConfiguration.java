package net.deepthought.platform;

import net.deepthought.DefaultDependencyResolver;
import net.deepthought.IApplicationConfiguration;
import net.deepthought.communication.listener.DeepThoughtsConnectorListener;
import net.deepthought.data.DeepThoughtFxProperties;
import net.deepthought.data.download.IFileDownloader;
import net.deepthought.data.download.WGetFileDownloader;
import net.deepthought.data.persistence.EntityManagerConfiguration;
import net.deepthought.data.persistence.IEntityManager;
import net.deepthought.data.search.ISearchEngine;
import net.deepthought.data.search.InMemorySearchEngine;
import net.deepthought.data.search.LuceneAndDatabaseSearchEngine;
import net.deepthought.javase.db.OrmLiteJavaSeEntityManager;
import net.deepthought.language.ILanguageDetector;
import net.deepthought.language.LanguageDetector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ganymed on 22/08/15.
 */
public class JavaSeApplicationConfiguration extends DefaultDependencyResolver implements IApplicationConfiguration {

  private final static Logger log = LoggerFactory.getLogger(JavaSeApplicationConfiguration.class);


  protected EntityManagerConfiguration entityManagerConfiguration;


  public JavaSeApplicationConfiguration(DeepThoughtsConnectorListener connectorListener) {
    super(connectorListener);

    this.entityManagerConfiguration = new EntityManagerConfiguration(getDataFolder());
  }


  @Override
  public EntityManagerConfiguration getEntityManagerConfiguration() {
    return entityManagerConfiguration;
  }

  @Override
  public IEntityManager createEntityManager(EntityManagerConfiguration configuration) throws Exception {
    return new OrmLiteJavaSeEntityManager(configuration);
  }

  @Override
  public ISearchEngine createSearchEngine() {
    try {
//          return new LuceneSearchEngine();
      return new LuceneAndDatabaseSearchEngine();
    } catch(Exception ex) {
      log.error("Could not initialize LuceneSearchEngine", ex);
    }
    return new InMemorySearchEngine(); // TODO: abort application?
  }

  @Override
  public IFileDownloader createDownloader() {
    return new WGetFileDownloader();
  }

  @Override
  public ILanguageDetector createLanguageDetector() {
    return new LanguageDetector();
  }


  public String getDataFolder() {
    return DeepThoughtFxProperties.getDataFolderOrCreateDefaultValuesOnNull();
  }

  public void setDataFolder(String dataFolder) {
    DeepThoughtFxProperties.setDataFolder(dataFolder);
  }

  public int getCurrentDataModelVersion() {
    return DeepThoughtFxProperties.getDataModelVersion();
  }

  public void setCurrentDataModelVersion(int newDataModelVersion) {
    DeepThoughtFxProperties.setDataModelVersion(newDataModelVersion);
  }
}

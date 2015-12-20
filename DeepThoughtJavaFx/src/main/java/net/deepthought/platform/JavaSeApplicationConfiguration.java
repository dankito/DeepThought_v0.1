package net.deepthought.platform;

import net.deepthought.DependencyResolverBase;
import net.deepthought.IApplicationConfiguration;
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
import net.deepthought.plugin.IPlugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by ganymed on 22/08/15.
 */
public class JavaSeApplicationConfiguration extends DependencyResolverBase implements IApplicationConfiguration {

  private final static Logger log = LoggerFactory.getLogger(JavaSeApplicationConfiguration.class);


  protected IPreferencesStore preferencesStore;

  protected IPlatformConfiguration platformConfiguration;

  protected EntityManagerConfiguration entityManagerConfiguration;


  public JavaSeApplicationConfiguration() {
    this.preferencesStore = new JavaSePreferencesStore();
    this.platformConfiguration = new JavaSePlatformConfiguration();
    this.entityManagerConfiguration = new EntityManagerConfiguration(preferencesStore.getDataFolder(), preferencesStore.getDatabaseDataModelVersion());
  }


  @Override
  public IPreferencesStore getPreferencesStore() {
    return preferencesStore;
  }

  @Override
  public IPlatformConfiguration getPlatformConfiguration() {
    return platformConfiguration;
  }

  @Override
  public EntityManagerConfiguration getEntityManagerConfiguration() {
    return entityManagerConfiguration;
  }

  @Override
  public Collection<IPlugin> getStaticallyLinkedPlugins() {
    return new ArrayList<>();
//    return Arrays.asList(new IPlugin[]{new SueddeutscheContentExtractor(), new SueddeutscheMagazinContentExtractor(), new SueddeutscheJetztContentExtractor(),
//        new PostillonContentExtractor(), new HeiseContentExtractor(), new ZeitContentExtractor(), new SpiegelContentExtractor(),
//        new YouTubeAndVimeoContentExtractor()});
  }

  @Override
  public IEntityManager createEntityManager(EntityManagerConfiguration configuration) throws Exception {
    return new OrmLiteJavaSeEntityManager(configuration);
  }

  @Override
  public IPlatformTools createPlatformTools() {
    return new JavaSePlatformTools();
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

}

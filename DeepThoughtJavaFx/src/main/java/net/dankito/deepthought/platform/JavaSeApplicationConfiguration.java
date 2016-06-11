package net.dankito.deepthought.platform;

import net.dankito.deepthought.DependencyResolverBase;
import net.dankito.deepthought.IApplicationConfiguration;
import net.dankito.deepthought.controls.html.DeepThoughtFxHtmlEditor;
import net.dankito.deepthought.controls.html.IHtmlEditorPool;
import net.dankito.deepthought.data.download.IFileDownloader;
import net.dankito.deepthought.data.download.WGetFileDownloader;
import net.dankito.deepthought.data.persistence.EntityManagerConfiguration;
import net.dankito.deepthought.data.persistence.IEntityManager;
import net.dankito.deepthought.data.search.ISearchEngine;
import net.dankito.deepthought.data.search.InMemorySearchEngine;
import net.dankito.deepthought.data.search.LuceneAndDatabaseSearchEngine;
import net.dankito.deepthought.javase.db.OrmLiteJavaSeEntityManager;
import net.dankito.deepthought.language.ILanguageDetector;
import net.dankito.deepthought.language.LanguageDetector;
import net.dankito.deepthought.plugin.IPlugin;
import net.dankito.deepthought.util.localization.Localization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by ganymed on 22/08/15.
 */
public class JavaSeApplicationConfiguration extends DependencyResolverBase<DeepThoughtFxHtmlEditor> implements IApplicationConfiguration<DeepThoughtFxHtmlEditor> {

  private final static Logger log = LoggerFactory.getLogger(JavaSeApplicationConfiguration.class);


  protected IPreferencesStore preferencesStore;

  protected IPlatformConfiguration platformConfiguration;

  protected EntityManagerConfiguration entityManagerConfiguration;


  public JavaSeApplicationConfiguration() {
    this.preferencesStore = new JavaSePreferencesStore();
    this.platformConfiguration = new net.dankito.deepthought.platform.JavaSePlatformConfiguration();
    this.entityManagerConfiguration = new EntityManagerConfiguration(preferencesStore.getDataFolder(), preferencesStore.getDatabaseDataModelVersion());

    net.dankito.deepthought.util.localization.JavaFxLocalization.setLocale(Localization.getLanguageLocale());
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
    List<IPlugin> staticPlugins = new ArrayList<>();

//    staticPlugins.addAll(Arrays.asList(new IPlugin[]{new SueddeutscheMagazinContentExtractor(), new SueddeutscheJetztContentExtractor(), new SueddeutscheContentExtractor(),
//        new PostillonContentExtractor(), new DerFreitagContentExtractor(), new HeiseContentExtractor(), new ZeitContentExtractor(), new SpiegelContentExtractor()}));
//    staticPlugins.add(new YouTubeAndVimeoContentExtractor());

    return staticPlugins;
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

  @Override
  public IHtmlEditorPool createHtmlEditorPool() {
    return new net.dankito.deepthought.controls.html.DeepThoughtFxHtmlEditorPool();
  }


}

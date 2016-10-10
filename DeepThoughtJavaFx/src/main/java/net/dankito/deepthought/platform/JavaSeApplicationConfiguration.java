package net.dankito.deepthought.platform;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.DependencyResolverBase;
import net.dankito.deepthought.IApplicationConfiguration;
import net.dankito.deepthought.application.IApplicationLifeCycleService;
import net.dankito.deepthought.clipboard.IClipboardHelper;
import net.dankito.deepthought.communication.ICommunicationConfigurationManager;
import net.dankito.deepthought.communication.connected_device.IConnectedRegisteredDevicesListenerManager;
import net.dankito.deepthought.communication.connected_device.IDevicesFinderListenerManager;
import net.dankito.deepthought.controls.html.DeepThoughtFxHtmlEditor;
import net.dankito.deepthought.controls.html.IHtmlEditorPool;
import net.dankito.deepthought.data.download.IFileDownloader;
import net.dankito.deepthought.data.download.WGetFileDownloader;
import net.dankito.deepthought.data.html.IHtmlHelper;
import net.dankito.deepthought.data.html.JsoupAndBoilerpipeHtmlHelper;
import net.dankito.deepthought.data.listener.ApplicationListener;
import net.dankito.deepthought.data.model.Category;
import net.dankito.deepthought.data.model.DeepThought;
import net.dankito.deepthought.data.model.Entry;
import net.dankito.deepthought.data.model.FileLink;
import net.dankito.deepthought.data.model.Person;
import net.dankito.deepthought.data.model.Reference;
import net.dankito.deepthought.data.model.ReferenceSubDivision;
import net.dankito.deepthought.data.model.SeriesTitle;
import net.dankito.deepthought.data.model.Tag;
import net.dankito.deepthought.data.model.User;
import net.dankito.deepthought.data.persistence.CouchbaseLiteEntityManagerBase;
import net.dankito.deepthought.data.persistence.EntityManagerConfiguration;
import net.dankito.deepthought.data.persistence.IEntityManager;
import net.dankito.deepthought.data.persistence.JavaCouchbaseLiteEntityManager;
import net.dankito.deepthought.data.search.ISearchEngine;
import net.dankito.deepthought.data.search.InMemorySearchEngine;
import net.dankito.deepthought.data.search.LuceneSearchEngine;
import net.dankito.deepthought.data.sync.CouchbaseLiteSyncManager;
import net.dankito.deepthought.data.sync.IDeepThoughtSyncManager;
import net.dankito.deepthought.javase.db.OrmLiteJavaSeEntityManager;
import net.dankito.deepthought.language.ILanguageDetector;
import net.dankito.deepthought.language.LanguageDetector;
import net.dankito.deepthought.plugin.IPlugin;
import net.dankito.deepthought.util.IThreadPool;
import net.dankito.deepthought.util.JavaFxClipboardHelper;
import net.dankito.deepthought.util.Notification;
import net.dankito.deepthought.util.NotificationType;
import net.dankito.deepthought.util.file.FileUtils;
import net.dankito.deepthought.util.localization.Localization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
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

  protected IApplicationLifeCycleService lifeCycleService;

  @Override
  public IApplicationLifeCycleService createApplicationLifeCycleService() {
    return lifeCycleService;
  }

  public JavaSeApplicationConfiguration(IApplicationLifeCycleService lifeCycleService) {
    this.lifeCycleService = lifeCycleService;
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

//    staticPlugins.addAll(Arrays.asList(new IPlugin[]{
//        new SueddeutscheMagazinContentExtractor(), new SueddeutscheJetztContentExtractor(), new SueddeutscheContentExtractor(),
//        new PostillonContentExtractor(), new DerFreitagContentExtractor(), new CtContentExtractor(), new HeiseContentExtractor(),
//        new ZeitContentExtractor(), new SpiegelContentExtractor()
//    }));

//    staticPlugins.add(new YouTubeAndVimeoContentExtractor());

    return staticPlugins;
  }

  protected OrmLiteJavaSeEntityManager importSqliteDataEntityManager;

  @Override
  public IEntityManager createEntityManager(EntityManagerConfiguration configuration) throws Exception {
    try {
//      importSqliteDataEntityManager = new OrmLiteJavaSeEntityManager(new EntityManagerConfiguration(configuration.getDataFolder(), EntityManagerConfiguration.DatabaseType.SQLite));
//      Application.addApplicationListener(importSqliteDataApplicationListener);
    } catch(Exception e) {
      log.error("Could not instantiate SQLite EntityManager", e);
    }

    return new JavaCouchbaseLiteEntityManager(configuration);
  }

  protected ApplicationListener importSqliteDataApplicationListener = new ApplicationListener() {
    @Override
    public void deepThoughtChanged(DeepThought deepThought) {

    }

    @Override
    public void notification(Notification notification) {
      if(notification.getType() == NotificationType.ApplicationInstantiated) {
        Application.getThreadPool().runTaskAsync(new Runnable() {
          @Override
          public void run() {
            try {
              importSqliteData();
            } catch(Exception e) {
              log.error("Could not import data from SQLite database", e);
            }
          }
        });
      }
    }
  };

  protected void importSqliteData() {
    if(importSqliteDataEntityManager != null && new File(importSqliteDataEntityManager.getDatabasePath()).exists()) {
      List<Entry> entries = importSqliteDataEntityManager.getAllEntitiesOfType(Entry.class);
      List<Tag> tags = importSqliteDataEntityManager.getAllEntitiesOfType(Tag.class);
      List<SeriesTitle> seriesTitles = importSqliteDataEntityManager.getAllEntitiesOfType(SeriesTitle.class);
      List<Reference> references = importSqliteDataEntityManager.getAllEntitiesOfType(Reference.class);
      List<ReferenceSubDivision> subDivisions = importSqliteDataEntityManager.getAllEntitiesOfType(ReferenceSubDivision.class);
      List<Person> persons = importSqliteDataEntityManager.getAllEntitiesOfType(Person.class);
      List<Category> categories = importSqliteDataEntityManager.getAllEntitiesOfType(Category.class);
      List<FileLink> files = importSqliteDataEntityManager.getAllEntitiesOfType(FileLink.class);

      DeepThought deepThought = Application.getDeepThought();
      User loggedOnUser = Application.getLoggedOnUser();
      IEntityManager couchbaseEntityManager = Application.getEntityManager();

      for(Tag tag : tags) {
        if(tag.isDeleted() == false) {
          tag.setId(null);
          tag.setOwner(null);

          couchbaseEntityManager.persistEntity(tag);
          deepThought.addTag(tag);
        }
      }

      for(Category category : categories) {
        if(category.isDeleted() == false && category != deepThought.getTopLevelCategory()) {
          category.setId(null);
          category.setOwner(null);
          if (category.isTopLevelCategory())
            category.setParentCategory(null);

          couchbaseEntityManager.persistEntity(category);
          deepThought.addCategory(category);
        }
      }

      for(Person person : persons) {
        if(person.isDeleted() == false) {
          person.setId(null);
          person.setOwner(null);

          couchbaseEntityManager.persistEntity(person);
          deepThought.addPerson(person);
        }
      }

      for(FileLink file : files) {
        if(file.isDeleted() == false) {
          file.setId(null);
          file.setFileType(FileUtils.getFileType(file));

          couchbaseEntityManager.persistEntity(file);
          deepThought.addFile(file);
        }
      }

      for(SeriesTitle series : seriesTitles) {
        if(series.isDeleted() == false) {
          series.setId(null);
          series.setOwner(null);

          List<Person> associatedPersons = new ArrayList<>(series.getPersons());

          couchbaseEntityManager.persistEntity(series);
          deepThought.addSeriesTitle(series);

          for (Person person : associatedPersons) {
            series.removePerson(person);
            series.addPerson(person);
          }
        }
      }

      for(Reference reference : references) {
        if(reference.isDeleted() == false) {
          reference.setId(null);
          reference.setOwner(null);

          List<Person> associatedPersons = new ArrayList<>(reference.getPersons());

          couchbaseEntityManager.persistEntity(reference);
          deepThought.addReference(reference);

          for (Person person : associatedPersons) {
            reference.removePerson(person);
            reference.addPerson(person);
          }
        }
      }

      for(ReferenceSubDivision subDivision : subDivisions) {
        if(subDivision.isDeleted() == false) {
          subDivision.setId(null);
          subDivision.setOwner(null);

          List<Person> associatedPersons = new ArrayList<>(subDivision.getPersons());

          couchbaseEntityManager.persistEntity(subDivision);
          deepThought.addReferenceSubDivision(subDivision);

          for (Person person : associatedPersons) {
            subDivision.removePerson(person);
            subDivision.addPerson(person);
          }
        }
      }


      for (Entry entry : entries) {
        if(entry.isDeleted() == false && entry != deepThought.getTopLevelEntry()) {
          entry.setId(null);
          entry.setOwner(null);
          entry.setParentEntry(null);

          List<Tag> associatedTags = new ArrayList<>(entry.getTags());
          List<Category> associatedCategories = new ArrayList<>(entry.getCategories());
          List<Person> associatedPersons = new ArrayList<>(entry.getPersons());
          SeriesTitle associatedSeries = entry.getSeries();
          Reference associatedReference = entry.getReference();
          ReferenceSubDivision associatedSubDivision = entry.getReferenceSubDivision();
          List<FileLink> attachedFiles = new ArrayList<>(entry.getAttachedFiles());
          List<FileLink> embeddedFiles = new ArrayList<>(entry.getEmbeddedFiles());

          couchbaseEntityManager.persistEntity(entry);
          deepThought.addEntry(entry);

          for (Tag tag : associatedTags) {
            entry.removeTag(tag);
            entry.addTag(tag);
          }

          for (Category category : associatedCategories) {
            entry.removeCategory(category);
            entry.addCategory(category);
          }

          for (Person person : associatedPersons) {
            entry.removePerson(person);
            entry.addPerson(person);
          }

          entry.setSeries(null);
          entry.setSeries(associatedSeries);

          entry.setReference(null);
          entry.setReference(associatedReference);
          if(associatedReference != null && associatedReference.getSeries() == null && associatedSeries != null) {
            associatedReference.setSeries(associatedSeries);
          }

          entry.setReferenceSubDivision(null);
          entry.setReferenceSubDivision(associatedSubDivision);
          if(associatedSubDivision != null && associatedSubDivision.getReference() == null && associatedReference != null) {
            associatedSubDivision.setReference(associatedReference);
          }

          for (FileLink file : attachedFiles) {
            entry.removeAttachedFile(file);
            entry.addAttachedFile(file);
          }

          for (FileLink file : embeddedFiles) {
            entry.removeEmbeddedFile(file);
            entry.addEmbeddedFile(file);
          }

          entry.setDeepThought(deepThought);
          couchbaseEntityManager.updateEntity(entry);
        }
      }


      for(Tag tag : tags) {
        if(tag.isDeleted() == false) {
          tag.setDeepThought(deepThought);
          couchbaseEntityManager.updateEntity(tag);
        }
      }

      for(Category category : categories) {
        if(category.isDeleted() == false && category != deepThought.getTopLevelCategory()) {
          category.setDeepThought(deepThought);
          couchbaseEntityManager.updateEntity(category);
        }
      }

      for(SeriesTitle series : seriesTitles) {
        if(series.isDeleted() == false) {
          series.setDeepThought(deepThought);
          couchbaseEntityManager.updateEntity(series);
        }
      }

      for(Reference reference : references) {
        if(reference.isDeleted() == false) {
          reference.setDeepThought(deepThought);
          couchbaseEntityManager.updateEntity(reference);
        }
      }

      for(ReferenceSubDivision subDivision : subDivisions) {
        if(subDivision.isDeleted() == false) {
          subDivision.setDeepThought(deepThought);
          couchbaseEntityManager.updateEntity(subDivision);
        }
      }

      for(Person person : persons) {
        if(person.isDeleted() == false) {
          person.setDeepThought(deepThought);
          couchbaseEntityManager.updateEntity(person);
        }
      }

      for(FileLink file : files) {
        if(file.isDeleted() == false) {
          file.setDeepThought(deepThought);
          couchbaseEntityManager.updateEntity(file);
        }
      }
    }
  }


  @Override
  public IPlatformTools createPlatformTools() {
    return new JavaSePlatformTools();
  }

  @Override
  public ISearchEngine createSearchEngine() {
    try {
          return new LuceneSearchEngine();
//      return new LuceneAndDatabaseSearchEngine();
    } catch(Exception ex) {
      log.error("Could not initialize LuceneSearchEngine", ex);
    }

    // TODO: implement InMemorySearchEngine
    return new InMemorySearchEngine();  // TODO: abort application?
  }

  @Override
  public IFileDownloader createDownloader() {
    return new WGetFileDownloader();
  }

  @Override
  public IDeepThoughtSyncManager createSyncManager(IEntityManager entityManager, IConnectedRegisteredDevicesListenerManager connectedDevicesListenerManager,
                                                   IDevicesFinderListenerManager devicesFinderListenerManager, ICommunicationConfigurationManager configurationManager, IThreadPool threadPool) {
    return new CouchbaseLiteSyncManager((CouchbaseLiteEntityManagerBase)entityManager, threadPool, connectedDevicesListenerManager,
        devicesFinderListenerManager, configurationManager);
  }

  @Override
  public ILanguageDetector createLanguageDetector() {
    return new LanguageDetector();
  }

  @Override
  public IHtmlHelper createHtmlHelper() {
    return new JsoupAndBoilerpipeHtmlHelper();
  }

  @Override
  public IHtmlEditorPool createHtmlEditorPool() {
    return new net.dankito.deepthought.controls.html.DeepThoughtFxHtmlEditorPool();
  }

  @Override
  public IClipboardHelper createClipboardHelper() {
    return new JavaFxClipboardHelper();
  }

}

package net.deepthought.data;

import net.deepthought.Application;
import net.deepthought.data.backup.IBackupManager;
import net.deepthought.data.listener.ApplicationListener;
import net.deepthought.data.model.Category;
import net.deepthought.data.model.DeepThought;
import net.deepthought.data.model.DeepThoughtApplication;
import net.deepthought.data.model.Device;
import net.deepthought.data.model.EntriesLinkGroup;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.FileLink;
import net.deepthought.data.model.Group;
import net.deepthought.data.model.Note;
import net.deepthought.data.model.Person;
import net.deepthought.data.model.Tag;
import net.deepthought.data.model.User;
import net.deepthought.data.model.enums.ApplicationLanguage;
import net.deepthought.data.model.listener.EntityListener;
import net.deepthought.data.model.settings.UserDeviceSettings;
import net.deepthought.data.persistence.EntityManagerConfiguration;
import net.deepthought.data.persistence.IEntityManager;
import net.deepthought.data.persistence.db.AssociationEntity;
import net.deepthought.data.persistence.db.BaseEntity;
import net.deepthought.util.DeepThoughtError;
import net.deepthought.util.FileUtils;
import net.deepthought.util.Localization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by ganymed on 13/10/14.
 */
public class DefaultDataManager implements IDataManager {

  private final static Logger log = LoggerFactory.getLogger(DefaultDataManager.class);


  protected DeepThoughtApplication application = null;
  protected User loggedOnUser = null;
  protected DeepThought currentDeepThought = null;

  protected IEntityManager entityManager;

  protected String dataFolder;

  protected Set<ApplicationListener> applicationListeners = new CopyOnWriteArraySet<>();

  protected Set<BaseEntity> unpersistedUpdatedEntities = new CopyOnWriteArraySet<>();
  protected Timer persistUpdatedEntitiesTimer = null;


  public DefaultDataManager(IEntityManager entityManager) {
    this.entityManager = entityManager; // TODO: how to set dataCollectionPersistencePath?

    File databasePath = new File(entityManager.getDatabasePath());
    if(databasePath.getParentFile() != null) { // on Android parent file will be null, but data folder will be set explicitly there
      if(databasePath.getParentFile().getName().startsWith("DeepThoughtDb"))
        this.dataFolder = databasePath.getParentFile().getParent() + "/";
      else
        this.dataFolder = databasePath.getParent() + "/";
    }
    else
      this.dataFolder = "";
  }

  public void close() {
    persistUpdatedEntities();

    Application.getBackupManager().createBackupsForAllRegisteredBackupFileServices();

    if(entityManager != null) {
      if(currentDeepThought != null)
        entityManager.updateEntity(currentDeepThought);
      entityManager.close();
    }

    application = null;
    loggedOnUser = null;
    currentDeepThought = null;
    entityManager = null;
  }


  public DeepThought retrieveDeepThoughtApplication() {
    try {
      List<DeepThoughtApplication> applicationsQueryResult = entityManager.getAllEntitiesOfType(DeepThoughtApplication.class);

      if (applicationsQueryResult.size() > 0) { // TODO: what to do if there's more than one DeepThoughtApplication instance persisted?
        application = applicationsQueryResult.get(0);
        loggedOnUser = application.getLastLoggedOnUser();

        application.addEntityListener(entityListener);

        // TODO: what to return if user was already logged on but autoLogOn is set to false?
        if (application.autoLogOnLastLoggedOnUser()) {
          DeepThought deepThought = loggedOnUser.getLastViewedDeepThought();
          setCurrentDeepThought(deepThought);
          return deepThought;
        }
      }
    } catch(Exception ex) {
      log.error("Could not deserialize DeepThoughtApplication", ex);
      // TODO: determine if this is ok because this is the first Application start or if a severe error occurred?
    }

    return createAndPersistDefaultDeepThought();
  }

  protected DeepThought createAndPersistDefaultDeepThought() {
    application = DeepThoughtApplication.createApplication();
    loggedOnUser = application.getLastLoggedOnUser();

    DeepThought newDeepThought = loggedOnUser.getLastViewedDeepThought();

//    entityManager.persistEntity(loggedOnUser);
    entityManager.persistEntity(application);
    entityManager.persistEntity(newDeepThought);
    Application.getSettings().setLanguage(Application.getSettings().getLanguage());
    newDeepThought.getSettings().setLastViewedCategory(newDeepThought.getSettings().getLastViewedCategory());

    addNewlyCreatedApplicationEntityListeners();
    setCurrentDeepThought(newDeepThought);

    return newDeepThought;
  }

  protected void addNewlyCreatedApplicationEntityListeners() {
    application.addEntityListener(entityListener);

    for(User user : application.getUsers())
      user.addEntityListener(entityListener);

    for(Group group : application.getGroups())
      group.addEntityListener(entityListener);

    for(Device device : application.getDevices())
      device.addEntityListener(entityListener);

    for(ApplicationLanguage language : application.getApplicationLanguages())
      language.addEntityListener(entityListener);
  }

  protected void setCurrentDeepThought(DeepThought deepThought) {
    if(currentDeepThought != null)
//      deepThought.removeEntityListener(entityListener);
      deepThought.removeEntityListener(entityListener);

    currentDeepThought = deepThought;

    if(currentDeepThought != null)
//      deepThought.addEntityListener(entityListener);
      deepThought.addEntityListener(entityListener);

    callDeepThoughtChangedListeners(currentDeepThought);
  }


  // Created Entities get persisted immediately
  protected void entityCreated(BaseEntity entity) {
    log.info("Entity {} has been created", entity);
    entityManager.persistEntity(entity);
  }

  // On updated Entities it depends if autoSaveChanges is set to true and after which time changes should be persisted
  protected synchronized void entityUpdated(BaseEntity entity) {
    log.info("Entity {} has been updated", entity);

    if(getSettings().autoSaveChanges() == true) {
      if (getSettings().getAutoSaveChangesAfterMilliseconds() == 0) // persist modified entities immediately
        persistUpdatedEntity(entity);
      else {
        unpersistedUpdatedEntities.add(entity);

        if (persistUpdatedEntitiesTimer == null) { // if persistUpdatedEntitiesTimer != null it has already been set another entity
          createPersistUpdatedEntitiesTimer();
        }
      }
    }
  }

  // Deleted Entities also get persisted immediately (we don't delete any Entities physically, we only set its 'Deleted' flag to true)
  protected void entityDeleted(BaseEntity entity) {
    log.info("Entity {} has been deleted", entity);

    if(entity instanceof AssociationEntity) { // AssociationEntities really get deleted from Database
      if(entity.isDeleted() == false) {
        entity.setDeleted();
        entityManager.deleteEntity(entity);
      }
    }
    else { // on all other Entities the Deleted field just gets set to true
      entity.setDeleted();
      entityManager.updateEntity(entity);
    }
  }

  protected void createPersistUpdatedEntitiesTimer() {
    persistUpdatedEntitiesTimer = new Timer("Persist updated Entities timer");

    persistUpdatedEntitiesTimer.schedule(new TimerTask() {
      @Override
      public void run() {
        persistUpdatedEntitiesTimer = null;
        persistUpdatedEntities();
      }
    }, getSettings().getAutoSaveChangesAfterMilliseconds());
  }

  protected void persistUpdatedEntities() {
    for(BaseEntity updatedEntity : unpersistedUpdatedEntities)
      persistUpdatedEntity(updatedEntity);

    unpersistedUpdatedEntities.clear();
  }

  protected void persistUpdatedEntity(BaseEntity entity) {
    log.info("Persisting updated Entity {}", entity);
    entityManager.updateEntity(entity);
  }


  protected EntityListener entityListener = new EntityListener() {
    @Override
    public void propertyChanged(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {
      DefaultDataManager.this.entityUpdated(entity);
    }

    @Override
    public void entityAddedToCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity) {
//      addedEntity.addEntityListener(baseEntityListener);
      if(addedEntity.isPersisted() == false)
        DefaultDataManager.this.entityCreated(addedEntity);
    }

    @Override
    public void entityOfCollectionUpdated(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity updatedEntity) {
      DefaultDataManager.this.entityUpdated(updatedEntity);

      if(updatedEntity instanceof Entry)
        Application.getSearchEngine().updateIndexForEntity((Entry) updatedEntity, "");
    }

    @Override
    public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {
//      removedEntity.removeEntityListener(baseEntityListener);
      if(collectionHolder instanceof DeepThought || collectionHolder instanceof DeepThoughtApplication || removedEntity instanceof AssociationEntity)
        DefaultDataManager.this.entityDeleted(removedEntity);
    }
  };


  public boolean addApplicationListener(ApplicationListener listener) {
    if(listener == null)
      return false;
    return applicationListeners.add(listener);
  }

  public boolean removeApplicationListener(ApplicationListener listener) {
    return applicationListeners.remove(listener);
  }

  protected void callDeepThoughtChangedListeners(DeepThought deepThought) {
    for(ApplicationListener listener : applicationListeners)
      listener.deepThoughtChanged(deepThought);
  }

  protected void callErrorOccurredListeners(DeepThoughtError error) {
    for(ApplicationListener listener : applicationListeners)
      listener.errorOccurred(error);
  }


  public void ensureAllLazyLoadingDataIsLoaded(BaseEntity entity) {
    try {
      Application.getEntityManager().resolveAllLazyRelations(application);
    } catch(Exception ex) {
      log.error("Could not resolve all Lazy loaded fields", ex);
    }

//    ensureAllLazyLoadingDataIsLoadedRecursive(entity);
  }

  protected void ensureAllLazyLoadingDataIsLoadedRecursive(BaseEntity entity) {
    if(entity instanceof DeepThoughtApplication) {
      DeepThoughtApplication application = (DeepThoughtApplication)entity;
      for(User user : application.getUsers())
        ensureAllLazyLoadingDataIsLoadedRecursive(user);
      for(Group group : application.getGroups())
        ensureAllLazyLoadingDataIsLoadedRecursive(group);
    }

    if(entity instanceof Group) {
      Group group = (Group)entity;
      group.getName();
      for(User user : group.getUsers()){}
//        ensureAllLazyLoadingDataIsLoadedRecursive(user);
    }

    if(entity instanceof Device) {
      Device device = (Device)entity;
      device.getName();
//      for(User user : device.getUsers())
//        ensureAllLazyLoadingDataIsLoadedRecursive(user);
    }

    if(entity instanceof User) {
      User user = (User)entity;
      user.getUserName();
      for(DeepThought deepThought : user.getDeepThoughts())
        ensureAllLazyLoadingDataIsLoadedRecursive(deepThought);
      for(Device device : user.getDevices())
        ensureAllLazyLoadingDataIsLoadedRecursive(device);
      for(Group group : user.getGroups())
        ensureAllLazyLoadingDataIsLoadedRecursive(group);
    }

    if(entity instanceof DeepThought) {
      DeepThought deepThought = (DeepThought)entity;

      Set<Entry> entries = new HashSet<>(deepThought.getEntries());
      deepThought.getSettings().setLastViewedEntry(deepThought.getSettings().getLastViewedEntry());

      deepThought.getSettings().getLastSelectedTab();
      ensureAllLazyLoadingDataIsLoadedRecursive(deepThought.getTopLevelCategory());
      for(Category category : deepThought.getCategories())
        ensureAllLazyLoadingDataIsLoadedRecursive(category);
      for(Entry entry : deepThought.getEntries())
        ensureAllLazyLoadingDataIsLoadedRecursive(entry);
      for(Tag tag : deepThought.getTags())
        ensureAllLazyLoadingDataIsLoadedRecursive(tag);
      for(Person person : deepThought.getPersons())
        ensureAllLazyLoadingDataIsLoadedRecursive(person);

      ensureAllLazyLoadingDataIsLoadedRecursive(deepThought.getSettings().getLastViewedCategory());
      ensureAllLazyLoadingDataIsLoadedRecursive(deepThought.getSettings().getLastViewedEntry());
      ensureAllLazyLoadingDataIsLoadedRecursive(deepThought.getSettings().getLastViewedTag());
    }

    if(entity instanceof Category) {
      Category category = (Category) entity;
      category.getName();
//      ensureAllLazyLoadingDataIsLoadedRecursive(category.getParentCategory());
      category.getParentCategory();
      for(Category subCategory : category.getSubCategories())
        ensureAllLazyLoadingDataIsLoadedRecursive(subCategory);
      for(Entry entry : category.getEntries()){}
//        ensureAllLazyLoadingDataIsLoadedRecursive(entry);
//      ensureAllLazyLoadingDataIsLoadedRecursive(category.getDeepThought());
      category.getDeepThought();
    }

    if(entity instanceof Entry) {
      Entry entry = (Entry)entity;
      for(Category category : entry.getCategories())
        ensureAllLazyLoadingDataIsLoadedRecursive(category);
      for(Tag tag : entry.getTags())
        ensureAllLazyLoadingDataIsLoadedRecursive(tag);
      for(Person person : entry.getPersons()) {
          ensureAllLazyLoadingDataIsLoadedRecursive(person);
      }
      for(FileLink file : entry.getFiles())
        ensureAllLazyLoadingDataIsLoadedRecursive(file);
      for(Note note : entry.getNotes())
        ensureAllLazyLoadingDataIsLoadedRecursive(note);
      for(EntriesLinkGroup link : entry.getLinkGroups())
        ensureAllLazyLoadingDataIsLoadedRecursive(link);
      // TODO: has this to be done are is this already covered by deepThought.getEntries() ?
      for(Entry subEntry : entry.getSubEntries())
        ensureAllLazyLoadingDataIsLoadedRecursive(subEntry);
//      ensureAllLazyLoadingDataIsLoadedRecursive(entry.getDeepThought());
      entry.getDeepThought();
    }

    if(entity instanceof Tag) {
      Tag tag = (Tag) entity;
      tag.getName();
      for(Entry entry : tag.getEntries()) {}
//        ensureAllLazyLoadingDataIsLoadedRecursive(entry);
//      ensureAllLazyLoadingDataIsLoadedRecursive(tag.getDeepThought());
      tag.getDeepThought();
    }

    if(entity instanceof Person) {
      Person person = (Person) entity;
      person.getLastName();
    }

    if(entity instanceof Note) {
      Note note = (Note) entity;
      note.getNote();
//      ensureAllLazyLoadingDataIsLoadedRecursive(note.getEntryFragment());
      note.getEntry();
    }

    if(entity instanceof FileLink) {
      FileLink file = (FileLink) entity;
      file.getName();
//      ensureAllLazyLoadingDataIsLoadedRecursive(file.getEntryFragment());
//      ensureAllLazyLoadingDataIsLoadedRecursive(file.getPreviewImage());
      file.getEntries();
    }

    if(entity instanceof EntriesLinkGroup) {
      EntriesLinkGroup link = (EntriesLinkGroup) entity;
      link.getGroupName();
      for(Entry entry : link.getEntries()){}
//        ensureAllLazyLoadingDataIsLoadedRecursive(entry);
    }
  }

  @Override
  public void deleteExistingDataCollection() {
    entityManager.close();

    callDeepThoughtChangedListeners(null);

    FileUtils.moveFile(new File(getDataCollectionSavePath()), new File(Application.getBackupManager().getRestoredBackupsFolder(), getDeletedDataCollectionFileName()));
  }

  protected String getDeletedDataCollectionFileName() {
    String dataCollectionFileName = new File(getDataCollectionSavePath()).getName();

    return IBackupManager.BackupDateFormat.format(new Date()) + "_" + "DeletedDataCollection_" + dataCollectionFileName;
  }

  @Override
  public void replaceExistingDataCollectionWithData(final DeepThoughtApplication data) {
    deleteExistingDataCollection();

    try {
      EntityManagerConfiguration configuration = Application.getEntityManagerConfiguration().copy();
      configuration.setCreateDatabase(true);
      configuration.setCreateTables(true);
      entityManager = Application.getDependencyResolver().createEntityManager(configuration);

//      if(entityManager.persistEntity(data) == false) {
//        log.debug("Could not persist {} to replace current Data, trying to merge it ...", data);
//
//        if(entityManager.updateEntity(data) == false) {
//          log.error("Also updating (merging) {} didn't succeed", data);
//        }
//      }

//      retrieveDeepThoughtApplication();

      Application.getDataMerger().addToCurrentData(new ArrayList<BaseEntity>() {{ add(data); }}, null);
    } catch(Exception ex) {
      log.error("Could not replace existing Data Collection with " + data, ex);
      callErrorOccurredListeners(new DeepThoughtError(Localization.getLocalizedStringForResourceKey("alert.message.message.could.not.replace.existing.data.collection"),
          ex, false, Localization.getLocalizedStringForResourceKey("alert.message.title.could.not.replace.existing.data.collection")));
    }
  }

  public void recreateEntityManagerAndRetrieveDeepThoughtApplication() {
    try {
      entityManager = Application.getDependencyResolver().createEntityManager(Application.getEntityManagerConfiguration());

      retrieveDeepThoughtApplication();
    } catch(Exception ex) {
      log.error("Could not recreate EntityManager", ex);
      // TODO: show a different error message than for replaceExistingDataCollectionWithData() ?
      callErrorOccurredListeners(new DeepThoughtError(Localization.getLocalizedStringForResourceKey("alert.message.message.could.not.replace.existing.data.collection"),
          ex, false, Localization.getLocalizedStringForResourceKey("alert.message.title.could.not.replace.existing.data.collection")));
    }
  }


  public DeepThoughtApplication getApplication() {
    return application;
  }

  public UserDeviceSettings getSettings() {
    return getLoggedOnUser().getSettings();
  }

  public User getLoggedOnUser() {
    return loggedOnUser;
  }

  public DeepThought getDeepThought() {
    return currentDeepThought;
  }

  public String getDataCollectionSavePath() {
    return entityManager.getDatabasePath();
  }

  @Override
  public String getDataFolderPath() {
    return dataFolder;
  }

  public IEntityManager getEntityManager() {
    return entityManager;
  }
}

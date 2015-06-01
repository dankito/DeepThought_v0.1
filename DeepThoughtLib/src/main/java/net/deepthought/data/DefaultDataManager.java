package net.deepthought.data;

import net.deepthought.Application;
import net.deepthought.data.backup.IBackupManager;
import net.deepthought.data.listener.ApplicationListener;
import net.deepthought.data.model.DeepThought;
import net.deepthought.data.model.DeepThoughtApplication;
import net.deepthought.data.model.Device;
import net.deepthought.data.model.Group;
import net.deepthought.data.model.User;
import net.deepthought.data.model.enums.ApplicationLanguage;
import net.deepthought.data.model.listener.EntityListener;
import net.deepthought.data.model.settings.UserDeviceSettings;
import net.deepthought.data.persistence.EntityManagerConfiguration;
import net.deepthought.data.persistence.IEntityManager;
import net.deepthought.data.persistence.db.AssociationEntity;
import net.deepthought.data.persistence.db.BaseEntity;
import net.deepthought.util.DeepThoughtError;
import net.deepthought.util.Localization;
import net.deepthought.util.file.FileUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
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
    setCurrentDeepThoughtApplication(application);
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

  protected void setCurrentDeepThoughtApplication(DeepThoughtApplication application) {
    if(this.application != null)
      this.application.removeEntityListener(entityListener);

    this.application = application;

    if(application != null)
      application.addEntityListener(entityListener);
  }

  protected void setCurrentDeepThought(DeepThought deepThought) {
    if(currentDeepThought != null)
      deepThought.removeEntityListener(entityListener);

    currentDeepThought = deepThought;

    if(currentDeepThought != null)
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

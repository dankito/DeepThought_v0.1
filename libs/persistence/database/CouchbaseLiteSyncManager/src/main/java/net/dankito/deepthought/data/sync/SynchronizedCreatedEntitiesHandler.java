package net.dankito.deepthought.data.sync;

import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.DocumentChange;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.data.listener.AllEntitiesListener;
import net.dankito.deepthought.data.listener.ApplicationListener;
import net.dankito.deepthought.data.model.Category;
import net.dankito.deepthought.data.model.DeepThought;
import net.dankito.deepthought.data.model.DeepThoughtApplication;
import net.dankito.deepthought.data.model.Device;
import net.dankito.deepthought.data.model.Entry;
import net.dankito.deepthought.data.model.FileLink;
import net.dankito.deepthought.data.model.Group;
import net.dankito.deepthought.data.model.Note;
import net.dankito.deepthought.data.model.Person;
import net.dankito.deepthought.data.model.Reference;
import net.dankito.deepthought.data.model.ReferenceSubDivision;
import net.dankito.deepthought.data.model.SeriesTitle;
import net.dankito.deepthought.data.model.Tag;
import net.dankito.deepthought.data.model.User;
import net.dankito.deepthought.data.model.enums.BackupFileServiceType;
import net.dankito.deepthought.data.model.enums.FileType;
import net.dankito.deepthought.data.model.enums.Language;
import net.dankito.deepthought.data.model.enums.NoteType;
import net.dankito.deepthought.data.persistence.CouchbaseLiteEntityManagerBase;
import net.dankito.deepthought.data.persistence.db.BaseEntity;
import net.dankito.deepthought.util.Notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Created by ganymed on 16/09/16.
 */
public class SynchronizedCreatedEntitiesHandler {

  private static final Logger log = LoggerFactory.getLogger(SynchronizedCreatedEntitiesHandler.class);


  protected CouchbaseLiteSyncManager syncManager;

  protected CouchbaseLiteEntityManagerBase entityManager;

  protected Database database;

  protected DeepThoughtApplication deepThoughtApplication;

  protected DeepThought deepThought = null;

  protected List<Class> synchronizedDeepThoughtApplicationEntities = new ArrayList<>();

  protected List<Class> synchronizedDeepThoughtEntities = new ArrayList<>();

  protected Set<String> synchronizedEntities = new ConcurrentSkipListSet<>();

  protected Map<Class, String> currentEntityIds = new ConcurrentHashMap<>();


  public SynchronizedCreatedEntitiesHandler(CouchbaseLiteSyncManager syncManager, CouchbaseLiteEntityManagerBase entityManager, Database database) {
    this.syncManager = syncManager;
    this.entityManager = entityManager;
    this.database = database;

    // TODO: pass as constructor parameter
    Application.getEntityChangesService().addAllEntitiesListener(allEntitiesListener);

    setupDeepThoughtApplication();
    setupDeepThought();
  }

  protected void setupDeepThoughtApplication() {
    deepThoughtApplication = Application.getApplication();

    synchronizedDeepThoughtApplicationEntities.addAll(Arrays.asList(User.class, Device.class, Group.class));

    for(Class entityClass : synchronizedDeepThoughtApplicationEntities) {
      cacheEntityIds(entityClass, getDeepThoughtApplicationDocument());
    }
  }

  protected void setupDeepThought() {
    synchronizedDeepThoughtEntities.addAll(Arrays.asList(Entry.class, Tag.class, Category.class, SeriesTitle.class, Reference.class, ReferenceSubDivision.class,
        Person.class, FileLink.class, Note.class, Language.class, NoteType.class, FileType.class, BackupFileServiceType.class));

    deepThoughtChanged(Application.getDeepThought());

    Application.addApplicationListener(new ApplicationListener() {
      @Override
      public void deepThoughtChanged(DeepThought deepThought) {
        deepThoughtChanged(deepThought);
      }

      @Override
      public void notification(Notification notification) {

      }
    });
  }

  public boolean handleNewlyCreatedEntities(DocumentChange change, Class entityType) {
    String entityId = change.getDocumentId();
    if(entityId == null || entityType == null || deepThought == null) {
      return false; // should actually never be the case
    }

    if(synchronizedEntities.contains(entityId)) {
      return false;
    }

    synchronizedEntities.add(entityId);

    try {
      String entityIds = currentEntityIds.get(entityType);
      if(entityIds != null && entityIds.contains(entityId) == false) { // check if Entity has been stored but just not loaded yet from DB
        BaseEntity entity = entityManager.getEntityById(entityType, entityId);

        if(addEntityToDeepThought(entity, entityType, entityId)) {
          log.info("Added synchronized Entity to DeepThought: " + entity);
          return true;
        }
      }
    } catch(Exception e) {
      log.error("Could not handle changes as newly created entity", e);
    }

    return false;
  }

  protected boolean addEntityToDeepThought(BaseEntity entity, Class entityType, String entityId) {
    if(Entry.class.equals(entityType)) {
      if(entityId.equals(deepThought.getTopLevelEntry().getId()) == false) {
        // TODO: adjust entryIndex
        // TODO: also respect hierarchy
        log.info("Entry with " + ((Entry)entity).getCountTags() + " Tags");
        return deepThought.addEntry((Entry)entity);
      }
    }
    else if(Category.class.equals(entityType)) {
      if(entityId.equals(deepThought.getTopLevelCategory().getId()) == false) {
        // TODO: adjust categoryOrder
        // TODO: also respect hierarchy
        return deepThought.addCategory((Category)entity);
      }
    }
    else if(Tag.class.equals(entityType)) {
      log.info("Tag with " + ((Tag)entity).getEntries().size() + " Entries");
      return deepThought.addTag((Tag)entity);
    }
    else if(SeriesTitle.class.equals(entityType)) {
      return deepThought.addSeriesTitle((SeriesTitle)entity);
    }
    else if(Reference.class.equals(entityType)) {
      return deepThought.addReference((Reference)entity);
    }
    else if(ReferenceSubDivision.class.equals(entityType)) {
      return deepThought.addReferenceSubDivision((ReferenceSubDivision)entity);
    }
    else if(Person.class.equals(entityType)) {
      return deepThought.addPerson((Person)entity);
    }
    else if(FileLink.class.equals(entityType)) {
      return deepThought.addFile((FileLink)entity);
    }
    else if(Note.class.equals(entityType)) {
      return deepThought.addNote((Note)entity);
    }

    else if(Language.class.equals(entityType)) {
      return deepThought.addLanguage((Language)entity);
    }
    else if(FileType.class.equals(entityType)) {
      return deepThought.addFileType((FileType)entity);
    }
    else if(NoteType.class.equals(entityType)) {
      return deepThought.addNoteType((NoteType)entity);
    }
    else if(BackupFileServiceType.class.equals(entityType)) {
      return deepThought.addBackupFileServiceType((BackupFileServiceType)entity);
    }

    else if(User.class.equals(entityType)) {
      return deepThoughtApplication.addUser((User)entity);
    }
    else if(Device.class.equals(entityType)) {
      return deepThoughtApplication.addDevice((Device)entity);
    }
    else if(Group.class.equals(entityType)) {
      return deepThoughtApplication.addGroup((Group) entity);
    }

    return false;
  }

  protected void deepThoughtChanged(DeepThought deepThought) {
    synchronizedEntities.clear();

    this.deepThought = deepThought;

    if(deepThought != null) {
      Document deepThoughtDocument = database.getDocument(deepThought.getId());


      for(Class entityClass : synchronizedDeepThoughtEntities) {
        cacheEntityIds(entityClass, deepThoughtDocument);
      }
    }
  }

  protected void cacheEntityIds(Class entityClass, Document document) {
    currentEntityIds.put(entityClass, getIdsOfProperty(document, getPropertyNameForEntity(entityClass)));
  }

  protected Document getDeepThoughtApplicationDocument() {
    return database.getDocument(deepThoughtApplication.getId());
  }

  protected Document getDeepThoughtDocument() {
    return database.getDocument(deepThought.getId());
  }

  protected String getIdsOfDeepThoughtApplicationProperty(String propertyName) {
    return getIdsOfProperty(getDeepThoughtApplicationDocument(), propertyName);
  }

  protected String getIdsOfDeepThoughtProperty(String propertyName) {
    return getIdsOfProperty(getDeepThoughtDocument(), propertyName);
  }

  protected String getIdsOfProperty(BaseEntity collectionHolder, String propertyName) {
    if(collectionHolder instanceof DeepThoughtApplication) {
      return getIdsOfDeepThoughtApplicationProperty(propertyName);
    }

    return getIdsOfDeepThoughtProperty(propertyName);
  }

  protected String getIdsOfProperty(Document document, String propertyName) {
    return (String)document.getProperty(propertyName);
  }


  public boolean entityDeleted(BaseEntity entity, Class entityType) {
    if(Entry.class.equals(entityType)) {
      return deepThought.addEntry((Entry)entity);
    }
    else if(Category.class.equals(entityType)) {
      return deepThought.addCategory((Category)entity);
    }
    else if(Tag.class.equals(entityType)) {
      return deepThought.addTag((Tag)entity);
    }
    else if(SeriesTitle.class.equals(entityType)) {
      return deepThought.addSeriesTitle((SeriesTitle)entity);
    }
    else if(Reference.class.equals(entityType)) {
      return deepThought.addReference((Reference)entity);
    }
    else if(ReferenceSubDivision.class.equals(entityType)) {
      return deepThought.addReferenceSubDivision((ReferenceSubDivision)entity);
    }
    else if(Person.class.equals(entityType)) {
      return deepThought.addPerson((Person)entity);
    }
    else if(FileLink.class.equals(entityType)) {
      return deepThought.addFile((FileLink)entity);
    }
    else if(Note.class.equals(entityType)) {
      return deepThought.addNote((Note)entity);
    }

    else if(Language.class.equals(entityType)) {
      return deepThought.addLanguage((Language)entity);
    }
    else if(FileType.class.equals(entityType)) {
      return deepThought.addFileType((FileType)entity);
    }
    else if(NoteType.class.equals(entityType)) {
      return deepThought.addNoteType((NoteType)entity);
    }
    else if(BackupFileServiceType.class.equals(entityType)) {
      return deepThought.addBackupFileServiceType((BackupFileServiceType)entity);
    }

    else if(User.class.equals(entityType)) {
      return deepThoughtApplication.addUser((User)entity);
    }
    else if(Device.class.equals(entityType)) {
      return deepThoughtApplication.addDevice((Device)entity);
    }
    else if(Group.class.equals(entityType)) {
      return deepThoughtApplication.addGroup((Group) entity);
    }
    else {
      syncManager.callEntityDeletedListeners(entity);
      return false;
    }
  }


  protected AllEntitiesListener allEntitiesListener = new AllEntitiesListener() {
    @Override
    public void entityCreated(BaseEntity entity) {

    }

    @Override
    public void entityUpdated(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {

    }

    @Override
    public void entityDeleted(BaseEntity entity) {

    }

    @Override
    public void entityAddedToCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity) {
      entityAddedOrRemovedFromCollection(collectionHolder, collection, addedEntity, true);
    }

    @Override
    public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {
      entityAddedOrRemovedFromCollection(collectionHolder, collection, removedEntity, false);
    }
  };

  protected void entityAddedOrRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedOrRemovedEntity, boolean added) {
    if(collectionHolder instanceof DeepThoughtApplication || collectionHolder instanceof DeepThought) {
      handleEntityCreatedOrRemoved(collectionHolder, collection, addedOrRemovedEntity, added);
    }
  }

  protected void handleEntityCreatedOrRemoved(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedOrRemovedEntity, boolean added) {
    updateEntityIds(collectionHolder, collection, addedOrRemovedEntity);

    if(added) {
      synchronizedEntities.add(addedOrRemovedEntity.getId());
    }
  }

  protected void updateEntityIds(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity entity) {
    // TODO: a shorter way would be (if it's a EntitiesCollection): ((EntitiesCollection)collection).getTargetEntitiesIds()  and create a JsonArray String from it
    try {
      Class entityType = entity.getClass();
      String propertyName = getPropertyNameForEntity(entityType);

      currentEntityIds.put(entityType, getIdsOfProperty(collectionHolder, propertyName));
    } catch(Exception e) { log.error("Could not update Entities of " + entity); }
  }

  protected String getPropertyNameForEntity(Class entityType) {
    if(Entry.class.equals(entityType)) {
      return "entries";
    }
    else if(Category.class.equals(entityType)) {
      return "categories";
    }
    else if(Tag.class.equals(entityType)) {
      return "tags";
    }
    else if(SeriesTitle.class.equals(entityType)) {
      return "seriesTitles";
    }
    else if(Reference.class.equals(entityType)) {
      return "references";
    }
    else if(ReferenceSubDivision.class.equals(entityType)) {
      return "referenceSubDivisions";
    }
    else if(Person.class.equals(entityType)) {
      return "persons";
    }
    else if(FileLink.class.equals(entityType)) {
      return "files";
    }
    else if(Note.class.equals(entityType)) {
      return "notes";
    }

    else if(Language.class.equals(entityType)) {
      return "languages";
    }
    else if(FileType.class.equals(entityType)) {
      return "fileTypes";
    }
    else if(NoteType.class.equals(entityType)) {
      return "noteTypes";
    }
    else if(BackupFileServiceType.class.equals(entityType)) {
      return "backupFileServiceTypes";
    }

    else if(User.class.equals(entityType)) {
      return "users";
    }
    else if(Device.class.equals(entityType)) {
      return "devices";
    }
    else if(Group.class.equals(entityType)) {
      return "groups";
    }

    return null;
  }

}

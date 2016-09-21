package net.dankito.deepthought.data.sync;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.DocumentChange;
import com.couchbase.lite.SavedRevision;
import com.couchbase.lite.TransactionalTask;
import com.couchbase.lite.UnsavedRevision;

import net.dankito.deepthought.data.persistence.CouchbaseLiteEntityManagerBase;
import net.dankito.deepthought.data.persistence.db.BaseEntity;
import net.dankito.deepthought.data.persistence.db.TableConfig;
import net.dankito.jpa.annotationreader.config.EntityConfig;
import net.dankito.jpa.annotationreader.config.PropertyConfig;
import net.dankito.jpa.annotationreader.config.inheritance.DiscriminatorColumnConfig;
import net.dankito.jpa.couchbaselite.Dao;
import net.dankito.jpa.relationship.collections.EntitiesCollection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by ganymed on 14/09/16.
 */
public class SynchronizedDataMerger {

  private static final Logger log = LoggerFactory.getLogger(SynchronizedDataMerger.class);


  protected CouchbaseLiteSyncManager syncManager;

  protected CouchbaseLiteEntityManagerBase entityManager;

  protected Database database;

  protected SynchronizedCreatedEntitiesHandler createdEntitiesHandler;


  public SynchronizedDataMerger(CouchbaseLiteSyncManager syncManager, CouchbaseLiteEntityManagerBase entityManager, Database database) {
    this.syncManager = syncManager;
    this.entityManager = entityManager;
    this.database = database;

    createdEntitiesHandler = new SynchronizedCreatedEntitiesHandler(entityManager, database);
  }


  public BaseEntity synchronizedChange(DocumentChange change) {
      log.info("isCurrentRevision() = " + change.isCurrentRevision());
//      if(change.isCurrentRevision()) {
    String entityTypeString = (String)change.getAddedRevision().getPropertyForKey(Dao.TYPE_COLUMN_NAME);

    Class entityType = null;
    try { entityType = (Class<BaseEntity>)Class.forName(entityTypeString); } catch(Exception e) { log.error("Could not get class for entity type " + entityTypeString); }

      if (change.isConflict()) {
        handleConflict(change, entityType);
      }

    createdEntitiesHandler.handleNewlyCreatedEntities(change, entityType);


      return updateCachedSynchronizedEntity(change, entityType);
  }


  protected void handleConflict(DocumentChange change, Class entityType) {
    if(entityType != null) {
      try {
        Document storedDocument = database.getExistingDocument(change.getDocumentId());
        final List<SavedRevision> conflicts = storedDocument.getConflictingRevisions();

        if(conflicts.size() > 1) {
          log.info("Handling Conflict for " + entityType + " of Revision " + change.getRevisionId());

          Dao dao = entityManager.getDaoForClass(entityType);
          final String currentRevisionId = storedDocument.getCurrentRevisionId();

          final Map<String, Object> updatedProperties = new HashMap<>();
          updatedProperties.putAll(storedDocument.getCurrentRevision().getProperties());

          Map<String, Object> mergedProperties = mergeProperties(dao, conflicts);
          updatedProperties.putAll(mergedProperties);

          updateWinningRevisionDeleteConflictedOnes(conflicts, updatedProperties, currentRevisionId);
        }
      } catch (Exception e) {
        log.error("Could not handle conflict for Document Id " + change.getDocumentId() + " of Entity " + entityType, e);
      }
    }
  }

  protected void updateWinningRevisionDeleteConflictedOnes(final List<SavedRevision> conflicts, final Map<String, Object> updatedProperties, final String currentRevisionId) {
    // a modified version of http://labs.couchbase.com/couchbase-mobile-portal/develop/guides/couchbase-lite/native-api/document/index.html#Understanding%20Conflicts
    database.runInTransaction(new TransactionalTask() {
      @Override
      public boolean run() {
        boolean success = true;

        // Delete the conflicting revisions to get rid of the conflict:
        for (SavedRevision rev : conflicts) {
          success &= SynchronizedDataMerger.this.updateWinningRevisionDeleteConflictedOnes(rev, updatedProperties, currentRevisionId);
        }

        return success;
      }
    });
  }

  protected boolean updateWinningRevisionDeleteConflictedOnes(SavedRevision revision, Map<String, Object> updatedProperties, String currentRevisionId) {
    try {
      UnsavedRevision newRevision = revision.createRevision();

      if (revision.getId().equals(currentRevisionId)) {
        newRevision.setProperties(updatedProperties);
      }
      else {
        newRevision.setIsDeletion(true);
      }

      // saveAllowingConflict allows 'revision' to be updated even if it
      // is not the document's current revision.
      newRevision.save(true);

      return true;
    }
    catch (CouchbaseLiteException e) {
      log.error("Could not resolve conflict", e);
      return false;
    }
  }

  protected Map<String, Object> mergeProperties(Dao dao, List<SavedRevision> conflicts) {
    Map<String, Object> mergedProperties = new HashMap<>();
    EntityConfig entityConfig = dao.getEntityConfig();

    SavedRevision newerRevision = conflicts.get(0);
    SavedRevision olderRevision = conflicts.get(1);
    // get which one is newer
    if((Long)newerRevision.getProperty(TableConfig.BaseEntityModifiedOnColumnName) < (Long)olderRevision.getProperty(TableConfig.BaseEntityModifiedOnColumnName)) {
      newerRevision = olderRevision;
      olderRevision = conflicts.get(0);
    }

    SavedRevision commonParent = findCommonParent(newerRevision, olderRevision);
    Map<String, Object> commonParentProperties = new HashMap<>();
    if(commonParent != null) {
      commonParentProperties.putAll(commonParent.getProperties());
    }

    for(PropertyConfig property : entityConfig.getPropertiesIncludingInheritedOnes()) {
      try {
        mergeProperty(dao, property, newerRevision, olderRevision, commonParent, mergedProperties);
      } catch(Exception e) {
        log.error("Could not merge Property " + property + " on conflicted Entity of Type " + entityConfig.getEntityClass(), e);
      }
    }

    return mergedProperties;
  }

  protected SavedRevision findCommonParent(SavedRevision revision01, SavedRevision revision02) {
    try {
      List<SavedRevision> history01 = revision01.getRevisionHistory();
      List<SavedRevision> history02 = revision02.getRevisionHistory();

      for(int i = history01.size() - 1; i >= 0; i--) {
        SavedRevision parentRevision = history01.get(i);

        if(history02.contains(parentRevision)) {
          return parentRevision;
        }
      }
    } catch(Exception e) {
      log.error("Could not get common parent", e);
    }
    return null;
  }

  protected void mergeProperty(Dao dao, PropertyConfig property, SavedRevision newerRevision, SavedRevision olderRevision, SavedRevision commonParent, Map<String, Object> mergedProperties) throws SQLException {
    String propertyName = property.getColumnName();

    if(isCouchbaseLiteSystemProperty(propertyName) == false) {
      Object newerValue = newerRevision.getProperty(propertyName);
      Object olderValue = olderRevision.getProperty(propertyName);

      if(property.isCollectionProperty() == false) {
        if ((newerValue != null && newerValue.equals(olderValue) == false) ||
            (newerValue == null && olderValue != null && newerRevision.getProperties().containsKey(propertyName))) { // only put a null value to mergedProperties if by this a previous value got deleted
          mergedProperties.put(propertyName, newerValue);
        } else if (olderValue != null && newerValue == null) {
          mergedProperties.put(propertyName, olderValue);
        }
      }
      else {
        mergeCollectionProperty(property, newerValue, olderValue, commonParent, mergedProperties);
      }
    }
  }

  protected void mergeCollectionProperty(PropertyConfig property, Object newerValue, Object olderValue, SavedRevision commonParent, Map<String, Object> mergedProperties) throws SQLException {
    Dao targetEntityDao = entityManager.getDaoForClass(property.getTargetEntityClass());

    Collection<Object> newerValueTargetEntityIds = targetEntityDao.parseJoinedEntityIdsFromJsonString((String)newerValue);
    Collection<Object> olderValueTargetEntityIds = targetEntityDao.parseJoinedEntityIdsFromJsonString((String)olderValue);

    Collection<Object> commonParentTargetEntityIds = new ArrayList<>(0);
    if(commonParent != null) {
      commonParentTargetEntityIds = targetEntityDao.parseJoinedEntityIdsFromJsonString((String) commonParent.getProperty(property.getColumnName()));
    }

    Set<Object> mergedEntityIds = new HashSet<>();
    mergedEntityIds.addAll(newerValueTargetEntityIds);
    mergedEntityIds.addAll(olderValueTargetEntityIds);

    for(Object targetEntityId : commonParentTargetEntityIds) {
      if(newerValueTargetEntityIds.contains(targetEntityId) == false || olderValueTargetEntityIds.contains(targetEntityId) == false) { // Entity has been deleted
//        mergedEntityIds.remove(targetEntityId);
      }
    }

    String mergedEntityIdsString = targetEntityDao.getPersistableCollectionTargetEntities(mergedEntityIds);
    mergedProperties.put(property.getColumnName(), mergedEntityIdsString);
  }

  protected BaseEntity updateCachedSynchronizedEntity(DocumentChange change, Class entityType) {
    BaseEntity cachedEntity = null;

    if(entityType != null) { // sometimes only some Couchbase internal data is synchronized without any user data -> skip these
      try {
        cachedEntity = (BaseEntity) entityManager.getObjectCache().get(entityType, change.getDocumentId());
        if(cachedEntity != null) { // cachedEntity == null: Entity not retrieved / cached yet -> will be read from DB on next access anyway, therefore no need to update it
          log.info("Updating cached synchronized Entity of Revision " + change.getRevisionId() + ": " + cachedEntity);

          Document storedDocument = database.getExistingDocument(change.getDocumentId());
          Dao dao = entityManager.getDaoForClass(entityType);

          List<SavedRevision> revisionHistory = storedDocument.getRevisionHistory();
          SavedRevision currentRevision = storedDocument.getCurrentRevision();

          if(getVersionFromRevision(currentRevision).equals(1L)) { // TODO: how should it come to here if we call return on non-cached instances?
            log.warn("Did it really ever come to here?");
            newEntityCreated(entityType, change);
          }
          else {
            updateCachedEntity(cachedEntity, dao, currentRevision);
          }
        }
      } catch (Exception e) {
        log.error("Could not handle Change", e);
      }
    }

    return cachedEntity;
  }

  protected void updateCachedEntity(BaseEntity cachedEntity, Dao dao, SavedRevision currentRevision) throws SQLException {
    EntityConfig entityConfig = dao.getEntityConfig();
    Map<String, Object> detectedChanges = getChanges(cachedEntity, dao, entityConfig, currentRevision);

    if (detectedChanges.size() > 0) {
      for(String propertyName : detectedChanges.keySet()) {
        try {
          updateProperty(cachedEntity, propertyName, dao, entityConfig, currentRevision, detectedChanges);
        } catch(Exception e) {
          log.error("Could not update Property " + propertyName + " on synchronized Object " + cachedEntity, e);
        }
      }
    }
  }

  protected void updateProperty(BaseEntity cachedEntity, String propertyName, Dao dao, EntityConfig entityConfig, SavedRevision currentRevision, Map<String, Object> detectedChanges) throws SQLException {
    PropertyConfig property = entityConfig.getPropertyByColumnName(propertyName);
    Object previousValue = dao.extractValueFromObject(cachedEntity, property);

    if(property.isCollectionProperty() == false) {
      Object updatedValue = dao.deserializePersistedValue(property, currentRevision.getProperty(propertyName));
      dao.setValueOnObject(cachedEntity, property, updatedValue);

      syncManager.callEntityUpdatedListeners(cachedEntity, propertyName, previousValue, updatedValue);
    }
    else {
      updateCollectionProperty(cachedEntity, property, propertyName, currentRevision, detectedChanges, previousValue);
    }
  }

  protected void updateCollectionProperty(BaseEntity cachedEntity, PropertyConfig property, String propertyName, SavedRevision currentRevision, Map<String, Object> detectedChanges,
                                          Object previousValue) throws SQLException {
    String previousTargetEntityIdsString = (String)detectedChanges.get(propertyName);
    String currentTargetEntityIdsString = (String)currentRevision.getProperty(propertyName);
    if(currentRevision.getProperties().containsKey(propertyName) == false) { // currentRevision has no information about this property
      currentTargetEntityIdsString = "[]"; // TODO: what to do here? Assuming "[]" is for sure false. Removing all items?
    }

    Dao targetDao = entityManager.getDaoForClass(property.getTargetEntityClass());
    Collection<Object> currentTargetEntityIds = targetDao.parseJoinedEntityIdsFromJsonString(currentTargetEntityIdsString);
    Collection previousTargetEntityIds = targetDao.parseJoinedEntityIdsFromJsonString(previousTargetEntityIdsString);

    Collection previousValueCollection = (Collection)previousValue;

    log.info("Collection Property " + property + " of Revision " + currentRevision.getId() + " has now Ids of " + currentTargetEntityIdsString + ". Previous ones: " + previousTargetEntityIdsString);

    if(previousValue instanceof EntitiesCollection) { // TODO: what to do if it's not an EntitiesCollection yet?
      ((EntitiesCollection)previousValue).refresh(currentTargetEntityIds);
    }
    else {
      log.warn("Not an EntitiesCollection: " + previousValue);
    }

    String parentRevisionTargetEntityIdsString = null;
    SavedRevision parentRevision = currentRevision.getParent();
    if(parentRevision != null) {
      try { parentRevisionTargetEntityIdsString = (String) parentRevision.getProperty(propertyName); }
      catch(Exception ignored) { } // not always can parent revision's property be loaded, that's quite natural
    }

    Collection<BaseEntity> addedEntities = getEntitiesAddedToCollection(property, currentTargetEntityIdsString, previousTargetEntityIdsString);
    for(BaseEntity addedEntity : addedEntities) {
      syncManager.callEntityAddedToCollectionListeners(cachedEntity, previousValueCollection, addedEntity);
    }

    Collection<BaseEntity> removedEntities = getEntitiesRemovedFromCollection(property, currentTargetEntityIdsString, previousTargetEntityIdsString);
    for(BaseEntity removedEntity : removedEntities) {
      if(parentRevisionTargetEntityIdsString != null && parentRevisionTargetEntityIdsString.contains(removedEntity.getId())) {
        syncManager.callEntityRemovedFromCollectionListeners(cachedEntity, previousValueCollection, removedEntity);
      }
      else {
        syncManager.callEntityAddedToCollectionListeners(cachedEntity, previousValueCollection, removedEntity);
      }
    }
  }

  protected Collection<BaseEntity> getEntitiesAddedToCollection(PropertyConfig property, String currentTargetEntityIdsString, String previousTargetEntityIdsString) {
    Set<BaseEntity> addedEntities = new HashSet<>();

    try {
      Dao targetDao = entityManager.getDaoForClass(property.getTargetEntityClass());

      Collection currentTargetEntityIds = targetDao.parseJoinedEntityIdsFromJsonString(currentTargetEntityIdsString);
      Collection previousTargetEntityIds = targetDao.parseJoinedEntityIdsFromJsonString(previousTargetEntityIdsString);

      for(Object currentId : currentTargetEntityIds) {
        if(previousTargetEntityIds.contains(currentId) == false) {
          addedEntities.add((BaseEntity)targetDao.retrieve(currentId));
        }
      }
    } catch(Exception e) {
      log.error("Could not get Collection Items of property " + property + " from currentTargetEntityIdsString = " + currentTargetEntityIdsString +
          " and previousTargetEntityIdsString = " + previousTargetEntityIdsString, e);
    }

    return addedEntities;
  }

  protected Collection<BaseEntity> getEntitiesRemovedFromCollection(PropertyConfig property, String currentTargetEntityIdsString, String previousTargetEntityIdsString) {
    Set<BaseEntity> removedEntities = new HashSet<>();

    try {
      Dao targetDao = entityManager.getDaoForClass(property.getTargetEntityClass());

      Collection currentTargetEntityIds = targetDao.parseJoinedEntityIdsFromJsonString(currentTargetEntityIdsString);
      Collection previousTargetEntityIds = targetDao.parseJoinedEntityIdsFromJsonString(previousTargetEntityIdsString);

      for(Object previousId : previousTargetEntityIds) {
        if(currentTargetEntityIds.contains(previousId) == false) {
          removedEntities.add((BaseEntity)targetDao.retrieve(previousId));
        }
      }
    } catch(Exception e) {
      log.error("Could not get Entities removed from Collection from currentTargetEntityIdsString = " + currentTargetEntityIdsString +
          " and previousTargetEntityIdsString = " + previousTargetEntityIdsString, e);
    }

    return removedEntities;
  }

  protected Map<String, Object> getChanges(BaseEntity cachedEntity, Dao dao, EntityConfig entityConfig, SavedRevision currentRevision) {
    Map<String, Object> detectedChanges = new HashMap<>();
    Map<String, Object> currentRevisionProperties = currentRevision.getProperties();

    for(PropertyConfig propertyConfig : entityConfig.getPropertiesIncludingInheritedOnes()) {
      if(propertyConfig.isId() || propertyConfig.isVersion() || propertyConfig instanceof DiscriminatorColumnConfig ||
          TableConfig.BaseEntityModifiedOnColumnName.equals(propertyConfig.getColumnName())) {
        continue;
      }

      try {
        Object currentRevisionValue = currentRevisionProperties.get(propertyConfig.getColumnName());
        Object cachedEntityValue = dao.getPersistablePropertyValue(cachedEntity, propertyConfig);

        if(propertyConfig.isCollectionProperty() == false) {
          if ((cachedEntityValue == null && currentRevisionValue != null) || (cachedEntityValue != null && currentRevisionValue == null) ||
              (cachedEntityValue != null && cachedEntityValue.equals(currentRevisionValue) == false)) {
            detectedChanges.put(propertyConfig.getColumnName(), cachedEntityValue);
          }
        }
        else {
          if (hasCollectionPropertyChanged(dao, currentRevisionValue, cachedEntityValue)) {
            detectedChanges.put(propertyConfig.getColumnName(), cachedEntityValue);
          }
        }
      } catch(Exception e) {
        log.error("Could not check Property " + propertyConfig + " for changes", e);
      }
    }

    return detectedChanges;
  }

  protected boolean hasCollectionPropertyChanged(Dao dao, Object currentRevisionValue, Object cachedEntityValue) throws SQLException {
    if(currentRevisionValue == null || cachedEntityValue == null) {
      return currentRevisionValue != cachedEntityValue; // if only one of them is null, than there's a change
    }

    Collection<Object> currentRevisionTargetEntityIds = dao.parseJoinedEntityIdsFromJsonString((String)currentRevisionValue);
    Collection<Object> cachedEntityTargetEntityIds = dao.parseJoinedEntityIdsFromJsonString((String)cachedEntityValue);

    if(currentRevisionTargetEntityIds.size() != cachedEntityTargetEntityIds.size()) {
      return true;
    }

    for(Object targetEntityId : currentRevisionTargetEntityIds) {
      if(cachedEntityTargetEntityIds.contains(targetEntityId) == false) {
        return true;
      }
    }

    return false; // cachedEntityTargetEntityIds contains all targetEntityIds of currentRevisionTargetEntityIds
  }

  protected Map<String, Object> getChanges(DocumentChange change, SavedRevision previousRevision) {
    Map<String, Object> detectedChanges = new HashMap<>();

    for (Map.Entry<String, Object> property : change.getAddedRevision().getProperties().entrySet()) {
      if(isCouchbaseLiteSystemProperty(property.getKey()) == false) {
        Object newValue = property.getValue();
        Object previousValue = previousRevision.getProperties().get(property.getKey());

        if ((newValue == null && previousValue != null) || (newValue != null && previousValue == null) ||
            (newValue != null && newValue.equals(previousValue) == false)) {
          detectedChanges.put(property.getKey(), property.getValue());
        }
      }
    }

    return detectedChanges;
  }

  protected boolean isCouchbaseLiteSystemProperty(String propertyName) {
    return "_id".equals(propertyName) || "_rev".equals(propertyName) || "_revisions".equals(propertyName) ||
        "attachments".equals(propertyName) || "_deleted".equals(propertyName);
  }

  protected void newEntityCreated(Class<BaseEntity> entityClass, DocumentChange change) {
    try {
      BaseEntity entity = entityManager.<BaseEntity>getEntityById(entityClass, change.getDocumentId());

      Dao dao = entityManager.getDaoForClass(entityClass);
      EntityConfig entityConfig = dao.getEntityConfig();
      PropertyConfig deepThoughtProperty = entityConfig.getPropertyByColumnName("deep_thought_id");
      if(deepThoughtProperty != null) {
        String targetFieldName = deepThoughtProperty.getTargetPropertyConfig().getFieldName();
//        Application.getDeepThought().callEntityAddedListeners();
      }
    } catch(Exception e) { }
  }

  protected Long getVersionFromRevision(SavedRevision revision) {
    return getVersionFromRevisionId(revision.getId());
  }

  protected Long getVersionFromRevisionId(String revisionId) {
    String versionString = revisionId.substring(0, revisionId.indexOf('-'));
    return Long.parseLong(versionString);
  }

}

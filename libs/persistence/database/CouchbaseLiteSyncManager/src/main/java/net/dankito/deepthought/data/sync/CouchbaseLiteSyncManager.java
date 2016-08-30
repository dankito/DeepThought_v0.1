package net.dankito.deepthought.data.sync;

import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.DocumentChange;
import com.couchbase.lite.Manager;
import com.couchbase.lite.SavedRevision;
import com.couchbase.lite.listener.Credentials;
import com.couchbase.lite.listener.LiteListener;
import com.couchbase.lite.replicator.Replication;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.communication.Constants;
import net.dankito.deepthought.communication.IDeepThoughtConnector;
import net.dankito.deepthought.communication.model.ConnectedDevice;
import net.dankito.deepthought.data.persistence.CouchbaseLiteEntityManagerBase;
import net.dankito.deepthought.data.persistence.db.BaseEntity;
import net.dankito.jpa.annotationreader.config.EntityConfig;
import net.dankito.jpa.annotationreader.config.PropertyConfig;
import net.dankito.jpa.annotationreader.config.inheritance.DiscriminatorColumnConfig;
import net.dankito.jpa.couchbaselite.Dao;
import net.dankito.jpa.relationship.collections.EntitiesCollection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class CouchbaseLiteSyncManager extends SyncManagerBase {

  private static final Logger log = LoggerFactory.getLogger(CouchbaseLiteSyncManager.class);


  protected CouchbaseLiteEntityManagerBase entityManager;

  protected Database database;

  protected Manager manager;

  protected boolean alsoUsePullReplication;

  protected int synchronizationPort;
  protected Credentials allowedCredentials;

  protected Thread listenerThread;

  protected LiteListener couchbaseLiteListener;

  protected Map<String, Replication> pushReplications = new ConcurrentHashMap<>();
  protected Map<String, Replication> pullReplications = new ConcurrentHashMap<>();


  public CouchbaseLiteSyncManager(CouchbaseLiteEntityManagerBase entityManager, IDeepThoughtConnector deepThoughtConnector) {
    this(entityManager, deepThoughtConnector, true);
  }

  public CouchbaseLiteSyncManager(CouchbaseLiteEntityManagerBase entityManager, IDeepThoughtConnector deepThoughtConnector, boolean alsoUsePullReplication) {
    super(deepThoughtConnector);
    this.entityManager = entityManager;
    this.database = entityManager.getDatabase();
    this.manager = database.getManager();
    this.alsoUsePullReplication = alsoUsePullReplication;
  }



  protected void startCBLListener(ConnectedDevice device, int listenPort, Manager manager, Credentials allowedCredentials) throws Exception {
    couchbaseLiteListener = new LiteListener(manager, listenPort, allowedCredentials);
    synchronizationPort = couchbaseLiteListener.getListenPort();

    listenerThread = new Thread(couchbaseLiteListener);
    listenerThread.start();

    startSynchronizationWithDevice(device);
  }

  protected void stopCBLListener() {
    if(listenerThread != null) {
      try { listenerThread.join(500); } catch(Exception ignored) { }

      listenerThread = null;
    }

    if(couchbaseLiteListener != null) {
      couchbaseLiteListener.stop();
      couchbaseLiteListener = null;
    }
  }


  @Override
  protected void startSynchronizationWithDevice(ConnectedDevice device) throws Exception {
    synchronized(this) {
      if (isListenerStarted() == false) { // first device has connected -> start Listener first
        startCBLListener(device, Constants.SynchronizationDefaultPort, manager, allowedCredentials);
      }
      else {
        startReplication(device);
      }
    }
  }

  protected void startReplication(ConnectedDevice device) throws Exception {
    URL syncUrl;
    try {
      int remoteDeviceSyncPort = Constants.SynchronizationDefaultPort; // TODO: get remote device's real sync port
      syncUrl = new URL("http://" + device.getAddress() + ":" + remoteDeviceSyncPort + "/" + database.getName());
    } catch (MalformedURLException e) {
      throw new Exception(e);
    }

    Replication pushReplication = database.createPushReplication(syncUrl);
    pushReplication.addChangeListener(replicationChangeListener);
    pushReplication.setContinuous(true);

    pushReplications.put(device.getDeviceId(), pushReplication);

    pushReplication.start();


    if (alsoUsePullReplication) {
      Replication pullReplication = database.createPullReplication(syncUrl);
      pullReplication.addChangeListener(replicationChangeListener);
      pullReplication.setContinuous(true);

      pullReplications.put(device.getDeviceId(), pullReplication);

      pullReplication.start();
    }

    database.addChangeListener(databaseChangeListener);
  }

  @Override
  protected void stopSynchronizationWithDevice(ConnectedDevice device) {
    synchronized(this) {
      Replication pullReplication = pullReplications.get(device.getDeviceId());
      if (pullReplication != null) {
        pullReplication.stop();
      }

      Replication pushReplication = pullReplications.get(device.getDeviceId());
      if (pushReplications != null) {
        pushReplication.stop();
      }

      if (pushReplications.size() == 0) { // no devices connected anymore
        stopCBLListener();
      }
    }
  }


  public boolean isListenerStarted() {
    return couchbaseLiteListener != null;
  }


  protected Replication.ChangeListener replicationChangeListener = new Replication.ChangeListener() {
    @Override
    public void changed(Replication.ChangeEvent event) {

    }
  };

  protected Database.ChangeListener databaseChangeListener = new Database.ChangeListener() {
    @Override
    public void changed(final Database.ChangeEvent event) {
      if(event.isExternal()) {
        Application.getThreadPool().runTaskAsync(new Runnable() {
          @Override
          public void run() {
            handleSynchronizedChanges(event.getChanges());
          }
        });
      }
    }
  };

  protected void handleSynchronizedChanges(List<DocumentChange> changes) {
    for(DocumentChange change : changes) {
      if(change.isCurrentRevision()) {
        if (change.isConflict()) {
          handleConflict(change);
        }

        notifyListenersOfChanges(change);
      }
    }
  }

  protected void handleConflict(DocumentChange change) {
    // TODO:
  }

  protected void notifyListenersOfChanges(DocumentChange change) {
    try {
      Class<BaseEntity> entityClass = (Class<BaseEntity>)Class.forName((String)change.getAddedRevision().getPropertyForKey(Dao.TYPE_COLUMN_NAME));
      BaseEntity cachedEntity = (BaseEntity) entityManager.getObjectCache().get(entityClass, change.getDocumentId());
      if(cachedEntity == null) { // Entity not retrieved / cached yet -> will be read from DB on next access anyway, therefore no need to update it
        return;
      }

      Document storedDocument = database.getExistingDocument(change.getDocumentId());
      Dao dao = entityManager.getDaoForClass(entityClass);

      List<SavedRevision> revisionHistory = storedDocument.getRevisionHistory();
      SavedRevision currentRevision = storedDocument.getCurrentRevision();

      if(getVersionFromRevision(currentRevision).equals(1L)) { // TODO: how should it come to here if we call return on non-cached instances?
        newEntityCreated(entityClass, change);
      }
      else {
        updateCachedEntity(cachedEntity, dao, currentRevision);
      }
    }
    catch(Exception e) {
      log.error("Could not handle Change", e);
    }
  }

  protected void updateCachedEntity(BaseEntity cachedEntity, Dao dao, SavedRevision currentRevision) throws SQLException {
    EntityConfig entityConfig = dao.getEntityConfig();
    Map<String, Object> detectedChanges = getChanges(cachedEntity, dao, entityConfig, currentRevision);

    if (detectedChanges.size() > 0) {
      for(String propertyName : detectedChanges.keySet()) {
        updateProperty(cachedEntity, propertyName, dao, entityConfig, currentRevision, detectedChanges);
      }
    }
  }

  protected void updateProperty(BaseEntity cachedEntity, String propertyName, Dao dao, EntityConfig entityConfig, SavedRevision currentRevision, Map<String, Object> detectedChanges) throws SQLException {
    PropertyConfig property = entityConfig.getPropertyByColumnName(propertyName);
    Object previousValue = dao.extractValueFromObject(cachedEntity, property);

    if(property.isCollectionProperty() == false) {
      Object updatedValue = dao.deserializePersistedValue(property, currentRevision.getProperty(propertyName));
      dao.setValueOnObject(cachedEntity, property, updatedValue);

      cachedEntity.callPropertyChangedListeners(propertyName, null, detectedChanges.get(propertyName)); // TODO: also supply previousValue
    }
    else {
      updateCollectionProperty(cachedEntity, property, propertyName, currentRevision, detectedChanges, previousValue);
    }
  }

  protected void updateCollectionProperty(BaseEntity cachedEntity, PropertyConfig property, String propertyName, SavedRevision currentRevision, Map<String, Object> detectedChanges,
                                  Object previousValue) throws SQLException {
    String previousTargetEntityIds = (String)detectedChanges.get(propertyName);
    String currentTargetEntityIds = (String)currentRevision.getProperty(propertyName);

    Collection previousValueCollection = (Collection)previousValue;

    if(previousValue instanceof EntitiesCollection) { // TODO: what to do if it's not an EntitiesCollection yet?
      ((EntitiesCollection)previousValue).refresh();
    }

    Collection<BaseEntity> addedEntities = getEntitiesAddedToCollection(property, currentTargetEntityIds, previousTargetEntityIds);
    for(BaseEntity addedEntity : addedEntities) {
      cachedEntity.callEntityAddedListeners(previousValueCollection, addedEntity);
    }

    Collection<BaseEntity> removedEntities = getEntitiesRemovedFromCollection(property, currentTargetEntityIds, previousTargetEntityIds);
    for(BaseEntity removedEntity : removedEntities) {
      cachedEntity.callEntityRemovedListeners(previousValueCollection, removedEntity);
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
      log.error("Could not get Entities added to Collection from currentTargetEntityIdsString = " + currentTargetEntityIdsString +
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
      if(propertyConfig.isId() || propertyConfig.isVersion() || propertyConfig instanceof DiscriminatorColumnConfig) {
        continue;
      }

      try {
        Object currentRevisionValue = currentRevisionProperties.get(propertyConfig.getColumnName());
        Object cachedEntityValue = dao.getPersistablePropertyValue(cachedEntity, propertyConfig);

        if((cachedEntityValue == null && currentRevisionValue != null) || (cachedEntityValue != null && currentRevisionValue == null) ||
            (cachedEntityValue != null && cachedEntityValue.equals(currentRevisionValue) == false)) {
          detectedChanges.put(propertyConfig.getColumnName(), cachedEntityValue);
        }
      } catch(Exception e) {
        log.error("Could not check Property " + propertyConfig + " for changes", e);
      }
    }

    return detectedChanges;
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

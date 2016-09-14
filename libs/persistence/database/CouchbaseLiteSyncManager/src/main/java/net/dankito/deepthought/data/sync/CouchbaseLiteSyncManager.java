package net.dankito.deepthought.data.sync;

import com.couchbase.lite.Database;
import com.couchbase.lite.DocumentChange;
import com.couchbase.lite.Manager;
import com.couchbase.lite.listener.Credentials;
import com.couchbase.lite.listener.LiteListener;
import com.couchbase.lite.replicator.Replication;

import net.dankito.deepthought.communication.Constants;
import net.dankito.deepthought.communication.connected_device.IConnectedDevicesListenerManager;
import net.dankito.deepthought.communication.model.ConnectedDevice;
import net.dankito.deepthought.data.persistence.CouchbaseLiteEntityManagerBase;
import net.dankito.deepthought.data.persistence.db.BaseEntity;
import net.dankito.deepthought.util.IThreadPool;
import net.dankito.jpa.couchbaselite.Dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
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

  protected SynchronizedDataMerger dataMerger;


  public CouchbaseLiteSyncManager(CouchbaseLiteEntityManagerBase entityManager, IThreadPool threadPool, IConnectedDevicesListenerManager connectedDevicesListenerManager) {
    this(entityManager, threadPool, connectedDevicesListenerManager, Constants.SynchronizationDefaultPort, true);
  }

  public CouchbaseLiteSyncManager(CouchbaseLiteEntityManagerBase entityManager, IThreadPool threadPool, IConnectedDevicesListenerManager connectedDevicesListenerManager, int synchronizationPort, boolean alsoUsePullReplication) {
    super(connectedDevicesListenerManager, threadPool);
    this.entityManager = entityManager;
    this.database = entityManager.getDatabase();
    this.manager = database.getManager();
    this.synchronizationPort = synchronizationPort;
    this.alsoUsePullReplication = alsoUsePullReplication;

    this.dataMerger = new SynchronizedDataMerger(entityManager, database);
  }


  public void stop() {
    stopCBLListener();

    stopAllReplications();
  }

  protected void stopAllReplications() {
    for(Replication pullReplication : pullReplications.values()) {
      pullReplication.stop();
    }

    for(Replication pushReplication : pushReplications.values()) {
      pushReplication.stop();
    }
  }


  protected void startCBLListener(int listenPort, Manager manager, Credentials allowedCredentials) throws Exception {
    couchbaseLiteListener = new LiteListener(manager, listenPort, allowedCredentials);
    synchronizationPort = couchbaseLiteListener.getListenPort();

    listenerThread = new Thread(couchbaseLiteListener);
    listenerThread.start();
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
        startCBLListener(synchronizationPort, manager, allowedCredentials);
      }

      if(isAlreadySynchronizingWithDevice(device) == false) { // avoid that synchronization is started twice with the same device
        startReplication(device);
      }
    }
  }

  protected boolean isAlreadySynchronizingWithDevice(ConnectedDevice device) {
    return pushReplications.containsKey(getDeviceKey(device));
  }

  protected String getDeviceKey(ConnectedDevice device) {
    return device.getDeviceId();
  }

  protected void startReplication(ConnectedDevice device) throws Exception {
    URL syncUrl;
    try {
      int remoteDeviceSyncPort = device.getSynchronizationPort();
      syncUrl = new URL("http://" + device.getAddress() + ":" + remoteDeviceSyncPort + "/" + database.getName());
    } catch (MalformedURLException e) {
      throw new Exception(e);
    }

    Replication pushReplication = database.createPushReplication(syncUrl);
    pushReplication.addChangeListener(replicationChangeListener);
    pushReplication.setContinuous(true);

    pushReplications.put(getDeviceKey(device), pushReplication);

    pushReplication.start();

    if (alsoUsePullReplication) {
      Replication pullReplication = database.createPullReplication(syncUrl);
      pullReplication.addChangeListener(replicationChangeListener);
      pullReplication.setContinuous(true);

      pullReplications.put(getDeviceKey(device), pullReplication);

      pullReplication.start();
    }

    database.addChangeListener(databaseChangeListener);
  }

  @Override
  protected void stopSynchronizationWithDevice(ConnectedDevice device) {
    synchronized(this) {
      Replication pullReplication = pullReplications.remove(getDeviceKey(device));
      if(pullReplication != null) {
        pullReplication.stop();
      }

      Replication pushReplication = pushReplications.remove(getDeviceKey(device));
      if(pushReplication != null) {
        pushReplication.stop();
      }

      if(pushReplications.size() == 0) { // no devices connected anymore
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
        threadPool.runTaskAsync(new Runnable() {
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
      BaseEntity cachedEntity = dataMerger.synchronizedChange(change);

      if(cachedEntity != null) {
        callEntitySynchronizedListeners(cachedEntity);
      }
      else if(hasSynchronizationListeners()) {
        BaseEntity synchronizedEntity = getSynchronizedEntity(change);
        if(synchronizedEntity != null) {
          callEntitySynchronizedListeners(synchronizedEntity);
        }
      }
    }
  }

  protected BaseEntity getSynchronizedEntity(DocumentChange change) {
    try {
      Class entityClass = (Class<BaseEntity>) Class.forName((String) change.getAddedRevision().getPropertyForKey(Dao.TYPE_COLUMN_NAME));
      return entityManager.getEntityById(entityClass, change.getDocumentId());
    } catch(Exception e) {
      log.error("Could not get Entity Class for DocumentChange, AddedRevision's " + Dao.TYPE_COLUMN_NAME + " property is " + change.getAddedRevision().getPropertyForKey(Dao.TYPE_COLUMN_NAME), e);
    }

    return null;
  }

}

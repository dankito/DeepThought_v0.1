package net.dankito.deepthought.data.sync;

import com.couchbase.lite.Database;
import com.couchbase.lite.DocumentChange;
import com.couchbase.lite.Manager;
import com.couchbase.lite.ReplicationFilter;
import com.couchbase.lite.SavedRevision;
import com.couchbase.lite.listener.Credentials;
import com.couchbase.lite.listener.LiteListener;
import com.couchbase.lite.replicator.Replication;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.communication.Constants;
import net.dankito.deepthought.communication.ICommunicationConfigurationManager;
import net.dankito.deepthought.communication.connected_device.IConnectedRegisteredDevicesListenerManager;
import net.dankito.deepthought.communication.connected_device.IDevicesFinderListenerManager;
import net.dankito.deepthought.communication.model.ConnectedDevice;
import net.dankito.deepthought.data.model.DeepThought;
import net.dankito.deepthought.data.model.DeepThoughtApplication;
import net.dankito.deepthought.data.model.enums.ApplicationLanguage;
import net.dankito.deepthought.data.persistence.CouchbaseLiteEntityManagerBase;
import net.dankito.deepthought.util.AsyncProducerConsumerQueue;
import net.dankito.deepthought.util.ConsumerListener;
import net.dankito.deepthought.util.IThreadPool;
import net.dankito.deepthought.util.StringUtils;
import net.dankito.jpa.couchbaselite.Dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CouchbaseLiteSyncManager extends SyncManagerBase {

  protected static final String FILTER_NAME = "ENTITIES_FILTER";


  private static final Logger log = LoggerFactory.getLogger(CouchbaseLiteSyncManager.class);


  protected CouchbaseLiteEntityManagerBase entityManager;

  protected Database database;

  protected Manager manager;

  protected ICommunicationConfigurationManager configurationManager;

  protected boolean alsoUsePullReplication;

  protected int synchronizationPort;
  protected Credentials allowedCredentials;

  protected Thread listenerThread;

  protected LiteListener couchbaseLiteListener;

  protected Map<String, Replication> pushReplications = new ConcurrentHashMap<>();
  protected Map<String, Replication> pullReplications = new ConcurrentHashMap<>();

  protected SynchronizedDataMerger dataMerger;

  protected AsyncProducerConsumerQueue<Database.ChangeEvent> changeQueue;


  public CouchbaseLiteSyncManager(CouchbaseLiteEntityManagerBase entityManager, IThreadPool threadPool, IConnectedRegisteredDevicesListenerManager connectedDevicesListenerManager,
                                  IDevicesFinderListenerManager devicesFinderListenerManager, ICommunicationConfigurationManager configurationManager) {
    this(entityManager, threadPool, connectedDevicesListenerManager, devicesFinderListenerManager, configurationManager, Constants.SynchronizationDefaultPort, true);
  }

  public CouchbaseLiteSyncManager(CouchbaseLiteEntityManagerBase entityManager, IThreadPool threadPool, IConnectedRegisteredDevicesListenerManager connectedDevicesListenerManager,
                                  IDevicesFinderListenerManager devicesFinderListenerManager, ICommunicationConfigurationManager configurationManager, int synchronizationPort, boolean alsoUsePullReplication) {
    super(connectedDevicesListenerManager, devicesFinderListenerManager, threadPool);
    this.entityManager = entityManager;
    this.database = entityManager.getDatabase();
    this.manager = database.getManager();
    this.configurationManager = configurationManager;
    this.synchronizationPort = synchronizationPort;
    this.alsoUsePullReplication = alsoUsePullReplication;

    this.dataMerger = new SynchronizedDataMerger(this, entityManager, database);

    this.changeQueue = new AsyncProducerConsumerQueue<>(synchronizationChangesHandler);

    setReplicationFilter(database);
  }

  private void setReplicationFilter(Database database) {
    final List<String> entitiesToFilter = new ArrayList<>();
    entitiesToFilter.add(DeepThought.class.getName());
    entitiesToFilter.add(DeepThoughtApplication.class.getName());
    entitiesToFilter.add(ApplicationLanguage.class.getName());

    database.setFilter(FILTER_NAME, new ReplicationFilter() {
      @Override
      public boolean filter(SavedRevision revision, Map<String, Object> params) {
        // TODO: also check if owner matches loggedOnUser (or if loggedOnUser is allowed to sync a Document from that owner)
        String entityType = (String)revision.getProperty(Dao.TYPE_COLUMN_NAME);
        return ! ( entitiesToFilter.contains(entityType) || filterEntity(revision.getDocument().getId()) );
      }
    });
  }

  protected boolean filterEntity(String id) {
    DeepThought deepThought = Application.getDeepThought();
    if(deepThought != null) {
      return id.equals(deepThought.getTopLevelEntry().getId()) ||
          id.equals(deepThought.getTopLevelCategory().getId());
    }

    return false;
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


  @Override
  public boolean isListenerStarted() {
    return couchbaseLiteListener != null;
  }

  @Override
  protected void startSynchronizationListener() {
    try {
      startCBLListener(synchronizationPort, manager, allowedCredentials);
    } catch(Exception e) { log.error("Could not start Couchbase Lite synchronization listener", e); }
  }

  @Override
  protected void stopSynchronizationListener() {
    stopCBLListener();
  }

  protected void startCBLListener(int listenPort, Manager manager, Credentials allowedCredentials) throws Exception {
    log.info("Starting Couchbase Lite Listener");

    couchbaseLiteListener = new LiteListener(manager, listenPort, allowedCredentials);
    synchronizationPort = couchbaseLiteListener.getListenPort();

    configurationManager.setSynchronizationPort(synchronizationPort);

    listenerThread = new Thread(couchbaseLiteListener);
    listenerThread.start();
  }

  protected void stopCBLListener() {
    log.info("Stopping Couchbase Lite Listener");

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
      if(isListenerStarted() == false) { // first device has connected -> start Listener first
        startSynchronizationListener();
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
    return device.getDeviceUniqueId();
  }

  protected void startReplication(ConnectedDevice device) throws Exception {
    log.info("Starting Replication with Device " + device);

    URL syncUrl;
    try {
      int remoteDeviceSyncPort = device.getSynchronizationPort();
      syncUrl = new URL("http://" + device.getAddress() + ":" + remoteDeviceSyncPort + "/" + database.getName());
    } catch (MalformedURLException e) {
      throw new Exception(e);
    }

    Replication pushReplication = database.createPushReplication(syncUrl);
    pushReplication.setFilter(FILTER_NAME);
    pushReplication.addChangeListener(replicationChangeListener);
    pushReplication.setContinuous(true);

    pushReplications.put(getDeviceKey(device), pushReplication);

    pushReplication.start();

    if (alsoUsePullReplication) {
      Replication pullReplication = database.createPullReplication(syncUrl);
      pullReplication.setFilter(FILTER_NAME);
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
      log.info("Stopping Replication with Device " + device);

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



  protected Replication.ChangeListener replicationChangeListener = new Replication.ChangeListener() {
    @Override
    public void changed(Replication.ChangeEvent event) {

    }
  };

  protected Database.ChangeListener databaseChangeListener = new Database.ChangeListener() {
    @Override
    public void changed(final Database.ChangeEvent event) {
      logChanges(event); // TODO: remove again

      if(event.isExternal()) {
        changeQueue.add(event);
      }
    }
  };


  protected ConsumerListener<Database.ChangeEvent> synchronizationChangesHandler = new ConsumerListener<Database.ChangeEvent>() {
    @Override
    public void consumeItem(Database.ChangeEvent item) {
      handleSynchronizedChanges(item.getChanges());
    }
  };

  protected void handleSynchronizedChanges(List<DocumentChange> changes) {
    for(DocumentChange change : changes) {
      dataMerger.synchronizeChange(change);
    }
  }

  protected void logChanges(Database.ChangeEvent event) {
    for(DocumentChange change : event.getChanges()) {
      String entityTypeString = (String)change.getAddedRevision().getPropertyForKey(Dao.TYPE_COLUMN_NAME);
      if(StringUtils.isNullOrEmpty(entityTypeString)) {
        break;
      }

      if(entityTypeString.endsWith(".Entry")) {
        String content = Application.getHtmlHelper().extractPlainTextFromHtmlBody((String)change.getAddedRevision().getPropertyForKey("content"));
        content = content.length() > 30 ? content.substring(0, 30) : content;
        log.info("Synchronized Entry of Revision " + change.getAddedRevision().getRevID() + ": " + content);
        log.info("Tags: " + change.getAddedRevision().getPropertyForKey("tags"));
      }
      else if(entityTypeString.endsWith(".Tag")) {
        log.info("Synchronized Tag " + change.getAddedRevision().getPropertyForKey("name") + " of Revision " + change.getAddedRevision().getRevID());
        log.info("Entries: " + change.getAddedRevision().getPropertyForKey("entries"));
      }
      else if(entityTypeString.endsWith(".User")) {
        log.info("Synchronized User " + change.getDocumentId() + " " + change.getAddedRevision().getPropertyForKey("user_name") + " of Revision " + change.getAddedRevision().getRevID());
        log.info("Devices: " + change.getAddedRevision().getPropertyForKey("devices"));
        log.info("Groups: " + change.getAddedRevision().getPropertyForKey("groups"));
      }
      else if(entityTypeString.endsWith(".Device")) {
        log.info("Synchronized Device " + change.getDocumentId() + " " + change.getAddedRevision().getPropertyForKey("universallyUniqueId") + " " +
            change.getAddedRevision().getPropertyForKey("name") + " " + change.getAddedRevision().getPropertyForKey("platform") + " of Revision " + change.getAddedRevision().getRevID());
        log.info("Users: " + change.getAddedRevision().getPropertyForKey("users"));
        log.info("Groups: " + change.getAddedRevision().getPropertyForKey("groups"));
      }
    }
  }

}

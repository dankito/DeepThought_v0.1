package net.dankito.deepthought.data.sync;

import com.couchbase.lite.Database;
import com.couchbase.lite.DocumentChange;
import com.couchbase.lite.Manager;
import com.couchbase.lite.listener.Credentials;
import com.couchbase.lite.listener.LiteListener;
import com.couchbase.lite.replicator.Replication;

import net.dankito.deepthought.communication.Constants;
import net.dankito.deepthought.communication.IDeepThoughtConnector;
import net.dankito.deepthought.communication.model.ConnectedDevice;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CouchbaseLiteSyncManager extends SyncManagerBase {

  protected Database database;

  protected Manager manager;

  protected boolean alsoUsePullReplication;

  protected int synchronizationPort;
  protected Credentials allowedCredentials;

  protected Thread listenerThread;

  protected LiteListener couchbaseLiteListener;

  protected Map<String, Replication> pushReplications = new ConcurrentHashMap<>();
  protected Map<String, Replication> pullReplications = new ConcurrentHashMap<>();


  public CouchbaseLiteSyncManager(Database database, IDeepThoughtConnector deepThoughtConnector) {
    this(database, deepThoughtConnector, true);
  }

  public CouchbaseLiteSyncManager(Database database, IDeepThoughtConnector deepThoughtConnector, boolean alsoUsePullReplication) {
    super(deepThoughtConnector);
    this.database = database;
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
    public void changed(Database.ChangeEvent event) {
      List<DocumentChange> changes = event.getChanges();
      if(event.isExternal()) {

      }
    }
  };

}

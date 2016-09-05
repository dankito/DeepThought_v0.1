package net.dankito.deepthought.data.sync;

import com.couchbase.lite.CouchbaseLiteException;

import net.dankito.deepthought.TestEntityManagerConfiguration;
import net.dankito.deepthought.communication.Constants;
import net.dankito.deepthought.communication.NetworkHelper;
import net.dankito.deepthought.communication.model.ConnectedDevice;
import net.dankito.deepthought.data.model.Tag;
import net.dankito.deepthought.data.persistence.CouchbaseLiteEntityManagerBase;
import net.dankito.deepthought.data.persistence.JavaCouchbaseLiteEntityManager;
import net.dankito.deepthought.data.persistence.db.BaseEntity;
import net.dankito.deepthought.data.sync.helper.TestConnectedDevicesListenerManager;
import net.dankito.deepthought.util.IThreadPool;
import net.dankito.deepthought.util.ThreadPool;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by ganymed on 05/09/16.
 */
public class CouchbaseLiteSyncManagerTest {

  public static final int DEVICE_1_SYNCHRONIZATION_PORT = 23456;

  public static final int DEVICE_2_SYNCHRONIZATION_PORT = 23458;


  protected TestConnectedDevicesListenerManager listenerManager1 = new TestConnectedDevicesListenerManager();
  protected TestConnectedDevicesListenerManager listenerManager2 = new TestConnectedDevicesListenerManager();

  protected CouchbaseLiteEntityManagerBase entityManager1;
  protected CouchbaseLiteEntityManagerBase entityManager2;

  protected CouchbaseLiteSyncManager syncManager1;
  protected CouchbaseLiteSyncManager syncManager2;

  protected ConnectedDevice device1;
  protected ConnectedDevice device2;


  @Before
  public void setUp() throws Exception {
    IThreadPool threadPool = new ThreadPool();

    entityManager1 = new JavaCouchbaseLiteEntityManager(new TestEntityManagerConfiguration("data/tests/couchbase/01"));
    entityManager2 = new JavaCouchbaseLiteEntityManager(new TestEntityManagerConfiguration("data/tests/couchbase/02"));

    syncManager1 = new CouchbaseLiteSyncManager(entityManager1, threadPool, listenerManager1, DEVICE_1_SYNCHRONIZATION_PORT, true);
    syncManager2 = new CouchbaseLiteSyncManager(entityManager2, threadPool, listenerManager2, DEVICE_2_SYNCHRONIZATION_PORT, true);

    device1 = new ConnectedDevice(UUID.randomUUID().toString(), NetworkHelper.getIPAddressString(true), Constants.MessageHandlerDefaultPort);
    device1.setSynchronizationPort(DEVICE_1_SYNCHRONIZATION_PORT);

    device2 = new ConnectedDevice(UUID.randomUUID().toString(), NetworkHelper.getIPAddressString(true), Constants.MessageHandlerDefaultPort);
    device2.setSynchronizationPort(DEVICE_2_SYNCHRONIZATION_PORT);

    listenerManager1.simulateDeviceConnected(device2);
    listenerManager2.simulateDeviceConnected(device1);
  }


  @After
  public void tearDown() throws CouchbaseLiteException {
    syncManager1.stop();
    syncManager2.stop();

    entityManager1.close();
    entityManager2.close();

    entityManager1.getDatabase().delete();
    entityManager2.getDatabase().delete();
  }


  @Test
  public void persistEntityWithoutRelation_EntityGetSynchronizedCorrectly() {
    final CountDownLatch synchronizationLatch = new CountDownLatch(1);
    final List<BaseEntity> synchronizedEntities = new ArrayList<>();

    syncManager2.addSynchronizationListener(new ISynchronizationListener() {
      @Override
      public void entitySynchronized(BaseEntity entity) {
        synchronizedEntities.add(entity);
        synchronizationLatch.countDown();
      }
    });

    Tag testEntity = new Tag("Test");
    entityManager1.persistEntity(testEntity);

    try { synchronizationLatch.await(5, TimeUnit.SECONDS); } catch(Exception ignored) { }

    Assert.assertEquals(1, synchronizedEntities.size());

    Tag synchronizedEntity = (Tag)synchronizedEntities.get(0);
    Assert.assertEquals(testEntity.getId(), synchronizedEntity.getId());
    Assert.assertEquals(testEntity.getName(), synchronizedEntity.getName());
    Assert.assertEquals(testEntity.getDescription(), synchronizedEntity.getDescription());
  }
}

package net.dankito.deepthought.data.sync;

import com.couchbase.lite.CouchbaseLiteException;

import net.dankito.deepthought.TestEntityManagerConfiguration;
import net.dankito.deepthought.communication.Constants;
import net.dankito.deepthought.communication.NetworkHelper;
import net.dankito.deepthought.communication.model.ConnectedDevice;
import net.dankito.deepthought.data.model.DeepThought;
import net.dankito.deepthought.data.model.Entry;
import net.dankito.deepthought.data.model.Tag;
import net.dankito.deepthought.data.persistence.CouchbaseLiteEntityManagerBase;
import net.dankito.deepthought.data.persistence.JavaCouchbaseLiteEntityManager;
import net.dankito.deepthought.data.persistence.db.BaseEntity;
import net.dankito.deepthought.data.sync.helper.TestCommunicationConfigurationManager;
import net.dankito.deepthought.data.sync.helper.TestConnectedDevicesListenerManager;
import net.dankito.deepthought.data.sync.helper.TestDevicesFinderListenerManager;
import net.dankito.deepthought.util.IThreadPool;
import net.dankito.deepthought.util.ThreadPool;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by ganymed on 05/09/16.
 */
public class CouchbaseLiteSyncManagerTest {

  public static final int DEVICE_1_SYNCHRONIZATION_PORT = 23456;

  public static final int DEVICE_2_SYNCHRONIZATION_PORT = 23458;

  protected static final String COMMON_ENTRY_CONTENT = "Common";

  protected static final String ENTRY_ON_FIRST_DEVICE_1_CONTENT = "1_1";
  protected static final String ENTRY_ON_FIRST_DEVICE_2_CONTENT = "1_2";

  protected static final String ENTRY_ON_SECOND_DEVICE_1_CONTENT = "2_1";
  protected static final String ENTRY_ON_SECOND_DEVICE_2_CONTENT = "2_2";


  protected TestConnectedDevicesListenerManager listenerManager1 = new TestConnectedDevicesListenerManager();
  protected TestConnectedDevicesListenerManager listenerManager2 = new TestConnectedDevicesListenerManager();

  protected TestDevicesFinderListenerManager devicesFinderListenerManager1 = new TestDevicesFinderListenerManager();
  protected TestDevicesFinderListenerManager devicesFinderListenerManager2 = new TestDevicesFinderListenerManager();

  protected TestCommunicationConfigurationManager configurationManager1 = new TestCommunicationConfigurationManager();
  protected TestCommunicationConfigurationManager configurationManager2 = new TestCommunicationConfigurationManager();

  protected CouchbaseLiteEntityManagerBase entityManager1;
  protected CouchbaseLiteEntityManagerBase entityManager2;

  protected CouchbaseLiteSyncManager syncManager1;
  protected CouchbaseLiteSyncManager syncManager2;

  protected ConnectedDevice device1;
  protected ConnectedDevice device2;


  @Before
  public void setUp() throws Exception {
    IThreadPool threadPool = new ThreadPool();

    entityManager1 = new JavaCouchbaseLiteEntityManager(new TestEntityManagerConfiguration("data/tests/couchbase/01" + System.currentTimeMillis()));
    entityManager2 = new JavaCouchbaseLiteEntityManager(new TestEntityManagerConfiguration("data/tests/couchbase/02" + System.currentTimeMillis()));

    syncManager1 = new CouchbaseLiteSyncManager(entityManager1, threadPool, listenerManager1, devicesFinderListenerManager1, configurationManager1, DEVICE_1_SYNCHRONIZATION_PORT, true);
    syncManager2 = new CouchbaseLiteSyncManager(entityManager2, threadPool, listenerManager2, devicesFinderListenerManager2, configurationManager2, DEVICE_2_SYNCHRONIZATION_PORT, true);

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

  @Test
  public void createConflictInCollectionProperty_ConflictGetsResolved() {
    final CountDownLatch preliminaryLatch = new CountDownLatch(8);
    syncManager2.addSynchronizationListener(new ISynchronizationListener() {
      @Override
      public void entitySynchronized(BaseEntity entity) {
        preliminaryLatch.countDown();
      }
    });

    DeepThought deepThought = new DeepThought(Entry.createTopLevelEntry());
    entityManager1.persistEntity(deepThought);

    Entry commonEntry = new Entry(COMMON_ENTRY_CONTENT);
    entityManager1.persistEntity(commonEntry);

    deepThought.addEntry(commonEntry);
    entityManager1.updateEntity(deepThought);

    // now wait till deepThought and commonEntry are synchronized
    try { preliminaryLatch.await(5, TimeUnit.SECONDS); } catch(Exception ignored) { }

    stopSynchronization();

    Entry entryOn1_1 = new Entry(ENTRY_ON_FIRST_DEVICE_1_CONTENT);
    entityManager1.persistEntity(entryOn1_1);
    Entry entryOn1_2 = new Entry(ENTRY_ON_FIRST_DEVICE_2_CONTENT);
    entityManager1.persistEntity(entryOn1_2);

    deepThought.addEntry(entryOn1_1);
    deepThought.addEntry(entryOn1_2);
    entityManager1.updateEntity(deepThought);


    Entry entryOn2_1 = new Entry(ENTRY_ON_SECOND_DEVICE_1_CONTENT);
    entityManager2.persistEntity(entryOn2_1);
    Entry entryOn2_2 = new Entry(ENTRY_ON_SECOND_DEVICE_2_CONTENT);
    entityManager2.persistEntity(entryOn2_2);

    DeepThought synchronizedDeepThought = entityManager2.getAllEntitiesOfType(DeepThought.class).get(0);
    Assert.assertEquals(1, synchronizedDeepThought.getCountEntries());

    synchronizedDeepThought.addEntry(entryOn2_1);
    synchronizedDeepThought.addEntry(entryOn2_2);
    entityManager2.updateEntity(synchronizedDeepThought);


    final CountDownLatch resolveConflictLatch = new CountDownLatch(6);
    syncManager2.addSynchronizationListener(new ISynchronizationListener() {
      @Override
      public void entitySynchronized(BaseEntity entity) {
        resolveConflictLatch.countDown();
      }
    });

    restartSynchronization();

    try { resolveConflictLatch.await(5, TimeUnit.SECONDS); } catch(Exception ignored) { }


    Collection<Entry> device2Entries = synchronizedDeepThought.getEntries();

    Assert.assertEquals(5, device2Entries.size());
    Assert.assertTrue(device2Entries.contains(entryOn2_1));
    Assert.assertTrue(device2Entries.contains(entryOn2_2));

    Set<String> device2EntryContents = new HashSet<>();
    for(Entry entry : device2Entries) {
      device2EntryContents.add(entry.getContent());
    }

    Assert.assertTrue(device2EntryContents.contains(COMMON_ENTRY_CONTENT));
    Assert.assertTrue(device2EntryContents.contains(ENTRY_ON_FIRST_DEVICE_1_CONTENT));
    Assert.assertTrue(device2EntryContents.contains(ENTRY_ON_FIRST_DEVICE_2_CONTENT));
    Assert.assertTrue(device2EntryContents.contains(ENTRY_ON_SECOND_DEVICE_1_CONTENT));
    Assert.assertTrue(device2EntryContents.contains(ENTRY_ON_SECOND_DEVICE_2_CONTENT));


    Collection<Entry> device1Entries = deepThought.getEntries();

    Assert.assertEquals(5, device1Entries.size());
    Assert.assertTrue(device1Entries.contains(commonEntry));
    Assert.assertTrue(device1Entries.contains(entryOn1_1));
    Assert.assertTrue(device1Entries.contains(entryOn1_2));

    Set<String> device1EntryContents = new HashSet<>();
    for(Entry entry : device1Entries) {
      device1EntryContents.add(entry.getContent());
    }

    Assert.assertTrue(device1EntryContents.contains(COMMON_ENTRY_CONTENT));
    Assert.assertTrue(device1EntryContents.contains(ENTRY_ON_FIRST_DEVICE_1_CONTENT));
    Assert.assertTrue(device1EntryContents.contains(ENTRY_ON_FIRST_DEVICE_2_CONTENT));
    Assert.assertTrue(device1EntryContents.contains(ENTRY_ON_SECOND_DEVICE_1_CONTENT));
    Assert.assertTrue(device1EntryContents.contains(ENTRY_ON_SECOND_DEVICE_2_CONTENT));
  }

  protected void stopSynchronization() {
    listenerManager1.simulateDeviceDisconnected(device2);
    listenerManager2.simulateDeviceDisconnected(device1);
  }

  protected void restartSynchronization() {
    listenerManager1.simulateDeviceConnected(device2);
    listenerManager2.simulateDeviceConnected(device1);

    try { Thread.sleep(2000); } catch(Exception ignored) { } // give Replicators some time to start
  }

}

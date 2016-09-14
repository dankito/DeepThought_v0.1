package net.dankito.deepthought.data.sync;

import com.couchbase.lite.CouchbaseLiteException;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.TestEntityManagerConfiguration;
import net.dankito.deepthought.communication.model.DeepThoughtInfo;
import net.dankito.deepthought.communication.model.GroupInfo;
import net.dankito.deepthought.communication.model.HostInfo;
import net.dankito.deepthought.communication.model.UserInfo;
import net.dankito.deepthought.data.model.DeepThought;
import net.dankito.deepthought.data.model.DeepThoughtApplication;
import net.dankito.deepthought.data.model.Device;
import net.dankito.deepthought.data.model.Entry;
import net.dankito.deepthought.data.model.Reference;
import net.dankito.deepthought.data.model.ReferenceSubDivision;
import net.dankito.deepthought.data.model.SeriesTitle;
import net.dankito.deepthought.data.model.Tag;
import net.dankito.deepthought.data.model.User;
import net.dankito.deepthought.data.persistence.CouchbaseLiteEntityManagerBase;
import net.dankito.deepthought.data.persistence.JavaCouchbaseLiteEntityManager;
import net.dankito.deepthought.data.persistence.db.UserDataEntity;
import net.dankito.deepthought.util.IThreadPool;
import net.dankito.deepthought.util.ThreadPool;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Application.class)
public class InitialSyncManagerTest {

  public static final int COUNT_ENTRIES_1 = 4;
  public static final int COUNT_TAGS_ON_ENTRY_1 = 3;
  public static final int COUNT_TAGS_1 = 5;
  public static final int COUNT_SERIES_TITLES_1 = 2;
  public static final int COUNT_REFERENCES_1 = 5;
  public static final int COUNT_REFERENCE_SUB_DIVISIONS_1 = 5;


  protected InitialSyncManager underTest;

  protected CouchbaseLiteEntityManagerBase entityManager1;
  protected CouchbaseLiteEntityManagerBase entityManager2;

  protected DeepThought deepThought1;
  protected DeepThought deepThought2;

  protected User user1;
  protected User user2;

  protected Device localDevice1;
  protected Device localDevice2;

  protected List<Entry> localEntries = new ArrayList<>();
  protected List<Tag> localTags = new ArrayList<>();
  protected List<SeriesTitle> localSeries = new ArrayList<>();
  protected List<Reference> localReferences = new ArrayList<>();
  protected List<ReferenceSubDivision> localReferenceSubDivisions = new ArrayList<>();

  protected Random random = new Random(System.currentTimeMillis());


  @Before
  public void setUp() throws Exception {
    IThreadPool threadPool = new ThreadPool();

    entityManager1 = new JavaCouchbaseLiteEntityManager(new TestEntityManagerConfiguration("data/tests/couchbase/01" + System.currentTimeMillis()));
    entityManager2 = new JavaCouchbaseLiteEntityManager(new TestEntityManagerConfiguration("data/tests/couchbase/02" + System.currentTimeMillis()));

    underTest = new InitialSyncManager(entityManager1);

    setupEntities();
  }

  protected void setupEntities() {
    DeepThoughtApplication deepThoughtApplication1 = DeepThoughtApplication.createApplication();

    user1 = deepThoughtApplication1.getLastLoggedOnUser();
    user1.setUserName("User 1");
    user1.getUsersDefaultGroup().setName("Group 1");
    localDevice1 = deepThoughtApplication1.getLocalDevice();
    deepThought1 = user1.getLastViewedDeepThought();

    PowerMockito.mockStatic(Application.class);
    BDDMockito.given(Application.getLoggedOnUser()).willReturn(user1);

    createTestEntitiesOnDeepThought(deepThought1);
    entityManager1.persistEntity(deepThoughtApplication1);

    DeepThoughtApplication deepThoughtApplication2 = DeepThoughtApplication.createApplication();

    user2 = deepThoughtApplication2.getLastLoggedOnUser();
    user2.setUserName("User 2");
    user2.getUsersDefaultGroup().setName("Group 2");
    localDevice2 = deepThoughtApplication2.getLocalDevice();
    deepThought2 = user2.getLastViewedDeepThought();

    entityManager2.persistEntity(deepThoughtApplication2);
  }


  @After
  public void tearDown() throws CouchbaseLiteException {
    entityManager1.close();
    entityManager2.close();

    entityManager1.getDatabase().delete();
    entityManager2.getDatabase().delete();
  }


  @Test
  public void syncLocalDatabaseIdsWithRemoteOnes() {
    assertEntityIdsDoNotEqual();

    underTest.syncLocalDatabaseIdsWithRemoteOnes(deepThought1, user1, localDevice1, DeepThoughtInfo.fromDeepThought(deepThought2), UserInfo.fromUser(user2),
        HostInfo.fromUserAndDevice(user2, localDevice2), GroupInfo.fromGroup(user2.getUsersDefaultGroup()));

    Assert.assertEquals(deepThought1.getId(), deepThought2.getId());

    Assert.assertEquals(user1.getId(), user2.getId());
    Assert.assertEquals(user1.getUniversallyUniqueId(), user2.getUniversallyUniqueId());
    Assert.assertEquals(user1.getUserName(), user2.getUserName());
    Assert.assertEquals(user1.getFirstName(), user2.getFirstName());
    Assert.assertEquals(user1.getLastName(), user2.getLastName());

    Assert.assertEquals(user1.getUsersDefaultGroup().getId(), user2.getUsersDefaultGroup().getId());
    Assert.assertEquals(user1.getUsersDefaultGroup().getName(), user2.getUsersDefaultGroup().getName());

    testDeepThoughtUserDataEntities(deepThought1, user2);
  }

  protected void assertEntityIdsDoNotEqual() {
    Assert.assertNotEquals(deepThought1.getId(), deepThought2.getId());

    Assert.assertNotEquals(user1.getId(), user2.getId());
    Assert.assertNotEquals(user1.getUniversallyUniqueId(), user2.getUniversallyUniqueId());
    Assert.assertNotEquals(user1.getUserName(), user2.getUserName());

    Assert.assertNotEquals(user1.getUsersDefaultGroup().getId(), user2.getUsersDefaultGroup().getId());
    Assert.assertNotEquals(user1.getUsersDefaultGroup().getName(), user2.getUsersDefaultGroup().getName());
  }

  protected void testDeepThoughtUserDataEntities(DeepThought localDeepThought, User remoteUser) {
    for(Entry entry : localDeepThought.getEntries()) {
      testUserDataEntityGotSynchronizedCorrectly(entry, remoteUser);
    }

    for(Tag tag : localDeepThought.getTags()) {
      testUserDataEntityGotSynchronizedCorrectly(tag, remoteUser);
    }

    for(SeriesTitle series : localDeepThought.getSeriesTitles()) {
      testUserDataEntityGotSynchronizedCorrectly(series, remoteUser);
    }

    for(Reference reference : localDeepThought.getReferences()) {
      testUserDataEntityGotSynchronizedCorrectly(reference, remoteUser);
    }

    for(ReferenceSubDivision subDivision : localDeepThought.getReferenceSubDivisions()) {
      testUserDataEntityGotSynchronizedCorrectly(subDivision, remoteUser);
    }
  }

  protected void testUserDataEntityGotSynchronizedCorrectly(UserDataEntity entity, User remoteUser) {
    Assert.assertEquals(remoteUser.getId(), entity.getCreatedBy().getId());
    Assert.assertEquals(remoteUser.getUniversallyUniqueId(), entity.getCreatedBy().getUniversallyUniqueId());

    Assert.assertEquals(remoteUser.getId(), entity.getModifiedBy().getId());
    Assert.assertEquals(remoteUser.getUniversallyUniqueId(), entity.getModifiedBy().getUniversallyUniqueId());

    Assert.assertEquals(remoteUser.getId(), entity.getOwner().getId());
    Assert.assertEquals(remoteUser.getUniversallyUniqueId(), entity.getOwner().getUniversallyUniqueId());
  }


  protected void createTestEntitiesOnDeepThought(DeepThought deepThought) {
    localTags = createTestTags();
    for(Tag tag : localTags) {
      deepThought.addTag(tag);
    }

    localSeries = createTestSeriesTitles();
    for(SeriesTitle series : localSeries) {
      deepThought.addSeriesTitle(series);
    }

    localReferences = createTestReferences(localSeries);
    for(Reference reference : localReferences) {
      deepThought.addReference(reference);
    }

    localReferenceSubDivisions = createTestReferenceSubDivisions(localReferences);
    for(ReferenceSubDivision subDivision : localReferenceSubDivisions) {
      deepThought.addReferenceSubDivision(subDivision);
    }

    localEntries = createTestEntries(localTags, localReferenceSubDivisions);
    for(Entry entry : localEntries) {
      deepThought.addEntry(entry);
    }
  }

  protected List<Tag> createTestTags() {
    List<Tag> tags = new ArrayList<>();

    for(int i = 0; i < COUNT_TAGS_1; i++) {
      Tag tag = new Tag("" + (i + 1));
      tags.add(tag);
    }

    return tags;
  }

  protected List<SeriesTitle> createTestSeriesTitles() {
    List<SeriesTitle> seriesTitles = new ArrayList<>();

    for(int i = 0; i < COUNT_SERIES_TITLES_1; i++) {
      SeriesTitle series = new SeriesTitle("" + (i + 1));
      seriesTitles.add(series);
    }

    return seriesTitles;
  }

  protected List<Reference> createTestReferences(List<SeriesTitle> seriesTitles) {
    List<Reference> references = new ArrayList<>();

    for(int i = 0; i < COUNT_REFERENCES_1; i++) {
      Reference reference = new Reference("" + (i + 1));
      reference.setSeries((SeriesTitle)getRandomEntityFromList(seriesTitles));
      references.add(reference);
    }

    return references;
  }

  protected List<ReferenceSubDivision> createTestReferenceSubDivisions(List<Reference> references) {
    List<ReferenceSubDivision> subDivisions = new ArrayList<>();

    for(int i = 0; i < COUNT_REFERENCE_SUB_DIVISIONS_1; i++) {
      ReferenceSubDivision subDivision = new ReferenceSubDivision("" + (i + 1));
      subDivision.setReference((Reference)getRandomEntityFromList(references));
      subDivisions.add(subDivision);
    }

    return subDivisions;
  }

  protected List<Entry> createTestEntries(List<Tag> tags, List<ReferenceSubDivision> subDivisions) {
    List<Entry> entries = new ArrayList<>();

    for(int i = 0; i < COUNT_ENTRIES_1; i++) {
      Entry entry = new Entry("" + (i + 1));
      entry.setReferenceSubDivision((ReferenceSubDivision)getRandomEntityFromList(subDivisions));

      for(int j = 0; j < COUNT_TAGS_ON_ENTRY_1; j++) {
        entry.addTag((Tag)getRandomEntityFromList(tags));
      }

      entries.add(entry);
    }

    return entries;
  }

  protected Object getRandomEntityFromList(List entities) {
    int randomIndex = random.nextInt(entities.size());

    return entities.get(randomIndex);
  }

}

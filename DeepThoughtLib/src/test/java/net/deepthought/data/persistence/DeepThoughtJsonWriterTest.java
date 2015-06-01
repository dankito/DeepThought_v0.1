package net.deepthought.data.persistence;

import net.deepthought.Application;
import net.deepthought.data.DefaultDataManager;
import net.deepthought.data.IDataManager;
import net.deepthought.data.TestApplicationConfiguration;
import net.deepthought.data.helper.DataHelper;
import net.deepthought.data.helper.MockEntityManager;
import net.deepthought.data.helper.TestDependencyResolver;
import net.deepthought.data.model.DeepThought;
import net.deepthought.data.model.DeepThoughtApplication;
import net.deepthought.data.model.User;
import net.deepthought.data.persistence.db.BaseEntity;
import net.deepthought.data.persistence.db.UserDataEntity;
import net.deepthought.data.persistence.deserializer.DeserializationResult;
import net.deepthought.data.persistence.json.JsonIoJsonHelper;
import net.deepthought.data.persistence.serializer.SerializationResult;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;

/**
 * Created by ganymed on 02/05/15.
 */
public class DeepThoughtJsonWriterTest {

  @Before
  public void setup() throws Exception {
    Application.instantiate(new TestApplicationConfiguration(), new TestDependencyResolver(new MockEntityManager()) {
//      @Override
//      public IEntityManager createEntityManager(EntityManagerConfiguration configuration) throws Exception {
//        return new MockEntityManagerWithPredefinedData();
//      }

      @Override
      public IDataManager createDataManager(IEntityManager entityManager) {
        return new DefaultDataManager(entityManager) {
          @Override
          public DeepThought retrieveDeepThoughtApplication() {
            this.application = DataHelper.createTestApplication();
            entityManager.persistEntity(application);
            application.addEntityListener(entityListener);

            loggedOnUser = application.getLastLoggedOnUser();

            DeepThought deepThought = loggedOnUser.getLastViewedDeepThought();
            setCurrentDeepThought(deepThought);
            entityManager.persistEntity(deepThought);

            return deepThought;
          }
        };
      }
    });
  }

  @After
  public void tearDown() {
    Application.shutdown();
  }


  @Test
  public void writeTestDeepThoughtToJson() {
    SerializationResult result = JsonIoJsonHelper.generateJsonString(Application.getApplication(), true);

    Assert.assertTrue(result.successful());
    Assert.assertNull(result.getError());

    String json = result.getSerializationResult();
    Assert.assertNotNull(json);
  }

  @Test
  public void writeTestDeepThoughtToJsonAndDeserialize() {
    SerializationResult serializationResult = JsonIoJsonHelper.generateJsonString(Application.getApplication(), true);

    DeserializationResult<DeepThoughtApplication> result = JsonIoJsonHelper.parseJsonString(serializationResult.getSerializationResult(), DeepThoughtApplication.class);
    Assert.assertTrue(result.successful());
    Assert.assertNull(result.getError());

    DeepThoughtApplication application = result.getResult();
    Assert.assertNotNull(application);

    testBaseEntityCollection(application.getUsers(), null, 1);
    Assert.assertNotNull(application.getLastLoggedOnUser());
    Assert.assertNotNull(application.getLastLoggedOnUser().getId());
    testBaseEntityCollection(application.getGroups(), null, 1);
    testBaseEntityCollection(application.getDevices(), null, 1);
    Assert.assertNotNull(application.getLocalDevice());
    Assert.assertNotNull(application.getLocalDevice().getId());

    User currentUser = application.getLastLoggedOnUser();
    DeepThought deepThought = currentUser.getLastViewedDeepThought();
    Assert.assertNotNull(deepThought);
    Assert.assertNotNull(deepThought.getId());

    Assert.assertNotNull(deepThought.getTopLevelCategory());
    Assert.assertNotNull(deepThought.getTopLevelCategory().getId());
    testBaseEntityCollection(deepThought.getCategories(), currentUser, 5);

    Assert.assertNotNull(deepThought.getTopLevelEntry());
    Assert.assertNotNull(deepThought.getTopLevelEntry().getId());
    testBaseEntityCollection(deepThought.getEntries(), currentUser, 3);

    testBaseEntityCollection(deepThought.getTags(), currentUser, 2);
    testBaseEntityCollection(deepThought.getPersons(), currentUser, 3);

    testBaseEntityCollection(deepThought.getFiles(), currentUser, 6);
    testBaseEntityCollection(deepThought.getBackupFileServiceTypes(), currentUser, 3);

    testBaseEntityCollection(deepThought.getNotes(), currentUser, 3);
    testBaseEntityCollection(deepThought.getNoteTypes(), currentUser, 5);

    testBaseEntityCollection(deepThought.getLanguages(), currentUser, 49);
  }

  protected void testBaseEntityCollection(Collection entities, User currentUser, int collectionSize) {
    Assert.assertEquals(collectionSize, entities.size());
    for(Object entity : entities)
      testBaseEntity((BaseEntity)entity, currentUser);
  }

  protected void testBaseEntity(BaseEntity entity, User currentUser) {
    Assert.assertNotNull(entity.getId());
    Assert.assertNotNull(entity.getCreatedOn());
    Assert.assertNotNull(entity.getModifiedOn());
    Assert.assertNotNull(entity.getVersion());
    Assert.assertFalse(entity.isDeleted());

    if(entity instanceof UserDataEntity) {
      UserDataEntity userDataEntity = (UserDataEntity)entity;

      Assert.assertEquals(currentUser, userDataEntity.getCreatedBy());
      Assert.assertEquals(currentUser, userDataEntity.getModifiedBy());
      Assert.assertEquals(currentUser, userDataEntity.getOwner());
    }
  }
}

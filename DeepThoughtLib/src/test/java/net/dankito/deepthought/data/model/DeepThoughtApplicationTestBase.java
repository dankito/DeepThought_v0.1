package net.dankito.deepthought.data.model;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.data.model.enums.ApplicationLanguage;
import net.dankito.deepthought.data.persistence.db.TableConfig;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by ganymed on 16/12/14.
 */
public abstract class DeepThoughtApplicationTestBase extends DataModelTestBase {


  @Test
  public void updateLastLoggedOnUser_UpdatedValueGetsPersistedInDb() throws Exception {
    DeepThoughtApplication application = Application.getApplication();

    User newValue = new User("Test Dummy");
    application.addUser(newValue);
    application.setLastLoggedOnUser(newValue);

    Assert.assertTrue(doIdsEqual(newValue.getId(),
        getValueFromTable(TableConfig.DeepThoughtApplicationTableName, TableConfig.DeepThoughtApplicationLastLoggedOnUserJoinColumnName, application.getId())));
  }

  @Test
  public void updateAutoLogOnLastLoggedOnUser_UpdatedValueGetsPersistedInDb() throws Exception {
    DeepThoughtApplication application = Application.getApplication();

    boolean newValue = !application.autoLogOnLastLoggedOnUser();
    application.setAutoLogOnLastLoggedOnUser(newValue);

    Object storedValue = getValueFromTable(TableConfig.DeepThoughtApplicationTableName, TableConfig.DeepThoughtApplicationAutoLogOnLastLoggedOnUserColumnName, application.getId());
    compareBoolValue(newValue, storedValue);
  }

  @Test
  public void addUser_RelationGetsPersisted() throws Exception {
    User user = new User("test");

    DeepThoughtApplication application = Application.getApplication();
    application.addUser(user);

    // assert user really got written to database
    Object persistedApplicationId = getValueFromTable(TableConfig.UserTableName, TableConfig.UserDeepThoughtApplicationJoinColumnName, user.getId());
    Assert.assertTrue(doIdsEqual(user.getApplication().getId(), persistedApplicationId));
  }

  @Test
  public void addUser_EntitiesGetAddedToRelatedCollections() throws Exception {
    User user = new User("test");

    DeepThoughtApplication application = Application.getApplication();
    application.addUser(user);

    Assert.assertNotNull(user.getApplication());
    Assert.assertTrue(application.getUsers().contains(user));
  }

  @Test
  public void removeUser_RelationGetsDeleted() throws Exception {
    User user = new User("test");

    DeepThoughtApplication application = Application.getApplication();
    application.addUser(user);

    application.removeUser(user);

    // assert user really got deleted from database
    Object persistedApplicationId = getValueFromTable(TableConfig.UserTableName, TableConfig.UserDeepThoughtApplicationJoinColumnName, user.getId());
    Assert.assertNull(persistedApplicationId);

    Assert.assertTrue(user.isDeleted());
  }

  @Test
  public void removeUser_EntitiesGetRemovedFromRelatedCollections() throws Exception {
    User user = new User("test");

    DeepThoughtApplication application = Application.getApplication();
    application.addUser(user);

    application.removeUser(user);

    Assert.assertNull(user.getApplication());
    Assert.assertFalse(application.getUsers().contains(user));
  }


  @Test
  public void addGroup_RelationGetsPersisted() throws Exception {
    Group group = new Group("test");

    DeepThoughtApplication application = Application.getApplication();
    application.addGroup(group);

    // assert group really got written to database
    Object persistedApplicationId = getValueFromTable(TableConfig.GroupTableName, TableConfig.GroupDeepThoughtApplicationJoinColumnName, group.getId());
    Assert.assertTrue(doIdsEqual(group.getApplication().getId(), persistedApplicationId));
  }

  @Test
  public void addGroup_EntitiesGetAddedToRelatedCollections() throws Exception {
    Group group = new Group("test");

    DeepThoughtApplication application = Application.getApplication();
    application.addGroup(group);

    Assert.assertNotNull(group.getApplication());
    Assert.assertTrue(application.getGroups().contains(group));
  }

  @Test
  public void removeGroup_RelationGetsDeleted() throws Exception {
    Group group = new Group("test");

    DeepThoughtApplication application = Application.getApplication();
    application.addGroup(group);

    application.removeGroup(group);

    // assert group really got deleted from database
    Object persistedApplicationId = getValueFromTable(TableConfig.GroupTableName, TableConfig.GroupDeepThoughtApplicationJoinColumnName, group.getId());
    Assert.assertNull(persistedApplicationId);

    Assert.assertTrue(group.isDeleted());
  }

  @Test
  public void removeGroup_EntitiesGetRemovedFromRelatedCollections() throws Exception {
    Group group = new Group("test");

    DeepThoughtApplication application = Application.getApplication();
    application.addGroup(group);

    application.removeGroup(group);

    Assert.assertNull(group.getApplication());
    Assert.assertFalse(application.getGroups().contains(group));
  }


  @Test
  public void addDevice_RelationGetsPersisted() throws Exception {
    Device device = new Device("unique", "test", "linux");

    DeepThoughtApplication application = Application.getApplication();
    application.addDevice(device);

    // assert device really got written to database
    Object persistedApplicationId = getValueFromTable(TableConfig.DeviceTableName, TableConfig.DeviceDeepThoughtApplicationJoinColumnName, device.getId());
    Assert.assertTrue(doIdsEqual(device.getApplication().getId(), persistedApplicationId));
  }

  @Test
  public void addDevice_EntitiesGetAddedToRelatedCollections() throws Exception {
    Device device = new Device("unique", "test", "linux");

    DeepThoughtApplication application = Application.getApplication();
    application.addDevice(device);

    Assert.assertNotNull(device.getApplication());
    Assert.assertTrue(application.getDevices().contains(device));
  }

  @Test
  public void removeDevice_RelationGetsDeleted() throws Exception {
    Device device = new Device("unique", "test", "linux");

    DeepThoughtApplication application = Application.getApplication();
    application.addDevice(device);

    application.removeDevice(device);

    // assert device really got deleted from database
    Object persistedApplicationId = getValueFromTable(TableConfig.DeviceTableName, TableConfig.DeviceDeepThoughtApplicationJoinColumnName, device.getId());
    Assert.assertNull(persistedApplicationId);

    Assert.assertTrue(device.isDeleted());
  }

  @Test
  public void removeDevice_EntitiesGetRemovedFromRelatedCollections() throws Exception {
    Device device = new Device("unique", "test", "linux");

    DeepThoughtApplication application = Application.getApplication();
    application.addDevice(device);

    application.removeDevice(device);

    Assert.assertNull(device.getApplication());
    Assert.assertFalse(application.getDevices().contains(device));
  }


  @Test
  public void addApplicationLanguage_RelationGetsPersisted() throws Exception {
    ApplicationLanguage language = new ApplicationLanguage("test");

    DeepThoughtApplication application = Application.getApplication();
    application.addApplicationLanguage(language);

    // assert ApplicationLanguage really got written to database
    Object persistedApplicationId = getValueFromTable(TableConfig.ApplicationLanguageTableName, TableConfig.ApplicationLanguageDeepThoughtApplicationJoinColumnName, language.getId());
    Assert.assertTrue(doIdsEqual(language.getApplication().getId(), persistedApplicationId));
  }

  @Test
  public void addApplicationLanguage_EntitiesGetAddedToRelatedCollections() throws Exception {
    ApplicationLanguage language = new ApplicationLanguage("test");

    DeepThoughtApplication application = Application.getApplication();
    application.addApplicationLanguage(language);

    Assert.assertNotNull(language.getApplication());
    Assert.assertTrue(application.getApplicationLanguages().contains(language));
  }

}

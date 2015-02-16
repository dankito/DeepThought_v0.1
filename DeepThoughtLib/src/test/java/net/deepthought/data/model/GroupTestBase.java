package net.deepthought.data.model;

import net.deepthought.Application;
import net.deepthought.data.persistence.db.TableConfig;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

/**
 * Created by ganymed on 10/11/14.
 */
public abstract class GroupTestBase extends DataModelTestBase {

  @Test
  public void updateUniversallyUniqueId_UpdatedValueGetsPersistedInDb() throws Exception {
    DeepThoughtApplication application = Application.getApplication();
    Group group = new ArrayList<>(application.getGroups()).get(0);

    String newValue = "New value";
    group.setUniversallyUniqueId(newValue);

    Assert.assertEquals(newValue, getValueFromTable(TableConfig.GroupTableName, TableConfig.GroupUniversallyUniqueIdColumnName, group.getId()));
  }

  @Test
  public void updateName_UpdatedValueGetsPersistedInDb() throws Exception {
    DeepThoughtApplication application = Application.getApplication();
    Group group = new ArrayList<>(application.getGroups()).get(0);

    String newValue = "New value";
    group.setName(newValue);

    Assert.assertEquals(newValue, getValueFromTable(TableConfig.GroupTableName, TableConfig.GroupNameColumnName, group.getId()));
  }

  @Test
  public void updateDescription_UpdatedValueGetsPersistedInDb() throws Exception {
    DeepThoughtApplication application = Application.getApplication();
    Group group = new ArrayList<>(application.getGroups()).get(0);

    String newValue = "New value";
    group.setDescription(newValue);

    Assert.assertEquals(newValue, getValueFromTable(TableConfig.GroupTableName, TableConfig.GroupDescriptionColumnName, group.getId()));
  }


  @Test
  public void addDevice_RelationGetsPersisted() throws Exception {
    DeepThoughtApplication application = Application.getApplication();
    Group group = new ArrayList<>(application.getGroups()).get(0);

    Device device = new Device("unique", "test", "Linux of course");
    application.addDevice(device);

    group.addDevice(device);

    Assert.assertTrue(doesGroupDeviceJoinTableEntryExist(group.getId(), device.getId()));
  }

  @Test
  public void addDevice_EntitiesGetAddedToRelatedCollections() throws Exception {
    DeepThoughtApplication application = Application.getApplication();
    Group group = new ArrayList<>(application.getGroups()).get(0);

    Device device = new Device("unique", "test", "Linux of course");
    application.addDevice(device);

    group.addDevice(device);

    Assert.assertTrue(group.getDevices().contains(device));
    Assert.assertTrue(device.getGroups().contains(group));
  }

  @Test
  public void removeDevice_RelationGetsDeleted() throws Exception {
    DeepThoughtApplication application = Application.getApplication();
    Group group = new ArrayList<>(application.getGroups()).get(0);

    Device device = new Device("unique", "test", "Linux of course");
    application.addDevice(device);

    group.addDevice(device);

    group.removeDevice(device);

    Assert.assertFalse(doesGroupDeviceJoinTableEntryExist(group.getId(), device.getId()));

    Assert.assertFalse(group.isDeleted());
    Assert.assertFalse(device.isDeleted());
  }

  @Test
  public void removeDevice_EntitiesGetRemovedFromRelatedCollections() throws Exception {
    DeepThoughtApplication application = Application.getApplication();
    Group group = new ArrayList<>(application.getGroups()).get(0);

    Device device = new Device("unique", "test", "Linux of course");
    application.addDevice(device);

    group.addDevice(device);

    group.removeDevice(device);

    Assert.assertFalse(group.getDevices().contains(device));
    Assert.assertFalse(device.getGroups().contains(group));
  }


  @Test
  public void addUser_RelationGetsPersisted() throws Exception {
    DeepThoughtApplication application = Application.getApplication();
    Group group = new ArrayList<>(application.getGroups()).get(0);

    User user = new User("test");
    application.addUser(user);

    group.addUser(user);

    Assert.assertTrue(doesUserGroupJoinTableEntryExist(user.getId(), group.getId()));
  }

  @Test
  public void addUser_EntitiesGetAddedToRelatedCollections() throws Exception {
    DeepThoughtApplication application = Application.getApplication();
    Group group = new ArrayList<>(application.getGroups()).get(0);

    User user = new User("test");
    application.addUser(user);

    group.addUser(user);

    Assert.assertTrue(user.getGroups().contains(group));
    Assert.assertTrue(group.getUsers().contains(user));
  }

  @Test
  public void removeUser_RelationGetsDeleted() throws Exception {
    DeepThoughtApplication application = Application.getApplication();
    Group group = new ArrayList<>(application.getGroups()).get(0);

    User user = new User("test");
    application.addUser(user);

    group.addUser(user);

    group.removeUser(user);

    Assert.assertFalse(doesUserGroupJoinTableEntryExist(user.getId(), group.getId()));

    Assert.assertFalse(group.isDeleted());
    Assert.assertFalse(user.isDeleted());
  }

  @Test
  public void removeUser_EntitiesGetRemovedFromRelatedCollections() throws Exception {
    DeepThoughtApplication application = Application.getApplication();
    Group group = new ArrayList<>(application.getGroups()).get(0);

    User user = new User("test");
    application.addUser(user);

    group.addUser(user);

    group.removeUser(user);

    Assert.assertFalse(user.getGroups().contains(group));
    Assert.assertFalse(group.getUsers().contains(user));
  }


  protected boolean doesGroupDeviceJoinTableEntryExist(Long userId, Long groupId) {
    return doesJoinTableEntryExist(TableConfig.GroupDeviceJoinTableName, TableConfig.GroupDeviceJoinTableGroupIdColumnName, userId,
        TableConfig.GroupDeviceJoinTableDeviceIdColumnName, groupId);
  }

  protected boolean doesUserGroupJoinTableEntryExist(Long userId, Long groupId) {
    return doesJoinTableEntryExist(TableConfig.UserGroupJoinTableName, TableConfig.UserGroupJoinTableUserIdColumnName, userId,
        TableConfig.UserGroupJoinTableGroupIdColumnName, groupId);
  }


}

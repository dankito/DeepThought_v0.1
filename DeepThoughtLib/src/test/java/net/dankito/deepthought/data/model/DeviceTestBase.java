package net.dankito.deepthought.data.model;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.data.persistence.db.TableConfig;

import org.junit.Assert;
import org.junit.Test;

import java.sql.SQLException;

/**
 * Created by ganymed on 10/11/14.
 */
public abstract class DeviceTestBase extends DataModelTestBase {

  @Test
  public void updateUniversallyUniqueId_UpdatedValueGetsPersistedInDb() throws Exception {
    DeepThoughtApplication application = Application.getApplication();
    Device device = application.getLocalDevice();

    String newValue = "New value";
    device.setUniversallyUniqueId(newValue);

    Assert.assertEquals(newValue, getValueFromTable(TableConfig.DeviceTableName, TableConfig.DeviceUniversallyUniqueIdColumnName, device.getId()));
  }

  @Test
  public void updateName_UpdatedValueGetsPersistedInDb() throws Exception {
    DeepThoughtApplication application = Application.getApplication();
    Device device = application.getLocalDevice();

    String newValue = "New value";
    device.setName(newValue);

    Assert.assertEquals(newValue, getValueFromTable(TableConfig.DeviceTableName, TableConfig.DeviceNameColumnName, device.getId()));
  }

  @Test
  public void updateDescription_UpdatedValueGetsPersistedInDb() throws Exception {
    DeepThoughtApplication application = Application.getApplication();
    Device device = application.getLocalDevice();

    String newValue = "New value";
    device.setDescription(newValue);

    Assert.assertEquals(newValue, getValueFromTable(TableConfig.DeviceTableName, TableConfig.DeviceDescriptionColumnName, device.getId()));
  }

  @Test
  public void updateOsVersion_UpdatedValueGetsPersistedInDb() throws Exception {
    DeepThoughtApplication application = Application.getApplication();
    Device device = application.getLocalDevice();

    String newValue = "New value";
    device.setOsVersion(newValue);

    Assert.assertEquals(newValue, getValueFromTable(TableConfig.DeviceTableName, TableConfig.DeviceOsVersionColumnName, device.getId()));
  }

  @Test
  public void updateLastKnownIpAddress_UpdatedValueGetsPersistedInDb() throws Exception {
    DeepThoughtApplication application = Application.getApplication();
    Device device = application.getLocalDevice();

    String newValue = "New value";
    device.setLastKnownIpAddress(newValue);

    Assert.assertEquals(newValue, getValueFromTable(TableConfig.DeviceTableName, TableConfig.DeviceLastKnownIpColumnName, device.getId()));
  }


  @Test
  public void addUser_RelationGetsPersisted() throws Exception {
    DeepThoughtApplication application = Application.getApplication();
    Device device = application.getLocalDevice();

    User user = new User("test");
    application.addUser(user);

    device.addUser(user);

    Assert.assertTrue(doesUserDeviceJoinTableEntryExist(user.getId(), device.getId()));
  }

  @Test
  public void addUser_EntitiesGetAddedToRelatedCollections() throws Exception {
    DeepThoughtApplication application = Application.getApplication();
    Device device = application.getLocalDevice();

    User user = new User("test");
    application.addUser(user);

    device.addUser(user);

    Assert.assertTrue(user.getDevices().contains(device));
    Assert.assertTrue(device.getUsers().contains(user));
  }

  @Test
  public void removeUser_RelationGetsDeleted() throws Exception {
    DeepThoughtApplication application = Application.getApplication();
    Device device = application.getLocalDevice();

    User user = new User("test");
    application.addUser(user);

    device.addUser(user);

    device.removeUser(user);

    Assert.assertFalse(doesUserDeviceJoinTableEntryExist(user.getId(), device.getId()));

    Assert.assertFalse(device.isDeleted());
    Assert.assertFalse(user.isDeleted());
  }

  @Test
  public void removeUser_EntitiesGetRemovedFromRelatedCollections() throws Exception {
    DeepThoughtApplication application = Application.getApplication();
    Device device = application.getLocalDevice();

    User user = new User("test");
    application.addUser(user);

    device.addUser(user);

    device.removeUser(user);

    Assert.assertFalse(user.getDevices().contains(device));
    Assert.assertFalse(device.getUsers().contains(user));
  }


  @Test
  public void addGroup_RelationGetsPersisted() throws Exception {
    DeepThoughtApplication application = Application.getApplication();
    Device device = application.getLocalDevice();

    Group group = new Group("test");
    application.addGroup(group);

    device.addGroup(group);

    Assert.assertTrue(doesGroupDeviceJoinTableEntryExist(group.getId(), device.getId()));
  }

  @Test
  public void addGroup_EntitiesGetAddedToRelatedCollections() throws Exception {
    DeepThoughtApplication application = Application.getApplication();
    Device device = application.getLocalDevice();

    Group group = new Group("test");
    application.addGroup(group);

    device.addGroup(group);

    Assert.assertTrue(group.getDevices().contains(device));
    Assert.assertTrue(device.getGroups().contains(group));
  }

  @Test
  public void removeGroup_RelationGetsDeleted() throws Exception {
    DeepThoughtApplication application = Application.getApplication();
    Device device = application.getLocalDevice();

    Group group = new Group("test");
    application.addGroup(group);

    device.addGroup(group);

    device.removeGroup(group);

    Assert.assertFalse(doesGroupDeviceJoinTableEntryExist(group.getId(), device.getId()));

    Assert.assertFalse(device.isDeleted());
    Assert.assertFalse(group.isDeleted());
  }

  @Test
  public void removeGroup_EntitiesGetRemovedFromRelatedCollections() throws Exception {
    DeepThoughtApplication application = Application.getApplication();
    Device device = application.getLocalDevice();

    Group group = new Group("test");
    application.addGroup(group);

    device.addGroup(group);

    device.removeGroup(group);

    Assert.assertFalse(group.getDevices().contains(device));
    Assert.assertFalse(device.getGroups().contains(group));
  }


  protected boolean doesUserDeviceJoinTableEntryExist(String userId, String deviceId) throws SQLException {
    return doesJoinTableEntryExist(TableConfig.UserDeviceJoinTableName, TableConfig.UserDeviceJoinTableUserIdColumnName, userId,
        TableConfig.UserDeviceJoinTableDeviceIdColumnName, deviceId);
  }

  protected boolean doesGroupDeviceJoinTableEntryExist(String groupId, String deviceId) throws SQLException {
    return doesJoinTableEntryExist(TableConfig.GroupDeviceJoinTableName, TableConfig.GroupDeviceJoinTableGroupIdColumnName, groupId,
        TableConfig.GroupDeviceJoinTableDeviceIdColumnName, deviceId);
  }

}

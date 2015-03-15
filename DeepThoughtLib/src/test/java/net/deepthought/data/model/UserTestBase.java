package net.deepthought.data.model;

import net.deepthought.Application;
import net.deepthought.data.model.enums.ApplicationLanguage;
import net.deepthought.data.model.settings.SettingsBase;
import net.deepthought.data.model.settings.UserDeviceSettings;
import net.deepthought.data.persistence.db.TableConfig;

import org.junit.Assert;
import org.junit.Test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ganymed on 10/11/14.
 */
public abstract class UserTestBase extends DataModelTestBase {

  @Test
  public void updateUniversallyUniqueId_UpdatedValueGetsPersistedInDb() throws Exception {
    DeepThoughtApplication application = Application.getApplication();
    User user = application.getLastLoggedOnUser();

    String newValue = "New value";
    user.setUniversallyUniqueId(newValue);

    Assert.assertEquals(newValue, getValueFromTable(TableConfig.UserTableName, TableConfig.UserUniversallyUniqueIdColumnName, user.getId()));
  }

  @Test
  public void updateUserName_UpdatedValueGetsPersistedInDb() throws Exception {
    DeepThoughtApplication application = Application.getApplication();
    User user = application.getLastLoggedOnUser();

    String newValue = "New value";
    user.setUserName(newValue);

    Assert.assertEquals(newValue, getValueFromTable(TableConfig.UserTableName, TableConfig.UserUserNameColumnName, user.getId()));
  }

  @Test
  public void updateFirstName_UpdatedValueGetsPersistedInDb() throws Exception {
    DeepThoughtApplication application = Application.getApplication();
    User user = application.getLastLoggedOnUser();

    String newValue = "New value";
    user.setFirstName(newValue);

    Assert.assertEquals(newValue, getValueFromTable(TableConfig.UserTableName, TableConfig.UserFirstNameColumnName, user.getId()));
  }

  @Test
  public void updateLastName_UpdatedValueGetsPersistedInDb() throws Exception {
    DeepThoughtApplication application = Application.getApplication();
    User user = application.getLastLoggedOnUser();

    String newValue = "New value";
    user.setLastName(newValue);

    Assert.assertEquals(newValue, getValueFromTable(TableConfig.UserTableName, TableConfig.UserLastNameColumnName, user.getId()));
  }

  @Test
  public void updateSettings_UpdatedValueGetsPersistedInDb() throws Exception {
    DeepThoughtApplication application = Application.getApplication();
    User user = application.getLastLoggedOnUser();
    UserDeviceSettings settings = user.getSettings();

    String previousStoredSettings = getClobString(TableConfig.UserTableName, TableConfig.UserUserDeviceSettingsColumnName, user.getId());

    int newValue = 77542;
    settings.setMaxBackupsToKeep(newValue);

    String updatedValue = getClobString(TableConfig.UserTableName, TableConfig.UserUserDeviceSettingsColumnName, user.getId());
    Assert.assertNotEquals(previousStoredSettings, updatedValue);
  }

  @Test
  public void setSettingsWithMoreThan2048Characters_UpdatedValueGetsPersistedInDb() throws Exception {
    DeepThoughtApplication application = Application.getApplication();
    User user = application.getLastLoggedOnUser();
    UserDeviceSettings settings = user.getSettings();

    String previousStoredSettings = getClobString(TableConfig.UserTableName, TableConfig.UserUserDeviceSettingsColumnName, user.getId());

    user.setSettingsString(DataModelTestBase.StringWithMoreThan2048CharactersLength); // set internal settings string
    user.setFirstName("dummy"); // now set any User property to a new value so that user gets updated in Database

//    ApplicationLanguage newValue = new ApplicationLanguage(DataModelTestBase.StringWithMoreThan2048CharactersLength);
//    settings.setLanguage(newValue);

    String updatedValue = getClobString(TableConfig.UserTableName, TableConfig.UserUserDeviceSettingsColumnName, user.getId());
    Assert.assertNotEquals(previousStoredSettings, updatedValue);
    Assert.assertTrue(updatedValue.length() > 2048);
  }

  @Test
  public void updateSettingsLanguage_AfterDeserializationLanguageHasReallyBeenUpdated() throws Exception {
    DeepThoughtApplication application = Application.getApplication();
    User user = Application.getLoggedOnUser();
    UserDeviceSettings settings = user.getSettings();
    List<ApplicationLanguage> languages = new ArrayList<>(application.getApplicationLanguages());

    ApplicationLanguage newValue = languages.get(0);
    if(newValue.equals(settings.getLanguage()))
      newValue = languages.get(1);

    settings.setLanguage(newValue);

    UserDeviceSettings result = SettingsBase.createSettingsFromString(user.settingsString, UserDeviceSettings.class);

    Assert.assertNotNull(result);
    Assert.assertTrue(doIdsEqual(newValue.getId(), result.getLanguage().getId()));
  }


  @Test
  public void updateLastViewedDeepThought_UpdatedValueGetsPersistedInDb() throws Exception {
    DeepThoughtApplication application = Application.getApplication();
    User user = application.getLastLoggedOnUser();

    DeepThought newValue = new DeepThought();
    user.addDeepThought(newValue);
    user.setLastViewedDeepThought(newValue);

    Assert.assertTrue(doIdsEqual(newValue.getId(), getValueFromTable(TableConfig.UserTableName, TableConfig.UserLastViewedDeepThoughtColumnName, user.getId())));
  }


  @Test
  public void addDevice_RelationGetsPersisted() throws Exception {
    DeepThoughtApplication application = Application.getApplication();
    User user = application.getLastLoggedOnUser();

    Device device = new Device("unique", "test", "Linux of course");
    application.addDevice(device);

    user.addDevice(device);

    Assert.assertTrue(doesUserDeviceJoinTableEntryExist(user.getId(), device.getId()));
  }

  @Test
  public void addDevice_EntitiesGetAddedToRelatedCollections() throws Exception {
    DeepThoughtApplication application = Application.getApplication();
    User user = application.getLastLoggedOnUser();

    Device device = new Device("unique", "test", "Linux of course");
    application.addDevice(device);

    user.addDevice(device);

    Assert.assertTrue(user.getDevices().contains(device));
    Assert.assertTrue(device.getUsers().contains(user));
  }

  @Test
  public void removeDevice_RelationGetsDeleted() throws Exception {
    DeepThoughtApplication application = Application.getApplication();
    User user = application.getLastLoggedOnUser();

    Device device = new Device("unique", "test", "Linux of course");
    application.addDevice(device);

    user.addDevice(device);

    user.removeDevice(device);

    Assert.assertFalse(doesUserDeviceJoinTableEntryExist(user.getId(), device.getId()));

    Assert.assertFalse(user.isDeleted());
    Assert.assertFalse(device.isDeleted());
  }

  @Test
  public void removeDevice_EntitiesGetRemovedFromRelatedCollections() throws Exception {
    DeepThoughtApplication application = Application.getApplication();
    User user = application.getLastLoggedOnUser();

    Device device = new Device("unique", "test", "Linux of course");
    application.addDevice(device);

    user.addDevice(device);

    user.removeDevice(device);

    Assert.assertFalse(user.getDevices().contains(device));
    Assert.assertFalse(device.getUsers().contains(user));
  }


  @Test
  public void addGroup_RelationGetsPersisted() throws Exception {
    DeepThoughtApplication application = Application.getApplication();
    User user = application.getLastLoggedOnUser();

    Group group = new Group("test");
    application.addGroup(group);

    user.addGroup(group);

    Assert.assertTrue(doesUserGroupJoinTableEntryExist(user.getId(), group.getId()));
  }

  @Test
  public void addGroup_EntitiesGetAddedToRelatedCollections() throws Exception {
    DeepThoughtApplication application = Application.getApplication();
    User user = application.getLastLoggedOnUser();

    Group group = new Group("test");
    application.addGroup(group);

    user.addGroup(group);

    Assert.assertTrue(user.getGroups().contains(group));
    Assert.assertTrue(group.getUsers().contains(user));
  }

  @Test
  public void removeGroup_RelationGetsDeleted() throws Exception {
    DeepThoughtApplication application = Application.getApplication();
    User user = application.getLastLoggedOnUser();

    Group group = new Group("test");
    application.addGroup(group);

    user.addGroup(group);

    user.removeGroup(group);

    Assert.assertFalse(doesUserGroupJoinTableEntryExist(user.getId(), group.getId()));

    Assert.assertFalse(user.isDeleted());
    Assert.assertFalse(group.isDeleted());
  }

  @Test
  public void removeGroup_EntitiesGetRemovedFromRelatedCollections() throws Exception {
    DeepThoughtApplication application = Application.getApplication();
    User user = application.getLastLoggedOnUser();

    Group group = new Group("test");
    application.addGroup(group);

    user.addGroup(group);

    user.removeGroup(group);

    Assert.assertFalse(user.getGroups().contains(group));
    Assert.assertFalse(group.getUsers().contains(user));
  }


  protected boolean doesUserDeviceJoinTableEntryExist(Long userId, Long deviceId) throws SQLException {
    return doesJoinTableEntryExist(TableConfig.UserDeviceJoinTableName, TableConfig.UserDeviceJoinTableUserIdColumnName, userId,
        TableConfig.UserDeviceJoinTableDeviceIdColumnName, deviceId);
  }

  protected boolean doesUserGroupJoinTableEntryExist(Long userId, Long groupId) throws SQLException {
    return doesJoinTableEntryExist(TableConfig.UserGroupJoinTableName, TableConfig.UserGroupJoinTableUserIdColumnName, userId,
        TableConfig.UserGroupJoinTableGroupIdColumnName, groupId);
  }

}

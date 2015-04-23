package net.deepthought.data.model;

import net.deepthought.Application;
import net.deepthought.data.model.enums.ApplicationLanguage;
import net.deepthought.data.persistence.db.TableConfig;
import net.deepthought.data.persistence.db.UserDataEntity;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by ganymed on 16/12/14.
 */
public abstract class CreateDeepThoughtApplicationTestBase extends DataModelTestBase {

  @Test
  public void testLocalDeviceGetsCreatedCorrectly() throws Exception {
    DeepThoughtApplication application = Application.getApplication();

    Assert.assertNotNull(application.getLocalDevice());

    Assert.assertTrue(doIdsEqual(application.getLocalDevice().getId(), getValueFromTable(TableConfig.DeepThoughtApplicationTableName, TableConfig.DeepThoughtApplicationLocalDeviceJoinColumnName, application.getId())));
  }

  @Test
  public void testLastLoggedOnUserGetsCreatedCorrectly() throws Exception {
    DeepThoughtApplication application = Application.getApplication();

    Assert.assertNotNull(application.getLastLoggedOnUser());

    Assert.assertTrue(doIdsEqual(application.getLastLoggedOnUser().getId(), getValueFromTable(TableConfig.DeepThoughtApplicationTableName, TableConfig.DeepThoughtApplicationLastLoggedOnUserJoinColumnName, application.getId())));
  }

  @Test
  public void testDataModelVersionGetsCreatedCorrectly() throws Exception {
    DeepThoughtApplication application = Application.getApplication();

    Assert.assertTrue(application.getDataModelVersion() > 0);
  }

  @Test
  public void testDefaultUsersGetCreatedCorrectly() throws Exception {
    DeepThoughtApplication application = Application.getApplication();

    Assert.assertEquals(1, application.getUsers().size());

    for(User user : application.getUsers()) {
      Assert.assertNotNull(user.getId());
      Assert.assertNotNull(user.getVersion());
      Assert.assertFalse(user.isDeleted());
    }
  }

  @Test
  public void testDefaultGroupsGetCreatedCorrectly() throws Exception {
    DeepThoughtApplication application = Application.getApplication();

    Assert.assertEquals(1, application.getGroups().size());

    for(Group group : application.getGroups()) {
      testUserDataEntityFields(group);
    }
  }

  @Test
  public void testDefaultDevicesGetCreatedCorrectly() throws Exception {
    DeepThoughtApplication application = Application.getApplication();

    Assert.assertEquals(1, application.getDevices().size());

    for(Device device : application.getDevices()) {
      testUserDataEntityFields(device);
    }
  }

  @Test
  public void testDefaultApplicationLanguagesGetCreatedCorrectly() throws Exception {
    DeepThoughtApplication deepThoughtApplication = Application.getApplication();

    Assert.assertEquals(2, deepThoughtApplication.getApplicationLanguages().size());


    int index = 1;
    for(ApplicationLanguage language : deepThoughtApplication.getApplicationLanguages()) {
      testUserDataEntityFields(language);

      Assert.assertNotNull(language.getName());
      Assert.assertNotNull(language.getApplication());
      Assert.assertNull(language.getDeepThought());
      Assert.assertTrue(language.isSystemValue());
      Assert.assertFalse(language.isDeletable());

      Assert.assertEquals(index, language.getSortOrder());
      index++;
    }
  }


  protected void testUserDataEntityFields(UserDataEntity entity) {
    Assert.assertNotNull(entity.getId());
    Assert.assertNotNull(entity.getVersion());
    Assert.assertNotNull(entity.getCreatedBy());
    Assert.assertNotNull(entity.getModifiedBy());
    Assert.assertNotNull(entity.getOwner());
    Assert.assertFalse(entity.isDeleted());
  }

}

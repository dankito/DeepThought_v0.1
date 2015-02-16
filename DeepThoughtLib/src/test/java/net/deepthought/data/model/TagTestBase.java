package net.deepthought.data.model;

import net.deepthought.Application;
import net.deepthought.data.persistence.db.TableConfig;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by ganymed on 10/11/14.
 */
public abstract class TagTestBase extends DataModelTestBase {

  @Test
  public void updateName_UpdatedValueGetsPersistedInDb() throws Exception {
    Tag tag = new Tag("test");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addTag(tag);

    String newValue = "New value";
    tag.setName(newValue);

    Assert.assertEquals(newValue, getValueFromTable(TableConfig.TagTableName, TableConfig.TagNameColumnName, tag.getId()));
  }

  @Test
  public void updateDescription_UpdatedValueGetsPersistedInDb() throws Exception {
    Tag tag = new Tag("test", "No Description");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addTag(tag);

    String newValue = "Now set Description to something fancy";
    tag.setDescription(newValue);

    Assert.assertEquals(newValue, getValueFromTable(TableConfig.TagTableName, TableConfig.TagDescriptionColumnName, tag.getId()));
  }

}

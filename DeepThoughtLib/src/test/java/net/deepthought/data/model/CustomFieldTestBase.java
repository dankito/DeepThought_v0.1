package net.deepthought.data.model;

import net.deepthought.Application;
import net.deepthought.data.model.enums.CustomFieldName;
import net.deepthought.data.persistence.db.TableConfig;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by ganymed on 14/02/15.
 */
public abstract class CustomFieldTestBase extends DataModelTestBase {

  @Test
  public void updateName_UpdatedValueGetsPersistedInDb() throws Exception {
    Entry entry = new Entry("test");
    CustomFieldName customFieldName = new CustomFieldName("test");
    CustomField customField = new CustomField(customFieldName, "test");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntry(entry);
    deepThought.addCustomFieldName(customFieldName);

    entry.addCustomField(customField);

    CustomFieldName newValue = new CustomFieldName("updated value");
    deepThought.addCustomFieldName(newValue);
    customField.setName(newValue);

    Object actual = getValueFromTable(TableConfig.CustomFieldTableName, TableConfig.CustomFieldNameJoinColumnName, customField.getId());
    Assert.assertNotNull(actual);
    Assert.assertTrue(doIdsEqual(newValue.getId(), actual));
  }

  @Test
  public void updateName_PreviousNameTypeGetsNotDeletedFromDb() throws Exception {
    Entry entry = new Entry("test");
    CustomFieldName previousName = new CustomFieldName("test");
    CustomField customField = new CustomField(previousName, "test");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntry(entry);
    deepThought.addCustomFieldName(previousName);

    entry.addCustomField(customField);

    CustomFieldName newValue = new CustomFieldName("updated value");
    deepThought.addCustomFieldName(newValue);
    customField.setName(newValue);

    Assert.assertFalse(previousName.isDeleted());
    Assert.assertNotNull(previousName.getId());
  }

  @Test
  public void updateValue_UpdatedValueGetsPersistedInDb() throws Exception {
    Entry entry = new Entry("test");
    CustomFieldName customFieldName = new CustomFieldName("test");
    CustomField customField = new CustomField(customFieldName, "test");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntry(entry);
    deepThought.addCustomFieldName(customFieldName);

    entry.addCustomField(customField);

    String newValue = "updated value";
    customField.setValue(newValue);

    Object actual = getValueFromTable(TableConfig.CustomFieldTableName, TableConfig.CustomFieldValueColumnName, customField.getId());
    Assert.assertEquals(newValue, actual);
  }

  @Test
  public void updateOrder_UpdatedValueGetsPersistedInDb() throws Exception {
    Entry entry = new Entry("test");
    CustomFieldName customFieldName = new CustomFieldName("test");
    CustomField customField = new CustomField(customFieldName, "test");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntry(entry);
    deepThought.addCustomFieldName(customFieldName);

    entry.addCustomField(customField);

    int newValue = 42;
    customField.setOrder(newValue);

    Object actual = getValueFromTable(TableConfig.CustomFieldTableName, TableConfig.CustomFieldOrderColumnName, customField.getId());
    Assert.assertEquals(newValue, actual);
  }

}

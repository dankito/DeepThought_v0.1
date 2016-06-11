package net.dankito.deepthought.data.model.enums;

import net.dankito.deepthought.data.model.DataModelTestBase;
import net.dankito.deepthought.data.persistence.db.TableConfig;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by ganymed on 10/11/14.
 */
public abstract class ExtensibleEnumerationTestBase extends DataModelTestBase {

  protected abstract ExtensibleEnumeration getExistingExtensibleEnumeration();

  protected abstract String getEnumerationTableName();


  @Test
  public void updateName_UpdatedValueGetsPersistedInDb() throws Exception {
    ExtensibleEnumeration enumeration = getExistingExtensibleEnumeration();

    String newName = "New name";
    enumeration.setName(newName);

    Assert.assertEquals(newName, getValueFromTable(getEnumerationTableName(), TableConfig.ExtensibleEnumerationNameColumnName, enumeration.getId()));
  }

  @Test
  public void updateDescription_UpdatedValueGetsPersistedInDb() throws Exception {
    ExtensibleEnumeration enumeration = getExistingExtensibleEnumeration();

    String newValue = "New name";
    enumeration.setDescription(newValue);

    Assert.assertEquals(newValue, getValueFromTable(getEnumerationTableName(), TableConfig.ExtensibleEnumerationDescriptionColumnName, enumeration.getId()));
  }

  @Test
  public void updateSortOrder_UpdatedValueGetsPersistedInDb() throws Exception {
    ExtensibleEnumeration enumeration = getExistingExtensibleEnumeration();

    int newValue = 99;
    enumeration.setSortOrder(newValue);

    Assert.assertEquals(newValue, getValueFromTable(getEnumerationTableName(), TableConfig.ExtensibleEnumerationSortOrderColumnName, enumeration.getId()));
  }

}

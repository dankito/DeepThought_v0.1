package net.deepthought.data.model;

import net.deepthought.Application;
import net.deepthought.data.persistence.db.TableConfig;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by ganymed on 10/11/14.
 */
public abstract class IndexTermTestBase extends DataModelTestBase {

  @Test
  public void updateName_UpdatedNameGetsPersistedInDb() throws Exception {
    IndexTerm indexTerm = new IndexTerm("test");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addIndexTerm(indexTerm);

    String newName = "New name";
    indexTerm.setName(newName);

    // assert name really got written to database
    Assert.assertEquals(newName, getValueFromTable(TableConfig.IndexTermTableName, TableConfig.IndexTermNameColumnName, indexTerm.getId()));
  }

  @Test
  public void updateDescription_UpdatedDescriptionGetsPersistedInDb() throws Exception {
    IndexTerm indexTerm = new IndexTerm("test", "No Description");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addIndexTerm(indexTerm);

    String newDescription = "Now set Description to something fancy";
    indexTerm.setDescription(newDescription);

    // assert helpText really got written to database
    Assert.assertEquals(newDescription, getValueFromTable(TableConfig.IndexTermTableName, TableConfig.IndexTermDescriptionColumnName, indexTerm.getId()));
  }

}

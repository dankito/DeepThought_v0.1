package net.deepthought.data.model;

import net.deepthought.Application;
import net.deepthought.data.persistence.db.TableConfig;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by ganymed on 10/11/14.
 */
public abstract class PublisherTestBase extends DataModelTestBase {

  @Test
  public void updateName_UpdatedNameGetsPersistedInDb() throws Exception {
    Publisher person = new Publisher("test");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addPublisher(person);

    String newValue = "New name";
    person.setName(newValue);

    Assert.assertEquals(newValue, getValueFromTable(TableConfig.PublisherTableName, TableConfig.PublisherNameColumnName, person.getId()));
  }

  @Test
  public void updateNotes_UpdatedNameGetsPersistedInDb() throws Exception {
    Publisher person = new Publisher("test");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addPublisher(person);

    String newValue = "New note";
    person.setNotes(newValue);

    Assert.assertEquals(newValue, getValueFromTable(TableConfig.PublisherTableName, TableConfig.PublisherNotesColumnName, person.getId()));
  }

}

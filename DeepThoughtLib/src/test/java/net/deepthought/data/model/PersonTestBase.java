package net.deepthought.data.model;

import net.deepthought.Application;
import net.deepthought.data.persistence.db.TableConfig;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by ganymed on 10/11/14.
 */
public abstract class PersonTestBase extends DataModelTestBase {

  @Test
  public void updateFirstName_UpdatedNameGetsPersistedInDb() throws Exception {
    Person person = new Person("test", "test");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addPerson(person);

    String newValue = "New name";
    person.setFirstName(newValue);

    Assert.assertEquals(newValue, getValueFromTable(TableConfig.PersonTableName, TableConfig.PersonFirstNameColumnName, person.getId()));
  }

  @Test
  public void updateLastName_UpdatedNameGetsPersistedInDb() throws Exception {
    Person person = new Person("test", "test");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addPerson(person);

    String newValue = "New name";
    person.setLastName(newValue);

    Assert.assertEquals(newValue, getValueFromTable(TableConfig.PersonTableName, TableConfig.PersonLastNameColumnName, person.getId()));
  }

  @Test
  public void updateNotes_UpdatedNameGetsPersistedInDb() throws Exception {
    Person person = new Person("test", "test");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addPerson(person);

    String newValue = "New name";
    person.setNotes(newValue);

    Assert.assertEquals(newValue, getValueFromTable(TableConfig.PersonTableName, TableConfig.PersonNotesColumnName, person.getId()));
  }

}

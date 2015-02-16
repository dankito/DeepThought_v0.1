package net.deepthought.data.model;

import net.deepthought.Application;
import net.deepthought.data.model.enums.Gender;
import net.deepthought.data.persistence.db.TableConfig;

import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

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
  public void updateMiddleName_UpdatedNameGetsPersistedInDb() throws Exception {
    Person person = new Person("test", "test");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addPerson(person);

    String newValue = "New name";
    person.setMiddleNames(newValue);

    Assert.assertEquals(newValue, getValueFromTable(TableConfig.PersonTableName, TableConfig.PersonMiddleNamesColumnName, person.getId()));
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
  public void updateTitle_UpdatedNameGetsPersistedInDb() throws Exception {
    Person person = new Person("test", "test");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addPerson(person);

    String newValue = "New name";
    person.setTitle(newValue);

    Assert.assertEquals(newValue, getValueFromTable(TableConfig.PersonTableName, TableConfig.PersonTitleColumnName, person.getId()));
  }

  @Test
  public void updatePrefix_UpdatedNameGetsPersistedInDb() throws Exception {
    Person person = new Person("test", "test");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addPerson(person);

    String newValue = "New name";
    person.setPrefix(newValue);

    Assert.assertEquals(newValue, getValueFromTable(TableConfig.PersonTableName, TableConfig.PersonPrefixColumnName, person.getId()));
  }

  @Test
  public void updateSuffix_UpdatedNameGetsPersistedInDb() throws Exception {
    Person person = new Person("test", "test");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addPerson(person);

    String newValue = "New name";
    person.setSuffix(newValue);

    Assert.assertEquals(newValue, getValueFromTable(TableConfig.PersonTableName, TableConfig.PersonSuffixColumnName, person.getId()));
  }

  @Test
  public void updateAbbreviation_UpdatedNameGetsPersistedInDb() throws Exception {
    Person person = new Person("test", "test");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addPerson(person);

    String newValue = "New name";
    person.setAbbreviation(newValue);

    Assert.assertEquals(newValue, getValueFromTable(TableConfig.PersonTableName, TableConfig.PersonAbbreviationColumnName, person.getId()));
  }

  @Test
  public void updateBirthDate_UpdatedNameGetsPersistedInDb() throws Exception {
    Person person = new Person("test", "test");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addPerson(person);

    Date newValue = new Date();
    person.setBirthDate(newValue);

    Assert.assertEquals(newValue, getValueFromTable(TableConfig.PersonTableName, TableConfig.PersonBirthDayColumnName, person.getId()));
  }

  @Test
  public void updateGender_UpdatedNameGetsPersistedInDb() throws Exception {
    Person person = new Person("test", "test");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addPerson(person);

    Gender newValue = Gender.Female;
    person.setGender(newValue);

    Object actual = getValueFromTable(TableConfig.PersonTableName, TableConfig.PersonGenderColumnName, person.getId());
    Assert.assertEquals(newValue.ordinal(), actual);
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

  @Test
  public void updateSortBy_UpdatedNameGetsPersistedInDb() throws Exception {
    Person person = new Person("test", "test");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addPerson(person);

    String newValue = "New name";
    person.setSortBy(newValue);

    Assert.assertEquals(newValue, getValueFromTable(TableConfig.PersonTableName, TableConfig.PersonSortByColumnName, person.getId()));
  }

}

package net.deepthought.data.model;

import net.deepthought.Application;
import net.deepthought.data.model.enums.CustomFieldName;
import net.deepthought.data.model.enums.PersonRole;
import net.deepthought.data.persistence.db.TableConfig;

import org.junit.Assert;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * Created by ganymed on 10/11/14.
 */
public abstract class ReferenceBaseTestBase extends DataModelTestBase {

  protected abstract ReferenceBase createReferenceBaseInstanceAndAddToDeepThought();


  @Test
  public void updateTitle_UpdatedValueGetsPersistedInDb() throws Exception {
    ReferenceBase referenceBase = createReferenceBaseInstanceAndAddToDeepThought();

    String newValue = "New value";
    referenceBase.setTitle(newValue);

    Assert.assertEquals(newValue, getValueFromTable(TableConfig.ReferenceBaseTableName, TableConfig.ReferenceBaseTitleColumnName, referenceBase.getId()));
  }

  @Test
  public void updateSubTitle_UpdatedValueGetsPersistedInDb() throws Exception {
    ReferenceBase referenceBase = createReferenceBaseInstanceAndAddToDeepThought();

    String newValue = "New value";
    referenceBase.setSubTitle(newValue);

    Assert.assertEquals(newValue, getValueFromTable(TableConfig.ReferenceBaseTableName, TableConfig.ReferenceBaseSubTitleColumnName, referenceBase.getId()));
  }

  @Test
  public void updateAbstract_UpdatedValueGetsPersistedInDb() throws Exception {
    ReferenceBase referenceBase = createReferenceBaseInstanceAndAddToDeepThought();

    String newValue = "New value";
    referenceBase.setAbstract(newValue);

    Assert.assertEquals(newValue, getValueFromTable(TableConfig.ReferenceBaseTableName, TableConfig.ReferenceBaseAbstractColumnName, referenceBase.getId()));
  }

  @Test
  public void updateOnlineAddress_UpdatedValueGetsPersistedInDb() throws Exception {
    ReferenceBase referenceBase = createReferenceBaseInstanceAndAddToDeepThought();

    String newValue = "New value";
    referenceBase.setOnlineAddress(newValue);

    Assert.assertEquals(newValue, getValueFromTable(TableConfig.ReferenceBaseTableName, TableConfig.ReferenceBaseOnlineAddressColumnName, referenceBase.getId()));
  }

  @Test
  public void updateLastAccessDate_UpdatedValueGetsPersistedInDb() throws Exception {
    ReferenceBase referenceBase = createReferenceBaseInstanceAndAddToDeepThought();

    Date newValue = new Date();
    referenceBase.setLastAccessDate(newValue);

    Assert.assertEquals(newValue, getDateValueFromTable(TableConfig.ReferenceBaseTableName, TableConfig.ReferenceBaseLastAccessDateColumnName, referenceBase.getId()));
  }

  @Test
  public void updateNotes_UpdatedValueGetsPersistedInDb() throws Exception {
    ReferenceBase referenceBase = createReferenceBaseInstanceAndAddToDeepThought();

    String newValue = "New value";
    referenceBase.setNotes(newValue);

    Assert.assertEquals(newValue, getValueFromTable(TableConfig.ReferenceBaseTableName, TableConfig.ReferenceBaseNotesColumnName, referenceBase.getId()));
  }


  @Test
  public void addPerson_RelationGetsPersisted() throws Exception {
    ReferenceBase referenceBase = createReferenceBaseInstanceAndAddToDeepThought();
    Person person = new Person("Mahatma", "Gandhi");
    PersonRole role = new PersonRole("Hero");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addPerson(person);
    deepThought.addPersonRole(role);

    referenceBase.addPerson(person, role);

    Assert.assertTrue(doesReferenceBasePersonRoleJoinTableEntryExist(referenceBase.getId(), role.getId(), person.getId()));
  }

  @Test
  public void addPerson_EntitiesGetAddedToRelatedCollections() throws Exception {
    ReferenceBase referenceBase = createReferenceBaseInstanceAndAddToDeepThought();
    Person person = new Person("Mahatma", "Gandhi");
    PersonRole role = new PersonRole("Hero");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addPerson(person);
    deepThought.addPersonRole(role);

    referenceBase.addPerson(person, role);

    Assert.assertTrue(referenceBase.getPersonRoles().contains(role));
    Assert.assertTrue(referenceBase.getPersonsForRole(role).contains(person));

    if(referenceBase instanceof SeriesTitle) {
      Assert.assertTrue(person.getAssociatedSeries().contains(referenceBase));
      Assert.assertTrue(person.getRolesForSeries((SeriesTitle) referenceBase).contains(role));
    }
    else if(referenceBase instanceof Reference) {
      Assert.assertTrue(person.getAssociatedReferences().contains(referenceBase));
      Assert.assertTrue(person.getRolesForReference((Reference)referenceBase).contains(role));
    }
    else if(referenceBase instanceof ReferenceSubDivision) {
      Assert.assertTrue(person.getAssociatedReferenceSubDivisions().contains(referenceBase));
      Assert.assertTrue(person.getRolesForSubDivision((ReferenceSubDivision) referenceBase).contains(role));
    }

    Assert.assertTrue(role.getReferenceBases().contains(referenceBase));
    Assert.assertTrue(role.getReferenceBasesPersons().contains(person));
  }

  @Test
  public void addPersons_PersonOrderGetsSetCorrectly() throws Exception {
    ReferenceBase referenceBase = createReferenceBaseInstanceAndAddToDeepThought();
    Person person1 = new Person("Mahatma", "Gandhi");
    Person person2 = new Person("Nelson", "Mandela");
    Person person3 = new Person("Mother Teresa", "");
    PersonRole role = new PersonRole("Hero");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addPerson(person1);
    deepThought.addPerson(person2);
    deepThought.addPerson(person3);
    deepThought.addPersonRole(role);

    referenceBase.addPerson(person1, role);
    referenceBase.addPerson(person2, role);
    referenceBase.addPerson(person3, role);

    for(ReferenceBasePersonAssociation association : referenceBase.getReferenceBasePersonAssociations()) {
      if(association.getPerson().equals(person1))
        Assert.assertEquals(0, association.getPersonOrder());
      if(association.getPerson().equals(person2))
        Assert.assertEquals(1, association.getPersonOrder());
      if(association.getPerson().equals(person3))
        Assert.assertEquals(2, association.getPersonOrder());
    }
  }

  @Test
  public void addSecondPersonToRole_EntitiesGetAddedToRelatedCollections() throws Exception {
    ReferenceBase referenceBase = createReferenceBaseInstanceAndAddToDeepThought();
    Person firstPerson = new Person("Mahatma", "Gandhi");
    Person secondPerson = new Person("Mandela", "Nelson");
    PersonRole role = new PersonRole("Hero");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addPerson(firstPerson);
    deepThought.addPerson(secondPerson);
    deepThought.addPersonRole(role);

    referenceBase.addPerson(firstPerson, role);

    referenceBase.addPerson(secondPerson, role);

    Assert.assertTrue(referenceBase.getPersonsForRole(role).contains(secondPerson));

    if(referenceBase instanceof SeriesTitle) {
      Assert.assertTrue(secondPerson.getAssociatedSeries().contains(referenceBase));
      Assert.assertTrue(secondPerson.getRolesForSeries((SeriesTitle) referenceBase).contains(role));
    }
    else if(referenceBase instanceof Reference) {
      Assert.assertTrue(secondPerson.getAssociatedReferences().contains(referenceBase));
      Assert.assertTrue(secondPerson.getRolesForReference((Reference)referenceBase).contains(role));
    }
    else if(referenceBase instanceof ReferenceSubDivision) {
      Assert.assertTrue(secondPerson.getAssociatedReferenceSubDivisions().contains(referenceBase));
      Assert.assertTrue(secondPerson.getRolesForSubDivision((ReferenceSubDivision) referenceBase).contains(role));
    }

    Assert.assertTrue(role.getReferenceBasesPersons().contains(secondPerson));
  }

  @Test
  public void addPersonToSecondPersonRole_EntitiesGetAddedToRelatedCollections() throws Exception {
    ReferenceBase referenceBase = createReferenceBaseInstanceAndAddToDeepThought();
    Person person = new Person("Mahatma", "Gandhi");
    PersonRole firstRole = new PersonRole("Hero");
    PersonRole secondRole = new PersonRole("Model"); // Vorbild

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addPerson(person);
    deepThought.addPersonRole(firstRole);
    deepThought.addPersonRole(secondRole);

    referenceBase.addPerson(person, firstRole);

    referenceBase.addPerson(person, secondRole);

    Assert.assertTrue(referenceBase.getPersonRoles().contains(secondRole));
    Assert.assertTrue(referenceBase.getPersonsForRole(secondRole).contains(person));

    if(referenceBase instanceof SeriesTitle)
      Assert.assertTrue(person.getRolesForSeries((SeriesTitle) referenceBase).contains(secondRole));
    else if(referenceBase instanceof Reference)
      Assert.assertTrue(person.getRolesForReference((Reference) referenceBase).contains(secondRole));
    else if(referenceBase instanceof ReferenceSubDivision)
      Assert.assertTrue(person.getRolesForSubDivision((ReferenceSubDivision) referenceBase).contains(secondRole));

    Assert.assertTrue(secondRole.getReferenceBases().contains(referenceBase));
    Assert.assertTrue(secondRole.getReferenceBasesPersons().contains(person));
  }

  @Test
  public void removePerson_PersistedRelationGetsDeleted() throws Exception {
    ReferenceBase referenceBase = createReferenceBaseInstanceAndAddToDeepThought();
    Person person = new Person("Mahatma", "Gandhi");
    PersonRole role = new PersonRole("Hero");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addPerson(person);
    deepThought.addPersonRole(role);

    referenceBase.addPerson(person, role);

    referenceBase.removePerson(person, role);

    Assert.assertFalse(doesReferenceBasePersonRoleJoinTableEntryExist(referenceBase.getId(), role.getId(), person.getId()));

    Assert.assertFalse(referenceBase.isDeleted());
    Assert.assertFalse(person.isDeleted());
    Assert.assertFalse(role.isDeleted());
  }

  @Test
  public void removePerson_EntitiesGetRemovedFromRelatedCollections() throws Exception {
    ReferenceBase referenceBase = createReferenceBaseInstanceAndAddToDeepThought();
    Person person = new Person("Mahatma", "Gandhi");
    PersonRole role = new PersonRole("Hero");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addPerson(person);
    deepThought.addPersonRole(role);

    referenceBase.addPerson(person, role);

    referenceBase.removePerson(person, role);

    Assert.assertFalse(referenceBase.getPersonRoles().contains(role));

    if(referenceBase instanceof SeriesTitle)
      Assert.assertFalse(person.getAssociatedSeries().contains(referenceBase));
    else if(referenceBase instanceof Reference)
      Assert.assertFalse(person.getAssociatedReferences().contains(referenceBase));
    else if(referenceBase instanceof ReferenceSubDivision)
      Assert.assertFalse(person.getAssociatedReferenceSubDivisions().contains(referenceBase));

    Assert.assertFalse(role.getReferenceBases().contains(referenceBase));
    Assert.assertFalse(role.getReferenceBasesPersons().contains(person));
  }

  @Test
  public void removeFirstOfTwoPersons_EntitiesGetRemovedFromRelatedCollections() throws Exception {
    ReferenceBase referenceBase = createReferenceBaseInstanceAndAddToDeepThought();
    Person firstPerson = new Person("Mahatma", "Gandhi");
    Person secondPerson = new Person("Mandela", "Nelson");
    PersonRole role = new PersonRole("Hero");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addPerson(firstPerson);
    deepThought.addPerson(secondPerson);
    deepThought.addPersonRole(role);

    referenceBase.addPerson(firstPerson, role);

    referenceBase.addPerson(secondPerson, role);

    referenceBase.removePerson(firstPerson, role);

    Assert.assertFalse(referenceBase.getPersonsForRole(role).contains(firstPerson));
    Assert.assertTrue(referenceBase.getPersonsForRole(role).contains(secondPerson));

    if(referenceBase instanceof SeriesTitle) {
      Assert.assertFalse(firstPerson.getAssociatedSeries().contains(referenceBase));
      Assert.assertTrue(secondPerson.getAssociatedSeries().contains(referenceBase));
    }
    else if(referenceBase instanceof Reference) {
      Assert.assertFalse(firstPerson.getAssociatedReferences().contains(referenceBase));
      Assert.assertTrue(secondPerson.getAssociatedReferences().contains(referenceBase));
    }
    else if(referenceBase instanceof ReferenceSubDivision) {
      Assert.assertFalse(firstPerson.getAssociatedReferenceSubDivisions().contains(referenceBase));
      Assert.assertTrue(secondPerson.getAssociatedReferenceSubDivisions().contains(referenceBase));
    }

    Assert.assertFalse(role.getReferenceBasesPersons().contains(firstPerson));
    Assert.assertTrue(role.getReferenceBasesPersons().contains(secondPerson));
  }

  @Test
  public void removeFirstOfTwoPersonRoles_EntitiesGetRemovedFromRelatedCollections() throws Exception {
    ReferenceBase referenceBase = createReferenceBaseInstanceAndAddToDeepThought();
    Person person = new Person("Mahatma", "Gandhi");
    PersonRole firstRole = new PersonRole("Hero");
    PersonRole secondRole = new PersonRole("Model"); // Vorbild

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addPerson(person);
    deepThought.addPersonRole(firstRole);

    referenceBase.addPerson(person, firstRole);

    referenceBase.addPerson(person, secondRole);

    referenceBase.removePerson(person, firstRole);

    Assert.assertFalse(referenceBase.getPersonRoles().contains(firstRole));

    if(referenceBase instanceof SeriesTitle)
      Assert.assertFalse(person.getRolesForSeries((SeriesTitle)referenceBase).contains(firstRole));
    else if(referenceBase instanceof Reference)
      Assert.assertFalse(person.getRolesForReference((Reference)referenceBase).contains(firstRole));
    else if(referenceBase instanceof ReferenceSubDivision)
      Assert.assertFalse(person.getRolesForSubDivision((ReferenceSubDivision)referenceBase).contains(firstRole));

    Assert.assertFalse(firstRole.getReferenceBases().contains(referenceBase));
    Assert.assertFalse(firstRole.getReferenceBasesPersons().contains(person));
  }

  @Test
  public void removePersons_PersonOrderGetsAdjustedCorrectly() throws Exception {
    ReferenceBase referenceBase = createReferenceBaseInstanceAndAddToDeepThought();
    Person person1 = new Person("Mahatma", "Gandhi");
    Person person2 = new Person("Nelson", "Mandela");
    Person person3 = new Person("Mother Teresa", "");
    Person person4 = new Person("Edward", "Snowden");
    PersonRole role = new PersonRole("Hero");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addPerson(person1);
    deepThought.addPerson(person2);
    deepThought.addPerson(person3);
    deepThought.addPerson(person4);
    deepThought.addPersonRole(role);

    referenceBase.addPerson(person1, role);
    referenceBase.addPerson(person2, role);
    referenceBase.addPerson(person3, role);
    referenceBase.addPerson(person4, role);

    referenceBase.removePerson(person2, role);

    for(ReferenceBasePersonAssociation association : referenceBase.getReferenceBasePersonAssociations()) {
      if(association.getPerson().equals(person1))
        Assert.assertEquals(0, association.getPersonOrder());
      if(association.getPerson().equals(person3))
        Assert.assertEquals(1, association.getPersonOrder());
      if(association.getPerson().equals(person4))
        Assert.assertEquals(2, association.getPersonOrder());
    }
  }


  @Test
  public void addFile_RelationGetsPersisted() throws Exception {
    FileLink internetFileAttachment = new FileLink("http://img0.joyreactor.com/pics/post/demotivation-posters-auto-347958.jpeg");
    ReferenceBase referenceBase = createReferenceBaseInstanceAndAddToDeepThought();

    referenceBase.addFile(internetFileAttachment);

    // assert FileLink really got written to database
    Assert.assertTrue(isFileReferenceJoinTableValueSet(internetFileAttachment.getId(), referenceBase.getId()));
  }

  @Test
  public void addFile_EntitiesGetAddedToRelatedCollections() throws Exception {
    FileLink internetFileAttachment = new FileLink("http://img0.joyreactor.com/pics/post/demotivation-posters-auto-347958.jpeg");
    ReferenceBase referenceBase = createReferenceBaseInstanceAndAddToDeepThought();

    referenceBase.addFile(internetFileAttachment);

    Assert.assertEquals(1, referenceBase.getFiles().size());
  }

  @Test
  public void removeFile_RelationGetsDeleted() throws Exception {
    FileLink internetFileAttachment = new FileLink("http://img0.joyreactor.com/pics/post/demotivation-posters-auto-347958.jpeg");
    ReferenceBase referenceBase = createReferenceBaseInstanceAndAddToDeepThought();

    referenceBase.addFile(internetFileAttachment);

    referenceBase.removeFile(internetFileAttachment);

    // assert file really got deleted from database
    Assert.assertFalse(isFileReferenceJoinTableValueSet(internetFileAttachment.getId(), referenceBase.getId()));

    Assert.assertFalse(referenceBase.isDeleted());
    Assert.assertTrue(internetFileAttachment.isDeleted());
  }

  @Test
  public void removeFile_EntitiesGetRemovedFromRelatedCollections() throws Exception {
    FileLink internetFileAttachment = new FileLink("http://img0.joyreactor.com/pics/post/demotivation-posters-auto-347958.jpeg");
    ReferenceBase referenceBase = createReferenceBaseInstanceAndAddToDeepThought();

    referenceBase.addFile(internetFileAttachment);

    referenceBase.removeFile(internetFileAttachment);

    Assert.assertFalse(referenceBase.getFiles().contains(internetFileAttachment));
  }


  @Test
  public void addCustomField_RelationGetsPersisted() throws Exception {
    CustomFieldName customFieldName = new CustomFieldName("test");
    CustomField customField = new CustomField(customFieldName, "test");
    ReferenceBase referenceBase = createReferenceBaseInstanceAndAddToDeepThought();

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addCustomFieldName(customFieldName);

    referenceBase.addCustomField(customField);

    Assert.assertNotNull(customField.getId());
    Assert.assertTrue(doIdsEqual(referenceBase.getId(), getValueFromTable(TableConfig.CustomFieldTableName, TableConfig.CustomFieldReferenceBaseJoinColumnName, customField.getId())));
  }

  @Test
  public void addCustomField_EntitiesGetAddedToRelatedCollections() throws Exception {
    CustomFieldName customFieldName = new CustomFieldName("test");
    CustomField customField = new CustomField(customFieldName, "test");
    ReferenceBase referenceBase = createReferenceBaseInstanceAndAddToDeepThought();

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addCustomFieldName(customFieldName);

    referenceBase.addCustomField(customField);

    Assert.assertEquals(referenceBase, customField.getReferenceBase());
    Assert.assertTrue(referenceBase.getCustomFields().contains(customField));
    Assert.assertEquals(1, customField.getOrder());
  }

  @Test
  public void addMultipleCustomFields() throws Exception {
    CustomFieldName customFieldName = new CustomFieldName("test");
    ReferenceBase referenceBase = createReferenceBaseInstanceAndAddToDeepThought();

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addCustomFieldName(customFieldName);

    CustomField customField1 = new CustomField(customFieldName, "test1");
    referenceBase.addCustomField(customField1);
    CustomField customField2 = new CustomField(customFieldName, "test2");
    referenceBase.addCustomField(customField2);
    CustomField customField3 = new CustomField(customFieldName, "test3");
    referenceBase.addCustomField(customField3);
    CustomField customField4 = new CustomField(customFieldName, "test4");
    referenceBase.addCustomField(customField4);

    Assert.assertEquals(4, referenceBase.getCustomFields().size());

    int index = 0;
    for(CustomField customField : referenceBase.getCustomFields()) {
      Assert.assertEquals(referenceBase, customField.getReferenceBase());
      Assert.assertEquals(++index, customField.getOrder());
    }
  }

  @Test
  public void removeCustomField_RelationGetsDeleted() throws Exception {
    CustomFieldName customFieldName = new CustomFieldName("test");
    CustomField customField = new CustomField(customFieldName, "test");
    ReferenceBase referenceBase = createReferenceBaseInstanceAndAddToDeepThought();

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addCustomFieldName(customFieldName);

    referenceBase.addCustomField(customField);
    referenceBase.removeCustomField(customField);

    // assert CustomField really got deleted from database
    Assert.assertNull(getValueFromTable(TableConfig.CustomFieldTableName, TableConfig.CustomFieldReferenceBaseJoinColumnName, customField.getId()));

    Assert.assertFalse(referenceBase.isDeleted());
    Assert.assertFalse(customFieldName.isDeleted());
    Assert.assertTrue(customField.isDeleted());
  }

  @Test
  public void removeCustomField_EntitiesGetRemovedFromRelatedCollections() throws Exception {
    CustomFieldName customFieldName = new CustomFieldName("test");
    CustomField customField = new CustomField(customFieldName, "test");
    ReferenceBase referenceBase = createReferenceBaseInstanceAndAddToDeepThought();

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addCustomFieldName(customFieldName);

    referenceBase.addCustomField(customField);
    referenceBase.removeCustomField(customField);

    Assert.assertNull(customField.getReferenceBase());
    Assert.assertFalse(referenceBase.getCustomFields().contains(customField));
  }

  @Test
  public void addMultipleCustomFields_ThenRemove2_OrderGetsAdjustedCorrectly() throws Exception {
    CustomFieldName customFieldName = new CustomFieldName("test");
    ReferenceBase referenceBase = createReferenceBaseInstanceAndAddToDeepThought();

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addCustomFieldName(customFieldName);

    CustomField customField1 = new CustomField(customFieldName, "test1");
    referenceBase.addCustomField(customField1);
    CustomField customField2 = new CustomField(customFieldName, "test2");
    referenceBase.addCustomField(customField2);
    CustomField customField3 = new CustomField(customFieldName, "test3");
    referenceBase.addCustomField(customField3);
    CustomField customField4 = new CustomField(customFieldName, "test4");
    referenceBase.addCustomField(customField4);

    referenceBase.removeCustomField(customField1);
    referenceBase.removeCustomField(customField3);

    Assert.assertEquals(2, referenceBase.getCustomFields().size());

    Assert.assertEquals(1, customField2.getOrder());
    Assert.assertEquals(2, customField4.getOrder());

    Assert.assertNull(customField1.getReferenceBase());
    Assert.assertNull(customField3.getReferenceBase());
  }


  protected boolean doesReferenceBasePersonRoleJoinTableEntryExist(Long referenceBaseId, Long personRoleId, Long personId) throws SQLException {
    List<Object[]> result = entityManager.doNativeQuery("SELECT * FROM " + TableConfig.ReferenceBasePersonAssociationTableName + " WHERE " +
        TableConfig.ReferenceBasePersonAssociationReferenceBaseJoinColumnName +  "=" + referenceBaseId +
        " AND " + TableConfig.ReferenceBasePersonAssociationPersonJoinColumnName + "=" + personId +
        " AND " + TableConfig.ReferenceBasePersonAssociationPersonRoleJoinColumnName + "=" + personRoleId);
    return result.size() == 1;
  }

  protected boolean isFileReferenceJoinTableValueSet(Long fileId, Long referenceBaseId) throws SQLException {
    Object persistedgetReferenceBaseId = getValueFromTable(TableConfig.FileLinkTableName, TableConfig.FileLinkReferenceBaseJoinColumnName, fileId);
    return doIdsEqual(referenceBaseId, persistedgetReferenceBaseId);
  }

}

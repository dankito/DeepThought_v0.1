package net.deepthought.data.model;

import net.deepthought.Application;
import net.deepthought.data.persistence.db.TableConfig;

import org.junit.Assert;
import org.junit.Test;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by ganymed on 10/11/14.
 */
public abstract class ReferenceBaseTestBase extends DataModelTestBase {

  protected Class ReferenceBaseSubClassClass;

  protected abstract ReferenceBase createReferenceBaseInstanceAndAddToDeepThought();


  @Test
  public void addReferenceBaseSubClass_QueryForReferenceBase_SubClassGetsReturned() throws Exception {
    ReferenceBase referenceBase = createReferenceBaseInstanceAndAddToDeepThought();

    ReferenceBase returnedReferenceBase = entityManager.getEntityById(ReferenceBase.class, referenceBase.getId());

    Assert.assertEquals(ReferenceBaseSubClassClass, returnedReferenceBase.getClass());
  }

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

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addPerson(person);

    referenceBase.addPerson(person);

    Assert.assertTrue(doesReferenceBasePersonRoleJoinTableEntryExist(referenceBase.getId(), person.getId()));
  }

  @Test
  public void addPerson_EntitiesGetAddedToRelatedCollections() throws Exception {
    ReferenceBase referenceBase = createReferenceBaseInstanceAndAddToDeepThought();
    Person person = new Person("Mahatma", "Gandhi");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addPerson(person);

    referenceBase.addPerson(person);

    Assert.assertTrue(referenceBase.getPersons().contains(person));

    if(referenceBase instanceof SeriesTitle) {
      Assert.assertTrue(person.getAssociatedSeries().contains(referenceBase));
    }
    else if(referenceBase instanceof Reference) {
      Assert.assertTrue(person.getAssociatedReferences().contains(referenceBase));
    }
    else if(referenceBase instanceof ReferenceSubDivision) {
      Assert.assertTrue(person.getAssociatedReferenceSubDivisions().contains(referenceBase));
    }
  }

  @Test
  public void addPersons_PersonOrderGetsSetCorrectly() throws Exception {
    ReferenceBase referenceBase = createReferenceBaseInstanceAndAddToDeepThought();
    Person person1 = new Person("Mahatma", "Gandhi");
    Person person2 = new Person("Nelson", "Mandela");
    Person person3 = new Person("Mother Teresa", "");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addPerson(person1);
    deepThought.addPerson(person2);
    deepThought.addPerson(person3);

    referenceBase.addPerson(person1);
    referenceBase.addPerson(person2);
    referenceBase.addPerson(person3);

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
  public void addSecondPerson_EntitiesGetAddedToRelatedCollections() throws Exception {
    ReferenceBase referenceBase = createReferenceBaseInstanceAndAddToDeepThought();
    Person firstPerson = new Person("Mahatma", "Gandhi");
    Person secondPerson = new Person("Mandela", "Nelson");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addPerson(firstPerson);
    deepThought.addPerson(secondPerson);

    referenceBase.addPerson(firstPerson);

    referenceBase.addPerson(secondPerson);

    Assert.assertTrue(referenceBase.getPersons().contains(secondPerson));

    if(referenceBase instanceof SeriesTitle) {
      Assert.assertTrue(secondPerson.getAssociatedSeries().contains(referenceBase));
    }
    else if(referenceBase instanceof Reference) {
      Assert.assertTrue(secondPerson.getAssociatedReferences().contains(referenceBase));
    }
    else if(referenceBase instanceof ReferenceSubDivision) {
      Assert.assertTrue(secondPerson.getAssociatedReferenceSubDivisions().contains(referenceBase));
    }
  }

  @Test
  public void removePerson_PersistedRelationGetsDeleted() throws Exception {
    ReferenceBase referenceBase = createReferenceBaseInstanceAndAddToDeepThought();
    Person person = new Person("Mahatma", "Gandhi");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addPerson(person);

    referenceBase.addPerson(person);

    referenceBase.removePerson(person);

    Assert.assertFalse(doesReferenceBasePersonRoleJoinTableEntryExist(referenceBase.getId(), person.getId()));

    Assert.assertFalse(referenceBase.isDeleted());
    Assert.assertFalse(person.isDeleted());
  }

  @Test
  public void removePerson_EntitiesGetRemovedFromRelatedCollections() throws Exception {
    ReferenceBase referenceBase = createReferenceBaseInstanceAndAddToDeepThought();
    Person person = new Person("Mahatma", "Gandhi");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addPerson(person);

    referenceBase.addPerson(person);

    referenceBase.removePerson(person);

    if(referenceBase instanceof SeriesTitle)
      Assert.assertFalse(person.getAssociatedSeries().contains(referenceBase));
    else if(referenceBase instanceof Reference)
      Assert.assertFalse(person.getAssociatedReferences().contains(referenceBase));
    else if(referenceBase instanceof ReferenceSubDivision)
      Assert.assertFalse(person.getAssociatedReferenceSubDivisions().contains(referenceBase));
  }

  @Test
  public void removeFirstOfTwoPersons_EntitiesGetRemovedFromRelatedCollections() throws Exception {
    ReferenceBase referenceBase = createReferenceBaseInstanceAndAddToDeepThought();
    Person firstPerson = new Person("Mahatma", "Gandhi");
    Person secondPerson = new Person("Mandela", "Nelson");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addPerson(firstPerson);
    deepThought.addPerson(secondPerson);

    referenceBase.addPerson(firstPerson);

    referenceBase.addPerson(secondPerson);

    referenceBase.removePerson(firstPerson);

    Assert.assertFalse(referenceBase.getPersons().contains(firstPerson));
    Assert.assertTrue(referenceBase.getPersons().contains(secondPerson));

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
  }

  @Test
  public void removePersons_PersonOrderGetsAdjustedCorrectly() throws Exception {
    ReferenceBase referenceBase = createReferenceBaseInstanceAndAddToDeepThought();
    Person person1 = new Person("Mahatma", "Gandhi");
    Person person2 = new Person("Nelson", "Mandela");
    Person person3 = new Person("Mother Teresa", "");
    Person person4 = new Person("Edward", "Snowden");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addPerson(person1);
    deepThought.addPerson(person2);
    deepThought.addPerson(person3);
    deepThought.addPerson(person4);

    referenceBase.addPerson(person1);
    referenceBase.addPerson(person2);
    referenceBase.addPerson(person3);
    referenceBase.addPerson(person4);

    referenceBase.removePerson(person2);

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
  public void addAttachedFile_RelationGetsPersisted() throws Exception {
    FileLink internetFileAttachment = new FileLink("http://img0.joyreactor.com/pics/post/demotivation-posters-auto-347958.jpeg");
    Application.getDeepThought().addFile(internetFileAttachment);

    ReferenceBase referenceBase = createReferenceBaseInstanceAndAddToDeepThought();
    referenceBase.addAttachedFile(internetFileAttachment);

    // assert FileLink really got written to database
    Assert.assertTrue(doesReferenceBaseAttachedFileJoinTableEntryExist(referenceBase.getId(), internetFileAttachment.getId()));
  }

  @Test
  public void addAttachedFile_EntitiesGetAddedToRelatedCollections() throws Exception {
    FileLink internetFileAttachment = new FileLink("http://img0.joyreactor.com/pics/post/demotivation-posters-auto-347958.jpeg");
    Application.getDeepThought().addFile(internetFileAttachment);

    ReferenceBase referenceBase = createReferenceBaseInstanceAndAddToDeepThought();
    referenceBase.addAttachedFile(internetFileAttachment);

    Assert.assertEquals(1, referenceBase.getAttachedFiles().size());
    Assert.assertEquals(Application.getDeepThought(), internetFileAttachment.getDeepThought());
  }

  @Test
  public void removeAttachedFile_RelationGetsDeleted() throws Exception {
    FileLink internetFileAttachment = new FileLink("http://img0.joyreactor.com/pics/post/demotivation-posters-auto-347958.jpeg");
    Application.getDeepThought().addFile(internetFileAttachment);

    ReferenceBase referenceBase = createReferenceBaseInstanceAndAddToDeepThought();
    referenceBase.addAttachedFile(internetFileAttachment);

    referenceBase.removeAttachedFile(internetFileAttachment);

    // assert file really got deleted from database
    Assert.assertFalse(doesReferenceBaseAttachedFileJoinTableEntryExist(referenceBase.getId(), internetFileAttachment.getId()));

    Assert.assertFalse(referenceBase.isDeleted());
    Assert.assertFalse(internetFileAttachment.isDeleted());

    Application.getDeepThought().removeFile(internetFileAttachment);

    Assert.assertNull(internetFileAttachment.getDeepThought());
    Assert.assertTrue(internetFileAttachment.isDeleted());
  }

  @Test
  public void removeAttachedFile_EntitiesGetRemovedFromRelatedCollections() throws Exception {
    FileLink internetFileAttachment = new FileLink("http://img0.joyreactor.com/pics/post/demotivation-posters-auto-347958.jpeg");
    Application.getDeepThought().addFile(internetFileAttachment);

    ReferenceBase referenceBase = createReferenceBaseInstanceAndAddToDeepThought();
    referenceBase.addAttachedFile(internetFileAttachment);

    referenceBase.removeAttachedFile(internetFileAttachment);

    Assert.assertFalse(referenceBase.getAttachedFiles().contains(internetFileAttachment));
  }


  @Test
  public void addEmbeddedFile_RelationGetsPersisted() throws Exception {
    FileLink internetFileEmbedding = new FileLink("http://img0.joyreactor.com/pics/post/demotivation-posters-auto-347958.jpeg");
    Application.getDeepThought().addFile(internetFileEmbedding);

    ReferenceBase referenceBase = createReferenceBaseInstanceAndAddToDeepThought();
    referenceBase.addEmbeddedFile(internetFileEmbedding);

    // assert FileLink really got written to database
    Assert.assertTrue(doesReferenceBaseEmbeddedFileJoinTableEntryExist(referenceBase.getId(), internetFileEmbedding.getId()));
  }

  @Test
  public void addEmbeddedFile_EntitiesGetAddedToRelatedCollections() throws Exception {
    FileLink internetFileEmbedding = new FileLink("http://img0.joyreactor.com/pics/post/demotivation-posters-auto-347958.jpeg");
    Application.getDeepThought().addFile(internetFileEmbedding);

    ReferenceBase referenceBase = createReferenceBaseInstanceAndAddToDeepThought();
    referenceBase.addEmbeddedFile(internetFileEmbedding);

    Assert.assertEquals(1, referenceBase.getEmbeddedFiles().size());
    Assert.assertEquals(Application.getDeepThought(), internetFileEmbedding.getDeepThought());
  }

  @Test
  public void removeEmbeddedFile_RelationGetsDeleted() throws Exception {
    FileLink internetFileEmbedding = new FileLink("http://img0.joyreactor.com/pics/post/demotivation-posters-auto-347958.jpeg");
    Application.getDeepThought().addFile(internetFileEmbedding);

    ReferenceBase referenceBase = createReferenceBaseInstanceAndAddToDeepThought();
    referenceBase.addEmbeddedFile(internetFileEmbedding);

    referenceBase.removeEmbeddedFile(internetFileEmbedding);

    // assert file really got deleted from database
    Assert.assertFalse(doesReferenceBaseEmbeddedFileJoinTableEntryExist(referenceBase.getId(), internetFileEmbedding.getId()));

    Assert.assertFalse(referenceBase.isDeleted());
    Assert.assertFalse(internetFileEmbedding.isDeleted());

    Application.getDeepThought().removeFile(internetFileEmbedding);

    Assert.assertNull(internetFileEmbedding.getDeepThought());
    Assert.assertTrue(internetFileEmbedding.isDeleted());
  }

  @Test
  public void removeEmbeddedFile_EntitiesGetRemovedFromRelatedCollections() throws Exception {
    FileLink internetFileEmbedding = new FileLink("http://img0.joyreactor.com/pics/post/demotivation-posters-auto-347958.jpeg");
    Application.getDeepThought().addFile(internetFileEmbedding);

    ReferenceBase referenceBase = createReferenceBaseInstanceAndAddToDeepThought();
    referenceBase.addEmbeddedFile(internetFileEmbedding);

    referenceBase.removeEmbeddedFile(internetFileEmbedding);

    Assert.assertFalse(referenceBase.getEmbeddedFiles().contains(internetFileEmbedding));
  }


  @Test
  public void setPreviewImage_RelationGetsPersistedInDb() throws Exception {
    ReferenceBase referenceBase = createReferenceBaseInstanceAndAddToDeepThought();
    FileLink previewImage = new FileLink("/tmp/dummy.png");

    DeepThought deepThought = Application.getDeepThought();

    referenceBase.setPreviewImage(previewImage);

    // assert preview image really got written to database
    Assert.assertNotNull(previewImage.getId());
    Assert.assertTrue(doIdsEqual(previewImage.getId(), getValueFromTable(TableConfig.ReferenceBaseTableName, TableConfig.ReferenceBasePreviewImageJoinColumnName, referenceBase.getId())));
  }

  @Test
  public void updatePreviewImage_UpdatedValueGetsPersistedInDb() throws Exception {
    ReferenceBase referenceBase = createReferenceBaseInstanceAndAddToDeepThought();
    FileLink firstPreviewImage = new FileLink("/tmp/dummy.png");

    DeepThought deepThought = Application.getDeepThought();

    referenceBase.setPreviewImage(firstPreviewImage);

    FileLink secondPreviewImage = new FileLink("/tmp/PhotoOfTheYear.png");
    referenceBase.setPreviewImage(secondPreviewImage);

    // assert preview image really got updated in database
    Assert.assertTrue(doIdsEqual(secondPreviewImage.getId(), getValueFromTable(TableConfig.ReferenceBaseTableName, TableConfig.ReferenceBasePreviewImageJoinColumnName, referenceBase.getId())));
  }


  protected boolean doesReferenceBasePersonRoleJoinTableEntryExist(Long referenceBaseId, Long personId) throws SQLException {
    List<Object[]> result = entityManager.doNativeQuery("SELECT * FROM " + TableConfig.ReferenceBasePersonAssociationTableName + " WHERE " +
        TableConfig.ReferenceBasePersonAssociationReferenceBaseJoinColumnName + "=" + referenceBaseId +
        " AND " + TableConfig.ReferenceBasePersonAssociationPersonJoinColumnName + "=" + personId);
    return result.size() == 1;
  }

  protected boolean doesReferenceBaseAttachedFileJoinTableEntryExist(Long referenceBaseId, Long fileId) throws SQLException {
    return doesJoinTableEntryExist(TableConfig.ReferenceBaseAttachedFileJoinTableName, TableConfig.ReferenceBaseAttachedFileJoinTableReferenceBaseIdColumnName, referenceBaseId,
        TableConfig.ReferenceBaseAttachedFileJoinTableFileLinkIdColumnName, fileId);
  }

  protected boolean doesReferenceBaseEmbeddedFileJoinTableEntryExist(Long referenceBaseId, Long fileId) throws SQLException {
    return doesJoinTableEntryExist(TableConfig.ReferenceBaseEmbeddedFileJoinTableName, TableConfig.ReferenceBaseEmbeddedFileJoinTableReferenceBaseIdColumnName, referenceBaseId,
        TableConfig.ReferenceBaseEmbeddedFileJoinTableFileLinkIdColumnName, fileId);
  }

}

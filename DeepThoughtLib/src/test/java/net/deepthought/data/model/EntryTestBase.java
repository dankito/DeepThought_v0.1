package net.deepthought.data.model;

import net.deepthought.Application;
import net.deepthought.data.model.enums.Language;
import net.deepthought.data.model.enums.PersonRole;
import net.deepthought.data.persistence.IEntityManager;
import net.deepthought.data.persistence.db.TableConfig;

import org.junit.Assert;
import org.junit.Test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by ganymed on 10/11/14.
 */
public abstract class EntryTestBase extends DataModelTestBase {

  protected boolean doesCategoryEntryJoinTableEntryExist(Long categoryId, Long entryId) throws SQLException {
    return doesJoinTableEntryExist(TableConfig.CategoryEntryJoinTableName, TableConfig.CategoryEntryJoinTableCategoryIdColumnName, categoryId,
        TableConfig.CategoryEntryJoinTableEntryIdColumnName, entryId);
  }

  protected boolean doesEntryTagJoinTableEntryExist(Long entryId, Long tagId) throws SQLException {
    return doesJoinTableEntryExist(TableConfig.EntryTagJoinTableName, TableConfig.EntryTagJoinTableEntryIdColumnName, entryId,
        TableConfig.EntryTagJoinTableTagIdColumnName, tagId);
  }

  protected boolean doesEntryIndexTermJoinTableEntryExist(Long entryId, Long indexTermId) throws SQLException {
    return doesJoinTableEntryExist(TableConfig.EntryIndexTermJoinTableName, TableConfig.EntryIndexTermJoinTableEntryIdColumnName, entryId,
        TableConfig.EntryIndexTermJoinTableIndexTermIdColumnName, indexTermId);
  }

  protected boolean doesEntryPersonRoleJoinTableEntryExist(Long entryId, Long personRoleId, Long personId) throws SQLException {
    List<Object[]> result = entityManager.doNativeQuery("SELECT * FROM " + TableConfig.EntryPersonAssociationTableName + " WHERE " +
        TableConfig.EntryPersonAssociationEntryJoinColumnName +  "=" + entryId +
        " AND " + TableConfig.EntryPersonAssociationPersonJoinColumnName + "=" + personId +
        " AND " + TableConfig.EntryPersonAssociationPersonRoleJoinColumnName + "=" + personRoleId);
    return result.size() == 1;
//    return doesJoinTableEntryExist(TableConfig.EntryPersonRolesTableName, TableConfig.EntryPersonRolesEntryJoinColumnName, entryId,
//        TableConfig.EntryPersonRolesPersonRoleJoinColumnName, personId);
  }

  protected boolean isNoteEntryJoinColumnValueSet(Long noteId, Long entryId) throws SQLException {
    Object persistedEntryId = getValueFromTable(TableConfig.NoteTableName, TableConfig.NoteEntryJoinColumnName, noteId);
    return doIdsEqual(entryId, persistedEntryId);
  }

  protected boolean doesEntryGroupLinkJoinTableEntryExist(Long entryId, Long linkGroupId) throws SQLException {
    return doesJoinTableEntryExist(TableConfig.EntryEntriesLinkGroupJoinTableName, TableConfig.EntryEntriesLinkGroupJoinTableEntryIdColumnName, entryId,
        TableConfig.EntryEntriesLinkGroupJoinTableLinkGroupIdColumnName, linkGroupId);
  }

  protected boolean isFileEntryJoinTableValueSet(Long fileId, Long entryId) throws SQLException {
    Object persistedEntryId = getValueFromTable(TableConfig.FileLinkTableName, TableConfig.FileLinkEntryJoinColumnName, fileId);
    return doIdsEqual(entryId, persistedEntryId);
  }


  @Test
  public void addEntryToDeepThought_EntryGetsPersistedInDb() throws Exception {
    Entry entry = new Entry("test", "no content");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntry(entry);

    // assert entry really got written to database
    Assert.assertNotNull(entry.getId());
    Assert.assertNotNull(entry.getDeepThought());
  }

  @Test
  public void updateTitle_UpdatedTitleGetsPersistedInDb() throws Exception {
    Entry entry = new Entry("test", "no content");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntry(entry);

    String newTitle = "New title";
    entry.setTitle(newTitle);

    // assert title really got written to database
    Assert.assertEquals(newTitle, getValueFromTable(TableConfig.EntryTableName, TableConfig.EntryTitleColumnName, entry.getId()));
  }

  @Test
  public void updateAbstract_UpdatedAbstractGetsPersistedInDb() throws Exception {
    Entry entry = new Entry("test", "no content");
    entry.setAbstract("Default abstract");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntry(entry);

    String newAbstract = "Updated abstract";
    entry.setAbstract(newAbstract);

    // assert content really got written to database
//    Clob clob = (Clob)getClobFromTable(TableConfig.EntryTableName, TableConfig.EntryAbstractColumnName, entry.getId());
//    String actual = clob.getSubString(1, (int)clob.length());
    String actual = getClobFromTable(TableConfig.EntryTableName, TableConfig.EntryAbstractColumnName, entry.getId());
    Assert.assertEquals(newAbstract, actual);
  }

  @Test
  public void setAbstractWithMoreThan255Characters_UpdatedContentGetsPersistedInDb() throws Exception {
    Entry entry = new Entry("test", "");
    String abstractString = DataModelTestBase.StringWithMoreThan2048CharactersLength;

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntry(entry);

    entry.setAbstract(abstractString);

    // assert abstractString really got written to database
    String actual = getClobFromTable(TableConfig.EntryTableName, TableConfig.EntryAbstractColumnName, entry.getId());
    Assert.assertEquals(abstractString, actual);
  }

  @Test
  public void updateContent_UpdatedContentGetsPersistedInDb() throws Exception {
    Entry entry = new Entry("test", "no content");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntry(entry);

    String newContent = "New content";
    entry.setContent(newContent);

    // assert content really got written to database
    String actual = getClobFromTable(TableConfig.EntryTableName, TableConfig.EntryContentColumnName, entry.getId());
    Assert.assertEquals(newContent, actual);
  }

  @Test
  public void setContentWithMoreThan255Characters_UpdatedContentGetsPersistedInDb() throws Exception {
    Entry entry = new Entry("test", "");
    String content = DataModelTestBase.StringWithMoreThan2048CharactersLength;
    content = content + content + content + content + content + content + content + content + content + content + content + content + content + content + content + content + content;

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntry(entry);

    entry.setContent(content);

    // assert content really got written to database
    String actual = getClobFromTable(TableConfig.EntryTableName, TableConfig.EntryContentColumnName, entry.getId());
    Assert.assertEquals(content, actual);
  }

  @Test
  public void updateEntryIndex_UpdatedValueGetsPersistedInDb() throws Exception {
    Entry entry = new Entry("test", "no content");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntry(entry);

    int newValue = 1860;
    entry.setEntryIndex(newValue);

    // assert value really got written to database
    int actual = (int)getValueFromTable(TableConfig.EntryTableName, TableConfig.EntryEntryIndexColumnName, entry.getId());
    Assert.assertEquals(newValue, actual);
  }


  @Test
  public void addSubCategory_SubCategoryGetsPersisted() throws Exception {
    Entry entry = new Entry("test");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntry(entry);

    Entry subEntry = new Entry("sub");
    entry.addSubEntry(subEntry);

    // assert entries really got written to database
    Object[] queryResult = getRowFromTable(TableConfig.EntryTableName, subEntry.getId());
    Assert.assertNotEquals(0, queryResult.length);
  }

  @Test
  public void addSubCategory_RelationsGetSet() throws Exception {
    Entry entry = new Entry("test");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntry(entry);

    Entry subEntry = new Entry("sub");
    entry.addSubEntry(subEntry);

    Assert.assertNotNull(subEntry.getId());
    Assert.assertEquals(entry, subEntry.getParentEntry());
    Assert.assertTrue(entry.getSubEntries().contains(subEntry));
  }

  @Test
  public void removeSubCategory_SubCategoryGetsDeletedFromDB() throws Exception {
    Entry entry = new Entry("test");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntry(entry);

    Entry subEntry = new Entry("sub");
    entry.addSubEntry(subEntry);

    Long subEntryId = subEntry.getId();
    entry.removeSubEntry(subEntry);

    // assert categories really didn't get deleted from database
    Object[] queryResult = getRowFromTable(TableConfig.EntryTableName, subEntryId);
    Assert.assertNotEquals(0, queryResult.length);

    Assert.assertFalse(entry.isDeleted());
    Assert.assertTrue(subEntry.isDeleted());
  }

  @Test
  public void removeSubCategory_RelationsGetRemoved() throws Exception {
    Entry entry = new Entry("test");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntry(entry);

    Entry subEntry = new Entry("sub");
    entry.addSubEntry(subEntry);

    entry.removeSubEntry(subEntry);

    Assert.assertNotNull(subEntry.getId()); // subCategories is not a Composition -> subCategory stays in DB till it gets removed from DeepThought
    Assert.assertNull(subEntry.getParentEntry());
    Assert.assertFalse(entry.getSubEntries().contains(subEntry));
  }


  @Test
  public void add2EntriesToDeepThought_EntryIndicesGetCorrectlySet() throws Exception {
    DeepThought deepThought = Application.getDeepThought();

    Entry entry1 = new Entry("Test Entry 1", "Just for EntryIndex testing");
    Entry entry2 = new Entry("Test Entry 2", "Just for EntryIndex testing");

    deepThought.addEntry(entry1);
    deepThought.addEntry(entry2);

    Assert.assertEquals(1, entry1.getEntryIndex());
    Assert.assertEquals(2, entry2.getEntryIndex());
  }

  @Test
  public void addEntriesWithTagsToDeepThought_EntriesGetQueriedCorrectly() throws Exception {
    DeepThought deepThought = Application.getDeepThought();

    Entry entry1 = new Entry("Test Entry 1", "Just for EntryIndex testing");
    Entry entry2 = new Entry("Test Entry 2", "Just for EntryIndex testing");

    deepThought.addEntry(entry1);
    deepThought.addEntry(entry2);

    Tag tag1 = new Tag("one");
    Tag tag2 = new Tag("two");
    Tag tag3 = new Tag("three");

    deepThought.addTag(tag1);
    deepThought.addTag(tag2);
    deepThought.addTag(tag3);

    entry1.addTag(tag1);
    entry1.addTag(tag2);
    entry2.addTag(tag2);
    entry2.addTag(tag3);

    IEntityManager newEntityManager = getEntityManager(configuration);
//    List<DeepThought> queriedDeepThoughts = newEntityManager.getAllEntitiesOfType(DeepThought.class);
//    Assert.assertEquals(1, queriedDeepThoughts.size());
//
//    DeepThought queriedDeepThought = queriedDeepThoughts.get(0);
    DeepThought queriedDeepThought = newEntityManager.getEntityById(DeepThought.class, deepThought.getId());
    Assert.assertEquals(2, queriedDeepThought.getEntries().size());
    Assert.assertEquals(3, queriedDeepThought.getTags().size());

    List<String> tagNames = new ArrayList<>();
    for(Tag tag : queriedDeepThought.getTags()) {
      Assert.assertFalse(tagNames.contains(tag.getName()));
      tagNames.add(tag.getName());
    }
    Assert.assertEquals(3, tagNames.size());

    List<String> entry1TagNames = new ArrayList<>(Arrays.asList(new String[] { "one", "two" }));
    for(Tag tag : entry1.getTags()) {
      Assert.assertTrue(entry1TagNames.contains(tag.getName()));
      entry1TagNames.remove(tag.getName());
    }
    Assert.assertEquals(0, entry1TagNames.size());

    List<String> entry2TagNames = new ArrayList<>(Arrays.asList(new String[] { "two", "three" }));
    for(Tag tag : entry2.getTags()) {
      Assert.assertTrue(entry2TagNames.contains(tag.getName()));
      entry2TagNames.remove(tag.getName());
    }
    Assert.assertEquals(0, entry2TagNames.size());
  }


  @Test
  public void addCategory_RelationGetsPersisted() throws Exception {
    Category category = new Category("test");
    Entry entry = new Entry("test", "no content");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addCategory(category);
    deepThought.addEntry(entry);

    category.addEntry(entry);

    // assert Category - Entry JoinTable entry really got written to database
    Assert.assertTrue(doesCategoryEntryJoinTableEntryExist(category.getId(), entry.getId()));
  }

  @Test
  public void addCategory_EntitiesGetAddedToRelatedCollections() throws Exception {
    Category category = new Category("test");
    Entry entry = new Entry("test", "no content");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addCategory(category);
    deepThought.addEntry(entry);

    category.addEntry(entry);

    Assert.assertTrue(category.getEntries().contains(entry));
    Assert.assertTrue(entry.getCategories().contains(category));
  }

  @Test
  public void removeCategory_RelationGetsDeleted() throws Exception {
    Category category = new Category("test");
    Entry entry = new Entry("test", "no content");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addCategory(category);
    deepThought.addEntry(entry);

    category.addEntry(entry);
    category.removeEntry(entry);

    // assert entry really got deleted from database
    Assert.assertFalse(doesCategoryEntryJoinTableEntryExist(category.getId(), entry.getId()));

    Assert.assertFalse(entry.isDeleted());
    Assert.assertFalse(category.isDeleted());
  }

  @Test
  public void removeCategory_EntitiesGetRemovedFromRelatedCollections() throws Exception {
    Category category = new Category("test");
    Entry entry = new Entry("test", "no content");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addCategory(category);
    deepThought.addEntry(entry);
    category.addEntry(entry);

    category.removeEntry(entry);

    Assert.assertFalse(category.getEntries().contains(entry));
    Assert.assertFalse(entry.getCategories().contains(category));
  }


  @Test
  public void addEntryTo2Categories_JoinTableContainsBothEntries() throws Exception {
    Category category1 = new Category("Category 1");
    Category category2 = new Category("Category 2");
    Entry entry = new Entry("test", "no content");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addCategory(category1);
    deepThought.addCategory(category2);
    deepThought.addEntry(entry);

    category1.addEntry(entry);
    category2.addEntry(entry);

    // assert entry really got written to database
    List<Object> joinTableEntries = getJoinTableEntries(TableConfig.CategoryEntryJoinTableName, TableConfig.CategoryEntryJoinTableEntryIdColumnName, entry.getId(),
        TableConfig.CategoryEntryJoinTableCategoryIdColumnName);
    Assert.assertEquals(2, joinTableEntries.size());
    Assert.assertTrue(joinTableEntriesContainEntityId(category1.getId(), joinTableEntries));
    Assert.assertTrue(joinTableEntriesContainEntityId(category2.getId(), joinTableEntries));
  }

  @Test
  public void addEntryTo2Categories_RelationsGetSet() throws Exception {
    Category category1 = new Category("Category 1");
    Category category2 = new Category("Category 2");
    Entry entry = new Entry("test", "no content");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addCategory(category1);
    deepThought.addCategory(category2);
    deepThought.addEntry(entry);

    category1.addEntry(entry);
    category2.addEntry(entry);

    Assert.assertTrue(category1.getEntries().contains(entry));
    Assert.assertTrue(category2.getEntries().contains(entry));

    Assert.assertTrue(entry.getCategories().contains(category1));
    Assert.assertTrue(entry.getCategories().contains(category2));
  }

  @Test
  public void addEntryTo2Categories_RemoveFromOneCategory_JoinTableContainsOnlyThatCategory() throws Exception {
    Category category1 = new Category("Category 1");
    Category category2 = new Category("Category 2");
    Entry entry = new Entry("test", "no content");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addCategory(category1);
    deepThought.addCategory(category2);
    deepThought.addEntry(entry);

    category1.addEntry(entry);
    category2.addEntry(entry);

    category2.removeEntry(entry);

    // assert entry really got written to database
    List<Object> joinTableEntries = getJoinTableEntries(TableConfig.CategoryEntryJoinTableName, TableConfig.CategoryEntryJoinTableEntryIdColumnName, entry.getId(),
        TableConfig.CategoryEntryJoinTableCategoryIdColumnName);
    Assert.assertEquals(1, joinTableEntries.size());
    Assert.assertTrue(joinTableEntriesContainEntityId(category1.getId(), joinTableEntries));
    Assert.assertFalse(joinTableEntriesContainEntityId(category2.getId(), joinTableEntries));
  }

  @Test
  public void addEntryTo2Categories_RemoveFromOneCategory_RelationGetsRemoved() throws Exception {
    Category category1 = new Category("Category 1");
    Category category2 = new Category("Category 2");
    Entry entry = new Entry("test", "no content");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addCategory(category1);
    deepThought.addCategory(category2);
    deepThought.addEntry(entry);

    category1.addEntry(entry);
    category2.addEntry(entry);

    category2.removeEntry(entry);

    Assert.assertFalse(category2.getEntries().contains(entry));
    Assert.assertFalse(entry.getCategories().contains(category2));
  }
  

  @Test
  public void addTag_RelationGetsPersisted() throws Exception {
    Entry entry = new Entry("test", "no content");
    Tag tag = new Tag("test");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntry(entry);
    deepThought.addTag(tag);

    entry.addTag(tag);

    // assert entry really got written to database
    Assert.assertTrue(doesEntryTagJoinTableEntryExist(entry.getId(), tag.getId()));
  }

  @Test
  public void addTag_EntitiesGetAddedToRelatedCollections() throws Exception {
    Entry entry = new Entry("test", "no content");
    Tag tag = new Tag("test");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntry(entry);
    deepThought.addTag(tag);

    entry.addTag(tag);

    Assert.assertTrue(entry.getTags().contains(tag));
    Assert.assertTrue(tag.getEntries().contains(entry));
  }

  @Test
  public void removeTag_RelationGetsDeleted() throws Exception {
    Entry entry = new Entry("test", "no content");
    Tag tag = new Tag("test");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntry(entry);
    deepThought.addTag(tag);

    entry.addTag(tag);
    entry.removeTag(tag);

    // assert Join Table entry really got deleted from database
    Assert.assertFalse(doesEntryTagJoinTableEntryExist(entry.getId(), tag.getId()));

    // assert as well that entities itself didn't get deleted from db
    Assert.assertFalse(entry.isDeleted());
    Assert.assertFalse(tag.isDeleted());
  }

  @Test
  public void removeTag_EntitiesGetRemovedFromRelatedCollections() throws Exception {
    Entry entry = new Entry("test", "no content");
    Tag tag = new Tag("test");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntry(entry);
    deepThought.addTag(tag);
    entry.addTag(tag);

    entry.removeTag(tag);

    Assert.assertFalse(entry.getTags().contains(tag));
    Assert.assertFalse(tag.getEntries().contains(entry));
  }


  @Test
  public void add2TagsToEntry_JoinTableContainsBothTags() throws Exception {
    Entry entry = new Entry("test", "no content");
    Tag tag1 = new Tag("Tag 1");
    Tag tag2 = new Tag("Tag 2");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addTag(tag1);
    deepThought.addTag(tag2);
    deepThought.addEntry(entry);

    entry.addTag(tag1);
    entry.addTag(tag2);

    // assert entry really got written to database
    List<Object> joinTableEntries = getJoinTableEntries(TableConfig.EntryTagJoinTableName, TableConfig.EntryTagJoinTableEntryIdColumnName, entry.getId(),
        TableConfig.EntryTagJoinTableTagIdColumnName);
    Assert.assertEquals(2, joinTableEntries.size());
    Assert.assertTrue(joinTableEntriesContainEntityId(tag1.getId(), joinTableEntries));
    Assert.assertTrue(joinTableEntriesContainEntityId(tag2.getId(), joinTableEntries));
  }

  @Test
  public void add2TagsToEntry_RelationsGetSet() throws Exception {
    Entry entry = new Entry("test", "no content");
    Tag tag1 = new Tag("Tag 1");
    Tag tag2 = new Tag("Tag 2");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addTag(tag1);
    deepThought.addTag(tag2);
    deepThought.addEntry(entry);

    entry.addTag(tag1);
    entry.addTag(tag2);

    Assert.assertTrue(tag1.getEntries().contains(entry));
    Assert.assertTrue(tag2.getEntries().contains(entry));

    Assert.assertTrue(entry.getTags().contains(tag1));
    Assert.assertTrue(entry.getTags().contains(tag2));
  }

  @Test
  public void add2TagsToEntry_RemoveOneTag_JoinTableContainsOnlyOtherTag() throws Exception {
    Entry entry = new Entry("test", "no content");
    Tag tag1 = new Tag("Tag 1");
    Tag tag2 = new Tag("Tag 2");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addTag(tag1);
    deepThought.addTag(tag2);
    deepThought.addEntry(entry);

    entry.addTag(tag1);
    entry.addTag(tag2);

    entry.removeTag(tag1);

    // assert entry really got written to database
    List<Object> joinTableEntries = getJoinTableEntries(TableConfig.EntryTagJoinTableName, TableConfig.EntryTagJoinTableEntryIdColumnName, entry.getId(),
        TableConfig.EntryTagJoinTableTagIdColumnName);
    Assert.assertEquals(1, joinTableEntries.size());
    Assert.assertFalse(joinTableEntriesContainEntityId(tag1.getId(), joinTableEntries));
    Assert.assertTrue(joinTableEntriesContainEntityId(tag2.getId(), joinTableEntries));
  }

  @Test
  public void add2TagsToEntry_RemoveOneTag_RelationGetsRemoved() throws Exception {
    Entry entry = new Entry("test", "no content");
    Tag tag1 = new Tag("Tag 1");
    Tag tag2 = new Tag("Tag 2");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addTag(tag1);
    deepThought.addTag(tag2);
    deepThought.addEntry(entry);

    entry.addTag(tag1);
    entry.addTag(tag2);

    entry.removeTag(tag1);

    Assert.assertFalse(tag1.getEntries().contains(entry));
    Assert.assertFalse(entry.getTags().contains(tag1));
  }
  

  @Test
  public void addIndexTerm_RelationGetsPersisted() throws Exception {
    Entry entry = new Entry("test", "no content");
    IndexTerm indexTerm = new IndexTerm("test");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntry(entry);
    deepThought.addIndexTerm(indexTerm);

    entry.addIndexTerm(indexTerm);

    // assert entry really got written to database
    Assert.assertTrue(doesEntryIndexTermJoinTableEntryExist(entry.getId(), indexTerm.getId()));
  }

  @Test
  public void addIndexTerm_EntitiesGetAddedToRelatedCollections() throws Exception {
    Entry entry = new Entry("test", "no content");
    IndexTerm indexTerm = new IndexTerm("test");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntry(entry);
    deepThought.addIndexTerm(indexTerm);

    entry.addIndexTerm(indexTerm);

    Assert.assertTrue(entry.getIndexTerms().contains(indexTerm));
    Assert.assertTrue(indexTerm.getEntries().contains(entry));
  }

  @Test
  public void removeIndexTerm_RelationGetsDeleted() throws Exception {
    Entry entry = new Entry("test", "no content");
    IndexTerm indexTerm = new IndexTerm("test");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntry(entry);
    deepThought.addIndexTerm(indexTerm);

    entry.addIndexTerm(indexTerm);
    entry.removeIndexTerm(indexTerm);

    // assert entry really got deleted from database
    Assert.assertFalse(doesEntryIndexTermJoinTableEntryExist(entry.getId(), indexTerm.getId()));

    Assert.assertFalse(entry.isDeleted());
    Assert.assertFalse(indexTerm.isDeleted());
  }

  @Test
  public void removeIndexTerm_EntitiesGetRemovedFromRelatedCollections() throws Exception {
    Entry entry = new Entry("test", "no content");
    IndexTerm indexTerm = new IndexTerm("test");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntry(entry);
    deepThought.addIndexTerm(indexTerm);
    entry.addIndexTerm(indexTerm);

    entry.removeIndexTerm(indexTerm);

    Assert.assertFalse(entry.getIndexTerms().contains(indexTerm));
    Assert.assertFalse(indexTerm.getEntries().contains(entry));
  }


  @Test
  public void addPerson_RelationGetsPersisted() throws Exception {
    Entry entry = new Entry("test", "no content");
    Person person = new Person("Mahatma", "Gandhi");
    PersonRole role = new PersonRole("Hero");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntry(entry);
    deepThought.addPerson(person);
    deepThought.addPersonRole(role);

    entry.addPerson(person, role);

    Assert.assertTrue(doesEntryPersonRoleJoinTableEntryExist(entry.getId(), role.getId(), person.getId()));
  }

  @Test
  public void addPerson_EntitiesGetAddedToRelatedCollections() throws Exception {
    Entry entry = new Entry("test", "no content");
    Person person = new Person("Mahatma", "Gandhi");
    PersonRole role = new PersonRole("Hero");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntry(entry);
    deepThought.addPerson(person);
    deepThought.addPersonRole(role);

    entry.addPerson(person, role);

    Assert.assertTrue(entry.getPersonRoles().contains(role));
    Assert.assertTrue(entry.getPersonsForRole(role).contains(person));

    Assert.assertTrue(person.getAssociatedEntries().contains(entry));
    Assert.assertTrue(person.getRolesForEntry(entry).contains(role));

    Assert.assertTrue(role.getEntries().contains(entry));
    Assert.assertTrue(role.getEntriesPersons().contains(person));
  }

  @Test
  public void addPersons_PersonOrderGetsSetCorrectly() throws Exception {
    Entry entry = new Entry("test", "no content");
    Person person1 = new Person("Mahatma", "Gandhi");
    Person person2 = new Person("Nelson", "Mandela");
    Person person3 = new Person("Mother Teresa", "");
    PersonRole role = new PersonRole("Hero");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntry(entry);
    deepThought.addPerson(person1);
    deepThought.addPerson(person2);
    deepThought.addPerson(person3);
    deepThought.addPersonRole(role);

    entry.addPerson(person1, role);
    entry.addPerson(person2, role);
    entry.addPerson(person3, role);

    for(EntryPersonAssociation association : entry.getEntryPersonAssociations()) {
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
    Entry entry = new Entry("test", "no content");
    Person firstPerson = new Person("Mahatma", "Gandhi");
    Person secondPerson = new Person("Mandela", "Nelson");
    PersonRole role = new PersonRole("Hero");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntry(entry);
    deepThought.addPerson(firstPerson);
    deepThought.addPerson(secondPerson);
    deepThought.addPersonRole(role);

    entry.addPerson(firstPerson, role);

    entry.addPerson(secondPerson, role);

    Assert.assertTrue(entry.getPersonsForRole(role).contains(secondPerson));

    Assert.assertTrue(secondPerson.getAssociatedEntries().contains(entry));
    Assert.assertTrue(secondPerson.getRolesForEntry(entry).contains(role));

    Assert.assertTrue(role.getEntriesPersons().contains(secondPerson));
  }

  @Test
  public void addPersonToSecondPersonRole_EntitiesGetAddedToRelatedCollections() throws Exception {
    Entry entry = new Entry("test", "no content");
    Person person = new Person("Mahatma", "Gandhi");
    PersonRole firstRole = new PersonRole("Hero");
    PersonRole secondRole = new PersonRole("Model"); // Vorbild

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntry(entry);
    deepThought.addPerson(person);
    deepThought.addPersonRole(firstRole);
    deepThought.addPersonRole(secondRole);

    entry.addPerson(person, firstRole);

    entry.addPerson(person, secondRole);

    Assert.assertTrue(entry.getPersonRoles().contains(secondRole));
    Assert.assertTrue(entry.getPersonsForRole(secondRole).contains(person));

    Assert.assertTrue(person.getRolesForEntry(entry).contains(secondRole));

    Assert.assertTrue(secondRole.getEntries().contains(entry));
    Assert.assertTrue(secondRole.getEntriesPersons().contains(person));
  }

  @Test
  public void removePerson_PersistedRelationGetsDeleted() throws Exception {
    Entry entry = new Entry("test", "no content");
    Person person = new Person("Mahatma", "Gandhi");
    PersonRole role = new PersonRole("Hero");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntry(entry);
    deepThought.addPerson(person);
    deepThought.addPersonRole(role);

    entry.addPerson(person, role);

    entry.removePerson(person, role);

    Assert.assertFalse(doesEntryPersonRoleJoinTableEntryExist(entry.getId(), role.getId(), person.getId()));

    Assert.assertFalse(entry.isDeleted());
    Assert.assertFalse(person.isDeleted());
    Assert.assertFalse(role.isDeleted());
  }

  @Test
  public void removePerson_EntitiesGetRemovedFromRelatedCollections() throws Exception {
    Entry entry = new Entry("test", "no content");
    Person person = new Person("Mahatma", "Gandhi");
    PersonRole role = new PersonRole("Hero");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntry(entry);
    deepThought.addPerson(person);
    deepThought.addPersonRole(role);

    entry.addPerson(person, role);

    entry.removePerson(person, role);

    Assert.assertFalse(entry.getPersonRoles().contains(role));

    Assert.assertFalse(person.getAssociatedEntries().contains(entry));

    Assert.assertFalse(role.getEntries().contains(entry));
    Assert.assertFalse(role.getEntriesPersons().contains(person));
  }

  @Test
  public void removeFirstOfTwoPersons_EntitiesGetRemovedFromRelatedCollections() throws Exception {
    Entry entry = new Entry("test", "no content");
    Person firstPerson = new Person("Mahatma", "Gandhi");
    Person secondPerson = new Person("Mandela", "Nelson");
    PersonRole role = new PersonRole("Hero");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntry(entry);
    deepThought.addPerson(firstPerson);
    deepThought.addPerson(secondPerson);
    deepThought.addPersonRole(role);

    entry.addPerson(firstPerson, role);

    entry.addPerson(secondPerson, role);

    entry.removePerson(firstPerson, role);

    Assert.assertFalse(entry.getPersonsForRole(role).contains(firstPerson));
    Assert.assertTrue(entry.getPersonsForRole(role).contains(secondPerson));

    Assert.assertFalse(firstPerson.getAssociatedEntries().contains(entry));
    Assert.assertTrue(secondPerson.getAssociatedEntries().contains(entry));

    Assert.assertFalse(role.getEntriesPersons().contains(firstPerson));
    Assert.assertTrue(role.getEntriesPersons().contains(secondPerson));
  }

  @Test
  public void removeFirstOfTwoPersonRoles_EntitiesGetRemovedFromRelatedCollections() throws Exception {
    Entry entry = new Entry("test", "no content");
    Person person = new Person("Mahatma", "Gandhi");
    PersonRole firstRole = new PersonRole("Hero");
    PersonRole secondRole = new PersonRole("Model"); // Vorbild

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntry(entry);
    deepThought.addPerson(person);
    deepThought.addPersonRole(firstRole);
    deepThought.addPersonRole(secondRole);

    entry.addPerson(person, firstRole);

    entry.addPerson(person, secondRole);

    entry.removePerson(person, firstRole);

    Assert.assertFalse(entry.getPersonRoles().contains(firstRole));

    Assert.assertFalse(person.getRolesForEntry(entry).contains(firstRole));

    Assert.assertFalse(firstRole.getEntries().contains(entry));
    Assert.assertFalse(firstRole.getEntriesPersons().contains(person));
  }

  @Test
  public void removePersons_PersonOrderGetsAdjustedCorrectly() throws Exception {
    Entry entry = new Entry("test", "no content");
    Person person1 = new Person("Mahatma", "Gandhi");
    Person person2 = new Person("Nelson", "Mandela");
    Person person3 = new Person("Mother Teresa", "");
    Person person4 = new Person("Edward", "Snowden");
    PersonRole role = new PersonRole("Hero");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntry(entry);
    deepThought.addPerson(person1);
    deepThought.addPerson(person2);
    deepThought.addPerson(person3);
    deepThought.addPerson(person4);
    deepThought.addPersonRole(role);

    entry.addPerson(person1, role);
    entry.addPerson(person2, role);
    entry.addPerson(person3, role);
    entry.addPerson(person4, role);

    entry.removePerson(person2, role);

    for(EntryPersonAssociation association : entry.getEntryPersonAssociations()) {
      if(association.getPerson().equals(person1))
        Assert.assertEquals(0, association.getPersonOrder());
      if(association.getPerson().equals(person3))
        Assert.assertEquals(1, association.getPersonOrder());
      if(association.getPerson().equals(person4))
        Assert.assertEquals(2, association.getPersonOrder());
    }
  }


  @Test
  public void addNote_RelationGetsPersisted() throws Exception {
    Note note = new Note("Ich bin noch ein ganz wichtiger Kommentar");
    Entry entry = new Entry("test", "no content but commented already (like most stuff in internet)");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntry(entry);

    entry.addNote(note);

    // assert Note really got written to database
    Assert.assertTrue(isNoteEntryJoinColumnValueSet(note.getId(), entry.getId()));
  }

  @Test
  public void addNote_EntitiesGetAddedToRelatedCollections() throws Exception {
    Note note = new Note("Ich bin noch ein ganz wichtiger Kommentar");
    Entry entry = new Entry("test", "no content but commented already (like most stuff in internet)");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntry(entry);

    entry.addNote(note);

    Assert.assertEquals(1, entry.getNotes().size());
    Assert.assertEquals(entry, note.getEntry());
  }

  @Test
  public void removeNote_RelationGetsDeleted() throws Exception {
    Note note = new Note("Ich bin noch ein ganz wichtiger Kommentar");
    Entry entry = new Entry("test", "no content but commented already (like most stuff in internet)");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntry(entry);

    entry.addNote(note);

    entry.removeNote(note);

    // assert entry really got deleted from database
    Assert.assertFalse(isNoteEntryJoinColumnValueSet(note.getId(), entry.getId()));

    Assert.assertFalse(entry.isDeleted());
    Assert.assertTrue(note.isDeleted());
  }

  @Test
  public void removeNote_EntitiesGetRemovedFromRelatedCollections() throws Exception {
    Note note = new Note("Ich bin noch ein ganz wichtiger Kommentar");
    Entry entry = new Entry("test", "no content but commented already (like most stuff in internet)");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntry(entry);

    entry.addNote(note);

    entry.removeNote(note);

    Assert.assertFalse(entry.getNotes().contains(note));
    Assert.assertNull(note.getEntry());
  }


  @Test
  public void addLinkGroup_RelationGetsPersisted() throws Exception {
    EntriesLinkGroup linkGroup = new EntriesLinkGroup("Ich verbinde Welten (oder zumindest Entries)");
    Entry entry1 = new Entry("test 1", "no content");
    Entry entry2 = new Entry("test 2", "no content");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntry(entry1);
    deepThought.addEntry(entry2);

    entry1.addLinkGroup(linkGroup);
    entry2.addLinkGroup(linkGroup);

    // assert LinkGroup really got written to database
    Assert.assertTrue(doesEntryGroupLinkJoinTableEntryExist(entry1.getId(), linkGroup.getId()));
    Assert.assertTrue(doesEntryGroupLinkJoinTableEntryExist(entry2.getId(), linkGroup.getId()));
  }

  @Test
  public void addLinkGroup_EntitiesGetAddedToRelatedCollections() throws Exception {
    EntriesLinkGroup linkGroup = new EntriesLinkGroup("Ich verbinde Welten (oder zumindest Entries)");
    Entry entry1 = new Entry("test 1", "no content");
    Entry entry2 = new Entry("test 2", "no content");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntry(entry1);
    deepThought.addEntry(entry2);

    entry1.addLinkGroup(linkGroup);
    entry2.addLinkGroup(linkGroup);

    Assert.assertEquals(1, entry1.getLinkGroups().size());
    Assert.assertTrue(entry1.getLinkGroups().contains(linkGroup));
    Assert.assertEquals(1, entry2.getLinkGroups().size());
    Assert.assertTrue(entry2.getLinkGroups().contains(linkGroup));

    Assert.assertEquals(2, linkGroup.getEntries().size());
    Assert.assertTrue(linkGroup.getEntries().contains(entry1));
    Assert.assertTrue(linkGroup.getEntries().contains(entry2));
  }

  @Test
  public void removeLinkGroup_RelationGetsDeleted() throws Exception {
    EntriesLinkGroup linkGroup = new EntriesLinkGroup("Ich verbinde Welten (oder zumindest Entries)");
    Entry entry1 = new Entry("test 1", "no content");
    Entry entry2 = new Entry("test 2", "no content");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntry(entry1);
    deepThought.addEntry(entry2);

    entry1.addLinkGroup(linkGroup);
    entry2.addLinkGroup(linkGroup);

    entry1.removeLinkGroup(linkGroup);

    // assert entry really got deleted from database
    Assert.assertFalse(doesEntryGroupLinkJoinTableEntryExist(entry1.getId(), linkGroup.getId()));

    Assert.assertFalse(entry1.isDeleted());
    Assert.assertFalse(linkGroup.isDeleted());
  }

  @Test
  public void removeLinkGroup_EntitiesGetRemovedFromRelatedCollections() throws Exception {
    EntriesLinkGroup linkGroup = new EntriesLinkGroup("Ich verbinde Welten (oder zumindest Entries)");
    Entry entry1 = new Entry("test 1", "no content");
    Entry entry2 = new Entry("test 2", "no content");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntry(entry1);
    deepThought.addEntry(entry2);

    entry1.addLinkGroup(linkGroup);
    entry2.addLinkGroup(linkGroup);

    entry1.removeLinkGroup(linkGroup);

    Assert.assertEquals(0, entry1.getLinkGroups().size());
    Assert.assertFalse(entry1.getLinkGroups().contains(linkGroup));

    Assert.assertEquals(1, linkGroup.getEntries().size());
    Assert.assertFalse(linkGroup.getEntries().contains(entry1));
  }


  @Test
  public void addFile_RelationGetsPersisted() throws Exception {
    FileLink internetFileAttachment = new FileLink("http://img0.joyreactor.com/pics/post/demotivation-posters-auto-347958.jpeg");
    Entry entry = new Entry("test", "no content but a file link");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntry(entry);

    entry.addFile(internetFileAttachment);

    // assert FileLink really got written to database
    Assert.assertTrue(isFileEntryJoinTableValueSet(internetFileAttachment.getId(), entry.getId()));
  }

  @Test
  public void addFile_EntitiesGetAddedToRelatedCollections() throws Exception {
    FileLink internetFileAttachment = new FileLink("http://img0.joyreactor.com/pics/post/demotivation-posters-auto-347958.jpeg");
    Entry entry = new Entry("test", "no content but a file link");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntry(entry);

    entry.addFile(internetFileAttachment);

    Assert.assertEquals(1, entry.getFiles().size());
    Assert.assertEquals(entry, internetFileAttachment.getEntry());
  }

  @Test
  public void removeFile_RelationGetsDeleted() throws Exception {
    FileLink internetFileAttachment = new FileLink("http://img0.joyreactor.com/pics/post/demotivation-posters-auto-347958.jpeg");
    Entry entry = new Entry("test", "no content but a file link");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntry(entry);

    entry.addFile(internetFileAttachment);

    entry.removeFile(internetFileAttachment);

    // assert entry really got deleted from database
    Assert.assertFalse(isFileEntryJoinTableValueSet(internetFileAttachment.getId(), entry.getId()));

    Assert.assertFalse(entry.isDeleted());
    Assert.assertTrue(internetFileAttachment.isDeleted());
  }

  @Test
  public void removeFile_EntitiesGetRemovedFromRelatedCollections() throws Exception {
    FileLink internetFileAttachment = new FileLink("http://img0.joyreactor.com/pics/post/demotivation-posters-auto-347958.jpeg");
    Entry entry = new Entry("test", "no content but a file link");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntry(entry);

    entry.addFile(internetFileAttachment);

    entry.removeFile(internetFileAttachment);

    Assert.assertFalse(entry.getFiles().contains(internetFileAttachment));
    Assert.assertNull(internetFileAttachment.getEntry());
  }


  @Test
  public void setPreviewImage_RelationGetsPersistedInDb() throws Exception {
    Entry entry = new Entry("test", "no content");
    FileLink previewImage = new FileLink("/tmp/dummy.png");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntry(entry);

    entry.setPreviewImage(previewImage);

    // assert preview image really got written to database
    Assert.assertNotNull(previewImage.getId());
    Assert.assertTrue(doIdsEqual(previewImage.getId(), getValueFromTable(TableConfig.EntryTableName, TableConfig.EntryPreviewImageJoinColumnName, entry.getId())));
  }

  @Test
  public void updatePreviewImage_UpdatedValueGetsPersistedInDb() throws Exception {
    Entry entry = new Entry("test", "no content");
    FileLink firstPreviewImage = new FileLink("/tmp/dummy.png");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntry(entry);

    entry.setPreviewImage(firstPreviewImage);

    FileLink secondPreviewImage = new FileLink("/tmp/PhotoOfTheYear.png");
    entry.setPreviewImage(secondPreviewImage);

    // assert preview image really got updated in database
    Assert.assertTrue(doIdsEqual(secondPreviewImage.getId(), getValueFromTable(TableConfig.EntryTableName, TableConfig.EntryPreviewImageJoinColumnName, entry.getId())));
  }


  @Test
  public void setSeries_RelationGetsPersisted() throws Exception {
    SeriesTitle series = new SeriesTitle("agora42");
    Entry entry = new Entry("test", "no content");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntry(entry);
    deepThought.addSeriesTitle(series);

    entry.setSeries(series);

    Assert.assertTrue(doIdsEqual(series.getId(), getValueFromTable(TableConfig.EntryTableName, TableConfig.EntrySeriesTitleJoinColumnName, entry.getId())));
  }

  @Test
  public void setSeries_EntitiesGetAddedToRelatedCollections() throws Exception {
    SeriesTitle series = new SeriesTitle("agora42");
    Entry entry = new Entry("test", "no content");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntry(entry);
    deepThought.addSeriesTitle(series);

    entry.setSeries(series);

    Assert.assertEquals(series, entry.getSeries());
    Assert.assertTrue(series.getEntries().contains(entry));
  }

  @Test
  public void unsetSeries_RelationGetsDeleted() throws Exception {
    SeriesTitle series = new SeriesTitle("agora42");
    Entry entry = new Entry("test", "no content");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntry(entry);
    deepThought.addSeriesTitle(series);

    entry.setSeries(series);

    entry.setSeries(null);

    Assert.assertEquals(null, getValueFromTable(TableConfig.EntryTableName, TableConfig.EntrySeriesTitleJoinColumnName, entry.getId()));
  }

  @Test
  public void unsetSeries_EntitiesGetRemovedFromRelatedCollections() throws Exception {
    SeriesTitle series = new SeriesTitle("agora42");
    Entry entry = new Entry("test", "no content");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntry(entry);
    deepThought.addSeriesTitle(series);

    entry.setSeries(series);

    entry.setSeries(null);

    Assert.assertNull(entry.getSeries());
    Assert.assertFalse(series.getEntries().contains(entry));
  }

  @Test
  public void unsetSeriesWithReferenceSet_RelationGetsDeleted() throws Exception {
    Entry entry = new Entry("test", "no content");
    SeriesTitle series = new SeriesTitle("agora42");
    Reference reference = new Reference("Ist Freiheit in Ordnung?");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntry(entry);
    deepThought.addSeriesTitle(series);
    deepThought.addReference(reference);

    reference.setSeries(series);

    entry.setReference(reference);

    entry.setSeries(null);

    Assert.assertEquals(null, getValueFromTable(TableConfig.EntryTableName, TableConfig.EntrySeriesTitleJoinColumnName, entry.getId()));
    Assert.assertEquals(null, getValueFromTable(TableConfig.EntryTableName, TableConfig.EntryReferenceJoinColumnName, entry.getId()));
  }

  @Test
  public void unsetSeriesWithReferenceSet_EntitiesGetRemovedFromRelatedCollections() throws Exception {
    Entry entry = new Entry("test", "no content");
    SeriesTitle series = new SeriesTitle("agora42");
    Reference reference = new Reference("Ist Freiheit in Ordnung?");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntry(entry);
    deepThought.addSeriesTitle(series);
    deepThought.addReference(reference);

    reference.setSeries(series);

    entry.setReference(reference);

    entry.setSeries(null);

    Assert.assertNull(entry.getSeries());
    Assert.assertFalse(series.getEntries().contains(entry));

    Assert.assertNull(entry.getReference());
    Assert.assertFalse(reference.getEntries().contains(entry));
  }


  @Test
  public void setReference_RelationGetsPersisted() throws Exception {
    Reference reference = new Reference("Selbst denken");
    Entry entry = new Entry("test", "no content");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntry(entry);
    deepThought.addReference(reference);

    entry.setReference(reference);

    Assert.assertTrue(doIdsEqual(reference.getId(), getValueFromTable(TableConfig.EntryTableName, TableConfig.EntryReferenceJoinColumnName, entry.getId())));
  }

  @Test
  public void setReference_EntitiesGetAddedToRelatedCollections() throws Exception {
    Reference reference = new Reference("Selbst denken");
    Entry entry = new Entry("test", "no content");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntry(entry);
    deepThought.addReference(reference);

    entry.setReference(reference);

    Assert.assertEquals(reference, entry.getReference());
    Assert.assertTrue(reference.getEntries().contains(entry));
  }

  @Test
  public void setReferenceWithSeriesTitle_RelationGetsPersisted() throws Exception {
    Entry entry = new Entry("test", "no content");
    SeriesTitle series = new SeriesTitle("agora42");
    Reference reference = new Reference("Ist Freiheit in Ordnung?");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntry(entry);
    deepThought.addSeriesTitle(series);
    deepThought.addReference(reference);

    reference.setSeries(series);

    entry.setReference(reference);

    Assert.assertTrue(doIdsEqual(reference.getId(), getValueFromTable(TableConfig.EntryTableName, TableConfig.EntryReferenceJoinColumnName, entry.getId())));
    Assert.assertTrue(doIdsEqual(series.getId(), getValueFromTable(TableConfig.EntryTableName, TableConfig.EntrySeriesTitleJoinColumnName, entry.getId())));
  }

  @Test
  public void setReferenceWithSeriesTitle_EntitiesGetAddedToRelatedCollections() throws Exception {
    Entry entry = new Entry("test", "no content");
    SeriesTitle series = new SeriesTitle("agora42");
    Reference reference = new Reference("Ist Freiheit in Ordnung?");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntry(entry);
    deepThought.addSeriesTitle(series);
    deepThought.addReference(reference);

    reference.setSeries(series);

    entry.setReference(reference);

    Assert.assertEquals(reference, entry.getReference());
    Assert.assertTrue(reference.getEntries().contains(entry));

    Assert.assertEquals(series, entry.getSeries());
    Assert.assertTrue(series.getEntries().contains(entry));
  }

  @Test
  public void unsetReference_RelationGetsDeleted() throws Exception {
    Reference reference = new Reference("Selbst denken");
    Entry entry = new Entry("test", "no content");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntry(entry);
    deepThought.addReference(reference);

    entry.setReference(reference);

    entry.setReference(null);

    Assert.assertEquals(null, getValueFromTable(TableConfig.EntryTableName, TableConfig.EntryReferenceJoinColumnName, entry.getId()));
  }

  @Test
  public void unsetReference_EntitiesGetRemovedFromRelatedCollections() throws Exception {
    Reference reference = new Reference("Selbst denken");
    Entry entry = new Entry("test", "no content");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntry(entry);
    deepThought.addReference(reference);

    entry.setReference(reference);

    entry.setReference(null);

    Assert.assertNull(entry.getReference());
    Assert.assertFalse(reference.getEntries().contains(entry));
  }

  @Test
  public void unsetReferenceWithReferenceSubDivisionSet_RelationGetsDeleted() throws Exception {
    Entry entry = new Entry("test", "no content");
    Reference reference = new Reference("Selbst denken");
    ReferenceSubDivision subDivision = new ReferenceSubDivision("Kapitel 1");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntry(entry);
    deepThought.addReference(reference);

    reference.addSubDivision(subDivision);

    entry.setReferenceSubDivision(subDivision);

    entry.setReference(null);

    Assert.assertEquals(null, getValueFromTable(TableConfig.EntryTableName, TableConfig.EntryReferenceJoinColumnName, entry.getId()));
    Assert.assertEquals(null, getValueFromTable(TableConfig.EntryTableName, TableConfig.EntryReferenceSubDivisionJoinColumnName, entry.getId()));
  }

  @Test
  public void unsetReferenceWithReferenceSubDivisionSet_EntitiesGetRemovedFromRelatedCollections() throws Exception {
    Entry entry = new Entry("test", "no content");
    Reference reference = new Reference("Selbst denken");
    ReferenceSubDivision subDivision = new ReferenceSubDivision("Kapitel 1");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntry(entry);
    deepThought.addReference(reference);

    reference.addSubDivision(subDivision);

    entry.setReferenceSubDivision(subDivision);

    entry.setReference(null);

    Assert.assertNull(entry.getReference());
    Assert.assertFalse(reference.getEntries().contains(entry));

    Assert.assertNull(entry.getReferenceSubDivision());
    Assert.assertFalse(subDivision.getEntries().contains(entry));
  }


  @Test
  public void setReferenceSubDivision_RelationGetsPersisted() throws Exception {
    Reference reference = new Reference("Selbst denken");
    ReferenceSubDivision subDivision = new ReferenceSubDivision("Kapitel 1");
    Entry entry = new Entry("test", "no content");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntry(entry);
    deepThought.addReference(reference);

    entry.setReference(reference);

    reference.addSubDivision(subDivision);
    entry.setReferenceSubDivision(subDivision);

    Assert.assertTrue(doIdsEqual(subDivision.getId(), getValueFromTable(TableConfig.EntryTableName, TableConfig.EntryReferenceSubDivisionJoinColumnName, entry.getId())));
  }

  @Test
  public void setReferenceSubDivision_EntitiesGetAddedToRelatedCollections() throws Exception {
    Reference reference = new Reference("Selbst denken");
    ReferenceSubDivision subDivision = new ReferenceSubDivision("Kapitel 1");
    Entry entry = new Entry("test", "no content");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntry(entry);
    deepThought.addReference(reference);

    entry.setReference(reference);

    reference.addSubDivision(subDivision);
    entry.setReferenceSubDivision(subDivision);

    Assert.assertEquals(subDivision, entry.getReferenceSubDivision());
    Assert.assertTrue(subDivision.getEntries().contains(entry));
  }

  @Test
  public void setReferenceSubDivisionWithReference_RelationGetsPersisted() throws Exception {
    Entry entry = new Entry("test", "no content");
    Reference reference = new Reference("Selbst denken");
    ReferenceSubDivision subDivision = new ReferenceSubDivision("Kapitel 1");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntry(entry);
    deepThought.addReference(reference);

    reference.addSubDivision(subDivision);

    entry.setReferenceSubDivision(subDivision);

    Assert.assertTrue(doIdsEqual(reference.getId(), getValueFromTable(TableConfig.EntryTableName, TableConfig.EntryReferenceJoinColumnName, entry.getId())));
    Assert.assertTrue(doIdsEqual(subDivision.getId(), getValueFromTable(TableConfig.EntryTableName, TableConfig.EntryReferenceSubDivisionJoinColumnName, entry.getId())));
  }

  @Test
  public void setReferenceSubDivisionWithReference_EntitiesGetAddedToRelatedCollections() throws Exception {
    Entry entry = new Entry("test", "no content");
    Reference reference = new Reference("Selbst denken");
    ReferenceSubDivision subDivision = new ReferenceSubDivision("Kapitel 1");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntry(entry);
    deepThought.addReference(reference);

    reference.addSubDivision(subDivision);

    entry.setReferenceSubDivision(subDivision);

    Assert.assertEquals(reference, entry.getReference());
    Assert.assertTrue(reference.getEntries().contains(entry));

    Assert.assertEquals(subDivision, entry.getReferenceSubDivision());
    Assert.assertTrue(subDivision.getEntries().contains(entry));
  }

  @Test
  public void unsetReferenceSubDivision_RelationGetsDeleted() throws Exception {
    Reference reference = new Reference("Selbst denken");
    ReferenceSubDivision subDivision = new ReferenceSubDivision("Kapitel 1");
    Entry entry = new Entry("test", "no content");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntry(entry);
    deepThought.addReference(reference);

    entry.setReference(reference);

    reference.addSubDivision(subDivision);
    entry.setReferenceSubDivision(subDivision);

    entry.setReferenceSubDivision(null);

    Assert.assertEquals(null, getValueFromTable(TableConfig.EntryTableName, TableConfig.EntryReferenceSubDivisionJoinColumnName, entry.getId()));
  }

  @Test
  public void unsetReferenceSubDivision_EntitiesGetRemovedFromRelatedCollections() throws Exception {
    Reference reference = new Reference("Selbst denken");
    ReferenceSubDivision subDivision = new ReferenceSubDivision("Kapitel 1");
    Entry entry = new Entry("test", "no content");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntry(entry);
    deepThought.addReference(reference);

    entry.setReference(reference);

    reference.addSubDivision(subDivision);
    entry.setReferenceSubDivision(subDivision);

    entry.setReferenceSubDivision(null);

    Assert.assertNull(entry.getReferenceSubDivision());
    Assert.assertFalse(subDivision.getEntries().contains(entry));
  }

  @Test
  public void updateReferenceIndication_UpdatedValueGetsPersistedInDb() throws Exception {
    Entry entry = new Entry("test", "no content");
    entry.setIndication("41");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntry(entry);

    String newReferenceIndication = "42"; // the Application is called after all DeepThought
    entry.setIndication(newReferenceIndication);

    Assert.assertEquals(newReferenceIndication, getValueFromTable(TableConfig.EntryTableName, TableConfig.EntryIndicationColumnName, entry.getId()).toString());
  }


  @Test
  public void updateLanguage_UpdatedValueGetsPersistedInDb() throws Exception {
    Entry entry = new Entry("test", "no content");

    DeepThought deepThought = Application.getDeepThought();
    List<Language> languages = new ArrayList<>(deepThought.getLanguages());
    entry.setLanguage(languages.get(0));
    deepThought.addEntry(entry);

    Language newLanguage = languages.get(1);
    entry.setLanguage(newLanguage);

    Assert.assertTrue(doIdsEqual(newLanguage.getId(), getValueFromTable(TableConfig.EntryTableName, TableConfig.EntryLanguageJoinColumnName, entry.getId())));
  }

}

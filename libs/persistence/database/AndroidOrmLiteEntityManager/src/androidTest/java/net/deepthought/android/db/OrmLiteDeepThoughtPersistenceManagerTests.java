package net.deepthought.android.db;

import android.test.AndroidTestCase;

import net.deepthought.Application;
import net.deepthought.DefaultDependencyResolver;
import net.deepthought.data.model.DeepThought;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.Tag;
import net.deepthought.data.persistence.IEntityManager;

import java.io.File;

/**
 * Created by ganymed on 12/10/14.
 */
public class OrmLiteDeepThoughtPersistenceManagerTests extends AndroidTestCase {

  IEntityManager entityManager;

  public OrmLiteDeepThoughtPersistenceManagerTests() {
    super();
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    entityManager = new OrmLiteAndroidEntityManager(this.getContext());

    Application.instantiate(new DefaultDependencyResolver(entityManager));

//    entityManager.clearData();
//    Data.createInstance(entityManager, false);
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();

    Application.shutdown();
    new File(OrmLiteAndroidEntityManager.DATABASE_NAME).delete();
  }


  public void testInsertDeepThoughtEntity() throws Exception {
    DeepThought deepThought = new DeepThought();

    entityManager.updateEntity(deepThought);

    assertNotNull(deepThought.getId());
  }

//  public void testInsertEntryEntity() throws Exception {
//    Entry entry = new Entry("Test Entry", "I'm just a test");
//
//    entityManager.persistEntry(entry);
//
//    assertNotNull(entry.getId());
//  }
//
//  public void testInsertTagEntity() throws Exception {
//    Tag tag = new Tag("Test Tag");
//
//    entityManager.persistTag(tag);
//
//    assertNotNull(tag.getId());
//  }

  public void testInsertDeepThoughtEntityWithEntriesRelationSet() throws Exception {
    DeepThought deepThought = new DeepThought();

    Entry entry1 = new Entry("Test1", "No content");
    deepThought.addEntry(entry1);
    //entityManager.persistEntry(entry1);
    Entry entry2 = new Entry("Test2", "No content");
    deepThought.addEntry(entry2);
    //entityManager.persistEntry(entry2);

    entityManager.updateEntity(deepThought);

    assertNotNull(entry1.getId());
    assertNotNull(entry2.getId());
  }

  public void testInsertEntryWithTagsRelationSet() throws Exception {
    Entry entry1 = new Entry("Test1", "No content");
    Entry entry2 = new Entry("Test2", "No content");

    Tag tag1 = new Tag("Tag 1");
    Tag tag2 = new Tag("Tag 2");
    Tag tag3 = new Tag("Tag 3");

    entry1.addTag(tag1);
    entry1.addTag(tag2);
    entry2.addTag(tag2);
    entry2.addTag(tag3);

    DeepThought persistedDeepThought = Application.getDeepThought();
    persistedDeepThought.addEntry(entry1);
    persistedDeepThought.addEntry(entry2);

    assertNotNull(tag1.getId());
    assertNotNull(tag2.getId());
    assertNotNull(tag3.getId());
  }

  public void testDeserializeDeepThought() throws Exception {
    DeepThought persistedDeepThought = Application.getDeepThought();
    buildDeepThoughtWithAllRelationsSet(persistedDeepThought);
    entityManager.updateEntity(persistedDeepThought);

    DeepThought deserializedDeepThought = Application.getDeepThought();

    assertEquals(persistedDeepThought.countEntries(), deserializedDeepThought.countEntries());
    assertEquals(persistedDeepThought.countTags(), deserializedDeepThought.countTags());
  }

  protected DeepThought buildDeepThoughtWithAllRelationsSet(DeepThought deepThought) {
    Entry entry1 = new Entry("Test Entry 1", "No content");
    deepThought.addEntry(entry1);
    Entry entry2 = new Entry("Test Entry 2", "No content");
    deepThought.addEntry(entry2);
    Entry entry3 = new Entry("Test Entry 3", "No content");
    deepThought.addEntry(entry3);
    Entry entry4 = new Entry("Test Entry 4", "No content");
    deepThought.addEntry(entry4);

    Tag commonTag = new Tag("common");
    for(int i = 0; i < deepThought.getEntries().size(); i++) {
      Entry entry = deepThought.entryAt(i);
      entry.addTag(commonTag);
      entry.addTag(new Tag("Entry Specific " + (i + 1)));
    }

    return deepThought;
  }

}

package net.deepthought.android.db;

import com.j256.ormlite.dao.cda.jointable.JoinTableDao;
import com.j256.ormlite.dao.cda.jointable.JoinTableDaoRegistry;

import net.deepthought.Application;
import net.deepthought.android.db.helper.TestJoinTableDaoRegistry;
import net.deepthought.data.model.DeepThought;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.Tag;

import java.util.List;

/**
 * Created by ganymed on 10/11/14.
 */
public class EntryTest extends EntitiesTestBase {

  public void testUpdateTitle_UpdatedTitleGetsPersistedInDb() throws Exception {
    Entry entry = new Entry("test", "no content");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntry(entry);

    String newTitle = "New title";
    entry.setTitle(newTitle);

    // assert title really got written to database
    List<String[]> queryResult = entityManager.<String[]>doNativeQuery("SELECT title FROM entry WHERE id=" + entry.getId());
    assertEquals(1, queryResult.size());
    assertEquals(1, queryResult.get(0).length);
    assertEquals(newTitle, queryResult.get(0)[0]);
  }

  public void testUpdateContent_UpdatedContentGetsPersistedInDb() throws Exception {
    Entry entry = new Entry("test", "no content");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntry(entry);

    String newContent = "New content";
    entry.setContent(newContent);

    // assert content really got written to database
    List<String[]> queryResult = entityManager.<String[]>doNativeQuery("SELECT content FROM entry WHERE id=" + entry.getId());
    assertEquals(1, queryResult.size());
    assertEquals(1, queryResult.get(0).length);
    assertEquals(newContent, queryResult.get(0)[0]);
  }

  public void testAddTag_RelationGetsPersisted() throws Exception {
    Entry entry = new Entry("test", "no content");
    Tag tag = new Tag("test");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntry(entry);
    deepThought.addTag(tag);

    entry.addTag(tag);

    // assert entry really got written to database
    // ok, time for a little hack
    JoinTableDao dao = ((TestJoinTableDaoRegistry) JoinTableDaoRegistry.getInstance()).getCachedDaoForRelation(Entry.class.getDeclaredField("tags"), Tag.class.getDeclaredField("entries"));
    assertTrue(dao.doesJoinTableEntryExist(entry.getId(), true, tag.getId()));
  }

  public void testAddTag_EntitiesGetAddedToRelatedCollections() throws Exception {
    Entry entry = new Entry("test", "no content");
    Tag tag = new Tag("test");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntry(entry);
    deepThought.addTag(tag);

    entry.addTag(tag);

    assertTrue(entry.getTags().contains(tag));
    assertTrue(tag.getEntries().contains(entry));
  }

  public void testRemoveTag_RelationGetsDeleted() throws Exception {
    Entry entry = new Entry("test", "no content");
    Tag tag = new Tag("test");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntry(entry);
    deepThought.addTag(tag);

    entry.addTag(tag);
    entry.removeTag(tag);

    // assert entry really got deleted from database
    JoinTableDao dao = ((TestJoinTableDaoRegistry) JoinTableDaoRegistry.getInstance()).getCachedDaoForRelation(Entry.class.getDeclaredField("tags"), Tag.class.getDeclaredField("entries"));
    assertFalse(dao.doesJoinTableEntryExist(entry.getId(), true, tag.getId()));
  }

  public void testRemoveTag_EntitiesGetRemovedFromRelatedCollections() throws Exception {
    Entry entry = new Entry("test", "no content");
    Tag tag = new Tag("test");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntry(entry);
    deepThought.addTag(tag);
    entry.addTag(tag);

    entry.removeTag(tag);

    assertFalse(entry.getTags().contains(tag));
    assertFalse(tag.getEntries().contains(entry));
  }
}

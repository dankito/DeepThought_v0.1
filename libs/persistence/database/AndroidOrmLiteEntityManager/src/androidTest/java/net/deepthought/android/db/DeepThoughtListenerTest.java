package net.deepthought.android.db;

import net.deepthought.Application;
import net.deepthought.data.model.Category;
import net.deepthought.data.model.DeepThought;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.Tag;
import net.deepthought.data.model.listener.EntityListener;
import net.deepthought.data.persistence.db.BaseEntity;

import java.util.Collection;

/**
 * Created by ganymed on 09/11/14.
 */
public class DeepThoughtListenerTest extends EntitiesTestBase {

  protected ListenerHasBeenCalled listenerCalled = null;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    listenerCalled = new ListenerHasBeenCalled();
  }

  public class ListenerHasBeenCalled {
    protected boolean listenerHasBeenCalled = false;

    public boolean hasListenerBeenCalled() {
      return listenerHasBeenCalled;
    }

    public void setListenerHasBeenCalled(boolean listenerHasBeenCalled) {
      this.listenerHasBeenCalled = listenerHasBeenCalled;
    }

    @Override
    public String toString() {
      return "Listener has been called: " + listenerCalled;
    }
  }

  public void testAddCategory_CategoryAddedListenerGetsCalled() throws Exception {
    final Category category = new Category("test");

    final DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntityListener(new EntityListener() {
      @Override
      public void propertyChanged(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {

      }

      @Override
      public void entityAddedToCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity) {
        if(collection == deepThought.getCategories() && addedEntity == category)
          listenerCalled.setListenerHasBeenCalled(true);
      }

      @Override
      public void entityOfCollectionUpdated(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity updatedEntity) {

      }

      @Override
      public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {

      }
    });

    deepThought.addCategory(category);

    assertTrue(listenerCalled.hasListenerBeenCalled());
  }

  public void testSetCategoryName_CategoryUpdatedListenerGetsCalled() throws Exception {
    Category category = new Category("test");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addCategory(category);

//    deepThought.addCategoriesChangedListener(new CategoriesChangedListener() {
//      @Override
//      public void categoryAdded(Category category) {
//
//      }
//
//      @Override
//      public void categoryUpdated(Category category) {
//        listenerCalled.setListenerHasBeenCalled(true);
//      }
//
//      @Override
//      public void categoryRemoved(Category category) {
//
//      }
//    });

    category.setName("New name");

    assertTrue(listenerCalled.hasListenerBeenCalled());
  }

  public void testAddSubCategoryToCategory_CategoryUpdatedListenerGetsCalled() throws Exception {
    Category category = new Category("test");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addCategory(category);

//    deepThought.addCategoriesChangedListener(new CategoriesChangedListener() {
//      @Override
//      public void categoryAdded(Category category) {
//
//      }
//
//      @Override
//      public void categoryUpdated(Category category) {
//        listenerCalled.setListenerHasBeenCalled(true);
//      }
//
//      @Override
//      public void categoryRemoved(Category category) {
//
//      }
//    });

    category.addSubCategory(new Category("sub"));

    assertTrue(listenerCalled.hasListenerBeenCalled());
  }

  public void testRemoveSubCategoryFromCategory_CategoryUpdatedListenerGetsCalled() throws Exception {
    Category category = new Category("test");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addCategory(category);
    Category subCategory = new Category("sub");
    category.addSubCategory(subCategory);

//    deepThought.addCategoriesChangedListener(new CategoriesChangedListener() {
//      @Override
//      public void categoryAdded(Category category) {
//
//      }
//
//      @Override
//      public void categoryUpdated(Category category) {
//        listenerCalled.setListenerHasBeenCalled(true);
//      }
//
//      @Override
//      public void categoryRemoved(Category category) {
//
//      }
//    });

    category.removeSubCategory(subCategory);

    assertTrue(listenerCalled.hasListenerBeenCalled());
  }

  public void testAddEntryToCategory_CategoryUpdatedListenerGetsCalled() throws Exception {
    Category category = new Category("test");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addCategory(category);

//    deepThought.addCategoriesChangedListener(new CategoriesChangedListener() {
//      @Override
//      public void categoryAdded(Category category) {
//
//      }
//
//      @Override
//      public void categoryUpdated(Category category) {
//        listenerCalled.setListenerHasBeenCalled(true);
//      }
//
//      @Override
//      public void categoryRemoved(Category category) {
//
//      }
//    });

    Entry newEntry = new Entry("entry", "contentless");
    newEntry.addCategory(category);

    assertTrue(listenerCalled.hasListenerBeenCalled());
  }

  public void testRemoveEntryToCategory_CategoryUpdatedListenerGetsCalled() throws Exception {
    Category category = new Category("test");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addCategory(category);
    Entry entry = new Entry("entry", "contentless");
    entry.addCategory(category);

//    deepThought.addCategoriesChangedListener(new CategoriesChangedListener() {
//      @Override
//      public void categoryAdded(Category category) {
//
//      }
//
//      @Override
//      public void categoryUpdated(Category category) {
//        listenerCalled.setListenerHasBeenCalled(true);
//      }
//
//      @Override
//      public void categoryRemoved(Category category) {
//
//      }
//    });

    entry.removeCategory(category);

    assertTrue(listenerCalled.hasListenerBeenCalled());
  }

  public void testRemoveCategory_CategoryRemovedListenerGetsCalled() throws Exception {
    Category category = new Category("test");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addCategory(category);

//    deepThought.addCategoriesChangedListener(new CategoriesChangedListener() {
//      @Override
//      public void categoryAdded(Category category) {
//
//      }
//
//      @Override
//      public void categoryUpdated(Category category) {
//
//      }
//
//      @Override
//      public void categoryRemoved(Category category) {
//        listenerCalled.setListenerHasBeenCalled(true);
//      }
//    });

    deepThought.removeCategory(category);

    assertTrue(listenerCalled.hasListenerBeenCalled());
  }


  public void testAddEntry_EntryAddedListenerGetsCalled() throws Exception {
    Entry entry = new Entry("test", "no content");

    DeepThought deepThought = Application.getDeepThought();
//    deepThought.addEntriesChangedListener(new EntriesChangedListener() {
//
//      @Override
//      public void entryAdded(Entry entry) {
//        listenerCalled.setListenerHasBeenCalled(true);
//      }
//
//      @Override
//      public void entryUpdated(Entry entry) {
//
//      }
//
//      @Override
//      public void entryRemoved(Entry entry) {
//
//      }
//    });

    deepThought.addEntry(entry);

    assertTrue(listenerCalled.hasListenerBeenCalled());
  }

  public void testUpdateEntryTitle_EntryUpdatedListenerGetsCalled() throws Exception {
    Entry entry = new Entry("test", "no content");

    DeepThought deepThought = Application.getDeepThought();
//    deepThought.addEntriesChangedListener(new EntriesChangedListener() {
//
//      @Override
//      public void entryAdded(Entry entry) {
//
//      }
//
//      @Override
//      public void entryUpdated(Entry entry) {
//        listenerCalled.setListenerHasBeenCalled(true);
//      }
//
//      @Override
//      public void entryRemoved(Entry entry) {
//
//      }
//    });

    deepThought.addEntry(entry);
    entry.setTitle("New test");

    assertTrue(listenerCalled.hasListenerBeenCalled());
    assertEquals("New test", entry.getTitle());
  }

  public void testUpdateEntryContent_EntryUpdatedListenerGetsCalled() throws Exception {
    Entry entry = new Entry("test", "no content");

    DeepThought deepThought = Application.getDeepThought();
//    deepThought.addEntriesChangedListener(new EntriesChangedListener() {
//
//      @Override
//      public void entryAdded(Entry entry) {
//
//      }
//
//      @Override
//      public void entryUpdated(Entry entry) {
//        listenerCalled.setListenerHasBeenCalled(true);
//      }
//
//      @Override
//      public void entryRemoved(Entry entry) {
//
//      }
//    });

    deepThought.addEntry(entry);
    entry.setContent("New content");

    assertTrue(listenerCalled.hasListenerBeenCalled());
    assertEquals("New content", entry.getContent());
  }

  public void testAddTagToEntry_EntryUpdatedListenerGetsCalled() throws Exception {
    Entry entry = new Entry("test", "no content");

    DeepThought deepThought = Application.getDeepThought();
//    deepThought.addEntriesChangedListener(new EntriesChangedListener() {
//
//      @Override
//      public void entryAdded(Entry entry) {
//
//      }
//
//      @Override
//      public void entryUpdated(Entry entry) {
//        listenerCalled.setListenerHasBeenCalled(true);
//      }
//
//      @Override
//      public void entryRemoved(Entry entry) {
//
//      }
//    });

    deepThought.addEntry(entry);

    Tag tag = new Tag("test");
    entry.addTag(tag);

    assertTrue(listenerCalled.hasListenerBeenCalled());
  }

  public void testRemoveTagFromEntry_EntryUpdatedListenerGetsCalled() throws Exception {
    Entry entry = new Entry("test", "no content");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntry(entry);

    Tag tag = new Tag("test");
    entry.addTag(tag);

//    deepThought.addEntriesChangedListener(new EntriesChangedListener() {
//
//      @Override
//      public void entryAdded(Entry entry) {
//
//      }
//
//      @Override
//      public void entryUpdated(Entry entry) {
//        listenerCalled.setListenerHasBeenCalled(true);
//      }
//
//      @Override
//      public void entryRemoved(Entry entry) {
//
//      }
//    });

    entry.removeTag(tag);

    assertTrue(listenerCalled.hasListenerBeenCalled());
  }

  public void testRemoveEntry_EntryRemovedListenerGetsCalled() throws Exception {
    Entry entry = new Entry("test", "no content");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntry(entry);

//    deepThought.addEntriesChangedListener(new EntriesChangedListener() {
//
//      @Override
//      public void entryAdded(Entry entry) {
//
//      }
//
//      @Override
//      public void entryUpdated(Entry entry) {
//
//      }
//
//      @Override
//      public void entryRemoved(Entry entry) {
//        listenerCalled.setListenerHasBeenCalled(true);
//      }
//    });

    deepThought.removeEntry(entry);

    assertTrue(listenerCalled.hasListenerBeenCalled());
  }

  public void testAddTag_TagAddedListenerGetsCalled() throws Exception {
    Tag tag = new Tag("test");

    DeepThought deepThought = Application.getDeepThought();
//    deepThought.addTagsChangedListener(new TagsChangedListener() {
//      @Override
//      public void tagAdded(Tag tag) {
//        listenerCalled.setListenerHasBeenCalled(true);
//      }
//
//      @Override
//      public void tagUpdated(Tag tag) {
//
//      }
//
//      @Override
//      public void tagRemoved(Tag tag) {
//
//      }
//    });

    deepThought.addTag(tag);

    assertTrue(listenerCalled.hasListenerBeenCalled());
  }

  public void testSetTagName_TagUpdatedListenerGetsCalled() throws Exception {
    Tag tag = new Tag("test");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addTag(tag);

//    deepThought.addTagsChangedListener(new TagsChangedListener() {
//      @Override
//      public void tagAdded(Tag tag) {
//
//      }
//
//      @Override
//      public void tagUpdated(Tag tag) {
//        listenerCalled.setListenerHasBeenCalled(true);
//      }
//
//      @Override
//      public void tagRemoved(Tag tag) {
//
//      }
//    });

    tag.setName("New name");

    assertTrue(listenerCalled.hasListenerBeenCalled());
  }

  public void testRemoveTag_TagRemovedListenerGetsCalled() throws Exception {
    Tag tag = new Tag("test");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addTag(tag);

//    deepThought.addTagsChangedListener(new TagsChangedListener() {
//      @Override
//      public void tagAdded(Tag tag) {
//
//      }
//
//      @Override
//      public void tagUpdated(Tag tag) {
//
//      }
//
//      @Override
//      public void tagRemoved(Tag tag) {
//        listenerCalled.setListenerHasBeenCalled(true);
//      }
//    });

    deepThought.removeTag(tag);

    assertTrue(listenerCalled.hasListenerBeenCalled());
  }
}

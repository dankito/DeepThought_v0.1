package net.dankito.deepthought.android.data.persistence.db;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.data.model.Category;
import net.dankito.deepthought.data.model.DeepThought;
import net.dankito.deepthought.data.model.Entry;
import net.dankito.deepthought.data.model.Tag;

import java.util.List;

/**
 * Created by ganymed on 09/11/14.
 */
public class DeepThoughtTest extends EntitiesTestBase {

  public void testAddCategory_CategoryGetsPersisted() throws Exception {
    Category category = new Category("test");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addCategory(category);

    // assert categories really got written to database
    List queryResult = entityManager.doNativeQuery("SELECT * FROM categories WHERE id=" + category.getId());
    assertEquals(1, queryResult.size());
  }

  public void testAddCategory_RelationsGetSet() throws Exception {
    Category category = new Category("test");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addCategory(category);

    assertNotNull(category.getId());
    assertEquals(deepThought, category.getDeepThought());
    assertTrue(deepThought.getCategories().contains(category));
    assertEquals(deepThought.getTopLevelCategory(), category.getParentCategory());
    assertTrue(deepThought.getTopLevelCategory().getSubCategories().contains(category));
  }

  public void testAddCategoryHierarchy_RelationsGetSet() throws Exception {
    Category topLevelCategory1 = new Category("Top Level 1");
    Category topLevelCategory2 = new Category("Top Level 2");

    for(int i = 1; i < 5; i++) {
      Category subLevel1Category = new Category("Sub Level 1-" + i);
      topLevelCategory1.addSubCategory(subLevel1Category);

      Category subLevel2Category = new Category("Sub Level 2-" + i);
      topLevelCategory2.addSubCategory(subLevel2Category);

      for(int j = 1; j < 4; j++) {
        subLevel1Category.addSubCategory(new Category("Sub sub 1-" + j));
        subLevel2Category.addSubCategory(new Category("Sub sub 2-" + j));
      }
    }

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addCategory(topLevelCategory1);
    deepThought.addCategory(topLevelCategory2);

    assertNotNull(deepThought.getTopLevelCategory());
    assertNotNull(deepThought.getTopLevelCategory().getId());
    assertNull(deepThought.getTopLevelCategory().getParentCategory());

    assertEquals(deepThought.getTopLevelCategory(), topLevelCategory1.getParentCategory());
    assertEquals(deepThought.getTopLevelCategory(), topLevelCategory2.getParentCategory());

    for(Category subCategory : topLevelCategory1.getSubCategories()) {
      assertNotNull(subCategory.getId());
      assertEquals(topLevelCategory1, subCategory.getParentCategory());
      assertTrue(topLevelCategory1.containsSubCategory(subCategory));

      for(Category subSubCategory : subCategory.getSubCategories()) {
        assertNotNull(subSubCategory.getId());
        assertEquals(subCategory, subSubCategory.getParentCategory());
        assertTrue(subCategory.containsSubCategory(subSubCategory));
        assertEquals(0, subSubCategory.getSubCategories().size());
      }
    }

    for(Category subCategory : topLevelCategory2.getSubCategories()) {
      assertNotNull(subCategory.getId());
      assertEquals(topLevelCategory2, subCategory.getParentCategory());
      assertTrue(topLevelCategory2.containsSubCategory(subCategory));

      for(Category subSubCategory : subCategory.getSubCategories()) {
        assertNotNull(subSubCategory.getId());
        assertEquals(subCategory, subSubCategory.getParentCategory());
        assertTrue(subCategory.containsSubCategory(subSubCategory));
        assertEquals(0, subSubCategory.getSubCategories().size());
      }
    }
  }

  public void testRemoveCategory_CategoryGetsDeletedFromDB() throws Exception {
    Category category = new Category("test");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addCategory(category);

    Long categoryId = category.getId();
    deepThought.removeCategory(category);

    // assert categories really got deleted from database
    List queryResult = entityManager.doNativeQuery("SELECT * FROM categories WHERE id=" + categoryId);
    assertEquals(0, queryResult.size());
  }

  public void testRemoveCategory_RelationsGetRemoved() throws Exception {
    Category category = new Category("test");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addCategory(category);

    deepThought.removeCategory(category);

    assertNull(category.getId());
    assertNull(category.getDeepThought());
    assertFalse(deepThought.getCategories().contains(category));
  }

  public void testAddEntry_EntryGetsPersisted() throws Exception {
    Entry entry = new Entry("test", "no content");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntry(entry);

    // assert entry really got written to database
    List queryResult = entityManager.doNativeQuery("SELECT * FROM entry WHERE id=" + entry.getId());
    assertEquals(1, queryResult.size());
  }

  public void testAddEntry_RelationsGetSet() throws Exception {
    Entry entry = new Entry("test", "no content");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntry(entry);

    assertNotNull(entry.getId());
    assertEquals(deepThought, entry.getDeepThought());
    assertTrue(deepThought.getEntries().contains(entry));
  }

  public void testRemoveEntry_EntryGetsDeletedFromDB() throws Exception {
    Entry entry = new Entry("test", "no content");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntry(entry);

    Long entryId = entry.getId();
    deepThought.removeEntry(entry);

    // assert entry really got deleted from database
    List queryResult = entityManager.doNativeQuery("SELECT * FROM entry WHERE id=" + entryId);
    assertEquals(0, queryResult.size());
  }

  public void testRemoveEntry_RelationsGetRemoved() throws Exception {
    Entry entry = new Entry("test", "no content");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntry(entry);

    deepThought.removeEntry(entry);

    assertNull(entry.getId());
    assertNull(entry.getDeepThought());
    assertFalse(deepThought.getEntries().contains(entry));
  }

  public void testAddTag_TagGetsPersisted() throws Exception {
    Tag tag = new Tag("test");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addTag(tag);

    // assert entry really got written to database
    List queryResult = entityManager.doNativeQuery("SELECT * FROM tag WHERE id=" + tag.getId());
    assertEquals(1, queryResult.size());
  }

  public void testAddTag_RelationsGetSet() throws Exception {
    Tag tag = new Tag("test");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addTag(tag);

    assertNotNull(tag.getId());
    assertEquals(deepThought, tag.getDeepThought());
    assertTrue(deepThought.getTags().contains(tag));
  }

  public void testRemoveTag_TagGetsDeletedFromDB() throws Exception {
    Tag tag = new Tag("test");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addTag(tag);

    Long tagId = tag.getId();
    deepThought.removeTag(tag);

    // assert entry really got deleted from database
    List queryResult = entityManager.doNativeQuery("SELECT * FROM tag WHERE id=" + tagId);
    assertEquals(0, queryResult.size());
  }

  public void testRemoveTag_RelationsGetRemoved() throws Exception {
    Tag tag = new Tag("test");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addTag(tag);

    deepThought.removeTag(tag);

    assertNull(tag.getId());
    assertNull(tag.getDeepThought());
    assertFalse(deepThought.getEntries().contains(tag));
  }
}

package net.deepthought.android.data.persistence.db;

import net.deepthought.Application;
import net.deepthought.data.model.Category;
import net.deepthought.data.model.DeepThought;
import net.deepthought.data.model.Entry;
import net.deepthought.data.persistence.db.TableConfig;

import java.util.List;

/**
 * Created by ganymed on 10/11/14.
 */
public class CategoryTest extends EntitiesTestBase {

  public void testUpdateName_UpdatedNameGetsPersistedInDb() throws Exception {
    Category category = new Category("test");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addCategory(category);

    String newName = "New name";
    category.setName(newName);

    // assert name really got written to database
    List<String[]> queryResult = entityManager.<String[]>doNativeQuery("SELECT name FROM " + TableConfig.CategoryTableName + " WHERE id=" + category.getId());
    assertEquals(1, queryResult.size());
    assertEquals(1, queryResult.get(0).length);
    assertEquals(newName, queryResult.get(0)[0]);
  }

  public void testAddSubCategory_SubCategoryGetsPersisted() throws Exception {
    Category category = new Category("test");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addCategory(category);

    Category subCategory = new Category("sub");
    category.addSubCategory(subCategory);

    // assert categories really got written to database
    List queryResult = entityManager.doNativeQuery("SELECT * FROM " + TableConfig.CategoryTableName + " WHERE id=" + subCategory.getId());
    assertEquals(1, queryResult.size());
  }

  public void testAddSubCategory_RelationsGetSet() throws Exception {
    Category category = new Category("test");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addCategory(category);

    Category subCategory = new Category("sub");
    category.addSubCategory(subCategory);

    assertNotNull(subCategory.getId());
    assertEquals(category, subCategory.getParentCategory());
    assertTrue(category.getSubCategories().contains(subCategory));
  }

  public void testRemoveSubCategory_SubCategoryGetsNotDeletedFromDB() throws Exception {
    Category category = new Category("test");
    DeepThought deepThought = Application.getDeepThought();
    deepThought.addCategory(category);

    Category subCategory = new Category("sub");
    category.addSubCategory(subCategory);

    Long subCategoryId = subCategory.getId();
//    category.removeSubCategory(subCategory);
    deepThought.removeCategory(subCategory);

    // assert categories really didn't get deleted from database
    List queryResult = entityManager.doNativeQuery("SELECT * FROM " + TableConfig.CategoryTableName + " WHERE id=" + subCategoryId);
    assertEquals(1, queryResult.size());
  }

  public void testRemoveSubCategory_RelationsGetRemoved() throws Exception {
    Category category = new Category("test");
    DeepThought deepThought = Application.getDeepThought();
    deepThought.addCategory(category);

    Category subCategory = new Category("sub");
    category.addSubCategory(subCategory);

//    category.removeSubCategory(subCategory);
    deepThought.removeCategory(subCategory);

    assertNotNull(subCategory.getId()); // subCategories is not a Composition -> subCategory stays in DB till it gets removed from DeepThought
    assertNull(subCategory.getParentCategory());
    assertFalse(category.getSubCategories().contains(subCategory));
  }

  public void testAddEntry_EntryGetsPersisted() throws Exception {
    Category category = new Category("test");
    DeepThought deepThought = Application.getDeepThought();
    deepThought.addCategory(category);

    Entry entry = new Entry("test", "no content");
    deepThought.addEntry(entry);

    entry.addCategory(category);

    // assert entry really got written to database
    List queryResult = entityManager.doNativeQuery("SELECT * FROM " + TableConfig.EntryTableName + " WHERE id=" + entry.getId());
    assertEquals(1, queryResult.size());
  }

  public void testAddEntry_RelationsGetSet() throws Exception {
    Category category = new Category("test");
    DeepThought deepThought = Application.getDeepThought();
    deepThought.addCategory(category);

    Entry entry = new Entry("test", "no content");
    deepThought.addEntry(entry);

    entry.addCategory(category);

    assertNotNull(entry.getId());
    assertTrue(entry.getCategories().contains(category));
    assertTrue(category.getEntries().contains(entry));
  }

  public void testRemoveEntry_EntryGetsNotDeletedFromDB() throws Exception {
    Category category = new Category("test");
    DeepThought deepThought = Application.getDeepThought();
    deepThought.addCategory(category);

    Entry entry = new Entry("test", "no content");
    deepThought.addEntry(entry);

    entry.addCategory(category);

    Long entryId = entry.getId();
    entry.removeCategory(category);

    // assert entry really didn't get deleted from database
    List queryResult = entityManager.doNativeQuery("SELECT * FROM entry WHERE id=" + entryId);
    assertEquals(1, queryResult.size());
  }

  public void testRemoveEntry_RelationFieldsGetUnset() throws Exception {
    Category category = new Category("test");
    DeepThought deepThought = Application.getDeepThought();
    deepThought.addCategory(category);

    Entry entry = new Entry("test", "no content");
    deepThought.addEntry(entry);

    entry.addCategory(category);

    entry.removeCategory(category);

    assertFalse(entry.getCategories().contains(category));
    assertFalse(category.getEntries().contains(entry));
  }
}

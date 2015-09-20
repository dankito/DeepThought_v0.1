package net.deepthought.data.model;

import net.deepthought.Application;
import net.deepthought.data.persistence.db.TableConfig;

import org.junit.Assert;
import org.junit.Test;

import java.sql.SQLException;

/**
 * Created by ganymed on 10/11/14.
 */
public abstract class CategoryTestBase extends DataModelTestBase {

  protected boolean doesCategoryEntryJoinTableEntryExist(Long categoryId, Long entryId) throws SQLException {
    return doesJoinTableEntryExist(TableConfig.EntryCategoryJoinTableName, TableConfig.EntryCategoryJoinTableCategoryIdColumnName, categoryId,
        TableConfig.EntryCategoryJoinTableEntryIdColumnName, entryId);
  }


  @Test
  public void updateName_UpdatedNameGetsPersistedInDb() throws Exception {
    Category category = new Category("test");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addCategory(category);

    String newName = "New name";
    category.setName(newName);

    // assert name really got written to database
    Assert.assertEquals(newName, getValueFromTable(TableConfig.CategoryTableName,TableConfig.CategoryNameColumnName, category.getId()));
  }

  @Test
  public void updateDescription_UpdatedNameGetsPersistedInDb() throws Exception {
    Category category = new Category("test");
    category.setDescription("no description");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addCategory(category);

    String newDescription = "Look at this beautiful Description!";
    category.setDescription(newDescription);

    // assert helpText really got updated in database
    Assert.assertEquals(newDescription, getValueFromTable(TableConfig.CategoryTableName, TableConfig.CategoryDescriptionColumnName, category.getId()));
  }

  @Test
  public void updateIsExpanded_UpdatedIsExpandedGetsPersistedInDb() throws Exception {
    Category category = new Category("test");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addCategory(category);

    category.setIsExpanded(true);

    // assert helpText really got updated in database
//    Assert.assertTrue((boolean) getValueFromTable(TableConfig.CategoryTableName, TableConfig.CategoryIsExpandedColumnName, category.getId()));
    Object dbValue = getValueFromTable(TableConfig.CategoryTableName, TableConfig.CategoryIsExpandedColumnName, category.getId());
    if(dbValue instanceof String)
      dbValue = Short.parseShort((String)dbValue);
    if(dbValue instanceof Short)
      dbValue = ((Short)dbValue).intValue();
    Assert.assertTrue(dbValue.equals(1));
  }

  @Test
  public void addSubCategory_SubCategoryGetsPersisted() throws Exception {
    Category category = new Category("test");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addCategory(category);

    Category subCategory = new Category("sub");
    category.addSubCategory(subCategory);

    // assert categories really got written to database
    Object[] queryResult = getRowFromTable(TableConfig.CategoryTableName, subCategory.getId());
    Assert.assertNotEquals(0, queryResult.length);
  }

  @Test
  public void addSubCategory_RelationsGetSet() throws Exception {
    Category category = new Category("test");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addCategory(category);

    Category subCategory = new Category("sub");
    category.addSubCategory(subCategory);

    Assert.assertNotNull(subCategory.getId());
    Assert.assertEquals(category, subCategory.getParentCategory());
    Assert.assertTrue(category.getSubCategories().contains(subCategory));
  }

  @Test
  public void addSubCategories_CategoryOrderGetsSetCorrectly() throws Exception {
    Category category = new Category("test");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addCategory(category);

    Category subCategory1 = new Category("sub 1");
    category.addSubCategory(subCategory1);
    Category subCategory2 = new Category("sub 2");
    category.addSubCategory(subCategory2);
    Category subCategory3 = new Category("sub 3");
    category.addSubCategory(subCategory3);

    Assert.assertEquals(0, subCategory1.getCategoryOrder());
    Assert.assertEquals(1, subCategory2.getCategoryOrder());
    Assert.assertEquals(2, subCategory3.getCategoryOrder());
  }

  @Test
  public void removeSubCategory_SubCategoryGetsDeletedFromDB() throws Exception {
    Category category = new Category("test");
    DeepThought deepThought = Application.getDeepThought();
    deepThought.addCategory(category);

    Category subCategory = new Category("sub");
    category.addSubCategory(subCategory);

    Long subCategoryId = subCategory.getId();
//    category.removeSubCategory(subCategory);
    deepThought.removeCategory(subCategory);

    // assert categories really didn't get deleted from database
    Object[] queryResult = getRowFromTable(TableConfig.CategoryTableName, subCategoryId);
    Assert.assertNotEquals(0, queryResult.length);

    Assert.assertFalse(category.isDeleted());
    Assert.assertTrue(subCategory.isDeleted());
  }

  @Test
  public void removeSubCategory_RelationsGetRemoved() throws Exception {
    Category category = new Category("test");
    Category subCategory = new Category("sub");
    category.addSubCategory(subCategory);

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addCategory(category);

//    category.removeSubCategory(subCategory);
    deepThought.removeCategory(subCategory);

    Assert.assertNotNull(subCategory.getId()); // subCategories is not a Composition -> subCategory stays in DB till it gets removed from DeepThought
    Assert.assertNull(subCategory.getParentCategory());
    Assert.assertFalse(category.getSubCategories().contains(subCategory));
  }

  @Test
  public void removeSubCategories_CategoryOrderGetsAdjustedCorrectly() throws Exception {
    Category category = new Category("test");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addCategory(category);

    Category subCategory1 = new Category("sub 1");
    category.addSubCategory(subCategory1);
    Category subCategory2 = new Category("sub 2");
    category.addSubCategory(subCategory2);
    Category subCategory3 = new Category("sub 3");
    category.addSubCategory(subCategory3);
    Category subCategory4 = new Category("sub 4");
    category.addSubCategory(subCategory4);
    Category subCategory5 = new Category("sub 5");
    category.addSubCategory(subCategory5);

//    category.removeSubCategory(subCategory2);
    deepThought.removeCategory(subCategory2);
//    category.removeSubCategory(subCategory4);
    deepThought.removeCategory(subCategory4);

    Assert.assertEquals(0, subCategory1.getCategoryOrder());
    Assert.assertEquals(1, subCategory3.getCategoryOrder());
    Assert.assertEquals(2, subCategory5.getCategoryOrder());
  }


  @Test
  public void addEntry_EntryGetsPersisted() throws Exception {
    DeepThought deepThought = Application.getDeepThought();
    Category category = new Category("test");
    deepThought.addCategory(category);

    Entry entry = new Entry("test", "no content");
    deepThought.addEntry(entry);
    entry.addCategory(category);

    // assert entry really got written to database
    Object[] queryResult = getRowFromTable(TableConfig.EntryTableName, entry.getId());
    Assert.assertNotEquals(0, queryResult.length);
  }

  @Test
  public void addEntry_RelationsGetSet() throws Exception {
    DeepThought deepThought = Application.getDeepThought();

    Category category = new Category("test");
    deepThought.addCategory(category);

    Entry entry = new Entry("test", "no content");
    deepThought.addEntry(entry);
    entry.addCategory(category);

    Assert.assertNotNull(entry.getId());
    Assert.assertTrue(entry.getCategories().contains(category));
    Assert.assertTrue(category.getEntries().contains(entry));
  }

  @Test
  public void removeEntry_EntryGetsNotDeletedFromDB() throws Exception {
    DeepThought deepThought = Application.getDeepThought();
    Category category = new Category("test");
    deepThought.addCategory(category);

    Entry entry = new Entry("test", "no content");
    deepThought.addEntry(entry);
    entry.addCategory(category);

    Long entryId = entry.getId();
    entry.removeCategory(category);

    // assert entry really didn't get deleted from database
    Object[] queryResult = getRowFromTable(TableConfig.EntryTableName, entry.getId());
    Assert.assertNotEquals(0, queryResult.length);
  }

  @Test
  public void removeEntry_RelationFieldsGetUnset() throws Exception {
    DeepThought deepThought = Application.getDeepThought();
    Category category = new Category("test");
    deepThought.addCategory(category);

    Entry entry = new Entry("test", "no content");
    deepThought.addEntry(entry);

    entry.addCategory(category);

    entry.removeCategory(category);

    Assert.assertFalse(entry.getCategories().contains(category));
    Assert.assertFalse(category.getEntries().contains(entry));

    Assert.assertFalse(category.isDeleted());
    Assert.assertFalse(entry.isDeleted());
  }

}

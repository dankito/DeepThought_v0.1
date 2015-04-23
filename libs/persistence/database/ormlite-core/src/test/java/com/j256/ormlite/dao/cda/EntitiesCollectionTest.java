package com.j256.ormlite.dao.cda;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.dao.cda.testmodel.Category;
import com.j256.ormlite.dao.cda.testmodel.Entry;
import com.j256.ormlite.dao.cda.testmodel.Keyword;
import com.j256.ormlite.dao.cda.testmodel.RelationEntities;
import com.j256.ormlite.stmt.PreparedQuery;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ganymed on 15/10/14.
 */
public class EntitiesCollectionTest extends EntitiesCollectionTestBase {

  protected Dao<RelationEntities.EagerOneSide, Long> oneSideDao = null;
  protected Dao<RelationEntities.EagerManySide, Long> manySideDao = null;

  protected Dao<Category, Long> categoryDao = null;
  protected Dao<Entry, Long> entryDao = null;
  protected Dao<Keyword, Long> keywordDao = null;


  @Override
  public void before() throws Exception {
    super.before();

    oneSideDao = createDao(RelationEntities.EagerOneSide.class, true, true);
    manySideDao = createDao(RelationEntities.EagerManySide.class, true, true);

    categoryDao = createDao(Category.class, true, true);
    entryDao = createDao(Entry.class, true, true);
    keywordDao = createDao(Keyword.class, true, true);
  }


  @Test
  public void creation_TestIfCollectionIsOfTypeEntitiesCollection() throws Exception {
    RelationEntities.EagerOneSide oneSide = new RelationEntities.EagerOneSide();
    oneSideDao.create(oneSide);

    Assert.assertEquals(EntitiesCollection.class, oneSide.getManySides().getClass());
  }

  @Test
  public void addManySideEntityBeforeCreation_TestIfEntityHasCorrectlyBeenAdded() throws Exception {
    RelationEntities.EagerOneSide oneSide = new RelationEntities.EagerOneSide("One");

    RelationEntities.EagerManySide manySide = new RelationEntities.EagerManySide("Many 1");
    oneSide.getManySides().add(manySide);

    oneSideDao.create(oneSide);

    Assert.assertNotNull(oneSide.getId());
    Assert.assertNotNull(manySide.getId());

    Assert.assertEquals(1, oneSide.getManySides().size());
    Assert.assertEquals(oneSide, manySide.getOneSide());
    Assert.assertEquals(manySide, oneSide.getManySides().get(0));
  }

  @Test
  public void addManySideEntityAfterCreation_TestIfEntityHasCorrectlyBeenAdded() throws Exception {
    RelationEntities.EagerOneSide oneSide = new RelationEntities.EagerOneSide("One");
    oneSideDao.create(oneSide);

    RelationEntities.EagerManySide manySide = new RelationEntities.EagerManySide("Many 1");
    oneSide.getManySides().add(manySide);

    Assert.assertNotNull(oneSide.getId());
    Assert.assertNotNull(manySide.getId());

    Assert.assertEquals(1, oneSide.getManySides().size());
    Assert.assertEquals(oneSide, manySide.getOneSide());
    Assert.assertEquals(manySide, oneSide.getManySides().get(0));
  }

  @Test
  public void addManySideEntity_TestIfTableEntryCreated() throws Exception {
    RelationEntities.EagerOneSide oneSide = new RelationEntities.EagerOneSide("One");
    oneSideDao.create(oneSide);

    RelationEntities.EagerManySide manySide = new RelationEntities.EagerManySide("Many 1");
    oneSide.getManySides().add(manySide);

    manySideDao.clearObjectCache();
    RelationEntities.EagerManySide persistedEntity = manySideDao.queryForId(manySide.getId());

    Assert.assertNotNull(persistedEntity);
    Assert.assertNotSame(manySide, persistedEntity); // to ensure persistedEntity hasn't just been taken from cache
  }

  @Test
  public void removeManySideEntityBeforeCreation_TestIfEntityHasCorrectlyBeenRemoved() throws Exception {
    RelationEntities.EagerOneSide oneSide = new RelationEntities.EagerOneSide("One");

    RelationEntities.EagerManySide manySide = new RelationEntities.EagerManySide("Many 1");
    oneSide.getManySides().add(manySide);
    oneSide.getManySides().remove(manySide);

    oneSideDao.create(oneSide);

    Assert.assertNotNull(oneSide.getId());
    Assert.assertNull(manySide.getId());

    Assert.assertEquals(0, oneSide.getManySides().size());
    Assert.assertEquals(null, manySide.getOneSide());
  }

  @Test
  public void removeManySideEntityAfterCreation_TestIfEntityHasCorrectlyBeenRemoved() throws Exception {
    RelationEntities.EagerOneSide oneSide = new RelationEntities.EagerOneSide("One");

    RelationEntities.EagerManySide manySide = new RelationEntities.EagerManySide("Many 1");
    oneSide.getManySides().add(manySide);
    oneSideDao.create(oneSide);

    oneSide.getManySides().remove(manySide);

    Assert.assertNotNull(oneSide.getId());
    Assert.assertNull(manySide.getId());

    Assert.assertEquals(0, oneSide.getManySides().size());
    Assert.assertEquals(null, manySide.getOneSide());
  }

  @Test
  public void removeManySideEntity_TestIfTableEntryHasBeenDeleted() throws Exception {
    RelationEntities.EagerOneSide oneSide = new RelationEntities.EagerOneSide("One");

    RelationEntities.EagerManySide manySide = new RelationEntities.EagerManySide("Many 1");
    oneSide.getManySides().add(manySide);
    oneSideDao.create(oneSide);

    Long manySideId = manySide.getId();
    oneSide.getManySides().remove(manySide);

    manySideDao.clearObjectCache();
    RelationEntities.EagerManySide persistedEntity = manySideDao.queryForId(manySideId);
    Assert.assertNull(persistedEntity);
  }

  @Test
  public void addManySideEntities_ClearCacheAndQueryForAll_TestIfPersistedEntitiesMatchAddedOnesButDoNotEqual() throws Exception {
    RelationEntities.EagerOneSide oneSide1 = new RelationEntities.EagerOneSide("One 1");
    oneSideDao.create(oneSide1);

    RelationEntities.EagerOneSide oneSide2 = new RelationEntities.EagerOneSide("One 2");
    oneSideDao.create(oneSide2);

    RelationEntities.EagerManySide manySide1 = new RelationEntities.EagerManySide("Many 1");
    RelationEntities.EagerManySide manySide2 = new RelationEntities.EagerManySide("Many 2");
    RelationEntities.EagerManySide manySide3 = new RelationEntities.EagerManySide("Many 3");
    RelationEntities.EagerManySide manySide4 = new RelationEntities.EagerManySide("Many 4");
    RelationEntities.EagerManySide manySide5 = new RelationEntities.EagerManySide("Many 5");
    RelationEntities.EagerManySide manySide6 = new RelationEntities.EagerManySide("Many 6");

    oneSide1.getManySides().add(manySide1);
    oneSide1.getManySides().add(manySide2);
    oneSide1.getManySides().add(manySide3);

    oneSide2.getManySides().add(manySide4);
    oneSide2.getManySides().add(manySide5);
    oneSide2.getManySides().add(manySide6);

    oneSideDao.clearObjectCache();
    manySideDao.clearObjectCache();
    List<RelationEntities.EagerOneSide> persistedEntities = oneSideDao.queryForAll();

    Assert.assertEquals(2, persistedEntities.size());

    RelationEntities.EagerOneSide persistedOneSide1 = persistedEntities.get(0);
    RelationEntities.EagerOneSide persistedOneSide2 = persistedEntities.get(1);

    // assert ids are the same
    Assert.assertEquals(oneSide1.getId(), persistedOneSide1.getId());
    Assert.assertEquals(oneSide2.getId(), persistedOneSide2.getId());
    // but instances don't equal (otherwise they would have been just taken from cache)
    Assert.assertNotSame(oneSide1, persistedOneSide1);
    Assert.assertNotSame(oneSide2, persistedOneSide2);

    Assert.assertEquals(3, persistedOneSide1.getManySides().size());
    Assert.assertEquals(3, persistedOneSide2.getManySides().size());

    Assert.assertEquals(manySide1.getId(), persistedOneSide1.getManySides().get(0).getId());
    Assert.assertEquals(manySide2.getId(), persistedOneSide1.getManySides().get(1).getId());
    Assert.assertEquals(manySide3.getId(), persistedOneSide1.getManySides().get(2).getId());

    Assert.assertEquals(manySide4.getId(), persistedOneSide2.getManySides().get(0).getId());
    Assert.assertEquals(manySide5.getId(), persistedOneSide2.getManySides().get(1).getId());
    Assert.assertEquals(manySide6.getId(), persistedOneSide2.getManySides().get(2).getId());

    Assert.assertNotSame(manySide1, persistedOneSide1.getManySides().get(0));
    Assert.assertNotSame(manySide2, persistedOneSide1.getManySides().get(1));
    Assert.assertNotSame(manySide3, persistedOneSide1.getManySides().get(2));

    Assert.assertNotSame(manySide4, persistedOneSide2.getManySides().get(0));
    Assert.assertNotSame(manySide5, persistedOneSide2.getManySides().get(1));
    Assert.assertNotSame(manySide6, persistedOneSide2.getManySides().get(2));
  }

  @Test
  public void addManySideEntities_ClearCacheQueryForAllAndAddSomeMoreEntities_TestIfLatestAddedEntitiesGetPersisted() throws Exception {
    RelationEntities.EagerOneSide oneSide1 = new RelationEntities.EagerOneSide("One 1");
    oneSideDao.create(oneSide1);

    RelationEntities.EagerOneSide oneSide2 = new RelationEntities.EagerOneSide("One 2");
    oneSideDao.create(oneSide2);

    RelationEntities.EagerManySide manySide1 = new RelationEntities.EagerManySide("Many 1");
    RelationEntities.EagerManySide manySide2 = new RelationEntities.EagerManySide("Many 2");
    RelationEntities.EagerManySide manySide3 = new RelationEntities.EagerManySide("Many 3");
    RelationEntities.EagerManySide manySide4 = new RelationEntities.EagerManySide("Many 4");
    RelationEntities.EagerManySide manySide5 = new RelationEntities.EagerManySide("Many 5");

    oneSide1.getManySides().add(manySide1);
    oneSide1.getManySides().add(manySide2);
    oneSide1.getManySides().add(manySide3);
    oneSide1.getManySides().add(manySide4);
    oneSide2.getManySides().add(manySide2);
    oneSide2.getManySides().add(manySide3);
    oneSide2.getManySides().add(manySide4);
    oneSide2.getManySides().add(manySide5);

    manySideDao.clearObjectCache();
    List<RelationEntities.EagerOneSide> persistedEntities = oneSideDao.queryForAll();

    RelationEntities.EagerOneSide oneSide3 = new RelationEntities.EagerOneSide("One 3");
    RelationEntities.EagerManySide manySide6 = new RelationEntities.EagerManySide("Many 6");
    RelationEntities.EagerManySide manySide7 = new RelationEntities.EagerManySide("Many 7");

    oneSide1.getManySides().add(manySide6);
    Assert.assertNotNull(manySide6.getId());
    Assert.assertEquals(oneSide1, manySide6.getOneSide());
    Assert.assertEquals(5, oneSide1.getManySides().size());

    oneSide1.getManySides().add(manySide7);
    Long manySide7Id = manySide7.getId();
    oneSide2.getManySides().add(manySide7);
    Assert.assertEquals(manySide7Id, manySide7.getId());
    Assert.assertEquals(6, oneSide1.getManySides().size());
    Assert.assertEquals(5, oneSide2.getManySides().size());

    oneSide3.getManySides().add(manySide6);
    oneSide3.getManySides().add(manySide7);

    oneSideDao.create(oneSide3);
    Assert.assertNotNull(oneSide3.getId());
    Assert.assertEquals(manySide7Id, manySide7.getId());
    Assert.assertEquals(2, oneSide3.getManySides().size());
  }

  @Test
  public void addManySideEntities_ClearCacheQueryForAllAndRemoveSomeEntities_TestIfEntitiesGetCorrectlyRemove() throws Exception {
    RelationEntities.EagerOneSide oneSide1 = new RelationEntities.EagerOneSide("One 1");
    oneSideDao.create(oneSide1);

    RelationEntities.EagerOneSide oneSide2 = new RelationEntities.EagerOneSide("One 2");
    oneSideDao.create(oneSide2);

    RelationEntities.EagerManySide manySide1 = new RelationEntities.EagerManySide("Many 1");
    RelationEntities.EagerManySide manySide2 = new RelationEntities.EagerManySide("Many 2");
    RelationEntities.EagerManySide manySide3 = new RelationEntities.EagerManySide("Many 3");
    RelationEntities.EagerManySide manySide4 = new RelationEntities.EagerManySide("Many 4");
    RelationEntities.EagerManySide manySide5 = new RelationEntities.EagerManySide("Many 5");
    RelationEntities.EagerManySide manySide6 = new RelationEntities.EagerManySide("Many 6");

    oneSide1.getManySides().add(manySide1);
    oneSide1.getManySides().add(manySide2);
    oneSide1.getManySides().add(manySide3);

    oneSide2.getManySides().add(manySide4);
    oneSide2.getManySides().add(manySide5);
    oneSide2.getManySides().add(manySide6);

    oneSideDao.clearObjectCache();
    manySideDao.clearObjectCache();

    GenericRawResults<String[]> rawResults = manySideDao.queryRaw("SELECT * FROM " + manySideDao.getEntityConfig().getTableName());
    List<String[]> debug = rawResults.getResults();

    List<RelationEntities.EagerOneSide> persistedEntities = oneSideDao.queryForAll();
    RelationEntities.EagerOneSide persistedOneSide1 = persistedEntities.get(0);
    RelationEntities.EagerOneSide persistedOneSide2 = persistedEntities.get(1);

    RelationEntities.EagerManySide persistedManySide2 = persistedOneSide1.getManySides().get(1);
    RelationEntities.EagerManySide persistedManySide3 = persistedOneSide1.getManySides().get(2);
    manySideDao.delete(persistedManySide2);
    manySideDao.delete(persistedManySide3);

    Assert.assertEquals(1, persistedOneSide1.getManySides().size());
    Assert.assertNull(persistedManySide2.getId());
    Assert.assertNull(persistedManySide2.getOneSide());
    Assert.assertNull(persistedManySide3.getId());
    Assert.assertNull(persistedManySide3.getOneSide());

    oneSideDao.delete(persistedOneSide2);
    Assert.assertNull(persistedOneSide2.getId());
    Assert.assertEquals(0, persistedOneSide2.getManySides().size());
  }

  @Test
  public void addOneSideEntity_TestIfCachingIsWorking() throws Exception {
    RelationEntities.EagerOneSide oneSide = new RelationEntities.EagerOneSide("One");
    oneSideDao.create(oneSide);

    RelationEntities.EagerManySide manySide1 = new RelationEntities.EagerManySide("Many 1");
    RelationEntities.EagerManySide manySide2 = new RelationEntities.EagerManySide("Many 2");
    RelationEntities.EagerManySide manySide3 = new RelationEntities.EagerManySide("Many 3");

    oneSide.getManySides().add(manySide1);
    oneSide.getManySides().add(manySide2);
    oneSide.getManySides().add(manySide3);

    int iteration = 0;
    while(iteration < 5) { // test if also after 5 iterations objects are the same
      RelationEntities.EagerOneSide persistedOneSide = oneSideDao.queryForId(oneSide.getId());
      Assert.assertSame(oneSide, persistedOneSide);

      Assert.assertSame(oneSide.getManySides().get(0), manySideDao.queryForId(manySide1.getId()));
      Assert.assertSame(oneSide.getManySides().get(1), manySideDao.queryForId(manySide2.getId()));
      Assert.assertSame(oneSide.getManySides().get(2), manySideDao.queryForId(manySide3.getId()));

      iteration++;
    }
  }

  @Test
  public void addOneSideEntity_ClearCache_TestForPersistedEntitiesIfCachingIsWorking() throws Exception {
    RelationEntities.EagerOneSide oneSide = new RelationEntities.EagerOneSide("One");
    oneSideDao.create(oneSide);

    RelationEntities.EagerManySide manySide1 = new RelationEntities.EagerManySide("Many 1");
    RelationEntities.EagerManySide manySide2 = new RelationEntities.EagerManySide("Many 2");
    RelationEntities.EagerManySide manySide3 = new RelationEntities.EagerManySide("Many 3");

    oneSide.getManySides().add(manySide1);
    oneSide.getManySides().add(manySide2);
    oneSide.getManySides().add(manySide3);

    oneSideDao.clearObjectCache();
    manySideDao.clearObjectCache();

    RelationEntities.EagerOneSide persistedOneSide = oneSideDao.queryForId(oneSide.getId());

    int iteration = 0;
    while(iteration < 5) { // test if also after 5 iterations objects are the same
      RelationEntities.EagerOneSide newlyRetrievedOneSide = oneSideDao.queryForId(oneSide.getId());
      Assert.assertSame(persistedOneSide, newlyRetrievedOneSide);

      Assert.assertSame(persistedOneSide.getManySides().get(0), manySideDao.queryForId(manySide1.getId()));
      Assert.assertSame(persistedOneSide.getManySides().get(1), manySideDao.queryForId(manySide2.getId()));
      Assert.assertSame(persistedOneSide.getManySides().get(2), manySideDao.queryForId(manySide3.getId()));

      iteration++;
    }
  }

  @Test
  public void addOneSideEntity_toArray_TestIfInstancesMatch() throws Exception {
    RelationEntities.EagerOneSide oneSide = new RelationEntities.EagerOneSide("One");
    oneSideDao.create(oneSide);

    RelationEntities.EagerManySide manySide1 = new RelationEntities.EagerManySide("Many 1");
    RelationEntities.EagerManySide manySide2 = new RelationEntities.EagerManySide("Many 2");
    RelationEntities.EagerManySide manySide3 = new RelationEntities.EagerManySide("Many 3");
    RelationEntities.EagerManySide manySide4 = new RelationEntities.EagerManySide("Many 4");
    RelationEntities.EagerManySide manySide5 = new RelationEntities.EagerManySide("Many 5");

    oneSide.getManySides().add(manySide1);
    oneSide.getManySides().add(manySide2);
    oneSide.getManySides().add(manySide3);
    oneSide.getManySides().add(manySide4);
    oneSide.getManySides().add(manySide5);

    Object[] array = oneSide.getManySides().toArray();

    Assert.assertEquals(oneSide.getManySides().size(), array.length);
    for(int i = 0; i < array.length; i++) {
      Assert.assertSame(array[i], oneSide.getManySides().get(i));
    }
  }

  @Test
  public void addOneSideEntity_toGenericArray_TestIfInstancesMatch() throws Exception {
    RelationEntities.EagerOneSide oneSide = new RelationEntities.EagerOneSide("One");
    oneSideDao.create(oneSide);

    RelationEntities.EagerManySide manySide1 = new RelationEntities.EagerManySide("Many 1");
    RelationEntities.EagerManySide manySide2 = new RelationEntities.EagerManySide("Many 2");
    RelationEntities.EagerManySide manySide3 = new RelationEntities.EagerManySide("Many 3");
    RelationEntities.EagerManySide manySide4 = new RelationEntities.EagerManySide("Many 4");
    RelationEntities.EagerManySide manySide5 = new RelationEntities.EagerManySide("Many 5");

    oneSide.getManySides().add(manySide1);
    oneSide.getManySides().add(manySide2);
    oneSide.getManySides().add(manySide3);
    oneSide.getManySides().add(manySide4);
    oneSide.getManySides().add(manySide5);

    RelationEntities.EagerManySide[] array = new RelationEntities.EagerManySide[oneSide.getManySides().size()];
    array = oneSide.getManySides().toArray(array);

    Assert.assertEquals(oneSide.getManySides().size(), array.length);
    for(int i = 0; i < array.length; i++) {
      Assert.assertSame(array[i], oneSide.getManySides().get(i));
    }
  }


  @Test
  public void testEntitesCollectionCreationForLazyRelation() throws Exception {
    Category category = new Category("Test Category");
    categoryDao.create(category);

    Assert.assertTrue(category.getSubCategories() instanceof LazyLoadingEntitiesCollection);
  }

  @Test
  public void testEntitesCollectionCreationForEagerRelation() throws Exception {
    Category category = new Category("Test Category");
    categoryDao.create(category);

    Assert.assertTrue(category.getEntries() instanceof EntitiesCollection);
  }

  @Test
  public void testRetrieveDataForLazyRelation() throws Exception {
    Category category = new Category("Test Category");
    category.getSubCategories().add(new Category("Sub 1"));
    category.getSubCategories().add(new Category("Sub 2"));
    categoryDao.create(category);

    categoryDao.clearObjectCache();
    PreparedQuery query = categoryDao.queryBuilder().where().isNull("parent_category_id").prepare();
    List<Category> persistedCategories = categoryDao.query(query);

    Assert.assertEquals(1, persistedCategories.size());

    for(Category persistedTopLevelCategory : persistedCategories) {
      Assert.assertTrue(persistedTopLevelCategory.getSubCategories() instanceof LazyLoadingEntitiesCollection);
      Assert.assertEquals(2, persistedTopLevelCategory.getSubCategories().size());

      for(Category persistedSubCategory : persistedTopLevelCategory.getSubCategories()) {
        Assert.assertNotNull(persistedSubCategory.getId());
        Assert.assertEquals(persistedTopLevelCategory, persistedSubCategory.getParentCategory());
      }
    }
  }

  @Test
  public void testRetrieveDataForEagerRelation() throws Exception {
    Category category = new Category("Test Category");
    category.getEntries().add(new Entry("Cat 1 Entry"));
    category.getEntries().add(new Entry("Cat 2 Entry"));
    categoryDao.create(category);

    categoryDao.clearObjectCache();
    entryDao.clearObjectCache();
    List<Category> persistedCategories = categoryDao.queryForAll();

    Assert.assertEquals(1, persistedCategories.size());

    for(Category persistedTopLevelCategory : persistedCategories) {
      Assert.assertTrue(persistedTopLevelCategory.getSubCategories() instanceof LazyLoadingEntitiesCollection);
      Assert.assertEquals(2, persistedTopLevelCategory.getEntries().size());

      for(Entry persistedEntry : persistedTopLevelCategory.getEntries()) {
        Assert.assertNotNull(persistedEntry.getId());
        Assert.assertEquals(persistedTopLevelCategory, persistedEntry.getCategory());
      }
    }
  }

  @Test
  public void testAddOneToManyRelationEntity() throws Exception {
    Entry entry = new Entry("Test entry");
    Category category = new Category("Test Category");
    category.getEntries().add(entry);
    categoryDao.create(category);

    Assert.assertEquals(1, category.getEntries().size());
    Assert.assertNotNull(entry.getCategory()); // check if foreign relation has been set
    Assert.assertNull(category.getParentCategory());
    Assert.assertEquals(0, category.getSubCategories().size());
    Assert.assertEquals(entry, category.getEntries().iterator().next()); // check if Entry instance in getEntries() equals entry
  }

  @Test
  public void testAddManyEntitiesToEagerLoadingOneToManyRelation() throws Exception {
    Category category = new Category("Test Category");

    category.getEntries().add(new Entry("Test entry 1"));
    category.getEntries().add(new Entry("Test entry 2"));
    category.getEntries().add(new Entry("Test entry 3"));
    category.getEntries().add(new Entry("Test entry 4"));
    category.getEntries().add(new Entry("Test entry 5"));

    categoryDao.create(category);

    Assert.assertEquals(5, category.getEntries().size());
  }

  @Test
  public void testAddManyEntitiesToLazyLoadingOneToManyRelation() throws Exception {
    Category category = new Category("Test Category");

    category.getSubCategories().add(new Category("Subcategory 1"));
    category.getSubCategories().add(new Category("Subcategory 2"));
    category.getSubCategories().add(new Category("Subcategory 3"));
    category.getSubCategories().add(new Category("Subcategory 4"));
    category.getSubCategories().add(new Category("Subcategory 5"));

    categoryDao.create(category);

    Assert.assertEquals(5, category.getSubCategories().size());
  }

  @Test
  public void testPersistManyToManyRelation() throws Exception {
    List<Entry> entries = buildEntryKeywordRelation();

    entryDao.create(entries);

    for(Entry entry : entries) {
      Assert.assertTrue(entry.getKeywords() instanceof EntitiesCollection);
      Assert.assertEquals(2, entry.getKeywords().size());

      for(Keyword keyword : entry.getKeywords()) {
        Assert.assertNotNull(keyword.getId());
        Assert.assertTrue(keyword.getEntries().contains(entry));
      }
    }
  }

  @Test
  public void testDeserializeManyToManyRelation() throws Exception {
    List<Entry> persistedEntries = buildEntryKeywordRelation();

    entryDao.create(persistedEntries);

    entryDao.clearObjectCache();
    keywordDao.clearObjectCache();

    List<Entry> deserializedEntries = entryDao.queryForAll();

    Assert.assertEquals(persistedEntries.size(), deserializedEntries.size());

    for(Entry entry : deserializedEntries) {
      Assert.assertNotNull(entry.getId());
      Assert.assertTrue(entry.getKeywords().size() > 0);

      for(Keyword keyword : entry.getKeywords()) {
        Assert.assertNotNull(keyword.getId());
        Assert.assertTrue(keyword.getEntries().contains(entry));
      }
    }
  }

  @Test
  public void testPersistHierarchy() throws Exception {
    Category topLevelCategory = new Category("Test Category");

    topLevelCategory.getSubCategories().add(new Category("Subcategory 1"));
    topLevelCategory.getSubCategories().add(new Category("Subcategory 2"));
    topLevelCategory.getSubCategories().add(new Category("Subcategory 3"));
    topLevelCategory.getSubCategories().add(new Category("Subcategory 4"));
    topLevelCategory.getSubCategories().add(new Category("Subcategory 5"));

    for(Category subCategory : topLevelCategory.getSubCategories()) {
      subCategory.getSubCategories().add(new Category("Third Level Category 1"));
      subCategory.getSubCategories().add(new Category("Third Level Category 2"));
    }

    categoryDao.create(topLevelCategory);

    for(Category subCategory : topLevelCategory.getSubCategories()) {
      Assert.assertEquals(topLevelCategory, subCategory.getParentCategory());
      Assert.assertEquals(2, subCategory.getSubCategories().size());

      for(Category thirdLevelCategory : subCategory.getSubCategories()) {
        Assert.assertEquals(subCategory, thirdLevelCategory.getParentCategory());
        Assert.assertEquals(0, thirdLevelCategory.getSubCategories().size());
      }
    }
  }


  protected List<Entry> buildEntryKeywordRelation() {
    List<Entry> entries = new ArrayList<>();

    entries.add(new Entry("Test Entry 1"));
    entries.add(new Entry("Test Entry 2"));
    entries.add(new Entry("Test Entry 3"));

    Keyword commonKeyword = new Keyword("Common Keyword");

    for(int i = 0; i < entries.size(); i++) {
      Entry entry = entries.get(i);

      entry.getKeywords().add(commonKeyword);
      entry.getKeywords().add(new Keyword("Entry specific Keyword " + i));
    }

    return entries;
  }


}

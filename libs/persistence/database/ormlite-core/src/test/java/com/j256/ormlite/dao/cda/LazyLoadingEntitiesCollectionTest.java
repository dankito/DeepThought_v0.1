package com.j256.ormlite.dao.cda;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.cda.testmodel.RelationEntities;
import com.j256.ormlite.dao.cda.testmodel.helper.OpenLazyLoadingEntitiesCollection;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * Created by ganymed on 15/10/14.
 */
public class LazyLoadingEntitiesCollectionTest extends EntitiesCollectionTestBase {

  protected Dao<RelationEntities.LazyOneSide, Long> oneSideDao = null;
  protected Dao<RelationEntities.LazyManySide, Long> manySideDao = null;


  @Override
  public void before() throws Exception {
    super.before();

    oneSideDao = createDao(RelationEntities.LazyOneSide.class, true, true);
    manySideDao = createDao(RelationEntities.LazyManySide.class, true, true);
  }


  @Test
  public void addOneSideEntity_ClearCacheQueryForAll_LazyCollectionRetrievedIdsButEntitiesNotCachedYet() throws Exception {
    RelationEntities.LazyOneSide oneSide = new RelationEntities.LazyOneSide("One");
    oneSideDao.create(oneSide);

    RelationEntities.LazyManySide manySide1 = new RelationEntities.LazyManySide("Many 1");
    RelationEntities.LazyManySide manySide2 = new RelationEntities.LazyManySide("Many 2");
    RelationEntities.LazyManySide manySide3 = new RelationEntities.LazyManySide("Many 3");
    RelationEntities.LazyManySide manySide4 = new RelationEntities.LazyManySide("Many 4");
    RelationEntities.LazyManySide manySide5 = new RelationEntities.LazyManySide("Many 5");

    oneSide.getManySides().add(manySide1);
    oneSide.getManySides().add(manySide2);
    oneSide.getManySides().add(manySide3);
    oneSide.getManySides().add(manySide4);
    oneSide.getManySides().add(manySide5);

    oneSideDao.clearObjectCache();
    manySideDao.clearObjectCache();

    RelationEntities.LazyOneSide persistedOneSide = oneSideDao.queryForId(oneSide.getId());
    OpenLazyLoadingEntitiesCollection persistedManySides = (OpenLazyLoadingEntitiesCollection)persistedOneSide.getManySides();

    Assert.assertEquals(oneSide.getManySides().size(), persistedManySides.getRetrievedIndices().size());
    Assert.assertEquals(0, persistedManySides.getCachedEntities().size());
  }

  @Test
  public void addOneSideEntity_ClearCacheQueryForAllThenGetEntity_EntityGottenIsNowCached() throws Exception {
    RelationEntities.LazyOneSide oneSide = new RelationEntities.LazyOneSide("One");
    oneSideDao.create(oneSide);

    RelationEntities.LazyManySide manySide1 = new RelationEntities.LazyManySide("Many 1");
    RelationEntities.LazyManySide manySide2 = new RelationEntities.LazyManySide("Many 2");
    RelationEntities.LazyManySide manySide3 = new RelationEntities.LazyManySide("Many 3");
    RelationEntities.LazyManySide manySide4 = new RelationEntities.LazyManySide("Many 4");
    RelationEntities.LazyManySide manySide5 = new RelationEntities.LazyManySide("Many 5");

    oneSide.getManySides().add(manySide1);
    oneSide.getManySides().add(manySide2);
    oneSide.getManySides().add(manySide3);
    oneSide.getManySides().add(manySide4);
    oneSide.getManySides().add(manySide5);

    oneSideDao.clearObjectCache();
    manySideDao.clearObjectCache();

    RelationEntities.LazyOneSide persistedOneSide = oneSideDao.queryForId(oneSide.getId());
    OpenLazyLoadingEntitiesCollection persistedManySides = (OpenLazyLoadingEntitiesCollection)persistedOneSide.getManySides();
    RelationEntities.LazyManySide anyEntity = persistedOneSide.getManySides().get(0);

    Assert.assertEquals(1, persistedManySides.getCachedEntities().size());
    Assert.assertTrue(persistedManySides.getCachedEntities().containsValue(anyEntity));
  }

  @Test
  public void removeEntity_RetrievedIdsAndCachedEntitiesDoNotContainThisEntityAnymore() throws Exception {
    RelationEntities.LazyOneSide oneSide = new RelationEntities.LazyOneSide("One");
    oneSideDao.create(oneSide);

    RelationEntities.LazyManySide manySide1 = new RelationEntities.LazyManySide("Many 1");
    RelationEntities.LazyManySide manySide2 = new RelationEntities.LazyManySide("Many 2");
    RelationEntities.LazyManySide manySide3 = new RelationEntities.LazyManySide("Many 3");

    oneSide.getManySides().add(manySide1);
    oneSide.getManySides().add(manySide2);
    oneSide.getManySides().add(manySide3);

    RelationEntities.LazyManySide removedEntity = manySide2;
    Long removedEntityId = removedEntity.getId();
    oneSide.getManySides().remove(removedEntity);

    OpenLazyLoadingEntitiesCollection persistedManySides = (OpenLazyLoadingEntitiesCollection)oneSide.getManySides();

    Assert.assertFalse(persistedManySides.getRetrievedIndices().contains(removedEntityId));
    Assert.assertFalse(persistedManySides.getCachedEntities().containsValue(removedEntity));
  }


  @Test
  public void creation_TestIfCollectionIsOfTypeEntitiesCollection() throws Exception {
    RelationEntities.LazyOneSide oneSide = new RelationEntities.LazyOneSide();
    oneSideDao.create(oneSide);

    Assert.assertTrue(oneSide.getManySides() instanceof LazyLoadingEntitiesCollection);
  }

  @Test
  public void addManySideEntityBeforeCreation_TestIfEntityHasCorrectlyBeenAdded() throws Exception {
    RelationEntities.LazyOneSide oneSide = new RelationEntities.LazyOneSide("One");

    RelationEntities.LazyManySide manySide = new RelationEntities.LazyManySide("Many 1");
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
    RelationEntities.LazyOneSide oneSide = new RelationEntities.LazyOneSide("One");
    oneSideDao.create(oneSide);

    RelationEntities.LazyManySide manySide = new RelationEntities.LazyManySide("Many 1");
    oneSide.getManySides().add(manySide);

    Assert.assertNotNull(oneSide.getId());
    Assert.assertNotNull(manySide.getId());

    Assert.assertEquals(1, oneSide.getManySides().size());
    Assert.assertEquals(oneSide, manySide.getOneSide());
    Assert.assertEquals(manySide, oneSide.getManySides().get(0));
  }

  @Test
  public void addManySideEntity_TestIfTableEntryCreated() throws Exception {
    RelationEntities.LazyOneSide oneSide = new RelationEntities.LazyOneSide("One");
    oneSideDao.create(oneSide);

    RelationEntities.LazyManySide manySide = new RelationEntities.LazyManySide("Many 1");
    oneSide.getManySides().add(manySide);

    manySideDao.clearObjectCache();
    RelationEntities.LazyManySide persistedEntity = manySideDao.queryForId(manySide.getId());

    Assert.assertNotNull(persistedEntity);
    Assert.assertNotSame(manySide, persistedEntity); // to ensure persistedEntity hasn't just been taken from cache
  }

  @Test
  public void removeManySideEntityBeforeCreation_TestIfEntityHasCorrectlyBeenRemoved() throws Exception {
    RelationEntities.LazyOneSide oneSide = new RelationEntities.LazyOneSide("One");

    RelationEntities.LazyManySide manySide = new RelationEntities.LazyManySide("Many 1");
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
    RelationEntities.LazyOneSide oneSide = new RelationEntities.LazyOneSide("One");

    RelationEntities.LazyManySide manySide = new RelationEntities.LazyManySide("Many 1");
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
    RelationEntities.LazyOneSide oneSide = new RelationEntities.LazyOneSide("One");

    RelationEntities.LazyManySide manySide = new RelationEntities.LazyManySide("Many 1");
    oneSide.getManySides().add(manySide);
    oneSideDao.create(oneSide);

    Long manySideId = manySide.getId();
    oneSide.getManySides().remove(manySide);

    manySideDao.clearObjectCache();
    RelationEntities.LazyManySide persistedEntity = manySideDao.queryForId(manySideId);
    Assert.assertNull(persistedEntity);
  }

  @Test
  public void addManySideEntities_ClearCacheAndQueryForAll_TestIfPersistedEntitiesMatchAddedOnesButDoNotEqual() throws Exception {
    RelationEntities.LazyOneSide oneSide1 = new RelationEntities.LazyOneSide("One 1");
    oneSideDao.create(oneSide1);

    RelationEntities.LazyOneSide oneSide2 = new RelationEntities.LazyOneSide("One 2");
    oneSideDao.create(oneSide2);

    RelationEntities.LazyManySide manySide1 = new RelationEntities.LazyManySide("Many 1");
    RelationEntities.LazyManySide manySide2 = new RelationEntities.LazyManySide("Many 2");
    RelationEntities.LazyManySide manySide3 = new RelationEntities.LazyManySide("Many 3");
    RelationEntities.LazyManySide manySide4 = new RelationEntities.LazyManySide("Many 4");
    RelationEntities.LazyManySide manySide5 = new RelationEntities.LazyManySide("Many 5");
    RelationEntities.LazyManySide manySide6 = new RelationEntities.LazyManySide("Many 6");

    oneSide1.getManySides().add(manySide1);
    oneSide1.getManySides().add(manySide2);
    oneSide1.getManySides().add(manySide3);

    oneSide2.getManySides().add(manySide4);
    oneSide2.getManySides().add(manySide5);
    oneSide2.getManySides().add(manySide6);

    oneSideDao.clearObjectCache();
    manySideDao.clearObjectCache();
    List<RelationEntities.LazyOneSide> persistedEntities = oneSideDao.queryForAll();

    Assert.assertEquals(2, persistedEntities.size());

    RelationEntities.LazyOneSide persistedOneSide1 = persistedEntities.get(0);
    RelationEntities.LazyOneSide persistedOneSide2 = persistedEntities.get(1);

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
    RelationEntities.LazyOneSide oneSide1 = new RelationEntities.LazyOneSide("One 1");
    oneSideDao.create(oneSide1);

    RelationEntities.LazyOneSide oneSide2 = new RelationEntities.LazyOneSide("One 2");
    oneSideDao.create(oneSide2);

    RelationEntities.LazyManySide manySide1 = new RelationEntities.LazyManySide("Many 1");
    RelationEntities.LazyManySide manySide2 = new RelationEntities.LazyManySide("Many 2");
    RelationEntities.LazyManySide manySide3 = new RelationEntities.LazyManySide("Many 3");
    RelationEntities.LazyManySide manySide4 = new RelationEntities.LazyManySide("Many 4");
    RelationEntities.LazyManySide manySide5 = new RelationEntities.LazyManySide("Many 5");

    oneSide1.getManySides().add(manySide1);
    oneSide1.getManySides().add(manySide2);
    oneSide1.getManySides().add(manySide3);
    oneSide1.getManySides().add(manySide4);
    oneSide2.getManySides().add(manySide2);
    oneSide2.getManySides().add(manySide3);
    oneSide2.getManySides().add(manySide4);
    oneSide2.getManySides().add(manySide5);

    manySideDao.clearObjectCache();
    List<RelationEntities.LazyOneSide> persistedEntities = oneSideDao.queryForAll();

    RelationEntities.LazyOneSide oneSide3 = new RelationEntities.LazyOneSide("One 3");
    RelationEntities.LazyManySide manySide6 = new RelationEntities.LazyManySide("Many 6");
    RelationEntities.LazyManySide manySide7 = new RelationEntities.LazyManySide("Many 7");

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
    RelationEntities.LazyOneSide oneSide1 = new RelationEntities.LazyOneSide("One 1");
    oneSideDao.create(oneSide1);

    RelationEntities.LazyOneSide oneSide2 = new RelationEntities.LazyOneSide("One 2");
    oneSideDao.create(oneSide2);

    RelationEntities.LazyManySide manySide1 = new RelationEntities.LazyManySide("Many 1");
    RelationEntities.LazyManySide manySide2 = new RelationEntities.LazyManySide("Many 2");
    RelationEntities.LazyManySide manySide3 = new RelationEntities.LazyManySide("Many 3");
    RelationEntities.LazyManySide manySide4 = new RelationEntities.LazyManySide("Many 4");
    RelationEntities.LazyManySide manySide5 = new RelationEntities.LazyManySide("Many 5");
    RelationEntities.LazyManySide manySide6 = new RelationEntities.LazyManySide("Many 6");

    oneSide1.getManySides().add(manySide1);
    oneSide1.getManySides().add(manySide2);
    oneSide1.getManySides().add(manySide3);

    oneSide2.getManySides().add(manySide4);
    oneSide2.getManySides().add(manySide5);
    oneSide2.getManySides().add(manySide6);

    oneSideDao.clearObjectCache();
    manySideDao.clearObjectCache();

    List<RelationEntities.LazyOneSide> persistedEntities = oneSideDao.queryForAll();
    RelationEntities.LazyOneSide persistedOneSide1 = persistedEntities.get(0);
    RelationEntities.LazyOneSide persistedOneSide2 = persistedEntities.get(1);

    RelationEntities.LazyManySide persistedManySide2 = persistedOneSide1.getManySides().get(1);
    RelationEntities.LazyManySide persistedManySide3 = persistedOneSide1.getManySides().get(2);
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
    RelationEntities.LazyOneSide oneSide = new RelationEntities.LazyOneSide("One");
    oneSideDao.create(oneSide);

    RelationEntities.LazyManySide manySide1 = new RelationEntities.LazyManySide("Many 1");
    RelationEntities.LazyManySide manySide2 = new RelationEntities.LazyManySide("Many 2");
    RelationEntities.LazyManySide manySide3 = new RelationEntities.LazyManySide("Many 3");

    oneSide.getManySides().add(manySide1);
    oneSide.getManySides().add(manySide2);
    oneSide.getManySides().add(manySide3);

    int iteration = 0;
    while(iteration < 5) { // test if also after 5 iterations objects are the same
      RelationEntities.LazyOneSide persistedOneSide = oneSideDao.queryForId(oneSide.getId());
      Assert.assertSame(oneSide, persistedOneSide);

      Assert.assertSame(oneSide.getManySides().get(0), manySideDao.queryForId(manySide1.getId()));
      Assert.assertSame(oneSide.getManySides().get(1), manySideDao.queryForId(manySide2.getId()));
      Assert.assertSame(oneSide.getManySides().get(2), manySideDao.queryForId(manySide3.getId()));

      iteration++;
    }
  }

  @Test
  public void addOneSideEntity_ClearCache_TestForPersistedEntitiesIfCachingIsWorking() throws Exception {
    RelationEntities.LazyOneSide oneSide = new RelationEntities.LazyOneSide("One");
    oneSideDao.create(oneSide);

    RelationEntities.LazyManySide manySide1 = new RelationEntities.LazyManySide("Many 1");
    RelationEntities.LazyManySide manySide2 = new RelationEntities.LazyManySide("Many 2");
    RelationEntities.LazyManySide manySide3 = new RelationEntities.LazyManySide("Many 3");

    oneSide.getManySides().add(manySide1);
    oneSide.getManySides().add(manySide2);
    oneSide.getManySides().add(manySide3);

    oneSideDao.clearObjectCache();
    manySideDao.clearObjectCache();

    RelationEntities.LazyOneSide persistedOneSide = oneSideDao.queryForId(oneSide.getId());

    int iteration = 0;
    while(iteration < 5) { // test if also after 5 iterations objects are the same
      RelationEntities.LazyOneSide newlyRetrievedOneSide = oneSideDao.queryForId(oneSide.getId());
      Assert.assertSame(persistedOneSide, newlyRetrievedOneSide);

      Assert.assertSame(persistedOneSide.getManySides().get(0), manySideDao.queryForId(manySide1.getId()));
      Assert.assertSame(persistedOneSide.getManySides().get(1), manySideDao.queryForId(manySide2.getId()));
      Assert.assertSame(persistedOneSide.getManySides().get(2), manySideDao.queryForId(manySide3.getId()));

      iteration++;
    }
  }

  @Test
  public void addOneSideEntity_toArray_TestIfInstancesMatch() throws Exception {
    RelationEntities.LazyOneSide oneSide = new RelationEntities.LazyOneSide("One");
    oneSideDao.create(oneSide);

    RelationEntities.LazyManySide manySide1 = new RelationEntities.LazyManySide("Many 1");
    RelationEntities.LazyManySide manySide2 = new RelationEntities.LazyManySide("Many 2");
    RelationEntities.LazyManySide manySide3 = new RelationEntities.LazyManySide("Many 3");
    RelationEntities.LazyManySide manySide4 = new RelationEntities.LazyManySide("Many 4");
    RelationEntities.LazyManySide manySide5 = new RelationEntities.LazyManySide("Many 5");

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
    RelationEntities.LazyOneSide oneSide = new RelationEntities.LazyOneSide("One");
    oneSideDao.create(oneSide);

    RelationEntities.LazyManySide manySide1 = new RelationEntities.LazyManySide("Many 1");
    RelationEntities.LazyManySide manySide2 = new RelationEntities.LazyManySide("Many 2");
    RelationEntities.LazyManySide manySide3 = new RelationEntities.LazyManySide("Many 3");
    RelationEntities.LazyManySide manySide4 = new RelationEntities.LazyManySide("Many 4");
    RelationEntities.LazyManySide manySide5 = new RelationEntities.LazyManySide("Many 5");

    oneSide.getManySides().add(manySide1);
    oneSide.getManySides().add(manySide2);
    oneSide.getManySides().add(manySide3);
    oneSide.getManySides().add(manySide4);
    oneSide.getManySides().add(manySide5);

    RelationEntities.LazyManySide[] array = new RelationEntities.LazyManySide[oneSide.getManySides().size()];
    array = oneSide.getManySides().toArray(array);

    Assert.assertEquals(oneSide.getManySides().size(), array.length);
    for(int i = 0; i < array.length; i++) {
      Assert.assertSame(array[i], oneSide.getManySides().get(i));
    }
  }

}

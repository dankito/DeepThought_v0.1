package com.j256.ormlite.dao.cda;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.cda.testmodel.ManyToManyEntities;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ganymed on 02/11/14.
 */
public class ManyToManyEntitiesCollectionTest extends EntitiesCollectionTestBase {

  protected Dao<ManyToManyEntities.EagerOwningSide, Long> owningSideDao = null;
  protected Dao<ManyToManyEntities.EagerInverseSide, Long> inverseSideDao = null;


  @Override
  public void before() throws Exception {
    super.before();

    owningSideDao = createDao(ManyToManyEntities.EagerOwningSide.class, true, true);
    inverseSideDao = createDao(ManyToManyEntities.EagerInverseSide.class, true, true);
  }

  @Override
  public void after() throws Exception {
    super.after();

    owningSideDao.clearObjectCache();
    inverseSideDao.clearObjectCache();
    if(owningSideDao.getConnectionSource() != null)
      owningSideDao.getConnectionSource().close();
    if(inverseSideDao.getConnectionSource() != null)
      inverseSideDao.getConnectionSource().close();
    owningSideDao = null;
    inverseSideDao = null;
  }


  @Test
  public void creation_TestIfCollectionIsOfTypeEntitiesCollection() throws Exception {
    ManyToManyEntities.EagerOwningSide owningSide = new ManyToManyEntities.EagerOwningSide();
    owningSideDao.create(owningSide);

    Assert.assertEquals(ManyToManyEntitiesCollection.class, owningSide.getInverseSides().getClass());
  }

  @Test
  public void addManySideEntityBeforeCreation_TestIfEntityHasCorrectlyBeenAdded() throws Exception {
    ManyToManyEntities.EagerOwningSide owningSide = new ManyToManyEntities.EagerOwningSide("One");

    ManyToManyEntities.EagerInverseSide inverseSide = new ManyToManyEntities.EagerInverseSide("Many 1");
    owningSide.getInverseSides().add(inverseSide);

    owningSideDao.create(owningSide);

    Assert.assertNotNull(owningSide.getId());
    Assert.assertNotNull(inverseSide.getId());

    Assert.assertEquals(1, owningSide.getInverseSides().size());
    Assert.assertTrue(inverseSide.getOwningSides().contains(owningSide));
    Assert.assertEquals(inverseSide, owningSide.getInverseSides().get(0));
  }

  @Test
  public void addManySideEntityAfterCreation_TestIfEntityHasCorrectlyBeenAdded() throws Exception {
    ManyToManyEntities.EagerOwningSide owningSide = new ManyToManyEntities.EagerOwningSide("One");
    owningSideDao.create(owningSide);

    ManyToManyEntities.EagerInverseSide inverseSide = new ManyToManyEntities.EagerInverseSide("Many 1");
    owningSide.getInverseSides().add(inverseSide);

    Assert.assertNotNull(owningSide.getId());
    Assert.assertNotNull(inverseSide.getId());

    Assert.assertEquals(1, owningSide.getInverseSides().size());
    Assert.assertTrue(inverseSide.getOwningSides().contains(owningSide));
    Assert.assertEquals(inverseSide, owningSide.getInverseSides().get(0));
  }

  @Test
  public void addManySideEntity_TestIfTableEntryCreated() throws Exception {
    ManyToManyEntities.EagerOwningSide owningSide = new ManyToManyEntities.EagerOwningSide("One");
    owningSideDao.create(owningSide);

    ManyToManyEntities.EagerInverseSide inverseSide = new ManyToManyEntities.EagerInverseSide("Many 1");
    owningSide.getInverseSides().add(inverseSide);

    inverseSideDao.clearObjectCache();
    ManyToManyEntities.EagerInverseSide persistedEntity = inverseSideDao.queryForId(inverseSide.getId());

    Assert.assertNotNull(persistedEntity);
    Assert.assertNotSame(inverseSide, persistedEntity); // to ensure persistedEntity hasn't just been taken from cache
  }

  @Test
  public void removeManySideEntityBeforeCreation_TestIfEntityHasCorrectlyBeenRemoved() throws Exception {
    ManyToManyEntities.EagerOwningSide owningSide = new ManyToManyEntities.EagerOwningSide("One");

    ManyToManyEntities.EagerInverseSide inverseSide = new ManyToManyEntities.EagerInverseSide("Many 1");
    owningSide.getInverseSides().add(inverseSide);
    owningSide.getInverseSides().remove(inverseSide);

    owningSideDao.create(owningSide);

    Assert.assertNotNull(owningSide.getId());
    Assert.assertNull(inverseSide.getId());

    Assert.assertEquals(0, owningSide.getInverseSides().size());
    Assert.assertEquals(0, inverseSide.getOwningSides().size());
  }

  @Test
  public void removeManySideEntityAfterCreation_TestIfEntityHasCorrectlyBeenRemoved() throws Exception {
    ManyToManyEntities.EagerOwningSide owningSide = new ManyToManyEntities.EagerOwningSide("One");

    ManyToManyEntities.EagerInverseSide inverseSide = new ManyToManyEntities.EagerInverseSide("Many 1");
    owningSide.getInverseSides().add(inverseSide);
    owningSideDao.create(owningSide);

    owningSide.getInverseSides().remove(inverseSide);

    Assert.assertNotNull(owningSide.getId());
    Assert.assertNull(inverseSide.getId());

    Assert.assertEquals(0, owningSide.getInverseSides().size());
    Assert.assertEquals(0, inverseSide.getOwningSides().size());
  }

  @Test
  public void addManySideEntities_ClearCacheAndQueryForAll_TestIfPersistedEntitiesMatchAddedOnesButDoNotEqual() throws Exception {
    ManyToManyEntities.EagerOwningSide oneSide1 = new ManyToManyEntities.EagerOwningSide("One 1");
    owningSideDao.create(oneSide1);

    ManyToManyEntities.EagerOwningSide oneSide2 = new ManyToManyEntities.EagerOwningSide("One 2");
    owningSideDao.create(oneSide2);

    ManyToManyEntities.EagerInverseSide inverseSide1 = new ManyToManyEntities.EagerInverseSide("Many 1");
    ManyToManyEntities.EagerInverseSide inverseSide2 = new ManyToManyEntities.EagerInverseSide("Many 2");
    ManyToManyEntities.EagerInverseSide inverseSide3 = new ManyToManyEntities.EagerInverseSide("Many 3");
    ManyToManyEntities.EagerInverseSide inverseSide4 = new ManyToManyEntities.EagerInverseSide("Many 4");
    ManyToManyEntities.EagerInverseSide inverseSide5 = new ManyToManyEntities.EagerInverseSide("Many 5");
    ManyToManyEntities.EagerInverseSide inverseSide6 = new ManyToManyEntities.EagerInverseSide("Many 6");

    oneSide1.getInverseSides().add(inverseSide1);
    oneSide1.getInverseSides().add(inverseSide2);
    oneSide1.getInverseSides().add(inverseSide3);

    oneSide2.getInverseSides().add(inverseSide4);
    oneSide2.getInverseSides().add(inverseSide5);
    oneSide2.getInverseSides().add(inverseSide6);

    owningSideDao.clearObjectCache();
    inverseSideDao.clearObjectCache();
    List<ManyToManyEntities.EagerOwningSide> persistedEntities = owningSideDao.queryForAll();

    Assert.assertEquals(2, persistedEntities.size());

    ManyToManyEntities.EagerOwningSide persistedOneSide1 = persistedEntities.get(0);
    ManyToManyEntities.EagerOwningSide persistedOneSide2 = persistedEntities.get(1);

    // assert ids are the same
    Assert.assertEquals(oneSide1.getId(), persistedOneSide1.getId());
    Assert.assertEquals(oneSide2.getId(), persistedOneSide2.getId());
    // but instances don't equal (otherwise they would have been just taken from cache)
    Assert.assertNotSame(oneSide1, persistedOneSide1);
    Assert.assertNotSame(oneSide2, persistedOneSide2);

    Assert.assertEquals(3, persistedOneSide1.getInverseSides().size());
    Assert.assertEquals(3, persistedOneSide2.getInverseSides().size());

    Assert.assertEquals(inverseSide1.getId(), persistedOneSide1.getInverseSides().get(0).getId());
    Assert.assertEquals(inverseSide2.getId(), persistedOneSide1.getInverseSides().get(1).getId());
    Assert.assertEquals(inverseSide3.getId(), persistedOneSide1.getInverseSides().get(2).getId());

    Assert.assertEquals(inverseSide4.getId(), persistedOneSide2.getInverseSides().get(0).getId());
    Assert.assertEquals(inverseSide5.getId(), persistedOneSide2.getInverseSides().get(1).getId());
    Assert.assertEquals(inverseSide6.getId(), persistedOneSide2.getInverseSides().get(2).getId());

    Assert.assertNotSame(inverseSide1, persistedOneSide1.getInverseSides().get(0));
    Assert.assertNotSame(inverseSide2, persistedOneSide1.getInverseSides().get(1));
    Assert.assertNotSame(inverseSide3, persistedOneSide1.getInverseSides().get(2));

    Assert.assertNotSame(inverseSide4, persistedOneSide2.getInverseSides().get(0));
    Assert.assertNotSame(inverseSide5, persistedOneSide2.getInverseSides().get(1));
    Assert.assertNotSame(inverseSide6, persistedOneSide2.getInverseSides().get(2));
  }

  @Test
  public void addManySideEntities_ClearCacheQueryForAllAndAddSomeMoreEntities_TestIfLatestAddedEntitiesGetPersisted() throws Exception {
    ManyToManyEntities.EagerOwningSide oneSide1 = new ManyToManyEntities.EagerOwningSide("One 1");
    owningSideDao.create(oneSide1);

    ManyToManyEntities.EagerOwningSide oneSide2 = new ManyToManyEntities.EagerOwningSide("One 2");
    owningSideDao.create(oneSide2);

    ManyToManyEntities.EagerInverseSide inverseSide1 = new ManyToManyEntities.EagerInverseSide("Many 1");
    ManyToManyEntities.EagerInverseSide inverseSide2 = new ManyToManyEntities.EagerInverseSide("Many 2");
    ManyToManyEntities.EagerInverseSide inverseSide3 = new ManyToManyEntities.EagerInverseSide("Many 3");
    ManyToManyEntities.EagerInverseSide inverseSide4 = new ManyToManyEntities.EagerInverseSide("Many 4");
    ManyToManyEntities.EagerInverseSide inverseSide5 = new ManyToManyEntities.EagerInverseSide("Many 5");

    oneSide1.getInverseSides().add(inverseSide1);
    oneSide1.getInverseSides().add(inverseSide2);
    oneSide1.getInverseSides().add(inverseSide3);
    oneSide1.getInverseSides().add(inverseSide4);
    oneSide2.getInverseSides().add(inverseSide2);
    oneSide2.getInverseSides().add(inverseSide3);
    oneSide2.getInverseSides().add(inverseSide4);
    oneSide2.getInverseSides().add(inverseSide5);

    inverseSideDao.clearObjectCache();
    List<ManyToManyEntities.EagerOwningSide> persistedEntities = owningSideDao.queryForAll();

    ManyToManyEntities.EagerOwningSide oneSide3 = new ManyToManyEntities.EagerOwningSide("One 3");
    ManyToManyEntities.EagerInverseSide inverseSide6 = new ManyToManyEntities.EagerInverseSide("Many 6");
    ManyToManyEntities.EagerInverseSide inverseSide7 = new ManyToManyEntities.EagerInverseSide("Many 7");

    oneSide1.getInverseSides().add(inverseSide6);
    Assert.assertNotNull(inverseSide6.getId());
    Assert.assertTrue(inverseSide6.getOwningSides().contains(oneSide1));
    Assert.assertEquals(5, oneSide1.getInverseSides().size());

    oneSide1.getInverseSides().add(inverseSide7);
    Long inverseSide7Id = inverseSide7.getId();
    oneSide2.getInverseSides().add(inverseSide7);
    Assert.assertEquals(inverseSide7Id, inverseSide7.getId());
    Assert.assertEquals(6, oneSide1.getInverseSides().size());
    Assert.assertEquals(5, oneSide2.getInverseSides().size());

    oneSide3.getInverseSides().add(inverseSide6);
    oneSide3.getInverseSides().add(inverseSide7);

    owningSideDao.create(oneSide3);
    Assert.assertNotNull(oneSide3.getId());
    Assert.assertEquals(inverseSide7Id, inverseSide7.getId());
    Assert.assertEquals(2, oneSide3.getInverseSides().size());
  }

  @Test
  public void addManySideEntities_ClearCacheQueryForAllAndRemoveSomeEntities_TestIfEntitiesGetCorrectlyRemove() throws Exception {
    ManyToManyEntities.EagerOwningSide oneSide1 = new ManyToManyEntities.EagerOwningSide("One 1");
    owningSideDao.create(oneSide1);

    ManyToManyEntities.EagerOwningSide oneSide2 = new ManyToManyEntities.EagerOwningSide("One 2");
    owningSideDao.create(oneSide2);

    ManyToManyEntities.EagerInverseSide inverseSide1 = new ManyToManyEntities.EagerInverseSide("Many 1");
    ManyToManyEntities.EagerInverseSide inverseSide2 = new ManyToManyEntities.EagerInverseSide("Many 2");
    ManyToManyEntities.EagerInverseSide inverseSide3 = new ManyToManyEntities.EagerInverseSide("Many 3");
    ManyToManyEntities.EagerInverseSide inverseSide4 = new ManyToManyEntities.EagerInverseSide("Many 4");
    ManyToManyEntities.EagerInverseSide inverseSide5 = new ManyToManyEntities.EagerInverseSide("Many 5");
    ManyToManyEntities.EagerInverseSide inverseSide6 = new ManyToManyEntities.EagerInverseSide("Many 6");

    oneSide1.getInverseSides().add(inverseSide1);
    oneSide1.getInverseSides().add(inverseSide2);
    oneSide1.getInverseSides().add(inverseSide3);

    oneSide2.getInverseSides().add(inverseSide4);
    oneSide2.getInverseSides().add(inverseSide5);
    oneSide2.getInverseSides().add(inverseSide6);

    owningSideDao.clearObjectCache();
    inverseSideDao.clearObjectCache();

    List<ManyToManyEntities.EagerOwningSide> persistedEntities = owningSideDao.queryForAll();
    ManyToManyEntities.EagerOwningSide persistedOneSide1 = persistedEntities.get(0);
    ManyToManyEntities.EagerOwningSide persistedOneSide2 = persistedEntities.get(1);

    ManyToManyEntities.EagerInverseSide persistedManySide2 = persistedOneSide1.getInverseSides().get(1);
    ManyToManyEntities.EagerInverseSide persistedManySide3 = persistedOneSide1.getInverseSides().get(2);
    inverseSideDao.delete(persistedManySide2);
    inverseSideDao.delete(persistedManySide3);

    Assert.assertEquals(1, persistedOneSide1.getInverseSides().size());
    Assert.assertNull(persistedManySide2.getId());
    Assert.assertEquals(0, persistedManySide2.getOwningSides().size());
    Assert.assertNull(persistedManySide3.getId());
    Assert.assertEquals(0, persistedManySide3.getOwningSides().size());

    owningSideDao.delete(persistedOneSide2);
    Assert.assertNull(persistedOneSide2.getId());
    Assert.assertEquals(0, persistedOneSide2.getInverseSides().size());
  }

  @Test
  public void addOneSideEntity_TestIfCachingIsWorking() throws Exception {
    ManyToManyEntities.EagerOwningSide owningSide = new ManyToManyEntities.EagerOwningSide("One");
    owningSideDao.create(owningSide);

    ManyToManyEntities.EagerInverseSide inverseSide1 = new ManyToManyEntities.EagerInverseSide("Many 1");
    ManyToManyEntities.EagerInverseSide inverseSide2 = new ManyToManyEntities.EagerInverseSide("Many 2");
    ManyToManyEntities.EagerInverseSide inverseSide3 = new ManyToManyEntities.EagerInverseSide("Many 3");

    owningSide.getInverseSides().add(inverseSide1);
    owningSide.getInverseSides().add(inverseSide2);
    owningSide.getInverseSides().add(inverseSide3);

    int iteration = 0;
    while(iteration < 5) { // test if also after 5 iterations objects are the same
      ManyToManyEntities.EagerOwningSide persistedOneSide = owningSideDao.queryForId(owningSide.getId());
      Assert.assertSame(owningSide, persistedOneSide);

      Assert.assertSame(owningSide.getInverseSides().get(0), inverseSideDao.queryForId(inverseSide1.getId()));
      Assert.assertSame(owningSide.getInverseSides().get(1), inverseSideDao.queryForId(inverseSide2.getId()));
      Assert.assertSame(owningSide.getInverseSides().get(2), inverseSideDao.queryForId(inverseSide3.getId()));

      iteration++;
    }
  }

  @Test
  public void addOneSideEntity_ClearCache_TestForPersistedEntitiesIfCachingIsWorking() throws Exception {
    ManyToManyEntities.EagerOwningSide owningSide = new ManyToManyEntities.EagerOwningSide("One");
    owningSideDao.create(owningSide);

    ManyToManyEntities.EagerInverseSide inverseSide1 = new ManyToManyEntities.EagerInverseSide("Many 1");
    ManyToManyEntities.EagerInverseSide inverseSide2 = new ManyToManyEntities.EagerInverseSide("Many 2");
    ManyToManyEntities.EagerInverseSide inverseSide3 = new ManyToManyEntities.EagerInverseSide("Many 3");

    owningSide.getInverseSides().add(inverseSide1);
    owningSide.getInverseSides().add(inverseSide2);
    owningSide.getInverseSides().add(inverseSide3);

    owningSideDao.clearObjectCache();
    inverseSideDao.clearObjectCache();

    ManyToManyEntities.EagerOwningSide persistedOneSide = owningSideDao.queryForId(owningSide.getId());

    int iteration = 0;
    while(iteration < 5) { // test if also after 5 iterations objects are the same
      ManyToManyEntities.EagerOwningSide newlyRetrievedOneSide = owningSideDao.queryForId(owningSide.getId());
      Assert.assertSame(persistedOneSide, newlyRetrievedOneSide);

      Assert.assertSame(persistedOneSide.getInverseSides().get(0), inverseSideDao.queryForId(inverseSide1.getId()));
      Assert.assertSame(persistedOneSide.getInverseSides().get(1), inverseSideDao.queryForId(inverseSide2.getId()));
      Assert.assertSame(persistedOneSide.getInverseSides().get(2), inverseSideDao.queryForId(inverseSide3.getId()));

      iteration++;
    }
  }

  @Test
  public void addOneSideEntity_toArray_TestIfInstancesMatch() throws Exception {
    ManyToManyEntities.EagerOwningSide owningSide = new ManyToManyEntities.EagerOwningSide("One");
    owningSideDao.create(owningSide);

    ManyToManyEntities.EagerInverseSide inverseSide1 = new ManyToManyEntities.EagerInverseSide("Many 1");
    ManyToManyEntities.EagerInverseSide inverseSide2 = new ManyToManyEntities.EagerInverseSide("Many 2");
    ManyToManyEntities.EagerInverseSide inverseSide3 = new ManyToManyEntities.EagerInverseSide("Many 3");
    ManyToManyEntities.EagerInverseSide inverseSide4 = new ManyToManyEntities.EagerInverseSide("Many 4");
    ManyToManyEntities.EagerInverseSide inverseSide5 = new ManyToManyEntities.EagerInverseSide("Many 5");

    owningSide.getInverseSides().add(inverseSide1);
    owningSide.getInverseSides().add(inverseSide2);
    owningSide.getInverseSides().add(inverseSide3);
    owningSide.getInverseSides().add(inverseSide4);
    owningSide.getInverseSides().add(inverseSide5);

    Object[] array = owningSide.getInverseSides().toArray();

    Assert.assertEquals(owningSide.getInverseSides().size(), array.length);
    for(int i = 0; i < array.length; i++) {
      Assert.assertSame(array[i], owningSide.getInverseSides().get(i));
    }
  }

  @Test
  public void addOneSideEntity_toGenericArray_TestIfInstancesMatch() throws Exception {
    ManyToManyEntities.EagerOwningSide owningSide = new ManyToManyEntities.EagerOwningSide("One");
    owningSideDao.create(owningSide);

    ManyToManyEntities.EagerInverseSide inverseSide1 = new ManyToManyEntities.EagerInverseSide("Many 1");
    ManyToManyEntities.EagerInverseSide inverseSide2 = new ManyToManyEntities.EagerInverseSide("Many 2");
    ManyToManyEntities.EagerInverseSide inverseSide3 = new ManyToManyEntities.EagerInverseSide("Many 3");
    ManyToManyEntities.EagerInverseSide inverseSide4 = new ManyToManyEntities.EagerInverseSide("Many 4");
    ManyToManyEntities.EagerInverseSide inverseSide5 = new ManyToManyEntities.EagerInverseSide("Many 5");

    owningSide.getInverseSides().add(inverseSide1);
    owningSide.getInverseSides().add(inverseSide2);
    owningSide.getInverseSides().add(inverseSide3);
    owningSide.getInverseSides().add(inverseSide4);
    owningSide.getInverseSides().add(inverseSide5);

    ManyToManyEntities.EagerInverseSide[] array = new ManyToManyEntities.EagerInverseSide[owningSide.getInverseSides().size()];
    array = owningSide.getInverseSides().toArray(array);

    Assert.assertEquals(owningSide.getInverseSides().size(), array.length);
    for(int i = 0; i < array.length; i++) {
      Assert.assertSame(array[i], owningSide.getInverseSides().get(i));
    }
  }


  @Test
  public void testEagerManyToManyCreation() throws Exception {
    // the actual test is here, if dao and therefore JoinTable can be created without errors
    Dao<ManyToManyEntities.EagerOwningSide, Long> owningSideDao = createDao(ManyToManyEntities.EagerOwningSide.class, true, true);
    Dao<ManyToManyEntities.EagerInverseSide, Long> inverseSideDao = createDao(ManyToManyEntities.EagerInverseSide.class, true, true);

    ManyToManyEntities.EagerOwningSide owningSideEntity = new ManyToManyEntities.EagerOwningSide("Owning side");
    ManyToManyEntities.EagerInverseSide inverseSideEntity = new ManyToManyEntities.EagerInverseSide("Inverse side");
    owningSideEntity.getInverseSides().add(inverseSideEntity);
    owningSideDao.create(owningSideEntity);

    Assert.assertNotNull(owningSideEntity.getId());
    Assert.assertNotNull(inverseSideEntity.getId());
    Assert.assertTrue(owningSideEntity.getInverseSides().contains(inverseSideEntity));
    Assert.assertTrue(inverseSideEntity.getOwningSides().contains(owningSideEntity));
    Assert.assertEquals(1, owningSideEntity.getInverseSides().size());
    Assert.assertEquals(1, inverseSideEntity.getOwningSides().size());
  }

  @Test
  public void testRetrieveEagerManyToMany() throws Exception {
    // the actual test is here, if dao and therefore JoinTable can be created without errors
    Dao<ManyToManyEntities.EagerOwningSide, Long> owningSideDao = createDao(ManyToManyEntities.EagerOwningSide.class, true, true);
    Dao<ManyToManyEntities.EagerInverseSide, Long> inverseSideDao = createDao(ManyToManyEntities.EagerInverseSide.class, true, true);

    ManyToManyEntities.EagerOwningSide owningSideEntity1 = new ManyToManyEntities.EagerOwningSide("Owning side 1");
    ManyToManyEntities.EagerOwningSide owningSideEntity2 = new ManyToManyEntities.EagerOwningSide("Owning side 2");

    ManyToManyEntities.EagerInverseSide inverseSideEntity1 = new ManyToManyEntities.EagerInverseSide("Inverse side 1");
    ManyToManyEntities.EagerInverseSide inverseSideEntity2 = new ManyToManyEntities.EagerInverseSide("Inverse side 2");
    ManyToManyEntities.EagerInverseSide inverseSideEntity3 = new ManyToManyEntities.EagerInverseSide("Inverse side 3");
    ManyToManyEntities.EagerInverseSide inverseSideEntity4 = new ManyToManyEntities.EagerInverseSide("Inverse side 4");

    owningSideEntity1.getInverseSides().add(inverseSideEntity1);
    owningSideEntity1.getInverseSides().add(inverseSideEntity2);
    owningSideEntity1.getInverseSides().add(inverseSideEntity4);

    owningSideEntity2.getInverseSides().add(inverseSideEntity1);
    owningSideEntity2.getInverseSides().add(inverseSideEntity3);
    owningSideEntity2.getInverseSides().add(inverseSideEntity4);

    owningSideDao.create(owningSideEntity1);
    owningSideDao.create(owningSideEntity2);
    owningSideDao.clearObjectCache();
    inverseSideDao.clearObjectCache();

    List<ManyToManyEntities.EagerOwningSide> loadedOwningEntities = owningSideDao.queryForAll();

    Assert.assertEquals(2, loadedOwningEntities.size());

    for(ManyToManyEntities.EagerOwningSide loadedOwningSideEntity : loadedOwningEntities) {
      Assert.assertNotNull(loadedOwningSideEntity.getId());
      Assert.assertEquals(3, loadedOwningSideEntity.getInverseSides().size());

      for(ManyToManyEntities.EagerInverseSide loadedInverseSideEntity : loadedOwningSideEntity.getInverseSides()) {
        Assert.assertNotNull(loadedInverseSideEntity.getId());
        Assert.assertTrue(loadedInverseSideEntity.getOwningSides().contains(loadedOwningSideEntity));
      }
    }
  }

  @Test
  public void testLazyManyToManyCreation() throws Exception {
    // the actual test is here, if dao and therefore JoinTable can be created without errors
    Dao<ManyToManyEntities.LazyOwningSide, Long> owningSideDao = createDao(ManyToManyEntities.LazyOwningSide.class, true, true);
    Dao<ManyToManyEntities.LazyInverseSide, Long> inverseSideDao = createDao(ManyToManyEntities.LazyInverseSide.class, true, true);

    ManyToManyEntities.LazyOwningSide owningSideEntity = new ManyToManyEntities.LazyOwningSide("Owning side");
    ManyToManyEntities.LazyInverseSide inverseSideEntity = new ManyToManyEntities.LazyInverseSide("Inverse side");
    owningSideEntity.getInverseSides().add(inverseSideEntity);
    owningSideDao.create(owningSideEntity);

    Assert.assertNotNull(owningSideEntity.getId());
    Assert.assertNotNull(inverseSideEntity.getId());
    Assert.assertTrue(owningSideEntity.getInverseSides().contains(inverseSideEntity));
    Assert.assertTrue(inverseSideEntity.getOwningSides().contains(owningSideEntity));
    Assert.assertEquals(1, owningSideEntity.getInverseSides().size());
    Assert.assertEquals(1, inverseSideEntity.getOwningSides().size());
  }

  @Test
  public void testRetrieveLazyManyToMany() throws Exception {
    // the actual test is here, if dao and therefore JoinTable can be created without errors
    Dao<ManyToManyEntities.LazyOwningSide, Long> owningSideDao = createDao(ManyToManyEntities.LazyOwningSide.class, true, true);
    Dao<ManyToManyEntities.LazyInverseSide, Long> inverseSideDao = createDao(ManyToManyEntities.LazyInverseSide.class, true, true);

    ManyToManyEntities.LazyOwningSide owningSideEntity1 = new ManyToManyEntities.LazyOwningSide("Owning side 1");
    ManyToManyEntities.LazyOwningSide owningSideEntity2 = new ManyToManyEntities.LazyOwningSide("Owning side 2");

    ManyToManyEntities.LazyInverseSide inverseSideEntity1 = new ManyToManyEntities.LazyInverseSide("Inverse side 1");
    ManyToManyEntities.LazyInverseSide inverseSideEntity2 = new ManyToManyEntities.LazyInverseSide("Inverse side 2");
    ManyToManyEntities.LazyInverseSide inverseSideEntity3 = new ManyToManyEntities.LazyInverseSide("Inverse side 3");
    ManyToManyEntities.LazyInverseSide inverseSideEntity4 = new ManyToManyEntities.LazyInverseSide("Inverse side 4");

    owningSideEntity1.getInverseSides().add(inverseSideEntity1);
    owningSideEntity1.getInverseSides().add(inverseSideEntity2);
    owningSideEntity1.getInverseSides().add(inverseSideEntity4);

    owningSideEntity2.getInverseSides().add(inverseSideEntity1);
    owningSideEntity2.getInverseSides().add(inverseSideEntity3);
    owningSideEntity2.getInverseSides().add(inverseSideEntity4);

    owningSideDao.create(owningSideEntity1);
    owningSideDao.create(owningSideEntity2);
    owningSideDao.clearObjectCache();
    inverseSideDao.clearObjectCache();

    List<ManyToManyEntities.LazyOwningSide> loadedOwningEntities = owningSideDao.queryForAll();

    Assert.assertEquals(2, loadedOwningEntities.size());

    for(ManyToManyEntities.LazyOwningSide loadedOwningSideEntity : loadedOwningEntities) {
      Assert.assertNotNull(loadedOwningSideEntity.getId());
      Assert.assertEquals(3, loadedOwningSideEntity.getInverseSides().size());

      for(ManyToManyEntities.LazyInverseSide loadedInverseSideEntity : loadedOwningSideEntity.getInverseSides()) {
        Assert.assertNotNull(loadedInverseSideEntity.getId());
        Assert.assertTrue(loadedInverseSideEntity.getOwningSides().contains(loadedOwningSideEntity));
      }
    }
  }

  @Test
  public void testNonGenericSetManyToManyRelation() throws Exception {
    // the actual test is here, if dao and therefore JoinTable can be created without errors
    Dao<ManyToManyEntities.NonGenericSetOwningSide, Long> owningSideDao = createDao(ManyToManyEntities.NonGenericSetOwningSide.class, true, true);
    Dao<ManyToManyEntities.NonGenericSetInverseSide, Long> inverseSideDao = createDao(ManyToManyEntities.NonGenericSetInverseSide.class, true, true);

    ManyToManyEntities.NonGenericSetOwningSide owningSideEntity1 = new ManyToManyEntities.NonGenericSetOwningSide("Owning 1");
    ManyToManyEntities.NonGenericSetOwningSide owningSideEntity2 = new ManyToManyEntities.NonGenericSetOwningSide("Owning 2");

    ManyToManyEntities.NonGenericSetInverseSide inverseSideEntity1 = new ManyToManyEntities.NonGenericSetInverseSide("Inverse 1");
    ManyToManyEntities.NonGenericSetInverseSide inverseSideEntity2 = new ManyToManyEntities.NonGenericSetInverseSide("Inverse 2");
    ManyToManyEntities.NonGenericSetInverseSide inverseSideEntity3 = new ManyToManyEntities.NonGenericSetInverseSide("Inverse 3");
    ManyToManyEntities.NonGenericSetInverseSide inverseSideEntity4 = new ManyToManyEntities.NonGenericSetInverseSide("Inverse 4");

    owningSideEntity1.getInverseSides().add(inverseSideEntity1);
    owningSideEntity1.getInverseSides().add(inverseSideEntity2);
    owningSideEntity1.getInverseSides().add(inverseSideEntity3);

    owningSideEntity2.getInverseSides().add(inverseSideEntity1);
    owningSideEntity2.getInverseSides().add(inverseSideEntity4);

    owningSideDao.create(owningSideEntity1);
    owningSideDao.create(owningSideEntity2);

    Assert.assertNotNull(owningSideEntity1.getId());
    Assert.assertEquals(3, owningSideEntity1.getInverseSides().size());
    for(ManyToManyEntities.NonGenericSetInverseSide inverseSideEntity : new ArrayList<ManyToManyEntities.NonGenericSetInverseSide>(owningSideEntity1.getInverseSides())) {
      Assert.assertNotNull(inverseSideEntity.getId());
      Assert.assertTrue(inverseSideEntity.getOwningSides().contains(owningSideEntity1));
    }

    Assert.assertNotNull(owningSideEntity2.getId());
    Assert.assertEquals(2, owningSideEntity2.getInverseSides().size());
    for(ManyToManyEntities.NonGenericSetInverseSide inverseSideEntity : new ArrayList<ManyToManyEntities.NonGenericSetInverseSide>(owningSideEntity2.getInverseSides())) {
      Assert.assertNotNull(inverseSideEntity.getId());
      Assert.assertTrue(inverseSideEntity.getOwningSides().contains(owningSideEntity2));
    }
  }
}

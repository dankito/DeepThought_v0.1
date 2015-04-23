package com.j256.ormlite.jpa.crud.query;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.jpa.EntityConfig;
import com.j256.ormlite.jpa.crud.JpaCrudTestBase;
import com.j256.ormlite.jpa.testmodel.Category;
import com.j256.ormlite.jpa.testmodel.Entry;
import com.j256.ormlite.jpa.testmodel.Tag;
import com.j256.ormlite.jpa.testmodel.User;

import org.junit.Assert;
import org.junit.Test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

/**
 * Created by ganymed on 11/03/15.
 */
public class QueryRelationshipTest extends JpaCrudTestBase {

  @Entity
  static class OneToOneOwningSide {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @OneToOne
    protected OneToOneInverseSide inverseSide;
  }

  @Entity
  static class OneToOneInverseSide {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @OneToOne(mappedBy = "inverseSide")
    protected OneToOneOwningSide owningSide;
  }

  @Test
  public void queryEmptyOneToOneOwningSideTable() throws SQLException {
    Dao pojoDao = buildConfigurationAndGetDao(OneToOneOwningSide.class, OneToOneInverseSide.class);
    List<OneToOneOwningSide> storedPojos = pojoDao.queryForAll();

    Assert.assertEquals(0, storedPojos.size());
  }

  @Test
  public void insertObjectsAndThenQueryOneToOneOwningSideTable() throws SQLException {
    EntityConfig[] entities = entityConfigurationReader.readConfigurationAndCreateTablesIfNotExists(OneToOneOwningSide.class, OneToOneInverseSide.class);
    Dao owningSideDao = entities[0].getDao();
    owningSideDao.setObjectCache(false); // really query, so turn off cache
    Dao inverseSideDao = entities[1].getDao();
    inverseSideDao.setObjectCache(false); // really query, so turn off cache

    OneToOneOwningSide owningSide = new OneToOneOwningSide();
    OneToOneInverseSide inverseSide = new OneToOneInverseSide();
    owningSide.inverseSide = inverseSide;
    inverseSide.owningSide = owningSide;

    int result2 = inverseSideDao.create(inverseSide);
    int result1 = owningSideDao.create(owningSide);

    Assert.assertEquals(1, result1);
    Assert.assertEquals(1, result2);

    List<OneToOneOwningSide> storedPojos = owningSideDao.queryForAll();

    Assert.assertEquals(1, storedPojos.size());
    OneToOneOwningSide storedPojo = storedPojos.get(0);
    Assert.assertNotSame(owningSide, storedPojo); // assert that Objects really have been read from Db

    Assert.assertEquals(owningSide.id, storedPojo.id);
    Assert.assertEquals(inverseSide.id, storedPojo.inverseSide.id);
    Assert.assertEquals(inverseSide.owningSide.id, storedPojo.id);
  }


  @Entity
  static class ManyToOneManySide {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @ManyToOne
    protected ManyToOneOneSide oneSide;

    public void setOneSide(ManyToOneOneSide oneSide) {
      this.oneSide = oneSide;
      this.oneSide.manySides.add(this);
    }
  }

  @Entity
  static class ManyToOneOneSide {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @OneToMany(mappedBy = "oneSide")
    protected Collection<ManyToOneManySide> manySides = new HashSet<>();
  }

  @Test
  public void queryEmptyManyToOneManySideTable() throws SQLException {
    Dao pojoDao = buildConfigurationAndGetDao(ManyToOneManySide.class, ManyToOneOneSide.class);
    List<OneToOneOwningSide> storedPojos = pojoDao.queryForAll();

    Assert.assertEquals(0, storedPojos.size());
  }

  @Test
  public void insertObjectsAndThenQueryManyToOneManySideTable() throws SQLException {
    EntityConfig[] entities = entityConfigurationReader.readConfigurationAndCreateTablesIfNotExists(ManyToOneManySide.class, ManyToOneOneSide.class);
    Dao manySideDao = entities[0].getDao();
    manySideDao.setObjectCache(false); // really query, so turn off cache
    Dao oneSideDao = entities[1].getDao();
    oneSideDao.setObjectCache(false); // really query, so turn off cache

    ManyToOneManySide manySide1 = new ManyToOneManySide();
    ManyToOneManySide manySide2 = new ManyToOneManySide();
    ManyToOneManySide manySide3 = new ManyToOneManySide();
    ManyToOneOneSide oneSide1 = new ManyToOneOneSide();
    ManyToOneOneSide oneSide2 = new ManyToOneOneSide();
    int result21 = oneSideDao.create(oneSide1);
    int result22 = oneSideDao.create(oneSide2);

    manySide1.setOneSide(oneSide1);
    manySide2.setOneSide(oneSide1);
    manySide3.setOneSide(oneSide2);

    int result11 = manySideDao.create(manySide1);
    int result12 = manySideDao.create(manySide2);
    int result13 = manySideDao.create(manySide3);

    Assert.assertEquals(1, result11);
    Assert.assertEquals(1, result12);
    Assert.assertEquals(1, result13);
    Assert.assertEquals(1, result21);
    Assert.assertEquals(1, result22);

    List<ManyToOneManySide> storedPojos = manySideDao.queryForAll();

    Assert.assertEquals(3, storedPojos.size());

    ManyToOneManySide storedPojo1 = storedPojos.get(0);
    Assert.assertNotSame(manySide1, storedPojo1); // assert that Objects really have been read from Db
    Assert.assertEquals(manySide1.id, storedPojo1.id);
    Assert.assertEquals(oneSide1.id, storedPojo1.oneSide.id);

    ManyToOneManySide storedPojo2 = storedPojos.get(1);
    Assert.assertNotSame(manySide2, storedPojo2); // assert that Objects really have been read from Db
    Assert.assertEquals(manySide2.id, storedPojo2.id);
    Assert.assertEquals(oneSide1.id, storedPojo2.oneSide.id);

    ManyToOneManySide storedPojo3 = storedPojos.get(2);
    Assert.assertNotSame(manySide3, storedPojo3); // assert that Objects really have been read from Db
    Assert.assertEquals(manySide3.id, storedPojo3.id);
    Assert.assertEquals(oneSide2.id, storedPojo3.oneSide.id);
  }

  @Test
  public void insertObjectsAndThenQueryManyToOneManySideTable_InversePersistenceOrder() throws SQLException {
    EntityConfig[] entities = entityConfigurationReader.readConfigurationAndCreateTablesIfNotExists(ManyToOneManySide.class, ManyToOneOneSide.class);
    Dao manySideDao = entities[0].getDao();
    manySideDao.setObjectCache(false); // really query, so turn off cache
    Dao oneSideDao = entities[1].getDao();
    oneSideDao.setObjectCache(false); // really query, so turn off cache

    ManyToOneManySide manySide1 = new ManyToOneManySide();
    ManyToOneManySide manySide2 = new ManyToOneManySide();
    ManyToOneManySide manySide3 = new ManyToOneManySide();
    ManyToOneOneSide oneSide1 = new ManyToOneOneSide();
    ManyToOneOneSide oneSide2 = new ManyToOneOneSide();
    manySide1.setOneSide(oneSide1);
    manySide2.setOneSide(oneSide1);
    manySide3.setOneSide(oneSide2);

    int result21 = oneSideDao.create(oneSide1);
    int result22 = oneSideDao.create(oneSide2);
    int result11 = manySideDao.create(manySide1);
    int result12 = manySideDao.create(manySide2);
    int result13 = manySideDao.create(manySide3);

    Assert.assertEquals(1, result11);
    Assert.assertEquals(1, result12);
    Assert.assertEquals(1, result13);
    Assert.assertEquals(1, result21);
    Assert.assertEquals(1, result22);

    List<ManyToOneManySide> storedPojos = manySideDao.queryForAll();

    Assert.assertEquals(3, storedPojos.size());

    ManyToOneManySide storedPojo1 = storedPojos.get(0);
    Assert.assertNotSame(manySide1, storedPojo1); // assert that Objects really have been read from Db
    Assert.assertEquals(manySide1.id, storedPojo1.id);
    Assert.assertEquals(oneSide1.id, storedPojo1.oneSide.id);

    ManyToOneManySide storedPojo2 = storedPojos.get(1);
    Assert.assertNotSame(manySide2, storedPojo2); // assert that Objects really have been read from Db
    Assert.assertEquals(manySide2.id, storedPojo2.id);
    Assert.assertEquals(oneSide1.id, storedPojo2.oneSide.id);

    ManyToOneManySide storedPojo3 = storedPojos.get(2);
    Assert.assertNotSame(manySide3, storedPojo3); // assert that Objects really have been read from Db
    Assert.assertEquals(manySide3.id, storedPojo3.id);
    Assert.assertEquals(oneSide2.id, storedPojo3.oneSide.id);
  }


  @Entity
  static class OneToManyOneSide {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @OneToMany(mappedBy = "one")
    protected Collection<OneToManyManySide> manys = new HashSet<>();

    public void addMany(OneToManyManySide many) {
      manys.add(many);
      many.one = this;
    }
  }

  @Entity
  static class OneToManyManySide {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @ManyToOne
    protected OneToManyOneSide one;
  }

  @Test
  public void queryEmptyOneToManyOneTable() throws SQLException {
    Dao pojoDao = buildConfigurationAndGetDao(OneToManyOneSide.class, OneToManyManySide.class);
    List<OneToOneOwningSide> storedPojos = pojoDao.queryForAll();

    Assert.assertEquals(0, storedPojos.size());
  }

  @Test
  public void insertObjectsAndThenQueryOneToManyOneSideTable() throws SQLException {
    EntityConfig[] entities = entityConfigurationReader.readConfigurationAndCreateTablesIfNotExists(OneToManyOneSide.class, OneToManyManySide.class);
    Dao oneSideDao = entities[0].getDao();
    oneSideDao.setObjectCache(false); // really query, so turn off cache
    Dao manySideDao = entities[1].getDao();
    manySideDao.setObjectCache(false); // really query, so turn off cache

    OneToManyManySide manySide1 = new OneToManyManySide();
    OneToManyManySide manySide2 = new OneToManyManySide();
    OneToManyManySide manySide3 = new OneToManyManySide();
    OneToManyManySide manySide4 = new OneToManyManySide();

    int result21 = manySideDao.create(manySide1);
    int result22 = manySideDao.create(manySide2);
    int result23 = manySideDao.create(manySide3);
    int result24 = manySideDao.create(manySide4);

    OneToManyOneSide oneSide1 = new OneToManyOneSide();
    OneToManyOneSide oneSide2 = new OneToManyOneSide();

    oneSide1.addMany(manySide1);
    oneSide1.addMany(manySide2);
    oneSide2.addMany(manySide3);
    oneSide2.addMany(manySide4);

    int result11 = oneSideDao.create(oneSide1);
    int result12 = oneSideDao.create(oneSide2);

    Assert.assertEquals(1, result11);
    Assert.assertEquals(1, result12);
    Assert.assertEquals(1, result21);
    Assert.assertEquals(1, result22);
    Assert.assertEquals(1, result23);
    Assert.assertEquals(1, result24);

    List<OneToManyOneSide> storedPojos = oneSideDao.queryForAll();

    Assert.assertEquals(2, storedPojos.size());

    OneToManyOneSide storedPojo1 = storedPojos.get(0);
    Assert.assertNotSame(oneSide1, storedPojo1); // assert that Objects really have been read from Db
    Assert.assertEquals(oneSide1.id, storedPojo1.id);
    Assert.assertEquals(2, storedPojo1.manys.size());
    Assert.assertEquals(manySide1.one.id, storedPojo1.id);
    Assert.assertEquals(manySide2.one.id, storedPojo1.id);

    OneToManyOneSide storedPojo2 = storedPojos.get(1);
    Assert.assertNotSame(oneSide2, storedPojo2); // assert that Objects really have been read from Db
    Assert.assertEquals(oneSide2.id, storedPojo2.id);
    Assert.assertEquals(2, storedPojo2.manys.size());
    Assert.assertEquals(manySide3.one.id, storedPojo2.id);
    Assert.assertEquals(manySide4.one.id, storedPojo2.id);
  }


  @Entity
  static class ManyToManyOwningSide {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @ManyToMany
    protected Collection<ManyToManyInverseSide> inverseSides = new HashSet<>();

    public void addInverse(ManyToManyInverseSide inverseSide) {
      inverseSides.add(inverseSide);
      inverseSide.owningSides.add(this);
    }
  }

  @Entity
  static class ManyToManyInverseSide {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @ManyToMany(mappedBy = "inverseSides")
    protected Collection<ManyToManyOwningSide> owningSides = new HashSet<>();
  }

  @Test
  public void queryEmptyManyToManyOwningSideTable() throws SQLException {
    Dao pojoDao = buildConfigurationAndGetDao(ManyToManyOwningSide.class, ManyToManyInverseSide.class);
    List<OneToOneOwningSide> storedPojos = pojoDao.queryForAll();

    Assert.assertEquals(0, storedPojos.size());
  }

  @Test
  public void insertObjectsAndThenQueryManyToManyOwningSideTable() throws SQLException {
    EntityConfig[] entities = entityConfigurationReader.readConfigurationAndCreateTablesIfNotExists(ManyToManyOwningSide.class, ManyToManyInverseSide.class);
    Dao owningSideDao = entities[0].getDao();
    owningSideDao.setObjectCache(false); // really query, so turn off cache
    Dao inverseSideDao = entities[1].getDao();
    inverseSideDao.setObjectCache(false); // really query, so turn off cache

    ManyToManyOwningSide owningSide1 = new ManyToManyOwningSide();
    ManyToManyOwningSide owningSide2 = new ManyToManyOwningSide();

    int result11 = owningSideDao.create(owningSide1);
    int result12 = owningSideDao.create(owningSide2);

    ManyToManyInverseSide inverseSide1 = new ManyToManyInverseSide();
    ManyToManyInverseSide inverseSide2 = new ManyToManyInverseSide();
    ManyToManyInverseSide inverseSide3 = new ManyToManyInverseSide();
    ManyToManyInverseSide inverseSide4 = new ManyToManyInverseSide();

    owningSide1.addInverse(inverseSide1);
    owningSide1.addInverse(inverseSide2);
    owningSide2.addInverse(inverseSide3);
    owningSide2.addInverse(inverseSide4);

    int result21 = inverseSideDao.create(inverseSide1);
    int result22 = inverseSideDao.create(inverseSide2);
    int result23 = inverseSideDao.create(inverseSide3);
    int result24 = inverseSideDao.create(inverseSide4);
//    owningSideDao.update(owningSide1);
//    owningSideDao.update(owningSide2);

    Assert.assertEquals(1, result11);
    Assert.assertEquals(1, result12);
    Assert.assertEquals(1, result21);
    Assert.assertEquals(1, result22);
    Assert.assertEquals(1, result23);
    Assert.assertEquals(1, result24);

    List<ManyToManyOwningSide> storedOwningPojos = owningSideDao.queryForAll();
    List<ManyToManyInverseSide> storedInversePojos = inverseSideDao.queryForAll();

    Assert.assertEquals(2, storedOwningPojos.size());

    ManyToManyOwningSide storedPojo1 = storedOwningPojos.get(0);
    Assert.assertNotSame(owningSide1, storedPojo1); // assert that Objects really have been read from Db
    Assert.assertEquals(owningSide1.id, storedPojo1.id);
    Assert.assertEquals(2, storedPojo1.inverseSides.size());
    List<ManyToManyInverseSide> storedInverseSides1 = new ArrayList<>(storedPojo1.inverseSides);
    Assert.assertEquals(inverseSide1.id, storedInverseSides1.get(0).id);
    Assert.assertEquals(inverseSide2.id, storedInverseSides1.get(1).id);

    ManyToManyOwningSide storedPojo2 = storedOwningPojos.get(1);
    Assert.assertNotSame(owningSide2, storedPojo2); // assert that Objects really have been read from Db
    Assert.assertEquals(owningSide2.id, storedPojo2.id);
    Assert.assertEquals(2, storedPojo2.inverseSides.size());
    List<ManyToManyInverseSide> storedInverseSides2 = new ArrayList<>(storedPojo2.inverseSides);
    Assert.assertEquals(inverseSide3.id, storedInverseSides2.get(0).id);
    Assert.assertEquals(inverseSide4.id, storedInverseSides2.get(1).id);
  }


  @Test
  public void addEntriesWithTagsToDeepThought_EntriesGetQueriedCorrectly() throws Exception {
    EntityConfig[] entities = entityConfigurationReader.readConfigurationAndCreateTablesIfNotExists(Entry.class, Tag.class, User.class, Category.class);
    Dao entryDao = entities[0].getDao();
    entryDao.setObjectCache(false); // really query, so turn off cache
    Dao tagDao = entities[1].getDao();
    tagDao.setObjectCache(false); // really query, so turn off cache

    Entry entry1 = new Entry("Test Entry 1");
    Entry entry2 = new Entry("Test Entry 2");

    entryDao.create(entry1);
    entryDao.create(entry2);

    Tag tag1 = new Tag("one");
    Tag tag2 = new Tag("two");
    Tag tag3 = new Tag("three");

    tagDao.create(tag1);
    tagDao.create(tag2);
    tagDao.create(tag3);

    entry1.addTag(tag1);
    entry1.addTag(tag2);
    entry2.addTag(tag2);
    entry2.addTag(tag3);

    List<Entry> storedEntries = entryDao.queryForAll();
    Assert.assertEquals(2, storedEntries.size());

    List<Tag> storedTags = tagDao.queryForAll();
    Assert.assertEquals(3, storedTags.size());

    List<String> tagNames = new ArrayList<>();
    for(Tag tag : storedTags) {
      Assert.assertFalse(tagNames.contains(tag.getName()));
      tagNames.add(tag.getName());
    }
    Assert.assertEquals(3, tagNames.size());

    List<String> entry1TagNames = new ArrayList<>(Arrays.asList(new String[]{"one", "two"}));
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


}

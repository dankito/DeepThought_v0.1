package net.deepthought.data.model;

import net.deepthought.Application;
import net.deepthought.data.persistence.db.TableConfig;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by ganymed on 10/11/14.
 */
public abstract class ReferenceSubDivisionTestBase extends ReferenceBaseTestBase {

  @Override
  protected ReferenceBase createReferenceBaseInstanceAndAddToDeepThought() {
    ReferenceBaseSubClassClass = ReferenceSubDivision.class;

    Reference reference = new Reference("War on People");
    ReferenceSubDivision subDivision = new ReferenceSubDivision("Chapter 1");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addReference(reference);
    deepThought.addReferenceSubDivision(subDivision);

    reference.addSubDivision(subDivision);

    return subDivision;
  }


  @Test
  public void setOnEntry_RelationsGetUpdated() throws Exception {
    Reference reference = new Reference("War on People");
    ReferenceSubDivision subDivision = new ReferenceSubDivision("Chapter 1");
    Entry entry = new Entry("test");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addReference(reference);
    deepThought.addReferenceSubDivision(subDivision);
    deepThought.addEntry(entry);

    reference.addSubDivision(subDivision);

    entry.setReferenceSubDivision(subDivision);

    Assert.assertEquals(subDivision, entry.getReferenceSubDivision());
    Assert.assertTrue(subDivision.getEntries().contains(entry));

    Assert.assertEquals(reference, entry.getReference());
    Assert.assertTrue(reference.getEntries().contains(entry));
  }

  @Test
  public void setOnEntryThanDeleteSubDivision_RelationsGetUpdated() throws Exception {
    Reference reference = new Reference("War on People");
    ReferenceSubDivision subDivision = new ReferenceSubDivision("Chapter 1");
    Entry entry = new Entry("test");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addReference(reference);
    deepThought.addReferenceSubDivision(subDivision);
    deepThought.addEntry(entry);

    reference.addSubDivision(subDivision);

    entry.setReferenceSubDivision(subDivision);

    reference.removeSubDivision(subDivision);

    Assert.assertNull(entry.getReferenceSubDivision());
    Assert.assertFalse(subDivision.getEntries().contains(entry));

    Assert.assertNotNull(entry.getReference());
    Assert.assertTrue(reference.getEntries().contains(entry));
  }

  @Test
  public void addReferenceSubDivision_EntitiesGetAddedToRelatedCollections() throws Exception {
    Reference reference = new Reference("War on People");
    ReferenceSubDivision subDivision = new ReferenceSubDivision("Chapter 1");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addReference(reference);
    deepThought.addReferenceSubDivision(subDivision);

    reference.addSubDivision(subDivision);

    Assert.assertTrue(reference.getSubDivisions().contains(subDivision));
    Assert.assertEquals(reference, subDivision.getReference());
    Assert.assertEquals(deepThought, subDivision.getDeepThought());

    Assert.assertEquals(0, subDivision.getSubDivisionOrder());
  }

  @Test
  public void removeReferenceSubDivision_RelationGetsDeleted() throws Exception {
    Reference reference = new Reference("War on People");
    ReferenceSubDivision subDivision = new ReferenceSubDivision("Chapter 1");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addReference(reference);
    deepThought.addReferenceSubDivision(subDivision);

    reference.addSubDivision(subDivision);

    reference.removeSubDivision(subDivision);

    Assert.assertNull(getValueFromTable(TableConfig.ReferenceSubDivisionTableName, TableConfig.ReferenceSubDivisionReferenceJoinColumnName, subDivision.getId()));
  }

  @Test
  public void removeReferenceSubDivision_EntitiesGetRemovedFromRelatedCollections() throws Exception {
    Reference reference = new Reference("War on People");
    ReferenceSubDivision subDivision = new ReferenceSubDivision("Chapter 1");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addReference(reference);
    deepThought.addReferenceSubDivision(subDivision);

    reference.addSubDivision(subDivision);

    reference.removeSubDivision(subDivision);

    Assert.assertFalse(reference.getSubDivisions().contains(subDivision));
    Assert.assertNull(subDivision.getDeepThought());
    Assert.assertNull(subDivision.getReference());
  }

  @Test
  public void removeReferenceSubDivision_SubDivisionGetsDeletedFromDatabase() throws Exception {
    Reference reference = new Reference("War on People");
    ReferenceSubDivision subDivision = new ReferenceSubDivision("Chapter 1");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addReference(reference);
    deepThought.addReferenceSubDivision(subDivision);

    reference.addSubDivision(subDivision);

    reference.removeSubDivision(subDivision);

    Assert.assertFalse(reference.isDeleted());
    Assert.assertNotNull(reference.getId());

    Assert.assertTrue(subDivision.isDeleted());
    Assert.assertNotNull(subDivision.getId());
  }


  @Test
  public void addSubDivision_SubDivisionGetsPersisted() throws Exception {
    Reference reference = new Reference("War on People");
    ReferenceSubDivision parent = new ReferenceSubDivision("Chapter 1");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addReference(reference);
    deepThought.addReferenceSubDivision(parent);

    reference.addSubDivision(parent);

    ReferenceSubDivision subDivision = new ReferenceSubDivision("Chapter 1.1");
    deepThought.addReferenceSubDivision(subDivision);
    parent.addSubDivision(subDivision);

    Assert.assertTrue(doIdsEqual(parent.getId(), getValueFromTable(TableConfig.ReferenceSubDivisionTableName, TableConfig.ReferenceSubDivisionParentSubDivisionJoinColumnName, subDivision.getId())));
  }

  @Test
  public void addSubDivision_RelationsGetSet() throws Exception {
    Reference reference = new Reference("War on People");
    ReferenceSubDivision parent = new ReferenceSubDivision("Chapter 1");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addReference(reference);
    deepThought.addReferenceSubDivision(parent);

    reference.addSubDivision(parent);

    ReferenceSubDivision subDivision = new ReferenceSubDivision("Chapter 1.1");
    deepThought.addReferenceSubDivision(subDivision);
    parent.addSubDivision(subDivision);

    Assert.assertEquals(parent, subDivision.getParentSubDivision());
    Assert.assertTrue(parent.getSubDivisions().contains(subDivision));

    Assert.assertEquals(0, subDivision.getSubDivisionOrder());
  }

  @Test
  public void removeSubDivision_SubDivisionGetsDeletedFromDB() throws Exception {
    Reference reference = new Reference("War on People");
    ReferenceSubDivision parent = new ReferenceSubDivision("Chapter 1");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addReference(reference);
    deepThought.addReferenceSubDivision(parent);

    reference.addSubDivision(parent);

    ReferenceSubDivision subDivision = new ReferenceSubDivision("Chapter 1.1");
    deepThought.addReferenceSubDivision(subDivision);
    parent.addSubDivision(subDivision);

    parent.removeSubDivision(subDivision);
    deepThought.removeReferenceSubDivision(subDivision);

    Assert.assertFalse(parent.isDeleted());
    Assert.assertTrue(subDivision.isDeleted());
    Assert.assertNull(getValueFromTable(TableConfig.ReferenceSubDivisionTableName, TableConfig.ReferenceSubDivisionParentSubDivisionJoinColumnName, subDivision.getId()));
  }

  @Test
  public void removeSubDivision_RelationsGetRemoved() throws Exception {
    Reference reference = new Reference("War on People");
    ReferenceSubDivision parent = new ReferenceSubDivision("Chapter 1");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addReference(reference);
    deepThought.addReferenceSubDivision(parent);

    reference.addSubDivision(parent);

    ReferenceSubDivision subDivision = new ReferenceSubDivision("Chapter 1.1");
    deepThought.addReferenceSubDivision(subDivision);
    parent.addSubDivision(subDivision);

    parent.removeSubDivision(subDivision);

    Assert.assertNull(subDivision.getParentSubDivision());
    Assert.assertFalse(parent.getSubDivisions().contains(subDivision));
  }

  @Test
  public void setSubDivisionOnEntryThanDeleteSubDivision_RelationsGetUpdated() throws Exception {
    Reference reference = new Reference("War on People");
    ReferenceSubDivision parent = new ReferenceSubDivision("Chapter 1");
    Entry entry = new Entry("test");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addReference(reference);
    deepThought.addReferenceSubDivision(parent);
    deepThought.addEntry(entry);

    reference.addSubDivision(parent);

    ReferenceSubDivision subDivision = new ReferenceSubDivision("Chapter 1.1");
    deepThought.addReferenceSubDivision(subDivision);
    parent.addSubDivision(subDivision);

    entry.setReferenceSubDivision(subDivision);

    parent.removeSubDivision(subDivision);

    Assert.assertNull(entry.getReferenceSubDivision());
    Assert.assertFalse(subDivision.getEntries().contains(entry));

    Assert.assertNotNull(entry.getReference());
    Assert.assertTrue(reference.getEntries().contains(entry));
  }

  @Test
  public void add2ReferenceSubDivisionsThanRemoveFirstOne_SubDivisionOrderGetsAdjusted() throws Exception {
    Reference reference = new Reference("War on People");
    ReferenceSubDivision parent = new ReferenceSubDivision("Chapter 1");

    ReferenceSubDivision subDivision1 = new ReferenceSubDivision("Chapter 1.1");
    ReferenceSubDivision subDivision2 = new ReferenceSubDivision("Chapter 1.2");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addReference(reference);
    deepThought.addReferenceSubDivision(parent);

    reference.addSubDivision(parent);

    deepThought.addReferenceSubDivision(subDivision1);
    deepThought.addReferenceSubDivision(subDivision2);
    parent.addSubDivision(subDivision1);
    parent.addSubDivision(subDivision2);

    Assert.assertEquals(1, subDivision2.getSubDivisionOrder());

    parent.removeSubDivision(subDivision1);

    Assert.assertEquals(0, subDivision2.getSubDivisionOrder());
  }

  @Test
  public void add3ReferenceSubDivisionsThanRemoveSecondOne_EnsureSubDivisionOrdersGetSetCorrectly() throws Exception {
    Reference reference = new Reference("War on People");
    ReferenceSubDivision parent = new ReferenceSubDivision("Chapter 1");

    ReferenceSubDivision subDivision1 = new ReferenceSubDivision("Chapter 1.1");
    ReferenceSubDivision subDivision2 = new ReferenceSubDivision("Chapter 1.2");
    ReferenceSubDivision subDivision3 = new ReferenceSubDivision("Chapter 1.3");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addReference(reference);
    deepThought.addReferenceSubDivision(subDivision1);
    deepThought.addReferenceSubDivision(subDivision2);
    deepThought.addReferenceSubDivision(subDivision3);

    reference.addSubDivision(parent);

    parent.addSubDivision(subDivision1);
    parent.addSubDivision(subDivision2);
    parent.addSubDivision(subDivision3);

    Assert.assertEquals(2, subDivision3.getSubDivisionOrder());

    parent.removeSubDivision(subDivision2);

    Assert.assertEquals(0, subDivision1.getSubDivisionOrder());
    Assert.assertEquals(1, subDivision3.getSubDivisionOrder());
  }

}

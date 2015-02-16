package net.deepthought.data.model;

import net.deepthought.Application;
import net.deepthought.data.model.enums.ReferenceIndicationUnit;
import net.deepthought.data.model.enums.ReferenceSubDivisionCategory;
import net.deepthought.data.persistence.db.TableConfig;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ganymed on 10/11/14.
 */
public abstract class ReferenceSubDivisionTestBase extends ReferenceBaseTestBase {

  @Override
  protected ReferenceBase createReferenceBaseInstanceAndAddToDeepThought() {
    Reference reference = new Reference("War on People");
    ReferenceSubDivision subDivision = new ReferenceSubDivision("Chapter 1");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addReference(reference);

    reference.addSubDivision(subDivision);

    return subDivision;
  }


  @Test
  public void seReferenceSubDivisionCategory_UpdatedValueGetsPersistedInDb() throws Exception {
    Reference reference = new Reference("War on People");
    ReferenceSubDivision subDivision = new ReferenceSubDivision("Chapter 1");

    DeepThought deepThought = Application.getDeepThought();
    List<ReferenceSubDivisionCategory> categories = new ArrayList<>(deepThought.getReferenceSubDivisionCategories());
    deepThought.addReference(reference);
    reference.addSubDivision(subDivision);

    ReferenceSubDivisionCategory category = categories.get(0);
    subDivision.setCategory(category);

    Assert.assertTrue(doIdsEqual(category.getId(), getValueFromTable(TableConfig.ReferenceSubDivisionTableName, TableConfig.ReferenceSubDivisionCategoryJoinColumnName, subDivision.getId())));
  }

  @Test
  public void updateReferenceSubDivisionCategory_UpdatedValueGetsPersistedInDb() throws Exception {
    Reference reference = new Reference("War on People");
    ReferenceSubDivision subDivision = new ReferenceSubDivision("Chapter 1");

    DeepThought deepThought = Application.getDeepThought();
    List<ReferenceSubDivisionCategory> categories = new ArrayList<>(deepThought.getReferenceSubDivisionCategories());
    subDivision.setCategory(categories.get(0));
    deepThought.addReference(reference);
    reference.addSubDivision(subDivision);

    ReferenceSubDivisionCategory newValue = categories.get(1);
    subDivision.setCategory(newValue);

    Assert.assertTrue(doIdsEqual(newValue.getId(), getValueFromTable(TableConfig.ReferenceSubDivisionTableName, TableConfig.ReferenceSubDivisionCategoryJoinColumnName, subDivision.getId())));
  }


  @Test
  public void setOnEntry_RelationsGetUpdated() throws Exception {
    Reference reference = new Reference("War on People");
    ReferenceSubDivision subDivision = new ReferenceSubDivision("Chapter 1");
    Entry entry = new Entry("test");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addReference(reference);
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

    reference.addSubDivision(subDivision);

    Assert.assertTrue(reference.getSubDivisions().contains(subDivision));
    Assert.assertEquals(reference, subDivision.getReference());

    Assert.assertEquals(0, subDivision.getSubDivisionOrder());
  }

  @Test
  public void removeReferenceSubDivision_RelationGetsDeleted() throws Exception {
    Reference reference = new Reference("War on People");
    ReferenceSubDivision subDivision = new ReferenceSubDivision("Chapter 1");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addReference(reference);

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

    reference.addSubDivision(subDivision);

    reference.removeSubDivision(subDivision);

    Assert.assertFalse(reference.getSubDivisions().contains(subDivision));
    Assert.assertNull(subDivision.getReference());
  }

  @Test
  public void removeReferenceSubDivision_SubDivisionGetsDeletedFromDatabase() throws Exception {
    Reference reference = new Reference("War on People");
    ReferenceSubDivision subDivision = new ReferenceSubDivision("Chapter 1");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addReference(reference);

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

    reference.addSubDivision(parent);

    ReferenceSubDivision subDivision = new ReferenceSubDivision("Chapter 1.1");
    parent.addSubDivision(subDivision);

    Assert.assertTrue(doIdsEqual(parent.getId(), getValueFromTable(TableConfig.ReferenceSubDivisionTableName, TableConfig.ReferenceSubDivisionParentSubDivisionJoinColumnName, subDivision.getId())));
  }

  @Test
  public void addSubDivision_RelationsGetSet() throws Exception {
    Reference reference = new Reference("War on People");
    ReferenceSubDivision parent = new ReferenceSubDivision("Chapter 1");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addReference(reference);

    reference.addSubDivision(parent);

    ReferenceSubDivision subDivision = new ReferenceSubDivision("Chapter 1.1");
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

    reference.addSubDivision(parent);

    ReferenceSubDivision subDivision = new ReferenceSubDivision("Chapter 1.1");
    parent.addSubDivision(subDivision);

    parent.removeSubDivision(subDivision);

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

    reference.addSubDivision(parent);

    ReferenceSubDivision subDivision = new ReferenceSubDivision("Chapter 1.1");
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
    deepThought.addEntry(entry);

    reference.addSubDivision(parent);

    ReferenceSubDivision subDivision = new ReferenceSubDivision("Chapter 1.1");
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

    reference.addSubDivision(parent);

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

    reference.addSubDivision(parent);

    parent.addSubDivision(subDivision1);
    parent.addSubDivision(subDivision2);
    parent.addSubDivision(subDivision3);

    Assert.assertEquals(2, subDivision3.getSubDivisionOrder());

    parent.removeSubDivision(subDivision2);

    Assert.assertEquals(0, subDivision1.getSubDivisionOrder());
    Assert.assertEquals(1, subDivision3.getSubDivisionOrder());
  }


  @Test
  public void updateLength_UpdatedValueGetsPersistedInDb() throws Exception {
    Reference reference = new Reference("War on People");
    ReferenceSubDivision subDivision = new ReferenceSubDivision("Chapter 1");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addReference(reference);
    reference.addSubDivision(subDivision);

    String newValue = "New value";
    subDivision.setLength(newValue);

    Assert.assertEquals(newValue, getValueFromTable(TableConfig.ReferenceSubDivisionTableName, TableConfig.ReferenceSubDivisionLengthColumnName, subDivision.getId()));
  }

  @Test
  public void setLengthUnit_ValueGetsPersistedInDb() throws Exception {
    Reference reference = new Reference("War on People");
    ReferenceSubDivision subDivision = new ReferenceSubDivision("Chapter 1");

    DeepThought deepThought = Application.getDeepThought();
    List<ReferenceIndicationUnit> units = new ArrayList<>(deepThought.getReferenceIndicationUnits());
    deepThought.addReference(reference);
    reference.addSubDivision(subDivision);

    ReferenceIndicationUnit unit = units.get(0);
    subDivision.setLengthUnit(unit);

    Assert.assertTrue(doIdsEqual(unit.getId(), getValueFromTable(TableConfig.ReferenceSubDivisionTableName, TableConfig.ReferenceSubDivisionLengthUnitJoinColumnName, subDivision.getId())));
  }

  @Test
  public void updateLengthUnit_UpdatedValueGetsPersistedInDb() throws Exception {
    Reference reference = new Reference("War on People");
    ReferenceSubDivision subDivision = new ReferenceSubDivision("Chapter 1");

    DeepThought deepThought = Application.getDeepThought();
    List<ReferenceIndicationUnit> units = new ArrayList<>(deepThought.getReferenceIndicationUnits());
    subDivision.setLengthUnit(units.get(0));
    deepThought.addReference(reference);
    reference.addSubDivision(subDivision);

    ReferenceIndicationUnit newValue = units.get(1);
    subDivision.setLengthUnit(newValue);

    Assert.assertTrue(doIdsEqual(newValue.getId(), getValueFromTable(TableConfig.ReferenceSubDivisionTableName, TableConfig.ReferenceSubDivisionLengthUnitJoinColumnName, subDivision.getId())));
  }

}

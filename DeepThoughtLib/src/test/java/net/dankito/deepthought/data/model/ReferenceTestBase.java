package net.dankito.deepthought.data.model;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.data.persistence.db.TableConfig;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by ganymed on 10/11/14.
 */
public abstract class ReferenceTestBase extends ReferenceBaseTestBase {

  @Override
  protected ReferenceBase createReferenceBaseInstanceAndAddToDeepThought() {
    ReferenceBaseSubClassClass = Reference.class;

    Reference reference = new Reference("test");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addReference(reference);

    return reference;
  }


  @Test
  public void setSeries_RelationGetsPersisted() throws Exception {
    SeriesTitle series = new SeriesTitle("agora42");
    Reference reference = new Reference("Ausgabe 2/15");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addSeriesTitle(series);
    deepThought.addReference(reference);

    reference.setSeries(series);

    Assert.assertTrue(doIdsEqual(series.getId(), getValueFromTable(TableConfig.ReferenceTableName, TableConfig.ReferenceSeriesTitleJoinColumnName, reference.getId())));
  }

  @Test
  public void setSeries_EntitiesGetAddedToRelatedCollections() throws Exception {
    SeriesTitle series = new SeriesTitle("agora42");
    Reference reference = new Reference("Ausgabe 2/15");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addSeriesTitle(series);
    deepThought.addReference(reference);

    reference.setSeries(series);

    Assert.assertEquals(series, reference.getSeries());
    Assert.assertTrue(series.getSerialParts().contains(reference));
  }

  @Test
  public void unsetSeries_RelationGetsDeleted() throws Exception {
    SeriesTitle series = new SeriesTitle("agora42");
    Reference reference = new Reference("Ausgabe 2/15");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addSeriesTitle(series);
    deepThought.addReference(reference);

    reference.setSeries(series);

    reference.setSeries(null);

    Assert.assertNull(getValueFromTable(TableConfig.ReferenceTableName, TableConfig.ReferenceSeriesTitleJoinColumnName, reference.getId()));
  }

  @Test
  public void unsetSeries_EntitiesGetRemovedFromRelatedCollections() throws Exception {
    SeriesTitle series = new SeriesTitle("agora42");
    Reference reference = new Reference("Ausgabe 2/15");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addSeriesTitle(series);
    deepThought.addReference(reference);

    reference.setSeries(series);

    reference.setSeries(null);

    Assert.assertNull(reference.getSeries());
    Assert.assertFalse(series.getSerialParts().contains(reference));
  }

  @Test
  public void setSeriesAfterAddingToEntries_AllEntriesSeriesGetUpdated() throws Exception {
    SeriesTitle series = new SeriesTitle("agora42");
    Reference reference = new Reference("Ausgabe 2/15");

    Entry entry1 = new Entry("Ist Freiheit in Ordnung?");
    Entry entry2 = new Entry("Warum nicht?");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addSeriesTitle(series);
    deepThought.addReference(reference);
    deepThought.addEntry(entry1);
    deepThought.addEntry(entry2);

    entry1.setReference(reference);
    entry2.setReference(reference);

    Assert.assertNull(entry1.getSeries());
    Assert.assertNull(entry2.getSeries());

    reference.setSeries(series);

    Assert.assertEquals(series, entry1.getSeries());
    Assert.assertEquals(series, entry2.getSeries());
  }

  @Test
  public void setSeriesBeforeAddingToEntries_AllEntriesSeriesGetUpdated() throws Exception {
    SeriesTitle series = new SeriesTitle("agora42");
    Reference reference = new Reference("Ausgabe 2/15");

    Entry entry1 = new Entry("Ist Freiheit in Ordnung?");
    Entry entry2 = new Entry("Warum nicht?");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addSeriesTitle(series);
    deepThought.addReference(reference);
    deepThought.addEntry(entry1);
    deepThought.addEntry(entry2);

    reference.setSeries(series);

    Assert.assertNull(entry1.getSeries());
    Assert.assertNull(entry2.getSeries());

    entry1.setReference(reference);
    entry2.setReference(reference);

    Assert.assertEquals(series, entry1.getSeries());
    Assert.assertEquals(series, entry2.getSeries());
  }

  @Test
  public void unsetSeries_AllEntriesSeriesGetUpdated() throws Exception {
    SeriesTitle series = new SeriesTitle("agora42");
    Reference reference = new Reference("Ausgabe 2/15");

    Entry entry1 = new Entry("Ist Freiheit in Ordnung?");
    Entry entry2 = new Entry("Warum nicht?");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addSeriesTitle(series);
    deepThought.addReference(reference);
    deepThought.addEntry(entry1);
    deepThought.addEntry(entry2);

    entry1.setReference(reference);
    entry2.setReference(reference);

    reference.setSeries(series);

    reference.setSeries(null);

    Assert.assertNull(entry1.getSeries());
    Assert.assertNull(entry2.getSeries());
  }


  @Test
  public void addReferenceSubDivision_RelationGetsPersisted() throws Exception {
    Reference reference = new Reference("War on People");
    ReferenceSubDivision subDivision = new ReferenceSubDivision("Chapter 1");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addReference(reference);
    deepThought.addReferenceSubDivision(subDivision);

    reference.addSubDivision(subDivision);

    Assert.assertTrue(doIdsEqual(reference.getId(), getValueFromTable(TableConfig.ReferenceSubDivisionTableName, TableConfig.ReferenceSubDivisionReferenceJoinColumnName, subDivision.getId())));
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
  public void add2ReferenceSubDivisionsThanRemoveFirstOne_SubDivisionOrderGetsAdjusted() throws Exception {
    Reference reference = new Reference("War on People");
    ReferenceSubDivision subDivision1 = new ReferenceSubDivision("Chapter 1");
    ReferenceSubDivision subDivision2 = new ReferenceSubDivision("Chapter 2");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addReference(reference);

    reference.addSubDivision(subDivision1);
    reference.addSubDivision(subDivision2);

    Assert.assertEquals(1, subDivision2.getSubDivisionOrder());

    reference.removeSubDivision(subDivision1);

    Assert.assertEquals(0, subDivision2.getSubDivisionOrder());
  }

  @Test
  public void add3ReferenceSubDivisionsThanRemoveSecondOne_EnsureSubDivisionOrdersGetSetCorrectly() throws Exception {
    Reference reference = new Reference("War on People");
    ReferenceSubDivision subDivision1 = new ReferenceSubDivision("Chapter 1");
    ReferenceSubDivision subDivision2 = new ReferenceSubDivision("Chapter 2");
    ReferenceSubDivision subDivision3 = new ReferenceSubDivision("Chapter 3");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addReference(reference);

    reference.addSubDivision(subDivision1);
    reference.addSubDivision(subDivision2);
    reference.addSubDivision(subDivision3);

    Assert.assertEquals(2, subDivision3.getSubDivisionOrder());

    reference.removeSubDivision(subDivision2);

    Assert.assertEquals(0, subDivision1.getSubDivisionOrder());
    Assert.assertEquals(1, subDivision3.getSubDivisionOrder());
  }


  @Test
  public void updateTableOfContents_UpdatedValueGetsPersistedInDb() throws Exception {
    Reference reference = new Reference("test");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addReference(reference);

    String newValue = "New value";
    reference.setTableOfContents(newValue);

    String actual = getClobFromTable(TableConfig.ReferenceTableName, TableConfig.ReferenceTableOfContentsColumnName, reference.getId());
    Assert.assertEquals(newValue, actual);
  }

  @Test
  public void setTableOfContentsWithMoreThan2048Characters_UpdatedTableOfContentsGetsPersistedInDb() throws Exception {
    Reference reference = new Reference("test");
    String tableOfContents = StringWithMoreThan2048CharactersLength;

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addReference(reference);

    reference.setTableOfContents(tableOfContents);

    // assert content really got written to database
    String actual = getClobFromTable(TableConfig.ReferenceTableName, TableConfig.ReferenceTableOfContentsColumnName, reference.getId());
    Assert.assertEquals(tableOfContents, actual);
  }

  @Test
  public void updatePublishingDate_UpdatedValueGetsPersistedInDb() throws Exception {
    Reference reference = new Reference("test");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addReference(reference);

    String newValue = "21.10.1983";
    reference.setIssueOrPublishingDate(newValue);

    Assert.assertEquals(newValue, getValueFromTable(TableConfig.ReferenceTableName, TableConfig.ReferenceIssueOrPublishingDateColumnName, reference.getId()));
  }


}

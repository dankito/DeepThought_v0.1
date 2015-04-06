package net.deepthought.data.model;

import net.deepthought.Application;
import net.deepthought.data.persistence.db.TableConfig;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by ganymed on 10/11/14.
 */
public abstract class SeriesTitleTestBase extends ReferenceBaseTestBase {

  @Override
  protected ReferenceBase createReferenceBaseInstanceAndAddToDeepThought() {
    SeriesTitle series = new SeriesTitle("test");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addSeriesTitle(series);

    return series;
  }


  @Test
  public void addSerialPart_RelationGetsPersisted() throws Exception {
    SeriesTitle series = new SeriesTitle("agora42");
    Reference reference = new Reference("Ausgabe 2/15");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addSeriesTitle(series);
    deepThought.addReference(reference);

    series.addSerialPart(reference);

    Assert.assertTrue(doIdsEqual(series.getId(), getValueFromTable(TableConfig.ReferenceTableName, TableConfig.ReferenceSeriesTitleJoinColumnName, reference.getId())));
  }

  @Test
  public void addSerialPart_EntitiesGetAddedToRelatedCollections() throws Exception {
    SeriesTitle series = new SeriesTitle("agora42");
    Reference reference = new Reference("Ausgabe 2/15");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addSeriesTitle(series);
    deepThought.addReference(reference);

    series.addSerialPart(reference);

    Assert.assertTrue(series.getSerialParts().contains(reference));
    Assert.assertEquals(series, reference.getSeries());

    Assert.assertEquals(0, reference.getSeriesOrder());
  }

  @Test
  public void addSerialPartWithSeriesSet_SeriesPropertyGetsUpdated() throws Exception {
    SeriesTitle series = new SeriesTitle("agora42");
    Reference reference = new Reference("Ausgabe 2/15");
    SeriesTitle referencesCurrentSeries = new SeriesTitle("Bild Zeitung");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addSeriesTitle(series);
    deepThought.addReference(reference);
    deepThought.addSeriesTitle(referencesCurrentSeries);

    reference.setSeries(referencesCurrentSeries);

    series.addSerialPart(reference);

    Assert.assertEquals(series, reference.getSeries());
    Assert.assertFalse(referencesCurrentSeries.getSerialParts().contains(reference));
  }

  @Test
  public void removeSerialPart_RelationGetsDeleted() throws Exception {
    SeriesTitle series = new SeriesTitle("agora42");
    Reference reference = new Reference("Ausgabe 2/15");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addSeriesTitle(series);
    deepThought.addReference(reference);

    series.addSerialPart(reference);

    series.removeSerialPart(reference);

    Assert.assertNull(getValueFromTable(TableConfig.ReferenceTableName, TableConfig.ReferenceSeriesTitleJoinColumnName, reference.getId()));
  }

  @Test
  public void removeSerialPart_EntitiesGetRemovedFromRelatedCollections() throws Exception {
    SeriesTitle series = new SeriesTitle("agora42");
    Reference reference = new Reference("Ausgabe 2/15");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addSeriesTitle(series);
    deepThought.addReference(reference);

    series.addSerialPart(reference);

    series.removeSerialPart(reference);

    Assert.assertFalse(series.getSerialParts().contains(reference));
    Assert.assertNull(reference.getSeries());
  }

  @Test
  public void removeSerialPart_SerialPartGetsNotDeletedFromDatabase() throws Exception {
    SeriesTitle series = new SeriesTitle("agora42");
    Reference reference = new Reference("Ausgabe 2/15");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addSeriesTitle(series);
    deepThought.addReference(reference);

    series.addSerialPart(reference);

    series.removeSerialPart(reference);

    Assert.assertFalse(series.isDeleted());
    Assert.assertNotNull(series.getId());

    Assert.assertFalse(reference.isDeleted());
    Assert.assertNotNull(reference.getId());
  }

  @Test
  public void add2ReferencesThanRemoveFirstOne_SubDivisionOrderGetsAdjusted() throws Exception {
    SeriesTitle series = new SeriesTitle("agora42");
    Reference reference1 = new Reference("Ausgabe 2/15");
    Reference reference2 = new Reference("Ausgabe 3/15");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addSeriesTitle(series);
    deepThought.addReference(reference1);
    deepThought.addReference(reference2);

    series.addSerialPart(reference1);
    series.addSerialPart(reference2);

    Assert.assertEquals(1, reference2.getSeriesOrder());

    series.removeSerialPart(reference1);

    Assert.assertEquals(0, reference2.getSeriesOrder());
  }

  @Test
  public void add3ReferencesThanRemoveSecondOne_EnsureSubDivisionOrdersGetSetCorrectly() throws Exception {
    SeriesTitle series = new SeriesTitle("agora42");
    Reference reference1 = new Reference("Ausgabe 2/15");
    Reference reference2 = new Reference("Ausgabe 3/15");
    Reference reference3 = new Reference("Ausgabe 4/15");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addSeriesTitle(series);
    deepThought.addReference(reference1);
    deepThought.addReference(reference2);
    deepThought.addReference(reference3);

    series.addSerialPart(reference1);
    series.addSerialPart(reference2);
    series.addSerialPart(reference3);

    Assert.assertEquals(2, reference3.getSeriesOrder());

    series.removeSerialPart(reference2);

    Assert.assertEquals(0, reference1.getSeriesOrder());
    Assert.assertEquals(1, reference3.getSeriesOrder());
  }


  @Test
  public void addSeriesTo2Entries_RelationGetsPersisted() throws Exception {
    SeriesTitle series = new SeriesTitle("agora42");
    Entry entry1 = new Entry("Entry 1");
    Entry entry2 = new Entry("Entry 2");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addSeriesTitle(series);
    deepThought.addEntry(entry1);
    deepThought.addEntry(entry2);

    entry1.setSeries(series);
    entry2.setSeries(series);

    Assert.assertTrue(doIdsEqual(series.getId(), getValueFromTable(TableConfig.EntryTableName, TableConfig.EntrySeriesTitleJoinColumnName, entry1.getId())));
    Assert.assertTrue(doIdsEqual(series.getId(), getValueFromTable(TableConfig.EntryTableName, TableConfig.EntrySeriesTitleJoinColumnName, entry2.getId())));
  }

  @Test
  public void addSeriesTo2Entries_RelationsGetSet() throws Exception {
    SeriesTitle series = new SeriesTitle("agora42");
    Entry entry1 = new Entry("Entry 1");
    Entry entry2 = new Entry("Entry 2");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addSeriesTitle(series);
    deepThought.addEntry(entry1);
    deepThought.addEntry(entry2);

    entry1.setSeries(series);
    entry2.setSeries(series);

    Assert.assertTrue(series.getEntries().contains(entry1));
    Assert.assertEquals(series, entry1.getSeries());

    Assert.assertTrue(series.getEntries().contains(entry2));
    Assert.assertEquals(series, entry2.getSeries());
  }

  @Test
  public void addSeriesTo2EntriesThenRemoveFromFirst_RelationGetsDeletedFromDb() throws Exception {
    SeriesTitle series = new SeriesTitle("agora42");
    Entry entry1 = new Entry("Entry 1");
    Entry entry2 = new Entry("Entry 2");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addSeriesTitle(series);
    deepThought.addEntry(entry1);
    deepThought.addEntry(entry2);

    entry1.setSeries(series);
    entry2.setSeries(series);

    entry1.setSeries(null);

    Assert.assertNull(getValueFromTable(TableConfig.EntryTableName, TableConfig.EntrySeriesTitleJoinColumnName, entry1.getId()));
  }

  @Test
  public void addSeriesTo2EntriesThenRemoveFromFirst_RelationsGetUnset() throws Exception {
    SeriesTitle series = new SeriesTitle("agora42");
    Entry entry1 = new Entry("Entry 1");
    Entry entry2 = new Entry("Entry 2");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addSeriesTitle(series);
    deepThought.addEntry(entry1);
    deepThought.addEntry(entry2);

    entry1.setSeries(series);
    entry2.setSeries(series);

    entry1.setSeries(null);

    Assert.assertFalse(series.getEntries().contains(entry1));
    Assert.assertNull(entry1.getSeries());
  }


  @Test
  public void updateTableOfContents_UpdatedValueGetsPersistedInDb() throws Exception {
    SeriesTitle series = new SeriesTitle("test");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addSeriesTitle(series);

    String newValue = "New value";
    series.setTableOfContents(newValue);

    String actual = getClobFromTable(TableConfig.SeriesTitleTableName, TableConfig.SeriesTitleTableOfContentsColumnName, series.getId());
    Assert.assertEquals(newValue, actual);
  }

  @Test
  public void setTableOfContentsWithMoreThan2048Characters_UpdatedTableOfContentsGetsPersistedInDb() throws Exception {
    SeriesTitle series = new SeriesTitle("test");
    String tableOfContents = DataModelTestBase.StringWithMoreThan2048CharactersLength;

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addSeriesTitle(series);

    series.setTableOfContents(tableOfContents);

    // assert content really got written to database
    String actual = getClobFromTable(TableConfig.SeriesTitleTableName, TableConfig.SeriesTitleTableOfContentsColumnName, series.getId());
    Assert.assertEquals(tableOfContents, actual);
  }

}

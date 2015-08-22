package net.deepthought.data.merger;

import net.deepthought.Application;
import net.deepthought.TestApplicationConfiguration;
import net.deepthought.data.helper.AssertSetToTrue;
import net.deepthought.data.helper.DatabaseHelper;
import net.deepthought.data.model.DeepThought;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.Tag;
import net.deepthought.data.persistence.EntityManagerConfiguration;
import net.deepthought.data.persistence.IEntityManager;
import net.deepthought.data.persistence.db.BaseEntity;
import net.deepthought.data.persistence.db.TableConfig;
import net.deepthought.util.DeepThoughtError;
import net.deepthought.util.ReflectionHelper;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by ganymed on 18/01/15.
 */
public abstract class DefaultDataMergerTestBase {

  protected IDataMerger dataMerger;


  protected abstract IEntityManager createEntityManager(EntityManagerConfiguration configuration) throws Exception;


  @Before
  public void setup() throws Exception {
    dataMerger = new DefaultDataMerger();

    Application.instantiate(new TestApplicationConfiguration() {
      @Override
      public IEntityManager createEntityManager(EntityManagerConfiguration configuration) throws Exception {
        return DefaultDataMergerTestBase.this.createEntityManager(configuration);
      }

      @Override
      public IDataMerger createDataMerger() {
        return DefaultDataMergerTestBase.this.dataMerger;
      }
    });
  }


  @Test
  public void mergeWithCurrentData_MergeCopiedEntity_EntityGetsMergedWithCurrentData() throws SQLException {
    DeepThought deepThought = Application.getDeepThought();
    Tag tagToMergeWith = new Tag("Tag to merge with");
    deepThought.addTag(tagToMergeWith);

    final Tag mergeCandidate = (Tag)ReflectionHelper.copyObject(tagToMergeWith);
    String newTagName = "I have been merged";
    mergeCandidate.setName(newTagName);

    final Set<Boolean> allStepsSucceededContainer = new HashSet<>();
    final AssertSetToTrue mergeResult = new AssertSetToTrue();

    dataMerger.mergeWithCurrentData(new ArrayList<BaseEntity>() {{ add(mergeCandidate); }}, false, new MergeDataListener() {
      @Override
      public void beginToMergeEntity(BaseEntity entity) {

      }

      @Override
      public void mergeEntityResult(BaseEntity entity, boolean successful, DeepThoughtError error) {
        allStepsSucceededContainer.add(successful);
      }

      @Override
      public void addingEntitiesDone(boolean successful, List<BaseEntity> entitiesSucceededToInsert, List<BaseEntity> entitiesFailedToInsert) {
        mergeResult.setValue(successful);
      }
    });

    Assert.assertTrue(mergeResult.getValue());
    Assert.assertFalse(allStepsSucceededContainer.contains(false));

    String persistedTagName = (String)DatabaseHelper.getValueFromTable(Application.getEntityManager(), TableConfig.TagTableName, TableConfig.TagNameColumnName,
        tagToMergeWith.getId());
    Assert.assertEquals(newTagName, persistedTagName);
  }

  @Test
  public void mergeWithCurrentData_MergeCopiedEntityWithSubEntities_EntityGetsMergedWithCurrentData() throws SQLException {
    DeepThought deepThought = Application.getDeepThought();
    final Entry entry1 = new Entry("Entry 1", "");
    deepThought.addEntry(entry1);
    Entry entry2 = new Entry("Entry 2", "");
    deepThought.addEntry(entry2);

    Tag tagToMergeWith = new Tag("Tag to merge with");
    deepThought.addTag(tagToMergeWith);
    entry1.addTag(tagToMergeWith);
    entry2.addTag(tagToMergeWith);

    final Entry mergeCandidateEntry = (Entry)ReflectionHelper.copyObject(entry1);
    String newEntry1Title = "I have been merged";
    mergeCandidateEntry.setTitle(newEntry1Title);

    final Tag mergeCandidateTag = (Tag)ReflectionHelper.copyObject(tagToMergeWith);
    String newTagName = "I have been merged";
    mergeCandidateTag.setName(newTagName);

    mergeCandidateEntry.removeTag(tagToMergeWith);
    mergeCandidateEntry.addTag(mergeCandidateTag);

    final Set<Boolean> allStepsSucceededContainer = new HashSet<>();
    final Set<BaseEntity> mergedEntitiesContainer = new HashSet<>();
    final AssertSetToTrue mergeResult = new AssertSetToTrue();

    dataMerger.mergeWithCurrentData(new ArrayList<BaseEntity>() {{ add(mergeCandidateEntry); add(mergeCandidateTag); }}, false, new MergeDataListener() {
      @Override
      public void beginToMergeEntity(BaseEntity entity) {

      }

      @Override
      public void mergeEntityResult(BaseEntity entity, boolean successful, DeepThoughtError error) {
        allStepsSucceededContainer.add(successful);
        mergedEntitiesContainer.add(entity);
      }

      @Override
      public void addingEntitiesDone(boolean successful, List<BaseEntity> entitiesSucceededToInsert, List<BaseEntity> entitiesFailedToInsert) {
        mergeResult.setValue(successful);
      }
    });

    Assert.assertTrue(mergeResult.getValue());
    Assert.assertFalse(allStepsSucceededContainer.contains(false));

    Assert.assertTrue(mergedEntitiesContainer.contains(mergeCandidateEntry));
    Assert.assertTrue(mergedEntitiesContainer.contains(mergeCandidateTag));

    String persistedEntry1Title = (String)DatabaseHelper.getValueFromTable(Application.getEntityManager(), TableConfig.EntryTableName, TableConfig.EntryTitleColumnName, entry1.getId());
    Assert.assertEquals(newEntry1Title, persistedEntry1Title);

    String persistedTagName = (String)DatabaseHelper.getValueFromTable(Application.getEntityManager(), TableConfig.TagTableName, TableConfig.TagNameColumnName, tagToMergeWith.getId());
    Assert.assertEquals(newTagName, persistedTagName);
  }
}

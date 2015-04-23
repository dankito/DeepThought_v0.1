package net.deepthought.data.model;

import net.deepthought.Application;
import net.deepthought.data.TestApplicationConfiguration;
import net.deepthought.data.helper.TestDependencyResolver;
import net.deepthought.data.persistence.EntityManagerConfiguration;
import net.deepthought.data.persistence.IEntityManager;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ganymed on 16/12/14.
 */
public abstract class LoadTagTestBase extends DataModelTestBase {

  protected IEntityManager loadSavedDataEntityManager = null;

  protected DeepThought loadedDeepThought = null;

  @Before
  public void setup() throws Exception {
    super.setup();

    for(Tag tag : new ArrayList<>(Application.getDeepThought().getTags()))
      Application.getDeepThought().removeTag(tag);
    for(Entry entry : new ArrayList<>(Application.getDeepThought().getEntries()))
      Application.getDeepThought().removeEntry(entry);

    Entry entry1 = new Entry("1");
    Entry entry2 = new Entry("2");
    Entry entry3 = new Entry("3");
    Application.getDeepThought().addEntry(entry1);
    Application.getDeepThought().addEntry(entry2);
    Application.getDeepThought().addEntry(entry3);

    Tag tag1 = new Tag("1");
    Tag tag2 = new Tag("2");
    Tag tag3 = new Tag("3");
    Application.getDeepThought().addTag(tag1);
    Application.getDeepThought().addTag(tag2);
    Application.getDeepThought().addTag(tag3);

    entry1.addTag(tag1);
    entry2.addTag(tag1);
    entry2.addTag(tag2);
    entry3.addTag(tag2);
    entry3.addTag(tag3);
    entry1.addTag(tag3);

    Application.shutdown();

    Application.instantiate(new TestApplicationConfiguration(), new TestDependencyResolver() {
      @Override
      public IEntityManager createEntityManager(EntityManagerConfiguration configuration) throws Exception {
        entityManager = getEntityManager(configuration);
        return entityManager;
      }
    });

//    loadSavedDataEntityManager = Application.getDependencyResolver().createEntityManager(configuration);
//    loadedDeepThought = loadSavedDataEntityManager.getEntityById(DeepThought.class, Application.getDeepThought().getId());
    loadSavedDataEntityManager = Application.getEntityManager();
    loadedDeepThought = Application.getDeepThought();
  }

  @After
  public void tearDown() {
    loadSavedDataEntityManager.deleteEntity(Application.getApplication()); // damn, why doesn't it close the db properly? So next try: delete DeepThoughtApplication object
    Application.shutdown();
  }


  @Test
  public void testEntriesGetLoadedCorrectly() throws Exception {
    Assert.assertEquals(3, loadedDeepThought.getTags().size());
    Assert.assertEquals(3, loadedDeepThought.getEntries().size());

    List<Entry> loadedSortedEntries = new ArrayList<>(loadedDeepThought.getEntries());
    Entry entry1 = loadedSortedEntries.get(2);
    Entry entry2 = loadedSortedEntries.get(1);
    Entry entry3 = loadedSortedEntries.get(0);

    List<Tag> loadedSortedTags = new ArrayList<>(loadedDeepThought.getSortedTags());

    Tag tag1 = loadedSortedTags.get(0);
    Assert.assertEquals("1", tag1.getName());
    Assert.assertEquals(2, tag1.getEntries().size());

    List<Entry> loadedTagEntries = new ArrayList<>(tag1.getEntries());
    Assert.assertEquals(entry2, loadedTagEntries.get(0));
    Assert.assertEquals(entry1, loadedTagEntries.get(1));

    Tag tag2 = loadedSortedTags.get(1);
    Assert.assertEquals("2", tag2.getName());
    Assert.assertEquals(2, tag2.getEntries().size());

    loadedTagEntries = new ArrayList<>(tag2.getEntries());
    Assert.assertEquals(entry3, loadedTagEntries.get(0));
    Assert.assertEquals(entry2, loadedTagEntries.get(1));

    Tag tag3 = loadedSortedTags.get(2);
    Assert.assertEquals("3", tag3.getName());
    Assert.assertEquals(2, tag3.getEntries().size());

    loadedTagEntries = new ArrayList<>(tag3.getEntries());
    Assert.assertEquals(entry3, loadedTagEntries.get(0));
    Assert.assertEquals(entry1, loadedTagEntries.get(1));
  }


}

package net.deepthought.data.model;

import net.deepthought.Application;
import net.deepthought.data.TestApplicationConfiguration;
import net.deepthought.data.helper.TestDependencyResolver;
import net.deepthought.data.model.enums.BackupFileServiceType;
import net.deepthought.data.model.enums.ExtensibleEnumeration;
import net.deepthought.data.model.enums.Language;
import net.deepthought.data.model.enums.NoteType;
import net.deepthought.data.persistence.EntityManagerConfiguration;
import net.deepthought.data.persistence.IEntityManager;
import net.deepthought.data.persistence.db.UserDataEntity;
import net.deepthought.util.file.FileUtils;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ganymed on 16/12/14.
 */
public abstract class LoadDeepThoughtTestBase extends DataModelTestBase {

  protected IEntityManager loadSavedDataEntityManager = null;

  protected DeepThought loadedDeepThought = null;

  @Before
  public void setup() throws Exception {
    super.setup();

    for(Entry entry : new ArrayList<>(Application.getDeepThought().getEntries()))
      Application.getDeepThought().removeEntry(entry);
    // add three test Entries
    Application.getDeepThought().addEntry(new Entry("1"));
    Application.getDeepThought().addEntry(new Entry("2"));
    Application.getDeepThought().addEntry(new Entry("3"));

//    entityManager.close();
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
    FileUtils.deleteFile(loadSavedDataEntityManager.getDatabasePath());
  }


  @Test
  public void testDefaultLanguagesGetLoadedCorrectly() throws Exception {
    Assert.assertEquals(49, loadedDeepThought.getLanguages().size());

    int index = 1;
    for(Language language : loadedDeepThought.getLanguages()) {
      testExtensibleEnumerationFields(language, true);
      Assert.assertEquals(index, language.getSortOrder());
      index++;
    }
  }

  @Test
  public void testDefaultNoteTypesGetLoadedCorrectly() throws Exception {
    Assert.assertEquals(5, loadedDeepThought.getNoteTypes().size());

    int index = 1;
    for(NoteType noteType : loadedDeepThought.getNoteTypes()) {
      testExtensibleEnumerationFields(noteType, false);
      Assert.assertEquals(index, noteType.getSortOrder());
      index++;
    }
  }

  @Test
  public void testDefaultBackupFileServiceTypesGetLoadedCorrectly() throws Exception {
    Assert.assertEquals(3, loadedDeepThought.getBackupFileServiceTypes().size());

    int index = 1;
    for(BackupFileServiceType backupFileServiceType : loadedDeepThought.getBackupFileServiceTypes()) {
      testExtensibleEnumerationFields(backupFileServiceType, false);
      Assert.assertEquals(index, backupFileServiceType.getSortOrder());
      index++;
    }
  }

  @Test
  public void testEntriesGetLoadedCorrectly() throws Exception {
    Assert.assertEquals(3, loadedDeepThought.getEntries().size());

    List<Entry> loadedEntries = new ArrayList<>(loadedDeepThought.getEntries());
    for(int i = loadedEntries.size() - 1; i >= 0; i--) {
      Entry entry = loadedEntries.get(i);
      Assert.assertEquals(loadedEntries.size() - i , entry.getEntryIndex());
      Assert.assertEquals(loadedEntries.size() - i, Integer.parseInt(entry.getContent()));
    }
  }


  protected void testUserDataEntityFields(UserDataEntity entity) {
    Assert.assertNotNull(entity.getId());
    Assert.assertNotNull(entity.getVersion());
    Assert.assertNotNull(entity.getCreatedBy());
    Assert.assertNotNull(entity.getModifiedBy());
    Assert.assertNotNull(entity.getOwner());
    Assert.assertFalse(entity.isDeleted());
  }

  protected void testExtensibleEnumerationFields(ExtensibleEnumeration entity, boolean isDeletable) {
    testUserDataEntityFields(entity);

    Assert.assertNotNull(entity.getName());
    Assert.assertNotNull(entity.getDeepThought());
    Assert.assertTrue(entity.isSystemValue());

    if(isDeletable == true)
      Assert.assertTrue(entity.isDeletable());
    else
      Assert.assertFalse(entity.isDeletable());
  }

}

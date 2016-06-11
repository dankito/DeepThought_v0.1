package net.dankito.deepthought.data.model;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.data.model.enums.BackupFileServiceType;
import net.dankito.deepthought.data.model.enums.ExtensibleEnumeration;
import net.dankito.deepthought.data.model.enums.Language;
import net.dankito.deepthought.data.model.enums.NoteType;
import net.dankito.deepthought.data.persistence.db.UserDataEntity;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by ganymed on 16/12/14.
 */
public abstract class CreateDeepThoughtTestBase extends DataModelTestBase {

  @Test
  public void testDefaultLanguagesGetCreatedCorrectly() throws Exception {
    DeepThought deepThought = Application.getDeepThought();

    Assert.assertEquals(49, deepThought.getLanguages().size());

    int index = 1;
    for(Language language : deepThought.getLanguages()) {
      testExtensibleEnumerationFields(language, true);
      Assert.assertEquals(index, language.getSortOrder());
      index++;
    }
  }

  @Test
  public void testDefaultNoteTypesGetCreatedCorrectly() throws Exception {
    DeepThought deepThought = Application.getDeepThought();

    Assert.assertEquals(5, deepThought.getNoteTypes().size());

    int index = 1;
    for(NoteType noteType : deepThought.getNoteTypes()) {
      testExtensibleEnumerationFields(noteType, false);
      Assert.assertEquals(index, noteType.getSortOrder());
      index++;
    }
  }

  @Test
  public void testDefaultBackupFileServiceTypesGetCreatedCorrectly() throws Exception {
    DeepThought deepThought = Application.getDeepThought();

    Assert.assertEquals(3, deepThought.getBackupFileServiceTypes().size());

    int index = 1;
    for(BackupFileServiceType backupFileServiceType : deepThought.getBackupFileServiceTypes()) {
      testExtensibleEnumerationFields(backupFileServiceType, false);
      Assert.assertEquals(index, backupFileServiceType.getSortOrder());
      index++;
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

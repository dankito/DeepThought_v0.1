package net.deepthought.data.model;

import net.deepthought.Application;
import net.deepthought.data.model.enums.BackupFileServiceType;
import net.deepthought.data.model.enums.ExtensibleEnumeration;
import net.deepthought.data.model.enums.Language;
import net.deepthought.data.model.enums.NoteType;
import net.deepthought.data.model.enums.PersonRole;
import net.deepthought.data.model.enums.ReferenceCategory;
import net.deepthought.data.model.enums.ReferenceIndicationUnit;
import net.deepthought.data.model.enums.ReferenceSubDivisionCategory;
import net.deepthought.data.model.enums.SeriesTitleCategory;
import net.deepthought.data.persistence.db.UserDataEntity;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by ganymed on 16/12/14.
 */
public abstract class CreateDeepThoughtTestBase extends DataModelTestBase {

  @Test
  public void testDefaultLanguagesGetCreatedCorrectly() throws Exception {
    DeepThought deepThought = Application.getDeepThought();

    Assert.assertEquals(2, deepThought.getLanguages().size());

    int index = 1;
    for(Language language : deepThought.getLanguages()) {
      testExtensibleEnumerationFields(language, true);
      Assert.assertEquals(index, language.getSortOrder());
      index++;
    }
  }

  @Test
  public void testDefaultPersonRolesGetCreatedCorrectly() throws Exception {
    DeepThought deepThought = Application.getDeepThought();

    Assert.assertEquals(15, deepThought.getPersonRoles().size());

    int index = 1;
    for(PersonRole personRole : deepThought.getPersonRoles()) {
      if(personRole == PersonRole.getWithoutRolePersonRole())
        testExtensibleEnumerationFields(personRole, false);
      else {
        testExtensibleEnumerationFields(personRole, true);

        Assert.assertEquals(index, personRole.getSortOrder());
        index++;
      }
    }
  }

  @Test
  public void testDefaultSeriesTitleCategoriesGetCreatedCorrectly() throws Exception {
    DeepThought deepThought = Application.getDeepThought();

    Assert.assertEquals(7, deepThought.getSeriesTitleCategories().size());

    int index = 1;
    for(SeriesTitleCategory seriesTitleCategory : deepThought.getSeriesTitleCategories()) {
      testExtensibleEnumerationFields(seriesTitleCategory, true);
      Assert.assertEquals(index, seriesTitleCategory.getSortOrder());
      index++;
    }
  }

  @Test
  public void testDefaultReferenceCategoriesGetCreatedCorrectly() throws Exception {
    DeepThought deepThought = Application.getDeepThought();

    Assert.assertEquals(8, deepThought.getReferenceCategories().size());

    int index = 1;
    for(ReferenceCategory referenceCategory : deepThought.getReferenceCategories()) {
      testExtensibleEnumerationFields(referenceCategory, true);
      Assert.assertEquals(index, referenceCategory.getSortOrder());
      index++;
    }
  }

  @Test
  public void testDefaultReferenceSubDivisionCategoriesGetCreatedCorrectly() throws Exception {
    DeepThought deepThought = Application.getDeepThought();

    Assert.assertEquals(4, deepThought.getReferenceSubDivisionCategories().size());

    int index = 1;
    for(ReferenceSubDivisionCategory referenceSubDivisionCategory : deepThought.getReferenceSubDivisionCategories()) {
      testExtensibleEnumerationFields(referenceSubDivisionCategory, true);
      Assert.assertEquals(index, referenceSubDivisionCategory.getSortOrder());
      index++;
    }
  }

  @Test
  public void testDefaultReferenceIndicationUnitsGetCreatedCorrectly() throws Exception {
    DeepThought deepThought = Application.getDeepThought();

    Assert.assertEquals(3, deepThought.getReferenceIndicationUnits().size());

    int index = 1;
    for(ReferenceIndicationUnit unit : deepThought.getReferenceIndicationUnits()) {
      testExtensibleEnumerationFields(unit, false);
      Assert.assertEquals(index, unit.getSortOrder());
      index++;
    }
  }

  @Test
  public void testDefaultNoteTypesGetCreatedCorrectly() throws Exception {
    DeepThought deepThought = Application.getDeepThought();

    Assert.assertEquals(4, deepThought.getNoteTypes().size());

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

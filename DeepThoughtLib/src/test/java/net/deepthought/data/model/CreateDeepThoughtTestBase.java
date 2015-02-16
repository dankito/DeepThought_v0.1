package net.deepthought.data.model;

import net.deepthought.Application;
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

    for(Language language : deepThought.getLanguages()) {
      testExtensibleEnumerationFields(language, true);
    }
  }

  @Test
  public void testDefaultPersonRolesGetCreatedCorrectly() throws Exception {
    DeepThought deepThought = Application.getDeepThought();

    Assert.assertEquals(15, deepThought.getPersonRoles().size());

    for(PersonRole personRole : deepThought.getPersonRoles()) {
      testExtensibleEnumerationFields(personRole, true);
    }
  }

  @Test
  public void testDefaultSeriesTitleCategoriesGetCreatedCorrectly() throws Exception {
    DeepThought deepThought = Application.getDeepThought();

    Assert.assertEquals(7, deepThought.getSeriesTitleCategories().size());

    for(SeriesTitleCategory seriesTitleCategory : deepThought.getSeriesTitleCategories()) {
      testExtensibleEnumerationFields(seriesTitleCategory, true);
    }
  }

  @Test
  public void testDefaultReferenceCategoriesGetCreatedCorrectly() throws Exception {
    DeepThought deepThought = Application.getDeepThought();

    Assert.assertEquals(7, deepThought.getReferenceCategories().size());

    for(ReferenceCategory referenceCategory : deepThought.getReferenceCategories()) {
      testExtensibleEnumerationFields(referenceCategory, true);
    }
  }

  @Test
  public void testDefaultReferenceSubDivisionCategoriesGetCreatedCorrectly() throws Exception {
    DeepThought deepThought = Application.getDeepThought();

    Assert.assertEquals(4, deepThought.getReferenceSubDivisionCategories().size());

    for(ReferenceSubDivisionCategory referenceSubDivisionCategory : deepThought.getReferenceSubDivisionCategories()) {
      testExtensibleEnumerationFields(referenceSubDivisionCategory, true);
    }
  }

  @Test
  public void testDefaultReferenceIndicationUnitsGetCreatedCorrectly() throws Exception {
    DeepThought deepThought = Application.getDeepThought();

    Assert.assertEquals(3, deepThought.getReferenceIndicationUnits().size());

    for(ReferenceIndicationUnit referenceSubDivisionCategory : deepThought.getReferenceIndicationUnits()) {
      testExtensibleEnumerationFields(referenceSubDivisionCategory, false);
    }
  }

  @Test
  public void testDefaultNoteTypesGetCreatedCorrectly() throws Exception {
    DeepThought deepThought = Application.getDeepThought();

    Assert.assertEquals(4, deepThought.getNoteTypes().size());

    for(NoteType noteType : deepThought.getNoteTypes()) {
      testExtensibleEnumerationFields(noteType, false);
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

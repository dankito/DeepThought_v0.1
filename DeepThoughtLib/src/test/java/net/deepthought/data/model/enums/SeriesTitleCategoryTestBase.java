package net.deepthought.data.model.enums;

import net.deepthought.Application;
import net.deepthought.data.model.DeepThought;
import net.deepthought.data.persistence.db.TableConfig;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by ganymed on 10/11/14.
 */
public abstract class SeriesTitleCategoryTestBase extends EditableExtensibleEnumerationTestBase<ReferenceCategory> {

  @Override
  protected ExtensibleEnumeration getExistingExtensibleEnumeration() {
    DeepThought deepThought = Application.getDeepThought();
    List<ReferenceCategory> referenceCategories = new ArrayList<>(deepThought.getReferenceCategories());

    return referenceCategories.get(0);
  }

  @Override
  protected String getEnumerationTableName() {
    return TableConfig.ReferenceCategoryTableName;
  }


  @Override
  protected ReferenceCategory createNewEnumValue() {
    return new ReferenceCategory("Love");
  }

  @Override
  protected void addToEnumeration(ReferenceCategory enumValue) {
    Application.getDeepThought().addReferenceCategory(enumValue);
  }

  @Override
  protected void removeFromEnumeration(ReferenceCategory enumValue) {
    Application.getDeepThought().removeReferenceCategory(enumValue);
  }

  @Override
  protected Collection<ReferenceCategory> getEnumeration() {
    return Application.getDeepThought().getReferenceCategories();
  }

}

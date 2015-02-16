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
public abstract class ReferenceSubDivisionCategoryTestBase extends EditableExtensibleEnumerationTestBase<SeriesTitleCategory> {

  @Override
  protected ExtensibleEnumeration getExistingExtensibleEnumeration() {
    DeepThought deepThought = Application.getDeepThought();
    List<SeriesTitleCategory> seriesTitleCategories = new ArrayList<>(deepThought.getSeriesTitleCategories());

    return seriesTitleCategories.get(0);
  }

  @Override
  protected String getEnumerationTableName() {
    return TableConfig.SeriesTitleCategoryTableName;
  }


  @Override
  protected SeriesTitleCategory createNewEnumValue() {
    return new SeriesTitleCategory("Love");
  }

  @Override
  protected void addToEnumeration(SeriesTitleCategory enumValue) {
    Application.getDeepThought().addSeriesTitleCategory(enumValue);
  }

  @Override
  protected void removeFromEnumeration(SeriesTitleCategory enumValue) {
    Application.getDeepThought().removeSeriesTitleCategory(enumValue);
  }

  @Override
  protected Collection<SeriesTitleCategory> getEnumeration() {
    return Application.getDeepThought().getSeriesTitleCategories();
  }

}

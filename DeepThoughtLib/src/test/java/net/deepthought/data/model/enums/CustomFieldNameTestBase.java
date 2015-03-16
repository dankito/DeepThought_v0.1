package net.deepthought.data.model.enums;

import net.deepthought.Application;
import net.deepthought.data.model.DeepThought;
import net.deepthought.data.persistence.db.TableConfig;

import org.junit.Before;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by ganymed on 10/11/14.
 */
public abstract class CustomFieldNameTestBase extends EditableExtensibleEnumerationTestBase<CustomFieldName> {

  @Before
  public void setup() throws Exception {
    super.setup();

    DeepThought deepThought = Application.getDeepThought();
    for(int i = 1; i < 5; i++)
      deepThought.addCustomFieldName(new CustomFieldName("Test" + i));
  }

  @Override
  protected ExtensibleEnumeration getExistingExtensibleEnumeration() {
    DeepThought deepThought = Application.getDeepThought();
    List<CustomFieldName> customFieldNames = new ArrayList<>(deepThought.getCustomFieldNames());

    return customFieldNames.get(0);
  }

  @Override
  protected String getEnumerationTableName() {
    return TableConfig.CustomFieldNameTableName;
  }


  @Override
  protected CustomFieldName createNewEnumValue() {
    return new CustomFieldName("Love");
  }

  @Override
  protected void addToEnumeration(CustomFieldName enumValue) {
    Application.getDeepThought().addCustomFieldName(enumValue);
  }

  @Override
  protected void removeFromEnumeration(CustomFieldName enumValue) {
    Application.getDeepThought().removeCustomFieldName(enumValue);
  }

  @Override
  protected Collection<CustomFieldName> getEnumeration() {
    return Application.getDeepThought().getCustomFieldNames();
  }

}

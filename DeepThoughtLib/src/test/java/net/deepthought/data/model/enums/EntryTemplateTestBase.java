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
public abstract class EntryTemplateTestBase extends EditableExtensibleEnumerationTestBase<EntryTemplate> {

  @Override
  protected ExtensibleEnumeration getExistingExtensibleEnumeration() {
    DeepThought deepThought = Application.getDeepThought();
    List<EntryTemplate> entryTemplates = new ArrayList<>(deepThought.getEntryTemplates());

    return entryTemplates.get(0);
  }

  @Override
  protected String getEnumerationTableName() {
    return TableConfig.EntryTemplateTableName;
  }


  @Override
  protected EntryTemplate createNewEnumValue() {
    return new EntryTemplate("all", "you", "need");
  }

  @Override
  protected void addToEnumeration(EntryTemplate enumValue) {
    Application.getDeepThought().addEntryTemplate(enumValue);
  }

  @Override
  protected void removeFromEnumeration(EntryTemplate enumValue) {
    Application.getDeepThought().removeEntryTemplate(enumValue);
  }

  @Override
  protected Collection<EntryTemplate> getEnumeration() {
    return Application.getDeepThought().getEntryTemplates();
  }

}

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
public abstract class LanguageTestBase extends EditableExtensibleEnumerationTestBase<Language> {

  @Override
  protected ExtensibleEnumeration getExistingExtensibleEnumeration() {
    DeepThought deepThought = Application.getDeepThought();
    List<Language> languages = new ArrayList<>(deepThought.getLanguages());

    return languages.get(0);
  }

  @Override
  protected String getEnumerationTableName() {
    return TableConfig.LanguageTableName;
  }


  @Override
  protected Language createNewEnumValue() {
    return new Language("Love");
  }

  @Override
  protected void addToEnumeration(Language enumValue) {
    Application.getDeepThought().addLanguage(enumValue);
  }

  @Override
  protected void removeFromEnumeration(Language enumValue) {
    Application.getDeepThought().removeLanguage(enumValue);
  }

  @Override
  protected Collection<Language> getEnumeration() {
    return Application.getDeepThought().getLanguages();
  }

}

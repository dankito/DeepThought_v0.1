package net.deepthought.data.model.enums;

import net.deepthought.Application;
import net.deepthought.data.persistence.db.TableConfig;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * Created by ganymed on 21/01/15.
 */
@Entity(name = TableConfig.LanguageTableName)
public class Language extends ExtensibleEnumeration {

  private static final long serialVersionUID = -3214627251287229480L;


  @Column(name = TableConfig.LanguageLanguageKeyColumnName)
  protected String languageKey = "";

  @Column(name = TableConfig.LanguageNameInLanguageColumnName)
  protected String nameInLanguage = "";

  // TODO: map Entries, References and SeriesTitles


  public Language() {

  }

  public Language(String name) {
    super(name);
  }

  public Language(String nameResourceKey, boolean isSystemValue, boolean isDeletable, int sortOrder) {
    super(nameResourceKey, isSystemValue, isDeletable, sortOrder);
  }

  public Language(String languageKey, String nameInLanguage, String nameResourceKey, boolean isSystemValue, boolean isDeletable, int sortOrder) {
    super(nameResourceKey, isSystemValue, isDeletable, sortOrder);

    this.languageKey = languageKey;
    this.nameInLanguage = nameInLanguage;
  }


  public String getLanguageKey() {
    return languageKey;
  }

  public String getNameInLanguage() {
    return nameInLanguage;
  }


  @Override
  public String toString() {
    return "Language " + getTextRepresentation();
  }


  public static Language LanguageWithThatNameNotFound = new Language("Language with that name not found");

  protected static Language defaultLanguage = null;

  public static Language getDefaultLanguage() {
    if(defaultLanguage == null)
      defaultLanguage = findByNameResourceKey("language.english");
    return defaultLanguage;
  }

  public static Language findByLanguageKey(String languageKey) {
    for(Language language : Application.getDeepThought().getLanguages()) {
      if(languageKey.equals(language.getLanguageKey()))
        return language;
    }

    return LanguageWithThatNameNotFound;
  }

  public static Language findByNameResourceKey(String nameResourceKey) {
    for(Language language : Application.getDeepThought().getLanguages()) {
      if(nameResourceKey.equals(language.nameResourceKey))
        return language;
    }

    return LanguageWithThatNameNotFound;
  }

}

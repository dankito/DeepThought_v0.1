package net.deepthought.data.model.enums;

import net.deepthought.data.persistence.db.TableConfig;

import javax.persistence.Entity;

/**
 * Created by ganymed on 21/01/15.
 */
@Entity(name = TableConfig.LanguageTableName)
public class Language extends ExtensibleEnumeration {

  private static final long serialVersionUID = -3214627251287229480L;

  // TODO: map Entries, References and SeriesTitles


  public Language() {

  }

  public Language(String name) {
    super(name);
  }

  public Language(String nameResourceKey, boolean isSystemValue, boolean isDeletable, int sortOrder) {
    super(nameResourceKey, isSystemValue, isDeletable, sortOrder);
  }


  @Override
  public String toString() {
    return "Language " + getTextRepresentation();
  }

}

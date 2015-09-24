package net.deepthought.data.model.enums;

import net.deepthought.Application;
import net.deepthought.data.model.DeepThought;
import net.deepthought.data.model.DeepThoughtApplication;
import net.deepthought.data.persistence.db.TableConfig;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * Created by ganymed on 21/01/15.
 */
@Entity(name = TableConfig.ApplicationLanguageTableName)
public class ApplicationLanguage extends ExtensibleEnumeration {

  private static final long serialVersionUID = -446610923063763955L;

  @Column(name = TableConfig.ApplicationLanguageLanguageKeyColumnName)
  protected String languageKey;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = TableConfig.ApplicationLanguageDeepThoughtApplicationJoinColumnName)
  protected DeepThoughtApplication application;


  protected ApplicationLanguage() {

  }

  public ApplicationLanguage(String name) {
    super(name);
  }

  public ApplicationLanguage(String nameResourceKey, String languageKey, boolean isSystemValue, boolean isDeletable, int sortOrder) {
    super(nameResourceKey, isSystemValue, isDeletable, sortOrder);
    this.languageKey = languageKey;
  }


  public String getLanguageKey() {
    return languageKey;
  }

  public DeepThoughtApplication getApplication() {
    return application;
  }

  public void setApplication(DeepThoughtApplication application) {
    this.application = application;
  }

  @Override
  public void setDeepThought(DeepThought deepThought) {
    throw new UnsupportedOperationException("get/setDeepThought is inherited from super class but not supported for ApplicationLanguage as ApplicationLanguages get stored on " +
        "DeepThoughtApplication and not DeepThought.");
  }

  @Override
  public String toString() {
    return "ApplicationLanguage " + getTextRepresentation();
  }


  public static void resetStaticCaches() {
    defaultApplicationLanguage = null;
  }

  public static ApplicationLanguage ApplicationLanguageWithThatNameNotFound = new ApplicationLanguage("ApplicationLanguage with that name not found");

  protected static ApplicationLanguage defaultApplicationLanguage = null;

  public static ApplicationLanguage getDefaultApplicationLanguage() {
    if(defaultApplicationLanguage == null)
      defaultApplicationLanguage = findByNameResourceKey("application.language.english");
    return defaultApplicationLanguage;
  }

  public static ApplicationLanguage findByNameResourceKey(String nameResourceKey) {
    for(ApplicationLanguage language : Application.getApplication().getApplicationLanguages()) {
      if(nameResourceKey.equals(language.nameResourceKey))
        return language;
    }

    return ApplicationLanguageWithThatNameNotFound;
  }

}

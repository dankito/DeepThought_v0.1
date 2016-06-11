package net.dankito.deepthought.data.model.enums;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.data.model.DeepThoughtApplication;
import net.dankito.deepthought.data.persistence.db.TableConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ganymed on 10/11/14.
 */
public abstract class ApplicationLanguageTestBase extends ExtensibleEnumerationTestBase {

  @Override
  protected ExtensibleEnumeration getExistingExtensibleEnumeration() {
    DeepThoughtApplication application = Application.getApplication();
    List<ApplicationLanguage> applicationLanguages = new ArrayList<>(application.getApplicationLanguages());

    return applicationLanguages.get(0);
  }

  @Override
  protected String getEnumerationTableName() {
    return TableConfig.ApplicationLanguageTableName;
  }

}

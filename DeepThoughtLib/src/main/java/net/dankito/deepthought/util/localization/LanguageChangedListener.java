package net.dankito.deepthought.util.localization;

import net.dankito.deepthought.data.model.enums.ApplicationLanguage;

/**
 * Created by ganymed on 30/01/16.
 */
public interface LanguageChangedListener {

  void languageChanged(ApplicationLanguage newLanguage);

}

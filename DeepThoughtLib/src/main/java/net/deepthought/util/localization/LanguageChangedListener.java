package net.deepthought.util.localization;

import net.deepthought.data.model.enums.ApplicationLanguage;

/**
 * Created by ganymed on 30/01/16.
 */
public interface LanguageChangedListener {

  void languageChanged(ApplicationLanguage newLanguage);

}

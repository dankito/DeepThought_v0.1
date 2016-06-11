package net.dankito.deepthought.language;

import net.dankito.deepthought.data.model.enums.Language;

/**
 * Created by ganymed on 23/04/15.
 */
public class NoOpLanguageDetector implements ILanguageDetector {

  @Override
  public Language detectLanguageOfText(String text) {
    return CouldNotDetectLanguage;
  }

}

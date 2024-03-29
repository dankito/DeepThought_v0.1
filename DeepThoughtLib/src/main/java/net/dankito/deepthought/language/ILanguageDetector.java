package net.dankito.deepthought.language;

import net.dankito.deepthought.data.model.enums.Language;

/**
 * Created by ganymed on 12/04/15.
 */
public interface ILanguageDetector {

  public final static Language CouldNotDetectLanguage = new Language("CouldNotDetectLanguage");

  public Language detectLanguageOfText(String text);

}

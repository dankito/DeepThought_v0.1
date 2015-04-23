package net.deepthought.language;

/**
 * Created by ganymed on 23/04/15.
 */
public class NoOpLanguageDetector implements ILanguageDetector {

  @Override
  public String detectLanguageOfText(String text) {
    return CouldNotDetectLanguage;
  }

}

package net.deepthought.language;

/**
 * Created by ganymed on 12/04/15.
 */
public interface ILanguageDetector {

  public final static String CouldNotDetectLanguage = "CouldNotDetectLanguage";

  public String detectLanguageOfText(String text);

}

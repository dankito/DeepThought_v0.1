package net.deepthought.language;

import com.norconex.language.detector.DetectedLanguage;
import com.norconex.language.detector.DetectedLanguages;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 *  This actually is a Wrapper for the Norconex Wrapper (https://github.com/Norconex/language-detector) <br />
 *  for the really amazing "language-detection" library from Nakatani Shuyo (https://code.google.com/p/language-detection). <br /> <br />
 *  Why did i use the Wrapper? The language-detection library is really great, but it's not perfectly clear how to configure at first glance. <br />
 *  So i used the Norconex Wrapper, which already did this job for me, which saved me some time.
 * </p>
 */
public class LanguageDetector implements ILanguageDetector {

  public final static String CouldNotDetectLanguage = "CouldNotDetectLanguage";

  private final static Logger log = LoggerFactory.getLogger(LanguageDetector.class);


  protected com.norconex.language.detector.LanguageDetector detector = null;


  public LanguageDetector() {
    try {
      detector = new com.norconex.language.detector.LanguageDetector(true);
    } catch(Exception ex) {
      log.error("Could not create LanguageDetector", ex);
      throw ex;
    }
  }

  public String detectLanguageOfText(String text) {
    try {
      DetectedLanguages detectedLanguages = detector.detect(text);
      for(DetectedLanguage probableLanguage : detectedLanguages) {
        if(probableLanguage.getProbability() > 0.75) {
          return probableLanguage.getTag();
        }
      }
    } catch(Exception ex) {
      log.error("Could not detect language for text " + text, ex);
    }

    return CouldNotDetectLanguage;
  }

}

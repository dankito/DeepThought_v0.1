package net.dankito.deepthought.language;

import com.norconex.language.detector.DetectedLanguage;
import com.norconex.language.detector.DetectedLanguages;

import net.dankito.deepthought.data.model.enums.Language;

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

  public Language detectLanguageOfText(String text) {
    String languageTag = getLanguageTagOfText(text);

    if(languageTag != null)
      return getLanguageFromLanguageTag(languageTag);
    return CouldNotDetectLanguage;
  }

  public String getLanguageTagOfText(String text) {
    try {
      DetectedLanguages detectedLanguages = detector.detect(text);
      for(DetectedLanguage probableLanguage : detectedLanguages) {
        if(probableLanguage.getProbability() > 0.75) {
          return probableLanguage.getTag();
        }
      }
    } catch(Exception ex) {
//      log.error("Could not detect language for text " + text, ex);
    }

    return null;
  }

  protected Language getLanguageFromLanguageTag(String languageTag) {
    return Language.findByLanguageKey(languageTag);
  }

}

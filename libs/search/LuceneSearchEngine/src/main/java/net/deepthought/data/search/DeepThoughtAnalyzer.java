package net.deepthought.data.search;

import net.deepthought.Application;
import net.deepthought.language.ILanguageDetector;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.DelegatingAnalyzerWrapper;
import org.apache.lucene.analysis.ar.ArabicAnalyzer;
import org.apache.lucene.analysis.bg.BulgarianAnalyzer;
import org.apache.lucene.analysis.cjk.CJKAnalyzer;
import org.apache.lucene.analysis.cz.CzechAnalyzer;
import org.apache.lucene.analysis.da.DanishAnalyzer;
import org.apache.lucene.analysis.de.GermanAnalyzer;
import org.apache.lucene.analysis.el.GreekAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.es.SpanishAnalyzer;
import org.apache.lucene.analysis.fa.PersianAnalyzer;
import org.apache.lucene.analysis.fi.FinnishAnalyzer;
import org.apache.lucene.analysis.fr.FrenchAnalyzer;
import org.apache.lucene.analysis.hi.HindiAnalyzer;
import org.apache.lucene.analysis.hu.HungarianAnalyzer;
import org.apache.lucene.analysis.id.IndonesianAnalyzer;
import org.apache.lucene.analysis.it.ItalianAnalyzer;
import org.apache.lucene.analysis.nl.DutchAnalyzer;
import org.apache.lucene.analysis.no.NorwegianAnalyzer;
import org.apache.lucene.analysis.pt.PortugueseAnalyzer;
import org.apache.lucene.analysis.ro.RomanianAnalyzer;
import org.apache.lucene.analysis.ru.RussianAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.sv.SwedishAnalyzer;
import org.apache.lucene.analysis.th.ThaiAnalyzer;
import org.apache.lucene.analysis.tr.TurkishAnalyzer;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ganymed on 16/04/15.
 */
public class DeepThoughtAnalyzer extends DelegatingAnalyzerWrapper {

  protected final Analyzer defaultAnalyzer;

  protected final Analyzer longFieldAnalyzer;

  protected final Analyzer defaultLanguageDependentFieldAnalyzer;
  protected Analyzer currentLanguageAnalyzer = null;

  protected Map<String, Analyzer> cachedLanguageAnalyzers = new HashMap<>();


  public DeepThoughtAnalyzer() {
    super(PER_FIELD_REUSE_STRATEGY);
    defaultAnalyzer = new StandardAnalyzer();
    longFieldAnalyzer = new StandardAnalyzer();
    defaultLanguageDependentFieldAnalyzer = new StandardAnalyzer();
    currentLanguageAnalyzer = defaultLanguageDependentFieldAnalyzer;
  }

  @Override
  protected Analyzer getWrappedAnalyzer(String fieldName) {
    // only Entry's Content and Abstract are analyzed based on current language (so that stop words get removed and words are being stemmed)
    if(FieldName.Content.equals(fieldName) || FieldName.Abstract.equals(fieldName))
      return currentLanguageAnalyzer;
    else if(FieldName.TagsIds.equals(fieldName))
      return longFieldAnalyzer;
    return defaultAnalyzer;
  }

  protected Analyzer getAnalyzerForTextLanguage(String text) {
    String language = Application.getLanguageDetector().detectLanguageOfText(text);
    if(language == ILanguageDetector.CouldNotDetectLanguage)
      return defaultAnalyzer;
    return getAnalyzerForLanguage(language);
  }

  protected Analyzer getAnalyzerForLanguage(String language) {
    if(cachedLanguageAnalyzers.containsKey(language) == false)
      cachedLanguageAnalyzers.put(language, createAnalyzerForLanguage(language));

    return cachedLanguageAnalyzers.get(language);
  }

  protected Analyzer createAnalyzerForLanguage(String language) {
    switch(language) {
      case "en":
        return new EnglishAnalyzer();
      case "es":
        return new SpanishAnalyzer();
      case "fr":
        return new FrenchAnalyzer();
      case "it":
        return new ItalianAnalyzer();
      case "de":
        return new GermanAnalyzer();
      case "ar":
        return new ArabicAnalyzer();
      case "bg":
        return new BulgarianAnalyzer();
      case "br": // Brazil
        return new PortugueseAnalyzer();
      case "cs":
        return new CzechAnalyzer();
      case "da":
        return new DanishAnalyzer();
      case "el":
        return new GreekAnalyzer();
      case "fa":
        return new PersianAnalyzer();
      case "fi":
        return new FinnishAnalyzer();
      case "hi":
        return new HindiAnalyzer();
      case "hu":
        return new HungarianAnalyzer();
      case "id":
        return new IndonesianAnalyzer();
      case "ja": // Japanese
        return new CJKAnalyzer();
      case "ko": // Korean
        return new CJKAnalyzer();
      case "nl":
        return new DutchAnalyzer();
      case "no":
        return new NorwegianAnalyzer();
      case "pt":
        return new PortugueseAnalyzer();
      case "ro":
        return new RomanianAnalyzer();
      case "ru":
        return new RussianAnalyzer();
      case "sv":
        return new SwedishAnalyzer();
      case "th":
        return new ThaiAnalyzer();
      case "tr":
        return new TurkishAnalyzer();
      case "zh-cn": // Simplified Chinese
        return new CJKAnalyzer();
      case "zh-tw": // Traditional Chinese
        return new CJKAnalyzer();
    }

    return defaultAnalyzer;
  }


  public Analyzer getDefaultAnalyzer() {
    return defaultAnalyzer;
  }

  public Analyzer getDefaultLanguageDependentFieldAnalyzer() {
    return defaultLanguageDependentFieldAnalyzer;
  }

  public Analyzer getCurrentLanguageAnalyzer() {
    return currentLanguageAnalyzer;
  }

  public void setCurrentLanguageAnalyzer(Analyzer currentLanguageAnalyzer) {
    this.currentLanguageAnalyzer = currentLanguageAnalyzer;
  }

  public void setNextLanguageDependentFieldValueToBeAnalyzed(String languageDependentFieldValue) {
    setCurrentLanguageAnalyzer(getAnalyzerForTextLanguage(languageDependentFieldValue));
  }

}

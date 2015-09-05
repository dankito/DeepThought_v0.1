package net.deepthought.data.search;

import net.deepthought.Application;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.enums.Language;
import net.deepthought.language.ILanguageDetector;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.AnalyzerWrapper;
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
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.util.Version;

import java.util.HashMap;
import java.util.Map;

/**
* Created by ganymed on 16/04/15.
*/
public class DeepThoughtAnalyzer extends AnalyzerWrapper {

  protected final Analyzer defaultAnalyzer;

  protected final Analyzer longFieldAnalyzer;

  protected final Analyzer defaultLanguageDependentFieldAnalyzer;
  protected Analyzer currentLanguageAnalyzer = null;

  protected Map<Language, Analyzer> cachedLanguageAnalyzers = new HashMap<>();


  public DeepThoughtAnalyzer() {
    super(PER_FIELD_REUSE_STRATEGY);
    defaultAnalyzer = new StandardAnalyzer(Version.LUCENE_47);
    longFieldAnalyzer = new StandardAnalyzer(Version.LUCENE_47);
    defaultLanguageDependentFieldAnalyzer = new StandardAnalyzer(Version.LUCENE_47);
    currentLanguageAnalyzer = defaultLanguageDependentFieldAnalyzer;
  }

  @Override
  protected Analyzer getWrappedAnalyzer(String fieldName) {
    // only Entry's EntryContent and EntryAbstract are analyzed based on current language (so that stop words get removed and words are being stemmed)
    // this is a bit problematic: as we only be the field name, but not the field value, we cannot determine the language of Abstract and Content directly
    if(FieldName.EntryContent.equals(fieldName) || FieldName.EntryAbstract.equals(fieldName))
      return currentLanguageAnalyzer;
    else if(FieldName.EntryTagsIds.equals(fieldName))
      return longFieldAnalyzer;
    return defaultAnalyzer;
  }

  protected Analyzer getAnalyzerForTextLanguage(String text) {
    Language language = Application.getLanguageDetector().detectLanguageOfText(text);
    if(language == ILanguageDetector.CouldNotDetectLanguage)
      return defaultAnalyzer;
    return getAnalyzerForLanguage(language);
  }

  protected Analyzer getAnalyzerForLanguage(Language language) {
    if(cachedLanguageAnalyzers.containsKey(language) == false)
      cachedLanguageAnalyzers.put(language, createAnalyzerForLanguage(language));

    return cachedLanguageAnalyzers.get(language);
  }

  protected Analyzer createAnalyzerForLanguage(Language language) {
    // i only need removing stop words, not stemming (as i'm doing PrefixQuery anyway) and also not normalizing letters like ä -> ae, è -> e, ... (without indexing them the can't be found later on)
    return new StandardAnalyzer(Version.LUCENE_47, getLanguageStopWords(language));
  }

  /*
  protected Analyzer createAnalyzerForLanguage(Language language) {
    if(language == null || language == ILanguageDetector.CouldNotDetectLanguage)
      return defaultAnalyzer;

    switch(language.getLanguageKey()) {
      case "en":
        return new EnglishAnalyzer(Version.LUCENE_47);
      case "es":
        return new SpanishAnalyzer(Version.LUCENE_47);
      case "fr":
        return new FrenchAnalyzer(Version.LUCENE_47);
      case "it":
        return new ItalianAnalyzer(Version.LUCENE_47);
      case "de":
        return new GermanAnalyzer(Version.LUCENE_47);
      case "ar":
        return new ArabicAnalyzer(Version.LUCENE_47);
      case "bg":
        return new BulgarianAnalyzer(Version.LUCENE_47);
      case "br": // Brazil
        return new PortugueseAnalyzer(Version.LUCENE_47);
      case "cs":
        return new CzechAnalyzer(Version.LUCENE_47);
      case "da":
        return new DanishAnalyzer(Version.LUCENE_47);
      case "el":
        return new GreekAnalyzer(Version.LUCENE_47);
      case "fa":
        return new PersianAnalyzer(Version.LUCENE_47);
      case "fi":
        return new FinnishAnalyzer(Version.LUCENE_47);
      case "hi":
        return new HindiAnalyzer(Version.LUCENE_47);
      case "hu":
        return new HungarianAnalyzer(Version.LUCENE_47);
      case "id":
        return new IndonesianAnalyzer(Version.LUCENE_47);
      case "ja": // Japanese
        return new CJKAnalyzer(Version.LUCENE_47);
      case "ko": // Korean
        return new CJKAnalyzer(Version.LUCENE_47);
      case "nl":
        return new DutchAnalyzer(Version.LUCENE_47);
      case "no":
        return new NorwegianAnalyzer(Version.LUCENE_47);
      case "pt":
        return new PortugueseAnalyzer(Version.LUCENE_47);
      case "ro":
        return new RomanianAnalyzer(Version.LUCENE_47);
      case "ru":
        return new RussianAnalyzer(Version.LUCENE_47);
      case "sv":
        return new SwedishAnalyzer(Version.LUCENE_47);
      case "th":
        return new ThaiAnalyzer(Version.LUCENE_47);
      case "tr":
        return new TurkishAnalyzer(Version.LUCENE_47);
      case "zh-cn": // Simplified Chinese
        return new CJKAnalyzer(Version.LUCENE_47);
      case "zh-tw": // Traditional Chinese
        return new CJKAnalyzer(Version.LUCENE_47);
    }

    return defaultAnalyzer;
  }
  */


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

  /**
   * <p>
   *   This is a bit problematic: as we only be the field name, but not the field value, we cannot determine the language of Abstract and Content directly
   *   So before an Entry's Abstract and Content can be analyzed, you have to tell DeepThoughtAnalyzer explicitly which Entry is going to be indexed next.
   * </p>
   * @param nextEntryToBeAnalyzed
   */
  public void setNextEntryToBeAnalyzed(Entry nextEntryToBeAnalyzed) {
    setCurrentLanguageAnalyzer(getAnalyzerForTextLanguage(nextEntryToBeAnalyzed.getAbstractAsPlainText() + " " + nextEntryToBeAnalyzed.getContentAsPlainText()));
  }



  protected final static CharArraySet DefaultStopWords = new CharArraySet(Version.LUCENE_47, 0, true);

  public static CharArraySet getLanguageStopWords(Language language) {
    if(language == null || language == ILanguageDetector.CouldNotDetectLanguage)
      return DefaultStopWords;

    switch(language.getLanguageKey()) {
      case "en":
        return EnglishAnalyzer.getDefaultStopSet();
      case "es":
        return SpanishAnalyzer.getDefaultStopSet();
      case "fr":
        return FrenchAnalyzer.getDefaultStopSet();
      case "it":
        return ItalianAnalyzer.getDefaultStopSet();
      case "de":
        CharArraySet stopWords = GermanAnalyzer.getDefaultStopSet();
        stopWords.add("dass"); // can't believe it, dass is missing in Lucene's German Stop Wort List
        return stopWords;
      case "ar":
        return ArabicAnalyzer.getDefaultStopSet();
      case "bg":
        return BulgarianAnalyzer.getDefaultStopSet();
      case "br": // Brazil
        return PortugueseAnalyzer.getDefaultStopSet();
      case "cs":
        return CzechAnalyzer.getDefaultStopSet();
      case "da":
        return DanishAnalyzer.getDefaultStopSet();
      case "el":
        return GreekAnalyzer.getDefaultStopSet();
      case "fa":
        return PersianAnalyzer.getDefaultStopSet();
      case "fi":
        return FinnishAnalyzer.getDefaultStopSet();
      case "hi":
        return HindiAnalyzer.getDefaultStopSet();
      case "hu":
        return HungarianAnalyzer.getDefaultStopSet();
      case "id":
        return IndonesianAnalyzer.getDefaultStopSet();
      case "ja": // Japanese
        return CJKAnalyzer.getDefaultStopSet();
      case "ko": // Korean
        return CJKAnalyzer.getDefaultStopSet();
      case "nl":
        return DutchAnalyzer.getDefaultStopSet();
      case "no":
        return NorwegianAnalyzer.getDefaultStopSet();
      case "pt":
        return PortugueseAnalyzer.getDefaultStopSet();
      case "ro":
        return RomanianAnalyzer.getDefaultStopSet();
      case "ru":
        return RussianAnalyzer.getDefaultStopSet();
      case "sv":
        return SwedishAnalyzer.getDefaultStopSet();
      case "th":
        return ThaiAnalyzer.getDefaultStopSet();
      case "tr":
        return TurkishAnalyzer.getDefaultStopSet();
      case "zh-cn": // Simplified Chinese
        return CJKAnalyzer.getDefaultStopSet();
      case "zh-tw": // Traditional Chinese
        return CJKAnalyzer.getDefaultStopSet();
    }

    return DefaultStopWords;
  }

}

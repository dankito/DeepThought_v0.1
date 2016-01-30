package net.deepthought.data.model.ui;

import net.deepthought.controls.ICleanUp;
import net.deepthought.data.model.DeepThought;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.Tag;
import net.deepthought.data.model.enums.ApplicationLanguage;
import net.deepthought.util.localization.LanguageChangedListener;
import net.deepthought.util.localization.Localization;

import java.util.Collection;
import java.util.HashSet;

/**
 * Created by ganymed on 09/12/14.
 */
public abstract class SystemTag extends Tag implements ICleanUp {

  protected Collection<Entry> filteredEntries = new HashSet<>();

  // TODO: in this way name doesn't get translated when Application Language changes
  public SystemTag(DeepThought deepThought) {
    this.deepThought = deepThought;
    this.name = getSystemTagName();

    Localization.addLanguageChangedListener(languageChangedListener);
  }

  protected abstract String getSystemTagName();

  @Override
  public void cleanUp() {
    Localization.removeLanguageChangedListener(languageChangedListener);
  }


  @Override
  public boolean hasEntries() {
    return filteredEntries.size() > 0;
  }

  @Override
  public Collection<Entry> getEntries() {
    return filteredEntries;
  }

//  @Override
//  protected boolean addEntry(Entry entry) {
//    boolean result = deepThought.addEntry((Entry)entry);
//    callEntityAddedListeners(entries, entry);
//    return result;
//  }
//
//  @Override
//  protected boolean removeEntry(Entry entry) {
//    boolean result = deepThought.removeEntry((Entry)entry);
//    callEntityRemovedListeners(entries, entry);
//    return result;
//  }

  @Override
  public String getTextRepresentation() {
    return name + " (" + filteredEntries.size() + ")";
  }


  protected LanguageChangedListener languageChangedListener = new LanguageChangedListener() {
    @Override
    public void languageChanged(ApplicationLanguage newLanguage) {
      setName(getSystemTagName());
    }
  };

}

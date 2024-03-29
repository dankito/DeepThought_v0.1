package net.dankito.deepthought.data.model.ui;

import net.dankito.deepthought.controls.ICleanUp;
import net.dankito.deepthought.data.model.DeepThought;
import net.dankito.deepthought.data.model.Entry;
import net.dankito.deepthought.data.model.Tag;
import net.dankito.deepthought.data.model.enums.ApplicationLanguage;
import net.dankito.deepthought.util.localization.LanguageChangedListener;
import net.dankito.deepthought.util.localization.Localization;

import java.util.Collection;
import java.util.HashSet;

/**
 * Created by ganymed on 09/12/14.
 */
public abstract class SystemTag extends Tag implements ICleanUp {

  protected Collection<Entry> filteredEntries = new HashSet<>();

  public SystemTag(DeepThought deepThought) {
    this.deepThought = deepThought;
    this.name = getSystemTagName();

    Localization.addLanguageChangedListener(languageChangedListener);
  }

  protected abstract String getSystemTagName();

  @Override
  public void cleanUp() { // TODO: not called right now
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

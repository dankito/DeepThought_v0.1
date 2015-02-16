package net.deepthought.model;

import net.deepthought.data.model.DeepThought;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.Tag;

import java.util.Collection;
import java.util.HashSet;

/**
 * Created by ganymed on 09/12/14.
 */
public abstract class SystemTag extends Tag {

  protected Collection<Entry> filteredEntries = new HashSet<>();

  // TODO: in this way name doesn't get translated when Application Language changes
  public SystemTag(DeepThought deepThought, String name) {
    this.deepThought = deepThought;
    this.name = name;
  }

//  protected abstract Set<Entry> filterDeepThoughtEntries();


  @Override
  public boolean hasEntries() {
    return filteredEntries.size() > 0;
  }

  @Override
  public Collection<Entry> getEntries() {
    return filteredEntries;
  }

  @Override
  protected boolean addEntry(Entry entry) {
    boolean result = deepThought.addEntry((Entry)entry);
    callEntityAddedListeners(entries, entry);
    return result;
  }

  @Override
  protected boolean removeEntry(Entry entry) {
    boolean result = deepThought.removeEntry((Entry)entry);
    callEntityRemovedListeners(entries, entry);
    return result;
  }

}

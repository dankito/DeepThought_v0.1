package net.deepthought.data.search.specific;

import net.deepthought.data.model.Entry;
import net.deepthought.data.model.Tag;

import java.util.Collection;
import java.util.Set;

/**
 * Created by ganymed on 27/07/15.
 */
public class FindAllEntriesHavingTheseTagsResult {

  protected Collection<Entry> entriesHavingFilteredTags;

  protected Set<Tag> tagsOnEntriesContainingFilteredTags;

  public FindAllEntriesHavingTheseTagsResult(Collection<Entry> entriesHavingFilteredTags, Set<Tag> tagsOnEntriesContainingFilteredTags) {
    this.entriesHavingFilteredTags = entriesHavingFilteredTags;
    this.tagsOnEntriesContainingFilteredTags = tagsOnEntriesContainingFilteredTags;
  }

  public Collection<Entry> getEntriesHavingFilteredTags() {
    return entriesHavingFilteredTags;
  }

  public Set<Tag> getTagsOnEntriesContainingFilteredTags() {
    return tagsOnEntriesContainingFilteredTags;
  }
}

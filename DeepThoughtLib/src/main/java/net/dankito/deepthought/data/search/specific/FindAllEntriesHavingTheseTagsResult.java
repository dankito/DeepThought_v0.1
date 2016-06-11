package net.dankito.deepthought.data.search.specific;

import net.dankito.deepthought.data.model.Entry;
import net.dankito.deepthought.data.model.Tag;
import net.dankito.deepthought.data.persistence.CombinedLazyLoadingList;

import java.util.Collection;
import java.util.List;

/**
 * Created by ganymed on 27/07/15.
 */
public class FindAllEntriesHavingTheseTagsResult {

  protected Collection<Entry> entriesHavingFilteredTags;

  protected Collection<Tag> tagsOnEntriesContainingFilteredTags;
  protected List<Tag> tagsOnEntriesContainingFilteredTagsList = null;


  public FindAllEntriesHavingTheseTagsResult(Collection<Entry> entriesHavingFilteredTags, Collection<Tag> tagsOnEntriesContainingFilteredTags) {
    this.entriesHavingFilteredTags = entriesHavingFilteredTags;
    this.tagsOnEntriesContainingFilteredTags = tagsOnEntriesContainingFilteredTags;
  }

  public Collection<Entry> getEntriesHavingFilteredTags() {
    return entriesHavingFilteredTags;
  }

  public int getTagsOnEntriesContainingFilteredTagsCount() {
    return tagsOnEntriesContainingFilteredTags.size();
  }

  public Collection<Tag> getTagsOnEntriesContainingFilteredTags() {
    return tagsOnEntriesContainingFilteredTags;
  }

  public Tag getTagsOnEntriesContainingFilteredTagsAt(int index) {
    if(index < 0 || index >= getTagsOnEntriesContainingFilteredTagsCount())
      return null;

    if(tagsOnEntriesContainingFilteredTagsList == null) {
      if(tagsOnEntriesContainingFilteredTags instanceof List)
        tagsOnEntriesContainingFilteredTagsList = (List<Tag>)tagsOnEntriesContainingFilteredTags;
      else
        tagsOnEntriesContainingFilteredTagsList = new CombinedLazyLoadingList<Tag>(tagsOnEntriesContainingFilteredTags);
    }

    return tagsOnEntriesContainingFilteredTagsList.get(index);
  }
}

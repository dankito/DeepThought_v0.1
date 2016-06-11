package net.dankito.deepthought.controls.entries;

import net.dankito.deepthought.data.model.Entry;
import net.dankito.deepthought.data.model.Tag;

/**
 * Created by ganymed on 28/11/14.
 */
public class EntryTagsTableCell extends EntryTableCell {

  @Override
  protected String getTextRepresentationForCell(Entry entry) {
    if(entry != null)
      return entry.getTagsPreview();
    else
      return "";
  }

  @Override
  protected void tagHasBeenAdded(Entry entry, Tag tag) {
    super.tagHasBeenAdded(entry, tag);
    entryUpdated(entry);
  }

  @Override
  protected void tagHasBeenRemoved(Entry entry, Tag tag) {
    super.tagHasBeenRemoved(entry, tag);
    entryUpdated(entry);
  }
}


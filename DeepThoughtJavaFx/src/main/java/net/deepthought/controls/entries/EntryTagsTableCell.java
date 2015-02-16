package net.deepthought.controls.entries;

import net.deepthought.data.model.Entry;
import net.deepthought.data.model.Tag;

/**
 * Created by ganymed on 28/11/14.
 */
public class EntryTagsTableCell extends EntryTableCell {

  @Override
  protected String getTextRepresentationForCell(Entry entry) {
    if(entry != null)
      return entry.getTagsStringRepresentation();
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


package net.deepthought.controls.entries;

import net.deepthought.data.model.Entry;

/**
 * Created by ganymed on 28/11/14.
 */
public class EntryModifiedTableCell extends EntryTableCell {

  @Override
  protected String getTextRepresentationForCell(Entry entry) {
    if(entry != null)
      return EntryCreatedTableCell.DateFormatter.format(entry.getModifiedOn());
    else
      return "";
  }

}


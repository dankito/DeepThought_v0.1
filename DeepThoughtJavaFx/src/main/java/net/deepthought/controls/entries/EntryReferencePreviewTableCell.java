package net.deepthought.controls.entries;

import net.deepthought.data.model.Entry;

/**
 * Created by ganymed on 28/11/14.
 */
public class EntryReferencePreviewTableCell extends EntryTableCell {

  @Override
  protected String getTextRepresentationForCell(Entry entry) {
    if(entry != null)
      return entry.getReferencePreview();
    else
      return "";
  }

}


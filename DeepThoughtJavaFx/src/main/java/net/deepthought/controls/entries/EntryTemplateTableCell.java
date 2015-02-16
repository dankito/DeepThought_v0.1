package net.deepthought.controls.entries;

import net.deepthought.data.model.Entry;

/**
 * Created by ganymed on 28/11/14.
 */
public class EntryTemplateTableCell extends EntryTableCell {


  public EntryTemplateTableCell() {
    setWrapText(false);
  }

  @Override
  protected String getTextRepresentationForCell(Entry entry) {
    if(entry != null)
      return entry.getTemplate().getName();
    else
      return "";
  }

}


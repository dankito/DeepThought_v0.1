package net.dankito.deepthought.controls.entries;

import net.dankito.deepthought.data.model.Entry;

import java.text.SimpleDateFormat;

/**
 * Created by ganymed on 28/11/14.
 */
public class EntryCreatedTableCell extends EntryTableCell {

  public final static SimpleDateFormat DateFormatter = new SimpleDateFormat("dd.MM.YYYY HH:mm:ss");


  @Override
  protected String getTextRepresentationForCell(Entry entry) {
    if(entry != null)
      return DateFormatter.format(entry.getCreatedOn());
    else
      return "";
  }

}


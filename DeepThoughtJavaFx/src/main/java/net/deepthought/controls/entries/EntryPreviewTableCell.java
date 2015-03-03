package net.deepthought.controls.entries;

import net.deepthought.data.model.Entry;

/**
 * Created by ganymed on 28/11/14.
 */
public class EntryPreviewTableCell extends EntryTableCell {

  @Override
  protected String getTextRepresentationForCell(Entry entry) {
    if(entry != null)
      return entry.getPreview();
    else
      return "";
  }

  // TODO: discuss how preview should look like for different templates
  protected String createEntryPreview(Entry entry) {
    String preview = "";

//    for(PersonRole role : entry.getPersonRoles()) {
//      for(Person person : entry.getPersonsForRole(role))
//        preview += person.getLastName(); // TODO: improve as in this way multiple Persons get put together; Only take one Person, preferably the Author
//    }

    if(entry.getTitle() != null && entry.getTitle().isEmpty() == false) {
      if(preview.length() > 0)
        preview += " - ";
      preview += entry.getTitle();
    }

    if(entry.getContent() != null && entry.getContent().isEmpty() == false) {
      if(preview.length() > 0)
        preview += " - ";
      preview += entry.getContent();
    }

    return preview;
  }

}


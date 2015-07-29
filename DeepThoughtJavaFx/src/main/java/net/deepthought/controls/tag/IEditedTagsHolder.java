package net.deepthought.controls.tag;

import net.deepthought.data.model.Tag;

import javafx.collections.ObservableSet;

/**
 * Created by ganymed on 29/07/15.
 */
public interface IEditedTagsHolder {

  public ObservableSet<Tag> getEditedTags();

  public void addTagToEntry(Tag tag);

  public void removeTagFromEntry(Tag tag);

  public boolean containsEditedTag(Tag tag);
}

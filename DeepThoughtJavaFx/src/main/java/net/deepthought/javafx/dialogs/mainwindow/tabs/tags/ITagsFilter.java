package net.deepthought.javafx.dialogs.mainwindow.tabs.tags;

import net.deepthought.controls.tag.IDisplayedTagsChangedListener;
import net.deepthought.data.model.Tag;
import net.deepthought.data.search.specific.TagsSearchResults;

import javafx.collections.ObservableSet;

/**
 * Created by ganymed on 03/01/16.
 */
public interface ITagsFilter {

  ObservableSet<Tag> getTagsFilter();

  TagsSearchResults getLastTagsSearchResults();

  void setTagFilterState(Tag tag, Boolean filterTag);

  boolean addDisplayedTagsChangedListener(IDisplayedTagsChangedListener listener);

  boolean removeDisplayedTagsChangedListener(IDisplayedTagsChangedListener listener);

}

package net.dankito.deepthought.javafx.dialogs.mainwindow.tabs.tags;

import net.dankito.deepthought.data.model.Tag;
import net.dankito.deepthought.data.search.specific.TagsSearchResults;

import javafx.collections.ObservableSet;

/**
 * Created by ganymed on 03/01/16.
 */
public interface ITagsFilter {

  void searchTags();

  void setTagFilterState(Tag tag, Boolean filterTag);

  void toggleCurrentTagsTagsFilter();

  void clearTagFilter();

  ObservableSet<Tag> getTagsFilter();

  TagsSearchResults getLastTagsSearchResults();

  boolean addDisplayedTagsChangedListener(net.dankito.deepthought.controls.tag.IDisplayedTagsChangedListener listener);

  boolean removeDisplayedTagsChangedListener(net.dankito.deepthought.controls.tag.IDisplayedTagsChangedListener listener);

}

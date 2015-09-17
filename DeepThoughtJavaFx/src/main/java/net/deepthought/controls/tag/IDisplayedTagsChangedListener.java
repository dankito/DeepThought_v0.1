package net.deepthought.controls.tag;

import net.deepthought.data.search.specific.FilterTagsSearchResults;

/**
 * Created by ganymed on 27/07/15.
 */
public interface IDisplayedTagsChangedListener {

  public void filteredTagsChanged(FilterTagsSearchResults results);

}

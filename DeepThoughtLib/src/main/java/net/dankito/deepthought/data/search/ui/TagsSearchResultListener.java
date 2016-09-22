package net.dankito.deepthought.data.search.ui;

import net.dankito.deepthought.data.model.Tag;

import java.util.List;

/**
 * Created by ganymed on 22/09/16.
 */
public interface TagsSearchResultListener {

  void completed(List<Tag> searchResult);

}

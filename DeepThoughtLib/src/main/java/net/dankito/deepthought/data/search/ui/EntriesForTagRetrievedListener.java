package net.dankito.deepthought.data.search.ui;

import net.dankito.deepthought.data.model.Entry;

import java.util.List;

/**
 * Created by ganymed on 16/10/16.
 */
public interface EntriesForTagRetrievedListener {

  void retrievedEntriesForTag(List<Entry> entries);

}

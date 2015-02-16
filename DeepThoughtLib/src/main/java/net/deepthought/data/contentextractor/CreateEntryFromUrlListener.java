package net.deepthought.data.contentextractor;

import net.deepthought.data.model.Entry;

import java.net.URL;

/**
 * Created by ganymed on 15/01/15.
 */
public interface CreateEntryFromUrlListener extends ExtractContentListener {


  void EntryCreated(URL url, Entry entry);

}

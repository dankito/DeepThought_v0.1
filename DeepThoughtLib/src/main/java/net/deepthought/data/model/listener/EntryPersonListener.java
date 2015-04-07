package net.deepthought.data.model.listener;

import net.deepthought.data.model.Entry;
import net.deepthought.data.model.Person;

/**
 * Created by ganymed on 01/02/15.
 */
public interface EntryPersonListener {

  public void personAdded(Entry entry, Person addedPerson);

  public void personRemoved(Entry entry, Person removedPerson);

}

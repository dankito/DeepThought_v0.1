package net.deepthought.data.model.listener;

import net.deepthought.data.model.Entry;
import net.deepthought.data.model.Person;
import net.deepthought.data.model.enums.PersonRole;

/**
 * Created by ganymed on 01/02/15.
 */
public interface EntryPersonListener {

  public void personAdded(Entry entry, PersonRole role, Person addedPerson);

  public void personRemoved(Entry entry, PersonRole role, Person removedPerson);

}

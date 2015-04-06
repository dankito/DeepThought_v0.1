package net.deepthought.data.search.helper;

import net.deepthought.data.model.Entry;

/**
 * Created by ganymed on 02/04/15.
 */
public class IdSettableEntry extends Entry {

  public IdSettableEntry(int id) {
    super();
    this.id = (long)id;
  }

}

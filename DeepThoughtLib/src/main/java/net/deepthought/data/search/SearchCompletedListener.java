package net.deepthought.data.search;

import net.deepthought.data.persistence.db.BaseEntity;

import java.util.Collection;

/**
 * Created by ganymed on 12/04/15.
 */
public interface SearchCompletedListener<T extends BaseEntity> {

  public void completed(Collection<T> results);

}

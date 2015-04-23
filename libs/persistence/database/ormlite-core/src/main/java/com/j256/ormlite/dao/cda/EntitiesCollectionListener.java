package com.j256.ormlite.dao.cda;

import java.util.List;

/**
 * Created by ganymed on 13/10/14.
 */
public interface EntitiesCollectionListener<T> {

  public void itemAdded(T item);
  public void itemRemoved(T item);

  public void entityMapped(T entity);

  public void cacheCleared(List<T> cachedEntities);

}

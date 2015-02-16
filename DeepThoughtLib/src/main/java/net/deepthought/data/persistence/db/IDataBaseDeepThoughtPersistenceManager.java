package net.deepthought.data.persistence.db;

import net.deepthought.data.persistence.IDeepThoughtPersistenceManager;

import java.util.List;

/**
 * Created by ganymed on 10/11/14.
 */
public interface IDataBaseDeepThoughtPersistenceManager extends IDeepThoughtPersistenceManager {

  public<T> List<T> doNativeQuery(String query);
  public int doNativeExecute(String statement);

  public void close();

}

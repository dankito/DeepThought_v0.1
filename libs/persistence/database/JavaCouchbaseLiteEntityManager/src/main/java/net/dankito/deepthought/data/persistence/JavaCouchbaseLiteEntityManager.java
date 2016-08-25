package net.dankito.deepthought.data.persistence;

import com.couchbase.lite.Context;
import com.couchbase.lite.JavaContext;

/**
 * Created by ganymed on 25/08/16.
 */
public class JavaCouchbaseLiteEntityManager extends CouchbaseLiteEntityManagerBase {


  public JavaCouchbaseLiteEntityManager(EntityManagerConfiguration configuration) throws Exception {
    super(configuration);
  }

  @Override
  protected Context createContext(EntityManagerConfiguration configuration) {
    return new JavaContext(configuration.getDataFolder());
  }

}

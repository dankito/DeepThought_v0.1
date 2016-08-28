package net.dankito.deepthought.data.persistence;

import com.couchbase.lite.Context;
import com.couchbase.lite.android.AndroidContext;

/**
 * Created by ganymed on 28/08/16.
 */
public class AndroidCouchbaseLiteEntityManager extends CouchbaseLiteEntityManagerBase {

  protected android.content.Context androidContext;


  public AndroidCouchbaseLiteEntityManager(android.content.Context context, EntityManagerConfiguration configuration) throws Exception {
    super(new AndroidContext(context), configuration);
    this.androidContext = context;

  }


  @Override
  protected String adjustDatabasePath(Context context, EntityManagerConfiguration configuration) {
    return configuration.getDataCollectionPersistencePath();
  }

}

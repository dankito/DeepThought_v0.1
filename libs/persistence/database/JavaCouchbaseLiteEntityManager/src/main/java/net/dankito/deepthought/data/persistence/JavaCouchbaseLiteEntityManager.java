package net.dankito.deepthought.data.persistence;

import com.couchbase.lite.Context;
import com.couchbase.lite.JavaContext;

import java.io.File;

/**
 * Created by ganymed on 25/08/16.
 */
public class JavaCouchbaseLiteEntityManager extends CouchbaseLiteEntityManagerBase {


  public JavaCouchbaseLiteEntityManager(EntityManagerConfiguration configuration) throws Exception {
    super(new JavaContext(configuration.getDataFolder()), configuration);
  }


  @Override
  protected String adjustDatabasePath(Context context, EntityManagerConfiguration configuration) {
    // TODO: implement this in a better way as this uses implementation internal details
    return new File(context.getFilesDir(), configuration.getDataCollectionFileName() + ".cblite2").getAbsolutePath();
  }

}

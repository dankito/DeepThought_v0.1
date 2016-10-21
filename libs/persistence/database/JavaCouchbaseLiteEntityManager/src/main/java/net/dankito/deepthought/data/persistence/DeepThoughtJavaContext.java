package net.dankito.deepthought.data.persistence;

import com.couchbase.lite.JavaContext;

import java.io.File;

/**
 * Created by ganymed on 21/10/16.
 */
public class DeepThoughtJavaContext extends JavaContext {

  protected String databaseDirectory;


  public DeepThoughtJavaContext() {
    super();
  }

  public DeepThoughtJavaContext(String databaseDirectory) {
    super(databaseDirectory);
    this.databaseDirectory = databaseDirectory;
  }


  @Override
  public File getFilesDir() {
    if(databaseDirectory == null) {
      return super.getFilesDir();
    }

    File filesDir = new File(databaseDirectory);
    if(filesDir.isAbsolute()) {
      return filesDir;
    }
    else {
      return new File(getWorkingDirectory(), databaseDirectory);
    }
  }

  protected String getWorkingDirectory() {
    return System.getProperty("user.dir");
  }

}

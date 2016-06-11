package net.dankito.deepthought.data;

import android.os.Environment;

import net.dankito.deepthought.data.DefaultDataManager;
import net.dankito.deepthought.data.persistence.IEntityManager;

import java.io.File;

/**
 * Created by ganymed on 11/01/15.
 */
public class AndroidDataManager extends DefaultDataManager {

  public AndroidDataManager(IEntityManager entityManager) {
    super(entityManager);
    determineDataFolder();
  }


  protected void determineDataFolder() {
    if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
        File folder = Environment.getExternalStorageDirectory();
        folder = new File(folder, "DeepThought");
        if(folder.exists() == false)
          folder.mkdir();
      this.dataFolder = folder.getAbsolutePath() + "/";
    }
    else {
      File dataFolderFile = new File(Environment.getDataDirectory(), "data");
      dataFolderFile = new File(dataFolderFile, "net.deepthought");
      dataFolderFile = new File(dataFolderFile, "data");
      if(dataFolderFile.exists() == false) {
        dataFolderFile.mkdirs();
        dataFolderFile.mkdir();
      }
      this.dataFolder = dataFolderFile.getAbsolutePath() + "/";
    }
  }


}

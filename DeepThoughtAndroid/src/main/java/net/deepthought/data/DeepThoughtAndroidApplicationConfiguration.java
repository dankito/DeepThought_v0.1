package net.deepthought.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

import java.io.File;

/**
 * Created by ganymed on 20/03/15.
 */
public class DeepThoughtAndroidApplicationConfiguration extends ApplicationConfiguration {

  public final static String DataFolderKey = "data.folder";


  protected String dataFolder = null;

  protected SharedPreferences preferences = null;


  public DeepThoughtAndroidApplicationConfiguration(Context context) {
    this.preferences = context.getSharedPreferences("DeepThoughtAndroidApplicationConfiguration", Context.MODE_PRIVATE);

    if(preferences.contains(DataFolderKey))
      this.dataFolder = preferences.getString(DataFolderKey, "");
    else { // data folder not yet set via shared preferences
      setDataFolder(getDefaultDataFolder());
    }
  }

  @Override
  public String getDataFolder() {
    return dataFolder;
  }

  @Override
  public void setDataFolder(String dataFolder) {
    SharedPreferences.Editor editor = preferences.edit();
    editor.putString(DataFolderKey, dataFolder);

    if(editor.commit())
      this.dataFolder = dataFolder;
  }

  @Override
  public int getDataBaseCurrentDataModelVersion() {
    return 0; // not needed on Android, is done by SQliteOpenHelper
  }

  @Override
  public void setDataBaseCurrentDataModelVersion(int newDataModelVersion) {
    // not needed on Android, is done by SQliteOpenHelper
  }


  protected String getDefaultDataFolder() {
    if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
      File folder = Environment.getExternalStorageDirectory();
      folder = new File(folder, "DeepThought");
      if (folder.exists() == false)
        folder.mkdir();
      return folder.getAbsolutePath() + "/";
    }
    else {
      File dataFolderFile = new File(Environment.getDataDirectory(), "data");
      dataFolderFile = new File(dataFolderFile, "net.deepthought");
      dataFolderFile = new File(dataFolderFile, "data");
      if (dataFolderFile.exists() == false) {
        dataFolderFile.mkdirs();
        dataFolderFile.mkdir();
      }
      return dataFolderFile.getAbsolutePath() + "/";
    }
  }

}

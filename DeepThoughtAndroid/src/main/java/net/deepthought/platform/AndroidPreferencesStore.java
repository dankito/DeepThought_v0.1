package net.deepthought.platform;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

import java.io.File;

/**
 * Created by ganymed on 24/08/15.
 */
public class AndroidPreferencesStore extends PreferencesStoreBase {

  protected Context context;

  protected SharedPreferences preferences = null;

  protected String androidDefaultDataFolderPath = getDefaultDataFolder();


  public AndroidPreferencesStore(Context context) {
    this.context = context;
    this.preferences = context.getSharedPreferences("DeepThoughtAndroidApplicationConfiguration", Context.MODE_PRIVATE);
  }


  protected String readValueFromStore(String key, String defaultValue) {
    if(DataFolderKey.equals(key))
      defaultValue = androidDefaultDataFolderPath;

    if(preferences != null)
      return preferences.getString(key, defaultValue);
    return defaultValue;
  }

  protected void saveValueToStore(String key, String value) {
    if(preferences != null) {
      SharedPreferences.Editor editor = preferences.edit();
      editor.putString(key, value);

      editor.commit();
    }
  }

  @Override
  protected boolean doesValueExist(String key) {
    return preferences != null && preferences.contains(key);
  }

  @Override
  protected String getDefaultDataFolder() {
    File dataFolderFile = null;

    if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
      dataFolderFile = Environment.getExternalStorageDirectory();
      dataFolderFile = new File(dataFolderFile, "DeepThought");
      dataFolderFile = new File(dataFolderFile, "data");
    }
    else {
      dataFolderFile = new File(Environment.getDataDirectory(), "data");
//      dataFolderFile = new File(dataFolderFile, "net.deepthought");
//      dataFolderFile = new File(dataFolderFile, "data");
    }

    if (dataFolderFile.exists() == false) {
      dataFolderFile.mkdirs();
      dataFolderFile.mkdir();
    }
    return dataFolderFile.getAbsolutePath() + "/";
  }
}

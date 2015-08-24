package net.deepthought.platform;

/**
 * Created by ganymed on 24/08/15.
 */
public interface IPreferencesStore {
  String getDataFolder();

  void setDataFolder(String dataFolder);

  int getDatabaseDataModelVersion();

  void setDatabaseDataModelVersion(int newDataModelVersion);

  String getDataModel();

  void setDataModel(String dataModel);
}

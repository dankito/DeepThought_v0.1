package net.deepthought.platform;

/**
 * Created by ganymed on 24/08/15.
 */
public interface IPreferencesStore {

  public final static String DefaultDataFolder = "data/";

  public final static int DefaultDatabaseDataModelVersion = 0;


  String getDataFolder();

  void setDataFolder(String dataFolder);

  int getDatabaseDataModelVersion();

  void setDatabaseDataModelVersion(int newDataModelVersion);

}

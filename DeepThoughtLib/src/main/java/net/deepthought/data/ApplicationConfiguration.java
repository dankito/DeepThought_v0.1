package net.deepthought.data;

/**
 * Created by ganymed on 20/03/15.
 */
public abstract class ApplicationConfiguration {

  public abstract String getDataFolder();

  public abstract void setDataFolder(String dataFolder);

  public abstract int getCurrentDataModelVersion();

  public abstract void setCurrentDataModelVersion(int newDataModelVersion);

}

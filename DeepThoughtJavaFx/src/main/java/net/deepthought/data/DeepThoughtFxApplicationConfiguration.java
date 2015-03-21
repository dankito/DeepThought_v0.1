package net.deepthought.data;

/**
 * Created by ganymed on 20/03/15.
 */
public class DeepThoughtFxApplicationConfiguration extends ApplicationConfiguration {


  public DeepThoughtFxApplicationConfiguration() {

  }

  @Override
  public String getDataFolder() {
    return DeepThoughtFxProperties.getDataFolderOrCreateDefaultValuesOnNull();
  }

  @Override
  public void setDataFolder(String dataFolder) {
    DeepThoughtFxProperties.setDataFolder(dataFolder);
  }

  @Override
  public int getCurrentDataModelVersion() {
    return DeepThoughtFxProperties.getDataModelVersion();
  }

  @Override
  public void setCurrentDataModelVersion(int newDataModelVersion) {
    DeepThoughtFxProperties.setDataModelVersion(newDataModelVersion);
  }
}

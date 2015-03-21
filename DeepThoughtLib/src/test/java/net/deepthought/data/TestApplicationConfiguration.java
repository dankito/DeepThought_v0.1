package net.deepthought.data;

/**
 * Created by ganymed on 20/03/15.
 */
public class TestApplicationConfiguration extends ApplicationConfiguration {

  @Override
  public String getDataFolder() {
    return "data/tests/";
  }

  @Override
  public void setDataFolder(String dataFolder) {

  }

  @Override
  public int getCurrentDataModelVersion() {
    return 0;
  }

  @Override
  public void setCurrentDataModelVersion(int newDataModelVersion) {

  }

}

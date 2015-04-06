package net.deepthought.data.search.helper;

import net.deepthought.data.ApplicationConfiguration;

/**
 * Created by ganymed on 02/04/15.
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

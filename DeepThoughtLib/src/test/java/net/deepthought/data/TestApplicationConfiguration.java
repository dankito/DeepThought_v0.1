package net.deepthought.data;

/**
 * Created by ganymed on 20/03/15.
 */
public class TestApplicationConfiguration extends ApplicationConfiguration {

  protected String dataFolder = "data/tests/";

  protected int dataModelVersion = 0;


  public TestApplicationConfiguration() {
    this("data/tests/");
  }

  public TestApplicationConfiguration(String dataFolder) {
    this(dataFolder, 0);
  }

  public TestApplicationConfiguration(String dataFolder, int dataModelVersion) {
    this.dataFolder = dataFolder;
    this.dataModelVersion = dataModelVersion;
  }


  @Override
  public String getDataFolder() {
    return dataFolder;
  }

  @Override
  public void setDataFolder(String dataFolder) {
    this.dataFolder = dataFolder;
  }

  @Override
  public int getCurrentDataModelVersion() {
    return 0;
  }

  @Override
  public void setCurrentDataModelVersion(int newDataModelVersion) {

  }

}

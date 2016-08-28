package net.dankito.deepthought.data.sync;

/**
 * Created by ganymed on 25/11/14.
 */
public class SyncConfig {

  protected String userName; // TODO: replace by User object

  protected String deviceName;

  protected String databaseUrl; // TODO: replace by working directory?
  protected String databaseDriver;
  protected String databaseUserName;
  protected String databasePassword;

  protected boolean isRegistrationNode = false;
  protected String registrationUrl = null;


  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getDeviceName() {
    return deviceName;
  }

  public void setDeviceName(String deviceName) {
    this.deviceName = deviceName;
  }

  public String getDatabaseUrl() {
    return databaseUrl;
  }

  public void setDatabaseUrl(String databaseUrl) {
    this.databaseUrl = databaseUrl;
  }

  public String getDatabaseDriver() {
    return databaseDriver;
  }

  public void setDatabaseDriver(String databaseDriver) {
    this.databaseDriver = databaseDriver;
  }

  public String getDatabaseUserName() {
    return databaseUserName;
  }

  public void setDatabaseUserName(String databaseUserName) {
    this.databaseUserName = databaseUserName;
  }

  public String getDatabasePassword() {
    return databasePassword;
  }

  public void setDatabasePassword(String databasePassword) {
    this.databasePassword = databasePassword;
  }

  public boolean isRegistrationNode() {
    return isRegistrationNode;
  }

  public void setIsRegistrationNode(boolean isRegistrationNode) {
    this.isRegistrationNode = isRegistrationNode;
  }

  public String getRegistrationUrl() {
    return registrationUrl;
  }

  public void setRegistrationUrl(String registrationUrl) {
    this.registrationUrl = registrationUrl;
  }
}

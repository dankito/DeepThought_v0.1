package net.dankito.deepthought.communication.model;

import net.dankito.deepthought.data.model.User;
import net.dankito.deepthought.util.StringUtils;

/**
 * Created by ganymed on 19/08/15.
 */
public class UserInfo {

  protected String databaseId = "";

  protected String universallyUniqueId = "";

  protected String userName = "";

  protected String firstName = "";

  protected String lastName = "";


  public UserInfo() {

  }

  public UserInfo(String databaseId, String universallyUniqueId, String userName, String firstName, String lastName) {
    this.databaseId = databaseId;
    this.universallyUniqueId = universallyUniqueId;
    this.userName = userName;
    this.firstName = firstName;
    this.lastName = lastName;
  }


  public String getDatabaseId() {
    return databaseId;
  }

  public String getUniversallyUniqueId() {
    return universallyUniqueId;
  }

  public void setUniversallyUniqueId(String universallyUniqueId) {
    this.universallyUniqueId = universallyUniqueId;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getUserInfoString() {
    String userInfo = getUserName();

    if(StringUtils.isNotNullOrEmpty(getFirstName()) || StringUtils.isNotNullOrEmpty(getLastName()))
      userInfo += " (" + getFirstName() + " " + getLastName() + ")";

    return userInfo;
  }


  public static UserInfo fromUser(User user) {
    return new UserInfo(user.getId(), user.getUniversallyUniqueId(), user.getUserName(), user.getFirstName(), user.getLastName());
  }

}

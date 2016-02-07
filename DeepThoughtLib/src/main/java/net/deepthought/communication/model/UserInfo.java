package net.deepthought.communication.model;

import net.deepthought.data.model.User;

/**
 * Created by ganymed on 19/08/15.
 */
public class UserInfo {

  protected String universallyUniqueId = "";

  protected String userName = "";

  protected String firstName = "";

  protected String lastName = "";


  public UserInfo() {

  }

  public UserInfo(String universallyUniqueId, String userName) {
    this.universallyUniqueId = universallyUniqueId;
    this.userName = userName;
  }

  public UserInfo(String universallyUniqueId, String userName, String firstName, String lastName) {
    this(universallyUniqueId, userName);
    this.firstName = firstName;
    this.lastName = lastName;
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


  public static UserInfo fromUser(User user) {
    return new UserInfo(user.getUniversallyUniqueId(), user.getUserName(), user.getFirstName(), user.getLastName());
  }

}

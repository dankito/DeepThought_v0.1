package com.j256.ormlite.jpa.testmodel;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 * Created by ganymed on 09/12/14.
 */
@Entity(name = TableConfig.UserTableName)
//@DiscriminatorValue("USER")
public class User extends BaseEntity implements Serializable {

  private static final long serialVersionUID = 7734370867234770314L;



  @Column(name = TableConfig.UserUserNameColumnName)
  protected String userName = "";


  protected User() {

  }

  public User(String userName) {
    this.userName = userName;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    Object previousValue = this.userName;
    this.userName = userName;
  }

  @Override
  @Transient
  public String getTextRepresentation() {
    return "User " + getUserName();
  }

  @Override
  public String toString() {
    return getTextRepresentation();
  }


  protected static User loggedOnUserMock = new User("Logged on User");

  public static User getLoggedOnUser() {
    return loggedOnUserMock;
  }


}

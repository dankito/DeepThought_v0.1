package net.deepthought.communication.model;

import net.deepthought.data.model.Group;

/**
 * Created by ganymed on 20/08/15.
 */
public class GroupInfo {

  protected String universallyUniqueId = "";

  protected String name = "";


  public GroupInfo(String universallyUniqueId, String name) {
    this.universallyUniqueId = universallyUniqueId;
    this.name = name;
  }


  public String getUniversallyUniqueId() {
    return universallyUniqueId;
  }

  public void setUniversallyUniqueId(String universallyUniqueId) {
    this.universallyUniqueId = universallyUniqueId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }


  public static GroupInfo fromGroup(Group group) {
    return new GroupInfo(group.getUniversallyUniqueId(), group.getName());
  }

}

package net.deepthought.communication.model;

import net.deepthought.data.model.Group;

/**
 * Created by ganymed on 20/08/15.
 */
public class GroupInfo {

  protected String universallyUniqueId = "";

  protected String name = "";

  protected String description = "";


  public GroupInfo(String universallyUniqueId, String name) {
    this.universallyUniqueId = universallyUniqueId;
    this.name = name;
  }

  public GroupInfo(String universallyUniqueId, String name, String description) {
    this(universallyUniqueId, name);
    this.description = description;
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

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }


  public static GroupInfo fromGroup(Group group) {
    return new GroupInfo(group.getUniversallyUniqueId(), group.getName(), group.getDescription());
  }

}

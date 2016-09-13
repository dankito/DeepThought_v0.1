package net.dankito.deepthought.communication.model;

import net.dankito.deepthought.data.model.Group;

/**
 * Created by ganymed on 20/08/15.
 */
public class GroupInfo {

  protected String databaseId = "";

  protected String universallyUniqueId = "";

  protected String name = "";

  protected String description = "";


  public GroupInfo(String databaseId, String universallyUniqueId, String name, String description) {
    this.databaseId = databaseId;
    this.universallyUniqueId = universallyUniqueId;
    this.name = name;
    this.description = description;
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
    return new GroupInfo(group.getId(), group.getUniversallyUniqueId(), group.getName(), group.getDescription());
  }

}

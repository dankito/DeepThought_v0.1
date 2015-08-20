package net.deepthought.data.model;

import net.deepthought.data.persistence.db.TableConfig;
import net.deepthought.data.persistence.db.UserDataEntity;
import net.deepthought.util.Localization;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

/**
 * Created by ganymed on 09/12/14.
 */
@Entity(name = TableConfig.GroupTableName)
public class Group extends UserDataEntity {

  private static final long serialVersionUID = 6783280420819608640L;


  @Column(name = TableConfig.GroupUniversallyUniqueIdColumnName)
  protected String universallyUniqueId = "";

  @Column(name = TableConfig.GroupNameColumnName)
  protected String name = "";

  @Column(name = TableConfig.GroupDescriptionColumnName)
  protected String description = "";

  @ManyToMany(fetch = FetchType.EAGER, mappedBy = "groups") // TODO: has cascade also to be set to { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH } as in Entry?
  protected Set<User> users = new HashSet<>();

  @ManyToMany(fetch = FetchType.EAGER/*, cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }*/ )
  @JoinTable(
      name = TableConfig.GroupDeviceJoinTableName,
      joinColumns = { @JoinColumn(name = TableConfig.GroupDeviceJoinTableGroupIdColumnName/*, referencedColumnName = "id"*/) },
      inverseJoinColumns = { @JoinColumn(name = TableConfig.GroupDeviceJoinTableDeviceIdColumnName/*, referencedColumnName = "id"*/) }
  )
  protected Set<Device> devices = new HashSet<>();

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = TableConfig.GroupDeepThoughtApplicationJoinColumnName)
  protected DeepThoughtApplication application;


  protected Group() {

  }

  public Group(String name) {
    this.name = name;
  }

  protected Group(String universallyUniqueId, String name, User owner) {
    this(name);
    this.universallyUniqueId = universallyUniqueId;
    this.owner = owner;
  }


  public String getUniversallyUniqueId() {
    return universallyUniqueId;
  }

  public void setUniversallyUniqueId(String universallyUniqueId) {
    Object previousValue = this.universallyUniqueId;
    this.universallyUniqueId = universallyUniqueId;
    callPropertyChangedListeners(TableConfig.GroupUniversallyUniqueIdColumnName, previousValue, universallyUniqueId);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    Object previousValue = this.name;
    this.name = name;
    callPropertyChangedListeners(TableConfig.GroupNameColumnName, previousValue, name);
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    Object previousValue = this.description;
    this.description = description;
    callPropertyChangedListeners(TableConfig.DeviceDescriptionColumnName, previousValue, description);
  }

  public Set<User> getUsers() {
    return users;
  }

  public boolean addUser(User user) {
    if(users.contains(user) == false) {
      if(users.add(user)) {
        user.addGroup(this);

        callEntityAddedListeners(users, user);
        return true;
      }
    }

    return false;
  }

  public boolean removeUser(User user) {
    if(users.contains(user) == true) {
      if(users.remove(user)) {
        user.removeGroup(this);

        callEntityRemovedListeners(users, user);
        return true;
      }
    }

    return false;
  }

  public Set<Device> getDevices() {
    return devices;
  }

  public boolean hasDevices() {
    return getDevices().size() > 0;
  }

  public boolean addDevice(Device device) {
    if(devices.contains(device) == false) {
      if(devices.add(device)) {
        device.addGroup(this);

        callEntityAddedListeners(devices, device);
        return true;
      }
    }

    return false;
  }

  public boolean removeDevice(Device device) {
    if(devices.contains(device) == true) {
      if(devices.remove(device)) {
        device.removeGroup(this);

        callEntityRemovedListeners(devices, device);
        return true;
      }
    }

    return false;
  }

  public DeepThoughtApplication getApplication() {
    return application;
  }


  @Override
  @Transient
  public String getTextRepresentation() {
    return "Group " + getName();
  }

  @Override
  public String toString() {
    return getTextRepresentation();
  }


  public static Group createUserDefaultGroup(User user) {
    String universallyUniqueId = UUID.randomUUID().toString();
    Group userGroup = new Group(universallyUniqueId, Localization.getLocalizedString("users.group", user.getUserName()), user);

    return userGroup;
  }

}

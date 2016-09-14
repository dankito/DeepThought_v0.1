package net.dankito.deepthought.data.model;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.data.persistence.db.TableConfig;
import net.dankito.deepthought.data.persistence.db.UserDataEntity;
import net.dankito.deepthought.util.localization.Localization;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

/**
 * Created by ganymed on 09/12/14.
 */
@Entity(name = TableConfig.DeviceTableName)
public class Device extends UserDataEntity {

  private static final long serialVersionUID = 7190723756152328858L;


  @Column(name = TableConfig.DeviceUniversallyUniqueIdColumnName)
  protected String universallyUniqueId = "";

  @Column(name = TableConfig.DeviceNameColumnName)
  protected String name = "";

  @Column(name = TableConfig.DeviceDescriptionColumnName)
  protected String description = "";

  @Column(name = TableConfig.DevicePlatformColumnName)
  protected String platform = "";

  @Column(name = TableConfig.DeviceOsVersionColumnName)
  protected String osVersion = "";

  @Column(name = TableConfig.DevicePlatformArchitectureColumnName)
  protected String platformArchitecture = "";

  @Column(name = TableConfig.DeviceLastKnownIpColumnName)
  protected String lastKnownIpAddress = "";

  @Column(name = TableConfig.DeviceCountSynchronizingDevicesColumnName)
  protected int countSynchronizingDevices = 0;

////  @JsonIgnore
//  @ManyToOne(fetch = FetchType.EAGER)
//  @JoinColumn(name = TableConfig.DeviceOwnerJoinColumnName)
//  protected User deviceOwner;

  @ManyToMany(fetch = FetchType.LAZY, mappedBy = "devices") // TODO: has cascade also to be set to { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }
  protected Set<User> users = new HashSet<>();

  @ManyToMany(fetch = FetchType.LAZY, mappedBy = "devices")
  protected Set<Group> groups = new HashSet<>();

  @Column(name = TableConfig.DeviceIconColumnName)
  @Lob
  protected byte[] deviceIcon = null;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = TableConfig.DeviceDeepThoughtApplicationJoinColumnName)
  protected DeepThoughtApplication application;


  protected Device() {

  }

  public Device(String universallyUniqueId, String name, String platform) {
    this.universallyUniqueId = universallyUniqueId;
    this.name = name;
    this.platform = platform;
  }

  public Device(String universallyUniqueId, String name, String platform, String osVersion) {
    this(universallyUniqueId, name, platform);
    this.osVersion = osVersion;
  }

  public Device(String universallyUniqueId, String name, String platform, String osVersion, String platformArchitecture) {
    this(universallyUniqueId, name, platform, osVersion);
    this.platformArchitecture = platformArchitecture;
  }


  public String getUniversallyUniqueId() {
    return universallyUniqueId;
  }

  protected void setUniversallyUniqueId(String universallyUniqueId) {
    Object previousValue = this.universallyUniqueId;
    this.universallyUniqueId = universallyUniqueId;
    callPropertyChangedListeners(TableConfig.DeviceUniversallyUniqueIdColumnName, previousValue, universallyUniqueId);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    Object previousValue = this.name;
    this.name = name;
    callPropertyChangedListeners(TableConfig.DeviceNameColumnName, previousValue, name);
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    Object previousValue = this.description;
    this.description = description;
    callPropertyChangedListeners(TableConfig.DeviceDescriptionColumnName, previousValue, description);
  }

  public String getPlatform() {
    return platform;
  }

  public void setPlatform(String platform) {
    this.platform = platform;
  }

  public String getPlatformArchitecture() {
    return platformArchitecture;
  }

  public void setPlatformArchitecture(String platformArchitecture) {
    this.platformArchitecture = platformArchitecture;
  }

  public String getOsVersion() {
    return osVersion;
  }

  public void setOsVersion(String osVersion) {
    Object previousValue = this.osVersion;
    this.osVersion = osVersion;
    callPropertyChangedListeners(TableConfig.DeviceOsVersionColumnName, previousValue, osVersion);
  }

  public String getLastKnownIpAddress() {
    return lastKnownIpAddress;
  }

  public void setLastKnownIpAddress(String lastKnownIpAddress) {
    Object previousValue = this.lastKnownIpAddress;
    this.lastKnownIpAddress = lastKnownIpAddress;
    callPropertyChangedListeners(TableConfig.DeviceLastKnownIpColumnName, previousValue, lastKnownIpAddress);
  }

  public int getCountSynchronizingDevices() {
    return countSynchronizingDevices;
  }

  public void setCountSynchronizingDevices(int countSynchronizingDevices) {
    Object previousValue = this.countSynchronizingDevices;
    this.countSynchronizingDevices = countSynchronizingDevices;
    callPropertyChangedListeners(TableConfig.DeviceCountSynchronizingDevicesColumnName, previousValue, countSynchronizingDevices);
  }

  public void incrementCountSynchronizingDevices() {
    setCountSynchronizingDevices(getCountSynchronizingDevices() + 1);
  }

  public Set<User> getUsers() {
    return users;
  }

  public boolean addUser(User user) {
    if(users.contains(user) == false) {
      if(users.add(user)) {
        user.addDevice(this);

        callEntityAddedListeners(users, user);
        return true;
      }
    }

    return false;
  }

  public boolean removeUser(User user) {
    if(users.contains(user) == true) {
      if(users.remove(user)) {
        user.removeDevice(this);

        callEntityRemovedListeners(users, user);
        return true;
      }
    }

    return false;
  }

  public Set<Group> getGroups() {
    return groups;
  }

  public boolean addGroup(Group group) {
    if(groups.contains(group) == false) {
      if(groups.add(group)) {
        group.addDevice(this);

        callEntityAddedListeners(groups, group);
        return true;
      }
    }

    return false;
  }

  public boolean removeGroup(Group group) {
    if(groups.contains(group) == true) {
      if(groups.remove(group)) {
        group.removeDevice(this);

        callEntityRemovedListeners(groups, group);
        return true;
      }
    }

    return false;
  }

  public byte[] getDeviceIcon() {
    return deviceIcon;
  }

  public void setDeviceIcon(byte[] deviceIcon) {
    this.deviceIcon = deviceIcon;
  }

  public DeepThoughtApplication getApplication() {
    return application;
  }


  @Override
  @Transient
  public String getTextRepresentation() {
    String infoString = platform + " " + osVersion;

    if(platform != null && platform.toLowerCase().contains("android")) {
      infoString = name + " (" + infoString + ")";
    }

    return infoString;
  }

  @Override
  public String toString() {
    return "Device " + getTextRepresentation();
  }


  public static Boolean isRunningOnAndroid() {
    try {
      Class.forName("android.app.Activity");
      return true;
    } catch(Exception ex) { }

    return false;
  }


  public static Device createUserDefaultDevice(User user) {
    String universallyUniqueId = UUID.randomUUID().toString();

    String platform = System.getProperty("os.name");
    String deviceName = Localization.getLocalizedString("users.default.device.name", user.getUserName(), platform);
    String osVersion = System.getProperty("os.version");

    if(Application.getPlatformConfiguration() != null) { // TODO: try to get rid of static method calls
      osVersion = Application.getPlatformConfiguration().getOsVersionString();
      platform = Application.getPlatformConfiguration().getPlatformName();

      if(Application.getPlatformConfiguration().getDeviceName() != null) {
        deviceName = Application.getPlatformConfiguration().getDeviceName();
      }
    }

    // TODO: may store timezone (System.getProperty("user.timezone")) for Synchronizing Data

    Device userDefaultDevice = new Device(universallyUniqueId, deviceName,
        platform, osVersion, System.getProperty("os.arch"));

    return userDefaultDevice;
  }

}

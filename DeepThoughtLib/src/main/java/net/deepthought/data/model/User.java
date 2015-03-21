package net.deepthought.data.model;

import net.deepthought.data.model.listener.SettingsChangedListener;
import net.deepthought.data.model.settings.SettingsBase;
import net.deepthought.data.model.settings.UserDeviceSettings;
import net.deepthought.data.model.settings.enums.Setting;
import net.deepthought.data.persistence.db.BaseEntity;
import net.deepthought.data.persistence.db.TableConfig;
import net.deepthought.util.Localization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

/**
 * Created by ganymed on 09/12/14.
 */
@Entity(name = TableConfig.UserTableName)
//@DiscriminatorValue("USER")
public class User extends BaseEntity implements Serializable {

  private static final long serialVersionUID = 7734370867234770314L;

  private final static Logger log = LoggerFactory.getLogger(User.class);



  @Column(name = TableConfig.UserUniversallyUniqueIdColumnName)
  protected String universallyUniqueId = "";

  @Column(name = TableConfig.UserUserNameColumnName)
  protected String userName = "";

  @Column(name = TableConfig.UserFirstNameColumnName)
  protected String firstName = "";

  @Column(name = TableConfig.UserLastNameColumnName)
  protected String lastName = "";

  @Column(name = TableConfig.UserPasswordColumnName)
  protected String password = "";

  @Column(name = TableConfig.UserIsLocalUserColumnName, columnDefinition = "SMALLINT DEFAULT 0", nullable = false)
  protected boolean isLocalUser = true;

  protected transient UserDeviceSettings settings;

  @Column(name = TableConfig.UserUserDeviceSettingsColumnName)
  @Lob
  protected String settingsString = "";

  @OneToOne(fetch = FetchType.EAGER, cascade = { CascadeType.PERSIST } )
  @JoinColumn(name = TableConfig.UserLastViewedDeepThoughtColumnName)
  protected DeepThought lastViewedDeepThought = null;

  // TODO: make lazy again if an user really can have multiple DeepThoughts
  @OneToMany(fetch = FetchType.LAZY, mappedBy = "deepThoughtOwner", cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH })
  protected Set<DeepThought> deepThoughts = new HashSet<>();

//  @OneToMany(fetch = FetchType.EAGER, mappedBy = "deviceOwner"/*, cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }*/)
  @ManyToMany(fetch = FetchType.EAGER/*, cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }*/ )
  @JoinTable(
      name = TableConfig.UserDeviceJoinTableName,
      joinColumns = { @JoinColumn(name = TableConfig.UserDeviceJoinTableUserIdColumnName/*, referencedColumnName = "id"*/) },
      inverseJoinColumns = { @JoinColumn(name = TableConfig.UserDeviceJoinTableDeviceIdColumnName/*, referencedColumnName = "id"*/) }
  )
  protected Set<Device> devices = new HashSet<>();

  @ManyToMany(fetch = FetchType.EAGER/*, cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }*/ )
  @JoinTable(
      name = TableConfig.UserGroupJoinTableName,
      joinColumns = { @JoinColumn(name = TableConfig.UserGroupJoinTableUserIdColumnName/*, referencedColumnName = "id"*/) },
      inverseJoinColumns = { @JoinColumn(name = TableConfig.UserGroupJoinTableGroupIdColumnName/*, referencedColumnName = "id"*/) }
  )
  protected Set<Group> groups = new HashSet<>();

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = TableConfig.UserDeepThoughtApplicationJoinColumnName)
  protected DeepThoughtApplication application;


  protected User() {

  }

  public User(String userName) {
    this.userName = userName;
  }

  protected User(String universallyUniqueId, String userName, boolean isLocalUser) {
    this.universallyUniqueId = universallyUniqueId;
    this.userName = userName;
    this.isLocalUser = isLocalUser;
  }


  public String getUniversallyUniqueId() {
    return universallyUniqueId;
  }

  protected void setUniversallyUniqueId(String universallyUniqueId) {
    Object previousValue = this.universallyUniqueId;
    this.universallyUniqueId = universallyUniqueId;
    callPropertyChangedListeners(TableConfig.UserUniversallyUniqueIdColumnName, previousValue, universallyUniqueId);
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    Object previousValue = this.userName;
    this.userName = userName;
    callPropertyChangedListeners(TableConfig.UserUserNameColumnName, previousValue, userName);
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    Object previousValue = this.firstName;
    this.firstName = firstName;
    callPropertyChangedListeners(TableConfig.UserLastNameColumnName, previousValue, firstName);
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    Object previousValue = this.lastName;
    this.lastName = lastName;
    callPropertyChangedListeners(TableConfig.UserLastNameColumnName, previousValue, lastName);
  }

  public boolean isLocalUser() {
    return isLocalUser;
  }

  public UserDeviceSettings getSettings() {
    if(settings == null) {
      settings = SettingsBase.createSettingsFromString(settingsString, UserDeviceSettings.class);
      settings.addSettingsChangedListener(userDeviceSettingsChangedListener);
    }

    return settings;
  }

  protected void setSettingsString(String settingsString) {
    Object previousValue = this.settingsString;
    this.settingsString = settingsString;
    callPropertyChangedListeners(TableConfig.UserUserDeviceSettingsColumnName, previousValue, settingsString);
  }

  public DeepThought getLastViewedDeepThought() {
    return lastViewedDeepThought;
  }

  public void setLastViewedDeepThought(DeepThought lastViewedDeepThought) {
    Object previousValue = this.lastViewedDeepThought;
    this.lastViewedDeepThought = lastViewedDeepThought;
    callPropertyChangedListeners(TableConfig.UserLastViewedDeepThoughtColumnName, previousValue, lastViewedDeepThought);
  }

  public Set<DeepThought> getDeepThoughts() {
    return deepThoughts;
  }

  public boolean addDeepThought(DeepThought deepThought) {
    if(this.deepThoughts.contains(deepThought) == false) {
      boolean result = this.deepThoughts.add(deepThought);
      if(result) {
        deepThought.deepThoughtOwner = this;
      }

      callEntityAddedListeners(deepThoughts, deepThought);
      return result;
    }

    return false;
  }

  public boolean removeDeepThought(DeepThought deepThought) {
    if(this.deepThoughts.contains(deepThought) == true) {
      boolean result = this.deepThoughts.remove(deepThought);
      if(result) {
        deepThought.deepThoughtOwner = null;
      }

      callEntityRemovedListeners(deepThoughts, deepThought);
      return result;
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
        device.addUser(this);

        callEntityAddedListeners(devices, device);
        return true;
      }
    }

    return false;
  }

  public boolean removeDevice(Device device) {
    if(devices.contains(device) == true) {
      if(devices.remove(device)) {
        device.removeUser(this);

        callEntityRemovedListeners(devices, device);
        return true;
      }
    }

    return false;
  }


  public Set<Group> getGroups() {
    return groups;
  }

  public boolean hasGroups() {
    return getGroups().size() > 0;
  }

  public boolean addGroup(Group group) {
    if(groups.contains(group) == false) {
      if(groups.add(group)) {
        group.addUser(this);

        callEntityAddedListeners(groups, group);
        return true;
      }
    }

    return false;
  }

  public boolean removeGroup(Group group) {
    if(groups.contains(group) == true) {
      if(groups.remove(group)) {
        group.removeUser(this);

        callEntityRemovedListeners(groups, group);
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
    return "User " + getUserName();
  }

  @Override
  public String toString() {
    return getTextRepresentation();
  }


  protected transient SettingsChangedListener userDeviceSettingsChangedListener = new SettingsChangedListener() {
    @Override
    public void settingsChanged(Setting setting, Object previousValue, Object newValue) {
      String serializationResult = SettingsBase.serializeSettings(settings);
      if(serializationResult != null)
        setSettingsString(serializationResult);
    }
  };


  public static User createNewLocalUser() {
    User user = null;
    String universallyUniqueId = UUID.randomUUID().toString();

    try {
      user = new User(universallyUniqueId, System.getProperty("user.name"), true);
    } catch(Exception ex) {
      log.error("Could not get System property user.name", ex);
      user = new User(universallyUniqueId, Localization.getLocalizedStringForResourceKey("default.user.name"), true);
    }

    Group userGroup = Group.createUserDefaultGroup(user);
    user.addGroup(userGroup);

    return user;
  }

}

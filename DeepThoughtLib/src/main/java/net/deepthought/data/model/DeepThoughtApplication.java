package net.deepthought.data.model;

import net.deepthought.data.model.enums.ApplicationLanguage;
import net.deepthought.data.model.listener.EntityListener;
import net.deepthought.data.model.settings.UserDeviceSettings;
import net.deepthought.data.persistence.db.BaseEntity;
import net.deepthought.data.persistence.db.TableConfig;
import net.deepthought.util.Localization;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;

/**
 * Created by ganymed on 04/01/15.
 */
@Entity(name = TableConfig.DeepThoughtApplicationTableName)
public class DeepThoughtApplication extends BaseEntity implements Serializable {

  private static final long serialVersionUID = -3232937271770851228L;


  @OneToOne(fetch = FetchType.EAGER, cascade = { CascadeType.PERSIST } )
  @JoinColumn(name = TableConfig.DeepThoughtApplicationLastLoggedOnUserJoinColumnName)
  protected User lastLoggedOnUser;

//  @Column(name = TableConfig.DeepThoughtApplicationAutoLogOnLastLoggedOnUserColumnName, columnDefinition = "SMALLINT DEFAULT 0", nullable = false)
  @Column(name = TableConfig.DeepThoughtApplicationAutoLogOnLastLoggedOnUserColumnName)
  protected boolean autoLogOnLastLoggedOnUser = false;

  @OneToMany(fetch = FetchType.EAGER, mappedBy = "application", cascade = CascadeType.PERSIST)
  protected Set<User> users = new HashSet<>();

  @OneToMany(fetch = FetchType.EAGER, mappedBy = "application", cascade = CascadeType.PERSIST)
  protected Set<Group> groups = new HashSet<>();

  @OneToMany(fetch = FetchType.EAGER, mappedBy = "application", cascade = CascadeType.PERSIST)
  protected Set<Device> devices = new HashSet<>();

  @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
  @JoinColumn(name = TableConfig.DeepThoughtApplicationLocalDeviceJoinColumnName)
  protected Device localDevice;


  @OneToMany(fetch = FetchType.LAZY, mappedBy = "application", cascade = CascadeType.PERSIST)
  @OrderBy(value = "sortOrder")
  protected Set<ApplicationLanguage> applicationLanguages = new HashSet<>(); // these are the Languages the UI can display



  protected DeepThoughtApplication() {

  }

  public DeepThoughtApplication(User lastLoggedOnUser, boolean autoLogOnLastLoggedOnUser) {
    this.lastLoggedOnUser = lastLoggedOnUser;
    this.autoLogOnLastLoggedOnUser = autoLogOnLastLoggedOnUser;
  }


  public User getLastLoggedOnUser() {
    return lastLoggedOnUser;
  }

  public void setLastLoggedOnUser(User lastLoggedOnUser) {
    Object previousValue = this.lastLoggedOnUser;
    this.lastLoggedOnUser = lastLoggedOnUser;
    callPropertyChangedListeners(TableConfig.DeepThoughtApplicationLastLoggedOnUserJoinColumnName, previousValue, lastLoggedOnUser);
  }

  public boolean autoLogOnLastLoggedOnUser() {
    return autoLogOnLastLoggedOnUser;
  }

  public void setAutoLogOnLastLoggedOnUser(boolean autoLogOnLastLoggedOnUser) {
    Object previousValue = this.autoLogOnLastLoggedOnUser;
    this.autoLogOnLastLoggedOnUser = autoLogOnLastLoggedOnUser;
    callPropertyChangedListeners(TableConfig.DeepThoughtApplicationAutoLogOnLastLoggedOnUserColumnName, previousValue, autoLogOnLastLoggedOnUser);
  }


  public Collection<User> getUsers() {
    return users;
  }

  public boolean addUser(User user) {
    boolean result = users.add(user);

    if(result) {
      user.application = this;
      callEntityAddedListeners(users, user);
    }

    return result;
  }

  public boolean removeUser(User user) {
    boolean result = users.remove(user);

    if(result) {
      user.application = null;
      callEntityRemovedListeners(users, user);
    }

    return result;
  }


  public Collection<Group> getGroups() {
    return groups;
  }

  public boolean addGroup(Group group) {
    boolean result = groups.add(group);

    if(result) {
      group.application = this;
      callEntityAddedListeners(groups, group);
    }

    return result;
  }

  public boolean removeGroup(Group group) {
    boolean result = groups.remove(group);

    if(result) {
      group.application = null;
      callEntityRemovedListeners(groups, group);
    }

    return result;
  }


  public Collection<Device> getDevices() {
    return devices;
  }

  public boolean addDevice(Device device) {
    boolean result = devices.add(device);

    if(result) {
      device.application = this;
      callEntityAddedListeners(devices, device);
    }

    return result;
  }

  public boolean removeDevice(Device device) {
    if(localDevice.equals(device)) // don't delete local device!
      return false;

    boolean result = devices.remove(device);

    if(result) {
      device.application = null;
      callEntityRemovedListeners(devices, device);
    }

    return result;
  }

  public Device getLocalDevice() {
    return localDevice;
  }

  protected void setLocalDevice(Device localDevice) {
    this.localDevice = localDevice;
  }


  public Collection<ApplicationLanguage> getApplicationLanguages() {
    return applicationLanguages;
  }

  protected boolean addApplicationLanguage(ApplicationLanguage applicationLanguage) {
    boolean result = applicationLanguages.add(applicationLanguage);

    if(result) {
      applicationLanguage.setApplication(this);
      callEntityAddedListeners(applicationLanguages, applicationLanguage);
    }

    return result;
  }

//  public boolean removeApplicationLanguage(ApplicationLanguage applicationLanguage) {
//    boolean result = applicationLanguages.remove(applicationLanguage);
//
//    if(result) {
//      applicationLanguage.setApplication(null);
//    }
//
//    return result;
//  }


  /*        Listeners handling        */

//  @Override
//  protected void callEntityAddedListeners(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity) {
//    addedEntity.addEntityListener(subEntitiesListener); // add a listener to every Entity so that it's changes can be tracked
//
//    super.callEntityAddedListeners(collectionHolder, collection, addedEntity);
//  }
//
//  @Override
//  protected void callEntityRemovedListeners(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {
//    // don't remove listener if removedEntity is still on a DeepThought collection
//    if(collectionHolder == this || isComposition(collectionHolder, removedEntity) == true)
//      removedEntity.removeEntityListener(subEntitiesListener);
//
//    super.callEntityRemovedListeners(collectionHolder, collection, removedEntity);
//  }
//
//  public boolean isComposition(BaseEntity collectionHolder, BaseEntity entity) {
//    // currently no Composition in DeepThoughtApplication sub Entities
//
//    return false;
//  }
//
//  protected transient EntityListener subEntitiesListener = new EntityListener() {
//    @Override
//    public void propertyChanged(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {
//      log.debug("SubEntity's property {} changed to {}; {}", propertyName, newValue, entity);
//
//      entityUpdated(entity);
//    }
//
//    @Override
//    public void entityAddedToCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity) {
//      callEntityAddedListeners(collectionHolder, collection, addedEntity);
//      entityUpdated(collectionHolder);
//    }
//
//    @Override
//    public void entityOfCollectionUpdated(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity updatedEntity) {
//      callEntityOfCollectionUpdatedListeners(collectionHolder, collection, updatedEntity);
//    }
//
//    @Override
//    public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {
//      callEntityRemovedListeners(collectionHolder, collection, removedEntity);
//      entityUpdated(collectionHolder);
//    }
//  };
//
//  protected void entityUpdated(final BaseEntity entity) {
//    if(entity instanceof User)
//      callEntityOfCollectionUpdatedListeners(getUsers(), entity);
//    else if(entity instanceof Group)
//      callEntityOfCollectionUpdatedListeners(getGroups(), entity);
//    else if(entity instanceof Device)
//      callEntityOfCollectionUpdatedListeners(getDevices(), entity);
//    else if(entity instanceof ApplicationLanguage)
//      callEntityOfCollectionUpdatedListeners(getApplicationLanguages(), entity);
//    else
//      log.warn("Updated entity of type " + entity.getClass() + " retrieved, but don't know what to do with this type");
//  }


  @Override
  public boolean addEntityListener(EntityListener listener) {
    return super.addEntityListener(listener);
  }

  @Override
  public String toString() {
    return "DeepThoughtApplication: " + getUsers().size() + " users, " + getGroups().size() + " group.";
  }


  public static DeepThoughtApplication createApplication() {
    User defaultLocalUser = User.createNewLocalUser();

    Device localDevice = Device.createUserDefaultDevice(defaultLocalUser);
    defaultLocalUser.addDevice(localDevice);

    DeepThought defaultDeepThought = DeepThought.createEmptyDeepThought();
    defaultLocalUser.addDeepThought(defaultDeepThought);
    defaultLocalUser.setLastViewedDeepThought(defaultDeepThought);

    DeepThoughtApplication application = new DeepThoughtApplication(defaultLocalUser, true);
    application.setLocalDevice(localDevice);

    application.addUser(defaultLocalUser);
    for(Group group : defaultLocalUser.getGroups()) {
      application.addGroup(group);
      group.addDevice(localDevice);
    }
    for(Device device : defaultLocalUser.getDevices())
      application.addDevice(device);

    createEnumerationsDefaultValues(application);

    setApplicationDefaultLanguage(application, defaultLocalUser.getSettings());

    return application;
  }

  protected static void setApplicationDefaultLanguage(DeepThoughtApplication application, UserDeviceSettings settings) {
    String userLanguage = System.getProperty("user.language").toLowerCase();

    for(ApplicationLanguage language : application.getApplicationLanguages()) {
      if(language.getLanguageKey().toLowerCase().startsWith(userLanguage)) {
        settings.setLanguage(language);
        return;
      }
    }

    String applicationLanguageEnglish = Localization.getLocalizedStringForResourceKey("application.language.english");

    for(ApplicationLanguage language : application.getApplicationLanguages()) {
      if(language.getName().equals(applicationLanguageEnglish)) {
        settings.setLanguage(language);
        return;
      }
    }
  }

  protected static void createEnumerationsDefaultValues(DeepThoughtApplication application) {
    createApplicationLanguageDefaultValues(application);
  }

  protected static void createApplicationLanguageDefaultValues(DeepThoughtApplication application) {
    application.addApplicationLanguage(new ApplicationLanguage("application.language.english", "en", true, false, 1));
    application.addApplicationLanguage(new ApplicationLanguage("application.language.german", "de", true, false, 2));
  }

}

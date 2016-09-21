package net.dankito.deepthought.data.sync;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.communication.messages.request.AskForDeviceRegistrationRequest;
import net.dankito.deepthought.communication.model.DeepThoughtInfo;
import net.dankito.deepthought.communication.model.GroupInfo;
import net.dankito.deepthought.communication.model.HostInfo;
import net.dankito.deepthought.communication.model.UserInfo;
import net.dankito.deepthought.data.model.Category;
import net.dankito.deepthought.data.model.DeepThought;
import net.dankito.deepthought.data.model.DeepThoughtApplication;
import net.dankito.deepthought.data.model.Device;
import net.dankito.deepthought.data.model.EntriesLinkGroup;
import net.dankito.deepthought.data.model.Entry;
import net.dankito.deepthought.data.model.FileLink;
import net.dankito.deepthought.data.model.Group;
import net.dankito.deepthought.data.model.Note;
import net.dankito.deepthought.data.model.Person;
import net.dankito.deepthought.data.model.Reference;
import net.dankito.deepthought.data.model.ReferenceSubDivision;
import net.dankito.deepthought.data.model.SeriesTitle;
import net.dankito.deepthought.data.model.Tag;
import net.dankito.deepthought.data.model.User;
import net.dankito.deepthought.data.model.enums.ApplicationLanguage;
import net.dankito.deepthought.data.model.enums.BackupFileServiceType;
import net.dankito.deepthought.data.model.enums.ExtensibleEnumeration;
import net.dankito.deepthought.data.model.enums.FileType;
import net.dankito.deepthought.data.model.enums.Language;
import net.dankito.deepthought.data.model.enums.NoteType;
import net.dankito.deepthought.data.persistence.IEntityManager;
import net.dankito.deepthought.util.Notification;
import net.dankito.deepthought.util.NotificationType;
import net.dankito.deepthought.util.StringUtils;
import net.dankito.deepthought.util.localization.Localization;

import java.util.Collection;
import java.util.Map;

/**
 * Created by ganymed on 07/09/16.
 */
public class InitialSyncManager {


  protected IEntityManager entityManager;


  public InitialSyncManager(IEntityManager entityManager) {
    this.entityManager = entityManager;
  }


  public void syncUserInformationWithRemoteOnes(User loggedOnUser, UserInfo remoteUser, GroupInfo remoteUserDefaultGroup) {
    synchronizeUser(loggedOnUser, remoteUser);

    Group userDefaultGroup = loggedOnUser.getUsersDefaultGroup();
    synchronizeGroup(userDefaultGroup, remoteUserDefaultGroup);
  }

  protected void synchronizeUser(User loggedOnUser, UserInfo remoteUser) {
    loggedOnUser.setUniversallyUniqueId(remoteUser.getUniversallyUniqueId());
    loggedOnUser.setUserName(remoteUser.getUserName());
    loggedOnUser.setFirstName(remoteUser.getFirstName());
    loggedOnUser.setLastName(remoteUser.getLastName());
  }

  protected void synchronizeGroup(Group userDefaultGroup, GroupInfo remoteUserDefaultGroup) {
    userDefaultGroup.setUniversallyUniqueId(remoteUserDefaultGroup.getUniversallyUniqueId());
    userDefaultGroup.setName(remoteUserDefaultGroup.getName());
    userDefaultGroup.setDescription(remoteUserDefaultGroup.getDescription());
  }


  public void syncLocalDatabaseIdsWithRemoteOnes(DeepThought localDeepThought, User loggedOnUser, Device localDevice, AskForDeviceRegistrationRequest requestOrResponse) {
    syncLocalDatabaseIdsWithRemoteOnes(localDeepThought, loggedOnUser, localDevice, requestOrResponse.getCurrentDeepThoughtInfo(), requestOrResponse.getUser(), requestOrResponse.getDevice(), requestOrResponse.getGroup());
  }

  public void syncLocalDatabaseIdsWithRemoteOnes(DeepThought localDeepThought, User loggedOnUser, Device localDevice, DeepThoughtInfo remoteDeepThought,
                                                 UserInfo remoteUser, HostInfo remoteDevice, GroupInfo remoteUserDefaultGroup) {
    Group userDefaultGroup = loggedOnUser.getUsersDefaultGroup();

    entityManager.deleteEntity(loggedOnUser);
    entityManager.deleteEntity(userDefaultGroup);
    entityManager.deleteEntity(localDeepThought);

    loggedOnUser.setId(remoteUser.getDatabaseId());

    userDefaultGroup.setId(remoteUserDefaultGroup.getDatabaseId());

    localDeepThought.setId(remoteDeepThought.getDatabaseId());

    entityManager.persistEntity(userDefaultGroup);
    entityManager.persistEntity(loggedOnUser);
    entityManager.persistEntity(localDeepThought);

    entityManager.updateEntity(loggedOnUser.getApplication());


    entityManager.deleteEntity(localDeepThought.getTopLevelEntry());
    localDeepThought.getTopLevelEntry().setId(remoteDeepThought.getTopLevelEntryId());
    entityManager.persistEntity(localDeepThought.getTopLevelEntry());

    entityManager.deleteEntity(localDeepThought.getTopLevelCategory());
    localDeepThought.getTopLevelCategory().setId(remoteDeepThought.getTopLevelCategoryId());
    entityManager.persistEntity(localDeepThought.getTopLevelCategory());

    updateExtensibleEnumerations(localDeepThought, remoteDeepThought, entityManager);

    // as User's Id has changed, all UserDataEntities pointing to that User Id have to be updated in Database
    updateAllUserDataEntities(loggedOnUser, entityManager);

    Application.notifyUser(new Notification(NotificationType.InitialDatabaseSynchronizationDone));
  }

  protected void updateExtensibleEnumerations(DeepThought localDeepThought, DeepThoughtInfo remoteDeepThought, IEntityManager entityManager) {
    updateExtensibleEnumeration(localDeepThought.getNoteTypes(), remoteDeepThought.getNoteTypeIds(), entityManager);

    updateExtensibleEnumeration(localDeepThought.getFileTypes(), remoteDeepThought.getFileTypeIds(), entityManager);

    updateExtensibleEnumeration(localDeepThought.getLanguages(), remoteDeepThought.getLanguageIds(), entityManager);

    updateExtensibleEnumeration(localDeepThought.getBackupFileServiceTypes(), remoteDeepThought.getBackupFileServiceTypesIds(), entityManager);

    entityManager.updateEntity(localDeepThought);
  }

  protected void updateExtensibleEnumeration(Collection localExtensibleEnumerationEntities, Map<String, String> remoteExtensibleEnumerationIds, IEntityManager entityManager) {
    for(String noteTypeNameResourceKey : remoteExtensibleEnumerationIds.keySet()) {
      for(ExtensibleEnumeration enumerationEntity : (Collection<ExtensibleEnumeration>)localExtensibleEnumerationEntities) {
        if(noteTypeNameResourceKey.equals(enumerationEntity.getNameResourceKey())) {
          updateExtensibleEnumeration(enumerationEntity, remoteExtensibleEnumerationIds.get(noteTypeNameResourceKey), entityManager);
          break;
        }
      }
    }
  }

  protected void updateExtensibleEnumeration(ExtensibleEnumeration enumerationEntity, String id, IEntityManager entityManager) {
    entityManager.deleteEntity(enumerationEntity);

    enumerationEntity.setId(id);

    entityManager.persistEntity(enumerationEntity);
  }

  protected void updateAllUserDataEntities(User loggedOnUser, IEntityManager entityManager) {
    for(Device userDevice : loggedOnUser.getDevices()) {
      entityManager.updateEntity(userDevice);
    }

    for(Group userGroup : loggedOnUser.getGroups()) {
      entityManager.updateEntity(userGroup);
    }

    DeepThoughtApplication deepThoughtApplication = loggedOnUser.getApplication();

    for(ApplicationLanguage language : deepThoughtApplication.getApplicationLanguages()) {
      entityManager.updateEntity(language);
    }

    for(DeepThought deepThought : loggedOnUser.getDeepThoughts()) {
      updateAllUserDataEntitiesOnDeepThought(deepThought, entityManager);
    }
  }

  protected void updateAllUserDataEntitiesOnDeepThought(DeepThought deepThought, IEntityManager entityManager) {
    entityManager.updateEntity(deepThought.getOwner());

    for(BackupFileServiceType backupFileServiceType : deepThought.getBackupFileServiceTypes()) {
      entityManager.updateEntity(backupFileServiceType);
    }

    for(Language language : deepThought.getLanguages()) {
      entityManager.updateEntity(language);
    }

    for(FileType fileType : deepThought.getFileTypes()) {
      entityManager.updateEntity(fileType);
    }

    for(NoteType noteType : deepThought.getNoteTypes()) {
      entityManager.updateEntity(noteType);
    }

    for(Note note : deepThought.getNotes()) {
      entityManager.updateEntity(note);
    }

    for(Tag tag : deepThought.getTags()) {
      entityManager.updateEntity(tag);
    }

    entityManager.updateEntity(deepThought.getTopLevelCategory());

    for(Category category : deepThought.getCategories()) {
      entityManager.updateEntity(category);
    }

    for(FileLink file : deepThought.getFiles()) {
      entityManager.updateEntity(file);
    }

    for(SeriesTitle seriesTitle : deepThought.getSeriesTitles()) {
      entityManager.updateEntity(seriesTitle);
    }

    for(Reference reference : deepThought.getReferences()) {
      entityManager.updateEntity(reference);
    }

    for(ReferenceSubDivision subDivision : deepThought.getReferenceSubDivisions()) {
      entityManager.updateEntity(subDivision);
    }

    for(Person person : deepThought.getPersons()) {
      entityManager.updateEntity(person);
    }

    entityManager.updateEntity(deepThought.getTopLevelEntry());

    for(Entry entry : deepThought.getEntries()) {
      entityManager.updateEntity(entry);

      for(EntriesLinkGroup entriesLinkGroup : entry.getLinkGroups()) {
        entityManager.updateEntity(entriesLinkGroup);
      }
    }
  }


  public boolean shouldUseLocalUserName(User localUser, UserInfo remoteUser) {
    String localUserName = localUser.getUserName();
    String remoteUserName = remoteUser.getUserName();

    if(StringUtils.isNullOrEmpty(localUserName) == false && StringUtils.isNullOrEmpty(remoteUserName)) {
      return true;
    }

    if(StringUtils.isNullOrEmpty(localUserName) && StringUtils.isNullOrEmpty(remoteUserName) == false) {
      return false;
    }

    if(remoteUserName.contains("root") && localUserName.contains("root") == false) {
      return true;
    }
    if(remoteUserName.contains("root") == false && localUserName.contains("root")) {
      return false;
    }

    // TODO: add additional tests
    return true;
  }

  public boolean shouldUseLocalDatabaseIds(DeepThought localDeepThought, User localUser, Device localDevice,
                                           DeepThoughtInfo remoteDeepThought, UserInfo remoteUser, HostInfo remoteDevice) throws IllegalStateException {
    if(localDevice.getCountSynchronizingDevices() > 0 && remoteDevice.getCountSynchronizingDevice() == 0) {
      return true;
    }
    if(localDevice.getCountSynchronizingDevices() == 0 && remoteDevice.getCountSynchronizingDevice() > 0) {
      return false;
    }
    if(localDevice.getCountSynchronizingDevices() > 0 && remoteDevice.getCountSynchronizingDevice() > 0) {
      if(isTheSameUser(localUser, remoteUser, localDeepThought, remoteDeepThought) == false) {
        // TODO: now we're in a Trap, this has to be urgently resolved:
        // Both devices have already synchronized their Database Ids with other Devices, so no matter which one we choose,
        // these already synchronized devices must update their Database Ids as well
        throw new IllegalStateException(Localization.getLocalizedString("alert.message.message.both.devices.already.synchronized.with.other.devices"));
      }
    }

    float localDeepThoughtWeight = calculateEntitiesWeight(localDeepThought);
    float remoteDeepThoughtWeight = calculateEntitiesWeight(remoteDeepThought);

    return localDeepThoughtWeight >= remoteDeepThoughtWeight;
  }

  protected boolean isTheSameUser(User localUser, UserInfo remoteUser, DeepThought localDeepThought, DeepThoughtInfo remoteDeepThought) {
    return localUser.getId().equals(remoteUser.getDatabaseId()) && localUser.getUniversallyUniqueId().equals(remoteUser.getUniversallyUniqueId()) &&
        localDeepThought.getId().equals(remoteDeepThought.getDatabaseId());
  }

  protected float calculateEntitiesWeight(DeepThought deepThought) {
    return calculateEntitiesWeight(deepThought.getCountEntries(), deepThought.getCountTags(), deepThought.getCountCategories(),
        (deepThought.getCountSeriesTitles() + deepThought.getCountReferences() + deepThought.getCountReferenceSubDivisions()),
        deepThought.getCountPersons(), deepThought.getCountFiles());
  }

  protected float calculateEntitiesWeight(DeepThoughtInfo deepThought) {
    return calculateEntitiesWeight(deepThought.getCountEntries(), deepThought.getCountTags(), deepThought.getCountCategories(),
          deepThought.getCountReferenceBases(), deepThought.getCountPersons(), deepThought.getCountFiles());
  }

  protected float calculateEntitiesWeight(int countEntries, int countTags, int countCategories, int countReferenceBases, int countPersons, int countFiles) {
    return 2.5f * countEntries + 1.0f * countTags + 1.0f * countCategories + 1.5f * countReferenceBases + 1.0f * countPersons + 1.0f * countFiles;
  }

}

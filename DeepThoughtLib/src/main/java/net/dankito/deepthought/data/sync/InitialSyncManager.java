package net.dankito.deepthought.data.sync;

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
import net.dankito.deepthought.data.model.enums.FileType;
import net.dankito.deepthought.data.model.enums.Language;
import net.dankito.deepthought.data.model.enums.NoteType;
import net.dankito.deepthought.data.persistence.IEntityManager;
import net.dankito.deepthought.util.StringUtils;
import net.dankito.deepthought.util.localization.Localization;

/**
 * Created by ganymed on 07/09/16.
 */
public class InitialSyncManager {


  protected IEntityManager entityManager;


  public InitialSyncManager(IEntityManager entityManager) {
    this.entityManager = entityManager;
  }


  public void syncLocalDatabaseIdsWithRemoteOnes(DeepThought localDeepThought, User loggedOnUser, Device localDevice, AskForDeviceRegistrationRequest request) {
    syncLocalDatabaseIdsWithRemoteOnes(localDeepThought, loggedOnUser, localDevice, request.getCurrentDeepThoughtInfo(), request.getUser(), request.getDevice(), request.getGroup());
  }

  public void syncLocalDatabaseIdsWithRemoteOnes(DeepThought localDeepThought, User loggedOnUser, Device localDevice, DeepThoughtInfo remoteDeepThought,
                                                 UserInfo remoteUser, HostInfo remoteDevice, GroupInfo remoteUserDefaultGroup) {
    entityManager.deleteEntity(loggedOnUser);

    synchronizeUser(loggedOnUser, remoteUser);
    // TODO: find a better solution then isAlreadyPersisted() in CouchbaseLiteEntityManager.Dao.persist()
    entityManager.persistEntity(loggedOnUser);

    Group userDefaultGroup = loggedOnUser.getUsersDefaultGroup();
    entityManager.deleteEntity(userDefaultGroup);

    synchronizeGroup(userDefaultGroup, remoteUserDefaultGroup);
    entityManager.persistEntity(userDefaultGroup);

    entityManager.deleteEntity(localDeepThought);

    localDeepThought.setId(remoteDeepThought.getDatabaseId());
    entityManager.persistEntity(localDeepThought);

    updateAllUserDataEntities(loggedOnUser, entityManager);
  }

  protected void synchronizeUser(User loggedOnUser, UserInfo remoteUser) {
    loggedOnUser.setId(remoteUser.getDatabaseId());
    loggedOnUser.setUniversallyUniqueId(remoteUser.getUniversallyUniqueId());
    loggedOnUser.setUserName(remoteUser.getUserName());
    loggedOnUser.setFirstName(remoteUser.getFirstName());
    loggedOnUser.setLastName(remoteUser.getLastName());
  }

  protected void synchronizeGroup(Group userDefaultGroup, GroupInfo remoteUserDefaultGroup) {
    userDefaultGroup.setId(remoteUserDefaultGroup.getDatabaseId());
    userDefaultGroup.setUniversallyUniqueId(remoteUserDefaultGroup.getUniversallyUniqueId());
    userDefaultGroup.setName(remoteUserDefaultGroup.getName());
    userDefaultGroup.setDescription(remoteUserDefaultGroup.getDescription());
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

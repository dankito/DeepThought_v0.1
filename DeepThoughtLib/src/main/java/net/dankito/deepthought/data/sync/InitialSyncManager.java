package net.dankito.deepthought.data.sync;

import net.dankito.deepthought.communication.model.DeepThoughtInfo;
import net.dankito.deepthought.communication.model.HostInfo;
import net.dankito.deepthought.communication.model.UserInfo;
import net.dankito.deepthought.data.model.DeepThought;
import net.dankito.deepthought.data.model.Device;
import net.dankito.deepthought.data.model.User;
import net.dankito.deepthought.util.StringUtils;
import net.dankito.deepthought.util.localization.Localization;

/**
 * Created by ganymed on 07/09/16.
 */
public class InitialSyncManager {

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

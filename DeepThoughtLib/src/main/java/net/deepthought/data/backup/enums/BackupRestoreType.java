package net.deepthought.data.backup.enums;

import net.deepthought.util.Localization;

/**
 * Created by ganymed on 03/01/15.
 */
public enum BackupRestoreType {

  TryToMergeWithExistingData,
  AddAsNewToExistingData,
  ReplaceExistingDataCollection,
  TryToMergeWithExistingDataAndReplaceExistingDataCollectionOnFailure;


  @Override
  public String toString() {
    switch(this) {
      case AddAsNewToExistingData:
        return Localization.getLocalizedString("restore.type.add.as.new.to.existing.data");
      case ReplaceExistingDataCollection:
        return Localization.getLocalizedString("restore.type.replace.existing.data");
      case TryToMergeWithExistingDataAndReplaceExistingDataCollectionOnFailure:
        return Localization.getLocalizedString("restore.type.try.to.merge.with.existing.data.and.replace.on.failure");
      default:
        return Localization.getLocalizedString("restore.type.try.to.merge.with.existing.data");
    }
  }
}

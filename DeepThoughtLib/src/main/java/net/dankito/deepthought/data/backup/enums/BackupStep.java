package net.dankito.deepthought.data.backup.enums;

/**
 * Created by ganymed on 01/01/15.
 */
public enum BackupStep {

  ReadingBackupFileFromFileSystem,

  DeserializeBackedUpDeepThought,

  SelectEntitiesToRestore,

  DeleteExistingDataCollection,

  CopyBackupFileToDataFolder,

  InsertEntitiesIntoDatabase,
  InsertEntityIntoDatabase,

  Abort,
  Done;


}

package net.deepthought.data.backup.enums;

/**
 * Created by ganymed on 01/01/15.
 */
public enum CreateBackupResult {

  CouldNotCreateBackupFolder,
  CouldNotCreateBackupFile,

  CouldNotSerializeDeepThought,
  CouldNotWriteSerializedDeepThoughtToFile,

  CouldNotCopyDatabaseFile,

  Successful

}

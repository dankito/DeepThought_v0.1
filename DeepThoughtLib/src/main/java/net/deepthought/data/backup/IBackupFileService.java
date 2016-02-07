package net.deepthought.data.backup;

import net.deepthought.data.model.enums.BackupFileServiceType;

import java.util.List;

/**
 * Created by ganymed on 05/01/15.
 */
public interface IBackupFileService {

  public BackupFileServiceType getFileServiceType();

//  public String getFileTypeKey();

  public String getFileTypeFileExtension();

  public void createBackup(CreateBackupParams params);

  public List<BackupFile> getAvailableBackupsForThisType();

  public  void restoreBackup(RestoreBackupParams params);

}

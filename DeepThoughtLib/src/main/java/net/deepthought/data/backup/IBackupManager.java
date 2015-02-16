package net.deepthought.data.backup;

import net.deepthought.data.model.enums.BackupFileServiceType;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Created by ganymed on 05/01/15.
 */
public interface IBackupManager {

  public void createBackupsForAllRegisteredBackupFileServices();

  public Collection<IBackupFileService> getAvailableBackupFileServices();

  public List<BackupFile> getAllAvailableBackups();

  public List<BackupFile> getAvailableBackupsForFileType(BackupFileServiceType fileServiceType);

  public void restoreBackupAsync(RestoreBackupParams params);
  public void restoreBackup(RestoreBackupParams params);

  public boolean copyDataCollectionAndBackupFileToRestoredBackupsFolder(RestoreBackupParams params);

  public Date getBackupTimeFromFileName(File backupFile);
  public Date getBackupTimeFromFileName(String backupFileName);

  public File getBackupsFolder();
  public String getBackupsFolderPath();
  public File getRestoredBackupsFolder();


  public final static DateFormat BackupDateFormat = new SimpleDateFormat("yyyy.MM.dd_HH-mm-ss");
  public final static Date CouldNotExtractBackupTimeFromFileName = new Date(-1);


  public final static IBackupFileService AllBackupsFileService = new AbstractBackupFileService("backup.file.service.type.all") {
    @Override
    protected String createBackupFileName(CreateBackupParams params) {
      return null;
    }

    @Override
    public String getFileTypeFileExtension() {
      return null;
    }

    @Override
    public void createBackup(CreateBackupParams params) {

    }

    @Override
    public void restoreBackup(RestoreBackupParams params) {

    }
  };

}

package net.deepthought.data.backup;

import net.deepthought.Application;
import net.deepthought.data.model.enums.BackupFileServiceType;
import net.deepthought.util.localization.Localization;

import java.io.File;
import java.text.DateFormat;
import java.util.Date;

/**
 * Created by ganymed on 01/01/15.
 */
public class BackupFile {


  protected BackupFileServiceType fileServiceType;

  protected File file;

  protected Date backupTime;


  public BackupFile(String filePath, BackupFileServiceType backupFileServiceType) {
    this(new File(filePath), backupFileServiceType);
  }

  public BackupFile(File backupFile, BackupFileServiceType backupFileServiceType) {
    this.file = backupFile;
    this.fileServiceType = backupFileServiceType;

    try {
      this.backupTime = Application.getBackupManager().getBackupTimeFromFileName(file);
      if(backupTime == IBackupManager.CouldNotExtractBackupTimeFromFileName)
        this.backupTime = new Date(file.lastModified());
    } catch(Exception ex) {
      this.backupTime = new Date(file.lastModified());
    }
  }


  public BackupFileServiceType getFileServiceType() {
    return fileServiceType;
  }

  public File getFile() {
    return file;
  }

  public String getFileName() {
    return file.getName();
  }

  public String getFilePath() {
    return file.getAbsolutePath();
  }

  public Date getBackupTime() {
    return backupTime;
  }

  public String getBackupTimeLocalized() {
    return DateFormat.getDateTimeInstance(DateFormat.LONG, 0, Localization.getLanguageLocale()).format(backupTime);
  }

  public long getFileSize() {
    return file.length();
  }


  @Override
  public String toString() {
    return getFileName();
  }

}

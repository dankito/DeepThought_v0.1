package net.deepthought.data.backup;

import net.deepthought.Application;
import net.deepthought.data.backup.listener.CreateBackupListener;
import net.deepthought.data.model.DeepThoughtApplication;

/**
 * Created by ganymed on 05/01/15.
 */
public class CreateBackupParams {

  protected DeepThoughtApplication application;

  protected String backupBaseFolder;

  protected String destinationFileName;

  protected CreateBackupListener listener;


  public CreateBackupParams(DeepThoughtApplication application) {
    this(application, null);
  }

  public CreateBackupParams(DeepThoughtApplication application, CreateBackupListener listener) {
    this(application, Application.getBackupManager().getBackupsFolderPath(), listener);
  }

  public CreateBackupParams(DeepThoughtApplication application, String backupBaseFolder, CreateBackupListener listener) {
    this.application = application;
    this.backupBaseFolder = backupBaseFolder;
    this.listener = listener;
  }

  public CreateBackupParams(DeepThoughtApplication application, String backupBaseFolder, String destinationFileName, CreateBackupListener listener) {
    this(application, backupBaseFolder, listener);
    this.destinationFileName = destinationFileName;
  }


  public DeepThoughtApplication getApplication() {
    return application;
  }

  public String getBackupBaseFolder() {
    return backupBaseFolder;
  }

  public String getDestinationFileName() {
    return destinationFileName;
  }

  public CreateBackupListener getListener() {
    return listener;
  }

}

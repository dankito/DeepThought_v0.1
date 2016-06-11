package net.dankito.deepthought.data.backup;

import net.dankito.deepthought.data.backup.enums.BackupStep;
import net.dankito.deepthought.util.DeepThoughtError;

/**
 * Created by ganymed on 06/01/15.
 */
public class RestoreBackupStepResult {

  protected BackupFile file;

  protected BackupStep stepDone;

  protected boolean successful;

  protected DeepThoughtError error;

  protected BackupStep nextStep;

  protected Object stepResultData;


  public RestoreBackupStepResult(BackupFile file, BackupStep stepDone, DeepThoughtError error, BackupStep nextStep) {
    this.file = file;
    this.stepDone = stepDone;
    this.successful = error == DeepThoughtError.Success;
    this.error = error;
    this.nextStep = nextStep;
  }

  public RestoreBackupStepResult(BackupFile file, BackupStep stepDone, DeepThoughtError error, BackupStep nextStep, Object stepResultData) {
    this(file, stepDone, error, nextStep);
    this.stepResultData = stepResultData;
  }

  public RestoreBackupStepResult(BackupFile file, BackupStep stepDone, boolean successful, BackupStep nextStep) {
    this.file = file;
    this.stepDone = stepDone;
    this.successful = successful;
    this.error = DeepThoughtError.Success;
    this.nextStep = nextStep;
  }

  public RestoreBackupStepResult(BackupFile file, BackupStep stepDone, boolean successful, DeepThoughtError error, BackupStep nextStep) {
    this(file, stepDone, error, nextStep);
    this.successful = successful;
  }

  public RestoreBackupStepResult(BackupFile file, BackupStep stepDone, boolean successful, BackupStep nextStep, Object stepResultData) {
    this(file, stepDone, successful, nextStep);
    this.stepResultData = stepResultData;
  }


  public BackupFile getFile() {
    return file;
  }

  public BackupStep getStepDone() {
    return stepDone;
  }

  public boolean successful() {
    return successful;
  }

  public DeepThoughtError getError() {
    return error;
  }

  public BackupStep getNextStep() {
    return nextStep;
  }

  public boolean hasStepResultData() {
    return stepResultData != null;
  }

  public Object getStepResultData() {
    return stepResultData;
  }

  @Override
  public String toString() {
    String description = stepDone + " Successful? " + successful + "; " + file.getFilePath();

    if(successful == false)
      description += error;

    return description;
  }


}

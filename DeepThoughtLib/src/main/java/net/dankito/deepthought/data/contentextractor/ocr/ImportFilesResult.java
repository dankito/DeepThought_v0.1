package net.dankito.deepthought.data.contentextractor.ocr;

import net.dankito.deepthought.util.file.FileUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Created by ganymed on 18/08/15.
 */
public class ImportFilesResult {

  private final static Logger log = LoggerFactory.getLogger(ImportFilesResult.class);

  protected boolean isUserCancelled = false;

  protected String errorMessage = null;

  protected boolean successful = false;

  protected boolean isDone = false;

  protected byte[] fileData = null;

  protected String fileUri = null;


  public ImportFilesResult() {

  }

  public ImportFilesResult(boolean successful) {
    this.successful = successful;
  }

  public ImportFilesResult(byte[] fileData) {
    this(true);

    this.fileData = fileData;
  }

  public ImportFilesResult(byte[] fileData, boolean isDone) {
    this(fileData);
    this.isDone = isDone;
  }


  public boolean isUserCancelled() {
    return isUserCancelled;
  }

  public void setIsUserCancelled(boolean isUserCancelled) {
    this.isUserCancelled = isUserCancelled;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  public boolean successful() {
    return successful;
  }

  public boolean isDone() {
    return isDone;
  }

  public void setIsDone(boolean isDone) {
    this.isDone = isDone;
  }

  public byte[] getFileData() {
    if(fileData == null && fileUri != null) {
      try {
        fileData = FileUtils.readFile(new File(fileUri));
      } catch(Exception ex) { log.error("Could not read File Data from Uri " + fileUri, ex); }
    }
    return fileData;
  }

  public void setFileData(byte[] fileData) {
    this.fileData = fileData;
  }

  public String getFileUri() {
    return fileUri;
  }

  public void setFileUri(String fileUri) {
    this.fileUri = fileUri;
  }


  @Override
  public String toString() {
    if(isUserCancelled())
      return "User cancelled";
    else if(isDone())
      return "Done";
    else
      return "Successfully imported File";
  }


  public static ImportFilesResult createRecognitionProcessDoneResult() {
    ImportFilesResult result = new ImportFilesResult();
    result.setIsDone(true);

    return result;
  }

  public static ImportFilesResult createUserCancelledResult() {
    ImportFilesResult result = new ImportFilesResult();
    result.setIsUserCancelled(true);

    return result;
  }

  public static ImportFilesResult createCapturingSuccessfulResult(byte[] fileData) {
    ImportFilesResult result = new ImportFilesResult(fileData);

    return result;
  }

  public static ImportFilesResult createCapturingSuccessfulResult(byte[] fileData, boolean isDone) {
    ImportFilesResult result = new ImportFilesResult(fileData);
    result.setIsDone(isDone);

    return result;
  }

}

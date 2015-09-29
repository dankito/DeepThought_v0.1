package net.deepthought.data.contentextractor.ocr;

import net.deepthought.Application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ganymed on 18/08/15.
 */
public class CaptureImageResult {

  private final static Logger log = LoggerFactory.getLogger(CaptureImageResult.class);

  protected boolean isUserCancelled = false;

  protected String errorMessage = null;

  protected boolean successful = false;

  protected boolean isDone = false;

  protected byte[] imageData = null;

  protected String encodedImageData = null;


  public CaptureImageResult() {

  }

  public CaptureImageResult(byte[] imageData) {
    if(Application.getPlatformTools() != null) // for sending over the wire Base64 encode data. The user doesn't know this, externally she/he sees only the byte array
      this.encodedImageData = Application.getPlatformTools().base64EncodeByteArray(imageData);
    else
      this.imageData = imageData;

    this.successful = true;
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

  public byte[] getImageData() {
    if(imageData == null && encodedImageData != null && Application.getPlatformTools() != null) {
      log.debug("Decoding Base64 Image Data ...");
      imageData = Application.getPlatformTools().base64DecodeByteArray(encodedImageData);
      log.debug("Decoding done");
    }

    return imageData;
  }

  public void setImageData(byte[] imageData) {
    this.imageData = imageData;
  }


  @Override
  public String toString() {
    if(isUserCancelled())
      return "User cancelled";
    else if(isDone())
      return "Done";
    else
      return "Successfully captured Image";
  }


  public static CaptureImageResult createRecognitionProcessDoneResult() {
    CaptureImageResult result = new CaptureImageResult();
    result.setIsDone(true);

    return result;
  }

  public static CaptureImageResult createUserCancelledResult() {
    CaptureImageResult result = new CaptureImageResult();
    result.setIsUserCancelled(true);

    return result;
  }

  public static CaptureImageResult createCapturingSuccessfulResult(byte[] imageData) {
    CaptureImageResult result = new CaptureImageResult(imageData);

    return result;
  }

  public static CaptureImageResult createCapturingSuccessfulResult(byte[] imageData, boolean isDone) {
    CaptureImageResult result = new CaptureImageResult(imageData);
    result.setIsDone(isDone);

    return result;
  }

}

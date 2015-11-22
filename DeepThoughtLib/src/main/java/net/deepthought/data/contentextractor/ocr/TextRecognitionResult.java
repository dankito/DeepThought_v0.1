package net.deepthought.data.contentextractor.ocr;

/**
 * Created by ganymed on 18/08/15.
 */
public class TextRecognitionResult {

  protected boolean isUserCancelled = false;

  protected boolean recognitionSuccessful = false;

  protected String errorMessage = null;

  protected boolean isDone = false;

  protected float accuracy = 0f;

  protected String recognizedText = null;


  public TextRecognitionResult() {

  }

  public TextRecognitionResult(String recognizedText) {
    this.recognizedText = recognizedText;
    this.recognitionSuccessful = recognizedText != null;
  }

  public TextRecognitionResult(String recognizedText, boolean isDone) {
    this(recognizedText);
    this.isDone = isDone;
  }

  public boolean isUserCancelled() {
    return isUserCancelled;
  }

  public void setIsUserCancelled(boolean isUserCancelled) {
    this.isUserCancelled = isUserCancelled;
  }

  public boolean recognitionSuccessful() {
    return recognitionSuccessful;
  }

  public void setRecognitionSuccessful(boolean recognitionSuccessful) {
    this.recognitionSuccessful = recognitionSuccessful;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  public boolean isDone() {
    return isDone;
  }

  public void setIsDone(boolean isDone) {
    this.isDone = isDone;
  }

  public float getAccuracy() {
    return accuracy;
  }

  public void setAccuracy(float accuracy) {
    this.accuracy = accuracy;
  }

  public String getRecognizedText() {
    return recognizedText;
  }

  public void setRecognizedText(String recognizedText) {
    this.recognizedText = recognizedText;
  }


  @Override
  public String toString() {
    if(isUserCancelled())
      return "User cancelled";
    else if(isDone())
      return "Done";
    else if(recognitionSuccessful() == false)
      return "Recognition failed: " + getErrorMessage();
    else
      return "Recognized text with " + getAccuracy() + " percent accuracy: " + getRecognizedText();
  }


  public static TextRecognitionResult createRecognitionProcessDoneResult() {
    TextRecognitionResult result = new TextRecognitionResult();
    result.setIsDone(true);

    return result;
  }

  public static TextRecognitionResult createUserCancelledResult() {
    TextRecognitionResult result = new TextRecognitionResult();
    result.setIsUserCancelled(true);

    return result;
  }

  public static TextRecognitionResult createErrorOccurredResult(String errorMessage) {
    TextRecognitionResult result = new TextRecognitionResult();
    result.setRecognitionSuccessful(false);
    result.setErrorMessage(errorMessage);

    return result;
  }

  public static TextRecognitionResult createRecognitionSuccessfulResult(String recognizedText) {
    TextRecognitionResult result = new TextRecognitionResult();
    result.setRecognitionSuccessful(true);
    result.setRecognizedText(recognizedText);
    result.setIsDone(true);

    return result;
  }

  public static TextRecognitionResult createRecognitionSuccessfulResult(String recognizedText, float accuracy) {
    TextRecognitionResult result = createRecognitionSuccessfulResult(recognizedText);
    result.setAccuracy(accuracy);

    return result;
  }

  public static TextRecognitionResult createRecognitionSuccessfulResult(String recognizedText, float accuracy, boolean isDone) {
    TextRecognitionResult result = createRecognitionSuccessfulResult(recognizedText, accuracy);
    result.setIsDone(isDone);

    return result;
  }

}

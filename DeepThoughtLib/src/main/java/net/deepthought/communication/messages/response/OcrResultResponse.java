package net.deepthought.communication.messages.response;

import net.deepthought.communication.messages.request.Request;
import net.deepthought.data.contentextractor.ocr.TextRecognitionResult;

/**
 * Created by ganymed on 23/08/15.
 */
public class OcrResultResponse extends Request implements ResponseToAsynchronousRequest {

  protected TextRecognitionResult textRecognitionResult;

  protected int requestMessageId;


  public OcrResultResponse(TextRecognitionResult textRecognitionResult, int requestMessageId) {
    this.textRecognitionResult = textRecognitionResult;
    this.requestMessageId = requestMessageId;
  }


  public TextRecognitionResult getTextRecognitionResult() {
    return textRecognitionResult;
  }


  @Override
  public String toString() {
    return "" + textRecognitionResult;
  }

  @Override
  public int getRequestMessageId() {
    return requestMessageId;
  }

  @Override
  public boolean isDone() {
    if(textRecognitionResult != null) {
      return textRecognitionResult.isDone();
    }

    return false;
  }
}

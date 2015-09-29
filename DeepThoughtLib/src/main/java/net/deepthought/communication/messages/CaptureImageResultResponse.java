package net.deepthought.communication.messages;

import net.deepthought.data.contentextractor.ocr.CaptureImageResult;

/**
 * Created by ganymed on 23/08/15.
 */
public class CaptureImageResultResponse extends Request {

  protected CaptureImageResult result;

  protected int messageId;


  public CaptureImageResultResponse(CaptureImageResult result, int messageId) {
    this.result = result;
    this.messageId = messageId;
  }


  public CaptureImageResult getResult() {
    return result;
  }

  public int getMessageId() {
    return messageId;
  }


}

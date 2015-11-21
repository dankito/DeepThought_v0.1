package net.deepthought.communication.messages.request;

/**
 * Created by ganymed on 23/08/15.
 */
public class StopCaptureImageOrDoOcrRequest extends Request {

  protected int messageId;


  public StopCaptureImageOrDoOcrRequest(int messageId) {
    this.messageId = messageId;
  }


  public int getMessageId() {
    return messageId;
  }

}

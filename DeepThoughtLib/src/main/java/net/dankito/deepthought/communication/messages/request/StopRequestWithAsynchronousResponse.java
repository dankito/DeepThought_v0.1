package net.dankito.deepthought.communication.messages.request;

/**
 * Created by ganymed on 23/08/15.
 */
public class StopRequestWithAsynchronousResponse extends Request {

  protected int requestMessageId;


  public StopRequestWithAsynchronousResponse(int requestMessageId) {
    this.requestMessageId = requestMessageId;
  }


  public int getRequestMessageId() {
    return requestMessageId;
  }

}

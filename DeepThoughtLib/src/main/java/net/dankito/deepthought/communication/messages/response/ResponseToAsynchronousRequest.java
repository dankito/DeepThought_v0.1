package net.dankito.deepthought.communication.messages.response;

/**
 * Created by ganymed on 21/11/15.
 */
public interface ResponseToAsynchronousRequest {

  int getRequestMessageId();

  boolean isDone();

}

package net.deepthought.communication;

import net.deepthought.communication.messages.Response;

/**
 * Created by ganymed on 23/08/15.
 */
public interface CommunicatorResponseListener {

  void responseReceived(Response communicatorResponse);

}

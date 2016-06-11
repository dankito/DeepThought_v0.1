package net.dankito.deepthought.communication;

import net.dankito.deepthought.communication.messages.response.Response;

/**
 * Created by ganymed on 23/08/15.
 */
public interface CommunicatorResponseListener {

  void responseReceived(Response communicatorResponse);

}

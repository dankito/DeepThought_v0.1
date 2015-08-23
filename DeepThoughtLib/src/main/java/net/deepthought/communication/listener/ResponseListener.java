package net.deepthought.communication.listener;

import net.deepthought.communication.messages.Request;
import net.deepthought.communication.messages.Response;

/**
 * Created by ganymed on 23/08/15.
 */
public interface ResponseListener {

  void responseReceived(Request request, Response response);

}

package net.deepthought.communication.listener;

import net.deepthought.communication.messages.request.Request;
import net.deepthought.communication.messages.response.Response;

/**
 * Created by ganymed on 23/08/15.
 */
public interface ResponseListener {

  void responseReceived(Request request, Response response);

}

package net.dankito.deepthought.communication.listener;

import net.dankito.deepthought.communication.messages.request.Request;
import net.dankito.deepthought.communication.messages.response.Response;

/**
 * Created by ganymed on 23/08/15.
 */
public interface ResponseListener {

  void responseReceived(Request request, Response response);

}

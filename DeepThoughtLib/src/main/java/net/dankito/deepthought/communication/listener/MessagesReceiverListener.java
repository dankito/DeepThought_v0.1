package net.dankito.deepthought.communication.listener;

import net.dankito.deepthought.communication.messages.request.Request;

/**
 * Created by ganymed on 21/08/15.
 */
public interface MessagesReceiverListener {

  boolean messageReceived(String methodName, Request request);

}

package net.deepthought.communication.listener;

import net.deepthought.communication.messages.request.Request;

/**
 * Created by ganymed on 21/08/15.
 */
public interface MessagesReceiverListener {

  boolean messageReceived(String methodName, Request request);

}

package net.deepthought.communication;

import net.deepthought.communication.messages.AsynchronousResponseListenerManager;
import net.deepthought.communication.messages.IMessagesDispatcher;

/**
 * Created by ganymed on 22/11/15.
 */
public class CommunicatorConfig {

  protected IMessagesDispatcher dispatcher;

  protected AsynchronousResponseListenerManager listenerManager;

  protected int messageReceiverPort;

  public CommunicatorConfig(IMessagesDispatcher dispatcher, AsynchronousResponseListenerManager listenerManager, int messageReceiverPort) {
    this.dispatcher = dispatcher;
    this.listenerManager = listenerManager;
    this.messageReceiverPort = messageReceiverPort;
  }


  public IMessagesDispatcher getDispatcher() {
    return dispatcher;
  }

  public AsynchronousResponseListenerManager getListenerManager() {
    return listenerManager;
  }

  public int getMessageReceiverPort() {
    return messageReceiverPort;
  }

}

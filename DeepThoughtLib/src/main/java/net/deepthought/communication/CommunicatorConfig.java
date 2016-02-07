package net.deepthought.communication;

import net.deepthought.communication.messages.AsynchronousResponseListenerManager;
import net.deepthought.communication.messages.IMessagesDispatcher;
import net.deepthought.communication.registration.IRegisteredDevicesManager;

/**
 * Created by ganymed on 22/11/15.
 */
public class CommunicatorConfig {

  protected IMessagesDispatcher dispatcher;

  protected AsynchronousResponseListenerManager listenerManager;

  protected int messageReceiverPort;

  protected ConnectorMessagesCreator connectorMessagesCreator;

  protected IRegisteredDevicesManager registeredDevicesManager;

  public CommunicatorConfig(IMessagesDispatcher dispatcher, AsynchronousResponseListenerManager listenerManager, int messageReceiverPort,
                            ConnectorMessagesCreator connectorMessagesCreator, IRegisteredDevicesManager registeredDevicesManager) {
    this.dispatcher = dispatcher;
    this.listenerManager = listenerManager;
    this.messageReceiverPort = messageReceiverPort;
    this.connectorMessagesCreator = connectorMessagesCreator;
    this.registeredDevicesManager = registeredDevicesManager;
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

  public ConnectorMessagesCreator getConnectorMessagesCreator() {
    return connectorMessagesCreator;
  }

  public IRegisteredDevicesManager getRegisteredDevicesManager() {
    return registeredDevicesManager;
  }

}

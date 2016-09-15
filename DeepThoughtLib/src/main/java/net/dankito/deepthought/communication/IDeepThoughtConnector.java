package net.dankito.deepthought.communication;

import net.dankito.deepthought.communication.connected_device.ConnectedDevicesManager;
import net.dankito.deepthought.communication.connected_device.IConnectedRegisteredDevicesListenerManager;
import net.dankito.deepthought.communication.listener.ImportFilesOrDoOcrListener;
import net.dankito.deepthought.communication.listener.MessagesReceiverListener;
import net.dankito.deepthought.communication.registration.IUnregisteredDevicesListener;
import net.dankito.deepthought.communication.registration.RegisteredDevicesManager;

/**
 * Created by ganymed on 20/08/15.
 */
public interface IDeepThoughtConnector extends IConnectedRegisteredDevicesListenerManager {

  void runAsync();

  void shutDown();

  boolean addUnregisteredDevicesListener(IUnregisteredDevicesListener listener);
  boolean removeUnregisteredDevicesListener(IUnregisteredDevicesListener listener);

  boolean isStarted();

  int getMessageReceiverPort();

  Communicator getCommunicator();

  ConnectorMessagesCreator getMessagesCreator();

  RegisteredDevicesManager getRegisteredDevicesManager();

  ConnectedDevicesManager getConnectedDevicesManager();

  boolean addImportFilesOrDoOcrListener(ImportFilesOrDoOcrListener listener);
  boolean removeImportFilesOrDoOcrListener(ImportFilesOrDoOcrListener listener);

  boolean addMessagesReceiverListener(MessagesReceiverListener listener);
  boolean removeMessagesReceiverListener(MessagesReceiverListener listener);

}

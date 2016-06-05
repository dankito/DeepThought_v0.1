package net.deepthought.communication;

import net.deepthought.communication.connected_device.ConnectedDevicesManager;
import net.deepthought.communication.connected_device.IConnectedDevicesListener;
import net.deepthought.communication.listener.ImportFilesOrDoOcrListener;
import net.deepthought.communication.listener.MessagesReceiverListener;
import net.deepthought.communication.registration.IUnregisteredDevicesListener;
import net.deepthought.communication.registration.RegisteredDevicesManager;

/**
 * Created by ganymed on 20/08/15.
 */
public interface IDeepThoughtConnector {

  void runAsync();

  void shutDown();

  boolean addUnregisteredDevicesListener(IUnregisteredDevicesListener listener);
  boolean removeUnregisteredDevicesListener(IUnregisteredDevicesListener listener);

  boolean isStarted();

  int getMessageReceiverPort();

  Communicator getCommunicator();

  RegisteredDevicesManager getRegisteredDevicesManager();

  ConnectedDevicesManager getConnectedDevicesManager();

  boolean addConnectedDevicesListener(IConnectedDevicesListener listener);
  boolean removeConnectedDevicesListener(IConnectedDevicesListener listener);

  boolean addImportFilesOrDoOcrListener(ImportFilesOrDoOcrListener listener);
  boolean removeImportFilesOrDoOcrListener(ImportFilesOrDoOcrListener listener);

  boolean addMessagesReceiverListener(MessagesReceiverListener listener);
  boolean removeMessagesReceiverListener(MessagesReceiverListener listener);

}

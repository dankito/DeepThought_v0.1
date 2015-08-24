package net.deepthought.communication;

import net.deepthought.communication.connected_device.ConnectedDevicesManager;
import net.deepthought.communication.listener.CaptureImageOrDoOcrListener;
import net.deepthought.communication.listener.ConnectedDevicesListener;
import net.deepthought.communication.listener.MessagesReceiverListener;
import net.deepthought.communication.registration.RegisteredDevicesManager;
import net.deepthought.communication.registration.RegistrationRequestListener;
import net.deepthought.communication.registration.UserDeviceRegistrationRequestListener;

/**
 * Created by ganymed on 20/08/15.
 */
public interface IDeepThoughtsConnector {

  void runAsync();

  void shutDown();

  void openUserDeviceRegistrationServer(UserDeviceRegistrationRequestListener listener);
  void closeUserDeviceRegistrationServer();

  void findOtherUserDevicesToRegisterAtAsync(RegistrationRequestListener listener);
  void stopSearchingOtherUserDevicesToRegisterAt();

  boolean isStarted();

  boolean isRegistrationServerRunning();
  boolean isSearchRegistrationServersClientRunning();

  boolean isRegisteredDevicesSearcherRunning();

  boolean isConnectionWatcherRunning();

  int getMessageReceiverPort();

  Communicator getCommunicator();

  RegisteredDevicesManager getRegisteredDevicesManager();

  ConnectedDevicesManager getConnectedDevicesManager();

  boolean addConnectedDevicesListener(ConnectedDevicesListener listener);
  boolean removeConnectedDevicesListener(ConnectedDevicesListener listener);

  boolean addCaptureImageOrDoOcrListener(CaptureImageOrDoOcrListener listener);
  boolean removeCaptureImageOrDoOcrListener(CaptureImageOrDoOcrListener listener);

  boolean addMessagesReceiverListener(MessagesReceiverListener listener);
  boolean removeMessagesReceiverListener(MessagesReceiverListener listener);

}

package net.deepthought.communication;

import net.deepthought.communication.registration.RegisteredPeersManager;
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

  int getMessageReceiverPort();

  Communicator getCommunicator();

  RegisteredPeersManager getRegisteredPeersManager();

  boolean isStarted();

}

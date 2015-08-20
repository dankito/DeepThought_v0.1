package net.deepthought.communication;

import net.deepthought.communication.model.HostInfo;

/**
 * Created by ganymed on 20/08/15.
 */
public interface IDeepThoughtsConnector {

  void runAsync();

  void shutDown();

  boolean isDeviceRegistered(HostInfo info);

  int getMessageReceiverPort();

  Communicator getCommunicator();

  boolean isStarted();

}

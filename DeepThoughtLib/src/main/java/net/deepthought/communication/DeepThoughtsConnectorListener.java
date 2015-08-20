package net.deepthought.communication;

import net.deepthought.communication.messages.AskForDeviceRegistrationRequest;
import net.deepthought.communication.model.AllowDeviceToRegisterResult;
import net.deepthought.communication.model.ConnectedPeer;

/**
 * Created by ganymed on 20/08/15.
 */
public interface DeepThoughtsConnectorListener {

  AllowDeviceToRegisterResult registerDeviceRequestRetrieved(AskForDeviceRegistrationRequest request);

  void registeredDeviceConnected(ConnectedPeer peer);

  void registeredDeviceDisconnected(ConnectedPeer peer);

}

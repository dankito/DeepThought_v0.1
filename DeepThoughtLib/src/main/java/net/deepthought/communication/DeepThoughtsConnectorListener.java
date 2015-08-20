package net.deepthought.communication;

import net.deepthought.communication.model.ConnectedPeer;

/**
 * Created by ganymed on 20/08/15.
 */
public interface DeepThoughtsConnectorListener {

  void registeredDeviceConnected(ConnectedPeer peer);

  void registeredDeviceDisconnected(ConnectedPeer peer);

}

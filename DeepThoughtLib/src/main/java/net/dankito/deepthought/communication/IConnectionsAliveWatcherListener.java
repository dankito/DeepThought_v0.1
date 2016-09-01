package net.dankito.deepthought.communication;

import net.dankito.deepthought.communication.model.ConnectedDevice;

/**
 * Created by ganymed on 01/09/16.
 */
public interface IConnectionsAliveWatcherListener {

  void deviceDisconnected(ConnectedDevice device);

}

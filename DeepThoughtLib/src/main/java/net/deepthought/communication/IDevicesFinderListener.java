package net.deepthought.communication;

import net.deepthought.communication.model.HostInfo;

/**
 * Created by ganymed on 05/06/16.
 */
public interface IDevicesFinderListener {

  void deviceFound(HostInfo device);

  void deviceDisconnected(HostInfo device);

}

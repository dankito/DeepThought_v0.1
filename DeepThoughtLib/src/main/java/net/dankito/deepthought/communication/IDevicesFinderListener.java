package net.dankito.deepthought.communication;

import net.dankito.deepthought.communication.model.HostInfo;

/**
 * Created by ganymed on 05/06/16.
 */
public interface IDevicesFinderListener {

  void deviceFound(HostInfo device);

  void deviceDisconnected(HostInfo device);

}

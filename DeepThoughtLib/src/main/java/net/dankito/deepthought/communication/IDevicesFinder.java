package net.dankito.deepthought.communication;

import net.dankito.deepthought.communication.model.HostInfo;

/**
 * Created by ganymed on 05/06/16.
 */
public interface IDevicesFinder {

  boolean isRunning();

  void startAsync(HostInfo localHost, int searchDevicesPort, IDevicesFinderListener listener);

  void stop();

}

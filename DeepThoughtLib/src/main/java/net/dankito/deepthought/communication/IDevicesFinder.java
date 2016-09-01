package net.dankito.deepthought.communication;

import net.dankito.deepthought.communication.model.ConnectedDevice;
import net.dankito.deepthought.communication.model.HostInfo;

/**
 * Created by ganymed on 05/06/16.
 */
public interface IDevicesFinder {

  boolean isRunning();

  void startAsync(HostInfo localHost, int searchDevicesPort, IDevicesFinderListener listener);

  void stop();

  /**
   * As there are as well other ways of being notified of Device disconnection, inform DevicesFinder so it doesn't think it's still connected
   * and therefore can judge device correctly as re-connected on next received message
   * @param device
   */
  void disconnectedFromDevice(ConnectedDevice device);

}

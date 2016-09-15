package net.dankito.deepthought.communication.connected_device;

import net.dankito.deepthought.communication.IDevicesFinderListener;

/**
 * Created by ganymed on 05/09/16.
 */
public interface IDevicesFinderListenerManager {

  boolean addDevicesFinderListener(IDevicesFinderListener listener);

  boolean removeDevicesFinderListener(IDevicesFinderListener listener);

}

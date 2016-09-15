package net.dankito.deepthought.data.sync.helper;

import net.dankito.deepthought.communication.IDevicesFinderListener;
import net.dankito.deepthought.communication.connected_device.IDevicesFinderListenerManager;

/**
 * Created by ganymed on 15/09/16.
 */
public class TestDevicesFinderListenerManager implements IDevicesFinderListenerManager {

  @Override
  public boolean addDevicesFinderListener(IDevicesFinderListener listener) {
    return false;
  }

  @Override
  public boolean removeDevicesFinderListener(IDevicesFinderListener listener) {
    return false;
  }

}

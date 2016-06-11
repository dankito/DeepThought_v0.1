package net.dankito.deepthought.communication;

/**
 * Created by ganymed on 05/06/16.
 */
public interface IDevicesFinder {

  void startAsync(IDevicesFinderListener listener);

  void stop();

}

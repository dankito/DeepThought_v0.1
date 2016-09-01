package net.dankito.deepthought.communication;

import net.dankito.deepthought.communication.connected_device.ConnectedDevicesManager;
import net.dankito.deepthought.communication.connected_device.UdpDevicesSearcher;
import net.dankito.deepthought.communication.model.HostInfo;
import net.dankito.deepthought.util.IThreadPool;

/**
 * Created by ganymed on 05/06/16.
 */
public class UdpDevicesFinder implements IDevicesFinder {

  protected IDevicesFinderListener listener = null;

  protected UdpDevicesSearcher udpDevicesSearcher;

  protected IThreadPool threadPool;

  protected ConnectedDevicesManager connectedDevicesManager;

  protected ConnectorMessagesCreator connectorMessagesCreator;


  public UdpDevicesFinder(IThreadPool threadPool, ConnectedDevicesManager connectedDevicesManager, ConnectorMessagesCreator connectorMessagesCreator) {
    this.threadPool = threadPool;
    this.connectedDevicesManager = connectedDevicesManager;
    this.connectorMessagesCreator = connectorMessagesCreator;
  }

  @Override
  public void startAsync(final HostInfo localHost, final int searchDevicesPort, final IDevicesFinderListener listener) {
    threadPool.runTaskAsync(new Runnable() {
      @Override
      public void run() {
        start(localHost, searchDevicesPort, listener);
      }
    });
  }

  protected void start(HostInfo localHost, int searchDevicesPort, IDevicesFinderListener listener) {
    this.listener = listener;

    startDevicesSearcher(localHost, searchDevicesPort, listener);
  }

  @Override
  public void stop() {
    stopDevicesSearcher();
  }


  protected void startDevicesSearcher(HostInfo localHost, int searchDevicesPort, IDevicesFinderListener listener) {
    stopDevicesSearcher();

    udpDevicesSearcher = new UdpDevicesSearcher(connectorMessagesCreator, connectedDevicesManager, threadPool);
    udpDevicesSearcher.startSearchingAsync(localHost, searchDevicesPort, listener);
  }

  protected void stopDevicesSearcher() {
    if(udpDevicesSearcher != null) {
      udpDevicesSearcher.stop();
      udpDevicesSearcher = null;
    }
  }


  public boolean isRegisteredDevicesSearcherRunning() {
    return udpDevicesSearcher != null;
  }

  @Override
  public boolean isRunning() {
    return isRegisteredDevicesSearcherRunning();
  }

}

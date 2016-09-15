package net.dankito.deepthought.communication;

import android.content.Context;
import android.net.wifi.WifiManager;

import net.dankito.deepthought.communication.model.HostInfo;
import net.dankito.deepthought.util.IThreadPool;

/**
 * Created by ganymed on 15/09/16.
 */
public class AndroidUdpDevicesFinder extends UdpDevicesFinder {

  protected final String MULTICAST_LOCK_NAME = "jmDNSDevicesFinder";


  protected WifiManager.MulticastLock multicastLock;

  protected Context context;


  public AndroidUdpDevicesFinder(Context context, IThreadPool threadPool) {
    super(threadPool);

    this.context = context;
  }


  @Override
  public void startAsync(HostInfo localHost, int searchDevicesPort, ConnectorMessagesCreator messagesCreator, IDevicesFinderListener listener) {
    acquireWifiLock();

    super.startAsync(localHost, searchDevicesPort, messagesCreator, listener);
  }

  @Override
  public void stop() {
    releaseWifiLock();

    super.stop();
  }


  /**
   * To improve battery life, processing of multicast packets is disabled by default on Android.
   * We can and must reenable this for the service discovery to work.
   * This is done programmatically by acquiring a lock in our activity.
   * (Explanation copied from http://home.heeere.com/tech-androidjmdns.html)
   */
  protected void acquireWifiLock() {
    WifiManager wifiManager = (WifiManager)context.getSystemService(android.content.Context.WIFI_SERVICE);
    multicastLock = wifiManager.createMulticastLock(MULTICAST_LOCK_NAME);
    multicastLock.setReferenceCounted(true);
    multicastLock.acquire();
  }

  protected void releaseWifiLock() {
    if (multicastLock != null && multicastLock.isHeld()) {
      multicastLock.release();
    }
    multicastLock = null;
  }

}

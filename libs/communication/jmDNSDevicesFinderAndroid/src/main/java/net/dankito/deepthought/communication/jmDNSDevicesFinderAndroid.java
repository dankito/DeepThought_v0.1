package net.dankito.deepthought.communication;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import net.dankito.deepthought.communication.model.HostInfo;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Most parts have been copied from https://github.com/alwx/android-jmdns/blob/master/LocalCommunication/src/main/java/me/alwx/localcommunication/connection/NetworkDiscovery.java
 *
 * TODO: When switch to minimum Android version of 4.1 and above, use build in NsdServiceInfo / NsdManager
 * (https://developer.android.com/training/connect-devices-wirelessly/nsd.html)
 *
 * Created by ganymed on 30/08/16.
 */
public class jmDNSDevicesFinderAndroid extends jmDNSDevicesFinderBase {

  protected WifiManager.MulticastLock multicastLock;

  protected Context context;


  public jmDNSDevicesFinderAndroid(Context context, HostInfo localHost) {
    super(localHost);
    this.context = context;
  }


  @Override
  protected void initJmDNS() throws IOException {
    super.initJmDNS();

    acquireWifiLock();
  }

  @Override
  public void stop() {
    super.stop();

    releaseWifiLock();
  }


  protected InetAddress getLocalNetworkIPAddress() throws UnknownHostException {
    WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    WifiInfo wifiInfo = wifi.getConnectionInfo();
    int intaddr = wifiInfo.getIpAddress();

    byte[] byteaddr = new byte[]{
        (byte) (intaddr & 0xff),
        (byte) (intaddr >> 8 & 0xff),
        (byte) (intaddr >> 16 & 0xff),
        (byte) (intaddr >> 24 & 0xff)
    };

    return InetAddress.getByAddress(byteaddr);
  }

  /**
   * To improve battery life, processing of multicast packets is disabled by default on Android.
   * We can and must reenable this for the service discovery to work.
   * This is done programmatically by acquiring a lock in our activity.
   * (Explanation copied from http://home.heeere.com/tech-androidjmdns.html)
   */
  protected void acquireWifiLock() {
    WifiManager wifiManager = (WifiManager) context.getSystemService(android.content.Context.WIFI_SERVICE);
    multicastLock = wifiManager.createMulticastLock(SERVICE_NAME);
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

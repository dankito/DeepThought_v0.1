package net.deepthought.communication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Inet4Address;
import java.net.InetAddress;

/**
 * Created by ganymed on 19/08/15.
 */
public class NetworkHelper {

  private final static Logger log = LoggerFactory.getLogger(NetworkHelper.class);


  public static InetAddress getBroadcastAddress() {
    try {
      InetAddress localHost = getHostIpAddress();
      byte[] broadcastAddress = localHost.getAddress();
      broadcastAddress[broadcastAddress.length - 1] = (byte)255;
      return Inet4Address.getByAddress(broadcastAddress);
    } catch(Exception ex) {
      log.error("Could not determine local network's Broadcast Address", ex);
    }

    return null;
  }

  public static InetAddress getHostIpAddress() {
    try {
      return Inet4Address.getLocalHost(); // TODO: test what is returned if network interfaces are turned off
    } catch(Exception ex) {
      log.error("Could not determine local network's IP Address", ex);
    }

    return null;
  }

  public static String getHostIpAddressString() {
    InetAddress localHost = getHostIpAddress();
    if(localHost != null)
      return localHost.getHostAddress();

    return null;
  }
}

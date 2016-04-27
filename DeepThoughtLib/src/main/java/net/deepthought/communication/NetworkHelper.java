package net.deepthought.communication;

import org.apache.http.conn.util.InetAddressUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by ganymed on 19/08/15.
 */
public class NetworkHelper {

  private final static Logger log = LoggerFactory.getLogger(NetworkHelper.class);


  public static List<InetAddress> getBroadcastAddresses() {
    List<InetAddress> broadcastAddresses = new ArrayList<>();

    for(InetAddress address : getIPAddresses(true)) { // in IPv6 there's no such thing as broadcast
      try {
        byte[] broadcastAddress = address.getAddress();
        broadcastAddress[broadcastAddress.length - 1] = (byte) 255;
        broadcastAddresses.add(Inet4Address.getByAddress(broadcastAddress));
      } catch (Exception ex) {
        log.error("Could not determine Broadcast Address of " + address.getHostAddress(), ex);
      }
    }

    return broadcastAddresses;
  }

  /**
   * Returns MAC address of the given interface name.
   * @param interfaceName eth0, wlan0 or NULL=use first interface
   * @return  mac address or empty string
   */
  public static String getMACAddress(String interfaceName) {
    try {
      List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
      for (NetworkInterface intf : interfaces) {
        if (interfaceName != null) {
          if (!intf.getName().equalsIgnoreCase(interfaceName)) continue;
        }
        byte[] mac = intf.getHardwareAddress();
        if (mac==null) return "";
        StringBuilder buf = new StringBuilder();
        for (int idx=0; idx<mac.length; idx++)
          buf.append(String.format("%02X:", mac[idx]));
        if (buf.length()>0) buf.deleteCharAt(buf.length()-1);
        return buf.toString();
      }
    } catch (Exception ex) { } // for now eat exceptions
    return "";
  }

  /**
   * Get IP address from first non-localhost interface
   * @param useIPv4  true=return ipv4, false=return ipv6
   * @return  address or empty string
   */
  public static InetAddress getIPAddress(boolean useIPv4) {
    try {
      List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
      for (NetworkInterface anInterface : interfaces) {
        List<InetAddress> addresses = Collections.list(anInterface.getInetAddresses());

        for (InetAddress address : addresses) {
          if (!address.isLoopbackAddress()) {
            String sAddr = address.getHostAddress().toUpperCase();
            boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);

            if (useIPv4 && isIPv4)
              return address;
            else if (useIPv4 == false && isIPv4 == false)
              return address;
          }
        }
      }
    } catch (Exception ex) { } // for now eat exceptions

    return null;
  }

  public static List<InetAddress> getIPAddresses(boolean onlyIPv4) {
    List<InetAddress> addresses = new ArrayList<>();

    try {
      List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
      for (NetworkInterface anInterface : interfaces) {
        List<InetAddress> interfaceAddresses = Collections.list(anInterface.getInetAddresses());

        for(InetAddress address : interfaceAddresses) {
          if(address.isLoopbackAddress() == false) {
            if(onlyIPv4 == false || address instanceof Inet4Address) {
              addresses.add(address);
            }
          }
        }
      }
    } catch (Exception ex) { } // for now eat exceptions

    return addresses;
  }

  // TODO: try to get rid of this method as it's not reliable (see above)
  public static String getIPAddressString(boolean useIPv4) {
    InetAddress address = getIPAddress(useIPv4);
    if(address != null) {
      String addressString = address.getHostAddress().toUpperCase();

      if (useIPv4)
        return addressString;
      else {
        int delim = addressString.indexOf('%'); // drop ip6 port suffix
        return delim < 0 ? addressString : addressString.substring(0, delim);
      }
    }

    return "";
  }


  public static boolean isSocketCloseException(Exception exception) {
    return exception instanceof SocketException && "Socket closed".equals(exception.getMessage());
  }

}

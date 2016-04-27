package net.deepthought.communication;

import org.junit.Assert;
import org.junit.Test;

import java.net.InetAddress;
import java.util.List;

/**
 * Created by ganymed on 19/08/15.
 */
public class NetworkHelperTest {

  @Test
  public void ensureGetBroadcastAddressReturnsCorrectAddress() {
    List<InetAddress> broadcastAddresses = NetworkHelper.getBroadcastAddresses();

    for(InetAddress broadcastAddress : broadcastAddresses) {
      byte[] address = broadcastAddress.getAddress();
      Assert.assertEquals((byte) 255, address[address.length - 1]); // Broadcast addresses end with 254
      Assert.assertNotEquals((byte) 0, address[0]); // address may not start with 0 (invalid address)
      Assert.assertNotEquals((byte) 127, address[0]); // or 127 (loopback address 127.0.0.1)
      Assert.assertTrue(address[0] == (byte) 192 || address[0] == (byte) 10); // local address start with 10.x.x.x or 192.168.x.x (or 17?...)
    }
  }

}

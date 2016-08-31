package net.dankito.deepthought.communicaton;

import net.dankito.deepthought.communication.jmDNSDevicesFinderBase;
import net.dankito.deepthought.communication.model.HostInfo;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class jmDNSDevicesFinderJava extends jmDNSDevicesFinderBase {


  public jmDNSDevicesFinderJava() {
    super();
  }


  @Override
  protected InetAddress getLocalNetworkIPAddress() throws UnknownHostException {
    return InetAddress.getLocalHost(); // TODO: return for each network interface
  }

}

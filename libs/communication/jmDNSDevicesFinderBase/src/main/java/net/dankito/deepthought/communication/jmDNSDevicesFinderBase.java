package net.dankito.deepthought.communication;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.communication.model.HostInfo;
import net.dankito.deepthought.util.IThreadPool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

public abstract class jmDNSDevicesFinderBase implements IDevicesFinder {

  protected final String SERVICE_TYPE = "_devicesfinder._deepthought._tcp.local.";

  protected static final String USER_ID_MAP_KEY = "userId";
  protected static final String USER_NAME_MAP_KEY = "userName";

  protected static final String DEVICE_ID_MAP_KEY = "deviceId";
  protected static final String DEVICE_NAME_MAP_KEY = "deviceName";

  protected static final String PLATFORM_MAP_KEY = "platform";
  protected static final String OS_VERSION_MAP_KEY = "osVersion";
  protected static final String PLATFORM_ARCHITECTURE_MAP_KEY = "platformArchitecture";
  protected static final String MESSAGES_PORT_MAP_KEY = "messagesPort";

  private static final Logger log = LoggerFactory.getLogger(jmDNSDevicesFinderBase.class);


  protected JmDNS jmDNS;
  protected ServiceInfo serviceInfo;
  protected ServiceListener serviceListener;

  protected InetAddress ipAddress;

  protected IThreadPool threadPool;

  protected Map<String, HostInfo> connectedDevices = new ConcurrentHashMap<>();


  public jmDNSDevicesFinderBase(IThreadPool threadPool) {
    this.threadPool = threadPool;
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

  protected void start(HostInfo localHost, int searchDevicesPort, final IDevicesFinderListener listener) {
    try {
      initJmDNS();

      localHost.setDeviceName(Application.getPlatformConfiguration().getDeviceName()); // TODO: remove again
      if(localHost.getDeviceName() == null) { // TODO: remove again
        localHost.setDeviceName("Manjaro");
      }
      serviceInfo = ServiceInfo.create(SERVICE_TYPE, localHost.getDeviceName(), searchDevicesPort, 1, 1, getHostInfoAsMap(localHost));
      jmDNS.registerService(serviceInfo);

      serviceListener = createServiceListener(listener);
      jmDNS.addServiceListener(SERVICE_TYPE, serviceListener);

    } catch (IOException e) {
      log.error("Error in JmDNS initialization: " + e);
    }
  }

  protected void initJmDNS() throws IOException {
    ipAddress = getLocalNetworkIPAddress();

    jmDNS = JmDNS.create(ipAddress);
  }

  protected abstract InetAddress getLocalNetworkIPAddress() throws UnknownHostException;


  protected ServiceListener createServiceListener(final IDevicesFinderListener listener) {
    return new ServiceListener() {
      @Override
      public void serviceAdded(ServiceEvent serviceEvent) {
        jmDNSDevicesFinderBase.this.serviceAdded(serviceEvent, listener);
      }

      @Override
      public void serviceRemoved(ServiceEvent serviceEvent) {
        jmDNSDevicesFinderBase.this.serviceRemoved(serviceEvent, listener);
      }

      @Override
      public void serviceResolved(ServiceEvent serviceEvent) {
        jmDNSDevicesFinderBase.this.serviceResolved(serviceEvent);
      }
    };
  }

  protected void serviceAdded(ServiceEvent serviceEvent, IDevicesFinderListener listener) {
    ServiceInfo info = jmDNS.getServiceInfo(serviceEvent.getType(), serviceEvent.getName());
    if(info == null || hasDeviceDiscoveredItself(info)) {
      return;
    }

    connectedToDevice(info, listener);
  }

  protected void serviceRemoved(ServiceEvent serviceEvent, IDevicesFinderListener listener) {
    ServiceInfo info = serviceEvent.getInfo();

    disconnectedFromDevice(info, listener);
  }

  protected void serviceResolved(ServiceEvent serviceEvent) {
    // TODO: remove again
    ServiceInfo info = serviceEvent.getInfo();
    HostInfo remoteHost = createHostInfo(info);
    log.info("Service resolved " + remoteHost);

    jmDNS.requestServiceInfo(serviceEvent.getType(), serviceEvent.getName(), 1);
  }

  @Override
  public void stop() {
    log.info("Stopping jmDNSDevicesFinder ...");

    if (jmDNS != null) {
      if (serviceListener != null) {
        jmDNS.removeServiceListener(SERVICE_TYPE, serviceListener);
        serviceListener = null;
      }

      jmDNS.unregisterAllServices();
      try {
        jmDNS.close();
      } catch(Exception e) {
        log.error("Could not close jmDNS", e);
      }

      jmDNS = null;
    }
  }


  protected void connectedToDevice(ServiceInfo info, IDevicesFinderListener listener) {
    HostInfo remoteHost = createHostInfo(info);
    log.info("Service added " + remoteHost); // TODO: remove again

    connectedDevices.put(getServiceKey(info), remoteHost);

    listener.deviceFound(remoteHost);
  }

  protected void disconnectedFromDevice(ServiceInfo info, IDevicesFinderListener listener) {
    HostInfo remoteHost = createHostInfo(info);
    log.info("Service removed " + remoteHost); // TODO: remove again

    if(connectedDevices.containsKey(getServiceKey(info))) {
      remoteHost = connectedDevices.remove(getServiceKey(info));
    }

    listener.deviceDisconnected(remoteHost);
  }

  protected String getServiceKey(ServiceInfo info) {
    return info.getName();
  }

  protected boolean hasDeviceDiscoveredItself(ServiceInfo info) {
    boolean discoveredItself = false;

    for(InetAddress address : info.getInet4Addresses()) {
      if(address.equals(ipAddress)) {
        discoveredItself = true;
        break;
      }
    }
    for(InetAddress address : info.getInet6Addresses()) {
      if(address.equals(ipAddress)) {
        discoveredItself = true;
        break;
      }
    }

    return discoveredItself;
  }


  protected Map<String, Object> getHostInfoAsMap(HostInfo hostInfo) {
    Map<String, Object> hostMap = new HashMap<>();

    hostMap.put(USER_ID_MAP_KEY, hostInfo.getUserUniqueId());
    hostMap.put(USER_NAME_MAP_KEY, hostInfo.getUserName());

    hostMap.put(DEVICE_ID_MAP_KEY, hostInfo.getDeviceId());
    hostMap.put(DEVICE_NAME_MAP_KEY, hostInfo.getDeviceName());

    hostMap.put(PLATFORM_MAP_KEY, hostInfo.getPlatform());
    hostMap.put(OS_VERSION_MAP_KEY, hostInfo.getOsVersion());
    hostMap.put(PLATFORM_ARCHITECTURE_MAP_KEY, hostInfo.getPlatformArchitecture());
    hostMap.put(MESSAGES_PORT_MAP_KEY, "" + Constants.MessageHandlerDefaultPort); // TODO: set real port

    return hostMap;
  }


  protected HostInfo createHostInfo(ServiceInfo info) {
    HostInfo remoteHost;
    if(info.getPropertyNames().hasMoreElements()) {
      remoteHost = getHostInfoFromServiceInfo(info);
    }
    else {
      remoteHost = new HostInfo();
    }

    if(info.getHostAddresses().length > 0) {
      remoteHost.setAddress(info.getHostAddresses()[0]);
    }
    return remoteHost;
  }

  protected HostInfo getHostInfoFromServiceInfo(ServiceInfo info) {
    Map<String, Object> hostMap = new HashMap<>();
    Enumeration<String> propertyNames = info.getPropertyNames();

    while(propertyNames.hasMoreElements()) {
      String propertyName = propertyNames.nextElement();
      hostMap.put(propertyName, info.getPropertyString(propertyName));
    }

    return getHostInfoFromMap(hostMap);
  }

  protected HostInfo getHostInfoFromMap(Map<String, Object> hostMap) {
    HostInfo host = new HostInfo();

    host.setUserUniqueId((String)hostMap.get(USER_ID_MAP_KEY));
    host.setUserName((String)hostMap.get(USER_NAME_MAP_KEY));

    host.setDeviceId((String)hostMap.get(DEVICE_ID_MAP_KEY));
    host.setDeviceName((String)hostMap.get(DEVICE_NAME_MAP_KEY));

    host.setPlatform((String)hostMap.get(PLATFORM_MAP_KEY));
    host.setOsVersion((String)hostMap.get(OS_VERSION_MAP_KEY));
    host.setPlatformArchitecture((String)hostMap.get(PLATFORM_ARCHITECTURE_MAP_KEY));

    Object port = hostMap.get(MESSAGES_PORT_MAP_KEY);
    if(port instanceof String) {
      port = Integer.parseInt((String)port);
    }
    host.setMessagesPort((int)port);

    return host;
  }


  @Override
  public boolean isRunning() {
    return jmDNS != null;
  }

}

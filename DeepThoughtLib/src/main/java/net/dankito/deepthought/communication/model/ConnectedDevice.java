package net.dankito.deepthought.communication.model;

import net.dankito.deepthought.communication.Constants;
import net.dankito.deepthought.data.model.Device;
import net.dankito.deepthought.data.model.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ganymed on 20/08/15.
 */
public class ConnectedDevice extends HostInfo {

  private final static Logger log = LoggerFactory.getLogger(ConnectedDevice.class);


  protected int synchronizationPort = Constants.SynchronizationDefaultPort;

  protected Object securityToken = null; // TODO: implement a Security feature to ensure that it's really the registered device that likes to connect

  protected boolean hasCaptureDevice = false;

  protected boolean canDoOcr = false;

  protected boolean canScanBarcodes = false;

  protected Device device = null;


  public ConnectedDevice(String deviceId, String address, int messagesPort) {
    this.deviceUniqueId = deviceId;
    this.address = address;
    this.messagesPort = messagesPort;
  }

  public ConnectedDevice(String userUniversallyUniqueId, String userName, String deviceDatabaseId, String deviceUniversallyUniqueId, String deviceName, String platform, String osVersion,
                         String platformArchitecture, int countSynchronizingDevices) {
    super(userUniversallyUniqueId, userName, deviceDatabaseId, deviceUniversallyUniqueId, deviceName, platform, osVersion, platformArchitecture, countSynchronizingDevices);
  }


  public int getSynchronizationPort() {
    return synchronizationPort;
  }

  public void setSynchronizationPort(int synchronizationPort) {
    this.synchronizationPort = synchronizationPort;
  }

  public boolean hasCaptureDevice() {
    return hasCaptureDevice;
  }

  public void setHasCaptureDevice(boolean hasCaptureDevice) {
    this.hasCaptureDevice = hasCaptureDevice;
  }

  public boolean canDoOcr() {
    return canDoOcr;
  }

  public void setCanDoOcr(boolean canDoOcr) {
    this.canDoOcr = canDoOcr;
  }

  public boolean canScanBarcodes() {
    return canScanBarcodes;
  }

  public void setCanScanBarcodes(boolean canScanBarcodes) {
    this.canScanBarcodes = canScanBarcodes;
  }

  public Device getDevice() {
    return device;
  }

  public void setDevice(Device device) {
    this.device = device;
  }


  @Override
  public String toString() {
    return address;
  }


  public void setStoredDeviceInstance(User loggedOnUser) {
    for(Device userDevice : loggedOnUser.getDevices()) {
      if(getDeviceUniqueId().equals(userDevice.getUniversallyUniqueId())) {
        setDevice(userDevice);
        return;
      }
    }

    // TODO: this happens on Server side on each new Device Registration (as there the remote device is not added yet to logged on User's devices)
    log.error("Could not find local device with unique id " + getDeviceUniqueId() + ". But this happens on Server side on each new Device Registration");
  }


  public static ConnectedDevice fromUserAndDevice(User user, Device device, String localHostIpAddress, int messagesPort, int synchronizationPort) {
    ConnectedDevice connectedDevice = new ConnectedDevice(user.getUniversallyUniqueId(), user.getUserName(), device.getId(), device.getUniversallyUniqueId(), device.getName(),
        device.getPlatform(), device.getOsVersion(), device.getPlatformArchitecture(), device.getCountSynchronizingDevices());

    connectedDevice.setAddress(localHostIpAddress);
    connectedDevice.setMessagesPort(messagesPort);
    connectedDevice.setSynchronizationPort(synchronizationPort);

    return connectedDevice;
  }

}

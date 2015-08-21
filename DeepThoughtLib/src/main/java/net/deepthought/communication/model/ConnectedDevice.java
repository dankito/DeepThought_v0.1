package net.deepthought.communication.model;

import net.deepthought.data.model.Device;

/**
 * Created by ganymed on 20/08/15.
 */
public class ConnectedDevice {

  protected String uniqueDeviceId;

  protected String address;

  protected int messagesPort;

  protected Object securityToken = null; // TODO: implement a Security feature to ensure that it's really the registered device that likes to connect

  protected boolean hasCaptureDevice = false;

  protected boolean canDoOcr = false;

  protected Device device = null;


  public ConnectedDevice(String uniqueDeviceId, String address, int messagesPort) {
    this.uniqueDeviceId = uniqueDeviceId;
    this.address = address;
    this.messagesPort = messagesPort;
  }

  public ConnectedDevice(String uniqueDeviceId, String address, int messagesPort, boolean hasCaptureDevice, boolean canDoOcr) {
    this(uniqueDeviceId, address, messagesPort);
    this.hasCaptureDevice = hasCaptureDevice;
    this.canDoOcr = canDoOcr;
  }


  public String getUniqueDeviceId() {
    return uniqueDeviceId;
  }

  public String getAddress() {
    return address;
  }

  public int getMessagesPort() {
    return messagesPort;
  }

  public boolean isHasCaptureDevice() {
    return hasCaptureDevice;
  }

  public void setHasCaptureDevice(boolean hasCaptureDevice) {
    this.hasCaptureDevice = hasCaptureDevice;
  }

  public boolean isCanDoOcr() {
    return canDoOcr;
  }

  public void setCanDoOcr(boolean canDoOcr) {
    this.canDoOcr = canDoOcr;
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

}

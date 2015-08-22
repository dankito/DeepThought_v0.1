package net.deepthought.communication;

import net.deepthought.Application;
import net.deepthought.communication.model.ConnectedDevice;
import net.deepthought.communication.model.HostInfo;
import net.deepthought.data.model.Device;
import net.deepthought.data.model.User;
import net.deepthought.data.persistence.deserializer.DeserializationResult;
import net.deepthought.data.persistence.json.JsonIoJsonHelper;
import net.deepthought.data.persistence.serializer.SerializationResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ganymed on 19/08/15.
 */
public class ConnectorMessagesCreator {

  public final static String LookingForRegistrationServerMessageHeader = "Looking for Registration Server";
  public final static String OpenRegistrationServerInfoMessageHeader = "Open Registration Server Info";

  public final static String SearchingForRegisteredDevicesMessage = "Searching for Registered Devices";
  public final static String RegisteredDeviceFoundMessage = "Registered Device Found";


  private final static Logger log = LoggerFactory.getLogger(ConnectorMessagesCreator.class);


  public byte[] createLookingForRegistrationServerMessage() {
    return createMessage(LookingForRegistrationServerMessageHeader, createHostInfoMessageString());
  }

  public boolean isLookingForRegistrationServerMessage(byte[] receivedBytes, int packetLength) {
    String receivedMessage = parseBytesToString(receivedBytes, packetLength);
    return receivedMessage.startsWith(LookingForRegistrationServerMessageHeader);
  }

  public byte[] createOpenRegistrationServerInfoMessage() {
    return createMessage(OpenRegistrationServerInfoMessageHeader, createHostInfoMessageString());
  }

  public boolean isOpenRegistrationServerInfoMessage(byte[] receivedBytes, int packetLength) {
    String receivedMessage = parseBytesToString(receivedBytes, packetLength);
    return receivedMessage.startsWith(OpenRegistrationServerInfoMessageHeader);
  }

  public byte[] createSearchingForRegisteredDevicesMessage() {
    return createMessage(SearchingForRegisteredDevicesMessage, createHostInfoMessageString());
  }

  public boolean isSearchingForRegisteredDevicesMessage(byte[] receivedBytes, int packetLength) {
    String receivedMessage = parseBytesToString(receivedBytes, packetLength);
    return receivedMessage.startsWith(SearchingForRegisteredDevicesMessage);
  }

  public byte[] createRegisteredDeviceFoundMessage() {
    return createMessage(RegisteredDeviceFoundMessage, createConnectedDeviceMessageString());
  }

  public boolean isRegisteredDeviceFoundMessage(byte[] receivedBytes, int packetLength) {
    String receivedMessage = parseBytesToString(receivedBytes, packetLength);
    return receivedMessage.startsWith(RegisteredDeviceFoundMessage);
  }

  public HostInfo getHostInfoFromMessage(byte[] receivedBytes, int packetLength) {
    String messageBody = getMessageBodyFromMessage(receivedBytes, packetLength);
    DeserializationResult<HostInfo> result = JsonIoJsonHelper.parseJsonString(messageBody, HostInfo.class);
    if(result.successful())
      return result.getResult();

    log.error("Could not deserialize message body " + messageBody + " to HostInfo", result.getError());
    return null;
  }

  public ConnectedDevice getConnectedDeviceFromMessage(byte[] receivedBytes, int packetLength) {
    String messageBody = getMessageBodyFromMessage(receivedBytes, packetLength);
    DeserializationResult<ConnectedDevice> result = JsonIoJsonHelper.parseJsonString(messageBody, ConnectedDevice.class);
    if(result.successful()) {
      ConnectedDevice device = result.getResult();
      for(Device userDevice : Application.getLoggedOnUser().getDevices()) {
        if(device.getUniqueDeviceId().equals(userDevice.getUniversallyUniqueId())) {
          device.setDevice(userDevice);
          return device;
        }
      }

      log.error("Could not find local device with unique id " + device.getUniqueDeviceId());
      return result.getResult();
    }

    log.error("Could not deserialize message body " + messageBody + " to ConnectedDevice", result.getError());
    return null;
  }


  protected String parseBytesToString(byte[] receivedBytes, int packetLength) {
    return new String(receivedBytes, 0, packetLength, Constants.MessagesCharset);
  }

  protected byte[] createMessage(String messageHeader, String messageBody) {
    String messageString = createMessageString(messageHeader, messageBody);
    return messageString.getBytes(Constants.MessagesCharset);
  }

  protected String createMessageString(String messageHeader, String messageBody) {
    return messageHeader + ":" + messageBody;
  }

  protected String getMessageBodyFromMessage(byte[] receivedBytes, int packetLength) {
    String receivedMessage = parseBytesToString(receivedBytes, packetLength);
    int index = receivedMessage.indexOf(':');
    return receivedMessage.substring(index + 1);
  }

  protected String createHostInfoMessageString() {
    return createHostInfoMessageString(Application.getLoggedOnUser(), Application.getApplication().getLocalDevice());
  }

  protected String createHostInfoMessageString(User loggedOnUser, Device localDevice) {
    HostInfo hostInfo = HostInfo.fromUserAndDevice(loggedOnUser, localDevice);
    hostInfo.setIpAddress(NetworkHelper.getIPAddressString(true));
    hostInfo.setPort(Application.getDeepThoughtsConnector().getMessageReceiverPort());

    SerializationResult result = JsonIoJsonHelper.generateJsonString(hostInfo);
    if(result.successful()) {
      return result.getSerializationResult();
    }

    log.error("Could not serialize HostInfo " + hostInfo, result.getError());
    return "";
  }

  protected String createConnectedDeviceMessageString() {
    // TODO: how to determine if host can capture images? i need a device independent solution
    ConnectedDevice device = new ConnectedDevice(Application.getApplication().getLocalDevice().getUniversallyUniqueId(), NetworkHelper.getIPAddressString(true),
        Application.getDeepThoughtsConnector().getMessageReceiverPort(), false, Application.getContentExtractorManager().hasOcrContentExtractors());

    return createConnectedDeviceMessageString(device);
  }

  protected String createConnectedDeviceMessageString(ConnectedDevice device) {
    SerializationResult result = JsonIoJsonHelper.generateJsonString(device);
    if(result.successful()) {
      return result.getSerializationResult();
    }

    log.error("Could not serialize ConnectedDevice " + device, result.getError());
    return "";
  }

}

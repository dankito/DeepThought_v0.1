package net.dankito.deepthought.communication;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.communication.model.ConnectedDevice;
import net.dankito.deepthought.communication.model.HostInfo;
import net.dankito.deepthought.data.persistence.deserializer.DeserializationResult;
import net.dankito.deepthought.data.persistence.json.JsonIoJsonHelper;
import net.dankito.deepthought.data.persistence.serializer.SerializationResult;
import net.dankito.deepthought.util.OsHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.DatagramPacket;
import java.net.InetAddress;

/**
 * Created by ganymed on 19/08/15.
 */
public class ConnectorMessagesCreator {

  public final static String LookingForRegistrationServerMessageHeader = "Looking for Registration Server";
  public final static String OpenRegistrationServerInfoMessageHeader = "Open Registration Server Info";

  public final static String SearchingForDevicesMessage = "Searching for Devices";
  public final static String RegisteredDeviceFoundMessage = "Registered Device Found";

  public static final String MultipartKeyAddress = "address";
  public static final String MultipartKeyPort = "port";
  public static final String MultipartKeyMessageId = "message_id";


  public static final String ImportFilesMultipartKeyConfiguration = "configuration";

  public static final String DoOcrMultipartKeyConfiguration = "configuration";
  public static final String DoOcrMultipartKeyImage = "image";

  public static final String ImportFilesResultMultipartKeyRequestMessageId = "request_message_id";
  public static final String ImportFilesResultMultipartKeyResponse = "response";
  public static final String ImportFilesResultMultipartKeyImage = "image";


  private final static Logger log = LoggerFactory.getLogger(ConnectorMessagesCreator.class);


  protected ConnectorMessagesCreatorConfig config;


  public ConnectorMessagesCreator(ConnectorMessagesCreatorConfig config) {
    this.config = config;
  }


  public byte[] createSearchingForDevicesMessage(HostInfo hostInfo) {
    return createMessage(SearchingForDevicesMessage, createHostInfoMessageString(hostInfo));
  }

  public boolean isSearchingForDevicesMessage(byte[] receivedBytes, int packetLength) {
    String receivedMessage = parseBytesToString(receivedBytes, packetLength);
    return receivedMessage.startsWith(SearchingForDevicesMessage);
  }

  public HostInfo getHostInfoFromMessage(byte[] receivedBytes, DatagramPacket packet) {
    String messageBody = getMessageBodyFromMessage(receivedBytes, packet.getLength());
    DeserializationResult<HostInfo> result = JsonIoJsonHelper.parseJsonString(messageBody, HostInfo.class);
    if(result.successful()) {
      HostInfo hostInfo = result.getResult();
      hostInfo.setAddress(packet.getAddress().getHostAddress());
      return hostInfo;
    }

    log.error("Could not deserialize message body " + messageBody + " to HostInfo", result.getError());
    return null;
  }

  public ConnectedDevice getConnectedDeviceFromMessage(byte[] receivedBytes, int packetLength, InetAddress address) {
    String messageBody = getMessageBodyFromMessage(receivedBytes, packetLength);
    DeserializationResult<ConnectedDevice> result = JsonIoJsonHelper.parseJsonString(messageBody, ConnectedDevice.class);

    if(result.successful()) {
      ConnectedDevice device = result.getResult();
      device.setAddress(address.getHostAddress());
      device.setStoredDeviceInstance(config.getLoggedOnUser());

      return device;
    }

    log.error("Could not deserialize message body " + messageBody + " to ConnectedDevice", result.getError());
    return null;
  }


  protected String parseBytesToString(byte[] receivedBytes, int packetLength) {
    if(OsHelper.isRunningOnJavaSeOrOnAndroidApiLevelAtLeastOf(9))
      return new String(receivedBytes, 0, packetLength, Constants.MessagesCharset);
    else  {
      try {
        return new String(receivedBytes, 0, packetLength, Constants.MessagesCharsetName);
      } catch (Exception ex) { log.error("Could not create String from byte array for Charset " + Constants.MessagesCharset, ex); }
      return "";
    }
  }

  protected byte[] createMessage(String messageHeader, String messageBody) {
    String messageString = createMessageString(messageHeader, messageBody);
    if(OsHelper.isRunningOnJavaSeOrOnAndroidApiLevelAtLeastOf(9))
      return messageString.getBytes(Constants.MessagesCharset);
    else {
      try {
        return messageString.getBytes(Constants.MessagesCharsetName);
      } catch (Exception ex) { log.error("Could not create byte array for Charset " + Constants.MessagesCharset + " from message " + messageString, ex); }
      return new byte[0];
    }
  }

  protected String createMessageString(String messageHeader, String messageBody) {
    return messageHeader + ":" + messageBody;
  }

  protected String getMessageBodyFromMessage(byte[] receivedBytes, int packetLength) {
    String receivedMessage = parseBytesToString(receivedBytes, packetLength);
    int index = receivedMessage.indexOf(':');
    return receivedMessage.substring(index + 1);
  }

  protected String createHostInfoMessageString(HostInfo hostInfo) {
    SerializationResult result = JsonIoJsonHelper.generateJsonString(hostInfo);
    if(result.successful()) {
      return result.getSerializationResult();
    }

    log.error("Could not serialize HostInfo " + hostInfo, result.getError());
    return "";
  }

  protected String createConnectedDeviceMessageString() {
    ConnectedDevice device = getLocalHostDevice();

    return createConnectedDeviceMessageString(device);
  }

  public ConnectedDevice getLocalHostDevice() {
    ConnectedDevice localHost = new ConnectedDevice(config.getLocalDevice().getUniversallyUniqueId(), config.getLocalHostIpAddress(), config.getMessageReceiverPort());

    // TODO: try to get rid of static method calls
    if(Application.getPlatformConfiguration() != null) {
      localHost.setHasCaptureDevice(Application.getPlatformConfiguration().hasCaptureDevice());
      localHost.setCanScanBarcodes(Application.getPlatformConfiguration().canScanBarcodes());
    }
    if(Application.getContentExtractorManager() != null) {
      localHost.setCanDoOcr(Application.getContentExtractorManager().hasOcrContentExtractors());
    }

    return localHost;
  }

  protected String createConnectedDeviceMessageString(ConnectedDevice device) {
    SerializationResult result = JsonIoJsonHelper.generateJsonString(device);
    if(result.successful()) {
      return result.getSerializationResult();
    }

    log.error("Could not serialize ConnectedDevice " + device, result.getError());
    return "";
  }


  public ConnectorMessagesCreatorConfig getConfig() {
    return config;
  }

}

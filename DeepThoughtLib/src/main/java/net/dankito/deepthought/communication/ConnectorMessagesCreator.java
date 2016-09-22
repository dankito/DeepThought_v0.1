package net.dankito.deepthought.communication;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.communication.model.ConnectedDevice;
import net.dankito.deepthought.communication.model.HostInfo;
import net.dankito.deepthought.data.model.Device;
import net.dankito.deepthought.data.model.User;
import net.dankito.deepthought.data.persistence.deserializer.DeserializationResult;
import net.dankito.deepthought.data.persistence.json.JsonIoJsonHelper;
import net.dankito.deepthought.data.persistence.serializer.SerializationResult;
import net.dankito.deepthought.util.OsHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ganymed on 19/08/15.
 */
public class ConnectorMessagesCreator implements ICommunicationConfigurationManager {

  public final static String SearchingForDevicesMessage = "Searching for Devices";

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


  protected User loggedOnUser;

  protected Device localDevice;

  protected String localHostIpAddress;

  protected int messageReceiverPort;

  protected int synchronizationPort;

  protected ConnectedDevice cachedLocalHost = null;

  protected Map<InetAddress, DatagramPacket> cachedSearchDevicesDatagramPackets = new ConcurrentHashMap<>();


  public ConnectorMessagesCreator(User loggedOnUser, Device localDevice, String localHostIpAddress, int messageReceiverPort, int synchronizationPort) {
    this.loggedOnUser = loggedOnUser;
    this.localDevice = localDevice;
    this.localHostIpAddress = localHostIpAddress;
    this.messageReceiverPort = messageReceiverPort;
    this.synchronizationPort = synchronizationPort;
  }


  public DatagramPacket getSearchDevicesDatagramPacket(InetAddress broadcastAddress, int searchDevicesPort) {
    DatagramPacket cachedPacket = cachedSearchDevicesDatagramPackets.get(broadcastAddress);

    if(cachedPacket == null) {
      byte[] message = createSearchingForDevicesMessage(getLocalHostDevice());
      cachedPacket = new DatagramPacket(message, message.length, broadcastAddress, searchDevicesPort);

      cachedSearchDevicesDatagramPackets.put(broadcastAddress, cachedPacket);
    }

    return cachedPacket;
  }

  protected byte[] createSearchingForDevicesMessage(HostInfo hostInfo) {
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

  @Override
  public ConnectedDevice getLocalHostDevice() {
    synchronized(this) {
      if(cachedLocalHost == null) {
        cachedLocalHost = ConnectedDevice.fromUserAndDevice(loggedOnUser, localDevice, localHostIpAddress, messageReceiverPort, synchronizationPort);

        // TODO: try to get rid of static method calls
        if(Application.getPlatformConfiguration() != null) {
          cachedLocalHost.setHasCaptureDevice(Application.getPlatformConfiguration().hasCaptureDevice());
          cachedLocalHost.setCanScanBarcodes(Application.getPlatformConfiguration().canScanBarcodes());
        }
        if(Application.getContentExtractorManager() != null) {
          cachedLocalHost.setCanDoOcr(Application.getContentExtractorManager().hasOcrContentExtractors());
        }
      }
    }

    return cachedLocalHost;
  }

  public boolean equalsLocalHostDevice(HostInfo remoteHost) {
    ConnectedDevice localHost = getLocalHostDevice();

    return localHost.getUserUniqueId().equals(remoteHost.getUserUniqueId()) &&
        localHost.getDeviceUniqueId().equals(remoteHost.getDeviceUniqueId()) &&
        localHost.getAddress().equals(remoteHost.getAddress());
  }


  @Override
  public void setLoggedOnUser(User loggedOnUser) {
    this.loggedOnUser = loggedOnUser;

    resetCachedLocalHostInstances();
  }

  @Override
  public void setLocalDevice(Device localDevice) {
    this.localDevice = localDevice;

    resetCachedLocalHostInstances();
  }

  @Override
  public void setLocalHostIpAddress(String localHostIpAddress) {
    this.localHostIpAddress = localHostIpAddress;

    resetCachedLocalHostInstances();
  }

  @Override
  public void setMessageReceiverPort(int messageReceiverPort) {
    this.messageReceiverPort = messageReceiverPort;

    resetCachedLocalHostInstances();
  }

  @Override
  public void setSynchronizationPort(int synchronizationPort) {
    this.synchronizationPort = synchronizationPort;

    resetCachedLocalHostInstances();
  }

  protected void resetCachedLocalHostInstances() {
    synchronized(this) {
      cachedLocalHost = null;

      cachedSearchDevicesDatagramPackets.clear();
    }
  }

}

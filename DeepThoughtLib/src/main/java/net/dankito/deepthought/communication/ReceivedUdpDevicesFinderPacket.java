package net.dankito.deepthought.communication;

import java.net.DatagramPacket;

/**
 * Created by ganymed on 20/10/16.
 */
public class ReceivedUdpDevicesFinderPacket {

  protected byte[] receivedData;

  protected DatagramPacket packet;

  protected String senderAddress;

  protected ConnectorMessagesCreator messagesCreator;

  protected IDevicesFinderListener listener;


  public ReceivedUdpDevicesFinderPacket(byte[] receivedData, DatagramPacket packet, String senderAddress, ConnectorMessagesCreator messagesCreator, IDevicesFinderListener listener) {
    this.receivedData = receivedData;
    this.packet = packet;
    this.senderAddress = senderAddress;
    this.messagesCreator = messagesCreator;
    this.listener = listener;
  }


  public byte[] getReceivedData() {
    return receivedData;
  }

  public DatagramPacket getPacket() {
    return packet;
  }

  public String getSenderAddress() {
    return senderAddress;
  }

  public ConnectorMessagesCreator getMessagesCreator() {
    return messagesCreator;
  }

  public IDevicesFinderListener getListener() {
    return listener;
  }

}

package net.dankito.deepthought.communication;

import java.net.DatagramPacket;

/**
 * Created by ganymed on 20/10/16.
 */
public class ReceivedUdpDevicesFinderPacket {

  protected byte[] buffer;

  protected DatagramPacket packet;

  protected String senderAddress;

  protected ConnectorMessagesCreator messagesCreator;

  protected IDevicesFinderListener listener;


  public ReceivedUdpDevicesFinderPacket(byte[] buffer, DatagramPacket packet, String senderAddress, ConnectorMessagesCreator messagesCreator, IDevicesFinderListener listener) {
    this.buffer = buffer;
    this.packet = packet;
    this.senderAddress = senderAddress;
    this.messagesCreator = messagesCreator;
    this.listener = listener;
  }


  public byte[] getBuffer() {
    return buffer;
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

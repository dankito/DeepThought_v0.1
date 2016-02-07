package net.deepthought.communication.messages.request;

/**
 * Created by ganymed on 23/08/15.
 */
public class RequestWithAsynchronousResponse extends Request {

  protected static int MessageId = 0;


  protected int messageId; // to be able to assign Server Response to Request

  protected String address;

  protected int port;


  protected RequestWithAsynchronousResponse() {
    this(getNextMessageId());
  }

  protected RequestWithAsynchronousResponse(int messageId) {
    this.messageId = messageId;
  }

  public RequestWithAsynchronousResponse(String address, int port) {
    this();

    this.address = address;
    this.port = port;
  }

  public RequestWithAsynchronousResponse(int messageId, String address, int port) {
    this(messageId);

    this.address = address;
    this.port = port;
  }



  public int getMessageId() {
    return messageId;
  }

  public void setMessageId(int messageId) {
    this.messageId = messageId;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }


  protected static int getNextMessageId() {
    return MessageId++;
  }

}

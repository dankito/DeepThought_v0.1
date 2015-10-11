package net.deepthought.communication.messages;

/**
 * Created by ganymed on 23/08/15.
 */
public class RequestWithAsynchronousResponse extends Request {

  protected static int MessageId = 0;


  protected String address;

  protected int port;

  protected int messageId; // to be able to assign Server Response to Request


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


  public String getAddress() {
    return address;
  }

  public int getPort() {
    return port;
  }

  public int getMessageId() {
    return messageId;
  }



  protected static int getNextMessageId() {
    return MessageId++;
  }

}

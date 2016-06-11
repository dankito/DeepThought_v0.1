package net.dankito.deepthought.communication.messages.request;

import net.dankito.deepthought.communication.ConnectorMessagesCreator;
import net.dankito.deepthought.communication.messages.MultipartPart;
import net.dankito.deepthought.communication.messages.MultipartType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by ganymed on 05/10/15.
 */
public class MultipartRequest extends RequestWithAsynchronousResponse {

  protected List<MultipartPart> parts = null;


  protected MultipartRequest() { // for sub classes
    this("", 0);
  }

  public MultipartRequest(String address, int port) {
    this(address, port, new ArrayList<MultipartPart>());
  }

  public MultipartRequest(String address, int port, MultipartPart[] parts) {
    this(address, port, new ArrayList<MultipartPart>(Arrays.asList(parts)));
  }

  public MultipartRequest(String address, int port, List<MultipartPart> parts) {
    this(getNextMessageId(), address, port, parts);
  }

  public MultipartRequest(int messageId, String address, int port) {
    this(messageId, address, port, new ArrayList<MultipartPart>());
  }

  public MultipartRequest(int messageId, String address, int port, MultipartPart[] parts) {
    this(messageId, address, port, new ArrayList<MultipartPart>(Arrays.asList(parts)));
  }

  public MultipartRequest(int messageId, String address, int port, List<MultipartPart> parts) {
    super(messageId, address, port);
    this.parts = parts;

    // TODO: wouldn't it be better to create one part (Host / MultipartInfo) instead of three?
    addPart(new MultipartPart<String>(ConnectorMessagesCreator.MultipartKeyAddress, MultipartType.Text, address));
    addPart(new MultipartPart<String>(ConnectorMessagesCreator.MultipartKeyPort, MultipartType.Text, Integer.toString(port)));
    addPart(new MultipartPart<String>(ConnectorMessagesCreator.MultipartKeyMessageId, MultipartType.Text, Integer.toString(messageId)));
  }


  public List<MultipartPart> getParts() {
    return parts;
  }

  public boolean addPart(MultipartPart part) {
    return parts.add(part);
  }

}

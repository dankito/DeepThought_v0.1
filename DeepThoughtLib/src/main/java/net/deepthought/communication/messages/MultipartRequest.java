package net.deepthought.communication.messages;

import net.deepthought.communication.ConnectorMessagesCreator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by ganymed on 05/10/15.
 */
public class MultipartRequest extends RequestWithAsynchronousResponse {

  protected List<MultipartPart> parts = null;


  public MultipartRequest(String address, int port) {
    this(address, port, new ArrayList<MultipartPart>());
  }

  public MultipartRequest(String address, int port, MultipartPart[] parts) {
    this(address, port, new ArrayList<MultipartPart>(Arrays.asList(parts)));
  }

  public MultipartRequest(String address, int port, List<MultipartPart> parts) {
    this(getNextMessageId(), address, port, parts);
  }

  public MultipartRequest(int messageId, String address, int port, MultipartPart[] parts) {
    this(messageId, address, port, new ArrayList<MultipartPart>(Arrays.asList(parts)));
  }

  public MultipartRequest(int messageId, String address, int port, List<MultipartPart> parts) {
    super(messageId, address, port);
    this.parts = parts;

    parts.add(new MultipartPart<String>(ConnectorMessagesCreator.MultipartKeyAddress, MultipartType.Text, address));
    parts.add(new MultipartPart<String>(ConnectorMessagesCreator.MultipartKeyPort, MultipartType.Text, Integer.toString(port)));
    parts.add(new MultipartPart<String>(ConnectorMessagesCreator.MultipartKeyMessageId, MultipartType.Text, Integer.toString(messageId)));
  }


  public List<MultipartPart> getParts() {
    return parts;
  }

}

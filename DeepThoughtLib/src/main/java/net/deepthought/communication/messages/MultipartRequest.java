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
    super(address, port);
    this.parts = parts;

    parts.add(new MultipartPart<String>(ConnectorMessagesCreator.DoOcrMultipartKeyAddress, MultipartType.Text, address));
    parts.add(new MultipartPart<String>(ConnectorMessagesCreator.DoOcrMultipartKeyPort, MultipartType.Text, Integer.toString(port)));
  }


  public List<MultipartPart> getParts() {
    return parts;
  }

}

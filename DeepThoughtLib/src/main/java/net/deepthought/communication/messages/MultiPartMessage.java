package net.deepthought.communication.messages;

import net.deepthought.communication.messages.request.Request;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ganymed on 20/11/15.
 */
public class MultiPartMessage extends Request {

  protected String address;

  protected int port;

  protected int messageId;

  protected Map<String, Object> deserializedParts = new HashMap<>();


}

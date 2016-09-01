package net.dankito.deepthought.communication;

import org.apache.http.protocol.HTTP;

import java.nio.charset.Charset;

/**
 * Created by ganymed on 19/08/15.
 */
public class Constants {

  public final static int MessageHandlerDefaultPort = 27388;

  public final static int SynchronizationDefaultPort = 27387;

  public final static int SearchDevicesListenerPort = 27384;

  /**
   * Interval at which a Broadcast that we're alive is sent in milliseconds
   */
  public final static int SendWeAreAliveMessageInterval = 500;


  public final static Charset MessagesCharset = Charset.forName("utf8");
  public final static String MessagesCharsetName = HTTP.UTF_8;

  public final static String JsonMimeType = "application/json";


}

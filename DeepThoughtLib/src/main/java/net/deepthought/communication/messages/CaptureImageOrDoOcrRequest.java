package net.deepthought.communication.messages;

/**
 * Created by ganymed on 23/08/15.
 */
public class CaptureImageOrDoOcrRequest extends RequestWithAsynchronousResponse {

  protected boolean captureImage;

  protected boolean doOcr = false;


  public CaptureImageOrDoOcrRequest(String ipAddress, int port, boolean captureImage) {
    super(ipAddress, port);
    this.captureImage = captureImage;
  }

  public CaptureImageOrDoOcrRequest(String ipAddress, int port, boolean captureImage, boolean doOcr) {
    super(ipAddress, port);
    this.captureImage = captureImage;
    this.doOcr = doOcr;
  }


  public boolean captureImage() {
    return captureImage;
  }

  public boolean doOcr() {
    return doOcr;
  }

}

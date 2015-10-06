package net.deepthought.communication.messages;

import net.deepthought.communication.model.DoOcrConfiguration;

import java.io.IOException;

/**
 * Created by ganymed on 23/08/15.
 */
public class CaptureImageOrDoOcrRequest extends RequestWithAsynchronousResponse {

  protected boolean captureImage;
  protected boolean doOcr;

  protected DoOcrConfiguration configuration = null;


  public CaptureImageOrDoOcrRequest(String ipAddress, int port, boolean captureImage) {
    this(ipAddress, port, captureImage, false);
  }

  public CaptureImageOrDoOcrRequest(String ipAddress, int port, boolean captureImage, boolean doOcr) {
    super(ipAddress, port);
    this.captureImage = captureImage;
    this.doOcr = doOcr;
  }

  public CaptureImageOrDoOcrRequest(String ipAddressString, int messageReceiverPort, DoOcrConfiguration configuration) {
    this(ipAddressString, messageReceiverPort, false, true);
    this.configuration = configuration;
  }


  public boolean captureImage() {
    return captureImage;
  }

  public boolean doOcr() {
    return doOcr;
  }

  public DoOcrConfiguration getConfiguration() {
    return configuration;
  }

  public byte[] getImageToRecognize() {
    return configuration.getImageToRecognize();
  }

  public byte[] readBytesFromImageUri() throws IOException {
    return configuration.readBytesFromImageUri();
  }

  public boolean showSettingsUi() {
    return configuration.showSettingsUi();
  }

  public boolean showMessageOnRemoteDeviceWhenProcessingDone() {
    return configuration.showMessageOnRemoteDeviceWhenProcessingDone();
  }

}

package net.deepthought.communication.messages;

import net.deepthought.communication.model.CaptureImageOrDoOcrConfiguration;

/**
 * Created by ganymed on 23/08/15.
 */
public class CaptureImageOrDoOcrRequest extends RequestWithAsynchronousResponse {

  protected CaptureImageOrDoOcrConfiguration configuration = null;


  public CaptureImageOrDoOcrRequest(String ipAddress, int port, boolean captureImage) {
    super(ipAddress, port);
    this.configuration = new CaptureImageOrDoOcrConfiguration(null, captureImage, false);
  }

  public CaptureImageOrDoOcrRequest(String ipAddress, int port, boolean captureImage, boolean doOcr) {
    super(ipAddress, port);
    this.configuration = new CaptureImageOrDoOcrConfiguration(null, captureImage, doOcr);
  }

  public CaptureImageOrDoOcrRequest(String ipAddressString, int messageReceiverPort, CaptureImageOrDoOcrConfiguration configuration) {
    super(ipAddressString, messageReceiverPort);
    this.configuration = configuration;
  }


  public CaptureImageOrDoOcrConfiguration getConfiguration() {
    return configuration;
  }

  public boolean captureImage() {
    return configuration.captureImage();
  }

  public boolean doOcr() {
    return configuration.doOcr();
  }

  public byte[] getImageToRecognize() {
    return configuration.getImageToRecognize();
  }

  public boolean showSettingsUi() {
    return configuration.showSettingsUi();
  }

  public boolean showMessageOnRemoteDeviceWhenProcessingDone() {
    return configuration.showMessageOnRemoteDeviceWhenProcessingDone();
  }

}

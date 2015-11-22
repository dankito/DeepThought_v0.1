package net.deepthought.communication.messages.request;

import net.deepthought.communication.ConnectorMessagesCreator;
import net.deepthought.communication.messages.MultipartPart;
import net.deepthought.communication.model.DoOcrConfiguration;

/**
 * Created by ganymed on 23/08/15.
 */
public class CaptureImageOrDoOcrRequest extends MultipartRequest {

  protected boolean captureImage;
  protected boolean doOcr;

  protected DoOcrConfiguration configuration = null;


  public CaptureImageOrDoOcrRequest() { // for Reflection
    this("", 0, false);
  }

  public CaptureImageOrDoOcrRequest(String ipAddress, int port, boolean captureImage) {
    this(ipAddress, port, captureImage, false);
  }

  public CaptureImageOrDoOcrRequest(String ipAddress, int port, boolean captureImage, boolean doOcr) {
    this(getNextMessageId(), ipAddress, port, captureImage, doOcr);
    this.captureImage = captureImage;
    this.doOcr = doOcr;
  }

  public CaptureImageOrDoOcrRequest(int messageId, String ipAddress, int port, boolean captureImage, boolean doOcr) {
    super(messageId, ipAddress, port);
    this.captureImage = captureImage;
    this.doOcr = doOcr;
  }

  public CaptureImageOrDoOcrRequest(String ipAddressString, int messageReceiverPort, DoOcrConfiguration configuration) {
    this(ipAddressString, messageReceiverPort, false, true);
    this.configuration = configuration;
  }

  public CaptureImageOrDoOcrRequest(int messageId, String ipAddressString, int messageReceiverPort, DoOcrConfiguration configuration) {
    this(messageId, ipAddressString, messageReceiverPort, false, true);

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

  public boolean showSettingsUi() {
    return configuration.showSettingsUi();
  }

  public boolean showMessageOnRemoteDeviceWhenProcessingDone() {
    return configuration.showMessageOnRemoteDeviceWhenProcessingDone();
  }


  @Override
  public boolean addPart(MultipartPart part) {
    if(ConnectorMessagesCreator.DoOcrMultipartKeyConfiguration.equals(part.getPartName()) && part.getData() instanceof DoOcrConfiguration) {
      this.configuration = (DoOcrConfiguration)part.getData();
    }
    else if(ConnectorMessagesCreator.DoOcrMultipartKeyImage.equals(part.getPartName())) {
      if(part.getData() instanceof String && configuration != null) {
        configuration.setImageUri((String)part.getData());
      }
    }

    return super.addPart(part);
  }
}

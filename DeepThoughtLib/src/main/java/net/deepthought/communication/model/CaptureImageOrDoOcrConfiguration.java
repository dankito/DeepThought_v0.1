package net.deepthought.communication.model;

/**
 * Created by ganymed on 04/10/15.
 */
public class CaptureImageOrDoOcrConfiguration {

  protected ConnectedDevice deviceToDoTheJob;

  protected boolean captureImage;
  protected boolean doOcr;

  protected byte[] imageToRecognize = null;

  protected boolean showSettingsUi = true;

  protected boolean showMessageOnRemoteDeviceWhenProcessingDone = false;


  public CaptureImageOrDoOcrConfiguration(ConnectedDevice deviceToDoTheJob, boolean captureImage, boolean doOcr) {
    this.deviceToDoTheJob = deviceToDoTheJob;
    this.captureImage = captureImage;
    this.doOcr = doOcr;
  }

  public CaptureImageOrDoOcrConfiguration(ConnectedDevice deviceToDoTheJob, byte[] imageToRecognize) {
    this(deviceToDoTheJob, imageToRecognize, true);
  }

  public CaptureImageOrDoOcrConfiguration(ConnectedDevice deviceToDoTheJob, byte[] imageToRecognize, boolean showSettingsUi) {
    this(deviceToDoTheJob, imageToRecognize, showSettingsUi, false);
  }

  public CaptureImageOrDoOcrConfiguration(ConnectedDevice deviceToDoTheJob, byte[] imageToRecognize, boolean showSettingsUi, boolean showMessageOnRemoteDeviceWhenProcessingDone) {
    this.deviceToDoTheJob = deviceToDoTheJob;
    this.imageToRecognize = imageToRecognize;
    this.showSettingsUi = showSettingsUi;
    this.showMessageOnRemoteDeviceWhenProcessingDone = showMessageOnRemoteDeviceWhenProcessingDone;

    this.captureImage = false;
    this.doOcr = true;
  }


  public ConnectedDevice getDeviceToDoTheJob() {
    return deviceToDoTheJob;
  }

  public boolean captureImage() {
    return captureImage;
  }

  public boolean doOcr() {
    return doOcr;
  }

  public byte[] getImageToRecognize() {
    return imageToRecognize;
  }

  public boolean showSettingsUi() {
    return showSettingsUi;
  }

  public boolean showMessageOnRemoteDeviceWhenProcessingDone() {
    return showMessageOnRemoteDeviceWhenProcessingDone;
  }

}
